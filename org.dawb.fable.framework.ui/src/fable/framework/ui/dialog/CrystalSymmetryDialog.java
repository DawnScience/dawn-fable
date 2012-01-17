/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * 
 */
package fable.framework.ui.dialog;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import fable.framework.internal.ICrystalSymmetryVarKeys;
import fable.framework.ui.internal.IVarKeys;
import fable.framework.ui.rcp.Activator;

/**
 * @author david This class creates a dialog box to get space group number if
 *         user knows crystal symbol based on IUCr table.
 */
public class CrystalSymmetryDialog extends Dialog {

	private Combo spaceGroupSymbolCombo;
	private Shell shell;
	private Spinner spaceGroupSpinner;
	private static final int COMBO_BOX_WIDTH = 50;
	private Text spaceSystemText;
	private int spaceGroup = 1, spaceGroupSystem = 0;

	public CrystalSymmetryDialog(Shell parentShell, int spgr) {
		this(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, spgr);
	}

	public CrystalSymmetryDialog(Shell parentShell, int style, int spgr) {
		super(parentShell, style);
		setText("Space Group");
		spaceGroup = spgr;
	}

	public int open() {
		shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, true));
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return spaceGroup;
	}

	protected Control createContents(Composite container) {
		//int numColInGroup = 2;
		/*Group group = LookAndFeel.getGroup(container, "", 2,
				numColInGroup);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setToolTipText(IVarKeys.spaceGroupDesc);*/
		Composite composite = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
				false).applyTo(composite);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		new Label(composite, SWT.NONE).setText("Symbol");
		spaceGroupSymbolCombo = new Combo(composite, SWT.READ_ONLY | SWT.RESIZE
				| SWT.DROP_DOWN);

		spaceGroupSymbolCombo.setLayoutData(new GridData(SWT.FILL, SWT.RESIZE,
				true, false));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
				false).hint(COMBO_BOX_WIDTH, SWT.DEFAULT).applyTo(
				spaceGroupSymbolCombo);

		spaceGroupSymbolCombo.setToolTipText(IVarKeys.spaceGroupDesc);
		for (int i = 0; i < ICrystalSymmetryVarKeys.MAX_SPACEGROUP_VALUE; i++) {
			spaceGroupSymbolCombo
					.add(ICrystalSymmetryVarKeys.SPACE_GROUP_SYMBOLS[i]);
		}
		spaceGroupSymbolCombo.select(0);
		spaceGroupSymbolCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// The number is 1 more than the index
				spaceGroup = spaceGroupSymbolCombo.getSelectionIndex() + 1;
				setSpaceGroup(spaceGroup);
			}
		});

		// Number
		composite = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
				false).applyTo(composite);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		new Label(composite, SWT.NONE).setText("Number");
		spaceGroupSpinner = new Spinner(composite, SWT.SINGLE | SWT.BORDER
				| SWT.FILL | SWT.RESIZE);
		spaceGroupSpinner.setToolTipText(IVarKeys.spaceGroupDesc);
		spaceGroupSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.RESIZE,
				true, false));
		spaceGroupSpinner
				.setMinimum(ICrystalSymmetryVarKeys.MIN_SPACEGROUP_VALUE);
		spaceGroupSpinner
				.setMaximum(ICrystalSymmetryVarKeys.MAX_SPACEGROUP_VALUE);
		spaceGroupSpinner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spaceGroup = spaceGroupSpinner.getSelection();
				setSpaceGroup(spaceGroup);
			}
		});

		// System
		composite = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true,
				false).span(2, 1).applyTo(composite);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		new Label(composite, SWT.NONE).setText("Crystal System:");
		spaceSystemText = new Text(composite, SWT.NONE);
		spaceSystemText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(
				spaceSystemText);
		spaceSystemText
				.setText(ICrystalSymmetryVarKeys.SPACE_GROUP_TYPES[spaceGroupSystem]);
		spaceSystemText.setToolTipText(IVarKeys.crystalSystemDesc);
		createButtons();
		setSpaceGroup(spaceGroup);
		return container;

	}

	private void createButtons() {
		Composite composite = new Composite(shell, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		Button ok = new Button(composite, SWT.PUSH | SWT.RESIZE);
		ok.setText("Ok");
		ImageDescriptor descriptor = Activator
				.getImageDescriptor("images/add.gif");
		if (descriptor != null) {
			Image image = descriptor.createImage();
			if (image != null) {
				ok.setImage(image);

			}
		}
		ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		// Create the cancel button and add a handler
		// so that pressing it will set input to null
		Button cancel = new Button(composite, SWT.PUSH | SWT.RESIZE);
		cancel.setText("Cancel");
		descriptor = Activator.getImageDescriptor("images/delete.gif");
		if (descriptor != null) {
			Image image = descriptor.createImage();
			if (image != null) {
				cancel.setImage(image);

			}
		}
		cancel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				spaceGroup = 0;
				shell.close();
			}
		});

		// Set the OK button as the default, so
		// user can type input and press Enter
		// to dismiss
		shell.setDefaultButton(ok);
	}

	/**
	 * set the space group and update the space group type and associated combo
	 * boxes
	 * 
	 * @param _spaceGroup
	 *            - space group (1 to 230)
	 */
	public void setSpaceGroup(int spaceGroup) {
		this.spaceGroup = spaceGroup;

		// Set the controls
		if (spaceGroupSymbolCombo.getSelectionIndex() != spaceGroup - 1) {
			spaceGroupSymbolCombo.select(spaceGroup - 1);
		}
		if (spaceGroupSpinner.getSelection() != spaceGroup) {
			spaceGroupSpinner.setSelection(spaceGroup);
		}

		// Determine space group system
		int newspaceGroupSystem = 0;
		for (int i = 0; i < ICrystalSymmetryVarKeys.SPACE_GROUP_MAXIMUM.length; i++) {
			if (spaceGroup <= ICrystalSymmetryVarKeys.SPACE_GROUP_MAXIMUM[i]) {
				newspaceGroupSystem = i;
				break;
			}
		}
		spaceGroupSystem = newspaceGroupSystem;
		spaceSystemText
				.setText(ICrystalSymmetryVarKeys.SPACE_GROUP_TYPES[spaceGroupSystem]);
	}
}
