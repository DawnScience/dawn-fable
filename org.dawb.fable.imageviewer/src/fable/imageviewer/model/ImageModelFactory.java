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

import org.dawb.fabio.FabioFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import fable.framework.navigator.Activator;
import fable.framework.navigator.preferences.FabioPreferenceConstants;


/**
 * This class either uses a loader read from an extension point, or
 * it returns an ImageModel backed by a fabio file.
 * 
 * This allows non-fabio loaders to be used with fable.
 * 
 * @author gerring
 *
 */
public class ImageModelFactory {

	/**
	 * Gets ImageModel from path
	 * @param path
	 * @return
	 * @throws Throwable
	 */
	public static ImageModel getImageModel(final Object path)  throws Throwable {
		
		if (path instanceof FabioFile) return ImageModelFactory.getImageModel((FabioFile)path);
		return ImageModelFactory.getImageModel((String)path);
	}
	
	/**
	 * This extension point is implemented ouside fable code base.
	 */
	private static final String IFABLE_LOADER_ID = "fable.imageviewer.model.fableLoader";

	/**
	 * Gets ImageModel from path
	 * @param path
	 * @return
	 * @throws Throwable
	 */
	public static ImageModel getImageModel(String path)  throws Throwable {
		
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(IFABLE_LOADER_ID);
		if (config!=null && config.length>0) {
			final IFableLoader loader = (IFableLoader)config[0].createExecutableExtension("class");
			final boolean      isFabio= Activator.getDefault().getPreferenceStore().getBoolean(FabioPreferenceConstants.USE_FABIO);
			
			path = path.replace("%20", " ");
			final IFableImage  file   = loader.loadFile(path, null, isFabio, null);
			
			if (file!=null) {
				return new ImageModel(file.getFileName(),
						              file.getWidth(),
						              file.getHeight(),
						              file.getImage(),
						              file.getLoadTime());
			}
		}
		
		/**
		 * By default we use FableFile!
		 */
		return ImageModelFactory.getImageModel(new FabioFile(path));
	}

	/**
	 * Gets ImageModel directly from data.
	 * @param fileName
	 * @param width
	 * @param height
	 * @param imageDiffArray
	 * @return
	 */
	public static ImageModel getImageModel(String fileName, 
			                               int width,
			                               int height, 
			                               float[] imageDiffArray) {

		return new ImageModel(fileName,width,height,imageDiffArray,-1);
	}


    /**
     * Trying to isolate Fabio File here in case other loaders are
     * chosen by the user.
     * 
     * @param imageFile
     * @return
     * @throws Throwable
     */
	private static ImageModel getImageModel(FabioFile imageFile)throws Throwable {
		return new ImageModel(imageFile);
	}

}
