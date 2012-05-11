/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.fabio;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;
import org.embl.cca.utils.imageviewer.HeaderData;
import org.embl.cca.utils.imageviewer.TwoDimFloatArrayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.toolbox.ToolBox;
//import fable.python.jep.FableJep;
import loaders.pilatus.PilatusLoader;

/**
 * The FabioFile class is used to read files using the Python fabio package. It
 * uses the Jep interpreter to call Python. It can be used in multi-threaded
 * Java environments. Different thread should supply their own Jep interpreter.
 * The image data is stored in a cache so that the last N images will be read
 * from the cache and not from disk. This will be reflected in the time to read
 * i.e. it will be zero. The cache size can be increased in function of the
 * memory available. It is currently set to 20 i.e. 20*16 MB.
 * 
 * @author Andy Gotz + Gaelle Suchet
 * 
 */
public class FabioFile implements java.lang.Comparable<Object>, IPropertyChangeListener {

	private Semaphore semaphore = new Semaphore(1);
	public boolean headerRead = false;
//	public boolean imageRead = false;
	private HashMap<String, String> header;
	private String fullFileName;
	private String fileName;
	private Vector<String> vKeysInHeader; // a list of keys as they arrive in
	// the header; useful in edfViewer
//	private int width, height;
//	private float minimum = Float.MAX_VALUE;
//	private float maximum = Float.MIN_VALUE;
//	private float sum = 0.f;
//	private float mean = Float.MIN_VALUE;
	// private float stddev = Float.MIN_VALUE;
	final int IMAGE_CACHE_MAX = 10;
	private int floatImageBufferI = -1;
	private long timeToReadImage = 0;
	private String stem; // GS for peaksearch
	private String fileNumber; // Gs for peaksearch
	ImageLoader loader; // the loader for the current image file
//	ImageData[] imageDataArray; // all image data read from the current file
	Logger logger;
	private int index;
	private boolean flag = true;
	/*
	 * to speed up FabioFile memory management we keep a buffer of the last N
	 * images in memory. This caches the image so that the most recently read
	 * images will be found in the cache and do not have to be read from disk
	 */
	private static TwoDimFloatArrayData floatImageBuffer[] = null;
	private static int floatImageBufferPointer = 0;
	private static String fileImageBuffer[] = null;

	private String comparatorKey = "filename";
	private int comparatorDir = SWT.DOWN;
	private int comparedResult;

	/**
	 * Create a FabioFile object which be able to read Fabio files via the fabio
	 * Python module.
	 * 
	 * @param fileName
	 * @throws FabioFileException
	 * @description filename should contains path for the load
	 */
	public FabioFile(String _fullFileName) throws FabioFileException {
		
		logger = LoggerFactory.getLogger(FabioFile.class);
		if (floatImageBuffer == null) {
			// logger.debug("initialise floatImageBuffer");
//			floatImageBuffer = new float[IMAGE_CACHE_MAX][];
			floatImageBuffer = new TwoDimFloatArrayData[IMAGE_CACHE_MAX];
			fileImageBuffer = new String[IMAGE_CACHE_MAX];
			for (int i = 0; i < floatImageBuffer.length; i++) {
				floatImageBuffer[i] = null;
				fileImageBuffer[i] = new String();
			}
		}
		// Check if file exists - why ?
		if (!new File(_fullFileName).exists()) {
			throw new FabioFileException(this.getClass().getName(), "FabioFile", "File not found: " + _fullFileName);
		}
		this.header = new HashMap<String, String>();
		vKeysInHeader = new Vector<String>();
		fullFileName = _fullFileName;
		String[] split = fullFileName.split("[\\\\/]");
		fileName = fullFileName;
		if (split.length > 1) {
			fileName = split[split.length - 1];
		}
	}

	public synchronized void acquire() {
		semaphore.acquireUninterruptibly();
	}

	public synchronized void release() {
		semaphore.release();
	}

	private void importFabioModules(FableJep fj) throws Throwable {
		try {
			final boolean isMsg = fj.isRequireErrorMessage();
			fj.jepImportModules("sys",   isMsg);
			fj.jepImportModules("numpy", isMsg);
			fj.jepImportModules("PIL",   isMsg);
			fj.jepImportModules("fabio.openimage", isMsg);
		} catch (Throwable e) {
			throw e;
		}
	}

	/**
	 * Set the fullfilename of fabio file if different from header
	 * 
	 * @param fullfilename
	 */
	public void setFullFileName(String fullfilename) {
		fullFileName = fullfilename;
	}

	public String getFullFileName() {
		return fullFileName;
	}

	public boolean isHeaderRead() {
		return headerRead;
	}

	/*
	 * public boolean isFabioFile(){ boolean ok=true; File f=new
	 * File(fullFileName);
	 * 
	 * if(f.exists()){ try { FableJep.getJep().set("filename",fullFileName); //
	 * this is a fairly "heavy" approach to determine if a file is a fabio file
	 * i.e. reading the header
	 * FableJep.getJep().eval("im = fabio.openimage.openheader(filename)"); }
	 * catch (Throwable e) { ok=false; } }else{ ok=false; } return ok; }
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String myString = "{ \\n";
		try {
			synchronized( header ) {
				if (!isHeaderRead())
					loadHeader();
				Set<Map.Entry<String, String>> mySet = header.entrySet();
				Iterator<Entry<String, String>> it = mySet.iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
					myString += entry.getKey() + "=" + entry.getValue() + ";\\n";
				}
			}
		} catch (FabioFileException e) {
		}
		myString += "} \\n";
		return myString;
	}

	/**
	 * Load only the header and not the image. loadHeader() uses the python
	 * fabio module to read the header.
	 * 
	 * @throws FabioFileException
	 * @throws Throwable
	 * 
	 * @update 15-01-2008 GS : Check if file exists to avoid jep exception (not
	 *         caught !)
	 */
	public void loadHeader() throws FabioFileException {
		synchronized( header ) {
			if (!isHeaderRead())
				try {
					loadHeaderExternal();
				} catch (Throwable e) {
					throw new FabioFileException(this.getClass().getName(),
							"loadHeader " + fullFileName, e.getMessage());
				}
		}
	}

	/**
	 * Load only the header and not the image. loadHeader() uses the python
	 * fabio module to read the header. Use the jep interpreter provided by the
	 * callee. This is needed for reading headers in multithreaded environments.
	 * 
	 * @throws FabioFileException
	 * 
	 */
	public void loadHeaderExternal() throws FabioFileException {
		synchronized( header ) {
			if (isHeaderRead())
				return;
			HeaderData pHeader = null;
			if( PilatusLoader.supports( fullFileName ) ) {
				pHeader = PilatusLoader.loadHeader(fullFileName);
			} else {
				File f = new File(fullFileName);
				if (f.exists()) {
					try {
						FableJep fableJep = getFableJep();
						importFabioModules(fableJep);
						fableJep.set("filename", fullFileName);
						fableJep.eval("im = fabio.openimage.openheader(filename)");
						fableJep.eval("keys = im.header.keys()");
						fableJep.eval("vals = im.header.values()");
						fableJep.eval("res = len(keys)");
						int n = (Integer) fableJep.getValue("res");
						String key = "", val;
						HashMap<String, String> newHeader = new HashMap<String, String>();
						for (int i = 0; i < n; i++) {
							fableJep.set("i", i);
							fableJep.eval("res = str(keys[i])"); // have python
							key = (String) fableJep.getValue("res");
							try {
								// coerce to str
								fableJep.eval("res = str(vals[i])");
								val = (String) fableJep.getValue("res");
							} catch (Throwable e) {
								// if header fails
								val = "-1";
							}
							newHeader.put(key, val);
						}
						pHeader = new HeaderData( newHeader );
					} catch (Throwable e) {
						logger.error(e.getMessage());
						throw new FabioFileException(this.getClass().getName(),
								"loadHeader" + fullFileName, e.getMessage());
					}
				} else {
					throw new FabioFileException(this.getClass().getName(),
							"loadHeader", "File not found" + fullFileName);
				}
			}
			pHeader.getKeyAndValuePairs().put("name", fileName);
			pHeader.getKeyAndValuePairs().put("#", "" + index);
			vKeysInHeader.addAll( pHeader.getKeyAndValuePairs().keySet() );
			header.putAll( pHeader.getKeyAndValuePairs() );
			headerRead = true;
		}
	}

	/**
	 * Add new key in header info
	 */
	public void addHeaderInfo(String key, String value) {
		synchronized( header ) {
			header.put(key, value);
		}
	}

	/**
	 * @description get keys sorted alphabetically
	 * @return Fabio header keys
	 * @throws FabioFileException
	 * @throws Throwable
	 */
	public String[] getKeys() throws FabioFileException, Throwable {
		Set<String> mySet;
		synchronized( header ) {
			if (!isHeaderRead()) {
				loadHeader();
			}
			mySet = header.keySet();
		}
		String[] keys = mySet.toArray(new String[(mySet.size())]);
		ToolBox.quicksort(keys, 0, keys.length);
		return keys;
	}

	/**
	 * @description use in viewer to get the keys sorted as they are in the edf
	 *              header file
	 * @return a vector of EDF Header Keys
	 * @throws FabioFileException
	 * @throws Throwable
	 */
	public Vector<String> getKeysAsListedInHeader() throws FabioFileException,
			Throwable {
		synchronized( header ) {
			if (!isHeaderRead()) {
				loadHeader();
			}
			return vKeysInHeader;
		}
	}

	/**
	 * 
	 * @param key
	 *            the header key
	 * @return the value of the key header
	 * @throws FabioFileException
	 * @throws Throwable
	 */
	public String getValue(String key) throws FabioFileException {
		synchronized( header ) {
			if (!isHeaderRead()) {
				loadHeader();
			}
			if (header.containsKey(key)) {
				return header.get(key);
			} else {
				throw new FabioFileException(this.getClass().getName(),
						"getValue()", "The key " + key
								+ " has not been found in the header for the file "
								+ fileName);
			}
		}
	}

	/**
	 * Gaelle : add a sample Vector index for this fabioFile
	 * 
	 * @param index
	 *            index in sample vector file
	 */
	public void addIndex(int index) {
		this.index = index;
	}

	public String getFullFilename() {
		return fullFileName;
	}

	/**
	 * return short file name i.e. without the path
	 * 
	 * @return short file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * get time to read image in ms
	 * 
	 * @return time to took to read image and make a copy
	 */
	public long getTimeToReadImage() {
		return timeToReadImage;
	}

	/**
	 * 
	 * 8 janv. 08
	 * 
	 * @author G. Suchet
	 * @return fabio file stem name
	 */
	public String getStems() {
		if (stem == null) {
			String s = null;
			String[] splitter = this.fileName.split("\\.");
			s = fileName;
			if (splitter != null && splitter.length >= 2) {
				// check if it is bruker or compressed bruker
				String ext = splitter[1];
				try {
					Integer.valueOf(ext);
					int index = fileName.indexOf(".");
					s = fileName.substring(0, index);
				} catch (NumberFormatException n) {
					// this is not a bruker format
					int j = splitter[0].length() - 1;

					while (j > 1) {
						try {
							Integer.valueOf(splitter[0].substring(j - 1, j));
							j--;
						} catch (NumberFormatException exc) {
							s = splitter[0].substring(0, j);
							j = 0;
						}
					}
				}
			}
			stem = s;
		}
		return stem;
	}

	/**
	 * @name getFileNumber
	 * @return file number, even for bruker format //TODO test with bruker files
	 */
	public String getFileNumber() {
		if (fileNumber == null) {
			String s = null;
			String[] splitter = this.fileName.split("\\.");
			s = fileName;
			if (splitter != null && splitter.length >= 2) {
				// check if it is bruker or compressed bruker
				String ext = splitter[1];
				try {
					Integer.valueOf(ext);

					s = ext;
				} catch (NumberFormatException n) {
					// this is not a bruker format
					int j = splitter[0].length() - 1;

					while (j > 1) {
						try {
							Integer.valueOf(splitter[0].substring(j - 1, j));
							j--;
						} catch (NumberFormatException exc) {
							s = splitter[0].substring(j, splitter[0].length());
							j = 0;
						}
					}
				}
			}
			fileNumber = s;
		}
		return fileNumber;

	}

	/**
	 * get image width
	 * 
	 * @return image width
	 * @throws Throwable
	 */
	public int getWidth() throws Throwable {
		synchronized( floatImageBuffer ) {
			if (!isCached()) {
				readImage();
			}
			return floatImageBuffer[floatImageBufferI].getWidth();
		}
	}

	/**
	 * get image height
	 * 
	 * @return image height
	 * @throws Throwable
	 */
	public int getHeight() throws Throwable {
		synchronized( floatImageBuffer ) {
			if (!isCached()) {
				readImage();
			}
			return floatImageBuffer[floatImageBufferI].getHeight();
		}
	}

	public int getBytesPerPixel() {
		return 2;
	}

	public boolean isCached() {
		do {
			if( floatImageBufferI >= 0 && fileImageBuffer[floatImageBufferI] != null
				&& fileImageBuffer[floatImageBufferI].equalsIgnoreCase(fullFileName) )
				break;
			int i;
			int iMax = fileImageBuffer.length;
			for( i = 0; i < iMax; i++ )
				if( fileImageBuffer[ i ] != null
					&& fileImageBuffer[ i ].equalsIgnoreCase( fullFileName ) )
					break;
			if( i >= iMax )
				i = -1;
			floatImageBufferI = i;
		} while( false );
		return floatImageBufferI >= 0;
	}

	/**
	 * read image into memory
	 * 
	 * @throws Throwable
	 */
	public void readImage() throws Throwable {
		readImageAsFloat();
	}

	/**
	 * read image as float into memory
	 * 
	 * @throws Throwable
	 */
	public void readImageAsFloat() throws Throwable {
		readImageAsFloatExternal();
	}

	/**
	 * increment float image buffer pointer, wrap around if end of buffer
	 * reached
	 */
	private void incrementBufferPointer() {
		synchronized( floatImageBuffer ) {
			if (++floatImageBufferPointer >= floatImageBuffer.length)
				floatImageBufferPointer = 0;
		}
	}

	/**
	 * read image as float into memory
	 * 
	 * @throws Throwable
	 */
	protected void readImageAsFloatExternal() throws Throwable {
		synchronized( floatImageBuffer ) {
			timeToReadImage = 0;
			if (isCached())
				return;
			TwoDimFloatArrayData pData = null;
			long before = System.currentTimeMillis();
			if( PilatusLoader.supports( fullFileName ) ) {
				pData = PilatusLoader.loadPilatus(fullFileName);
			} else {
				FableJep fableJep = getFableJep();
				logger.debug("read file " + fileName);
				try {
					importFabioModules(fableJep);
					fableJep.set("filename", fullFileName);
					fableJep.eval("im = fabio.openimage.openimage(filename)");
					fableJep.eval("res = im.data.astype(numpy.float32).tostring()");
					float[] data = fableJep.getValue_floatarray("res");
					// now overwrite data to free memory in python
					fableJep.eval("res = im.dim1");
					int width = (Integer) fableJep.getValue("res");
					fableJep.eval("res = im.dim2");
					// KE: There are not used for anything
					// long elapsed;
					// long before_get;
					int height = (Integer) fableJep.getValue("res");
					pData = new TwoDimFloatArrayData( width, height, data );
					/*
					 * getting the mean via Python takes too long (approx. 160 ms
					 * for 2048x2048 image)
					 */
					/*
					 * if (mean == Float.MIN_VALUE) { before_get =
					 * System.currentTimeMillis(); jep.eval("res = im.getmean()");
					 * mean = (Float) jep.getValue("res"); elapsed =
					 * System.currentTimeMillis()-before_get;
					 * logger.info("fabio.getmean() took "+elapsed+" ms"); }
					 */
					/*
					 * getting the standard deviation via Python takes too long
					 * (approx. 330 ms for 2048x2048 image)
					 */
					/*
					 * if (stddev == Float.MIN_VALUE) { before_get =
					 * System.currentTimeMillis(); jep.eval("res = im.getstddev()");
					 * stddev = (Float) jep.getValue("res"); elapsed =
					 * System.currentTimeMillis()-before_get;
					 * logger.info("fabio.getstddev() took "+elapsed+" ms"); }
					 */
					// calculate min and max if not done so already
	//				if (minimum == Float.MAX_VALUE || maximum == Float.MIN_VALUE) {
						/*
						 * could use fabio to calculate min, max and mean (if we
						 * assume NumPy is faster than Java) but the data type
						 * returned is undetermined and therefore I do not know how
						 * to interpret the result
						 */
						/*
						 * jep.eval("res = im.getmin()"); minimum = (float)
						 * (Integer)jep.getValue("res");
						 * jep.eval("res = im.getmax()"); maximum = (float)
						 * (Integer)jep.getValue("res");
						 */
	
						// Java version to calculate minimum and maximum (takes only
						// 60 ms for 2048x2048 image)
						// before_get = System.currentTimeMillis();
/*
						sum = 0.f;
						int iMax = data.length;
						for (int i = 0; i < iMax; i++) {
							float f = data[i];
							sum += f;
							if (f < minimum)
								minimum = f;
							if (f > maximum)
								maximum = f;
						}
						mean = sum / (float) (iMax);
						// elapsed = System.currentTimeMillis() - before_get;
						// logger.info("java get mean,min.max took "+elapsed+" ms");
*/
				} catch (Throwable j) {
					throw j;
					// j.printStackTrace();
				}
			}
			floatImageBuffer[floatImageBufferPointer] = pData;
			fileImageBuffer[floatImageBufferPointer] = fullFileName;
			floatImageBufferI = floatImageBufferPointer;
			incrementBufferPointer();
			timeToReadImage = System.currentTimeMillis() - before;
		}
		return;
	}

	/**
	 * Return image as floating pint array
	 * 
	 * @return image as floating point array
	 * @throws Throwable
	 */
	public float[] getImageAsFloat() throws Throwable {
		synchronized( floatImageBuffer ) {
			readImageAsFloat();
			return floatImageBuffer[floatImageBufferI].getData();
		}
	}

	/**
	 * Return image as floating pint array
	 * 
	 * @return image as floating point array
	 */
/*
	public float[] getImageAsFloat(FableJep jep) {
		timeToReadImage = 0;
		if (!isCached())
			try {
				readImageAsFloat2(jep);
				fileCached = true;
			} catch (Throwable e) {
				// do not print exception - what should be done ?
			}
		return floatImageBuffer[floatImageBufferI];
	}
*/
	/**
	 * return image as int by converting the float image to int, do not keep the
	 * image in memory
	 * 
	 * @return image as integer array
	 * @throws Throwable
	 */
	public int[] getImageAsInt() throws Throwable {
		float[] data = getImageAsFloat();
		int[] _imageAsInt = new int[getWidth() * getHeight()];
		int iMax = data.length;
		for (int i = 0; i < iMax; i++) {
			_imageAsInt[i] = (int) data[i];
		}
		return _imageAsInt;
	}

	/**
	 * return minimum value in image
	 * 
	 * @return image minimum
	 * @throws Throwable
	 * 
	 */
/*
	public float getMinimum2() throws Throwable {
		if (!isCached()) {
			readImage();
		}
		return minimum;
	}
*/
	/**
	 * return maximum value in image
	 * 
	 * @return image maximum
	 * @throws Throwable
	 * 
	 */
/*
	public float getMaximum2() throws Throwable {
		if (!isCached()) {
			readImage();
		}
		return maximum;
	}
*/
	/**
	 * return mean value in image
	 * 
	 * @return image mean
	 * @throws Throwable
	 * 
	 */
/*
	public float getMean2() throws Throwable {
		if (!isCached()) {
			readImage();
		}
		return mean;
	}
*/
	/*
	 * What is the flag used for ? We need a description here or it should be
	 * deleted (andy)
	 */

	public void setFlag(boolean b) {
		flag = b;
	}

	public boolean getFlag() {
		return flag;
	}

	public int compareTo(final Object other) {

		try {
			String valueOther;
			valueOther = ((FabioFile) other).getValue(comparatorKey);
			String valueThis = this.getValue(comparatorKey);

			if (comparatorDir == SWT.UP) {
				comparedResult = valueOther.compareTo(valueThis);

			} else {
				comparedResult = valueThis.compareTo(valueOther);

			}

		} catch (FabioFileException e) {
			logger.error(e.getMessage());
		}

		return comparedResult;
	}

	/**
	 * 
	 * @param key
	 *            the key used to sort
	 * @param other
	 *            the fableFile
	 * @return 0 if equal, 1 if keyValue for other is greater than this,-1 if
	 *         otherValue<thisvalue, else 99 if an error occured
	 * 
	 */
	public int compareTo(String key, Object other) {
		comparatorKey = key;
		return compareTo(other);
	}

	public void propertyChange(PropertyChangeEvent event) {
		// Listen to its Sample
		if (event.getProperty().equals("comparator")) {
			this.comparatorKey = ((String) event.getNewValue());
		} else if (event.getProperty().equals("dir")) {
			this.comparatorDir = ((Integer) event.getNewValue());
		}

	}

	private FableJep getFableJep() throws Throwable {
		return FableJep.getFableJep();
	}

}
