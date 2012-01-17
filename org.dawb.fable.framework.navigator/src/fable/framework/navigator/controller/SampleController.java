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
package fable.framework.navigator.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.dawb.fabio.FabioFile;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.slf4j.Logger;

import fable.framework.logging.FableLogger;
import fable.framework.navigator.toolBox.IVarKeys;
import fable.python.Sample;
import fable.python.SampleException;

/**
 * @author suchet
 * 
 */
public class SampleController implements IPropertyChangeListener {

	/**
	 * class to register listeners for a specific sample name
	 * 
	 * @author andy
	 * 
	 */
	/*
	 * class SampleChangeListener { String sampleName; IPropertyChangeListener
	 * listener;
	 * 
	 * SampleChangeListener(String _name, IPropertyChangeListener _listener) {
	 * sampleName = _name; listener = _listener; } }
	 */
	private Vector<Sample> vSample = new Vector<Sample>();
	private static SampleController controller;
	// private ISampleAdapter sampleListener;
	private Sample sample; // active view ; current sample
	private ArrayList<IPropertyChangeListener> myListeners;
	private FabioFile currentFile;
	private int currentIndex;
	private Logger logger;

	private SampleController() {

		logger = FableLogger.getLogger((Class<?>) SampleController.class);
		vSample = new Vector<Sample>();
		myListeners = new ArrayList<IPropertyChangeListener>();

	}

	public String[] getKeys() throws SampleException {
		String[] res = null;
		if (sample != null) {
			res = sample.getKeys();
		}
		return res;
	}

	public static SampleController getController() {
		if (controller == null) {
			controller = new SampleController();

		}
		return controller;

	}

	public void addSample(Sample s) {
		vSample.add(s);
		s.addPropertyChangeListener(this);

		fireAddSample();
		// 06/08/2008 for peakssptBy default, set this sample to current sample
		// !
		sample = s;
	}

	public void setCurrentSample(Sample s) {
		sample = s;
		// System.out.println("setCurrentSample(): set current sample to "+sample.get_name());
		fireCurrentSample(vSample.indexOf(sample));
	}

	public Sample getCurrentsample() {
		return sample;
	}

	public int getCurrentsampleIndex() {
		int i = 0;
		if (vSample != null && sample != null) {
			i = vSample.indexOf(sample);
		}
		return i;
	}

	public int getNumberOfSample() {
		return vSample.size();
	}

	public Sample getLastSample() {
		return vSample.lastElement();
	}

	public void removeSample(int index) {
		vSample.removeElementAt(index);
	}

	public void removeSample(Sample s) {
		fireSampleRemoved(s);
		vSample.remove(s);

	}

	public int getCurrentFileIndex() {
		return currentIndex;
	}

	/**
	 * 
	 * 23 oct. 07
	 * 
	 * @author G. Suchet
	 * @param index
	 *            edf index in sample
	 * @description set current file (selected in table for example)from current
	 *              sample
	 */
	public void setCurrentFileIndex(int index) {
		if (sample != null) {
			if (index < 0)
				index = 0;
			else if (index >= sample.getFilteredfiles().size())
				index = sample.getFilteredfiles().size() - 1;
			currentIndex = index;
			currentFile = sample.getFilteredfiles().elementAt(index);
			sample.setCurrentFile(currentFile);
			fireCurrentFileHasChanged();
		}
	}

	/**
	 * 
	 * 23 oct. 07
	 * 
	 * @author G. Suchet
	 * @return currentFile if exists, else if one sample at least exists, return
	 *         the first element of the last sample loaded
	 */
	public FabioFile getCurrentFile() {
		if (currentFile != null) {
			return currentFile;
		} else if (vSample != null && vSample.size() > 0) {
			Sample directory = vSample.lastElement();
			if (directory.getFabioFiles() != null
					&& directory.getFabioFiles().size() > 0) {
				return directory.getFabioFiles().firstElement();
			}
		}
		return null;
	}

	// --------- LISTENER -----------------------------------------------------
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// DEBUG
		// System.out.println("SampleController.addPropertyChangeListener: " +
		// listener);
		myListeners.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// DEBUG
		// System.out.println("SampleController.removePropertyChangeListener: "
		// + listener);
		myListeners.remove(listener);
	}

	public synchronized void fireCurrentFileHasChanged() {
		logger.debug("fire event " + IVarKeys.SET_CURRENTFILE_EVENT);
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			// DEBUG
			// System.out.println("SampleController.fireCurrentFileHasChanged: "
			// + element);
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.SET_CURRENTFILE_EVENT, null, currentFile));
		}
	}

	/**
	 * Send an event to all listeners to do something on the next image in a
	 * list if i > 0 or with the previous image if i < 0. See an example in
	 * <code>fable.peaksearch</code>.
	 */
	private void fireGetNext(int i) {
		logger.debug("fire event "
				+ fable.framework.internal.IVarKeys.NEXTIMAGE);
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					fable.framework.internal.IVarKeys.NEXTIMAGE, null, i));
		}

	}

	/**
	 * Send an event to all listeners to do something on the first image in a
	 * list. See an example in <code>fable.peaksearch</code>.
	 */
	private void fireGetFirst() {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					fable.framework.internal.IVarKeys.FIRSTIMAGE, null, null));
		}

	}

	/**
	 * Send an event to all listeners to do something on the last image in a
	 * list. See an example in <code>fable.peaksearch</code>.
	 */
	private void fireGetLast() {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					fable.framework.internal.IVarKeys.LASTIMAGE, null, null));
		}

	}

	public void fireSampleRemoved(Sample s) {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.REMOVE_SAMPLE_EVENT, null, s));

		}
	}

	public void fireCurrentSample(int index) {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.CURRENT_SAMPLE_EVENT, null, index));

		}

	}//

	public void fireAddSample() {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.NEW_SAMPLE_EVENT, null, vSample.lastElement()));

		}
	}

	public void fireUpdatePlot() {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.UPDATE_PLOT_EVENT, null, null));

		}
	}

	/**
	 * This event is sent if a sample has new files.
	 */
	public void fireUpdateFiles() {
		for (Iterator<IPropertyChangeListener> iter = myListeners.iterator(); iter
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) iter
					.next();
			element.propertyChange(new PropertyChangeEvent(this,
					IVarKeys.UPDATE_SAMPLEFILES_EVENT, null, vSample
							.lastElement()));

		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals("updateHeaderValues")) {
			fireUpdatePlot();
		} else if (event.getProperty().equals(
				fable.framework.internal.IVarKeys.UPDATEFILES_EVENT)) {
			fireUpdateFiles();
		}

	}

	public Vector<Sample> getSamples() {
		return vSample;
	}

	/**
	 * 
	 * @param i
	 *            1 to get next image or -1 to get previous image
	 */
	public void getNext(int i) {
		fireGetNext(i);

	}

	/**
	 * Send an event to all <code>SampleController</code> listener to do
	 * something with the first image. See example in <code>PeaksSptView</code>.
	 */
	public void getFirstImage() {
		fireGetFirst();
	}

	/**
	 * Send an event to all <code>SampleController</code> listener to do
	 * something with the last image. See example in <code>PeaksSptView</code>.
	 */
	public void getLastImage() {
		fireGetLast();
	}
}
