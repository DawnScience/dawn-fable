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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.dawb.fabio.FableJep;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

import fable.framework.internal.IVarKeys;
import fable.framework.toolbox.IEdfVarKeys;

/***
 * 
 * @author SUCHET
 * @description This class represents a sample.
 */
public class Sample {

	/**
	 * This was called experiment. This is the name of the parent directory of
	 * the directory of this file serie. dir + 1
	 */
	private String parentDirectoryName;
	// KE: Is the really the sample name
	private String directoryName;
	private String directoryPath;
	private String[] currentFiles; // current file names to process only. Should
	// contain path for the loads
	private static String DEFAULT_SAMPLE_PATH = "\\data\\opid11\\inhouse\\";
	private Vector<FabioFile> fabioFiles = new Vector<FabioFile>();
	private Vector<FabioFile> filteredfabiofiles = new Vector<FabioFile>();
	private Vector<ISampleListener> listListener = new Vector<ISampleListener>();
	private String fileFormat = "";
	public boolean headersRead = false;
	private String ndigits = "4";
	private String peaksearchOutStem = "peaks";
	private ArrayList<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();
	private String[] keys = null;
	private int last = -1; // //for peaksearch.
	int first = -1; // for peaksearch.
	private String filter = "";

	//
	private boolean showInNavigator = true;
	// A sample may exists without anyfiles (On Line ... waiting for images)
	private boolean isDirectory = true;

	private HashMap<String, double[]> headerValues = new HashMap<String, double[]>();
	private HashMap<String, double[]> headerdiffValues = new HashMap<String, double[]>();
	private FabioFile currentFabioFile; // Since we need current header keys
	private boolean isValide = true;
	private FableJep fableJep;
	private String stem = null;

	// Mode off line
	@SuppressWarnings("unchecked")
	// KE: Don't see a way to avoid this warning
	public Sample(String experimentName, String sampleName, String path,
			String[] listFiles) throws FabioFileException {
		this.setDirectoryName(sampleName);
		this.setParentDirectoryName(experimentName);
		this.setDirectoryPath(path);
		fabioFiles = new Vector<FabioFile>();
		filteredfabiofiles = (Vector<FabioFile>) fabioFiles.clone();
		addFabioFiles(listFiles);
	}

	public Sample(String experimentName, String sampleName, String path) {
		this.setDirectoryName(sampleName);
		this.setParentDirectoryName(experimentName);
		this.setDirectoryPath(path);

	}

	// Mode on line
	public Sample(String experimentName, String sampleName) {
		this.setDirectoryName(sampleName);
		this.setParentDirectoryName(experimentName);
		String path = DEFAULT_SAMPLE_PATH + "\\" + experimentName + "\\"
				+ sampleName;
		this.setDirectoryPath(path);
	}

	public Sample() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	// KE: Don't see a way to avoid this warning
	public Sample(String path, File[] files) throws FabioFileException {
		this.setDirectoryPath(path);
		fabioFiles = new Vector<FabioFile>();
		filteredfabiofiles = (Vector<FabioFile>) fabioFiles.clone();
		addFabioFiles(files);
	}

	public Sample(String experimentName, String sampleName, String directory,
			File[] filesInDir) throws FabioFileException {
		this(directory, filesInDir);
		this.setDirectoryName(sampleName);
		this.setParentDirectoryName(experimentName);
	}

	@SuppressWarnings("unchecked")
	// KE: Don't see a way to avoid this warning
	private void addFabioFiles(File[] files) throws FabioFileException {
		for (int i = 0; files != null && i < files.length; i++) {
			// load header
			addFabioFile(new FabioFile(files[i].getAbsolutePath()));
		}
		currentFiles = new String[fabioFiles.size()];
		for (int i = 0; fabioFiles != null && i < fabioFiles.size(); i++) {
			currentFiles[i] = fabioFiles.elementAt(i).getFullFilename();

		}
		filteredfabiofiles = (Vector<FabioFile>) fabioFiles.clone();
		fireSampleHasNewFiles();
	}

	/**
	 * For preprocessor, to avoid invalid thread access
	 */
	public void addCurrentFiles(String[] newFiles) {
		currentFiles = newFiles;
		fireSampleHasNewFiles();
	}

	/**
	 * @description add a new list of fabio files. currentFileName should
	 *              contain path for the call to load in edf constructor
	 * @throws FabioFileException
	 */
	@SuppressWarnings("unchecked")
	// KE: Don't see a way to avoid this warning
	public void addFabioFiles(String[] newFiles) throws FabioFileException {
		for (int i = 0; newFiles != null && i < newFiles.length; i++) {
			// load header
			addFabioFile(new FabioFile(newFiles[i]));
		}
		currentFiles = new String[fabioFiles.size()];
		for (int i = 0; fabioFiles != null && i < fabioFiles.size(); i++) {
			currentFiles[i] = fabioFiles.elementAt(i).getFullFilename();

		}
		filteredfabiofiles = (Vector<FabioFile>) fabioFiles.clone();
		fireSampleHasNewFiles();
	}

	public void updatefabioFiles(String[] newFiles) throws FabioFileException {
		fabioFiles.removeAllElements();
		for (int i = 0; newFiles != null && i < newFiles.length; i++) {
			// load header
			addFabioFile(new FabioFile(newFiles[i]));
		}
		currentFiles = new String[fabioFiles.size()];
		for (int i = 0; fabioFiles != null && i < fabioFiles.size(); i++) {
			currentFiles[i] = fabioFiles.elementAt(i).getFullFilename();
		}
		applyFilter();
		fireUpdatefiles();
	}

	/**
	 * 
	 * 30 nov. 07
	 * 
	 * @author G. Suchet
	 * @param fileName
	 * @throws FabioFileException
	 *             This method adds a fabio file to the vector
	 *             <code>fabiofiles</code>
	 */
	public boolean addFabioFile(FabioFile _fabioFile) throws FabioFileException {
		addPropertyChangeListener(_fabioFile);
		_fabioFile.addIndex(fabioFiles.size());
		fabioFiles.add(_fabioFile);
		// init filtered file here too
		filteredfabiofiles.add(_fabioFile);
		return true;
	}

	/**
	 * Get keys for current file
	 * 
	 * @return
	 * @throws SampleException
	 */
	public String[] getKeys() throws SampleException {
		// if(keys==null){
		if (fabioFiles != null && fabioFiles.size() > 0) {
			try {
				if (currentFabioFile != null) {

					keys = currentFabioFile.getKeys();
				}
			} catch (FabioFileException e) {

				throw new SampleException(this.getClass().getName(), "getKeys",
						"Header keys cannot be retrieved for current file ");
			} catch (Throwable e) {
				throw new SampleException(this.getClass().getName(), "getKeys",
						"Header keys cannot be retrieved for current file ");
			}
		}
		// }
		return keys;
	}

	/**
	 * 
	 * @return true if sample contains valid values (for example, if all files
	 *         are ready to be processed with a dark and a flood that has the
	 *         same dimension as files.
	 */
	public boolean isValide() {
		/*
		 * int i=0; boolean b=true; while(fabioFiles!=null &&
		 * i<fabioFiles.size() && b){
		 * 
		 * b = fabioFiles.elementAt(i).getFlag(); i++; }
		 */
		return isValide;
	}

	/**
	 * 
	 * 26 sept. 07
	 * 
	 * @author G. Suchet
	 * @return true if a file has been stored into sample at least
	 */
	public boolean hasFile() {
		return (currentFiles != null && currentFiles.length > 0);
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 14, 2007
	 * 
	 * @author G. Suchet
	 * @return the _Experiment
	 */
	public String getParentDirectoryName() {
		return parentDirectoryName;
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 14, 2007
	 * 
	 * @author G. Suchet
	 * @param experiment
	 *            the _Experiment to set
	 */
	public void setParentDirectoryName(String experiment) {
		if (experiment != null) {
			parentDirectoryName = experiment;
		}
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 12, 2007
	 * 
	 * @author G. Suchet
	 * @return the _name
	 */
	public String getDirectoryName() {
		return directoryName;
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 12, 2007
	 * 
	 * @author G. Suchet
	 * @param directoryName
	 * 
	 */
	public void setDirectoryName(String name) {
		if (name != null) {
			this.directoryName = name;
		}
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 14, 2007
	 * 
	 * @author G. Suchet
	 * @return the directoryPath
	 */
	public String getDirectoryPath() {
		return directoryPath;
	}

	/**
	 *fable.preprocessor fable.preprocessor.process Feb 14, 2007
	 * 
	 * @author G. Suchet
	 * @param path
	 *            the directoryPath to set
	 */
	public void setDirectoryPath(String path) {
		// KE: Seems to be a problem in that this doesn't also update the
		// directoryName
		directoryPath = path;
	}

	public String toString() {
		return "Experiment : " + getParentDirectoryName() + " Sample name: "
				+ getDirectoryName() + " Directory: " + getDirectoryPath();
	}

	/**
	 * Get files to process with the filter and also a good size comparing to
	 * the dark and/or the flood
	 * 
	 * @param flag
	 * @return
	 */
	public String[] getFabioFilesToProcess(boolean flag) {
		Vector<String> filesToGet = new Vector<String>();
		for (int i = 0; filteredfabiofiles != null
				&& i < filteredfabiofiles.size()
				&& filteredfabiofiles.elementAt(i).getFlag() == true; i++) {
			filesToGet.add(filteredfabiofiles.elementAt(i).getFullFilename());
		}
		String[] files = new String[filesToGet.size()];
		for (int i = 0; i < filesToGet.size(); i++) {
			files[i] = filesToGet.elementAt(i);
		}
		return files;
	}

	/**
	 * @return A Tab with the names of the current files to process
	 */
	public String[] getFiles() {
		return currentFiles;
	}

	/**
	 * @param _files
	 *            the current files to set. Warning : no fabio files loaded
	 */
	public void setFiles(String[] _files) {
		this.currentFiles = _files;
	}

	/**
	 * @param _files
	 *            the current files to set. Warning : no fabio files loaded
	 */
	public void setCurrentFilesFromFabio() {
		currentFiles = new String[fabioFiles.size()];
		for (int i = 0; fabioFiles != null && i < fabioFiles.size(); i++) {
			currentFiles[i] = fabioFiles.elementAt(i).getFullFilename();
		}
		fireSampleHasNewFiles();
	}

	/**
	 * 
	 * 26 sept. 07
	 * 
	 * @author G. Suchet
	 * @return a vector containing all edf files object
	 */
	public Vector<FabioFile> getFabioFiles() {
		return this.fabioFiles;
	}

	/**
	 * 
	 * 22 Jul 2007
	 * 
	 * @author G. Suchet
	 * @return a vector containing filtered files
	 */
	public Vector<FabioFile> getFilteredfiles() {
		return this.filteredfabiofiles;
	}

	public void setComparator(String Key, int dir) {
		fireSetComparator(Key);
		fireSetDirection(dir);
	}

	/**
	 * This function is used to sort a vector of fabio files.
	 * 
	 * @return a sorted vector of fabio files
	 */
	public Vector<FabioFile> getSortedFiles() {
		// Date now = new Date();
		Collections.sort(fabioFiles);
		return fabioFiles;
	}

	/**
	 * This function is used to set a key comparator to each fabio files which
	 * are listening to Sample events.
	 * <p>
	 * For example, if you want to sort fabio files based on omega (key
	 * available in fabio header), this function send a comparator event with
	 * key as new value to all fabio files registered for this sample.
	 * 
	 * @param key
	 */
	private void fireSetComparator(String key) {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"comparator", null, key));
			}
		}
	}

	/**
	 * This function is used to set a comparator direction to each fabio files
	 * which are listening to Sample events.
	 * <p>
	 * For example, if you want to sort fabio files based on a key previously
	 * set (key available in fabio header), this function send a comparator
	 * direction event with ASC OR DESC as new value to all fabio files
	 * registered for this sample.
	 * 
	 * @param key
	 */
	private void fireSetDirection(int dir) {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this, "dir",
						null, dir));
			}
		}
	}

	/**
	 * This function is used to update sample files.
	 */
	private void fireUpdatefiles() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IVarKeys.UPDATEFILES_EVENT, null, this));
			}
		}

	}

	/***************************************************************************
	 * Register or remove listeners to sample changes
	 * 
	 **************************************************************************/
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void removePropertyrChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/**************************************************************************/
	public void removeFiles() {
		if (hasFile()) {
			currentFiles = null;
			fabioFiles.removeAllElements();
			filteredfabiofiles.removeAllElements();
			fireSampleHasChanged();
		}
	}

	public void removeFileAt(int indice) {
		if (hasFile()) {
			fabioFiles.removeElementAt(indice);
			fireSampleHasChanged();
		}
	}

	public void removeFabioFile(FabioFile f) {
		if (hasFile() && f != null) {
			fabioFiles.removeElement(f);
			if (filteredfabiofiles != null && filteredfabiofiles.size() > 0) {
				filteredfabiofiles.remove(f);
			}
			fireSampleHasChanged();
		}
	}

	public void setFilter(String s_filter) {
		filter = s_filter;
		applyFilter();
	}

	/**
	 * 22/07/2008 This function fills vector
	 * <code>filteredfabiofiles<code>, containing all files following 
	 * this filter expression as parameter.
	 * Used in <code>SampleNavigatorView<code>.
	 * This function is responsible for building regular expression.
	 * <p>
	 * If user has set a * in the filter expression (DB regex), this function replace all stars
	 *  with ".*".
	 *  If filter is null or equals to "", filteredfabiofiles is a clone of all files
	 *  available for this sample
	 * 
	 * @param filter
	 *            the name of the filter to apply for the regular expression.
	 */
	@SuppressWarnings("unchecked")
	// KE: Don't see a way to avoid this warning
	private void applyFilter() {
		assert (fabioFiles != null);
		filteredfabiofiles = (Vector<FabioFile>) fabioFiles.clone();
		if (filter != null && !filter.trim().equals("")) {
			/*
			 * if(filter.contains("*")){
			 * 
			 * filter = filter.replaceAll("\\*",".*"); }
			 */

			// Pattern pattern = Pattern.compile(filter);
			for (Iterator<FabioFile> iterate = fabioFiles.iterator(); iterate
					.hasNext();) {
				FabioFile file = iterate.next();
				String filename = file.getFileName();
				if (!filename.contains(filter)) {
					filteredfabiofiles.remove(file);
				} else {
					int updateIndex = filteredfabiofiles.indexOf(file);
					if (updateIndex >= 0) {
						file.addHeaderInfo("#", String.valueOf(updateIndex));
					}
				}
			}

		} else {
			// init index for fabio
			for (int i = 0; i < fabioFiles.size(); i++) {
				((FabioFile) fabioFiles.elementAt(i)).addHeaderInfo("#", String
						.valueOf(i));
			}
		}
	}

	/******************************************************************/
	private void fireSampleHasNewFiles() {
		SampleEvent se = new SampleEvent(this);
		for (Enumeration<ISampleListener> e = listListener.elements(); e
				.hasMoreElements();) {
			((ISampleListener) e.nextElement()).newImages(se);
		}
	}

	private void fireSampleHasChanged() {
		SampleEvent se = new SampleEvent(this);
		for (Enumeration<ISampleListener> e = listListener.elements(); e
				.hasMoreElements();) {
			((ISampleListener) e.nextElement()).sampleHasChanged(se);
		}
	}

	/**
	 * update plot every 10 files read
	 */
	public void fireSomeHeaderValuesLoaded() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"updateHeaderValues", null, null));
			}
		}
	}

	public void addSampleListener(ISampleListener pl) {
		this.listListener.add(pl);
	}

	public void removeSampleListener(ISampleListener pl) {
		listListener.remove(pl);
	}

	/*******************************************************
	 * 
	 * @return stem of sample
	 */
	public String getName() {
		return directoryName;
	}

	/*******************************************************
	 * 
	 * @return the number of the first file
	 * @throws FabioFileException
	 * @throws FabioFileException
	 */
	public int getFirst() {
		// sort files
		if (hasFile() && first == -1) {
			try {
				first = getFileNumber(fabioFiles.elementAt(0).getFileName());
			} catch (FabioFileException e) {
				first = 0;
				// throw e;
			}
		}
		return first;
	}

	public void setFirst(int one) {
		first = one;
	}

	public void setlast(int l) {
		last = l;
	}

	public int getLast() {
		if (hasFile() && last == -1) {
			try {
				last = getFileNumber(fabioFiles.lastElement().getFileName());
			} catch (FabioFileException e) {
				last = 0;
				// throw e;
			}
		}

		return last;
	}

	private int getFileNumber(String filename) throws FabioFileException {

		String first = "";
		int ret = -99;
		String format = getFileFormat();
		if (format != null) {
			if (format.equals(IEdfVarKeys.FILE_FORMAT_BRUKER)) {
				first = filename.substring(filename.lastIndexOf("."));
			} else {
				int index = filename.indexOf(".");
				if (index > 0) {
					filename = filename.substring(0, index);
					if (filename.length() - 4 > 0) {
						first = filename.substring(filename.length() - 4,
								filename.length());
					}
				}
			}
			try {
				ret = Integer.valueOf(first).intValue();
			} catch (Exception e) {
				throw new FabioFileException(this.getClass().getName(),
						"getFileNumber", "Sample can not get file number");
			}
		}
		return ret;
	}

	/**
	 * 
	 * @return the number of the last file
	 * @throws FabioFileException
	 */
	public String getFileFormat() {
		if (hasFile() && fileFormat.equals("")) {
			String fileName = this.fabioFiles.elementAt(0).getFileName();
			int indexExtension = fileName.indexOf(".");
			if (indexExtension >= 0) {
				fileFormat = fileName.substring(indexExtension);
				try {
					Integer.valueOf(fileFormat);
					fileFormat = IEdfVarKeys.FILE_FORMAT_BRUKER;
					// System.out.println("File format : " + fileFormat);
				} catch (NumberFormatException n) {
					// file format has been found previously
					// System.out.println("File format : " + fileFormat);
				}
			}
		}
		return fileFormat;
	}

	public void setFileFormat(String format) {
		fileFormat = format;
	}

	public void setNDigits(String val) {
		ndigits = val;
	}

	public String getNDigits() {
		return ndigits;
	}

	public void setPeaksearchOutfile(String outfile) {
		peaksearchOutStem = outfile;
	}

	public String getPeaksearchoutStem() {
		return peaksearchOutStem;
	}

	/**
	 * used in Header plot
	 * 
	 * @param key
	 *            header key with number values. Except for the date.
	 * @return double[] all double values for this key for all fabio files in
	 *         this sample
	 * @throws SampleException
	 */
	public double[] getHeaderValues(final String key) throws SampleException {
		final int size = filteredfabiofiles.size();
		double[] values = headerValues.get(key);
		Job job = new Job("Get values for " + key) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Wait while getting values for " + key
						+ " in sample " + getDirectoryName(),
						filteredfabiofiles.size());
				if (hasFile()) {
					int i = 0;
					// get all header values if it has not been done for this
					// key\
					for (Iterator<FabioFile> it = filteredfabiofiles.iterator(); it
							.hasNext();) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						final FabioFile f = ((FabioFile) it.next());
						if (i < headerValues.get(key).length) {
							final int j = i;
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									double[] fillValues = headerValues.get(key);
									try {
										if (key.toLowerCase().contains("date")) {
											// convert date final
											SimpleDateFormat dateFormat = new SimpleDateFormat(
													"EEE MMM dd HH:mm:ss yyyy",
													Locale.UK);
											try {
												Date date = dateFormat.parse(f
														.getValue(key));
												// long l = date.getTime();
												fillValues[j] = date.getTime();
											} catch (ParseException e) {
												// e.printStackTrace();
											}
										} else {
											fillValues[j] = Double.valueOf(f
													.getValue(key));
										}
										// Send a message every 10 files
										if (j % 10 == 0 && j > 0) {
											fireSomeHeaderValuesLoaded();
										} else if (j == fillValues.length - 1) {
											fireSomeHeaderValuesLoaded();
										}
									} catch (NumberFormatException e) {
										monitor.setCanceled(true);

									} catch (FabioFileException e) {
										monitor.setCanceled(true);
									}
								}
							});

							i++;
							monitor.worked(1);
						}
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		if (values == null || values.length != size) {
			values = new double[size];
			headerValues.put(key, values);
			// Load header values
			job.schedule();
		}
		return headerValues.get(key);
	}

	public void addHeaderValues(String key, double[] values) {
		headerValues.put(key, values);
	}

	/**
	 * used in Header plot
	 * 
	 * @param key
	 *            header key
	 * @return double[] all double values for this key for all fabio files in
	 *         this sample
	 * @throws SampleException
	 */
	public double[] getHeaderValuesDiff(final String key)
			throws SampleException {
		final int size = filteredfabiofiles.size();
		double[] values = headerdiffValues.get(key);
		Job job = new Job("Get values for " + key) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Wait while getting values for " + key
						+ " in sample " + getDirectoryName(),
						filteredfabiofiles.size());
				if (hasFile()) {
					// get all header values if it has not been done for this
					// key\
					for (int it = 0; it < filteredfabiofiles.size(); it++) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						final FabioFile f = filteredfabiofiles.elementAt(it);
						final FabioFile next;
						if (it + 1 < filteredfabiofiles.size()) {
							next = filteredfabiofiles.elementAt(it + 1);
							if (it < headerdiffValues.get(key).length) {
								final int j = it;
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										double[] fillValues = headerdiffValues
												.get(key);
										try {
											double dnext = Double.valueOf(next
													.getValue(key));
											double delem = Double.valueOf(f
													.getValue(key));
											fillValues[j] = dnext - delem;
											// Send a message every 10 files
											if (j % 10 == 0 && j > 0) {
												fireSomeHeaderValuesLoaded();
											} else if (j == fillValues.length - 1) {
												fireSomeHeaderValuesLoaded();
											}
										} catch (NumberFormatException e) {
											fillValues[j] = 0;
											if (j % 10 == 0 && j > 0) {
												fireSomeHeaderValuesLoaded();
											} else if (j == fillValues.length - 1) {
												fireSomeHeaderValuesLoaded();
											}

										} catch (FabioFileException e) {
											monitor.setCanceled(true);
										}
									}
								});

								monitor.worked(1);
							}
						}
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		if (values == null || values.length != size) {
			values = new double[size];
			headerdiffValues.put(key, values);
			// Load header values
			job.schedule();
		}
		return headerdiffValues.get(key);
	}

	public void setCurrentFile(FabioFile fabio) {
		currentFabioFile = fabio;
	}

	public FabioFile getCurrentFabioFile() {
		return currentFabioFile;
	}

	/**
	 * 
	 * @return true if files have been set to this sampl by selecting a
	 *         directory.
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * 
	 * @param isDirectory
	 *            true if files have been loaded from a folder. False if files
	 *            have been selected from files menu.
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getFilter() {
		return filter;
	}

	/**
	 * 07/08/08 Gaelle since peaksearchSptView is speaking with imageviewer
	 * 
	 * @return true if this sample must be shown in the navigator
	 */
	public boolean isShowInNavigator() {
		return showInNavigator;
	}

	/**
	 * 07/08/08 Gaelle since peaksearchSptView is speaking with imageviewer
	 * 
	 * @param showInNavigator
	 *            true if sample must be shown in sample navigator
	 */
	public void setShowInNavigator(boolean showInNavigator) {
		this.showInNavigator = showInNavigator;
	}

	public void setValide(boolean b) {
		isValide = b;
	}

	/**
	 * Create a fileserie based on files loaded from the directory.
	 */
	private boolean createFile_SeriesForPython() {
		boolean evalOk = true;
		if (fableJep == null) {
			try {
				fableJep = FableJep.getFableJep();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		try {
			FableJep.getFableJep().jepImportModules("sys");
			FableJep.getFableJep().jepImportModules("numpy");
			FableJep.getFableJep().jepImportModules("PIL");
			FableJep.getFableJep().jepImportModules("fabio.file_series");
			fableJep.eval("from fabio.file_series import file_series");
			fableJep.eval("fileList = []");
			for (int i = 0; i < filteredfabiofiles.size(); i++) {
				fableJep.set("file", filteredfabiofiles.elementAt(i)
						.getFullFilename());
				fableJep.eval("fileList.append(file)");
			}
			evalOk = fableJep.eval("fileseries = file_series(fileList)");
		} catch (Throwable e) {
			evalOk = false;
			e.printStackTrace();
		}
		return evalOk;

	}

	/**
	 * This method init a file serie in python object. It is based on
	 * filteredfabio file.
	 * 
	 * @return the stem taken from python code.
	 */
	public String getStem() {
		assert (filteredfabiofiles != null);
		if (stem == null) {
			if (createFile_SeriesForPython()) {
				int numberOfFiles = fableJep.getIntegerValue("len(fileseries)");
				if (numberOfFiles > 0) {
					stem = fableJep
							.getStringValue("fabio.deconstruct_filename(fileseries[0]).stem");
				}
			}
		}
		return stem;
	}

	/**
	 * Update the list of FabioFiles by replacing the current list with the new
	 * list of file names. For efficiency reasons reuse the existing FabioFile
	 * if exists in the old list.
	 * 
	 * @param Files
	 *            [] files - new list of files to replace existing list in
	 *            Sample
	 * @throws FabioFileException
	 */
	@SuppressWarnings("unchecked")
	public void updateFabioFiles(File[] files) throws FabioFileException {
		Vector<FabioFile> oldFabioFiles = (Vector<FabioFile>) fabioFiles
				.clone();
		fabioFiles.removeAllElements();
		for (int i = 0; files != null && i < files.length; i++) {
			FabioFile fabioFile;
			boolean fileFound = false;
			int fileFoundIndex = -1;
			/*
			 * for efficiency reasons viz. to avoid rereading files, first look
			 * if the file name exists in the old list if so reuse the FabioFile
			 * from the old list
			 */
			for (int j = 0; j < currentFiles.length && !fileFound; j++) {
				if (currentFiles[j]
						.equalsIgnoreCase(files[i].getAbsolutePath())
						&& j < oldFabioFiles.size()) {
					fileFound = true;
					fileFoundIndex = j;
					// if (false) {
					// Logger logger = Logger.getLogger(Sample.class);
					// logger.debug("found FabioFile at index "
					// + fileFoundIndex);
					// }
				}
			}
			if (!fileFound) {
				fabioFile = new FabioFile(files[i].getAbsolutePath());
			} else {
				fabioFile = oldFabioFiles.elementAt(fileFoundIndex);
			}
			addFabioFile(fabioFile);
		}
		currentFiles = new String[fabioFiles.size()];
		for (int i = 0; fabioFiles != null && i < fabioFiles.size(); i++) {
			currentFiles[i] = fabioFiles.elementAt(i).getFullFilename();
		}
		applyFilter();
		fireUpdatefiles();
	}

	/**
	 * 
	 * @param val
	 *            the stem to set to this file serie
	 */
	public void setStem(String val) {
		stem = val;
	}
}
