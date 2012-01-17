/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.internal;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import fable.framework.ui.actions.UpdateAction;

/**
 * A class to create the standard fable main menu bar for all fable rcp
 * applications. 01/23/2009 : add perspective short list to windows menu.
 * 
 * @author Andy Gotz
 * 
 */
public class MainMenuBar extends ActionBarAdvisor {
	/**
	 * Switch to add a Test menu to the Window menu. The test menu is defined in
	 * TestMenu.
	 */
	private static final boolean DO_TESTS = false;

	public MainMenuBar(IActionBarConfigurer configurer) {
		super(configurer);
	}

	public MenuManager fileMenu;
	public MenuManager editMenu;
	public MenuManager windowMenu;
	public MenuManager helpMenu;
	/** Other action. For wizard... */
	private IWorkbenchAction newAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction newWorkbenchAction;
	private IWorkbenchAction preferencesAction;
	private IWorkbenchAction perspectiveCustomizeAction;
	private IWorkbenchAction perspectiveSaveAsAction;
	private IWorkbenchAction perspectiveResetAction;
	private IWorkbenchAction perspectiveCloseAction;
	private IWorkbenchAction perspectiveCloseAllAction;
	private IWorkbenchAction helpAction;
	private IWorkbenchAction aboutAction;
	private IContributionItem viewsShortList;
	private IContributionItem perspectivesShortList;

	private UpdateAction updateAction;

	// Export, import
	private IWorkbenchAction exportProject;
	private IWorkbenchAction importProject;

	/**
	 * Make fable main menu bar actions.
	 * 
	 * @param window
	 *            - workbench window
	 */
	public void makeActions(IWorkbenchWindow window) {
		newAction = ActionFactory.NEW.create(window);
		register(newAction);
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		newWorkbenchAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		register(newWorkbenchAction);
		perspectivesShortList = ContributionItemFactory.PERSPECTIVES_SHORTLIST
				.create(window);

		viewsShortList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		perspectiveCustomizeAction = ActionFactory.EDIT_ACTION_SETS
				.create(window);
		register(perspectiveCustomizeAction);
		perspectiveSaveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
		register(perspectiveSaveAsAction);
		perspectiveResetAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(perspectiveResetAction);
		perspectiveCloseAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(perspectiveCloseAction);
		perspectiveCloseAllAction = ActionFactory.CLOSE_ALL_PERSPECTIVES
				.create(window);
		register(perspectiveCloseAllAction);
		preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(preferencesAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		updateAction = new UpdateAction(window);
		register(updateAction);

		/*
		 * what are all these actions used for - andy ? -->Gaelle : Text Editor
		 * plugin
		 */
		registerAsGlobal(ActionFactory.SAVE.create(window));
		registerAsGlobal(ActionFactory.SAVE_AS.create(window));
		registerAsGlobal(ActionFactory.ABOUT.create(window));
		registerAsGlobal(ActionFactory.SAVE_ALL.create(window));
		registerAsGlobal(ActionFactory.UNDO.create(window));
		registerAsGlobal(ActionFactory.REDO.create(window));
		registerAsGlobal(ActionFactory.CUT.create(window));
		registerAsGlobal(ActionFactory.COPY.create(window));
		registerAsGlobal(ActionFactory.PASTE.create(window));
		registerAsGlobal(ActionFactory.SELECT_ALL.create(window));
		registerAsGlobal(ActionFactory.FIND.create(window));
		registerAsGlobal(ActionFactory.CLOSE.create(window));
		registerAsGlobal(ActionFactory.CLOSE_ALL.create(window));
		registerAsGlobal(ActionFactory.CLOSE_ALL_SAVED.create(window));
		registerAsGlobal(ActionFactory.REVERT.create(window));
		registerAsGlobal(ActionFactory.QUIT.create(window));
		/***/
		exportProject = ActionFactory.EXPORT.create(window);
		register(exportProject);
		importProject = ActionFactory.IMPORT.create(window);
		register(importProject);

	}

	/**
	 * Fill fable menu bar with standard actions.
	 * 
	 */
	public void fillMenuBar(IMenuManager menuBar) {
		createMenuFile();
		menuBar.add(fileMenu);

		createMenuEdit();
		menuBar.add(editMenu);
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		createMenuWindows();
		menuBar.add(windowMenu);
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.M_HELP));

		createMenuHelp();
		menuBar.add(helpMenu);

		// common Fable menu

	}

	// @Override
	/*
	 * protected void fillCoolBar(ICoolBarManager cbManager) { cbManager.add(new
	 * GroupMarker("group.file")); //$NON-NLS-1$ { // File Group IToolBarManager
	 * fileToolBar = new ToolBarManager(cbManager .getStyle());
	 * fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
	 * fileToolBar .add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
	 * fileToolBar.add(new GroupMarker( IWorkbenchActionConstants.SAVE_GROUP));
	 * fileToolBar.add(getAction(ActionFactory.SAVE.getId()));
	 * 
	 * fileToolBar.add(new Separator( IWorkbenchActionConstants.MB_ADDITIONS));
	 * 
	 * // Add to the cool bar manager cbManager.add(new
	 * ToolBarContributionItem(fileToolBar,
	 * IWorkbenchActionConstants.TOOLBAR_FILE)); }
	 * 
	 * cbManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	 * 
	 * cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR)); }
	 */
	public void fillCoolBar(ICoolBarManager cbManager) {
		cbManager.add(new GroupMarker("group.file")); //$NON-NLS-1$
		{ // File Group
			IToolBarManager fileToolBar = new ToolBarManager(cbManager
					.getStyle());
			fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
			fileToolBar
					.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
			fileToolBar.add(new GroupMarker(
					IWorkbenchActionConstants.SAVE_GROUP));
			fileToolBar.add(getAction(ActionFactory.SAVE.getId()));
			fileToolBar.add(getAction(ActionFactory.SAVE_AS.getId()));

			fileToolBar.add(new Separator(
					IWorkbenchActionConstants.MB_ADDITIONS));

			// Add to the cool bar manager
			cbManager.add(new ToolBarContributionItem(fileToolBar,
					IWorkbenchActionConstants.TOOLBAR_FILE));
		}

		cbManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
	}

	/**
	 * Registers the action as global action and registers it for disposal.
	 * 
	 * @param action
	 *            the action to register
	 */
	private void registerAsGlobal(IAction action) {
		getActionBarConfigurer().registerGlobalAction(action);
		register(action);
	}

	/**
	 * This methods creates a menu File.
	 * <p>
	 * <UL>
	 * <LI>close action <code>(ActionFactory.CLOSE)</code>
	 * <LI>Close all action <code>(ActionFactory.CLOSE_ALL)</code>
	 * <LI>Close and exit <code>(ActionFactory.CLOSE_EXT)</code>
	 * <LI>Save editor <code>(ActionFactory.SAVE)</code>
	 * <LI>Save as opened editor <code>(ActionFactory.SAVE_AS)</code>
	 * <LI>Save all opened editors <code>(ActionFactory.SAVE_ALL)</code>
	 * <LI>Revert <code>(ActionFactory.REVERT)</code>
	 * <LI>Open last editor <code>(ActionFactory.REOPEN_EDITORS)</code>
	 * <LI>Exit application <code>(ActionFactory.QUIT)</code>
	 * </UL>
	 * </p>
	 */
	private void createMenuFile() {
		fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);

		MenuManager newMenu = new MenuManager("New",
				IWorkbenchActionConstants.M_PROJECT);

		fileMenu.add(newMenu);
		newMenu.add(newAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));

		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		fileMenu.add(getAction(ActionFactory.CLOSE.getId()));
		fileMenu.add(getAction(ActionFactory.CLOSE_ALL.getId()));
		// menu.add(closeAllSavedAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(getAction(ActionFactory.SAVE.getId()));
		fileMenu.add(getAction(ActionFactory.SAVE_AS.getId()));
		fileMenu.add(getAction(ActionFactory.SAVE_ALL.getId()));

		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(exportProject);
		fileMenu.add(importProject);

		fileMenu.add(getAction(ActionFactory.REVERT.getId()));
		fileMenu.add(ContributionItemFactory.REOPEN_EDITORS
				.create(getActionBarConfigurer().getWindowConfigurer()
						.getWindow()));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		fileMenu.add(new Separator());
		fileMenu.add(getAction(ActionFactory.QUIT.getId()));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
	}

	/**
	 * This methods add menu Edit and its actions to MainMenuBar.
	 * <p>
	 * <UL>
	 * <LI>Undo action <code>(ActionFactory.UNDO)</code>
	 * <LI>Redo action <code>(ActionFactory.REDO)</code>
	 * <LI>Cut action<code>(ActionFactory.CUT)</code>
	 * <LI>Copy action <code>(ActionFactory.COPY)</code>
	 * <LI>Paste opened editor <code>(ActionFactory.PASTE)</code>
	 * <LI><code>(ActionFactory.CUT_EXT)</code>
	 * <LI>Select all action <code>(ActionFactory.SELECT_ALL)</code>
	 * <LI><code>(ActionFactory.FIND_EXT)</code>
	 * <LI><code>(ActionFactory.ADD_EXT)</code>
	 * <LI><code>(ActionFactory.EDIT_END)</code>
	 * </UL>
	 * </p>
	 */
	private void createMenuEdit() {
		editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		editMenu.add(getAction(ActionFactory.UNDO.getId()));
		editMenu.add(getAction(ActionFactory.REDO.getId()));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));

		editMenu.add(getAction(ActionFactory.CUT.getId()));
		editMenu.add(getAction(ActionFactory.COPY.getId()));
		editMenu.add(getAction(ActionFactory.PASTE.getId()));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));

		editMenu.add(getAction(ActionFactory.SELECT_ALL.getId()));
		editMenu.add(new Separator());

		editMenu.add(getAction(ActionFactory.FIND.getId()));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));

		editMenu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		editMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

	}

	/**
	 * This method creates menu Window.
	 */
	private void createMenuWindows() {
		windowMenu = new MenuManager("&Window",
				IWorkbenchActionConstants.M_WINDOW);

		windowMenu.add(newWorkbenchAction);

		windowMenu.add(new Separator());

		MenuManager perspectiveMenu = new MenuManager("Open Perspective");
		windowMenu.add(perspectiveMenu);
		perspectiveMenu.add(perspectivesShortList);
		MenuManager viewsMenu = new MenuManager("Show View");
		viewsMenu.add(viewsShortList);
		windowMenu.add(viewsMenu);

		windowMenu.add(new Separator());
		windowMenu.add(perspectiveCustomizeAction);
		windowMenu.add(perspectiveSaveAsAction);
		windowMenu.add(perspectiveResetAction);
		windowMenu.add(perspectiveCloseAction);
		windowMenu.add(perspectiveCloseAllAction);

		if (DO_TESTS) {
			MenuManager testMenu = TestMenu.createTestMenu();
			if (testMenu != null) {
				windowMenu.add(new Separator());
				windowMenu.add(testMenu);
			}
		}

		windowMenu.add(new Separator());
		windowMenu.add(preferencesAction);
	}

	/**
	 * This method creates menu Help.<br>
	 * <UL>
	 * <LI>Help action : Opens online help.
	 * <LI>Update action : open update box to install new features or to update
	 * installed features for the bundle.
	 * <LI>About Action : opens plugin about box.
	 * </UL>
	 */
	private void createMenuHelp() {
		helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(helpAction);
		helpMenu.add(updateAction);
        helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(new Separator());
		helpMenu.add(aboutAction);
	}

	@Override
	public void dispose() {
		super.dispose();
		aboutAction.dispose();
		exitAction.dispose();
		helpAction.dispose();
		newAction.dispose();
		newWorkbenchAction.dispose();
		perspectiveCustomizeAction.dispose();
		perspectiveSaveAsAction.dispose();
		perspectiveResetAction.dispose();
		perspectiveCloseAction.dispose();
		perspectiveCloseAllAction.dispose();
		preferencesAction.dispose();
	}

}
