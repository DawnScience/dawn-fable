/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.imageprint.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import fable.framework.imageprint.ImagePrintPreviewDialog;
import fable.framework.imageprint.ImagePrintSetupDialog;
import fable.framework.imageprint.ImagePrintUtils;
import fable.framework.imageprint.PrintSettings;
import fable.framework.toolbox.SWTUtils;

public class SWTImageViewer {
	// private static final boolean usePrintSettingDialog = true;
	private static final boolean useStartImage = true;
	private static final String startImageName1 = "C:/Documents and Settings/evans/My Documents/My Pictures/DAZ.Dogfight.15017.jpg";
	private static final String startImageName2 = "C:/users/evans/Pictures/DAZ.Dogfight.15017.jpg";
	// private static final String startImageName1 =
	// "C:/Documents and Settings/evans/My Documents/My Pictures/ChromaticityDiagram.png";
	private String startImageName = startImageName2;
	private Display display = new Display();
	private Shell shell = new Shell(display);
	private PrintSettings settings;
	private Canvas canvas;
	private ScrollBar hBar;
	private ScrollBar vBar;
	private Point origin;
	private Image image;
	private String fileName;

	public SWTImageViewer() {
		shell.setText("SWT Image Viewer");
		shell.setLayout(new GridLayout(1, true));
		settings = new PrintSettings();

		// Pick the default file to use
		if (System.getProperty("os.name", "Windows XP").equalsIgnoreCase(
				"Windows XP")) {
			startImageName = startImageName1;
		}

		ToolBar toolBar = new ToolBar(shell, SWT.FLAT);
		ToolItem itemOpen = new ToolItem(toolBar, SWT.PUSH);
		itemOpen.setText("Open");
		itemOpen.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String file = dialog.open();
				if (file != null) {
					if (image != null)
						image.dispose();
					image = null;
					try {
						image = new Image(display, file);
					} catch (RuntimeException ex) {
						SWTUtils.excMsgAsync("Failed to load image from file: "
								+ file, ex);
					}
					if (image != null) {
						fileName = file;
						shell.setText("SWT Image Viewer " + fileName);
						canvas.redraw();
					}
				}
			}
		});

		ToolItem itemPrintSetup = new ToolItem(toolBar, SWT.PUSH);
		itemPrintSetup.setText("Print Setup");
		itemPrintSetup.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ImagePrintSetupDialog dialog = new ImagePrintSetupDialog(shell,
						image, settings);
				settings = dialog.open();
			}
		});

		ToolItem itemPrintPreview = new ToolItem(toolBar, SWT.PUSH);
		itemPrintPreview.setText("Preview");
		itemPrintPreview.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ImagePrintPreviewDialog dialog = new ImagePrintPreviewDialog(
						shell, image, settings);
				settings = dialog.open();
			}
		});

		ToolItem itemPrint = new ToolItem(toolBar, SWT.PUSH);
		itemPrint.setText("Print");
		itemPrint.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				print();
			}
		});

		origin = new Point(0, 0);
		canvas = new Canvas(shell, SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		// canvas = new Canvas(shell, SWT.NO_BACKGROUND
		// | SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL);
		canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));

		hBar = canvas.getHorizontalBar();
		hBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event ev) {
				int hSelection = hBar.getSelection();
				int destX = -hSelection - origin.x;
				Rectangle rect = image.getBounds();
				canvas.scroll(destX, 0, 0, 0, rect.width, rect.height, false);
				origin.x = -hSelection;
			}
		});

		vBar = canvas.getVerticalBar();
		vBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event ev) {
				int vSelection = vBar.getSelection();
				int destY = -vSelection - origin.y;
				Rectangle rect = image.getBounds();
				canvas.scroll(0, destY, 0, 0, rect.width, rect.height, false);
				origin.y = -vSelection;
			}
		});

		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event ev) {
				if (image == null || image.isDisposed())
					return;
				Rectangle rect = image.getBounds();
				Rectangle client = canvas.getClientArea();
				hBar.setMaximum(rect.width);
				vBar.setMaximum(rect.height);
				hBar.setThumb(Math.min(rect.width, client.width));
				vBar.setThumb(Math.min(rect.height, client.height));
				int hPage = rect.width - client.width;
				int vPage = rect.height - client.height;
				int hSelection = hBar.getSelection();
				int vSelection = vBar.getSelection();
				if (hSelection >= hPage) {
					if (hPage <= 0)
						hSelection = 0;
					origin.x = -hSelection;
				}
				if (vSelection >= vPage) {
					if (vPage <= 0)
						vSelection = 0;
					origin.y = -vSelection;
				}
				canvas.redraw();
			}
		});

		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event ev) {
				if (image == null || image.isDisposed()) {
					return;
				}
				GC gc = ev.gc;
				gc.drawImage(image, origin.x, origin.y);
				Rectangle rect = image.getBounds();
				Rectangle client = canvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					gc.fillRectangle(rect.width, 0, marginWidth, client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					gc
							.fillRectangle(0, rect.height, client.width,
									marginHeight);
				}
			}
		});

		// Load an image without using the Open menu
		if (useStartImage) {
			try {
				fileName = startImageName;
				image = new Image(display, startImageName);
				shell.setText("SWT Image Viewer " + fileName);
			} catch (RuntimeException ex) {
				// Fail silently
			} finally {
				canvas.redraw();
			}
		}

		shell.setSize(800, 600);
		shell.open();

		// textUser.forceFocus();

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		display.dispose();
	}

	/**
	 * Lets the user select a printer and prints the image on it.
	 * 
	 */
	void print() {
		PrintDialog dialog = new PrintDialog(shell);
		// Prompts the printer dialog to let the user select a printer.
		PrinterData printerData = dialog.open();
		if (printerData == null) // the user cancels the dialog
			return;
		// Loads the printer.
		ImagePrintUtils.printImage(image, shell.getDisplay().getDPI(), null);
	}

	public static void main(String[] args) {
		new SWTImageViewer();
	}
}
