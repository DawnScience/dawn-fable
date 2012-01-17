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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.editors.ColumnFileEditorInput;
import fable.framework.views.FableIOConsole;

/**
 * Opens a ColFile from an IFile resource.
 * 
 * @author evans
 * 
 */
public class OpenColumnFileResourceAction implements IObjectActionDelegate {
	private ISelection selection = null;

	/**
	 * Constructor for Action1.
	 */
	public OpenColumnFileResourceAction() {
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
	 * Loads a .xem PeakSearch options IFile.
	 * 
	 * @param file
	 */
	private void loadFile(IFile iFile) {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			try {
				final String fileToLoad = iFile.getLocation().toString();
				final Job j = new Job("Load column file job " + fileToLoad) {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						// setProperty(IProgressConstants.ICON_PROPERTY,
						// pluginImage);
						monitor.beginTask("Please wait while loading file "
								+ "in editor " + fileToLoad,
								IProgressMonitor.UNKNOWN);

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						IEditorInput input = new ColumnFileEditorInput(
								fileToLoad);
						IWorkbenchPage page = window.getActivePage();

						monitor.done();
						ColumnEditorAction.openEditors(input, page);
						return Status.OK_STATUS;
					}
				};
				j.setUser(true);
				j.schedule();
				j.addJobChangeListener(new JobChangeAdapter() {
					public void done(final IJobChangeEvent event) {
						if (event.getResult().isOK()) {
							if (FableIOConsole.console != null) {
								FableIOConsole.console.displayOut(event
										.getJob().getName()
										+ " completed successfully");
							}

						} else {
							if (FableIOConsole.console != null) {
								FableIOConsole.console.displayOut(event
										.getJob().getName()
										+ " did not complete successfully");
							}
						}
					}
				});
			} catch (Exception ex) {
				FableUtils.excMsg(this, "Error opening ColFile", ex);
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
