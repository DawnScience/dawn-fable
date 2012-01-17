/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.toolbox.FableUtils;
import fable.python.views.PythonConsole;

public class OpenPythonConsoleHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage()
					.findView(IConsoleConstants.ID_CONSOLE_VIEW) == null) {
				IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindow(event);
				window.getActivePage().showView(
						IConsoleConstants.ID_CONSOLE_VIEW, "0",
						IWorkbenchPage.VIEW_ACTIVATE);
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Cannot open Console View", ex);
			return null;
		}
		PythonConsole pythonConsole = new PythonConsole();
		pythonConsole.run();

		// Must currently be null
		return null;
	}

}
