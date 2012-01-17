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

import fable.imageviewer.internal.ZoomSelection;

/**
 * ZoomRockingAction is an action for setting the image zoom mode to ROCKING.
 * 
 * It can be an IViewActionDelegate or an Action.
 * 
 * @author Andy Gotz
 * 
 */

public class ZoomRockingAction extends AbstractImageComponentAction implements IViewActionDelegate {


	public ZoomRockingAction() {
	}

	public void run(IAction action) {
		imageComp.getImage().selectZoom(ZoomSelection.ROCKINGCURVE);

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	/* Action part */
	public ZoomRockingAction(String text) {
		super(text, AS_RADIO_BUTTON);
		setEnabled(true);
	}

	public void run() {
		imageComp.getImage().selectZoom(ZoomSelection.ROCKINGCURVE);
	}

}
