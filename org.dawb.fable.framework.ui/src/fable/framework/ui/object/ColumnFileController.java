/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.object;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import fable.python.ColumnFile;

/**
 * This class is a link between ColumnFileView and transformer, as we can add
 * columns file from it. Transformer will now act on the current column file.
 * 
 * @author SUCHET
 * 
 */
public class ColumnFileController {

	private Vector<ColumnFile> columnFileVector = new Vector<ColumnFile>();
	private ColumnFile currentcolumnFile;
	private Vector<IPropertyChangeListener> listeners = new Vector<IPropertyChangeListener>();
	private static ColumnFileController columnController = null;

	public static ColumnFileController getColumnFileController() {
		if (columnController == null) {
			columnController = new ColumnFileController();
		}
		return columnController;
	}

	public void addColumnFile(ColumnFile col) {
		columnFileVector.add(col);
		fireAddColumnFile();
	}

	public void removeColumnFile(ColumnFile col) {
		columnFileVector.remove(col);
	}

	public void setcolumnFileVector(Vector<ColumnFile> vector) {
		columnFileVector = vector;
	}

	public void setCurrentColumnFile(ColumnFile column) {
		currentcolumnFile = column;
		fireSetCurrentColumnFile();
	}

	public ColumnFile getCurrentColumnFile() {
		return currentcolumnFile;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Send an event when a current column is set to all ColumnFileController
	 * listeners
	 */
	public void fireSetCurrentColumnFile() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element
						.propertyChange(new PropertyChangeEvent(
								this,
								fable.framework.internal.IPropertyVarKeys.SET_CURRENTCOLUMN,
								null, null));
			}
		}
	}

	private void fireAddColumnFile() {
		// TODO Auto-generated method stub

	}
}
