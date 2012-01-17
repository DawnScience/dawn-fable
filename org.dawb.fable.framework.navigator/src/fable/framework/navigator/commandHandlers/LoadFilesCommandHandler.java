/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.commandHandlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;

public class LoadFilesCommandHandler implements IHandler {
	String initialDirData = "";

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			FableUtils.errMsg(this, "Cannot determine the workbench window");
			return null;
		}
		FileDialog dlg = new FileDialog(window.getShell(), SWT.MULTI);
		// add directories

		initialDirData = SampleNavigatorView.getInitialDirectory();
		dlg.setFilterPath(initialDirData);
		String selectedDirectory = dlg.open();
		String files[] = dlg.getFileNames();
		if (selectedDirectory != null && files != null) {

			SampleNavigatorView.view.addFiles(files, initialDirData);// listOfSamples
			// instantiated

		}

		return null;
	}

	public boolean isEnabled() {

		return true;
	}

	public boolean isHandled() {

		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
