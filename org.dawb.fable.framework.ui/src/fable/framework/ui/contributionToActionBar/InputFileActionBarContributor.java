/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * 
 */
package fable.framework.ui.contributionToActionBar;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * This class is used to add save and saveAs to the toolbar for the input file
 * editor <code>Fitallb</code>.
 * 
 * @author SUCHET
 */
public class InputFileActionBarContributor extends TextEditorActionContributor {

	private RetargetTextEditorAction save;
	private RetargetTextEditorAction saveAs;

	/**
	 * Constructor.
	 */
	public InputFileActionBarContributor() {
		saveAs = new RetargetTextEditorAction(ResourceBundle
				.getBundle("fable.fitallb"), "test");
		save = new RetargetTextEditorAction(ResourceBundle
				.getBundle("fable.fitallb"), "test");

		String cmd = ITextEditorActionDefinitionIds.SAVE;
		save.setActionDefinitionId(cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.
	 * IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		// TODO Auto-generated method stub
		super.init(bars);
		// bars.setGlobalActionHandler(SaveAsInifile.ID, saveAs);
		// getPage().addPartListener(saveAs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org
	 * .eclipse.jface.action.IToolBarManager)
	 */
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		// TODO Auto-generated method stub
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(save);
		toolBarManager.add(saveAs);

		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.EditorActionBarContributor#setActiveEditor(org.eclipse
	 * .ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		super.setActiveEditor(targetEditor);
		IActionBars bars = getActionBars();
		bars.clearGlobalActionHandlers();
		bars.updateActionBars();

	}

}
