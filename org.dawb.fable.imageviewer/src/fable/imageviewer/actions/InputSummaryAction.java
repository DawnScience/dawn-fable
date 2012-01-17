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

/**
 * Action to display image info.
 * 
 * @author evans
 * 
 */
public class InputSummaryAction extends AbstractImageComponentAction implements IViewActionDelegate {

	/**
	 * Do-nothing default constructor.
	 */
	public InputSummaryAction() {
	}

	/**
	 * Constructor to set the text and set the style to AS_PUSH_BUTTON.
	 * 
	 * @param text
	 */
	public InputSummaryAction(String text) {
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
		String info = "";
		info += "Mouse Operations\n\n";
		info += "Drag :          \tSelect zoom area or line\n";
		info += "Double Click :  \tClear selection\n";
		info += "Shift + Click : \tZoom in (in ZoomView only)\n";
		info += "Ctrl + Click :  \tZoom out (in ZoomView only)\n";
		info += "Alt + Click :   \tReset Zoom (in either view, affects ZoomView\n";
		info += "\nKeyboard Operations\n\n";
		info += "ESC :           \tAbort selection\n";
		// Don't use FableUtils as we don't want to log this
		SWTUtils.infoMsgAsync(info);
	}
}
