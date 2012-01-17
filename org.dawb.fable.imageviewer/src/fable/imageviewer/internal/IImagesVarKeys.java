/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * 
 */
package fable.imageviewer.internal;


/**
 * @author suchet
 * 
 */
public interface IImagesVarKeys {

	public static final String BTN_IMG_DELETE = "icons/delete.gif";
	public static final String BTN_IMG_ADD = "icons/add.gif";
	public static final String BTN_IMG_SUBTRACT = "icons/subtract.gif";
	public static final String BTN_IMG_ADD_PLOT = "icons/addplot.gif";
	public static final String BTN_IMG_SUBTRACT_PLOT = "icons/subtractplot.gif";
	public static final String BTN_IMG_SETTINGS = "icons/settings.gif";

	// Used for coordinate selection
	public static final int COORD_TL = 0;
	public static final int COORD_TR = 1;
	public static final int COORD_BR = 2;
	public static final int COORD_BL = 3;
	public static final int COORD_CUSTOM = 4;
	public static final String[][] coordNameValues = {
			{ "TL=(0,0) Image", Integer.toString(COORD_TL) },
			{ "TR=(0,0)", Integer.toString(COORD_TR) },
			{ "BR=(0,0) TotalCrys", Integer.toString(COORD_BR) },
			{ "BL=(0,0) Conventional", Integer.toString(COORD_BL) },
			{ "Custom...", Integer.toString(COORD_CUSTOM) }, };

	// Used for orientation selection (P -> +1, M -> -1, O -> 0)
	public static final int O_MOOM = 0;
	public static final int O_MOOP = 1;
	public static final int O_POOM = 2;
	public static final int O_POOP = 3;
	public static final int O_OMPO = 4;
	public static final int O_OPMO = 5;
	public static final int O_OMMO = 6;
	public static final int O_OPPO = 7;
	public static final String[][] orientNameValues = {
			{ "Original Image (-1 0 0 -1)", Integer.toString(O_MOOM) },
			{ "Flip H (-1 0 0 1)", Integer.toString(O_MOOP) },
			{ "Flip V (1 0 0 -1)", Integer.toString(O_POOM) },
			{ "Flip H and V (1 0 0 1)", Integer.toString(O_POOP) },
			{ "90 deg CW (0 -1 1 0)", Integer.toString(O_OMPO) },
			{ "90 deg CCW (0 1 -1 0)", Integer.toString(O_OPMO) },
			{ "90 deg CW, Flip H (0 -1 -1 0)", Integer.toString(O_OMMO) },
			{ "90 deg CW, Flip V (0 1 1 0)", Integer.toString(O_OPPO) }, };

}

