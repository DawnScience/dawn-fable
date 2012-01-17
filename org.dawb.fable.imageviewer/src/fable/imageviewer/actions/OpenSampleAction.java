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
package fable.imageviewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.perspective.Perspective;

/**
 * Action to open a directory containing image file(s) to display with the
 * imageviewer
 * 
 * This class implements the IWorkbenchWindowAction so it can be added to menu
 * bar via the Action extenion point
 * 
 * @author Andy Gotz + Gaelle Suchet
 * 
 */
public class OpenSampleAction implements IWorkbenchWindowActionDelegate {
	// private final static String ID =
	// "fable.imageviewer.actions.OpenSampleAction";

	String initialDirData = "";

	public void run(IAction action) {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			DirectoryDialog dlg = new DirectoryDialog(Display.getDefault()
					.getActiveShell(), SWT.NONE);
			initialDirData = SampleNavigatorView.getInitialDirectory();
			if (initialDirData != null) {
				dlg.setFilterPath(initialDirData);
			}
			String selectedDirectory = dlg.open();
			if (selectedDirectory != null) {
				// add directories
				try {
					activeWindow.getWorkbench().showPerspective(Perspective.ID,
							activeWindow);
					SampleNavigatorView sampleView = (SampleNavigatorView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().findView(SampleNavigatorView.ID);

					sampleView.addDirectory(selectedDirectory);// listOfSamples
					// instantiated
				} catch (WorkbenchException ex) {
					FableUtils.excMsg(this,
							"SampleNavigatorView cannot be opened", ex);
				}
			}
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}
}
