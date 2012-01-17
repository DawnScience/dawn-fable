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

import org.eclipse.core.runtime.IProgressMonitor;

public interface IFableLoader {

	/**
	 * Please implement this method to return the IFableImage.
	 * NOTE monitor can be null as can name.
	 * 
	 * Should throw and exception if there is a genuine error or
	 * return null if the file simply cannot be loaded with this
	 * loader.
	 * 
	 * @param path
	 * @param name optional parameter which says the image required
	 *        if the file contains more than one image.
	 * @param monitor
	 * @return
	 */
	public IFableImage loadFile(final String path, 
			                    final String name,
			                    final boolean isFabioConfigured,
			                    final IProgressMonitor monitor) throws Exception;

}
