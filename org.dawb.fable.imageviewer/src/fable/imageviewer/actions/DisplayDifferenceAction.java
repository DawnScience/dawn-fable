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
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * DisplayDifferenceAction is an action for un/selecting the display image difference.
 * 
 * It can be an IViewActionDelegate or an Action.
 * 
 * @author Andy Gotz
 * 
 */

public class DisplayDifferenceAction extends AbstractImageComponentAction implements IViewActionDelegate,
		IAction {

	boolean checked = false;

	public DisplayDifferenceAction() {

	}

	public void init(IViewPart view) {
		super.init(view);
	    checked = imageComp.isImageDiffOn();
	}

	public void run(IAction action) {
		checked = !checked;
		imageComp.setImageDiffOn(checked);
		setChecked(checked);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/* Action part */
	public DisplayDifferenceAction(String text) {
		super(text, AS_RADIO_BUTTON);
		setEnabled(true);
	}

	public void run() {
		checked = !checked;
		imageComp.setImageDiffOn(checked);
		setChecked(checked);
	}

}
