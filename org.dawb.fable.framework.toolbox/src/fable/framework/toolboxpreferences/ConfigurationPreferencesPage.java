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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.toolbox.Activator;

/**
 * This class defines configuration preferences for fable's programs.<br>
 * For instance, preference define perspective used for small or large screen. <br>
 * A perspective defines the arrangement of workbench element. If screen size is
 * large, views are displayed on the same page, otherwise they are layered. In
 * preferences initializer (<code>
 * fable.framework.toolboxpreferences.PreferencesInitializer.java</code>
 * ), default perspective option depends on screen size.
 * 
 * @author suchet
 * 
 */
public class ConfigurationPreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String perspectiveLayout = "configuration.perspective";
	/**
	 * This constant is used to define a prefRadioGroupFieldEditorerence for all
	 * fable perspective for <b>small screen</b>. <br>
	 * Screen size is defined in <code>
	 * fable.toolbox.internal.IVarKeys.SCREENHEIGHT
	 * </code>
	 */
	public static final String prefSmallScreen = "configuration.perspective."
			+ "smallScreen";
	/**
	 * This constant is used to define a preference for all fable perspective
	 * for <b>large screen</b>. <br>
	 * Screen size is defined in <code>
	 * fable.toolbox.internal.IVarKeys.SCREENHEIGHT
	 * </code>
	 */
	public static final String prefLargeScreen = "configuration.perspective."
			+ "LargeScreen";

	/** Bundle preference store. */
	private IPreferenceStore preferencesStore;
	/**
	 * This radio buttons group is used to define a preferred perspective. By
	 * default, initializer set default pespective depending on screen size.
	 * 
	 */
	private RadioGroupFieldEditor preferredPerspective;

	public ConfigurationPreferencesPage() {
		super(GRID);
	}

	public void init(IWorkbench workbench) {
		preferencesStore = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferencesStore);

	}

	@Override
	protected void performDefaults() {
		preferredPerspective.loadDefault();

		super.performDefaults();
	}

	@Override
	public boolean performOk() {

		return super.performOk();
	}

	@Override
	protected void createFieldEditors() {
		preferredPerspective = new RadioGroupFieldEditor(perspectiveLayout,
				"Perspective", 2, new String[][] {
						{ "Small screen", prefSmallScreen },
						{ "Large screen", prefLargeScreen }, },
				getFieldEditorParent(), true);
		addField(preferredPerspective);

	}

}
