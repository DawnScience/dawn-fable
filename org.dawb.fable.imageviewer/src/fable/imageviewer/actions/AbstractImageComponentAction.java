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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.views.ImageView;

public abstract class AbstractImageComponentAction extends Action implements
		IViewActionDelegate {
	
	
	public AbstractImageComponentAction() {
		super();
	}


	public AbstractImageComponentAction(String text, ImageDescriptor image) {
		super(text, image);
	}


	public AbstractImageComponentAction(String text, int style) {
		super(text, style);
	}


	public AbstractImageComponentAction(String text) {
		super(text);
	}


	protected ImageComponent imageComp = null;


	public void init(IViewPart view) {
		if (view instanceof ImageView) {
			imageComp = ((ImageView) view).getImageComponent();
		}
	}
	
	public void set(ImageComponent iv) {
		this.imageComp = iv;
	}
}
