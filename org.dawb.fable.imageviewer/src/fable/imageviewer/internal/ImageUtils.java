/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.internal;

import java.util.Vector;

import jep.JepException;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FableJep;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;

import fable.framework.logging.FableLogger;
import fable.framework.navigator.controller.SampleController;
import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.editor.ImageEditor;
import fable.imageviewer.views.ImageView;
import fable.python.Sample;

/**
 * ImageUtils is a class of utilities used mainly by the ImageView class. It
 * consists of static methods for doing a number of useful image related jobs.
 * 
 * @author Andy Gotz
 * 
 */
public class ImageUtils {

	/**
	 * Make and display a 2d image by taking a 1d slice across all images
	 * currently selected in the sample navigator.
	 * 
	 * This action runs as a job because it can take a lot time to read all the
	 * images.
	 * 
	 * @param y1
	 *            - line start x
	 * @param z1
	 *            - line start y
	 * @param y2
	 *            - line end x
	 * @param z2
	 *            - line end y
	 * @param width
	 *            - line width
	 */
	public static void Slice2DLine(int _y1, int _z1, int _y2, int _z2,
			int _width) {
		final int y1 = _y1, z1 = _z1, y2 = _y2, z2 = _z2, width = _width;
		Job job = new Job("Make 1D Slice") {
			protected IStatus run(IProgressMonitor monitor) {
				final Sample sample = SampleController.getController()
						.getCurrentsample();
				Vector<FabioFile> fabioFiles = sample.getFilteredfiles();
				Vector<Integer> selectedFiles = SampleNavigatorView.view
						.getSelectedFilesIndex();
				monitor.beginTask("Make 1D Slice", selectedFiles.size());
				FableJep fableJep;
				try {
					fableJep = FableJep.getFableJep();
					final int imageWidth = selectedFiles.size();
					final int imageHeight;
					int lineZLength = Math.abs(z2 - z1);
					int lineYLength = Math.abs(y2 - y1);
					if (lineZLength > lineYLength) {
						imageHeight = lineZLength;
					} else {
						imageHeight = lineYLength;
					}
					float sliceImage[] = new float[imageWidth * imageHeight];
					for (int i = 0; i < selectedFiles.size(); i++) {
						float line[];
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						try {
							line = SelectLine(fabioFiles.elementAt(
									selectedFiles.elementAt(i))
									.getImageAsFloat(fableJep), fabioFiles
									.elementAt(selectedFiles.elementAt(i))
									.getWidth(), fabioFiles.elementAt(
									selectedFiles.elementAt(i)).getHeight(),
									y1, z1, y2, z2, width);
							for (int j = 0; j < imageHeight; j++) {
								sliceImage[j * imageWidth + i] = line[j];
							}
						} catch (JepException ex) {
							FableUtils.excNoTraceMsg(this, "Error using Jep",
									ex);
						}
						monitor.worked(1);
					}
					fableJep.close();
					final float[] _sliceImage = sliceImage;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								ImageView slice2DImageView;
								slice2DImageView = (ImageView) PlatformUI
										.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage().showView(ImageView.ID,
												ImageComponent.SECONDARY_ID_SLICE2D,
												IWorkbenchPage.VIEW_ACTIVATE);
								slice2DImageView.getImage().changeImageRect(
										new Rectangle(0, 0, imageWidth,
												imageHeight), _sliceImage,
										sample.getDirectoryName(), null);
								slice2DImageView.setPartName(slice2DImageView
										.getSecondaryId()
										+ " " + sample.getDirectoryName());
							} catch (PartInitException ex) {
								FableUtils.excMsg(this,
										"Error opening Slice2DImageView", ex);
							}
						}
					});
				} catch (Throwable ex) {
					FableUtils.excNoTraceMsg(this, "Error using Jep", ex);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Make and display a 2d image by taking a 2d slice across all images
	 * currently selected in sample navigator.
	 * 
	 * This action runs as a job because it can take a lot time to read all the
	 * images.
	 * 
	 * @param _y1
	 *            - area to slice starts at [y1,z1]
	 * @param _z1
	 *            - area to slice starts at [y1,z1]
	 * @param _y2
	 *            - area to slice ends at [y2,z2]
	 * @param _z2
	 *            - area to slice ends at [y2,z2]
	 */
	public static void Slice2DArea(int _y1, int _z1, int _y2, int _z2) {
		final int y1 = _y1, z1 = _z1, y2 = _y2, z2 = _z2;
		Job job = new Job("Make 2D Slice of Selected Area") {
			protected IStatus run(IProgressMonitor monitor) {
				final Sample sample = SampleController.getController()
						.getCurrentsample();
				Vector<FabioFile> fabioFiles = sample.getFilteredfiles();
				Vector<Integer> selectedFiles = SampleNavigatorView.view
						.getSelectedFilesIndex();
				monitor.beginTask("Read " + selectedFiles.size()
						+ " files and select area [" + y1 + "," + z1 + "] to ["
						+ y2 + "," + z2 + "] ...", selectedFiles.size());
				FableJep fableJep;
				Logger logger = FableLogger
						.getLogger((Class<?>) ImageUtils.class);
				try {
					fableJep = FableJep.getFableJep();
					int selectedWidth, selectedHeight;
					selectedWidth = Math.abs(y2 - y1);
					selectedHeight = Math.abs(z2 - z1);
					logger.debug("selected width " + selectedWidth + " height "
							+ selectedHeight);
					final int imageWidth = selectedFiles.size() * selectedWidth;
					final int imageHeight = selectedHeight;
					float sliceImage[] = new float[imageWidth * imageHeight];
					logger.debug("image width " + imageWidth + " height "
							+ imageHeight);
					for (int i = 0; i < selectedFiles.size(); i++) {
						float area[];
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						try {
							monitor.subTask("Reading file "
									+ i
									+ " "
									+ fabioFiles.elementAt(
											selectedFiles.elementAt(i))
											.getFileName() + " ...");
							area = SelectArea(fabioFiles.elementAt(
									selectedFiles.elementAt(i))
									.getImageAsFloat(fableJep), fabioFiles
									.elementAt(selectedFiles.elementAt(i))
									.getWidth(), fabioFiles.elementAt(
									selectedFiles.elementAt(i)).getHeight(),
									y1, z1, y2, z2);
							for (int j = 0; j < selectedWidth; j++) {
								for (int k = 0; k < selectedHeight; k++) {
									float intensity;
									intensity = area[k * selectedWidth + j];
									sliceImage[k * imageWidth + i
											* selectedWidth + j] = intensity;
								}
							}
						} catch (JepException ex) {
							FableUtils.excNoTraceMsg(this, "Error using Jep",
									ex);
						}
						monitor.worked(1);
					}
					fableJep.close();
					final float[] _sliceImage = sliceImage;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								ImageView slice2DImageView;
								slice2DImageView = (ImageView) PlatformUI
										.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage().showView(ImageView.ID,
												ImageComponent.SECONDARY_ID_SLICE2D,
												IWorkbenchPage.VIEW_ACTIVATE);
								slice2DImageView.getImage().changeImageRect(
										new Rectangle(0, 0, imageWidth,
												imageHeight), _sliceImage,
										sample.getDirectoryName(), null);
								slice2DImageView.setPartName(slice2DImageView
										.getSecondaryId()
										+ " " + sample.getDirectoryName());
							} catch (PartInitException ex) {
								FableUtils.excMsg(this,
										"Error opening Slice2DImageView", ex);
							}
						}
					});
				} catch (Throwable ex) {
					FableUtils.excNoTraceMsg(this, "Error using Jep", ex);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Select an area over the requested range from the image array with the
	 * given dimensions. Returns an array with the pixel intensities of the
	 * corresponding area in the image.
	 * 
	 * @param imageAsFloat
	 *            - image data
	 * @param imageWidth
	 *            - image width
	 * @param imageHeight
	 *            - image height
	 * @param y1
	 *            - area begins at [y1,z1]
	 * @param z1
	 *            - area begins at [y1,z1]
	 * @param y2
	 *            - area ends at [y2,z2]
	 * @param z2
	 *            - area ends at [y2,z2]
	 * @return - float array containing the intensities of the pixels in the
	 *         selected area
	 */
	private static float[] SelectArea(float[] imageAsFloat, int imageWidth,
			int imageHeight, int y1, int z1, int y2, int z2) {
		float[] zoomAreaAsFloat = null;
		if (y1 > y2) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}
		if (z1 > z2) {
			int temp = z1;
			z1 = z2;
			z2 = temp;
		}
		if (y1 < 0)
			y1 = 0;
		if (y1 >= imageWidth)
			y1 = imageWidth - 1;
		if (y2 <= y1)
			y2 = y1 + 1;
		if (y2 >= imageWidth)
			y2 = imageWidth - 1;
		if (z1 < 0)
			z1 = 0;
		if (z1 >= imageHeight)
			z1 = imageHeight - 1;
		if (z2 <= z1)
			z2 = z1 + 1;
		if (z2 >= imageHeight)
			z2 = imageHeight - 1;
		int zoomWidth = y2 - y1;
		int zoomHeight = z2 - z1;
		zoomAreaAsFloat = new float[zoomWidth * zoomHeight];
		float areaMinimum = Float.MAX_VALUE;
		float areaMaximum = Float.MIN_VALUE;
		// float areaMean = 0.0f;
		float areaSum = 0.0f;
		for (int i = 0; i < zoomWidth; i++) {
			for (int j = 0; j < zoomHeight; j++) {
				zoomAreaAsFloat[i + j * zoomWidth] = imageAsFloat[y1 + i
						+ (z1 + j) * imageWidth];
				if (zoomAreaAsFloat[i + j * zoomWidth] < areaMinimum)
					areaMinimum = zoomAreaAsFloat[i + j * zoomWidth];
				if (zoomAreaAsFloat[i + j * zoomWidth] > areaMaximum)
					areaMaximum = zoomAreaAsFloat[i + j * zoomWidth];
				areaSum += zoomAreaAsFloat[i + j * zoomWidth];
			}
			// areaMean = areaSum / (zoomWidth * zoomHeight);
		}
		return zoomAreaAsFloat;
	}

	/**
	 * Select a line over the requested range from the image array with the
	 * given dimensions. Returns an array with the intensities averaged along
	 * the longest axis.
	 * 
	 * @param imageAsFloat
	 *            - image data
	 * @param imageWidth
	 *            - image width
	 * @param imageHeight
	 *            - image height
	 * @param y1
	 *            - line begins at [y1,z1]
	 * @param z1
	 *            - line begins at [y1,z1]
	 * @param y2
	 *            - line ends at [y2,z2]
	 * @param z2
	 *            - line ends at [y2,z2]
	 * @param lineWidth
	 *            - line width to average over (in pixels)
	 * @return - float array containing the intensities averaged along the line
	 */
	private static float[] SelectLine(float[] imageAsFloat, int imageWidth,
			int imageHeight, int y1, int z1, int y2, int z2, int lineWidth) {
		float line[];
		if (Math.abs(y2 - y1) > Math.abs(z2 - z1)) {
			if (y1 > y2) {
				// swap y1,y2 and z1,z2
				int temp = y1;
				y1 = y2;
				y2 = temp;
				temp = z1;
				z1 = z2;
				z2 = temp;
			}
		} else {
			if (z1 > z2) {
				// swap y1,y2 and z1,z2
				int temp = y1;
				y1 = y2;
				y2 = temp;
				temp = z1;
				z1 = z2;
				z2 = temp;
			}
		}
		if (y1 < 0)
			y1 = 0;
		if (y2 < 0)
			y2 = 0;
		if (y1 >= imageWidth)
			y1 = imageWidth - 1;
		if (y2 >= imageWidth)
			y2 = imageWidth - 1;
		if (z1 < 0)
			z1 = 0;
		if (z2 < 0)
			z2 = 0;
		if (z1 >= imageHeight)
			z1 = imageHeight - 1;
		if (z2 >= imageHeight)
			z2 = imageHeight - 1;
		float slope = 0;
		if ((y2 - y1) > (z2 - z1)) {
			line = new float[y2 - y1];
			if ((y2 - y1) != 0)
				slope = (float) (z2 - z1) / (float) (y2 - y1);
			for (int i = 0; i < (y2 - y1); i++) {
				// KE: Note that allocating inside a loop is inefficient since
				// it drives the GC crazy
				int x, y;
				y = (int) ((float) z1 + (slope * (float) (i)));
				x = y1 + i;
				/* integrate over linePeakWidth pixels along the line */
				int jMin = y - (int) (lineWidth / 2.);
				if (jMin < 0)
					jMin = 0;
				int jMax = y + (int) (lineWidth / 2.) + 1;
				if (jMax > imageHeight)
					jMax = imageHeight;
				float jWidth = jMax - jMin;
				for (int j = jMin; j < jMax; j++) {
					line[i] += imageAsFloat[x + (j) * imageWidth];
				}

				line[i] = line[i] / (jWidth);
			}
		} else {
			line = new float[z2 - z1];
			if ((z2 - z1) != 0)
				slope = (float) (y2 - y1) / (float) (z2 - z1);
			for (int i = 0; i < (z2 - z1); i++) {
				int x, y;
				/*
				 * x = (int)((float)y1 + (slope(float)(i))); y = z1+i;
				 */
				y = (int) ((float) y1 + (slope * (float) (i)));
				x = z1 + i;
				/* integrate over linePeakWidth pixels along the line */
				int jMin = y - (int) (lineWidth / 2.);
				if (jMin < 0)
					jMin = 0;
				int jMax = y + (int) (lineWidth / 2.) + 1;
				if (jMax > imageWidth)
					jMax = imageWidth;
				float jWidth = jMax - jMin;
				for (int j = jMin; j < jMax; j++) {
					line[i] += imageAsFloat[j + (x) * imageWidth];
				}
				line[i] = line[i] / (jWidth);
			}

		}
		return line;
	}
	
	
	
	public static ImageComponent getComponentFromPartSelected() {
		
		final IEditorPart sel = EclipseUtils.getActiveEditor();
		if (sel!=null && sel instanceof ImageEditor) {
			return ((ImageEditor)sel).getImageComponent();
		}
		
		final ImageView iv = (ImageView) PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getActivePage()
										.findViewReference(ImageView.ID,
												ImageComponent.SECONDARY_ID_SLICE2D).getView(
												true);
		if (iv!=null) return iv.getImageComponent();
		
		return null;
	}

}
