/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.explorerProvider;

import java.io.File;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import fable.framework.navigator.Activator;
import fable.framework.navigator.toolBox.IImagesKeys;

/**
 * @author Suchet
 * @date March 28, 2007
 * @description This class provides the labels for the tree in FileTree
 * */
public class FileTreeLabelProvider implements ILabelProvider {

	private Image _file, _dir, _esrf, _sample;
	private ImageDescriptor _ImageDescDir = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_DIR);
	private ImageDescriptor _ImageDescFile = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.IMG_FILE);
	private ImageDescriptor _ImageEDF = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.IMG_ESRF);
	private ImageDescriptor _ImageSample = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.IMG_SAMPLE);

	private Vector<ILabelProviderListener> _listeners;

	/**
	 * 
	 */
	public FileTreeLabelProvider() {
		// Create images
		_listeners = new Vector<ILabelProviderListener>();
		_file = _ImageDescFile.createImage();
		_dir = _ImageDescDir.createImage();
		_esrf = _ImageEDF.createImage();
		_sample = _ImageSample.createImage();
	}

	public Image getImage(Object element) {
		Image img = _file;
		File f = (File) element;
		if (f.getName().endsWith(".edf")) {
			img = _esrf;
		} else {

			if (f.isDirectory()) {

				String[] list = null;// ToolBox.getFilesFromDirectory((String)f.getPath(),
										// ".edf") ;
				if (list != null && list.length > 0) {
					img = _sample;
				} else {
					img = _dir;
				}
			} else {
				img = _file;
			}

		}
		return img;
	}

	public String getText(Object element) {
		// Get the name of the file
		String text = ((File) element).getName();
		if (text == null || text.length() == 0) {
			text = ((File) element).getPath();
		}

		return text;
	}

	public void addListener(ILabelProviderListener listener) {
		_listeners.add(listener);

	}

	public void dispose() {
		if (_dir != null) {
			_dir.dispose();
		}
		if (_file != null) {
			_file.dispose();
		}
		if (_esrf != null) {
			_esrf.dispose();
		}
		if (_sample != null) {
			_sample.dispose();
		}

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		_listeners.remove(listener);

	}

}
