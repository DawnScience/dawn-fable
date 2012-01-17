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

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

import fable.framework.logging.FableLogger;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.internal.ImageUtils;

/**
 * 
 * @author SUCHET
 * @description This action opens view image. Called in other plugins
 * @date Feb, 08 2008
 */
public class OpenImageAction extends Action {

	private final IWorkbenchWindow window;
	// private final String viewId;
	private final static String CMD_OPEN_IMAGE = "fable.imageviewer.openImageAction";
	private static String previousDirectory = null;

	public OpenImageAction(IWorkbenchWindow _window, String label,
			String viewId, String path) {
		window = _window;
		// this.viewId = viewId;
		setText(label);
		setId(CMD_OPEN_IMAGE);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(CMD_OPEN_IMAGE);
		previousDirectory = path;

	}

	public void run() {
		if (window != null) {
			FileDialog dialog = new FileDialog(window.getShell(), SWT.SINGLE);
			dialog.setText("Choose Image");
			// JFace does not memorize the previous directory by default, do it
			// manually
			if (previousDirectory != null) {
				dialog.setFilterPath(previousDirectory);
			}
			String fileName = dialog.open();

			if (fileName != null) {
				if (previousDirectory == null) {
					previousDirectory = (new File(fileName)).getAbsolutePath();
				}
				ImageComponent imageComp = ImageUtils.getComponentFromPartSelected();
				if (imageComp != null) {
					try {
						imageComp.loadFile(fileName);
					} catch (Throwable e) {
						FableLogger.error("Cannot load image "+fileName, e);
					}
				}
			}
		}
	}
}
