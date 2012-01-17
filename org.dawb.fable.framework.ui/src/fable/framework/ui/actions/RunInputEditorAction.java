/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fable.framework.ui.editors.IFableInputEditor;
import fable.framework.ui.rcp.Activator;

/**
 * This class launches a program with the current input file or current selected
 * input file which implements <code>IFableInputEditor</code>.
 * 
 * @author SUCHET
 * 
 */
public class RunInputEditorAction extends Action {
	// private final static String ID =
	// "fable.framework.ui.RunInputEditorAction";
	IWorkbenchWindow window;

	public RunInputEditorAction() {
		setImageDescriptor(Activator.getImageDescriptor("icons/run.gif"));
		setToolTipText("Run program depending on active editor");
	}

	@Override
	public void run() {
		IFableInputEditor editor = getActiveEditor();
		if (editor != null) {
			editor.run();
		}

	}

	private IFableInputEditor getActiveEditor() {
		IFableInputEditor editor = null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart part = page.getActiveEditor();
				if (part instanceof IFableInputEditor)
					editor = (IFableInputEditor) part;
			}
		}
		return editor;
	}

}
