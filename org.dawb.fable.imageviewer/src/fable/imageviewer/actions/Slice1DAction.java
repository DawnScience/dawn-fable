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

public class Slice1DAction extends SetDifferenceAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public Slice1DAction() {
	}

	public Slice1DAction(String string) {
		super(string, AS_PUSH_BUTTON);
		setEnabled(true);
	}

	@Override
	public void run(IAction action) {
		slice1D();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		slice1D();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * Make a a 1D slice of the current selection.
	 */
	private void slice1D() {
		try {
			Vector<Integer> selectedFiles = SampleNavigatorView.view
					.getSelectedFilesIndex();
			if (selectedFiles.size() <= 1) {
				imageComp.updateStatusLabel("select at least 2 files to make a slice");
			} else {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(ImageView.ID,
								ImageComponent.SECONDARY_ID_SLICE1D,
								IWorkbenchPage.VIEW_ACTIVATE);
				ImageView imageSlice1DView = (ImageView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findViewReference(ImageView.ID,
								ImageComponent.SECONDARY_ID_SLICE1D).getView(true);
				Rectangle selectionArea = imageComp.getImage().getSelectedArea();
				Rectangle imageArea = imageComp.getImage()
						.screenRectangleToImageRectangle(selectionArea, true);

				if (imageComp.getZoomSelection() == ZoomSelection.LINE) {
					ImageUtils.Slice2DLine(imageArea.x, imageArea.y,
							imageArea.x + imageArea.width, imageArea.y
									+ imageArea.height, 1);
				} else {
					imageComp.updateStatusLabel("zoom on a 1D line to make a slice");
				}
				imageSlice1DView.transferSelectedSettings(imageComp);
				imageSlice1DView.setPartName(imageSlice1DView.getSecondaryId()
						+ " " + imageComp.getFileName());
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Error opening Slice 1D view", ex);
		}
	}
}
