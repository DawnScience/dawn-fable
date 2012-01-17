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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.model.ImageModel;
import fable.imageviewer.views.ImageView;

public class ImageCopyAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public ImageCopyAction() {
	}

	public ImageCopyAction(String string) {
		super(string, AS_PUSH_BUTTON);
		setEnabled(true);
	}


	@Override
	public void run(IAction action) {
		copyImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		copyImage();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * Makes a copy of current image view. Uses ImageView.zoomSecondaryID as the
	 * secondary ID then increments it.
	 */
	private void copyImage() {
		try {
			// Create the secondary ID
			String id2 = "Copy " + Integer.toString(ImageComponent.copySecondaryID);
			// Show the view
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(ImageView.ID, id2,
							IWorkbenchPage.VIEW_ACTIVATE);
			// Get the reference
			ImageView imageCopyView = (ImageView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findViewReference(ImageView.ID, id2).getView(true);
			// Use the current image size, not the original
			Rectangle imageRect = imageComp.getImage().getImageRect();
			ImageModel imageModel = imageComp.getImageModel();
			imageCopyView.getImage().changeImageRect(imageRect,
					imageModel.getData(imageRect), imageModel.getFileName(),
					imageModel);
			imageCopyView.transferSelectedSettings(imageComp);
			imageCopyView.setPartName("Copy " + imageComp.getFileName());
			// Increment the ID for the next copy
			ImageComponent.copySecondaryID++;
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Making a copy if the current view failed",
					ex);
		}
	}
}
