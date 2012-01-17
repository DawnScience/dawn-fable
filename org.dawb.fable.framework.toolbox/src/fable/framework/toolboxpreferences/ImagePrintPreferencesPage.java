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
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.internal.IVarKeys;
import fable.framework.toolbox.Activator;

/**
 * The preference page for image printing for changing preferences which are
 * then stored in the preference store.
 */
public class ImagePrintPreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage, IVarKeys {

	public ImagePrintPreferencesPage() {
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

		addField(new ComboFieldEditor(PreferenceConstants.P_IMAGE_PRINT_UNITS,
				"Default units :", imagePrinterUnitsOpts, parent));
		addField(new StringFieldEditor(PreferenceConstants.P_IMAGE_PRINT_LEFT,
				"Left margin :", parent));
		addField(new StringFieldEditor(PreferenceConstants.P_IMAGE_PRINT_RIGHT,
				"Right margin :", parent));
		addField(new StringFieldEditor(PreferenceConstants.P_IMAGE_PRINT_TOP,
				"Top margin :", parent));
		addField(new StringFieldEditor(
				PreferenceConstants.P_IMAGE_PRINT_BOTTOM, "Bottom margin :",
				parent));
		Label label = new Label(parent, SWT.WRAP);
		label.setText("Note: The margins may be given as a number and a unit, for\n" +
				      "example \"1.234 cm\".  The unit must be one of the default units.\n" +
				      "If given as a number only, then the default unit will be used.");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(label);
		addField(new ComboFieldEditor(PreferenceConstants.P_IMAGE_PRINT_HALIGN,
				"Horizontal alignment :", imagePrinterHAlignOpts, parent));
		addField(new ComboFieldEditor(PreferenceConstants.P_IMAGE_PRINT_VALIGN,
				"Vertical alignment :", imagePrinterVAlignOpts, parent));
		addField(new ComboFieldEditor(PreferenceConstants.P_IMAGE_PRINT_ORIENT,
				"Orientation :", imagePrinterOrientOpts, parent));

		label = new Label(parent, SWT.WRAP);
		label.setText("Note: The orientation is for the preview.  "
				+ "The printer orientation\n"
				+ "will still have to be set for the printer when you print.");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(
				label);
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
