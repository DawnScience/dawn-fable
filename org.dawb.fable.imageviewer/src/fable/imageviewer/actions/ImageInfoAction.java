/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * 
 */
package fable.imageviewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;

import fable.framework.toolbox.SWTUtils;
import fable.imageviewer.model.ImageModel;

/**
 * Action to display image info.
 * 
 * @author evans
 * 
 */
public class ImageInfoAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public ImageInfoAction() {
	}

	/**
	 * Constructor to set the text and set the style to AS_PUSH_BUTTON.
	 * 
	 * @param text
	 */
	public ImageInfoAction(String text) {
		super(text, AS_PUSH_BUTTON);
		setEnabled(true);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		showInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		showInfo();
	}

	/**
	 * Shows the image info in a dialog.
	 */
	private void showInfo() {
		ImageModel imageModel = imageComp.getImageModel();
		if (imageModel == null)
			return;
		String info = "";
		info += imageModel.getFileName() + "\n";
		imageModel = imageComp.getImageSavedModel();
		if(imageComp.isImageDiffOn() && imageModel != null) {
			info += "  -  " + imageModel.getFileName() + "\n";
		}
		imageModel = imageComp.getImageModel();
		info += "\n";
		info += "width = " + imageModel.getWidth() + "\n";
		info += "height = " + imageModel.getHeight() + "\n";
		float[] statistics = imageModel.getStatistics();
		info += "\n";
		info += "min = " + statistics[0] + "\n";
		info += "max = " + statistics[1] + "\n";
		info += "mean = " + statistics[2] + "\n";
		imageModel = imageComp.getImageSavedModel();
		if(imageComp.isImageDiffOn() && imageModel != null) {
			statistics = imageModel.getStatistics();
			info += "\n";
			info += "saved min = " + statistics[0] + "\n";
			info += "saved max = " + statistics[1] + "\n";
			info += "saved mean = " + statistics[2] + "\n";
		}
		imageModel = imageComp.getImageDiffModel();
		if(imageComp.isImageDiffOn() && imageModel != null) {
			statistics = imageModel.getStatistics();
			info += "\n";
			info += "difference min = " + statistics[0] + "\n";
			info += "difference max = " + statistics[1] + "\n";
			info += "difference mean = " + statistics[2] + "\n";
		}
		// Don't use FableUtils as we don't want to log this
		SWTUtils.infoMsgAsync(info);
	}
}
