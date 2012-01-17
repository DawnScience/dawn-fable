/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.contentprovider;

import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fable.python.Experiment;
import fable.python.Sample;

public class SampleTableContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		Object[] kids = null;

		if (inputElement instanceof Experiment) {
			Vector<Sample> vector = ((Experiment) inputElement).getSamples();
			for (int i = 0; i < vector.size(); i++) {
				Sample s = vector.elementAt(i);
				if (!s.isShowInNavigator()) {
					vector.remove(s);
				}
			}
			kids = vector.toArray();

		}
		return kids;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// System.out.println("Sample inputChange");

	}

}
