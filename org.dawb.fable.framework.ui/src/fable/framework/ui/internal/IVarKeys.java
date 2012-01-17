/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.internal;

import org.eclipse.swt.graphics.RGB;

public interface IVarKeys {

	public static final int DEFAULT_FILES_PER_TABLE = 50;
	public static final String MAIN_STATUS_LINE = "fable_main_status_line";
	public static final String SAVE_AS_ACTION = "fable.editor.columnfile.saveas";

	public static final String crystalSystemDesc = ""
			+ "The crystal system is determined by the space group.";
	public static final String spaceGroupDesc = "" + "spacegroup:\n\n"
			+ "The space group ID number is needed to transform a \n"
			+ "determined orientation into the fundamental region \n"
			+ "(the unique part of the orientation space). This is \n"
			+ "done so that the same solution will always have the same \n"
			+ "U matrix, not one of the other possible symmetric \n"
			+ "orientations allowed by symmetry. Either the space group\n"
			+ " symbol or the number may equivalently be given, but the \n"
			+ "number is used in the program";
	
	
	RGB COLOR_GREY = new RGB(128, 128, 128); //Grey
	RGB DEFAULT = new RGB(0, 0, 0); //default
	RGB COLOR_BLUE = new RGB(0, 0, 128); //blue
	/**Color option in green*/
	RGB COLOR_GREEN = new RGB(0, 128, 0); //green

}
