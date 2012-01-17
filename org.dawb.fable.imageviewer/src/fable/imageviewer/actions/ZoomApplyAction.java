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

/**
 * ZoomAreaAction is an action for setting the image zoom mode to AREA.
 * 
 * It can be an IViewActionDelegate or an Action.
 * 
 * @author Andy Gotz
 * 
 */

public class ZoomApplyAction extends AbstractImageComponentAction implements IViewActionDelegate,
		                                                                     IAction {

	boolean checked = false;

	public ZoomApplyAction() {

	}

	public void run(IAction action) {
		run();
	}
	
	public void run() {
		if (this.imageComp==null) return;
		if(this.imageComp.getImage() != null) {
			this.imageComp.getImage().showSelection(true);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/* Action part */
	public ZoomApplyAction(String text) {
		super(text, AS_PUSH_BUTTON);
		setEnabled(true);
	}


}
