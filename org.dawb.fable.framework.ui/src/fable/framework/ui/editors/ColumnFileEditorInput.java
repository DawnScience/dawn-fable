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
package fable.framework.ui.editors;

import java.io.File;

import jep.JepException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import fable.framework.toolbox.SWTUtils;
import fable.framework.ui.rcp.Activator;
import fable.python.ColumnFile;

/**
 * @author suchet
 * 
 */
public class ColumnFileEditorInput implements IEditorInput {

	/** file name. */
	private String fileName;
	private ColumnFile columnFile;
	private ImageDescriptor image = Activator.getImageDescriptor("images/"
			+ "colfile_open.jpg");

	/**
	 * Constructor.
	 */
	public ColumnFileEditorInput() {

	}

	/**
	 * Constructor. <b>Warning</b>: As we are launching a job to load
	 * columnfile, please, create columnFile in default display thread, so that
	 * fableJep is instantiated in the same thread as the workbench.
	 * 
	 */
	public ColumnFileEditorInput(final String file) {
		Display.getDefault().syncExec(new Runnable() {
			// @Override
			public void run() {

				try {
					columnFile = new ColumnFile(file);

					fileName = file;
				} catch (Throwable e) {

					SWTUtils.errMsgAsync("Error in ColumnFileEditorInput "
							+ e.getMessage());
				}
			}
		});

	}

	/**
	 * 
	 * @return ColumnFile
	 */
	public ColumnFile getColumn() {
		return columnFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		// TODO Gaelle check this peace of code.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {

		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {

		return fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {

		return fileName ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	// KE: Don't see a way to fix this warning as the Interface defines it
	// without generic types.
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		// return false;
		if (this == obj) {
			return false;// ?To open this input in two differents Part;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ColumnFileEditorInput otherInput = (ColumnFileEditorInput) obj;
		if (fileName == null) {
			if (otherInput.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(otherInput.fileName)) {
			return false;
		}
		return true;
	}

	public void save(){
		columnFile.saveColumnFile(columnFile.getFileName());
		
	}
	/**
	 * Save this columnFile in a new file.
	 */
	public boolean saveAs() {
		FileDialog fileDlg = new FileDialog(Display.getCurrent()
				.getActiveShell(), SWT.SAVE);

		String[] filterExt = { "*.flt", "*.*" };
		String[] filterNames = { "filtered files (*.flt)", "All Files (*.*)" };
		fileDlg.setFilterExtensions(filterExt);
		fileDlg.setFilterNames(filterNames);
		// fileDlg.setFilterPath();

		// Change the title bar text
		fileDlg.setText("Save file");
		String file = fileDlg.open();

		if (file != null) {
			file = fileDlg.getFilterPath() + File.separatorChar
					+ fileDlg.getFileName();
			columnFile.saveColumnFile(file);
			this.fileName = file;
			return true;
		}

		return false;
	}

}
