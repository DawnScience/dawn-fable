/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.dawb.fabio.FabioFileException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import fable.framework.toolbox.IEdfVarKeys;

public class EdfFile {

	// Attributes
	private HashMap<String, String> header;
	private String fileName;
	private Vector<String> vKeysInHeader; // a list of keys as they arrive in
	// the header; useful in edfViewer
	private byte[] buffer = null;
	// private byte[] imageAsByte = null;
	private int[] imageAsInt = null;
	// private ImageData imgData;
	private int headerEnd = 0;
	private int skipbytes;
	private int width, height;
	private double minimum = Double.MAX_VALUE;
	private double maximum = Double.MIN_VALUE;

	ImageLoader loader; // the loader for the current image file
	ImageData[] imageDataArray; // all image data read from the current file

	/**
	 * @param fileName
	 * @throws FabioFileException
	 * @description filename should contains path for the load
	 */
	public EdfFile(String fileName) throws FabioFileException {
		this.header = new HashMap<String, String>();
		vKeysInHeader = new Vector<String>();
		this.fileName = fileName;
		loadHeader();
	}

	private void loadHeader() throws FabioFileException {

		String headerLine = null;

		BufferedReader inputStream = null;
		FileReader inputFileReader = null;
		boolean EdfHeaderBegin = false;
		// open an output file for reading
		try {

			inputFileReader = new FileReader(fileName);
			inputStream = new BufferedReader(inputFileReader);

		} catch (IOException e) {

			throw (new FabioFileException(this.getClass().getName(),
					"loadHeader()", e.getMessage()));
		}

		try {
			// search begin

			while ((headerLine = inputStream.readLine()) != null
					&& !headerLine.contains(IEdfVarKeys.EDF_HEADER_END)) {
				//
				if (headerLine.startsWith(IEdfVarKeys.EDF_HEADER_BEGIN)) {
					EdfHeaderBegin = true;
				} else if (EdfHeaderBegin) {
					String[] keyAndValue = headerLine
							.split(IEdfVarKeys.EDF_HEADER_SEP); // split between
					// =
					if (keyAndValue.length == 2) {
						String key = keyAndValue[0].trim(); // replaceAll("\\p{Space}",
						// "");
						String value = keyAndValue[1].replaceAll(
								IEdfVarKeys.EDF_HEADER_ENDOFLINE, "");

						header.put(key, value);
						vKeysInHeader.add(key);
						skipbytes += headerLine.length();
						headerEnd += 1;

					}
				}
			}
		} catch (IOException e1) {
			// close file
			try {
				inputFileReader.close();
				inputStream.close();
			} catch (IOException e) {
				throw (new FabioFileException(this.getClass().getName(),
						"loadHeader()", e.getMessage()));
			}
			throw (new FabioFileException(this.getClass().getName(),
					"loadHeader()", e1.getMessage()));
		}

		// close file
		try {
			inputFileReader.close();
			inputStream.close();
		} catch (IOException e) {
			throw (new FabioFileException(this.getClass().getName(),
					"loadHeader()", e.getMessage()));
		}

	}

	// Constructor
	// Getter and setter
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String myString = "{ \\n";
		Set<Entry<String, String>> mySet = header.entrySet();
		Iterator<Entry<String, String>> it = mySet.iterator();
		while (it.hasNext()) {

			Map.Entry<String, String> entry = it.next();
			myString += entry.getKey() + "=" + entry.getValue() + ";\\n";
		}
		myString += "} \\n";
		return myString;
	}

	/**
	 * @description get keys as they are stored in the HashMap
	 * @return EDF header keys
	 */
	public String[] getKeys() {
		Set<String> mySet = header.keySet();
		return mySet.toArray(new String[(mySet.size())]);
	}

	/**
	 * @description use in viewer to get the keys sorted as they are in the edf
	 *              header file
	 * @return a vector of EDF Header Keys
	 */
	public Vector<String> getKeysAsListedInHeader() {

		return vKeysInHeader;
	}

	public String getValue(String key) throws FabioFileException {
		String myValue = "";
		if (header.containsKey(key)) {
			myValue = header.get(key);
		} else {

			throw new FabioFileException(this.getClass().getName(),
					"getValue()", "The key " + key
							+ " has not be found in the header for the file ");
		}

		return myValue;

	}

	/**
	 * get image width from EDF keyword Dim_1
	 * 
	 * @return image width
	 */

	public int getWidth() {
		width = 0;
		try {
			width = Integer.parseInt(getValue("Dim_1").replaceAll("\\p{Space}",
					""));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FabioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return width;
	}

	/**
	 * get image height from EDF keyword Dim_2
	 * 
	 * @return image height
	 */
	public int getHeight() {
		height = 0;
		try {
			height = Integer.parseInt(getValue("Dim_2").replaceAll(
					"\\p{Space}", ""));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FabioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return height;
	}

	public int getBytesPerPixel() {
		return 2;
	}

	public void readBuffer() {
		RandomAccessFile randomFile = null;
		buffer = null;
		try {
			randomFile = new RandomAccessFile(fileName, "r");
			try {
				// DEBUG
				// System.out.println("readBuffer(): read buffer");
				int imgSize = getHeight() * getWidth() * getBytesPerPixel();
				buffer = new byte[imgSize];
				randomFile.seek(randomFile.length() - imgSize);
				randomFile.readFully(buffer);
				randomFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int[] getImageAsInt() {
		if (buffer == null) {
			readBuffer();
		}
		if (imageAsInt == null) {
			System.out.println("getImageAsInt(): convert buffer to int");
			imageAsInt = new int[getWidth() * getHeight()];
			// this assumes the image is unsigned short
			// TODO treat different EDF type e.g. float and double
			int msb, lsb;
			for (int i = 0; i < buffer.length / 2; i++) {
				lsb = 0x000000FF & (int) buffer[i * 2];
				msb = 0x000000FF & (int) buffer[i * 2 + 1];
				imageAsInt[i] = msb * 256 + lsb;
			}
		}
		return imageAsInt;
	}

	/**
	 * return minimum value in image
	 * 
	 * @return image minimum
	 * 
	 */
	public double getMinimum() {
		if (minimum == Double.MAX_VALUE) {
			// System.out.println("getMinimum(): calculate minimum");
			getImageAsInt();
			for (int i = 0; i < imageAsInt.length; i++) {
				if ((double) imageAsInt[i] < minimum)
					minimum = (double) imageAsInt[i];
			}
		}
		return minimum;
	}

	/**
	 * return maximum value in image
	 * 
	 * @return image maximum
	 * 
	 */
	public double getMaximum() {
		if (maximum == Double.MIN_VALUE) {
			// System.out.println("getMaximum(): calculate maximum");
			getImageAsInt();
			for (int i = 0; i < imageAsInt.length; i++) {
				if ((double) imageAsInt[i] > maximum)
					maximum = (double) imageAsInt[i];
			}
		}
		return maximum;
	}
}
