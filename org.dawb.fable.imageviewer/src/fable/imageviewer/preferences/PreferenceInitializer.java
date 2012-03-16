/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import fable.imageviewer.rcp.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		// KE: IPreferenceStore is the older form. Preferences is newer.
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_IMAGES_CACHED, "10");
		store.setDefault(PreferenceConstants.P_IMAGES_PER_TAB, "100");
		store.setDefault(PreferenceConstants.P_MAX_SAMPLES, "10");
		store.setDefault(PreferenceConstants.P_XLABEL, "");
		store.setDefault(PreferenceConstants.P_YLABEL, "");
		store.setDefault(PreferenceConstants.P_ORIENT, "0");
		store.setDefault(PreferenceConstants.P_PALETTE, "0");
		store.setDefault(PreferenceConstants.P_COORD_X0, "0");
		store.setDefault(PreferenceConstants.P_COORD_Y0, "0");
		store.setDefault(PreferenceConstants.P_COORD_PIXELWIDTH, "1");
		store.setDefault(PreferenceConstants.P_COORD_PIXELHEIGHT, "1");
		store.setDefault(PreferenceConstants.P_COORD_XNAME, "x");
		store.setDefault(PreferenceConstants.P_COORD_YNAME, "y");
		store.setDefault(PreferenceConstants.P_RELIEFMOVE, true);
		
		store.setDefault(PreferenceConstants.P_AUTOSCALE,  true);
		store.setDefault(PreferenceConstants.P_KEEPASPECT, true);
	}

}
