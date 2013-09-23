/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.fable.extensions;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import fable.imageviewer.model.IFableImage;

public class FableImageWrapper implements IFableImage {
	
	/**
	 * We make them final so that they can be 
	 * garbage collected quickly when this object
	 * goes out of scope.
	 */
	private final String  fileName;
	private final int     width, height;
	private final float[] image;
	private final long    loadTime;

	public FableImageWrapper(final String fileName, IDataset set, long time) {
		
		this.fileName = fileName;
		this.loadTime = time;
		final int[] shape = set.getShape();
		this.height   = shape[0];
		this.width    = shape[1];
		
		FloatDataset fSet = (FloatDataset)DatasetUtils.cast((AbstractDataset)set, AbstractDataset.FLOAT32);
		this.image = fSet.getData();
			
		set  = null;
		fSet = null;

	}

	public FableImageWrapper(final String fileName, 
			                 final AbstractDataset data,
			                 final List<AbstractDataset> axes,
			                 final long time) {
		
		this.fileName = fileName;
		this.loadTime = time;
		this.height   = axes.get(0).getSize();
		this.width    = axes.get(1).getSize();
		
		FloatDataset fSet = (FloatDataset)data.cast(AbstractDataset.FLOAT32);
		this.image = fSet.getData();
			
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public float[] getImage() {
		return image;
	}

	@Override
	public long getLoadTime() {
		return loadTime;
	}
}
