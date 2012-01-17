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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import fable.framework.imageprint.PrintSettings.Orientation;
import fable.framework.toolbox.SWTUtils;

public class ImagePrintUtils {
	private static final boolean debug = false;

	/**
	 * Brings up a PrintDialog to print the given Control with the default print
	 * settings. Calls printControl(control, null).
	 * 
	 * @param control
	 *            The control to print.
	 * @return
	 */
	public static boolean printControl(Control control) {
		return printControl(control, null);
	}

	/**
	 * Brings up a PrintDialog to print the given Control.
	 * 
	 * @param control
	 *            The control to print.
	 * @param settings
	 *            The desired PrinterSettings or null to use the default.
	 * @return If the operation was canceled or not.
	 */
	public static boolean printControl(Control control, PrintSettings settings) {
		// Get the Image
		if (control == null)
			return false;
		if (settings == null) {
			settings = new PrintSettings();
		}
		Point size = control.getSize();
		Image image = new Image(control.getDisplay(), size.x, size.y);
		if (image == null)
			return false;
		GC gc1 = new GC(control);
		gc1.copyArea(image, 0, 0);
		gc1.dispose();

		boolean res = dialogPrintImage(control.getShell(), image, control
				.getDisplay().getDPI(), settings);

		// We create it, we dispose it
		if (image != null && !image.isDisposed())
			image.dispose();
		return res;
	}

	/**
	 * Prints an image, first bringing up a PrintDialog. Works with a copy of
	 * the image and disposes the copy when done.
	 * 
	 * @param shell
	 *            The Shell parent for the dialog.
	 * @param image
	 *            The image. Cannot be null.
	 * @param imageDPI
	 *            The image DPI, typically the the Display DPI. Use (72, 72) if
	 *            you don't know what else to use.
	 * @param settings
	 *            The desired PrinterSettings or null to use the default.
	 * @return If the operation was canceled or not.
	 */
	public static boolean dialogPrintImage(Shell shell, Image image,
			Point imageDPI, PrintSettings settings) {
		if (image == null || image.isDisposed())
			return false;
		if (settings == null) {
			settings = new PrintSettings();
		}
		// Make a copy of the image so we can control its disposal and not worry
		// about whether it is changed under us
		Image image1 = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
		// Start a print dialog
		PrintDialog dialog = new PrintDialog(shell, SWT.NONE);
		PrinterData printerData = settings.getPrinterData();
		if (printerData != null) {
			dialog.setPrinterData(printerData);
		}
		PrinterData newPrinterData = dialog.open();
		if (newPrinterData == null) {
			// User canceled
			if (image1 != null && !image1.isDisposed())
				image1.dispose();
			return false;
		}

		// Ask for a file if printToFile was selected
		if (newPrinterData.printToFile) {
			FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
			String file = fileDialog.open();
			if (file != null) {
				newPrinterData.fileName = file;
			} else {
				if (image1 != null && !image1.isDisposed())
					image1.dispose();
				return false;
			}
		}

		// Get a printer
		Printer printer = new Printer(newPrinterData);
		if (printer == null)
			return false;

		// We are committed, set the printerData in the settings
		settings.setPrinterData(newPrinterData);
		printImage(image1, shell.getDisplay().getDPI(), settings);
		if (!printer.isDisposed())
			printer.dispose();
		return true;
	}

	/**
	 * Prints an image without bringing up a PrintDialog. Runs a print job in a
	 * separate thread. Works with a copy of the image and disposes the copy
	 * when done.
	 * 
	 * @param image
	 *            The image. Cannot be null.
	 * @param imageDPI
	 *            The image DPI, typically the the Display DPI. Use (72, 72) if
	 *            you don't know what else to use.
	 * @param settings
	 *            The desired PrinterSettings or null to use the default.
	 */
	public static void printImage(final Image image, final Point imageDPI,
			PrintSettings settings) {
		if (image == null || image.isDisposed())
			return;
		final PrintSettings settings1;
		if (settings == null) {
			settings1 = new PrintSettings();
		} else {
			settings1 = settings;
		}
		final Printer printer = new Printer(settings1.getPrinterData());
		if (printer == null)
			return;
		final Image image1 = new Image(image.getDevice(), image, SWT.IMAGE_COPY);

		Thread printThread = new Thread() {
			public void run() {
				if (!printer.startJob("JavaImagePrinting")) {
					SWTUtils.errMsgAsync("Failed to start print job!");
					if (!printer.isDisposed())
						printer.dispose();
					return;
				}
				drawImage(printer, printer.getDPI(), printer.getBounds(),
						image1, imageDPI, settings1);
				if (image1 != null && !image1.isDisposed())
					image1.dispose();
				printer.endPage();
				printer.endJob();
				if (!printer.isDisposed())
					printer.dispose();
				if (debug)
					System.out.println("Printing job done!");
			}
		};
		printThread.start();
	}

	/**
	 * A general method for drawing an image on a Drawable using parameters from
	 * a PrintSettings. The image will not be disposed.
	 * 
	 * @param drawable
	 *            Where to draw the image. Cannot be null.
	 * @param drawableDPI
	 *            The DPI of the Drawable. If null will use (72,72).
	 * @param bounds
	 *            The bounds of the area on the Drawable to use.
	 * @param image
	 *            The image. Cannot be null.
	 * @param imageDPI
	 *            The image DPI, typically the the Display DPI. If null will use
	 *            (72,72).
	 * @param settings
	 *            The desired PrinterSettings or null to use the default.
	 */
	public static void drawImage(Drawable drawable, Point drawableDPI,
			Rectangle bounds, Image image, Point imageDPI,
			PrintSettings settings) {
		if (drawable == null || image == null || image.isDisposed())
			return;
		if (settings == null) {
			settings = new PrintSettings();
		}
		if (drawableDPI == null) {
			drawableDPI = new Point(72, 72);
		}
		if (imageDPI == null) {
			imageDPI = new Point(72, 72);
		}

		// Calculate parameters
		int imageWidth = image.getBounds().width;
		int imageHeight = image.getBounds().height;
		double dpiScaleFactorX = drawableDPI.x * 1.0 / imageDPI.x;
		double dpiScaleFactorY = drawableDPI.y * 1.0 / imageDPI.y;
		double left = settings.getLeft() * drawableDPI.x;
		double right = settings.getRight() * drawableDPI.x;
		double top = settings.getTop() * drawableDPI.y;
		double bottom = settings.getBottom() * drawableDPI.y;
		int drawableWidth = bounds.width;
		int drawableHeight = bounds.height;
		Rectangle trim;
		if (drawable instanceof Printer) {
			trim = ((Printer) drawable).computeTrim(0, 0, 0, 0);
		} else {
			trim = new Rectangle(0, 0, 0, 0);
		}
		int leftMargin = (int) (left) + trim.x;
		int rightMargin = drawableWidth - (int) (right) + trim.x;
		int topMargin = (int) (top) + trim.y;
		int bottomMargin = drawableHeight - (int) (bottom) + trim.y;
		int availableWidth = rightMargin - leftMargin;
		int availableHeight = bottomMargin - topMargin;
		if (availableWidth <= 0) {
			SWTUtils.errMsgAsync("Horizontal margins are too large!");
			return;
		}
		if (availableHeight <= 0) {
			SWTUtils.errMsgAsync("Vertical margins are too large!");
			return;
		}

		// If the image is too large to draw on a page, reduce its
		// width and height proportionally.
		double imageSizeFactor = Math.min(1, (rightMargin - leftMargin) * 1.0
				/ (dpiScaleFactorX * imageWidth));
		imageSizeFactor = Math.min(imageSizeFactor, (bottomMargin - topMargin)
				* 1.0 / (dpiScaleFactorY * imageHeight));
		int drawnWidth = (int) (dpiScaleFactorX * imageSizeFactor * imageWidth);
		int drawnHeight = (int) (dpiScaleFactorX * imageSizeFactor * imageHeight);

		if (debug) {
			System.out.println("drawImage\n");
			System.out
					.println("dpi=" + imageDPI + " printerDPI=" + drawableDPI);
			System.out.println("dpiScaleFactorX=" + dpiScaleFactorX
					+ " dpiScaleFactorY=" + dpiScaleFactorY);
			System.out.println("printerWidth=" + drawableWidth
					+ " printerHeight=" + drawableHeight);
			System.out.println("drawnWidth=" + drawnWidth + " drawnHeight="
					+ drawnHeight);
			System.out.println("availableWidth=" + availableWidth
					+ " availableHeight=" + availableHeight);
			System.out.println("left=" + left + " right=" + right + " top="
					+ top + " bottom=" + bottom);
			System.out.println("leftMargin=" + leftMargin + " rightMargin="
					+ rightMargin + "topMargin=" + topMargin + " bottomMargin="
					+ bottomMargin);
			System.out.println("imageSizeFactor=" + imageSizeFactor);
		}

		// Handle FILL
		if (settings.getHorizontalAlign() == SWT.FILL) {
			drawnWidth = availableWidth;
		}
		if (settings.getVerticalAlign() == SWT.FILL) {
			drawnHeight = availableHeight;
		}

		// Align
		if (drawnWidth < availableWidth) {
			switch (settings.getHorizontalAlign()) {
			case SWT.LEFT:
				break;
			case SWT.CENTER:
				leftMargin += (availableWidth - drawnWidth) / 2;
				break;
			case SWT.RIGHT:
				leftMargin += (availableWidth - drawnWidth);
				break;
			}
		}
		if (drawnHeight < availableHeight) {
			switch (settings.getVerticalAlign()) {
			case SWT.TOP:
				break;
			case SWT.CENTER:
				topMargin += (availableHeight - drawnHeight) / 2;
				break;
			case SWT.BOTTOM:
				topMargin += (availableHeight - drawnHeight);
				break;
			}
		}

		// Draw the image to the drawable
		GC gc = new GC(drawable);
		gc.drawImage(image, 0, 0, imageWidth, imageHeight, bounds.x
				+ leftMargin, bounds.y + topMargin, drawnWidth, drawnHeight);
		gc.dispose();
	}

	/**
	 * A general method for painting a canvas with a print preview in response
	 * to a PaintEvent. This method draws the page area on the canvas, scaling
	 * it appropriately, then calls drawImage, which handles the drawing of the
	 * image on the page. The image will not be disposed.
	 * 
	 * @param ev
	 *            The paint event.
	 * @param canvas
	 *            The canvas.
	 * @param gc
	 *            The GC to use.
	 * @param printer
	 *            The desired Printer or null to use the default printer.
	 * @param image
	 *            The image. Cannot be null.
	 * @param settings
	 *            The desired PrinterSettings or null to use the default.
	 */
	public static void paintPreview(GC gc, Canvas canvas,
			Rectangle canvasBounds, Image image, PrintSettings settings) {
		// Handle paint events coming after things are disposed
		if (canvas == null || canvas.isDisposed() || image == null
				|| image.isDisposed() || settings == null
				|| canvasBounds == null) {
			return;
		}
		
		Printer printer = new Printer(settings.getPrinterData());
		Display display = canvas.getDisplay();
		Point displayDPI = display.getDPI();
		Point printerDPI = printer.getDPI();
		// The bounds are the size of the printer, not the printable area, which
		// comes from getClientArea
		Rectangle printerBounds = printer.getBounds();
		if (settings.getOrientation() == Orientation.LANDSCAPE) {
			printerBounds = new Rectangle(printerBounds.y, printerBounds.x,
					printerBounds.height, printerBounds.width);
			// TODO This may not be right, but presumably they are the same most
			// of
			// the time anyway
			printerDPI.x = printer.getDPI().y;
			printerDPI.y = printer.getDPI().x;
		}

		// Adjust the canvas bounds to have the same aspect ratio as the printer
		// and center inside the given bounds
		double printerAspect = (double) printerBounds.height
				/ (double) printerBounds.width;
		double canvasAspect = (double) canvasBounds.height
				/ (double) canvasBounds.width;
		if (canvasAspect > printerAspect) {
			// Canvas bounds is higher
			int newHeight = (int) (canvasBounds.height * printerAspect
					/ canvasAspect + .5);
			canvasBounds.y += (canvasBounds.height - newHeight) / 2;
			canvasBounds.height = newHeight;
		} else {
			// Canvas bounds is wider
			int newWidth = (int) (canvasBounds.width * canvasAspect
					/ printerAspect + .5);
			canvasBounds.x += (canvasBounds.width - newWidth) / 2;
			canvasBounds.width = newWidth;
		}

		// Adjust the print margins
		double scaleFactor = (double) canvasBounds.width / printerBounds.width
				* printerDPI.x / displayDPI.x;
		PrintSettings scaledSettings = settings.clone();
		scaledSettings.setLeft(scaleFactor * scaledSettings.getLeft());
		scaledSettings.setRight(scaleFactor * scaledSettings.getRight());
		scaledSettings.setTop(scaleFactor * scaledSettings.getTop());
		scaledSettings.setBottom(scaleFactor * scaledSettings.getBottom());

		// Draw on the canvas
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(canvasBounds);
		ImagePrintUtils.drawImage(canvas, displayDPI, canvasBounds, image,
				display.getDPI(), scaledSettings);

		// Dispose the printer (but not the GC)
		if (!printer.isDisposed())
			printer.dispose();
	}

}
