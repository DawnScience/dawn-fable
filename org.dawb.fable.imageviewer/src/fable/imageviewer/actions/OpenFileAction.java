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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.perspective.Perspective;

/**
 * Action to open image file(s) to display with the imageviewer. This class
 * implements the IWorkbenchWindowAction so it can be added to menu bar via the
 * Action extension point
 * 
 * @author Andy Gotz + Gaelle Suchet
 * 
 */
public class OpenFileAction implements IWorkbenchWindowActionDelegate {

	// private static final String ID =
	// "fable.imageviewer.actions.OpenFileAction";

	/**
	 * This is initial directory stored in SampleNavigatorView via a memento.
	 */
	private String initialDirData = "";

	public void run(IAction action) {

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			FileDialog dlg = new FileDialog(Display.getDefault()
					.getActiveShell(), SWT.MULTI);

			initialDirData = SampleNavigatorView.getInitialDirectory();
			dlg.setFilterPath(initialDirData);
			String selectedDirectory = dlg.open();
			if (selectedDirectory != null) {
				int index = selectedDirectory.lastIndexOf(System
						.getProperty("file.separator"));
				if (index > 0) {
					selectedDirectory = selectedDirectory.substring(0, index);

				}
				String files[] = dlg.getFileNames();

				if (selectedDirectory != null && files != null) {
					try {
						activeWindow.getWorkbench().showPerspective(
								Perspective.ID, activeWindow);
						SampleNavigatorView sampleView = (SampleNavigatorView) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().findView(
										SampleNavigatorView.ID);

						sampleView.addFiles(files, selectedDirectory);
					} catch (WorkbenchException ex) {
						FableUtils.excMsg(this, "ImageViewer cannot be opened",
								ex);
					}
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
