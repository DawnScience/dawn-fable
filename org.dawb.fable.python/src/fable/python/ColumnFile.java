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
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import jep.JepException;

import org.dawb.fabio.FableJep;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.internal.IPropertyVarKeys;
import fable.framework.logging.FableLogger;
import fable.framework.toolbox.ColumnFileId;
import fable.framework.toolbox.FableUtils;

/**
 * This is a python module interface in java for columnfile.py. Use it to parse
 * a file with columns
 * <p>
 * columnfile represents an ascii file with titles begining "#" and multiple
 * lines of data. An equals sign "=" on a "#" line implies a parameter = value
 * pair
 * 
 * @author SUCHET
 * 
 */
public class ColumnFile {

	private FableJep fableJep;
	protected HashMap<String, double[]> columns = new HashMap<String, double[]>();
	private ArrayList<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();
	private Logger logger;
	private String fullFileName;
	private String fileName;
	int nCols = 0;
	int nRows = 0;
	float[] unitCell;
	public String[] titles;

	private float[] table_data;
	private HashMap<String, Integer> column_index;
	/** To sort tableViewer for editor, please set selected columnindex */
	private int sortedColumnIndex;
	/** To know if we should sort in ascendant or descendant oder. */
	private int sortedDirection;
	/** Sorted index */
	private float[] bigArraySorted;

	/**
	 * Class constructor
	 * 
	 * @throws JepException
	 * 
	 */
	public ColumnFile() throws Throwable {
		fableJep = FableJep.getFableJep();
		importModules();
		logger = LoggerFactory.getLogger(ColumnFile.class);

	}

	/**
	 * Class constructor column_object already exists in jep_as_parameter
	 * 
	 * @param jep_as_parameter
	 * @throws JepException
	 */
	public ColumnFile(FableJep jep_as_parameter) throws JepException {
		fableJep = jep_as_parameter;
		importModules();
		logger = LoggerFactory.getLogger(ColumnFile.class);
		// name = (String)fableJep.getValue("column_object.filename");
		fireAddColumnFile();
	}

	/**
	 * Import the python modules needed to read columnfiles in jep.
	 */
	private void importModules() {
		try {
			FableJep.getFableJep().jepImportModules("numpy"); //$NON-NLS-1$
			FableJep.getFableJep().jepImportSpecificDefinition("ImageD11", "columnfile"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable ex) {
			FableUtils.excNoTraceMsg(this, "Error importing modules", ex);
		}
	}

	/**
	 * Class constructor specifying file to parse.
	 * <p>
	 * Creates a new column_object python object. Use it if you don' t have a
	 * columnfile object in your jep
	 * 
	 * @param FileName
	 * @throws JepException
	 */
	public ColumnFile(String _fileName) throws Throwable {
		this();
		loadColumnFile(_fileName);
	}

	/**
	 * open a new column file in the current jep Init sort index, based on the
	 * file id, or on the first column if it is not defined.
	 * 
	 * @param _fileName
	 *            - name of column file to open
	 * @throws JepException
	 */
	public void loadColumnFile(String _fileName) {

		/*
		 * final ImageDescriptor pluginImage = AbstractUIPlugin.
		 * imageDescriptorFromPlugin(Activator.PLUGIN_ID, "fable.gif");
		 */

		try {
			fableJep.set("name", _fileName); //$NON-NLS-1$
			setFileName(_fileName);
			fableJep.eval("column_object=columnfile.columnfile(name)"); //$NON-NLS-1$
			fableJep.eval("column_object.readfile(name)"); //$NON-NLS-1$

			loadRows();
			sortedColumnIndex = getColumnIDIndex();
			setSortedIndex(sortedColumnIndex, SWT.UP);
			if (sortedColumnIndex < 0) {
				sortedColumnIndex = 0;
			}
			fireAddColumnFile();

		} catch (JepException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage());
			// e.printStackTrace();
		}
	}

	/***
	 * For example, in transformer, colmnfile object exists in python object
	 * transformer. So we don't create a new columnfile like it is created in
	 * contructor. And filename haven' t been set.
	 * 
	 * @param name
	 */
	public void setFileName(String name) {
		fullFileName = name;
		if (fullFileName != null && fullFileName.length() > 1) {
			fileName = fullFileName.substring(fullFileName
					.lastIndexOf(File.separatorChar) + 1);
		} else {
			fileName = ""; //$NON-NLS-1$
		}

	}

	/**
	 * Return the full file name.
	 * 
	 * @return
	 */
	public String getFullFileName() {
		return fullFileName;
	}

	/**
	 * Return the short file name.
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	public void setPythonObject(FableJep jep_as_parameter) throws JepException {

		fableJep = jep_as_parameter;
		logger = LoggerFactory.getLogger(ColumnFile.class);
		fullFileName = (String) fableJep.getValue("column_object.filename"); //$NON-NLS-1$

	}

	/**
	 * This method load a big array of float from python object.
	 * 
	 */
	public void loadRows() {
		try {
			float f = 0f;
			Object objRows = fableJep.getValue("column_object.nrows");
			if (objRows instanceof Integer) {
				FableLogger.getLogger().debug("Integer");
				nRows = (Integer) fableJep.getValue("column_object.nrows"); //$NON-NLS-1$

			} else if (objRows instanceof Float) {
				FableLogger.getLogger().debug("Float");
				f = (Float) fableJep.getValue("column_object.nrows");//$NON-NLS-1$
				nRows = (int) f;

			}
			f = 0f;
			Object objCols = fableJep.getValue("column_object.ncols");
			if (objCols instanceof Integer) {
				FableLogger.getLogger().debug("Integer");
				nCols = (Integer) fableJep.getValue("column_object.ncols"); //$NON-NLS-1$

			} else if (objCols instanceof Float) {
				FableLogger.getLogger().debug("Float");
				f = (Float) fableJep.getValue("column_object.ncols");//$NON-NLS-1$
				nCols = (int) f;

			}
			titles = new String[nCols];
			column_index = new HashMap<String, Integer>();
			for (int i = 0; i < nCols; i++) {
				fableJep.set("i", i); //$NON-NLS-1$

				titles[i] = (String) fableJep
						.getValue("column_object.titles[i]"); //$NON-NLS-1$
				column_index.put(titles[i], i);
			}

			table_data = (float[]) fableJep
					.getValue_floatarray("column_object.bigarray.astype(numpy.float32).tostring()"); //$NON-NLS-1$

		} catch (JepException e) {
			logger.error("can not create loadRows : " + e.getMessage()); //$NON-NLS-1$
		}

	}

	/**
	 * This method returns the name of the id for a columnfile..... To be
	 * improved, no ?
	 * 
	 * @return columnfile id (spot3d_id if file type is flt.)
	 */
	public String getColumnfileId() {
		String id = ""; //$NON-NLS-1$
		if (this.fileName != null
				&& this.fileName.toLowerCase().endsWith(
						ColumnFileId.getString("ColumnFile.flt"))) { //$NON-NLS-1$
			id = ColumnFileId.getString("ColumnFile.idflt"); //$NON-NLS-1$
		}
		return id;
	}

	/**
	 * This method is used to get the index of the column that have columnFile
	 * if. For example, for a flt file, id=spot3d_id and index is the last
	 * column.
	 * 
	 * @return the index of the column in titles[] where key = file id. If index
	 *         is not found, return value is -1.
	 */
	public int getColumnIDIndex() {
		int i = 0;
		boolean found = false;
		for (i = 0; !found && i < titles.length; i++) {
			if (titles[i].equals(getColumnfileId())) {
				found = true;
			}

		}
		if (!found) {
			i = -1;
		}
		return i - 1;
	}

	/**
	 * This methods have been created (thanks to Jon for its help in python), to
	 * sort columns in table viewer, as it is now virtual. In fact, the content
	 * provider <code>ILazyContentProvider</code> is not compatible with a sort
	 * set on the table. <br>
	 * <b>Numpy module for python side of this method must have been imported
	 * previously.
	 * 
	 * @param the
	 *            index of the column to sort
	 * @return a table with sorted index
	 */
	public float[] getSortedIndex(int columnIndex) {
		try {
			fableJep.set("i", columnIndex); //$NON-NLS-1$
			// fableJep.set("sortedTable", columnIndex);
			// numpy sort returns a table of sorted values
			/*
			 * fableJep.eval("sortedTable =" +
			 * "numpy.argsort(column_object.bigarray[:i])");
			 */
			if (sortedDirection == SWT.UP) {
				return (float[]) fableJep
						.getValue_floatarray("numpy.argsort(column_object.bigarray[i,:]).astype(numpy.float32)." //$NON-NLS-1$
								+ "tostring()"); //$NON-NLS-1$
			} else {
				return (float[]) fableJep
						.getValue_floatarray("numpy.argsort(column_object.bigarray[i,:])[::-1]." + //$NON-NLS-1$
								"astype(numpy.float32)." //$NON-NLS-1$
								+ "tostring()"); //$NON-NLS-1$
			}
		} catch (JepException e) {
			logger.debug(e.getMessage());
		}
		return null;

	}

	/**
	 * This function is used to save a column file object, for example if spots
	 * have been removed for the original file.
	 * 
	 * @param filename
	 *            the name of the file to save this column file object.
	 * @return true if no error occured while saving this file.
	 */
	public boolean saveColumnFile(String filename) {
		boolean bok = true;
		try {
			fableJep.set("filename", filename); //$NON-NLS-1$
			fableJep.eval("column_object.writefile(filename)"); //$NON-NLS-1$
			setFileName(filename);
			fireSaveDone();
		} catch (JepException e) {
			bok = false;
		}
		return bok;
	}

	/**
	 * @return A HashMap<String, double[]> with keys for column label and
	 *         double[] for values to plot.
	 */
	public HashMap<String, double[]> getColumnstoPlot() {
		// Get column file
		Display.getDefault().syncExec(new Runnable() {
			// @Override
			public void run() {
				try {
					columns.clear();
					if (fableJep == null) {
						throw new InvalidObjectException("FableJep is null");
					}
					Object val = fableJep.getValue("column_object.ncols"); //$NON-NLS-1$
					if (val != null) {
						nCols = (Integer) val;
						titles = new String[nCols];
						for (int i = 0; i < nCols; i++) {
							fableJep.set("i", i); //$NON-NLS-1$
							String name = (String) fableJep
									.getValue("column_object.titles[i]"); //$NON-NLS-1$
							fableJep
									.eval("filteredValues=column_object.getcolumn(" + //$NON-NLS-1$
											"column_object.titles[i])"); //$NON-NLS-1$

							float[] myData = (float[]) fableJep
									.getValue_floatarray("filteredValues.astype(numpy.float32).tostring()"); //$NON-NLS-1$
							double[] myDoubleToplot = new double[myData.length];
							nRows = myData.length;

							// Converts float to double for the chart
							for (int j = 0; j < myData.length; j++) {
								myDoubleToplot[j] = (double) myData[j];

							}
							titles[i] = name;
							columns.put(name, myDoubleToplot);
						}
					}
				} catch (JepException ex) {
					FableUtils.excNoTraceMsg(this,
							"Cannot create columns to plot", ex);
				} catch (Exception ex) {
					FableUtils.excTraceMsg(this,
							"Unexpected error creating columns to plot", ex);
				}
			}
		});
		return columns;
	}

	/***
	 * 
	 * @param index
	 *            the number of the row.
	 * @param colName
	 *            the name of the column to get index.
	 * @return the value of the cell at for this index row and for this column.
	 * 
	 */
	public float getColumnFileCell(int index, String colName) {
		float returnValue = 0;

		Integer indexColumn = column_index.get(colName);
		if (indexColumn != null) {
			index = index + (indexColumn * nRows);
			if (index < table_data.length) {
				returnValue = table_data[index];
			}
		}
		return returnValue;
	}

	/*
	 * 
	 * /
	 */
	public void update() {
		fireColumnHasBeenUpdated();
	}

	/**
	 * 
	 * 
	 */
	public void AddUnitCell(float[] values) {
		unitCell = values;
		fireAddUnitCell();
	}

	public float[] getUnitCell() {
		return unitCell;
	}

	public int getNRows() {
		return nRows;
	}

	public int getNCols() {
		return nCols;
	}

	// String[] array = (String[])set.toArray(new String[set.size()]);
	public String[] getTitles() {
		return titles;

		// return (String[])columns.keySet().toArray(new
		// String[columns.keySet().
		// size()]);
	}

	/**
	 * Add a listener to this columnFile.
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * remove a listener from this columnFile.
	 * 
	 * @param listener
	 */
	public void removePropertyrChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Sends a <code>PropertyChangeEvent</code> to all ColumnFile listeners when
	 * a new column is added in python object.
	 */
	public void fireAddColumnFile() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IPropertyVarKeys.ADDCOLUMN, null, this));
			}
		}
	}

	/**
	 * Sends a <code>PropertyChangeEvent</code> to all ColumnFile listeners when
	 * a column have been updated in python object.
	 */
	public void fireColumnHasBeenUpdated() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IPropertyVarKeys.UPDATECOLUMN, null, this));
			}
		}
	}

	private void fireSaveDone() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IPropertyVarKeys.PROPDIRTY, true, false));
			}
		}

	}

	/**
	 * Sends a <code>PropertyChangeEvent</code> to ColumnFile listeners when
	 * unit cells have been added in python object. <BR>
	 * This method is very specific to filtered peaks file and for their
	 * calibration.
	 */
	public void fireAddUnitCell() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						IPropertyVarKeys.ADDUNITCELL, null, this));
			}
		}
	}

	/**
	 * remove all dots between these values
	 * 
	 * @param key1min
	 *            min value for key1
	 * @param key1max
	 *            max value for key1
	 * @param key2min
	 *            min value for key2
	 * @param key2max
	 *            max value for key2
	 * @throws JepException
	 */
	public void removeDots(String key1, double key1min, double key1max,
			String key2, double key2min, double key2max) throws JepException {
		if (fableJep != null) {
			fableJep.set("key1min", key1min); //$NON-NLS-1$
			fableJep.set("key1max", key1max); //$NON-NLS-1$
			fableJep.set("key2min", key2min); //$NON-NLS-1$
			fableJep.set("key2max", key2max); //$NON-NLS-1$
			fableJep.eval("column_object." + key1); //$NON-NLS-1$
			fableJep.eval("mask = (column_object." + key1 + " >= key1min) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key1 + "<= key1max) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key2 + " >= key2min) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key2 + "<= key2max)"); //$NON-NLS-1$ //$NON-NLS-2$
			fableJep.eval("column_object.filter(~mask)"); //$NON-NLS-1$
			loadRows();
			initBigArraySorted();
			update();
		}
	}

	/**
	 * This methods remove selected rows from columnfile.
	 * 
	 * @param idlist
	 *            a table of id selected in a tableViewer.
	 * @param id
	 *            columnFile id.
	 */
	public void removeRow(Object[] idlist, String id) {
		if (fableJep != null) {
			makeMaskForSelectedRows(idlist, id);
			try {
				fableJep.eval("column_object.filter(~mask)"); //$NON-NLS-1$
				loadRows();
				initBigArraySorted();
			} catch (JepException e) {
				logger.debug("filter on selected rows canno be apply."); //$NON-NLS-1$
				logger.debug(e.getMessage());
			}
			update();
		}
	}

	/**
	 * This methods remove selected rows from columnfile.
	 * 
	 * @param rows
	 *            a table of id selected in a tableViewer.
	 * @param id
	 *            columnFile id.
	 */
	public void keepRow(Object[] rows, String id) {
		if (fableJep != null) {
			makeMaskForSelectedRows(rows, id);
			try {
				fableJep.eval("column_object.filter(mask)"); //$NON-NLS-1$
				loadRows();
				// Update bigArraySorted
				initBigArraySorted();
			} catch (JepException e) {
				logger.debug("filter on selected rows cannot be apply."); //$NON-NLS-1$
				logger.debug(e.getMessage());
			}
			update();
		}
	}

	/**
	 * This method inits or reallocate this array containing a list of index.
	 */
	private void initBigArraySorted() {
		bigArraySorted = new float[nRows];
		bigArraySorted = getSortedIndex(sortedColumnIndex);
	}

	/**
	 * This function instantiates mask on selected id. For example, in a Table,
	 * user wants to remove selected rows for a columnfile. He wants to remove
	 * spots with these id : rows=[0, 1, 2, 9, 10, 11] and id name is spot3d_id.
	 * Thanks jon for your python code!
	 * 
	 * @param rows
	 *            a table of id.
	 * @param id
	 *            ColumnFile id name (spot3d_id for example)
	 */
	private void makeMaskForSelectedRows(Object[] rows, String id) {

		try {
			// Converts id into integer
			fableJep.eval("myid = column_object." + id //$NON-NLS-1$
					+ ".copy().astype(numpy.int32)"); //$NON-NLS-1$
			// init mask with zero values
			fableJep.eval("mask = numpy.zeros(myid.shape, numpy.int32)"); //$NON-NLS-1$
			// logger.debug(fableJep.getValue("print type(mask), \"mask.dtype\""));
			// logger.debug(fableJep.getValue("print type(myid),  \"myid.dtype \""));
		} catch (JepException e1) {
			logger.debug("mask can not be instantiate."); //$NON-NLS-1$
			logger.debug(e1.getMessage());
		}
		for (int i = 0; i < rows.length; i++) {
			try {
				fableJep.eval("numpy.add(mask, myid == " + rows[i] + ", mask)"); //$NON-NLS-1$ //$NON-NLS-2$

			} catch (JepException e) {
				logger.debug("mask can not be build."); //$NON-NLS-1$
				logger.debug(e.getMessage());
			}
		}
		// convert mask to a logical type
		try {
			fableJep.eval("mask = (mask != 0)"); //$NON-NLS-1$
		} catch (JepException e) {
			logger.debug("mask can not be converted into a logical type."); //$NON-NLS-1$
			logger.debug(e.getMessage());
		}
	}

	/**
	 * keep all dots selected in the zone and remove others
	 * 
	 * @param key1min
	 *            min value for key1
	 * @param key1max
	 *            max value for key1
	 * @param key2min
	 *            min value for key2
	 * @param key2max
	 *            max value for key2
	 * @throws JepException
	 */
	public void keepDots(String key1, double key1min, double key1max,
			String key2, double key2min, double key2max) throws JepException {
		if (fableJep != null) {
			fableJep.set("key1min", key1min); //$NON-NLS-1$
			fableJep.set("key1max", key1max); //$NON-NLS-1$
			fableJep.set("key2min", key2min); //$NON-NLS-1$
			fableJep.set("key2max", key2max); //$NON-NLS-1$
			fableJep.eval("mask = (column_object." + key1 + " >= key1min) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key1 + "<= key1max) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key2 + " >= key2min) & " //$NON-NLS-1$ //$NON-NLS-2$
					+ "(column_object." + key2 + "<= key2max)"); //$NON-NLS-1$ //$NON-NLS-2$
			fableJep.eval("column_object.filter(mask)"); //$NON-NLS-1$

			loadRows();
			initBigArraySorted();
			update();
		}
	}

	/**
	 * This method is called from transformer to display last computation i.e.
	 * tth and eta
	 * 
	 * @param xyLabel
	 */
	public void displayComputedData(String[] xyLabel) {
		firePlotData(xyLabel);
	}

	/**
	 * 
	 * @param xyLabel
	 *            a table with two String : {"xLabel", and "yLabel"} which are
	 *            data to plot.
	 */
	private void firePlotData(String[] xyLabel) {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"PlotData", null, xyLabel)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * This methods sends an event to all ColumnFile listeners, and espacially
	 * views, to remove all dots in plot.
	 */
	public void fireRemoveAll() {
		for (Iterator<IPropertyChangeListener> it = listeners.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"removeAll", null, null)); //$NON-NLS-1$
			}
		}
	}

	public float[] getData() {
		return table_data;

	}

	/*
	 * public Row[] getRows() {
	 * 
	 * return rows; }
	 */
	public void setSortedIndex(int index, int dir) {
		sortedColumnIndex = index;
		sortedDirection = dir;
		initBigArraySorted();
	}

	/**
	 * This method return a row at index
	 * 
	 * @param index
	 * @return
	 */
	public float[] getRowAt(int index) {
		float[] f = null;
		try {

			fableJep.set("col", bigArraySorted[index]); //$NON-NLS-1$

		} catch (JepException e) {
			logger.debug("getRowAt/bigArraySorted" + e.getMessage());
		}
		try {

			f = (float[]) fableJep.getValue_floatarray("column_object." + //$NON-NLS-1$
					"bigarray[:,col].astype(numpy.float32).tostring()"); //$NON-NLS-1$

		} catch (JepException e) {
			logger.debug("getRowAt/getValue_floatarray" + e.getMessage());
		}

		return f;
	}
}
/**/
