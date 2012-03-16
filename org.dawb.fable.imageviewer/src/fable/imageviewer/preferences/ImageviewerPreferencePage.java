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

import org.dawb.common.ui.image.PaletteFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.rcp.Activator;

/**
 * The Imageviewer preference page for changing preferences which are then
 * stored in the preference store.
 */

public class ImageviewerPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage, IImagesVarKeys {

	public ImageviewerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		/*
		 * could be used to set the starting directory
		 * 
		 * addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH,
		 * "&Directory preference:", getFieldEditorParent()));
		 */
		addField(new StringFieldEditor(PreferenceConstants.P_MAX_SAMPLES,
				"Number of &samples to open :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_IMAGES_PER_TAB,
				"Number of images per &tab :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_IMAGES_CACHED,
				"Number of images to &cache :", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceConstants.P_XLABEL,
				"&X Label :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_YLABEL,
				"&Y Label :", getFieldEditorParent()));

		String[][] choices = new String[PaletteFactory.PALETTES.size()][2];
		int i = -1;
		for (String name : PaletteFactory.PALETTES.keySet()) {
			i++;
			choices[i]=new String[]{name, PaletteFactory.PALETTES.get(name).toString()};
		}
		addField(new ComboFieldEditor(PreferenceConstants.P_PALETTE,
				"Default &palette :", choices, getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_ORIENT,
				"Default &orientation :", orientNameValues,
				getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceConstants.P_COORD,
				"Default coordinate &system :", coordNameValues,
				getFieldEditorParent()));
		// No accelerator key for these
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_X0,
				"Custom coordinates x0 :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_Y0,
				"Custom coordinates y0 :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_PIXELWIDTH,
				"Custom coordinates pixel width :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_PIXELHEIGHT,
				"Custom coordinates pixel height :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_XNAME,
				"Custom coordinates x name :", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_COORD_YNAME,
				"Custom coordinates y name :", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_RELIEFMOVE,"Move 3D relief",getFieldEditorParent()));
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
