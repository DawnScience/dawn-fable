/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import fable.framework.ui.rcp.Activator;

public class ColumnFilePlotPreferencesInitializer extends
		AbstractPreferenceInitializer {

	public ColumnFilePlotPreferencesInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setDefault(ColumnFilePlotPreferences.X_LABEL, "sc");
		prefs.setDefault(ColumnFilePlotPreferences.Y_LABEL, "fc");
	}

}
