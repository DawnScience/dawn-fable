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

public interface IFableImage {

	/**
	 * The name not the full path of the file.
	 * @return
	 */
	public String getFileName();

	/**
	 * The first dimension of the image
	 * @return
	 */
	public int getWidth();

	/**
	 * The secpnd dimension of the image
	 * @return
	 */
	public int getHeight();

	/**
	 * The image data.
	 * @return
	 */
	public float[] getImage();

	/**
	 * Time to load image, or -1 if not calculated.
	 * @return
	 */
	public long getLoadTime();

}
