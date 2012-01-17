/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.contributionToActionBar;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import fable.framework.ui.actions.SaveAsColumnFileEditorAction;
import fable.framework.ui.actions.SaveColumnFileEditorAction;

public class ColumnFileContributor extends EditorActionBarContributor {

	private SaveAsColumnFileEditorAction saveAs;
	private SaveColumnFileEditorAction save;

	public ColumnFileContributor() {
		saveAs = new SaveAsColumnFileEditorAction();
		save = new SaveColumnFileEditorAction();
		saveAs.setImageDescriptor(fable.framework.ui.rcp.Activator
				.imageDescriptorFromPlugin(
						fable.framework.ui.rcp.Activator.PLUGIN_ID,
						"images/saveas.gif"));
		saveAs.setToolTipText("Save columnfile as...");
		save.setImageDescriptor(fable.framework.ui.rcp.Activator
				.imageDescriptorFromPlugin(
						fable.framework.ui.rcp.Activator.PLUGIN_ID,
						"images/save.gif"));
		save.setToolTipText("Save columnfile");

	}

	@Override
	public void init(IActionBars bars) {
		// TODO Auto-generated method stub
		super.init(bars);
		bars.setGlobalActionHandler(SaveAsColumnFileEditorAction.ID, saveAs);
		// getPage().addPartListener(saveAs);
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		// TODO Auto-generated method stub
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(save);
		toolBarManager.add(saveAs);

	}

	@Override
	public void dispose() {

		super.dispose();
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		super.setActiveEditor(targetEditor);
		IActionBars bars = getActionBars();
		bars.clearGlobalActionHandlers();
		bars.updateActionBars();
		bars.setGlobalActionHandler(SaveAsColumnFileEditorAction.ID, saveAs);
		bars.setGlobalActionHandler(SaveColumnFileEditorAction.ID, save);

	}

}
