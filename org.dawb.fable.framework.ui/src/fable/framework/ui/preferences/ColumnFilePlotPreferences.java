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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.ui.rcp.Activator;


public class ColumnFilePlotPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static String X_LABEL="prefs_plot_XLabel";
	public static String Y_LABEL="prefs_plot_YLabel";
	private IPreferenceStore preferencesStore;
	public StringFieldEditor pref_y, pref_x;
	
	public ColumnFilePlotPreferences(){
		super(FieldEditorPreferencePage.GRID);
		preferencesStore=Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferencesStore);
	}
		

	@Override
	protected Control createContents(Composite parent) {
		
		return super.createContents(parent);
	}

	public void init(IWorkbench workbench) {
	

	}

	@Override
	protected void createFieldEditors() {
		Composite composite = getFieldEditorParent();
		pref_x=new StringFieldEditor(X_LABEL, "X label", composite);
		addField(pref_x);
		pref_y=new StringFieldEditor(Y_LABEL, "Y label", composite);
		addField(pref_y);
	}

	@Override
	protected void performDefaults() {
		
		pref_x.loadDefault();
		pref_y.loadDefault();
	}

	@Override
	public boolean performOk() {
		pref_x.store();
		pref_y.store();
		return super.performOk();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		
		super.propertyChange(event);
	}

}
