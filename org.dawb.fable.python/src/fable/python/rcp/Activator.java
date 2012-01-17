/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.rcp;

import org.dawb.fabio.FableJep;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import fable.python.jep.IPythonVarKeys;
import fable.python.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IPythonVarKeys {
	private IPreferenceStore prefs;
	private IPropertyChangeListener listener;
	// The plug-in ID
	public static final String PLUGIN_ID = "org.dawb.fable.python";
	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
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
		plugin = this;
		// DEBUG
		prefs = Activator.getDefault().getPreferenceStore();
		// Python
		String value = prefs.getString(PreferenceConstants.P_PYTHON_PYTHONPATH);
		
		// In debug mode this might be: file:/users/gerring/workspace_dawb/org.dawb.workbench.target/bundles/
		FableJep.setPythonPath(value);
		

		// Add a listener for preference changes
		prefs.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (property.equals(PreferenceConstants.P_PYTHON_PYTHONPATH)) {
					String value = (String) event.getNewValue();
				    FableJep.setPythonPath(value);
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
