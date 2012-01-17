/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;

import fable.python.views.PythonConsole;

public class OpenPythonConsoleAction implements IWorkbenchWindowActionDelegate {
	private final static String ID = "fable.python.actions.openPythonConsoleAction";
	private static PythonConsole pythonConsole = null;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		try {
			
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage()
					.findView(IConsoleConstants.ID_CONSOLE_VIEW) == null) {
				// show view
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(
								IConsoleConstants.ID_CONSOLE_VIEW, "0",
								IWorkbenchPage.VIEW_ACTIVATE);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		pythonConsole = new PythonConsole();
		pythonConsole.run();

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	public static String getID() {
		return ID;
	}

}
