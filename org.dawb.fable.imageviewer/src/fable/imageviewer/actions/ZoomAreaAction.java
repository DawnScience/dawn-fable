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
 * ZoomAreaAction is an action for setting the image zoom mode to AREA.
 * 
 * It can be an IViewActionDelegate or an Action.
 * 
 * @author Andy Gotz
 * 
 */

public class ZoomAreaAction extends AbstractImageComponentAction implements IViewActionDelegate,
		IAction {

	boolean checked = false;

	public ZoomAreaAction() {

	}

	public void run(IAction action) {
		imageComp.getImage().selectZoom(ZoomSelection.AREA);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/* Action part */
	public ZoomAreaAction(String text) {
		super(text, AS_RADIO_BUTTON);
		setEnabled(true);
	}

	public void run() {
		imageComp.getImage().selectZoom(ZoomSelection.AREA);
	}

}
