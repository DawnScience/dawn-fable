/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.ToolBox;
import fable.imageviewer.perspective.Perspective;
import fable.imageviewer.perspective.PerspectiveSmall;

public class OpenImageViewerResourceAction implements IObjectActionDelegate {
	private ISelection selection = null;

	/**
	 * Constructor for Action1.
	 */
	public OpenImageViewerResourceAction() {
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
		IFolder iFolder = null;
		ArrayList<String> list = new ArrayList<String>();
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				iFile = null;
				iFolder = null;
				Object element = it.next();
				if (element instanceof IFile) {
					iFile = (IFile) element;
				} else if (element instanceof IFolder) {
					iFolder = (IFolder) element;
				} else if (element instanceof IAdaptable) {
					iFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
					iFolder = (IFolder) ((IAdaptable) element)
							.getAdapter(IFolder.class);
				}
				if (iFile != null) {
					// Accumulate items
					list.add(iFile.getLocation().toString());
				} else if (iFolder != null) {
					loadFolder(iFolder);
				}
			}
		}
		// We have accumulates the files, now process them
		processFiles(list);
	}

	/**
	 * Processes the list of file so they can be added directory by directory.
	 * 
	 * @param list
	 */
	void processFiles(ArrayList<String> list) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			try {
				if (ToolBox.isSmallPerspectiveSet()) {
					PlatformUI.getWorkbench().showPerspective(
							PerspectiveSmall.ID, window);
				} else {
					PlatformUI.getWorkbench().showPerspective(Perspective.ID,
							window);
				}
			} catch (WorkbenchException ex) {
				FableUtils.excMsg(this,
						"Error opening ImageViewer perspective", ex);
				return;
			}

			try {
				SampleNavigatorView sampleView = (SampleNavigatorView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findView(SampleNavigatorView.ID);

				// Sort the list so the directories will be in order
				Collections.sort(list);
				// Loop over the files, accumulating them into directory, then
				// add the directory to the sampleView.
				ListIterator<String> iter = list.listIterator();
				String curDir = null;
				String prevDir = null;
				String fileName;
				File file;
				ArrayList<String> fileList = new ArrayList<String>();
				String files[];
				while (iter.hasNext()) {
					String filePath = iter.next();
					file = new File(filePath);
					curDir = file.getParent();
					fileName = file.getName();
					if (curDir.equals(prevDir)) {
						// Add to the file list
						fileList.add(fileName);
					} else {
						// Process the prevDir
						if (prevDir != null && !fileList.isEmpty()) {
							files = new String[fileList.size()];
							fileList.toArray(files);
							sampleView.addFiles(files, prevDir);
						}
						// Start a new directory
						prevDir = curDir;
						fileList.clear();
						fileList.add(fileName);
					}
					// Start a new directory
					files = new String[fileList.size()];
				}
				// Process the last directory
				if (prevDir != null && !fileList.isEmpty()) {
					files = new String[fileList.size()];
					fileList.toArray(files);
					sampleView.addFiles(files, prevDir);
				}
			} catch (Exception ex) {
				FableUtils.excMsg(this, "Error loading Image File", ex);
			}
		}
	}

	/**
	 * Loads an image IFolder.
	 * 
	 * @param file
	 */
	private void loadFolder(IFolder iFolder) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			try {
				if (ToolBox.isSmallPerspectiveSet()) {
					PlatformUI.getWorkbench().showPerspective(
							PerspectiveSmall.ID, window);
				} else {
					PlatformUI.getWorkbench().showPerspective(Perspective.ID,
							window);
				}
			} catch (WorkbenchException ex) {
				FableUtils.excMsg(this,
						"Error opening ImageViewer perspective", ex);
				return;
			}

			try {
				SampleNavigatorView sampleView = (SampleNavigatorView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findView(SampleNavigatorView.ID);

				sampleView.addDirectory(iFolder.getLocation().toString());
			} catch (Exception ex) {
				FableUtils.excMsg(this, "Error loading Image Folder", ex);
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
