/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import fable.framework.internal.IVarKeys;
import fable.framework.navigator.Activator;

/***
 * 
 * @author SUCHET This is the default extension for fabio files. 'edf' :
 *         ['edf'], 'cor' : ['edf'], 'pnm' : ['pnm'], 'pgm' : ['pnm'], 'pbm' :
 *         ['pnm'], 'tif' : ['tif'], 'tiff' : ['tif'], 'img' : ['adsc','OXD'],
 *         'mccd' : ['marccd'], 'mar2300': ['mar345'], 'sfrm' : ['bruker100'],
 *         'msk' : ['fit2dmask'],
 * 
 * 
 * 
 */
public class SampleNavigatorPreferencesInitializer extends
		AbstractPreferenceInitializer {
	IEclipsePreferences default_pref = new DefaultScope()
			.getNode(Activator.PLUGIN_ID);

	public SampleNavigatorPreferencesInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault()
				.getPreferenceStore();
		preferences.setDefault(FabioPreferenceConstants.FILE_TYPE,
				default_pref.get(FabioPreferenceConstants.FILE_TYPE,
						IVarKeys.FABIO_TYPES));
		preferences.setDefault(FabioPreferenceConstants.STEM_NAME,
				default_pref.get(FabioPreferenceConstants.STEM_NAME, ""));
		preferences.setDefault(FabioPreferenceConstants.FIX_COLUMN_SIZE,
				default_pref.getBoolean(
						FabioPreferenceConstants.FIX_COLUMN_SIZE, false));
		
		preferences.setDefault(FabioPreferenceConstants.USE_FABIO, false);

	}

}
