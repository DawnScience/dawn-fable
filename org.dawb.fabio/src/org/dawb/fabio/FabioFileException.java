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

public class FabioFileException extends Exception {
	private static final long serialVersionUID = 1L;
	private String classname; // the name of the class
	private String method; // the name of the method
	private String message; // a detailed message

	public FabioFileException(String myclassname, String mymethod,
			String mymessage) {
		super();
		classname = myclassname;
		method = mymethod;
		message = mymessage;

	}

	/**
	 * @return the classname
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return "An EdfFileException has been launched in classe "
				+ getClassname() + " for the method " + getMethod() + ": "
				+ getMessage().toUpperCase();
	}

}
