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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.ToolBox;
import fable.framework.views.FableMessageConsole;
import fable.imageviewer.perspective.Perspective;
import fable.imageviewer.perspective.PerspectiveSmall;

public class OpenPerspectiveHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			String id = Perspective.ID;
			if (ToolBox.isSmallPerspectiveSet()) {
				id = PerspectiveSmall.ID;
			}
			try {
				PlatformUI.getWorkbench().showPerspective(id, window);
				if (FableMessageConsole.console != null) {
					FableMessageConsole.console
							.displayInfo("Welcome to ImageViewer");
				}
			} catch (WorkbenchException ex) {
				FableUtils.excMsg(this,
						"Error opening ImageViewer perspective", ex);
			}
		}

		// Must currently be null
		return null;
	}

}
