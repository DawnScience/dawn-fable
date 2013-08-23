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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import fable.framework.clipboard.FableClipboard;
import fable.framework.imageprint.FableImagePrinter;
import fable.framework.internal.IVarKeys;
import fable.framework.logging.FableLogger;
import fable.framework.toolboxpreferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IVarKeys {
	/**
	 * Indicates if this is the first time the logger level was set from
	 * preferences. Used to write a startup info message after setting the level
	 * from preferences.
	 */
	private boolean loggerLevelSet = false;
	private IPreferenceStore prefs = null;
	private IPropertyChangeListener listener = null;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.dawb.fable.framework.toolbox";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		// KE: It might be better to put the logger initialization in
		// fable.Activator.
		// Logger
		int intVal = prefs.getInt(PreferenceConstants.P_FABLE_LOGGER_LEVEL);
		//Logger logger = FableLogger.getLogger();
		//setLoggerLevel(logger, intVal);

		// Image printing
		String value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_UNITS);
		FableImagePrinter.getSettings().setUnits(value);
		value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_LEFT);
		FableImagePrinter.getSettings().setLeftString(value);
		value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_RIGHT);
		FableImagePrinter.getSettings().setRightString(value);
		value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_TOP);
		FableImagePrinter.getSettings().setTopString(value);
		value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_BOTTOM);
		FableImagePrinter.getSettings().setBottomString(value);
		intVal = prefs.getInt(PreferenceConstants.P_IMAGE_PRINT_HALIGN);
		FableImagePrinter.getSettings().setHorizontalAlign(intVal);
		intVal = prefs.getInt(PreferenceConstants.P_IMAGE_PRINT_VALIGN);
		FableImagePrinter.getSettings().setVerticalAlign(intVal);
		value = prefs.getString(PreferenceConstants.P_IMAGE_PRINT_ORIENT);
		FableImagePrinter.getSettings().setOrientation(value);

		// Print startup message (after getting the level from preferences)
		if (!loggerLevelSet) {
			loggerLevelSet = true;
			//logger.info("Fable Starting");
			//logger.info("Setting logging to " + logger.getName());
		}

		// Root logger
		intVal = prefs.getInt(PreferenceConstants.P_ROOT_LOGGER_LEVEL);

		// Add a listener for preference changes
		prefs.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (property.equals(PreferenceConstants.P_FABLE_LOGGER_LEVEL)) {
					String value = (String) event.getNewValue();
					// System.out.println("fable logger level=" +
					// value);
					int intValue = Integer.parseInt(value);
					//Logger logger = FableLogger.getLogger();
					//setLoggerLevel(logger, intValue);
				} else if (property
						.equals(PreferenceConstants.P_ROOT_LOGGER_LEVEL)) {
					String value = (String) event.getNewValue();
					// System.out.println("root logger level=" + value);
					int intValue = Integer.parseInt(value);
					//Logger rootLogger = Logger.getRootLogger();
					//setLoggerLevel(rootLogger, intValue);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_UNITS)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setUnits(value);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_LEFT)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setLeftString(value);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_RIGHT)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setRightString(value);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_TOP)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setTopString(value);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_BOTTOM)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setBottomString(value);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_HALIGN)) {
					String value = (String) event.getNewValue();
					int intValue = Integer.parseInt(value);
					FableImagePrinter.getSettings().setHorizontalAlign(intValue);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_VALIGN)) {
					String value = (String) event.getNewValue();
					int intValue = Integer.parseInt(value);
					FableImagePrinter.getSettings().setVerticalAlign(intValue);
				} else if (property
						.equals(PreferenceConstants.P_IMAGE_PRINT_ORIENT)) {
					String value = (String) event.getNewValue();
					FableImagePrinter.getSettings().setOrientation(value);
				}
			}
		});
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			plugin = null;
			if (prefs != null && listener != null) {
				prefs.removePropertyChangeListener(listener);
			}
			prefs = null;
			listener = null;
			// This gets an exception and doesn't really do anything.
			try {
				// System.out.println("cur=" + Thread.currentThread());
				// System.out.println("display=" + Display.getCurrent());
				if (FableClipboard.clipboard != null
						&& !FableClipboard.clipboard.isDisposed()) {
					FableClipboard.clipboard.dispose();
				}
			} catch (SWTException ex) {
				// This is the exception it gets, indicating the device
				// (presumably the display) is disposed.
			} catch (Throwable t) {
				// Just to be sure ;-) Do nothing.
			}
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
