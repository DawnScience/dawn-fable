/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.model;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

import jep.JepException;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FableJep;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This class implements a simple image model that stores the the width, height,
 * and the pixel data. The data are stored as a float[index] with index = col +
 * row * width. It calculates the statistics (min, max, and mean) when requested
 * and then stores the values.
 * 
 * @author evans
 * 
 */
public class ImageModel {
	// Note: Change the ImageInfoAction if more fields are added
	private EventListenerList listenerList = null;
	private String fileName = null;;
	private int width = 0;
	private int height = 0;
	private float[] data = null;
	private float[] statistics = null;
	private long time;

	// Property change names
	/**
	 * Denotes that the data and statistics changed but not the other
	 * parameters.
	 */
	public static final String DATA_CHANGED = ImageModel.class.getName()
			+ ".DataChanged";
	/**
	 * Denotes the data and parameters changed.
	 */
	public static final String RESET = ImageModel.class.getName() + ".Reset";

	/**
	 * Empty constructor. Sets the listenerList.
	 */
	ImageModel() {
		listenerList = new EventListenerList();
	}

	/**
	 * Constructor that sets the model based on the given FabioFile. Calls
	 * reset(fabioFile). Note that any events fired will have no listeners, yet.
	 * If you need to be informed of events, create an ImageModel, add the
	 * listeners, and use reset instead of constructing a new ImageModel.
	 * 
	 * @param fabioFile
	 * @throws JepException
	 */
	ImageModel(FabioFile fabioFile) throws Throwable {
		this();
		set(fabioFile);
	}

	/**
	 * Constructor that sets the model based on the given parameters. Calls
	 * reset(fileName, width, height, data). Note that any events fired will
	 * have no listeners, yet. If you need to be informed of events, create an
	 * ImageModel, add the listeners, and use reset instead of constructing a
	 * new ImageModel.
	 * 
	 * @param fileName
	 * @param width
	 * @param height
	 * @param data
	 * @param time
	 */
	ImageModel(String fileName, int width, int height, float[] data, long time) {
		this();
		reset(fileName, width, height, data);
		this.time = time;
	}

	/**
	 * Adds the listener.
	 * 
	 * @param l
	 */
	public void addImageModelListener(ImageModelListener l) {
		listenerList.add(ImageModelListener.class, l);
	}

	/**
	 * Removes the listener.
	 * 
	 * @param l
	 */
	public void removeImageModelListener(ImageModelListener l) {
		listenerList.remove(ImageModelListener.class, l);
	}

	/**
	 * Removes all the listeners.
	 * 
	 * @param l
	 */
	public void removeAllImageModelListeners(ImageModelListener l) {
		EventListener[] listeners = listenerList
				.getListeners(ImageModelListener.class);
		for (EventListener listener : listeners) {
			listenerList.remove(ImageModelListener.class,
					(ImageModelListener) listener);
		}
	}

	/**
	 * Fires an ImageModelEvent with the given parameters.
	 * 
	 * @param name
	 *            Should be one of the ImageModel.xxx_CHANGED names.
	 * @param oldValue
	 * @param newValue
	 */
	protected void fireImageModelEvent(String name, Object oldValue,
			Object newValue) {
		EventListener[] listeners = listenerList
				.getListeners(ImageModelListener.class);
		for (EventListener listener : listeners) {
			ImageModelEvent imageModelEvent = new ImageModelEvent(this, name,
					oldValue, newValue);
			((ImageModelListener) listener).propertyChange(imageModelEvent);
		}
	}

	/**
	 * Resets the model based on the given FabioFile;
	 * 
	 * @param fabioFile
	 * @throws JepException
	 */
	public void set(FabioFile fabioFile) throws Throwable {
		try {
			statistics = null;
			this.fileName = fabioFile.getFileName();
			this.data     = fabioFile.getImageAsFloat(FableJep.getFableJep());
			this.width    = fabioFile.getWidth();
			this.height   = fabioFile.getHeight();
			this.time     = fabioFile.getTimeToReadImage();
		} finally {
			fireImageModelEvent(RESET, this, this);
		}
	}

	/**
	 * Resets the model based on the given parameters. Will cause a RESET
	 * ImageModelEvent but not a DATA_CHANGED event to be fired.
	 * 
	 * @param fileName
	 * @param width
	 * @param height
	 * @param mean
	 * @param data
	 */
	public void reset(String fileName, int width, int height, float[] data) {
		statistics = null;
		this.fileName = fileName;
		this.width = width;
		this.height = height;
		this.data = data;
		fireImageModelEvent(RESET, this, this);
	}

	/**
	 * Calculates the statistics (min, max, mean) for the whole image and stores
	 * it.
	 */
	private void calculateStatistics() {
		statistics = getStatistics(new Rectangle(0,0,width,height));
	}

	// Getters and setters

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	public Rectangle getRect() {
		return new Rectangle(0, 0, width, height);
	}

	/**
	 * Get the statistics (min, max, mean) for the whole image. The values are
	 * cached after the first time they are calculated.
	 * 
	 * @return The statistics as float[3] = {min, max, mean}.
	 */
	public float[] getStatistics() {
		if (statistics == null) {
			calculateStatistics();
		}
		return statistics;
	}

	/**
	 * Get the statistics (min, max, mean) for a sub Rectangle. These are
	 * calculated each time this method is called.
	 * 
	 * @param rect
	 * @return The statistics as float[3] = {min, max, mean}.
	 */
	public float[] getStatistics(Rectangle rect) {
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float mean = 0.0f;
		float sum = 0.0f;
		float val;
		for (int j = 0; j < rect.height; j++) {
			for (int i = 0; i < rect.width; i++) {
				val = data[rect.x + i + (rect.y + j) * width];
				sum += val;
				if (val < min) min = val;
				if (val > max) max = val;
			}
		}
		mean = sum / (rect.width * rect.height);
		return new float[] { min, max, mean };
	}

	/**
	 * @return the data
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * Return the value of the data corresponding to the given row and column of
	 * the stored data.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public float getData(int row, int col) {
		if (data == null) {
			return Float.NaN;
		}
		return data[col + row * width];
	}

	/**
	 * return the value of the data corresponding to given row and column of the
	 * given Rectangle.
	 * 
	 * @param row
	 * @param col
	 * @param rect
	 * @return
	 */
	public float getData(int row, int col, Rectangle rect) {
		if (data == null) {
			return Float.NaN;
		}
		int index1 = col + row * rect.width;
		int col1 = index1 % width;
		int row1 = index1 / width;
		return data[col1 + row1 * width];
	}

	/**
	 * Returns a sub array of the data corresponding to the given Rectangle.
	 * 
	 * @param rect
	 * @return
	 */
	public float[] getData(Rectangle rect) {
		if (data == null) {
			return null;
		}
		float[] array = new float[rect.width * rect.height];
		for (int j = 0; j < rect.height; j++) {
			for (int i = 0; i < rect.width; i++) {
				array[i + j * rect.width] = data[rect.x + i + (rect.y + j)
						* width];
			}
		}
		return array;
	}

	/**
	 * Sets a new value for the data and cause a DATA_CHANGED ImageModelEvent to
	 * be fired.
	 * 
	 * @param data
	 *            the data to set
	 */
	public void setData(float[] data) {
		float[] oldValue = this.data;
		if (data != oldValue) {
			statistics = null;
			this.data = data;
			fireImageModelEvent(DATA_CHANGED, oldValue, data);
		}
	}

	public long getTimeToReadImage() {
		return time;
	}

}
