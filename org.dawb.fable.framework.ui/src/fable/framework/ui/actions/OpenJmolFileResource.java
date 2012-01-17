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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.views.JmolView;

public class OpenJmolFileResource implements IObjectActionDelegate {
	private ISelection selection = null;

	/**
	 * Constructor for Action1.
	 */
	public OpenJmolFileResource() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IFile iFile = null;
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				iFile = null;
				Object element = it.next();
				if (element instanceof IFile) {
					iFile = (IFile) element;
				} else if (element instanceof IAdaptable) {
					iFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
				}
				if (iFile != null) {
					loadFile(iFile);
				}
			}
		}
	}

	/**
	 * Loads a .par IFile.
	 * 
	 * @param file
	 */
	private void loadFile(IFile iFile) {
		if (iFile == null) {
			return;
		}
		IPath iPath = iFile.getLocation();
		String path = null;
		if (iPath != null) {
			path = iPath.toString();
		}
		if (path == null) {
			FableUtils.errMsg(this, "Cannot determine the path");
			return;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			FableUtils.errMsg(this, "Cannot determine the workbench window");
			return;
		}
		try {
			IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				FableUtils.errMsg(this, "Cannot determine the active page");
				return;
			}
			JmolView view = (JmolView) page.findView(JmolView.getID());
			if (view == null) {
				page.showView(JmolView.getID());
				view = (JmolView) page.findView(JmolView.getID());
			}
			if (view != null) {
				view.openFile(path);
			} else {
				FableUtils.errMsg(this, "Failed to find JmolView");
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Failed to open JmolView", ex);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
