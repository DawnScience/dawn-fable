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

import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.internal.ImageUtils;
import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.views.ImageView;

public class Slice2DAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public Slice2DAction() {
	}

	public Slice2DAction(String string) {
		super(string, AS_PUSH_BUTTON);
		setEnabled(true);
	}

	@Override
	public void run(IAction action) {
		slice2D();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		slice2D();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * Make a a 2D slice of the current selection
	 */
	private void slice2D() {
		try {
			Vector<Integer> selectedFiles = SampleNavigatorView.view
					.getSelectedFilesIndex();
			if (selectedFiles.size() <= 1) {
				imageComp.updateStatusLabel("select at least 2 files to make a slice");
			} else {
				if (imageComp == null) {
					imageComp = ImageUtils.getComponentFromPartSelected();
				}
				if (imageComp != null) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(ImageView.ID,
									ImageComponent.SECONDARY_ID_SLICE2D,
									IWorkbenchPage.VIEW_ACTIVATE);
					ImageView imageSlice2DView = (ImageView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().findViewReference(ImageView.ID,
									ImageComponent.SECONDARY_ID_SLICE1D).getView(
									true);
					Rectangle selectionArea = imageComp.getImage().getSelectedArea();
					Rectangle imageArea = imageComp.getImage()
							.screenRectangleToImageRectangle(selectionArea,
									true);

					if (imageComp.getZoomSelection() == ZoomSelection.AREA) {
						ImageUtils.Slice2DArea(imageArea.x, imageArea.y,
								imageArea.x + imageArea.width, imageArea.y
										+ imageArea.height);
					} else {
						imageComp.updateStatusLabel("zoom on a 2D area to make a slice");

					}
					imageSlice2DView.transferSelectedSettings(imageComp);
					imageSlice2DView.setPartName(imageSlice2DView
							.getSecondaryId()
							+ " " + imageComp.getFileName());
				}
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Error opening Slice 2D view", ex);
		}
	}
}
