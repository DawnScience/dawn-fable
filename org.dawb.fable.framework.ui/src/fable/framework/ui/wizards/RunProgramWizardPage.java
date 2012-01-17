/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import fable.framework.toolbox.FileText;

public class RunProgramWizardPage extends WizardPage implements IWizardPage {

	/** Program to run. */
	private FileText programText;
	/** Input text for chosen program. */
	private FileText inputText;

	protected RunProgramWizardPage(String pageName) {
		super(pageName);

	}

	// @Override
	public void createControl(Composite parent) {
		Composite parent2 = new Composite(parent, SWT.NONE);
		Layout gridLayout = new GridLayout(3, false);
		parent2.setLayout(gridLayout);
		Label label1 = new Label(parent2, SWT.NONE);
		label1.setText("Program :");
		programText = new FileText(parent2, SWT.NONE);
		{
			programText.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
		}
		programText.getTextField().addModifyListener(new ModifyListener() {

			// @Override
			public void modifyText(ModifyEvent e) {
				validate();
			}

		});
		programText.set_isRequiredField(true);
		Button browseProgram = new Button(parent2, SWT.PUSH);
		browseProgram.setText("Browse...");
		browseProgram.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(((Button) e.widget).getParent()
						.getShell(), SWT.SIMPLE);
				// dlg.setFilterPath(currentPath);
				String sFile = dlg.open();
				if (sFile != null) {
					programText.set_Text(sFile);

				}

			}
		});

		Label label2 = new Label(parent2, SWT.NONE);
		label2.setText("Input file : ");
		inputText = new FileText(parent2, SWT.NONE);
		{
			inputText.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
		}
		inputText.getTextField().addModifyListener(new ModifyListener() {

			// @Override
			public void modifyText(ModifyEvent e) {
				validate();
			}

		});
		inputText.set_isRequiredField(true);

		Button browseInputFile = new Button(parent2, SWT.PUSH);
		browseInputFile.setText("Browse...");
		browseInputFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(((Button) e.widget).getParent()
						.getShell(), SWT.SIMPLE);
				// dlg.setFilterPath(currentPath);
				String sFile = dlg.open();
				if (sFile != null) {
					inputText.set_Text(sFile);

				}

			}
		});

		setPageComplete(false);
		setControl(parent2);

	}

	/**
	 * This method checks if page is valid :
	 * <UL>
	 * <LI>If required fields are filled,
	 * <LI>If files exist.
	 * </UL>
	 */
	private void validate() {
		if (programText.getText().trim().length() == 0) {
			setErrorMessage("Please, select a program.");
			setPageComplete(false);
		} else if (inputText.getText().trim().length() == 0) {
			setErrorMessage("Please select an input file.");
			setPageComplete(false);
		} else if (!programText.is_bValide()) {
			setErrorMessage(programText.getErrorMessage());
			setPageComplete(false);
		} else if (!inputText.is_bValide()) {
			setErrorMessage(inputText.getErrorMessage());
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	public String getProgram() {
		return programText.getText();

	}

	public String getInputFile() {
		return inputText.getText();
	}
}
