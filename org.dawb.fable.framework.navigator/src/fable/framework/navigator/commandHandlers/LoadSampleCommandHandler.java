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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.navigator.views.SampleNavigatorView;

public class LoadSampleCommandHandler implements IHandler {

	String initialDirData = "";

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		DirectoryDialog dlg = new DirectoryDialog(HandlerUtil
				.getActiveWorkbenchWindow(event).getShell());
	
		initialDirData = SampleNavigatorView.getInitialDirectory();
		if (initialDirData != null) {
			dlg.setFilterPath(initialDirData);
		}

		String selectedDirectory = dlg.open();

		if (selectedDirectory != null) {
			// add directories
			SampleNavigatorView.view.addDirectory(selectedDirectory);// listOfSamples
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
