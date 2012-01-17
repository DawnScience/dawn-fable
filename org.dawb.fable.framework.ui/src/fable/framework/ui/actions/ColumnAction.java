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

import jep.JepException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.framework.ui.views.ColumnFileContentView;
import fable.framework.ui.views.ColumnFilePlotView;
import fable.python.ColumnFile;

public class ColumnAction implements IWorkbenchWindowActionDelegate {
	private final static String ID = "fable.framework.views.actions.ColumnAction";
	private int nView_plot = 0;
	private int nView_Col = 0;

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
	}

	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			FileDialog dialog = new FileDialog(window.getShell(), SWT.MULTI);
			dialog.setFilterNames(new String[] { "*.flt", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.flt", "*.*" });
			// If not dialog is canceled

			if (dialog.open() != null) {
				String path = dialog.getFilterPath();
				// String file = dialog.getFileName();
				String[] files = dialog.getFileNames();
				for (int i = 0; i < files.length; i++) {
					try {
						IViewPart[] stack_plot = (PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.getViewStack(ColumnFilePlotView.view));
						if (stack_plot != null) {
							nView_plot = stack_plot.length;
						}
						IViewPart[] stack_Table = (PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.getViewStack(ColumnFileContentView.view));
						if (stack_Table != null) {
							nView_Col = stack_Table.length;
						}
						String filename = path
								+ System.getProperty("file.separator")
								+ files[i];

						// ColumnFileContentView.view.

						try {
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.showView(ColumnFilePlotView.ID,
											"\"" + nView_plot + "\"",
											IWorkbenchPage.VIEW_ACTIVATE);
							ColumnFile col;
							col = new ColumnFile(filename);
							ColumnFilePlotView.view.setColumnFile(col);

							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.showView(ColumnFileContentView.ID,
											"\"" + nView_Col + "\"",
											IWorkbenchPage.VIEW_ACTIVATE);
							ColumnFileContentView.view.setColumnFile(col);
						} catch (JepException e) {
							FableUtils.errMsg(this,
									"Error while parsing this file :"
											+ files[i]);
						}

					} catch (Throwable ex) {
						FableUtils.excMsg(this,
								"Error opening ColumnFilePlotView", ex);
					}
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	public static String getID() {
		return ID;
	}

}
