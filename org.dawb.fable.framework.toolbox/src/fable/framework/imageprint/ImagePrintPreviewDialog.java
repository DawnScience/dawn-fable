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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import fable.framework.imageprint.PrintSettings.Orientation;

/**
 * ImagePrintPreviewDialog is a dialog for previewing an image. It works with a
 * copy of the image and disposes the copy when done.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ImagePrintPreviewDialog extends Dialog {
	private static final int CANVAS_BORDER = 10;
	private static final Point CANVAS_SIZE = new Point(425, 550);
	private Point canvasSize = CANVAS_SIZE;
	/**
	 * The local copy of the input settings. Is returned on OK.
	 */
	private PrintSettings settings;
	private Image image;
	private Canvas canvas;
	private Button landscapeButton;
	private Button portraitButton;
	private boolean success = false;

	/**
	 * ImagePrintPreviewDialog constructor with default style.
	 * 
	 * @param parent
	 *            The parent of this control
	 * @param image
	 *            The image for the preview. Will not show an image preview if
	 *            null.
	 * @param settings
	 *            The input PrintSettings. Will construct a default one if null.
	 */
	public ImagePrintPreviewDialog(Shell parent, Image image,
			PrintSettings settings) {
		this(parent, SWT.DIALOG_TRIM, image, settings);
	}

	/**
	 * ImagePrintPreviewDialog constructor
	 * 
	 * @param parent
	 *            The parent of this control
	 * @param style
	 *            The style settings.
	 * @param image
	 *            The image for the preview. Will not show an image preview if
	 *            null.
	 * @param settings
	 *            The input PrintSettings. Will construct a default one if null.
	 */
	public ImagePrintPreviewDialog(Shell parent, int style, Image image,
			PrintSettings settings) {
		super(parent, style);
		// Create a copy of the image in case it changes under us
		if (image != null) {
			this.image = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
		} else {
			this.image = null;
		}
		if (settings != null) {
			this.settings = settings.clone();
		} else {
			this.settings = new PrintSettings();
		}
	}

	/**
	 * Creates and then opens the dialog. Note that setting or getting whether
	 * to use portrait or not must be handled separately.
	 * 
	 * @return The new value of the PrintSettings.
	 */
	public PrintSettings open() {
		final Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText("Print preview");
		shell.setLayout(new GridLayout(6, false));

		// Orientation
		Composite group = new Composite(shell, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		group.setLayout(grid);
		GridDataFactory.fillDefaults().applyTo(group);

		portraitButton = new Button(group, SWT.RADIO);
		portraitButton.setText("Portrait");
		portraitButton.setToolTipText("Use Portrait for the preview");
		portraitButton
				.setSelection(settings.getOrientation() != Orientation.LANDSCAPE);
		portraitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				settings.setOrientation(Orientation.PORTRAIT);
				canvas.redraw();
			}
		});

		landscapeButton = new Button(group, SWT.RADIO);
		landscapeButton.setText("Landscape");
		landscapeButton.setToolTipText("Use Portrait for the preview");
		landscapeButton
				.setSelection(settings.getOrientation() == Orientation.LANDSCAPE);
		landscapeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				settings.setOrientation(Orientation.LANDSCAPE);
				canvas.redraw();
			}
		});

		Button button = new Button(shell, SWT.PUSH);
		button.setText("Print Setup");
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ImagePrintSetupDialog dialog = new ImagePrintSetupDialog(shell,
						image, settings);
				settings = dialog.open();
				setOrientation();
				canvas.redraw();
			}
		});

		button = new Button(shell, SWT.PUSH);
		button.setText("Select Printer");
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				PrintDialog dialog = new PrintDialog(shell);
				// Prompts the printer dialog to let the user select a printer.
				PrinterData printerData = dialog.open();
				if (printerData == null) // The user canceled the dialog
					return;
				// Set this printer data into the settings and redraw
				settings.setPrinterData(printerData);
				canvas.redraw();
			}
		});

		button = new Button(shell, SWT.PUSH);
		button.setText("Print");
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ImagePrintUtils.dialogPrintImage(shell, image, shell
						.getDisplay().getDPI(), settings);
			}
		});

		button = new Button(shell, SWT.PUSH);
		button.setText("Cancel");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				success = false;
				shell.close();
			}
		});

		button = new Button(shell, SWT.PUSH);
		button.setText("OK");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				success = true;
				shell.close();
			}
		});

		canvas = new Canvas(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).span(6, 1).hint(
				canvasSize).applyTo(canvas);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent ev) {
				paint(ev);
			}
		});

		shell.pack();
		shell.open();

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				// If no more entries in event queue
				shell.getDisplay().sleep();
			}
		}
		if (image != null && !image.isDisposed()) {
			image.dispose();
			image = null;
		}
		if (success) {
			return settings;
		} else {
			return null;
		}
	}

	/**
	 * Set the orientation..
	 * 
	 * @param usePortrait
	 */
	private void setOrientation() {
		boolean usePortrait = settings.getOrientation() != Orientation.LANDSCAPE;
		if (portraitButton != null) {
			portraitButton.setSelection(usePortrait);
		}
		if (landscapeButton != null) {
			landscapeButton.setSelection(!usePortrait);
		}
	}

	/**
	 * Paint method for the canvas.
	 * 
	 * @param ev
	 */
	private void paint(PaintEvent ev) {
		GC gc = ev.gc; // Do not dispose
		Rectangle canvasBounds = canvas.getClientArea();
		canvasBounds.x += CANVAS_BORDER;
		canvasBounds.y += CANVAS_BORDER;
		canvasBounds.width -= 2 * CANVAS_BORDER;
		canvasBounds.height -= 2 * CANVAS_BORDER;
		if (settings.getPrinterData()!=null)
		    ImagePrintUtils.paintPreview(gc, canvas, canvasBounds, image, settings);
	}

	/**
	 * @return The value of canvasSize.
	 */
	public Point getCanvasSize() {
		return canvasSize;
	}

	/**
	 * @param canvasSize
	 *            The new value for canvasSize.
	 */
	public void setCanvasSize(Point canvasSize) {
		this.canvasSize = canvasSize;
	}

}
