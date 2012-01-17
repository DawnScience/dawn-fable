/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import fable.framework.internal.IVarKeys;

/**
 * 
 * @author G. Suchet fable.peaksearch 9 janv. 08 This class represents the
 *         output of peaksearcher.
 */
public class PeakSearchSpt {
	private int npks = 0;
	/** This is the name of spt file */
	private String parentName = "";
	private Vector<Peak> peaks; // keys (Number_of_pixels, Average_counts,f, s,
	// fc, sc , sig_f sig_s cov_fs)
	private float treshold = 0;
	float[] tabPeak = null; // an array containing (x,y) values for image viewer
	private List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();
	private String scannedPeak = ""; // file info block to be parsed when needed
	private String[] peakKeys;//
	private FabioFile imageFile = null;
	private String fabioFileName = "";
	private PeakSearchSpt parent = this;
	protected boolean parsed = false;

	/**
	 * f
	 * 
	 * @description constructor
	 * @param Filename
	 */
	public PeakSearchSpt(String parentFileName) {
		peaks = new Vector<Peak>(npks);
		parentName = parentFileName;
	}

	public void setNbPeaks(int pks) {
		npks = pks;
	}

	public void setTreshold(float t) {
		treshold = t;
	}

	public int getNbPeaks() {
		return npks;
	}

	public FabioFile getImageFile() {
		return imageFile;
	}

	public void initFabioFile(String fullFileName) throws FabioFileException {
		imageFile = new FabioFile(fullFileName);
	}

	/**
	 * 
	 * 14 janv. 08
	 * 
	 * @author G. Suchet
	 * @return treshold ; value stored in output peaksearch header
	 */
	public float getTreshold() {
		return treshold;
	}

	public void addPeak(Peak p) {
		peaks.add(p);
		addPropertyChangeListener(p);

	}

	public Vector<Peak> getSortedpeaks() {

		if (peaks != null && peaks.size() > 0) {
			Collections.sort(peaks);

		}

		return peaks;
	}

	public void setComparator(String Key, int dir) {
		fireChangeSorter(Key);
		firechangeSortdirection(dir);
	}

	public Peak getPeak(int i) {
		return peaks.elementAt(i);
	}

	public int getSize() {
		return peaks.size();
	}

	public String getParentFileName() {
		return parentName;
	}

	/**
	 * 
	 * @return filenames
	 */
	public String getFabioFileName() {
		return fabioFileName;
	}

	public Vector<Peak> getChildren() {
		return peaks;
	}

	/**
	 * 
	 * @return an array containing a pair of [i=s, i+1=f], for 2d peaks for
	 *         visible peaks (bshow=true).<br>
	 *         Peaks should be already parsed.
	 */
	public Vector<Float> getTabChildren() {
		Vector<Float> peaksToDisplay = new Vector<Float>(peaks.size());

		if (peaks != null) {

			int j = -1;
			int k = 0;
			for (int i = 0; i < peaks.size(); i++) {

				if (getPeak(i).isVisible()) {
					j = k + 1;
					peaksToDisplay.add(k, Float.valueOf(getPeak(i).getS()));
					peaksToDisplay.add(j, Float.valueOf(getPeak(i).getF()));

					k += 2;
				}
				peaksToDisplay.setSize(j + 1);
			}

		}
		return peaksToDisplay;
	}

	/**
	 * add this method for treeviewer content provider
	 */
	public String toString() {
		return parentName;
	}

	/**
	 * listKeys is:
	 * "Number_of_pixels Average_counts    f   s     fc   sc      sig_f sig_s cov_fs"
	 * As it comes from sptParser
	 */
	public void setKeys(String listKeys) {

		peakKeys = listKeys.split("\\s+");

	}

	/**
	 * listKeys is:
	 * "Number_of_pixels Average_counts    f   s     fc   sc      sig_f sig_s cov_fs"
	 * As it comes from sptParser
	 */
	public void setKeys(String[] listKeys) {

		peakKeys = listKeys;

	}

	public void setPeaks(String blockWithAllPeaks) {
		scannedPeak = blockWithAllPeaks;
	}

	/**
	 * 
	 * @param peak
	 *            this is one line coming from the parser 4194304 1040.879432
	 *            1025.619308 1024.586495 1025.797993 1025.442168 588.552309
	 *            588.380541 -0.000225
	 */
	public void concatPeaks(String peak) {

		scannedPeak = scannedPeak.concat(peak);
		scannedPeak = scannedPeak + System.getProperty("line.separator");
		// scannedPeak=scannedPeak.concat("\\n");
	}

	public void parse() {
		// npks=peaks.size();

		Job parseJob = new Job("Getting " + npks + " peaks in file "
				+ parentName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (!parsed) {
					parsed = true;
					Scanner scanner = new Scanner(scannedPeak);
					scanner.useDelimiter("[\n\r]+");//
					// System.out.println(scannedPeak);

					monitor.beginTask("Wait while loading peaks", npks);
					int percentage = 0;
					int j = 0;
					while (scanner.hasNext()) {
						String line = scanner.nextLine();
						if (!line.trim().equals("")) {
							line = peaks.size() + " " + line;
							Peak p = new Peak(peakKeys, line);
							p.setParent(parent);
							// The first time, all peaks can be displayed in the
							// imageViewer
							p.show(true);
							peaks.add(p);
							percentage = (peaks.size() * 100) / npks;
							monitor.subTask(percentage + "% done");
							// addPropertyChangeListener(p);
							if (j % 250 == 0 && j > 0) {
								fireSomePeaksLoaded();
							} else if (j == npks - 1) {
								fireSomePeaksLoaded();
							}
							j++;
							monitor.worked(1);
							if (monitor.isCanceled()) {
								scanner.close();
								return Status.CANCEL_STATUS;
							}
						}
					}

					scanner.close();
					// monitor.done();
				}
				monitor.done();
				return Status.OK_STATUS;

			}

		};
		parseJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				fireParseCompleted();
			}
		});
		parseJob.setUser(true);
		parseJob.schedule();

	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	/**
	 * Update table in sptview because parser is launched in a job
	 */
	private void fireSomePeaksLoaded() {

		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IVarKeys.ParseInProgress, null, null));
			}
		}

	}

	/**
	 * Update table in sptview because parser is launched in a job
	 */
	private void fireParseCompleted() {

		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IVarKeys.ParseCompleted, null, null));
			}
		}

	}

	/************************************************************************
	 * 
	 * Register or remove listeners
	 * 
	 **************************************************************************/
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeSorter(String comp) {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"comparator", null, comp));
			}
		}
	}

	public void firechangeSortdirection(int comp) {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this, "dir",
						null, comp));
			}
		}
	}

	public void setParentFileName(String sptFile) {
		parentName = sptFile;

	}

	public void setFabioFileName(String filename) {
		fabioFileName = filename;

	}

	public void setFabioFile(FabioFile object) {
		imageFile = object;

	}
}
