/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import fable.framework.logging.FableLogger;
import fable.framework.toolbox.ActionBarWrapper;
import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.GridUtils;
import fable.imageviewer.component.ActionsProvider;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.component.ImageComponentImage;
import fable.imageviewer.component.ImagePlay;
import fable.imageviewer.model.ImageModelFactory;
import fable.imageviewer.rcp.Activator;

/**
 * ImageEditor
 * 
 * @author Matthew Gerring
 * 
 */
public class ImageEditor extends EditorPart implements IReusableEditor, ActionsProvider {
	/**
	 * Plug-in ID.
	 */
	public static final String ID = "fable.imageviewer.editor.ImageEditor";
	
	/**
	 * The object which does the work, can be used in different view parts.
	 */
	private ImageComponent imageComponent;

	/**
	 * Redirects the action bars so that we can show local actions to the
	 * user
	 */
	private ActionBarWrapper actionBarsWrapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		parent.setLayout(new GridLayout(1,false));
		
		final Composite top    = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Text point = new Text(top, SWT.LEFT);
		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		point.setEditable(false);
		GridUtils.setVisible(point, true);
		point.setBackground(top.getBackground());

		final MenuManager    menuMan = new MenuManager();
	    final ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar toolBar = toolMan.createControl(top);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
	    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
	        @Override
	        public void run() {
                final Menu   mbar = menuMan.createContextMenu(toolBar);
       		    mbar.setVisible(true);
	        }
	    };
	
		final IActionBars bars = this.getEditorSite().getActionBars();
        this.actionBarsWrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
        
        final Composite plotComposite = new Composite(parent, SWT.NONE);
        plotComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.imageComponent = new ImageComponent(this);
        imageComponent.setStatusLabel(point);
        imageComponent.createPartControl(plotComposite);
        
        ImagePlay.setView(this.getImageComponent());
        
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
        	public void run() {
        		loadFile();
        	}
        });
        
        GridUtils.removeMargins(plotComposite);
        GridUtils.removeMargins(top);
        GridUtils.removeMargins(parent);
        
        toolMan.add(menuAction);
        toolMan.update(true);
   	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (imageComponent!=null) {
			imageComponent.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (imageComponent!=null) imageComponent.dispose();
	}

	public ImageComponentImage getImage() {
		return imageComponent.getImage();
	}

	public void setPartName(final String name) {
		super.setPartName(name);
	}

	public ImageComponent getImageComponent() {
		return imageComponent;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}
	
	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		loadFile();
	}

	private void loadFile() {
		if (imageComponent!=null && getEditorInput()!=null) {
			final String path = EclipseUtils.getFilePath(getEditorInput());
			try {
				imageComponent.loadModel(ImageModelFactory.getImageModel(path));
			} catch (Throwable e) {
				FableLogger.error("Cannot load file "+path, e);
			}
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public IActionBars getActionBars() {
		return actionBarsWrapper;
	}
}
