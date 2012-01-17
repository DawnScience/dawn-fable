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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.views.JmolView;

/**
 * This class implements a IWorkbenchWindowActionDelegate to open a file with
 * the JmolView
 * 
 * @author Andy Gotz
 * 
 */
public class OpenJmolFile implements IWorkbenchWindowActionDelegate,
		IObjectActionDelegate, IViewActionDelegate {

	private IViewPart targetView = null;

	// KE: Variables that are not really used are commented out
	// private IWorkbenchPart targetPart;

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
	}

	/**
	 * Action to either open a new column file plot view or browse for a file if
	 * this is called from a view
	 */
	public void run(IAction action) {
		try {
			if (targetView != null) {
				((JmolView) targetView).browseFile();
			} else {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(JmolView.ID,
								Integer.toString(JmolView.viewCount++),
								IWorkbenchPage.VIEW_ACTIVATE);
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Failed to open JmolView", ex);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	public void init(IViewPart view) {
		targetView = view;
	}

	public void setActivePart(IAction action, IWorkbenchPart part) {
		// KE: This is not used, even if it can be set
		// this.targetPart = part;
	}

}
