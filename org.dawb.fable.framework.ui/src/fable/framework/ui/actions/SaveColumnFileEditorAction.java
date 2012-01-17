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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fable.framework.ui.editors.ColumnFileEditor;
import fable.framework.ui.editors.ColumnFilePlotEditor;
import fable.framework.ui.editors.IColumnFileEditor;
import fable.framework.ui.internal.IVarKeys;
import fable.framework.ui.rcp.Activator;

public class SaveColumnFileEditorAction extends Action {

	public static final String ID = IVarKeys.SAVE_AS_ACTION;

	public SaveColumnFileEditorAction() {
		// TODO Auto-generated constructor stub
	}

	ImageDescriptor image = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "images/save.gif");

	protected IColumnFileEditor getColumnFileEditor() {
		IColumnFileEditor editor = null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart part = page.getActiveEditor();
				if (part instanceof IColumnFileEditor)
					editor = (IColumnFileEditor) part;
			}
		}
		return editor;
	}

	public void run() {
		IColumnFileEditor editor = getColumnFileEditor();
		run(editor);
	}

	public void run(IColumnFileEditor editor) {
//		SWTUtils.infoMsgAsync("ColumnFile input Editor: No implementation");
		if(editor instanceof ColumnFilePlotEditor){
			((ColumnFilePlotEditor)editor).doSave(null);
				
		}else if(editor instanceof ColumnFileEditor){
			((ColumnFileEditor)editor).doSave(null);
			
		}
	}

	public void setProps(String description) {
		setProps(description, image);
		setId(ID);
	}

	public void setProps(String description, ImageDescriptor imageDescriptor) {
		setText(description);
		setToolTipText(description);
		setImageDescriptor(imageDescriptor);
	}

}
