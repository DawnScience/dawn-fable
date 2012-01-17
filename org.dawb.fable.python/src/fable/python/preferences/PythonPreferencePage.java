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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.toolbox.TreeWithAddRemove;
import fable.python.rcp.Activator;

/**
 * This class is a python preferences pages. At this time, the only preference
 * available is dedicated to a prefered directory where user can save python
 * script recorded when using python commands via FableJep.
 * 
 * @author SUCHET
 * 
 */
public class PythonPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private DirectoryFieldEditor preferredScriptDirectory;
	private TreeWithAddRemove pythonPathTree;
	private IPreferenceStore preferencesStore;

	public PythonPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public PythonPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public PythonPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createContents(Composite parent) {
		
		
		// Rely on the field editor parent being a Composite with a GridData
		// layout. Set the span to be 2 columns. Will have to be modified if
		// there are field editors with more than 2 columns.
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		Label label = new Label(composite, SWT.WRAP);
		label.setText("Choose the directory for scripts :");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1).applyTo(
				label);

		preferredScriptDirectory = new DirectoryFieldEditor(
				PreferenceConstants.P_SCRIPT_PATH, "Script directory",
				composite);
		preferredScriptDirectory.setStringValue(preferencesStore
				.getString(PreferenceConstants.P_SCRIPT_PATH));

		label = new Label(composite, SWT.WRAP);
		label.setText("Choose the PYTHONPATH :");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1).applyTo(
				label);

		// Use SWT.NONE here. SWT.DEFAULT results in a scrolled window without
		// the contents on some platforms
		pythonPathTree = new TreeWithAddRemove(composite, SWT.NONE,
				preferencesStore.getString(PreferenceConstants.P_PYTHON_PYTHONPATH));
		GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(
				pythonPathTree);

		return composite;
	}

	public void init(IWorkbench workbench) {
		preferencesStore = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferencesStore);
	}

	@Override
	protected void performDefaults() {
		preferredScriptDirectory.setStringValue(preferencesStore
				.getDefaultString(PreferenceConstants.P_SCRIPT_PATH));
		String id = PreferenceConstants.P_PYTHON_PYTHONPATH;
		pythonPathTree.resetTreeItems(preferencesStore.getDefaultString(id));
		// ldPreLoad.setStringValue(preferencesStore.getDefaultString(PreferenceConstants.P_LD_PRELOAD));
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		preferencesStore.setValue(PreferenceConstants.P_SCRIPT_PATH,
				preferredScriptDirectory.getStringValue());
		String id = PreferenceConstants.P_PYTHON_PYTHONPATH;
		preferencesStore.setValue(id, pythonPathTree.getTreeItemsAsString());
		// preferencesStore.setValue(PreferenceConstants.P_LD_PRELOAD,
		// ldPreLoad.getStringValue());
		// Putenv.init();
		// Putenv.putenv("LD_PRELOAD", ldPreLoad.getStringValue());
		return super.performOk();
	}
}
