/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolboxpreferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import fable.framework.internal.IVarKeys;
import fable.framework.toolbox.Activator;

public class PreferencesInitializer extends AbstractPreferenceInitializer
		implements IVarKeys {
	IEclipsePreferences default_pref = new DefaultScope()
			.getNode(Activator.PLUGIN_ID);

	public PreferencesInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault()
				.getPreferenceStore();

		// Layout
		// Rectangle screenWidth = Display.getCurrent().getPrimaryMonitor()
		// .getBounds();

		/*
		 * if (screenWidth.height > IVarKeys.SCREENHEIGHT) { // preferences are
		 * large screen preferences.setDefault(
		 * ConfigurationPreferencesPage.perspectiveLayout,
		 * ConfigurationPreferencesPage.prefLargeScreen); } else { //
		 * preferences are small screen preferences.setDefault(
		 * ConfigurationPreferencesPage.perspectiveLayout,
		 * ConfigurationPreferencesPage.prefSmallScreen); }
		 */
		preferences.setDefault(ConfigurationPreferencesPage.perspectiveLayout,
				ConfigurationPreferencesPage.prefLargeScreen);

		// Logger
		preferences.setDefault(PreferenceConstants.P_FABLE_LOGGER_LEVEL,
				LOGGER_LEVEL_INFO);
		preferences.setDefault(PreferenceConstants.P_ROOT_LOGGER_LEVEL,
				LOGGER_LEVEL_ERROR);

		// Image printing (Note that units must be set before the margins
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_UNITS,
				IMAGE_PRINT_UNITS);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_LEFT,
				IMAGE_PRINT_LEFT);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_RIGHT,
				IMAGE_PRINT_RIGHT);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_TOP,
				IMAGE_PRINT_TOP);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_BOTTOM,
				IMAGE_PRINT_BOTTOM);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_HALIGN,
				IMAGE_PRINT_HALIGN);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_VALIGN,
				IMAGE_PRINT_VALIGN);
		preferences.setDefault(PreferenceConstants.P_IMAGE_PRINT_ORIENT,
				IMAGE_PRINT_ORIENT);

		// Memory Usage
		preferences.setDefault(PreferenceConstants.P_MU_SHOW_LEGEND,
				MU_SHOW_LEGEND);
		preferences.setDefault(PreferenceConstants.P_MU_SHOW_MAX, MU_SHOW_MAX);
		preferences.setDefault(PreferenceConstants.P_MU_INTERVAL, MU_INTERVAL);
		preferences.setDefault(PreferenceConstants.P_MU_MAX_AGE, MU_MAX_AGE);
	}

}
