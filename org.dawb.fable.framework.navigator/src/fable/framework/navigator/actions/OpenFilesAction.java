/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

import fable.framework.navigator.views.SampleNavigatorView;

public class OpenFilesAction extends Action {
	String initialDirData = "";
	public static final String ID = "fable.framework.navigator.openFilesAction";

	@Override
	public void run() {
		openFiles();

	}

	public void openFiles() {

		FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell(),
				SWT.MULTI);

		SampleNavigatorView sampleView = (SampleNavigatorView) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(SampleNavigatorView.ID);
		initialDirData = SampleNavigatorView.getInitialDirectory();
		dlg.setFilterPath(initialDirData);
		String selectedDirectory = dlg.open();

		String files[] = dlg.getFileNames();

		if (selectedDirectory != null && files != null) {
			int index = selectedDirectory.lastIndexOf(System
					.getProperty("file.separator"));
			if (index > 0) {
				selectedDirectory = selectedDirectory.substring(0, index);

			}
			sampleView.addFiles(files, selectedDirectory);
		}
	}
}
