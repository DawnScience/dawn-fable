/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.ui.UpdateManagerUI;

public class UpdateAction extends Action implements IAction {

	private IWorkbenchWindow window;

	public UpdateAction(IWorkbenchWindow window) {
		this.window = window;
		setId("fable.framework.ui.action.UpdateAction");
		setText("&Software Updates ...");
		setToolTipText("Search for software updates");
	}

	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(),
				new Runnable() {
			public void run() {
				/* the following lines should check for updates for the installed features 
				 * but currently they always return saying there are no new features
				 * therefore I am using the update installer wizard for now (next line)
				 */
//				UpdateJob job = new UpdateJob("Searching for updates", false, false);
//				UpdateManagerUI.openInstaller(window.getShell(), job);
				UpdateManagerUI.openInstaller(window.getShell());
			}
		});
	}

}
