/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import fable.framework.navigator.Activator;

/**
 * This class is used in preference page for Sample chooser.
 * <p>
 * This class is responsible of setting favorite files to load in Sample
 * chooser.
 * <p>
 * User can add or remove a file type listed.
 * <p>
 * Default values available in <code>IVarKeys.FABIO_TYPES<code> can be 
 * retrieve by clicking on default button.
 * 
 * @author suchet
 * 
 */

public class SampleNavigatorPreferences extends PreferencePage implements IWorkbenchPreferencePage {
	
	
	private ImageDescriptor addDescriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/add.gif");
	private ImageDescriptor removeDescriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/delete.gif");

	private IPreferenceStore preferencesStore;
	
	
	private Button packColumn,buttonAdd,buttonRemove,useFabio;
	private Table table;
	private Text texttoAdd;
	private StringFieldEditor stem;
	private Composite composite;


	public void init(IWorkbench workbench) {
		preferencesStore = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());

	}

	@Override
	protected Control createContents(Composite parent) {
		
	
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL));

		useFabio = new Button(composite, SWT.CHECK);
		useFabio.setText("Use Fabio");
		useFabio.setSelection(preferencesStore.getBoolean(FabioPreferenceConstants.USE_FABIO));
		useFabio.setToolTipText("Requires installation of python and setting PYTHONPATH and LD_PRELOAD variables.");
		
		
		final Label installLabel = new Label(composite, SWT.WRAP);
		installLabel.setText(getPythonMessage());
		
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		useFabio.setLayoutData(gd);

		useFabio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnabled();
			}
		});
		
		packColumn = new Button(composite, SWT.CHECK);

		packColumn.setText("Fix column size in Image Navigator ?");
		packColumn.setLayoutData(gd);
		packColumn.setSelection(preferencesStore.getBoolean(FabioPreferenceConstants.FIX_COLUMN_SIZE));

		stem = new StringFieldEditor(FabioPreferenceConstants.STEM_NAME, "File name filter", composite);

		stem.setStringValue(preferencesStore.getString(FabioPreferenceConstants.STEM_NAME));
		stem
				.getTextControl(composite)
				.setToolTipText(
						"Enter a filter on the file name to display your favorites files in the table list.");

		Group grpExtension = new Group(composite, SWT.NONE);
		grpExtension.setText("Favorites file extensions");
		grpExtension
				.setToolTipText("Set your list of your favorites image files extensions to load in the table here.");
		grpExtension.setLayout(new GridLayout(2, false));
		GridData gdForgrp = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		gdForgrp.horizontalSpan = 2;
		gdForgrp.verticalSpan = 6;
		grpExtension.setLayoutData(gdForgrp);
		texttoAdd = new Text(grpExtension, SWT.BORDER);
		texttoAdd.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR
						|| e.keyCode == SWT.KEYPAD_ADD) {
					addText();
				}
			}
		});
		this. buttonAdd = new Button(grpExtension, SWT.PUSH);
		texttoAdd.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		buttonAdd.setText("Add to list");
		final Image imageAdd = addDescriptor.createImage();
		buttonAdd.setImage(imageAdd);
		buttonAdd.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageAdd != null && !imageAdd.isDisposed()) {
					imageAdd.dispose();
				}

			}
		});
		buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addText();

			}
		});

		table = new Table(grpExtension, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		table
				.setToolTipText("List of filter file types (fabio files) for navigator");
		final GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
		tableLayoutData.widthHint  = 100;
		tableLayoutData.heightHint = 200;
		table.setLayoutData(tableLayoutData);
		
		((GridData) table.getLayoutData()).horizontalSpan = 2;
		TableColumn fabioColumn = new TableColumn(table, SWT.LEFT, 0);
		fabioColumn.setText("Name");
		new TableColumn(table, SWT.LEFT, 0); // fabioType

		table.setHeaderVisible(true);

		String list = preferencesStore.getString(FabioPreferenceConstants.FILE_TYPE);
		populateList(list);
		table.setRedraw(true);

		this. buttonRemove = new Button(grpExtension, SWT.PUSH);
		GridData removeGd = new GridData(GridData.END, GridData.CENTER, false,
				false);
		removeGd.horizontalSpan = 2;
		buttonRemove.setLayoutData(removeGd);
		buttonRemove.setText("Remove from list");
		final Image imageRemove = removeDescriptor.createImage();
		buttonRemove.setImage(imageRemove);
		buttonRemove.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageRemove != null && !imageRemove.isDisposed()) {
					imageRemove.dispose();
				}

			}
		});
		buttonRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				int[] indices = table.getSelectionIndices();
				if (indices != null && indices.length > 0) {
					table.remove(indices);
				}

			}
		});

		updateEnabled();
		
		return composite;
	}

	private String getPythonMessage() {
		final StringBuilder buf = new StringBuilder();
		buf.append("In order to use Fabio you will need to:\n\n");
		buf.append(" - Install python and ensure that it works\n");
		buf.append(" - Add the folder 'fabio' in this product to the PYTHONPATH\n");
		buf.append(" - On linux set LD_PRELOAD, usually to /usr/lib/libpython2.6.so.1.0\n\n");
		return buf.toString();
	}

	protected void updateEnabled() {
		final boolean enabled = useFabio.getSelection();
		packColumn.setEnabled(enabled);
		table.setEnabled(enabled);
		texttoAdd.setEnabled(enabled);
		buttonAdd.setEnabled(enabled);
		buttonRemove.setEnabled(enabled);
		stem.setEnabled(enabled, composite);
	}

	@Override
	protected void performDefaults() {
		String list = preferencesStore.getDefaultString(FabioPreferenceConstants.FILE_TYPE);
		populateList(list);
		stem.setStringValue(preferencesStore.getDefaultString(FabioPreferenceConstants.STEM_NAME));
		packColumn.setSelection(preferencesStore.getBoolean(FabioPreferenceConstants.FIX_COLUMN_SIZE));
		useFabio.setSelection(preferencesStore.getBoolean(FabioPreferenceConstants.USE_FABIO));
	}

	private void populateList(String list) {
		table.removeAll();
		String[] split = list.split("\\|");
		if (split.length > 0) {
			for (int i = 0; i < split.length; i++) {
				TableItem it = new TableItem(table, SWT.NONE);
				it.setText(0, split[i].trim());

			}
		}
		table.getColumn(0).pack();
	}

	@Override
	public boolean performOk() {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			String text = item.getText();
			if (i == table.getItemCount() - 1) {
				buffer.append(text);
			} else {
				buffer.append(text + "|");
			}
		}
		preferencesStore.setValue(FabioPreferenceConstants.FILE_TYPE, buffer.toString());
		preferencesStore.setValue(FabioPreferenceConstants.STEM_NAME, stem.getStringValue());
		preferencesStore.setValue(FabioPreferenceConstants.FIX_COLUMN_SIZE, packColumn.getSelection());
		preferencesStore.setValue(FabioPreferenceConstants.USE_FABIO, useFabio.getSelection());

		return super.performOk();
	}

	/**
	 * This function append text available in text field to the list of types
	 * file used to filter files in navigator.
	 */
	private void addText() {
		String string = texttoAdd.getText();
		if (!string.equals("")) {
			TableItem item = new TableItem(table, SWT.None);
			item.setText(texttoAdd.getText());
			texttoAdd.setText("");
		}
	}

}
