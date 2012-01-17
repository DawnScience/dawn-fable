/*******************************************************************************
 * Copyright 2007, UChicago Argonne, LLC
 * 
 * All Rights Reserved
 * 
 * X-Ray Analysis Software (XRAYS)
 * 
 * OPEN SOURCE LICENSE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. Software changes,
 * modifications, or derivative works, should be noted with comments and the
 * author and organizations name.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the names of UChicago Argonne, LLC or the Department of Energy nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * 4. The software and the end-user documentation included with the
 * redistribution, if any, must include the following acknowledgment:
 * 
 * "This product includes software produced by UChicago Argonne, LLC under
 * Contract No. DE-AC02-06CH11357 with the Department of Energy."
 * 
 * ***************************************************************************
 * 
 * DISCLAIMER
 * 
 * THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND.
 * 
 * NEITHER THE UNITED STATES GOVERNMENT, NOR THE UNITED STATES DEPARTMENT OF
 * ENERGY, NOR UCHICAGO ARGONNE, LLC, NOR ANY OF THEIR EMPLOYEES, MAKES ANY
 * WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR
 * RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY
 * INFORMATION, DATA, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS
 * THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 * 
 ******************************************************************************/

/*
 * Program to 
 * Created on Dec 27, 2006
 * By Kenneth Evans, Jr.
 */

package fable.framework.toolboxpreferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This is a General preference page to replace the one in org.eclipse.ui.ide
 * for preference pages that needs it as a parent. It is not needed and should
 * not be used if org.eclipse.ui.ide is used.
 * 
 * @author evans
 * 
 */
public class GeneralPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public GeneralPreferencePage() {
		setDescription("Fable General Preferences :");
	}

	public GeneralPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public GeneralPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 20;
		composite.setLayout(fillLayout);
		Label label = new Label(composite, SWT.CENTER | SWT.WRAP);
		label.setText("These are the general perferences for Fable.");
		return composite;
	}

	public void init(IWorkbench workbench) {
		// Remove the buttons since there is nothing to do
		noDefaultAndApplyButton();
	}

}
