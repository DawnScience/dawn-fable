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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.internal.IVarKeys;
import fable.framework.toolbox.Activator;

/**
 * The preference page for the log4j logger for changing preferences which are
 * then stored in the preference store.
 */

public class MemoryUsagePreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage, IVarKeys {

	public MemoryUsagePreferencesPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_MU_SHOW_LEGEND,
				"Show &Legend", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_MU_SHOW_MAX,
				"Show &Max", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.P_MU_INTERVAL,
				"Update &Interval (ms):", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.P_MU_MAX_AGE,
				"Maximum &Age (ms):", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
