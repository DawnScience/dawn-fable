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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.internal.IVarKeys;
import fable.framework.toolbox.Activator;

/**
 * The preference page for the log4j logger for changing preferences which are
 * then stored in the preference store.
 */

public class LoggerPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage, IVarKeys {

	public LoggerPreferencesPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		// Rely on the field editor parent being a Composite with a GridData
		// layout. Set the span to be 2 columns. Will have to be modified if
		// there are field editors with more than 2 columns.
		Composite parent = getFieldEditorParent();

		Label label = new Label(parent, SWT.WRAP);
		label.setText("The Fable logger is the main log4j logger.");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(
				label);
		addField(new ComboFieldEditor(PreferenceConstants.P_FABLE_LOGGER_LEVEL,
				"Fable logger level :", loggerLevels, parent));

		label = new Label(parent, SWT.WRAP);
		label.setText("The log4j root logger hierarchy is used "
				+ "primarily for debugging specific classes.");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(
				label);
		addField(new ComboFieldEditor(PreferenceConstants.P_ROOT_LOGGER_LEVEL,
				"Root logger level :", loggerLevels, parent));
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
