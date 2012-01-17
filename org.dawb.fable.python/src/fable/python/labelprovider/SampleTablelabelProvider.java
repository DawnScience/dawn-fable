/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.labelprovider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import fable.framework.toolbox.Activator;
import fable.framework.toolbox.IImagesKeys;
import fable.python.Sample;

public class SampleTablelabelProvider implements ITableLabelProvider {
	private Image sampleImage;

	public Image getColumnImage(Object element, int columnIndex) {
		ImageDescriptor descriptor = null;
		ImageDescriptor sampleNotvalideDescriptor = null;
		if (element instanceof Sample) {
			Sample mySample = (Sample) element;

			if (mySample.isDirectory()) {
				descriptor = Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, IImagesKeys.IMG_FOLDER);
				sampleNotvalideDescriptor = Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								IImagesKeys.IMG_LABEL_SAMPLE_UNVALIDE);

			} else {
				descriptor = Activator.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, IImagesKeys.IMG_FILES);
				sampleNotvalideDescriptor = Activator
						.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
								IImagesKeys.IMG_LABEL_SAMPLE_UNVALIDE);

			}
			// obtain the cached image corresponding to the descriptor
			if (((Sample) element).isValide()) {
				sampleImage = descriptor.createImage();
			} else {
				sampleImage = sampleNotvalideDescriptor.createImage();
			}

			return sampleImage;
		} else {
			return null;
		}
	}

	public String getColumnText(Object element, int columnIndex) {
		String text = "";
		if (columnIndex == 0) {
			text = ((Sample) element).getDirectoryName();
		}
		return text;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		if (sampleImage != null) {
			sampleImage.dispose();
		}

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
