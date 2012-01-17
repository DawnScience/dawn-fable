/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.imageprint;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import fable.framework.toolbox.ImageSelection;
import fable.framework.toolbox.SWTUtils;
import fr.esrf.tangoatk.widget.util.chart.JLChart;

/**
 * A class to provide some actions for a JLChart. Note that if the associated
 * chart changes, then the current one must be set into this class.
 * 
 * @author evans
 * 
 */
public class JLChartActions {
	/**
	 * The chart to be printed.
	 */
	JLChart chart;
	Display display;

	public Action printSetupAction;
	public Action printPreviewAction;
	public Action printAction;
	public Action copyAction;

	/**
	 * Constructor.
	 * 
	 * @param display
	 *            The display associated with the chart.
	 * @param chart
	 *            The chart. Note that if the associated chart changes, then the
	 *            current one must be set into this class.
	 */
	public JLChartActions(Display display, JLChart chart) {
		this.chart = chart;
		this.display = display;

		// Print Setup
		printSetupAction = new Action("Print Setup") {
			@Override
			public void run() {
				Image image = getSWTChartImage();
				if (image == null)
					return;
				ImagePrintSetupDialog dialog = new ImagePrintSetupDialog(
						JLChartActions.this.display.getActiveShell(), image,
						FableImagePrinter.getSettings());
				PrintSettings settings = dialog.open();
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
				if (settings != null) {
					// Dialog was not canceled
					FableImagePrinter.setSettings(settings);
				}
			}
		};

		// Print Preview
		printPreviewAction = new Action("Print Preview") {
			@Override
			public void run() {
				Image image = getSWTChartImage();
				if (image == null) {
					return;
				}
				ImagePrintPreviewDialog dialog = new ImagePrintPreviewDialog(
						JLChartActions.this.display.getActiveShell(), image,
						FableImagePrinter.getSettings());
				PrintSettings settings = dialog.open();
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
				if (settings != null) {
					// Dialog was not canceled
					FableImagePrinter.setSettings(settings);
				}
			}
		};

		// Print
		printAction = new Action("Print") {
			@Override
			public void run() {
				Image image = getSWTChartImage();
				if (image == null) {
					return;
				}
				ImagePrintUtils.dialogPrintImage(JLChartActions.this.display
						.getActiveShell(), image, JLChartActions.this.display
						.getDPI(), FableImagePrinter.getSettings());
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
			}
		};

		// Copy
		copyAction = new Action("Copy") {
			@Override
			public void run() {
				if (true) {
					// Use AWT
					BufferedImage image = getAWTChartImage();
					if (image == null) {
						return;
					}
					java.awt.datatransfer.Clipboard awtClipboard = Toolkit
							.getDefaultToolkit().getSystemClipboard();
					awtClipboard.setContents(new ImageSelection(image), null);
					// } else {
					// // Use SWT
					// Image image = getSWTChartImage();
					// if (image == null) {
					// return;
					// }
					// ImageTransfer transfer = ImageTransfer.getInstance();
					// Clipboard clipboard = new Clipboard(
					// JLChartActions.this.display);
					// clipboard.setContents(
					// new Object[] { image.getImageData() },
					// new Transfer[] { transfer });
					// clipboard.dispose();
					// if (image != null && !image.isDisposed()) {
					// image.dispose();
					// }
				}
			}
		};
	}

	/**
	 * Makes a new AWT BufferedImage from the chart.
	 * 
	 * @return The BufferedImage.
	 */
	public BufferedImage getAWTChartImage() {
		if (display == null || display.isDisposed() || chart == null)
			return null;
		// Make a new image the size of the chart
		int w = chart.getSize().width;
		int h = chart.getSize().height;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		if (img == null)
			return null;
		// Paint it with the chart's paint method
		Color oldBackground = chart.getBackground();
		chart.setBackground(Color.WHITE);
		chart.paint(img.getGraphics());
		chart.setBackground(oldBackground);
		return img;
	}

	/**
	 * Makes an SWT Image from the chart.
	 * 
	 * @return The Image. It must be disposed after use.
	 */
	public Image getSWTChartImage() {
		if (display == null || display.isDisposed() || chart == null)
			return null;
		// Make a new image the size of the chart
		BufferedImage img = getAWTChartImage();
		if (img == null)
			return null;
		ImageData data = SWTUtils.convertToSWT(img);
		if (data == null)
			return null;
		return new Image(display, data);
	}

	/**
	 * @return the chart
	 */
	public JLChart getChart() {
		return chart;
	}

	/**
	 * @param chart
	 *            the chart to set
	 */
	public void setChart(JLChart chart) {
		this.chart = chart;
	}

}
