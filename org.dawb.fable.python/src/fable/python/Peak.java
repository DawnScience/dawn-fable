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

import java.util.HashMap;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;

/**
 * 
 * @author G. Suchet fable.peaksearch 9 janv. 08
 *         <p>
 *         This class describes a peak as found in an outputFile from peaksearch
 *         f(fast-->x) and s (slow -->y) are the position of the centre of mass
 *         of the peak in the uncorrected image (first moments of the intensity
 *         distributions in the blobs is the mean) fc and sc are the x and y
 *         positions corrected for spatial distortion. sig_f and sig_s are the
 *         second moments of the intensity distribution in the blob. Something
 *         like the width, but off by some factor of twopi perhaps cov_fs is the
 *         covariance (the third one of the second moments). It ranges between
 *         -1 and 1 with a value of 0 for a circular peak and +1 and -1 refering
 *         to elliptical shapes rotated by 90 degrees from each other.
 *         </p>
 */
public class Peak implements java.lang.Comparable<Object>,
		IPropertyChangeListener {

	// KE: These are not used
	// private String fileName;
	// private String id; // peaks number id
	// private String Number_of_pixels;
	// private String Average_counts;
	private String f; // fast x
	private String s;// slow y
	// private String fc;
	// private String sc;
	// private String sig_f;
	// private String sig_s;
	// private String cov_fs;
	private String[] keysInfile;

	HashMap<String, String> keyAndVal;
	// To sort in the table
	private String comparatorKey = "#";
	private int comparatorDir = SWT.DOWN;
	private int comparedResult;
	boolean show = true;
	private PeakSearchSpt parent;

	/**
	 * @param id
	 * @param number_of_pixels
	 * @param average_counts
	 * @param f
	 * @param s
	 * @param fc
	 * @param sc
	 * @param sig_f
	 * @param sig_s
	 * @param cov_fs
	 */
	// @Deprecated
	/*
	 * public Peak(String index, String keys, String values ){ if(keys!= null ){
	 * String[] key=keys.split("\\p{Space}++"); keysInfile=new
	 * ArrayList<String>(); keyAndVal=new HashMap<String, String>(); String[]
	 * val=null; if(values!=null){val=values.split("\\p{Space}++");} String
	 * myVal=""; keyAndVal.put("#", index); keysInfile.add("#"); for(int i=1;
	 * i<key.length && i<val.length; i++){
	 * 
	 * keysInfile.add(key[i]); if(val!=null){myVal=val[i];}
	 * keyAndVal.put(key[i], val[i]); } f=keyAndVal.get("f");
	 * s=keyAndVal.get("s"); } }
	 */
	// Show or hide in image viewer
	public Peak(String[] keys, String values) {
		if (keys != null) {

			keysInfile = keys;

			keyAndVal = new HashMap<String, String>();
			String[] val = null;
			if (values != null) {
				val = values.split("\\p{Space}++");
			}
			// KE: This is not used
			// String myVal = "";
			// keyAndVal.put("#", String.valueOf(index));
			for (int i = 0; i < keysInfile.length && i < val.length; i++) {
				if (val != null) {
					// myVal = val[i];
				}
				keyAndVal.put(keysInfile[i], val[i]);
			}
			f = keyAndVal.get("f");
			s = keyAndVal.get("s");
		}
	}

	public void setParent(PeakSearchSpt father) {
		parent = father;
	}

	/**
	 * 
	 */
	public void show(boolean b) {
		show = b;
	}

	public boolean isVisible() {
		return show;
	}

	public String getF() {
		return f != null ? f : "0";
	}

	public String getS() {
		return s != null ? s : "0";
	}

	public String toString() {
		// boucle sur le arraylist;
		String myString = "";

		for (int i = 0; keysInfile != null && keysInfile.length > i; i++) {
			String currentKey = keysInfile[i];
			myString += currentKey + "=" + keyAndVal.get(currentKey);
		}
		return myString;
	}

	public String[] getValues() {
		String[] myString = new String[keysInfile.length];

		for (int i = 0; keysInfile != null && keysInfile.length > i; i++) {
			String currentKey = keysInfile[i];
			myString[i] = keyAndVal.get(currentKey);

		}
		return myString;
	}

	public String getValue(String k) {
		return keyAndVal.get(k);
	}

	public int compareTo(String key, Object other) {
		comparatorKey = key;
		return compareTo(other);
	}

	public int compareTo(Object other) {
		String valueOther;
		valueOther = ((Peak) other).getValue(comparatorKey);
		String valueThis = this.getValue(comparatorKey);

		if (comparatorDir == SWT.UP) {
			try {
				Float f_valueThis = Float.parseFloat(valueThis);
				Float f_valueOther = Float.parseFloat(valueOther);
				comparedResult = f_valueOther.compareTo(f_valueThis);
			} catch (NumberFormatException n) {
				comparedResult = valueOther.compareTo(valueThis);
			}
		} else {
			try {
				Float f_valueThis = Float.parseFloat(valueThis);
				Float f_valueOther = Float.parseFloat(valueOther);
				comparedResult = f_valueThis.compareTo(f_valueOther);
			} catch (NumberFormatException n) {
				comparedResult = valueThis.compareTo(valueOther);

			}
		}
		return comparedResult;
	}

	public void propertyChange(PropertyChangeEvent event) {
		// Listen to its Peakfile
		if (event.getProperty().equals("comparator")) {
			this.comparatorKey = ((String) event.getNewValue());
		} else if (event.getProperty().equals("dir")) {
			this.comparatorDir = ((Integer) event.getNewValue());
		}

	}

	/**
	 * This method returns the PeakSearchSpt which belongs thi peak
	 * 
	 * @return
	 */
	public PeakSearchSpt getParent() {

		return parent;
	}

}
