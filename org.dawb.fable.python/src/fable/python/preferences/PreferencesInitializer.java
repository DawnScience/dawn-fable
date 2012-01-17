/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import fable.python.jep.IPythonVarKeys;
import fable.python.rcp.Activator;

public class PreferencesInitializer extends AbstractPreferenceInitializer
		implements IPythonVarKeys {
	IEclipsePreferences default_pref = new DefaultScope()
			.getNode(Activator.PLUGIN_ID);

	public PreferencesInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault()
				.getPreferenceStore();

		// PYTHONPATH
		preferences.setDefault(PreferenceConstants.P_PYTHON_PYTHONPATH,
				               System.getProperty("eclipse.home.location"));
	}

}
