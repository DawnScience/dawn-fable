/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.perspective.Perspective;

public class OpenFileHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			FileDialog dlg = new FileDialog(Display.getDefault()
					.getActiveShell(), SWT.MULTI);

			String initialDirData = SampleNavigatorView.getInitialDirectory();
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

		// Must currently be null
		return null;
	}

}
