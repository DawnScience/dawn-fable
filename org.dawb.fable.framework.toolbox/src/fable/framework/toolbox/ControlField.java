/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolbox;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Pattern;
/**
 * This class is used in the toolbox to check the type of a String.
 * <br>
 * Use it for example to check if a String is an integer.
 * @author suchet
 *
 */
public class ControlField {

	/**
	 * This method checks if a string represents an integer.
	 * @param stringToTest 
	 * @return true if the string represents an integer.
	 */
	public static final boolean isInteger(String stringToTest) {
		boolean bok = true;
		try {
			Integer.valueOf(stringToTest);
		} catch (Exception e) {
			bok = false;
		}
		return bok;
	}

	/**
	 * This method checks if a string represents a Float with method format of an instance of DecimalFormat.
	 * This instance is created with the respect of the default Locale.
	 * @param stringToTest the string to test
	 * @return true if the string represents an Float.
	 */
	public static final boolean isFloat(String stringToTest) {
		boolean bok = true;
		try {
			DecimalFormat d = new DecimalFormat();
			d.parse(stringToTest);
			//Float.valueOf(stringToTest);
			bok = true;
		} catch (ParseException e) {
			bok = false;
		}

		/*
		 * final Pattern pattern= Pattern.compile("[ \\D &&[^\\.E\\s+-]]"); //a
		 * non digit [^0-9] except the dot and the minus for signed float
		 * if(pattern.matcher(s).find()){ bok=false; }
		 */
		return bok;
	}
/**
 * This method test is a string is an alphanumeric.
 * @param stringToTest the string to test
 * @return true if the string is an alphanumeric.
 */
	public static final boolean isAlphaNum(String stringToTest) {
		boolean bok = true;

		final Pattern pattern = Pattern.compile("\\p{Alnum}"); // An alpha
		// exept
		// punctuation
		if (pattern.matcher(stringToTest).find()) {
			bok = false;
		}

		return bok;
	}
}
