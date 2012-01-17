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

/**
 * Action to reset the zoom to the original image.
 * 
 * @author evans
 * 
 */
public class ResetZoomAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public ResetZoomAction() {
	}

	/**
	 * Constructor to set the text and set the style to AS_PUSH_BUTTON.
	 * 
	 * @param text
	 */
	public ResetZoomAction(String text) {
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
		resetZoom();
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
		resetZoom();
	}

	/**
	 * Shows the image info in a dialog.
	 */
	private void resetZoom() {
		if (imageComp != null) {
			imageComp.getImage().resetZoom();
		}
	}
}
