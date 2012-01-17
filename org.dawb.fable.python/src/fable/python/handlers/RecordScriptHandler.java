/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.SWTUtils;

public class RecordScriptHandler extends AbstractHandler {
	// private static String[] filterExtension = { "*.py", "*.*" };
	// private static String[] filterName = { "*.py", "All(*.*)" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			FableUtils.errMsg(this, "Cannot determine the workbench window");
			return null;
		}

		// FIXME
		SWTUtils.infoMsg(window.getShell(), "Not implemented yet");

		// // Get the preference for the script path
		// String fileDirectory = Activator.getDefault().getPreferenceStore()
		// .getString(PreferenceConstants.P_SCRIPT_PATH);
		//
		// FileDialog fileDialog = new FileDialog(Display.getDefault()
		// .getActiveShell(), SWT.SAVE);
		// fileDialog.setFilterPath(fileDirectory);
		// fileDialog.setFilterNames(filterName);
		// fileDialog.setFilterExtensions(filterExtension);
		//
		// if (action.isChecked()) {
		// String filename = fileDialog.open();
		// //check if file Exists. Gaelle 16/03/2009
		//			
		// if (filename != null && recordInThisFile(filename)) {
		//				
		// FableJep.record(true);
		// FableJep.setScriptFileName(filename);
		// //action.setText("Stop Recording Script");
		// action.setToolTipText("Stop recording python actions.");
		// } else {
		// action.setChecked(false);
		// FableJep.record(false);
		// //action.setText("Record Script");
		// action.setToolTipText("Record python actions in a script.");
		// }
		// } else {
		// FableJep.record(false);
		// //action.setText("Record Script");
		// action.setToolTipText("Record python actions in a script.");
		// }
		//		

		// Must currently be null
		return null;
	}

}
