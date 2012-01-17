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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fable.framework.imageprint.PrintSettings.Orientation;
import fable.framework.imageprint.PrintSettings.Units;

/**
 * ImagePrintSetupDialog is a dialog to set print settings. It works with a copy
 * of the image and disposes the copy when done.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ImagePrintSetupDialog extends Dialog {
	private static final int TEXT_WIDTH = 75;
	private static final int CANVAS_BORDER = 10;
	private static final Point CANVAS_SIZE = new Point(300, 300);
	private Point canvasSize = CANVAS_SIZE;
	public static final String[][] hAlignNames = {
			{ "Left", Integer.toString(SWT.LEFT) },
			{ "Center", Integer.toString(SWT.CENTER) },
			{ "Right", Integer.toString(SWT.RIGHT) },
			{ "Fill", Integer.toString(SWT.FILL) },
			};
	public static final String[][] vAlignNames = {
			{ "Top", Integer.toString(SWT.TOP) },
			{ "Center", Integer.toString(SWT.CENTER) },
			{ "Bottom", Integer.toString(SWT.BOTTOM) },
			{ "Fill", Integer.toString(SWT.FILL) },
			};
	/**
	 * The local copy of the input settings. Is returned on OK.
	 */
	private PrintSettings settings;
	private Image image;
	private Canvas canvas;
	private Text leftText;
	private Text rightText;
	private Text topText;
	private Text bottomText;
	private Combo hAlignCombo;
	private Combo vAlignCombo;
	private Button landscapeButton;
	private Button portraitButton;
	private boolean success = false;

	/**
	 * ImagePrintSetupDialog constructor with default style.
	 * 
	 * @param parent
	 *            The parent of this control
	 * @param image
	 *            The image for the preview. Will not show an image preview if
	 *            null.
	 * @param settings
	 *            The input PrintSettings. Will construct a default one if null.
	 */
	public ImagePrintSetupDialog(Shell parent, Image image,
			PrintSettings settings) {
		this(parent, SWT.DIALOG_TRIM, image, settings);
	}

	/**
	 * ImagePrintSetupDialog constructor
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
	public ImagePrintSetupDialog(Shell parent, 
			                     int style, Image image,
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
		Display display = getParent().getDisplay();
		Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText("Print Setup");
		createContents(shell);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
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
	 * Creates the controls for the dialog.
	 * 
	 * @param shell
	 */
	private void createContents(final Shell shell) {
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		shell.setLayout(grid);

		Composite top = new Composite(shell, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
		grid = new GridLayout();
		grid.numColumns = 2;
		top.setLayout(grid);

		Composite controls = new Composite(top, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(controls);
		grid = new GridLayout();
		grid.numColumns = 1;
		controls.setLayout(grid);

		Composite preview = new Composite(top, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(preview);
		grid = new GridLayout();
		grid.numColumns = 1;
		preview.setLayout(grid);
		if (image != null) {
			canvas = new Canvas(preview, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, true).hint(canvasSize)
					.applyTo(canvas);
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent ev) {
					paint(ev);
				}
			});
		}

		// Margins
		Group group = new Group(controls, SWT.NONE);
		group.setText("Margins");
		grid = new GridLayout();
		grid.numColumns = 4;
		group.setLayout(grid);
		GridDataFactory.fillDefaults().applyTo(group);

		Label label = new Label(group, SWT.NONE);
		label.setText("Left");
		leftText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		leftText.setText(settings.getLeftString());
		leftText.setToolTipText("Set the left margin");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				leftText);
		leftText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				String text = leftText.getText();
				PrintSettings.ValueWithUnits vwu = settings
						.parseUnitsString(text);
				PrintSettings.ValueWithUnits vwu1 = settings.scaleToInches(vwu);
				settings.setLeft(vwu1.getVal());
				leftText.setText(settings.getLeftString());
				canvas.redraw();
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText("Right");
		rightText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		rightText.setText(settings.getRightString());
		rightText.setToolTipText("Set the right margin");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				rightText);
		rightText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				String text = rightText.getText();
				PrintSettings.ValueWithUnits vwu = settings
						.parseUnitsString(text);
				PrintSettings.ValueWithUnits vwu1 = settings.scaleToInches(vwu);
				settings.setRight(vwu1.getVal());
				rightText.setText(settings.getRightString());
				canvas.redraw();
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText("Top");
		topText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		topText.setText(settings.getTopString());
		topText.setToolTipText("Set the top margin");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				topText);
		topText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				String text = topText.getText();
				PrintSettings.ValueWithUnits vwu = settings
						.parseUnitsString(text);
				PrintSettings.ValueWithUnits vwu1 = settings.scaleToInches(vwu);
				settings.setTop(vwu1.getVal());
				topText.setText(settings.getTopString());
				canvas.redraw();
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText("Bottom");
		bottomText = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		bottomText.setText(settings.getBottomString());
		bottomText.setToolTipText("Set the bottom margin");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				bottomText);
		bottomText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				String text = bottomText.getText();
				PrintSettings.ValueWithUnits vwu = settings
						.parseUnitsString(text);
				PrintSettings.ValueWithUnits vwu1 = settings.scaleToInches(vwu);
				settings.setBottom(vwu1.getVal());
				bottomText.setText(settings.getBottomString());
				canvas.redraw();
			}
		});

		// Alignment
		group = new Group(controls, SWT.NONE);
		group.setText("Alignment");
		grid = new GridLayout();
		grid.numColumns = 2;
		group.setLayout(grid);
		GridDataFactory.fillDefaults().applyTo(group);

		label = new Label(group, SWT.NONE);
		label.setText("Horizontal");
		hAlignCombo = new Combo(group, SWT.NULL);
		hAlignCombo.setToolTipText("Set the horizontal alignment");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				hAlignCombo);
		int len = hAlignNames.length;
		int curItem = -1;
		String[] items = new String[len];
		int val;
		for (int i = 0; i < len; i++) {
			items[i] = hAlignNames[i][0];
			val = Integer.parseInt(hAlignNames[i][1]);
			if (val == settings.getHorizontalAlign()) {
				curItem = i;
			}
		}
		hAlignCombo.setItems(items);
		hAlignCombo.select(curItem);
		hAlignCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = hAlignCombo.getSelectionIndex();
				int val = Integer.parseInt(hAlignNames[idx][1]);
				settings.setHorizontalAlign(val);
				canvas.redraw();
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText("Vertical");
		vAlignCombo = new Combo(group, SWT.NULL);
		vAlignCombo.setToolTipText("Set the horizontal alignment");
		GridDataFactory.fillDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(
				vAlignCombo);
		len = vAlignNames.length;
		items = new String[len];
		for (int i = 0; i < len; i++) {
			items[i] = vAlignNames[i][0];
			val = Integer.parseInt(vAlignNames[i][1]);
			if (val == settings.getVerticalAlign()) {
				curItem = i;
			}
		}
		vAlignCombo.setItems(items);
		vAlignCombo.select(curItem);
		vAlignCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int idx = vAlignCombo.getSelectionIndex();
				int val = Integer.parseInt(vAlignNames[idx][1]);
				settings.setVerticalAlign(val);
				canvas.redraw();
			}
		});

		// Units
		group = new Group(controls, SWT.NONE);
		group.setText("Units");
		grid = new GridLayout();
		grid.numColumns = 3;
		group.setLayout(grid);
		GridDataFactory.fillDefaults().applyTo(group);

		Button button = new Button(group, SWT.RADIO);
		button.setText("in");
		button.setToolTipText("Set default units to inches");
		button.setSelection(settings.getUnits() == Units.INCH);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				settings.setUnits(Units.INCH);
				resetMarginStrings();
			}
		});

		button = new Button(group, SWT.RADIO);
		button.setText("cm");
		button.setToolTipText("Set default units to centimeters");
		button.setSelection(settings.getUnits() == Units.CENTIMETER);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				settings.setUnits(Units.CENTIMETER);
				resetMarginStrings();
			}
		});

		button = new Button(group, SWT.RADIO);
		button.setText("mm");
		button.setSelection(settings.getUnits() == Units.MILLIMETER);
		button.setToolTipText("Set default units to millimeters");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				settings.setUnits(Units.MILLIMETER);
				resetMarginStrings();
			}
		});

		// Orientation
		group = new Group(controls, SWT.NONE);
		group.setText("Orientation");
		grid = new GridLayout();
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

		label = new Label(group, SWT.NONE);
		label.setText("Note: The orientation is for the preview\n"
				+ "The printer orientation will still have to\n"
				+ "be set for the printer when you print.");
		GridDataFactory.fillDefaults().span(new Point(2, 1)).applyTo(label);

		// Print buttons
		Composite composite = new Composite(controls, SWT.NONE);
		grid = new GridLayout();
		grid.numColumns = 2;
		composite.setLayout(grid);
		GridDataFactory.fillDefaults().applyTo(composite);

		button = new Button(composite, SWT.PUSH);
		button.setText("Select Printer");
		button.setToolTipText("Select a printer (Will not actually print)");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(
				composite);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				// Make a printer dialog to let the user select a printer
				PrintDialog dialog = new PrintDialog(shell);
				PrinterData printerData = dialog.open();
				if (printerData == null) {
					// The user canceled the dialog
					return;
				}
				// Set this printer data into the settings and redraw
				settings.setPrinterData(printerData);
				canvas.redraw();
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setText("Print");
		button.setToolTipText("Print now");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(
				composite);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				ImagePrintUtils.dialogPrintImage(shell, image, shell
						.getDisplay().getDPI(), settings);
			}
		});

		// Bottom composite
		Composite bottom = new Composite(shell, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(bottom);
		FormLayout form = new FormLayout();
		bottom.setLayout(form);

		Button cancelButton = new Button(bottom, SWT.PUSH);
		FormData cancelData = new FormData();
		cancelData.top = new FormAttachment(group, 8);
		cancelData.right = new FormAttachment(100, -4);
		cancelButton.setLayoutData(cancelData);
		cancelButton.setText("&Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				success = false;
				shell.close();
			}
		});

		Button okButton = new Button(bottom, SWT.PUSH);
		FormData okData = new FormData();
		okData.top = new FormAttachment(group, 8);
		okData.right = new FormAttachment(cancelButton, -8);
		okButton.setLayoutData(okData);
		okButton.setText("&OK");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				success = true;
				shell.close();
			}
		});
	}

	/**
	 * Resets the text in the margin controls to represent the current units.
	 */
	private void resetMarginStrings() {
		leftText.setText(settings.getLeftString());
		rightText.setText(settings.getRightString());
		topText.setText(settings.getTopString());
		bottomText.setText(settings.getBottomString());
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
