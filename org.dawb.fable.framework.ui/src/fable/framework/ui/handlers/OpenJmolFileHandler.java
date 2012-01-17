/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.views.JmolView;

public class OpenJmolFileHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			FableUtils.errMsg(this, "Cannot determine the workbench window");
			return null;
		}
		try {
			IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				FableUtils.errMsg(this, "Cannot determine the active page");
				return null;
			}
			JmolView view = (JmolView) page.findView(JmolView.getID());
			if (view == null) {
				page.showView(JmolView.getID());
				view = (JmolView) page.findView(JmolView.getID());
			}
			if (view != null) {
				view.browseFile();
			} else {
				FableUtils.errMsg(this, "Failed to find JmolView");
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Failed to open JmolView", ex);
		}

		// Must currently be null
		return null;
	}

}
