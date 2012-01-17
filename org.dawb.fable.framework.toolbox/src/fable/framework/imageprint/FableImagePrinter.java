/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.imageprint;

import org.eclipse.swt.widgets.Display;

/**
 * This class holds the print settings for image printing. The public field can
 * be used as an argument to print methods that take a PrintSettings. Often
 * these methods change the settings based on the user's last usage.
 * 
 * @author evans
 * 
 */
public class FableImagePrinter {
	/**
	 * The common PrintSettings for Fable. It can be reset to a new value as
	 * needed, typically after a print operation. It does not need to be
	 * disposed.
	 */
	private static PrintSettings settings;
	public static PrintSettings getSettings() {
		if (settings==null) settings = new PrintSettings();
		return settings;
	}
	public static void setSettings(PrintSettings settings2) {
		settings = settings2;
	}
}
