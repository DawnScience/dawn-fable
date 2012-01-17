/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import fable.framework.navigator.views.SampleNavigatorView;

public class OpensampleAction extends Action   {
	public static final String ID = "fable.framework.navigator.openSampleAction";
	String initialDirData="";
	@Override
	public void run() {

		openSample();
	}

	public void openSample(){
		DirectoryDialog dlg = new DirectoryDialog(Display.getDefault().getActiveShell());
		SampleNavigatorView sampleView =(SampleNavigatorView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SampleNavigatorView.ID);
		initialDirData=SampleNavigatorView.getInitialDirectory();
		if(initialDirData != null){
		dlg.setFilterPath(initialDirData);}
	
		String selectedDirectory = dlg.open();
		
		if(selectedDirectory!=null){
			// add directories
			sampleView.addDirectory(selectedDirectory) ;//listOfSamples instantiated

		}
	}
}
