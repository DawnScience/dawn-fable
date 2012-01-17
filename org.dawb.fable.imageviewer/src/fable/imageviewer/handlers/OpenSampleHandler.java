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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.perspective.Perspective;

public class OpenSampleHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String initialDirData = "";
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

		// Must currently be null
		return null;
	}

}
