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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.editors.ColumnFileEditor;
import fable.framework.ui.editors.ColumnFileEditorInput;
import fable.framework.ui.editors.ColumnFilePlotEditor;
import fable.framework.views.FableIOConsole;

/**
 * This class opens two editors for a Column File : <BR>
 * - <code>ColumnFileEditor</code> <br>
 * - <code>ColumnFilePlotEditor</code> This action is available fron Menu
 * Peaksearch/Opens filtered peaks file.
 * 
 * @author SUCHET
 * 
 */
public class ColumnEditorAction implements IWorkbenchWindowActionDelegate {

	public static final String ID = "fable.framework.ui.actions.ColumnEditorAction";
	private Display display;
	private IWorkbenchPage page;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(final IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method opens a file selector to choose a "Column File", that means,
	 * a file that contains columns of data. It opens two editors with the same
	 * input file :
	 * <UL>
	 * <LI><code>ColumnFileEditor</Code> : a table that represents columnFile.
	 * <LI><code>ColumnFilePlotEditor</code>: a plot that represents data in
	 * table.
	 * </UL>
	 */
	public void run(final IAction action) {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		setDisplay(window.getShell().getDisplay());
		if (window != null) {
			page = window.getActivePage();
			final FileDialog dialog = new FileDialog(window.getShell(),
					SWT.MULTI);
			dialog.setFilterNames(new String[] { "*.flt", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.flt", "*.*" });

			if (dialog.open() != null) {
				final String path = dialog.getFilterPath();
				// String file = dialog.getFileName();
				final String[] files = dialog.getFileNames();
				for (int i = 0; i < files.length; i++) {
					final String name = files[i];
					final String fileToLoad = path + File.separatorChar
							+ files[i];
					final Job j = new Job("Load column file job " + name) {
						@Override
						protected IStatus run(final IProgressMonitor monitor) {
							// setProperty(IProgressConstants.ICON_PROPERTY,
							// pluginImage);
							monitor.beginTask("Please wait while loading file "
									+ "in editor " + name,
									IProgressMonitor.UNKNOWN);

							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							IEditorInput input = new ColumnFileEditorInput(
									fileToLoad);

							monitor.done();
							openEditors(input, page);
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
				}
			}
		}
	}

	/**
	 * Tries to open a ColumnFileEditor or ColumnFilePlotEditor on the given
	 * IEditorInput on the given IWorkbenchPage.
	 * 
	 * @param input
	 * @param page
	 */
	public static void openEditors(final IEditorInput input,
			final IWorkbenchPage page) {
		Display.getDefault().syncExec(new Runnable() {
			// @Override
			public void run() {
				if (page != null) {
					final String id = ColumnFileEditor.getId();
					try {
						page.openEditor(input, id, true);
					} catch (final PartInitException e) {
						FableUtils.errMsg(this, e.getMessage());
					}
					// Plot
					final String plotId = ColumnFilePlotEditor.getId();

					try {
						page.openEditor(input, plotId, true);
					} catch (final PartInitException e) {
						FableUtils.errMsg(this, e.getMessage());
					}
				}
			}
		});
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		// TODO Auto-generated method stub

	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Display getDisplay() {
		return display;
	}

}
