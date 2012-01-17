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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Simple action which will have other actions in a drop down menu.
 */
public class MenuAction extends Action implements IMenuCreator, IPropertyChangeListener {
	
	private Menu fMenu;
	private List<IAction> actions;
	private Action lastAction;
	private String mainText;
	private boolean useTextOfAction;

	public MenuAction(final String text) {
       this(text,false);
	}
	public MenuAction(final String text, boolean useTextOfAction) {
		super(text, IAction.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
		this.actions = new ArrayList<IAction>(7);
		this.useTextOfAction = useTextOfAction;
	}


	@Override
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
		for (IAction action : actions) {
			action.removePropertyChangeListener(this);
		}
		actions.clear();
		lastAction=null;
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		this.lastAction = (Action)event.getSource();
		updateSelectedValue();
	}

    private void updateSelectedValue() {
    	IAction selAction = lastAction;
    	if (selAction==null) for (IAction action : actions) {
    		if (action.isChecked()) {
    			selAction = action;
    			break;
    		}
    	}
		if (selAction!=null) {
			
			final StringBuilder buf = new StringBuilder();
			if (mainText!=null) {
				buf.append(mainText);
				buf.append("\n");
			}
			buf.append(selAction.getText());
			super.setToolTipText(buf.toString());
			
			if (useTextOfAction) {
				setText(selAction.getText());
			}

		}
		
	}


	public void setToolTipText(final String ttext) {
    	this.mainText = ttext;
    	super.setToolTipText(ttext);
    	updateSelectedValue();
    }

	@Override
	public Menu getMenu(Menu parent) {
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);

		for (IAction action : actions) {
			addActionToMenu(fMenu, action);
		}

		return fMenu;
	}

	public void add(final IAction action) {
		actions.add(action);
		action.addPropertyChangeListener(this);
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);

		for (IAction action : actions) {
			addActionToMenu(fMenu, action);
		}

		return fMenu;
	}


	protected void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
		if (lastAction!=null) lastAction.run();
	}


	/**
	 * Get's rid of the menu, because the menu hangs on to * the searches, etc.
	 */
	void clear() {
		dispose();
	}

	public void setSelected(int index) {
		if (actions==null || index>actions.size()-1) return;
		actions.get(index).setChecked(true);
	}

}
