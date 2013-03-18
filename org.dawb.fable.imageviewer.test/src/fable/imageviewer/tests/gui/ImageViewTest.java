/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.tests.gui;

import static org.junit.Assert.assertNotNull;

import org.dawb.common.util.test.TestUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.views.ImageView;

/**
 * Unit tests for ImageViewer
 * 
 * @author Andy Gotz
 *
 */
public class ImageViewTest {

	IWorkbenchPage page;
	private ImageView imageView;
	
	@Before  
	public void runBefore() throws Exception {
		page      = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    imageView = (ImageView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ImageView.ID,"0" ,IWorkbenchPage.VIEW_ACTIVATE);
	}
	
	/**
	 * Test the ImageView is open
	 */
	@Test
	public void testOpenImageViewer() {
		assertNotNull(imageView);
	}
	
	/**
	 * Test selecting the zoom
	 */
	@Test
	public void testZoomSelect() {
		imageView.setZoomSelection(ZoomSelection.AREA);
		imageView.setZoomSelection(ZoomSelection.LINE);
		imageView.setZoomSelection(ZoomSelection.PROFILE);
		imageView.setZoomSelection(ZoomSelection.RELIEF);
		imageView.setZoomSelection(ZoomSelection.ROCKINGCURVE);
	}

	/**
	 * Test opening an image
	 */
	//@Test
	public void testOpenImage() throws Throwable{
		
		
		String fileName = TestUtils.getAbsolutePath(fable.imageviewer.rcp.Activator.getDefault().getBundle());
		fileName = fileName.substring(0, fileName.length()-1)+".test/src/fable/imageviewer/tests/gui/Cr8F8140k103.0026.bz2"; 
		imageView.getImageComponent().loadFile(fileName);
		imageView.getImageComponent().image.displayImage();
	}
		
}
