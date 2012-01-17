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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.editors.ColumnFileEditor;
import fable.framework.ui.editors.ColumnFileEditorInput;
import fable.framework.ui.editors.ColumnFilePlotEditor;
import fable.framework.views.FableIOConsole;

public class OpenColumnFileEditorHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (window == null) {
			FableUtils.errMsg(this, "Cannot determine the workbench window");
			return null;
		}
		final FileDialog dialog = new FileDialog(window.getShell(), SWT.MULTI);
		dialog.setFilterNames(new String[] { "*.flt", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.flt", "*.*" });

		if (dialog.open() != null) {
			final String path = dialog.getFilterPath();
			// String file = dialog.getFileName();
			final String[] files = dialog.getFileNames();
			for (int i = 0; i < files.length; i++) {
				final String name = files[i];
				final String fileToLoad = path + File.separatorChar + files[i];
				final Job job = new Job("Load column file job " + name) {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						// setProperty(IProgressConstants.ICON_PROPERTY,
						// pluginImage);
						monitor
								.beginTask("Please wait while loading file "
										+ "in editor " + name,
										IProgressMonitor.UNKNOWN);

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						monitor.done();

						IEditorInput input = new ColumnFileEditorInput(
								fileToLoad);
						IWorkbenchPage page = window.getActivePage();
						openEditors(input, page);
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
				job.addJobChangeListener(new JobChangeAdapter() {
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

		// Must currently be null
		return null;
	}

	/**
	 * Tries to open a ColumnFileEditor and a ColumnFilePlotEditor on the given
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

}
