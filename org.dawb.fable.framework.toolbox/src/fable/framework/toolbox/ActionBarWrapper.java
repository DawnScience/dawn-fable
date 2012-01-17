/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package fable.framework.toolbox;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.SubActionBars2;

public class ActionBarWrapper extends SubActionBars2 {


	private IToolBarManager    alternativeToolbarManager;
	private IMenuManager       alternativeMenuManager;
	private IStatusLineManager alternativeStatusManager;

	/**
	 * alternatives may be null.
	 * @param alternativeToolbarManager
	 * @param alternativeMenuManager
	 * @param alternativeStatusManager
	 * @param parent
	 */
	public ActionBarWrapper(final IToolBarManager    alternativeToolbarManager,
			                final IMenuManager       alternativeMenuManager,
			                final IStatusLineManager alternativeStatusManager,
			                IActionBars2 parent) {
		super(parent);
		this.alternativeToolbarManager = alternativeToolbarManager;
		this.alternativeMenuManager    = alternativeMenuManager;
		this.alternativeStatusManager  = alternativeStatusManager;
	}

	@Override
	public IMenuManager getMenuManager() {
		if (alternativeMenuManager!=null) return alternativeMenuManager;
		return super.getMenuManager();
	}

	/**
	 * Returns the status line manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the status line manager
	 */
	@Override
	public IStatusLineManager getStatusLineManager() {
		if (alternativeStatusManager!=null) return alternativeStatusManager;
		return super.getStatusLineManager();
	}

	/**
	 * Returns the tool bar manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the tool bar manager
	 */
	@Override
	public IToolBarManager getToolBarManager() {
		if (alternativeToolbarManager!=null) return alternativeToolbarManager;
		return super.getToolBarManager();
	}

}
