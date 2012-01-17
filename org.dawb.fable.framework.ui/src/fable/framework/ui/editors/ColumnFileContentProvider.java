/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.editors;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import fable.python.ColumnFile;

public class ColumnFileContentProvider implements ILazyContentProvider {

	private TableViewer viewer;
	// private Row[] rows;
	private ColumnFile columnFile;

	public ColumnFileContentProvider(TableViewer tv) {
		viewer = tv;
	}

	public void updateElement(int index) {
		float[] floatArray = columnFile.getRowAt(index);
		if (floatArray != null) {
			viewer.replace(floatArray, index);
		}

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.columnFile = (ColumnFile) newInput;

	}

}
