
/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package fable.imageviewer.psf;

import java.io.*;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * Internal class that separates ImageData from ImageLoader
 * to allow removal of ImageLoader from the toolkit.
 */
class ImageDataLoaderUpscale {

	public static ImageDataUpscale[] load(InputStream stream) {
		ImageDataUpscale[] result;
		ImageData[] images = new ImageLoader().load(stream);
		result = new ImageDataUpscale[ images.length ];
		for( int i = 0; i < images.length; i++ )
			result[ i ] = new ImageDataUpscale( images[ i ] );
		return result;
	}

	public static ImageDataUpscale[] load(String filename) {
		ImageDataUpscale[] result;
		ImageData[] images = new ImageLoader().load(filename); 
		result = new ImageDataUpscale[ images.length ];
		for( int i = 0; i < images.length; i++ )
			result[ i ] = new ImageDataUpscale( images[ i ] );
		return result;
	}

}
