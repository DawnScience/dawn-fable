/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.actions;

import java.io.File;

import org.dawb.fabio.FableJep;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import fable.framework.toolbox.FableUtils;
import fable.python.preferences.PreferenceConstants;
import fable.python.rcp.Activator;

/**
 * This action is triggered by user if he wants to record python action in a
 * script.
 * 
 * This action is represented by a toggle button. If button status is not
 * selected and user pushed it, button status becomes to true and a file dialog
 * box is opened. User can choose/create a file to record python actions.
 * 
 * If button status is set to true, and if user pushes it, button status becomes
 * to false and python actions are not recorded in a script.
 * 
 * @author SUCHET
 * 
 */
public class RecordScriptAction implements IWorkbenchWindowActionDelegate {
	private final static String ID = "fable.python.actions.recordAction";
	private String fileDirectory;
	private String[] filterExtension = { "*.py", "*.*" };
	private String[] filterName = { "*.py", "All(*.*)" };

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void init(IWorkbenchWindow window) {
		new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						PreferenceConstants.P_SCRIPT_PATH)) {
					fileDirectory = Activator.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.P_SCRIPT_PATH);
				}

			}
		};
		// Init fileDirectory
		fileDirectory = Activator.getDefault().getPreferenceStore().getString(
				PreferenceConstants.P_SCRIPT_PATH);
	}

	public void run(IAction action) {
		
		FileDialog fileDialog = new FileDialog(Display.getDefault()
				.getActiveShell(), SWT.SAVE);
		fileDialog.setFilterPath(fileDirectory);
		fileDialog.setFilterNames(filterName);
		fileDialog.setFilterExtensions(filterExtension);

		if (action.isChecked()) {
			String filename = fileDialog.open();
			//check if file Exists. Gaelle 16/03/2009
			
			if (filename != null && recordInThisFile(filename)) {
				
				FableJep.record(true);
				try {
					FableJep.getFableJep().setScriptFileName(filename);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				//action.setText("Stop Recording Script");
				action.setToolTipText("Stop recording python actions.");
			} else {
				action.setChecked(false);
				FableJep.record(false);
				//action.setText("Record Script");
				action.setToolTipText("Record python actions in a script.");
			}
		} else {
			FableJep.record(false);
			//action.setText("Record Script");
			action.setToolTipText("Record python actions in a script.");
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Do nothing
	}

	public static String getID() {
		return ID;
	}

	/**
	 * Check if this file exists. If yes, display a message to the user to avoid overwriting a file.
	 * @param filename
	 * @return true if file does not exists or if user wants to overwrite a file.
	 */
	public boolean recordInThisFile(String filename){
		File f = new File(filename);
		boolean record = true;
		if(f.exists()){
			record = FableUtils.confirmMsg(this, filename
					+ " already exists. \nDo you want to add your new records in it ?");
	
		}
		return record;
	}
}
