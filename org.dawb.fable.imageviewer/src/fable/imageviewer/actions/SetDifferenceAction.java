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

import fable.imageviewer.model.ImageModel;

/**
 * Action to set the background image which can will be subtracted from the displayed
 * when displaying the difference.
 * 
 * @author andy
 * 
 */
public class SetDifferenceAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public SetDifferenceAction() {
	}

	/**
	 * Constructor to set the text and set the style to AS_PUSH_BUTTON.
	 * 
	 * @param text
	 */
	public SetDifferenceAction(String text) {
		this(text, AS_PUSH_BUTTON);
	}

	public SetDifferenceAction(String text, int s) {
		super(text, s);
		setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		setBackground();
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
		setBackground();
	}

	/**
	 * Set the background image.
	 */
	private void setBackground() {
		if (imageComp != null) {
			ImageModel imageModel = imageComp.getImageModel();
			if (imageModel != null)
				imageComp.setImageSavedModel(imageModel);
		}
	}
}
