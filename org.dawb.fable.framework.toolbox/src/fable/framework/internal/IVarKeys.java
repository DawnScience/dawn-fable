/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.internal;

import org.eclipse.swt.SWT;

import fable.framework.imageprint.PrintSettings.Orientation;
import fable.framework.imageprint.PrintSettings.Units;

/**
 * This interface is used to declare static attributes for the framework.
 * 
 * @author suchet
 * 
 */
public interface IVarKeys {
	
	final String EDFFORMAT = "edf";
	final String EDFFORMATGZip = "edf.gz";
	final String EDFFORMATBz2 = "edf.bz2";
	
	final String CORFORMAT = "cor";
	final String CORFORMATGZip = "cor.gz";
	final String CORFORMATBz2 = "cor.bz2";
	
	final String TIFFORMAT = "tif";
	final String TIFFORMATGZip = "tif.gz";
	final String TIFFORMATBz2 = "tif.bz2";
	final String TIFF_FORMAT = "tiff", TIFF_FORMATGZip = "tiff.gz",
			TIFF_FORMATBz2 = "tiff.bz2";
	
	final String ADSCFORMAT = "img", ADSCFORMATGZip = "img.gz",
			ADSCFORMATBz2 = "img.bz2";
	
	final String MCCDFORMAT = "mccd", MCCDFORMATGZip = "mccd.gz",
			MCCDFORMATBz2 = "mccd.bz2";
	
	final String MAR2300FORMAT = "mccd", mar2300FormatGZip = "mccd.gz",
			MAR2300FORMATBz2 = "mccd.bz2";
	final String PNMFORMAT = "pnm", PGMFORMAT = "pgm", PBMFORMAT = "pbm";
	
	final String BRUKERFORMAT = "\\.\\d{4}$",
			BRUKERFORMATBz2 = "\\.\\d{4}\\.bz2$";
	final String CCDFORMAT = "ccd";
	final String UPDATEFILES_EVENT = "updatefiles";

	/**
	 * This is a regular expression for all fabio type.
	 */
	static String REGEX_FABIO_TYPES = EDFFORMAT + "|" + EDFFORMATGZip + "|"
			+ EDFFORMATBz2 + "|" + CORFORMAT + "|" + CORFORMATGZip + "|"
			+ CORFORMATBz2 + "|" + CCDFORMAT + "|" + TIFFORMAT + "|"
			+ TIFFORMATGZip + "|" + TIFFORMATBz2 + "|" + TIFF_FORMAT + "|"
			+ TIFF_FORMATGZip + "|" + TIFF_FORMATBz2 + "|" + ADSCFORMAT + "|"
			+ ADSCFORMATGZip + "|" + ADSCFORMATBz2 + "|" + MCCDFORMAT + "|"
			+ MCCDFORMATGZip + "|" + MCCDFORMATBz2 + "|" + MAR2300FORMAT + "|"
			+ mar2300FormatGZip + "|" + MAR2300FORMATBz2 + "|" + PNMFORMAT
			+ "|" + PGMFORMAT + "|" + PBMFORMAT + "|" + BRUKERFORMAT + "|"
			+ BRUKERFORMATBz2;

	/**
	 * this string represents a list of file type name managed by fabio.
	 * <p>
	 * This list set the name of "bruker" in the list of type files (for example
	 * in <code>SampleNavigatorPreferences</code>) instead of a regular
	 * expression.
	 */
	public static String FABIO_TYPES = EDFFORMAT + "|" + EDFFORMATGZip + "|"
			+ EDFFORMATBz2 + "|" + CORFORMAT + "|" + CORFORMATGZip + "|"
			+ CORFORMATBz2 + "|"  + "bruker" + "|"
			+ "bruker.bz2";

	/**
	 * This constant is used to set a minimum size to display perspective for
	 * large screen.
	 */
	public static int SCREENHEIGHT = 500;

	final String ParseCompleted = "Parsing_completed";
	final String ParseInProgress = "Parsing_working";
	/** Theses constants are used in a event.property */
	final String NEXTIMAGE = "next image";
	final String LASTIMAGE = "last image";
	final String FIRSTIMAGE = "first image";

	// Used for logger level selection
	public static final int LOGGER_LEVEL_ALL = 0;
	public static final int LOGGER_LEVEL_TRACE = 1;
	public static final int LOGGER_LEVEL_DEBUG = 2;
	public static final int LOGGER_LEVEL_INFO = 3;
	public static final int LOGGER_LEVEL_WARN = 4;
	public static final int LOGGER_LEVEL_ERROR = 5;
	public static final int LOGGER_LEVEL_FATAL = 6;
	public static final int LOGGER_LEVEL_OFF = 7;
	public static final String[][] loggerLevels = {
			{ "All", Integer.toString(LOGGER_LEVEL_ALL) },
			{ "Trace", Integer.toString(LOGGER_LEVEL_TRACE) },
			{ "Debug", Integer.toString(LOGGER_LEVEL_DEBUG) },
			{ "Info", Integer.toString(LOGGER_LEVEL_INFO) },
			{ "Warn", Integer.toString(LOGGER_LEVEL_WARN) },
			{ "Error", Integer.toString(LOGGER_LEVEL_ERROR) },
			{ "Fatal", Integer.toString(LOGGER_LEVEL_FATAL) },
			{ "None", Integer.toString(LOGGER_LEVEL_OFF) }, };

	// Image printing
	public static final String IMAGE_PRINT_LEFT = "2 cm";
	public static final String IMAGE_PRINT_RIGHT = "2 cm";
	public static final String IMAGE_PRINT_TOP = "2 cm";
	public static final String IMAGE_PRINT_BOTTOM = "2 cm";
	public static final int IMAGE_PRINT_HALIGN = SWT.CENTER;
	public static final int IMAGE_PRINT_VALIGN = SWT.TOP;
	public static final String IMAGE_PRINT_ORIENT = Orientation.PORTRAIT
			.getName();
	public static final String IMAGE_PRINT_UNITS = Units.CENTIMETER.getName();
	public static final String[][] imagePrinterHAlignOpts = {
			{ "Left", Integer.toString(SWT.LEFT) },
			{ "Center", Integer.toString(SWT.CENTER) },
			{ "Right", Integer.toString(SWT.RIGHT) },
			{ "Fill", Integer.toString(SWT.FILL) }, };
	public static final String[][] imagePrinterVAlignOpts = {
			{ "Top", Integer.toString(SWT.TOP) },
			{ "Center", Integer.toString(SWT.CENTER) },
			{ "Bottom", Integer.toString(SWT.BOTTOM) },
			{ "Fill", Integer.toString(SWT.FILL) }, };
	public static final String[][] imagePrinterOrientOpts = {
			{ "Portrait", "Portrait", }, { "Landscape", "Landscape" }, };
	public static final String[][] imagePrinterUnitsOpts = {
			{ Units.INCH.getName(), Units.INCH.getName() },
			{ Units.CENTIMETER.getName(), Units.CENTIMETER.getName() },
			{ Units.MILLIMETER.getName(), Units.MILLIMETER.getName() }, };

	// Memory Usage
	public static final boolean MU_SHOW_LEGEND = true;
	public static final boolean MU_SHOW_MAX = true;
	public static final int MU_INTERVAL = 1000;
	public static final int MU_MAX_AGE = 60000;

}
