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

import fable.imageviewer.internal.ZoomSelection;

public class ZoomNoneAction extends AbstractImageComponentAction {

	public ZoomNoneAction(String text) {
		super(text, AS_RADIO_BUTTON);
		setEnabled(true);
	}

	@Override
	public void run(IAction action) {
		imageComp.getImage().selectZoom(ZoomSelection.NONE);
	}
	public void run() {
		imageComp.getImage().selectZoom(ZoomSelection.NONE);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
