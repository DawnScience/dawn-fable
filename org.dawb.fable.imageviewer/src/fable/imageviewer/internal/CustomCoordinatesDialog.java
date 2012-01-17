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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import fable.framework.toolbox.FableUtils;
import fable.imageviewer.preferences.PreferenceConstants;
import fable.imageviewer.rcp.Activator;

/**
 * Class to implement a dialog to get the parameters for a custom Coordinate.
 * 
 * @author evans
 * 
 */
public class CustomCoordinatesDialog extends Dialog {
	private static final int DEFAULT_TEXT_WIDTH = 30;
	// private static final int VERTICAL_SPACING = 0;
	// private static final int TEXT_WIDTH = 400;
	// private static final int TEXT_HEIGHT = 300;
	private double x0 = 0;
	private double y0 = 0;
	private double pixelWidth = 1;
	private double pixelHeight = 1;
	private String xName = "x";
	private String yName = "y";
	boolean success = false;
	Text x0Text, y0Text, pixelWidthTexy, pixelHeightText, xNameText, yNameText;

	/**
	 * Constructor that gives the default style and makes the dialog modal.
	 * 
	 * @param parent
	 * @param x0
	 * @param y0
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param xName
	 * @param yName
	 */
	public CustomCoordinatesDialog(Shell parent, double x0, double y0,
			double pixelWidth, double pixelHeight, String xName, String yName) {
		// We want this to be modal
		this(parent, x0, y0, pixelWidth, pixelHeight, xName, yName,
				SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.x0 = x0;
		this.y0 = y0;
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.xName = xName;
		this.yName = yName;
	}

	/**
	 * Constructor that gets its parameters from the Preferences,has the default
	 * style, and is modal.
	 * 
	 * @param parent
	 */
	public CustomCoordinatesDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		try {
			x0 = Double.parseDouble(prefs
					.getString(PreferenceConstants.P_COORD_X0));
			y0 = Double.parseDouble(prefs
					.getString(PreferenceConstants.P_COORD_Y0));
			pixelWidth = Double.parseDouble(prefs
					.getString(PreferenceConstants.P_COORD_PIXELWIDTH));
			pixelHeight = Double.parseDouble(prefs
					.getString(PreferenceConstants.P_COORD_PIXELHEIGHT));
			xName = prefs.getString(PreferenceConstants.P_COORD_XNAME);
			yName = prefs.getString(PreferenceConstants.P_COORD_YNAME);
		} catch (NumberFormatException ex) {
			// Do nothing
		}
	}

	/**
	 * Constructor that allows setting the style, including modality.
	 * 
	 * @param parent
	 * @param x0
	 * @param y0
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param xName
	 * @param yName
	 * @param style
	 */
	public CustomCoordinatesDialog(Shell parent, double x0, double y0,
			double pixelWidth, double pixelHeight, String xName, String yName,
			int style) {
		super(parent, style);
	}

	public boolean open() {
		Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText("Set Custom Coordinates");
		Image image = null;
		try {
			image = PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJS_INFO_TSK);
			// Might be better to get the image from the main window, but
			// haven't figured out how
			// This doesn't seem to work:
			// image = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			// .getShell().getImage();
		} catch (Exception ex) {
			// Do nothing
		}
		if (image != null)
			shell.setImage(image);
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return success;
	}

	private void createContents(final Shell shell) {
		FormLayout form = new FormLayout();
		form.marginWidth = form.marginHeight = 8;
		shell.setLayout(form);

		Group box = new Group(shell, SWT.BORDER);
		box.setText("Custom Coordinates");
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		box.setLayout(grid);

		Text text = new Text(box, SWT.MULTI);
		text.setEditable(false);
		String info = "The displayed coordinates (x1, y1) are defined via the equations:\n"
				+ "\tx1 = pixelWidth * (x - x0)\n"
				+ "\ty2 = pixelWidth * (y - y0)\n"
				+ "where (x, y) are the image coordinates with origin at top left.\n"
				+ "The coordinates can be named other than x and y.";
		text.setText(info);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		// gridData.widthHint = TEXT_WIDTH;
		text.setLayoutData(gridData);

		Composite composite = new Composite(box, SWT.None);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.None);
		label.setText("x0");
		setGridData(label);
		final Text x0Text = new Text(composite, SWT.BORDER | SWT.SINGLE
				| SWT.RIGHT);
		x0Text.setText(String.valueOf(x0));
		setGridData(x0Text);

		label = new Label(composite, SWT.None);
		label.setText("y0");
		setGridData(label);
		final Text y0Text = new Text(composite, SWT.BORDER | SWT.SINGLE
				| SWT.RIGHT);
		y0Text.setText(String.valueOf(y0));
		setGridData(y0Text);

		label = new Label(composite, SWT.None);
		label.setText("pixelWidth");
		setGridData(label);
		final Text pixelWidthText = new Text(composite, SWT.BORDER | SWT.SINGLE
				| SWT.RIGHT);
		pixelWidthText.setText(String.valueOf(pixelWidth));
		setGridData(pixelWidthText);

		label = new Label(composite, SWT.None);
		label.setText("pixelHeight");
		setGridData(label);
		final Text pixelHeightText = new Text(composite, SWT.BORDER
				| SWT.SINGLE | SWT.RIGHT);
		pixelHeightText.setText(String.valueOf(pixelHeight));
		setGridData(pixelHeightText);

		label = new Label(composite, SWT.None);
		label.setText("xName");
		setGridData(label);
		final Text xNameText = new Text(composite, SWT.BORDER | SWT.SINGLE
				| SWT.RIGHT);
		xNameText.setText(xName);
		setGridData(xNameText);

		label = new Label(composite, SWT.None);
		label.setText("yName");
		setGridData(label);
		final Text yNameText = new Text(composite, SWT.BORDER | SWT.SINGLE
				| SWT.RIGHT);
		yNameText.setText(yName);
		setGridData(yNameText);

		Button cancelButton = new Button(shell, SWT.PUSH);
		FormData cancelData = new FormData();
		cancelData.top = new FormAttachment(box, 8);
		cancelData.right = new FormAttachment(100, -4);
		cancelButton.setLayoutData(cancelData);
		cancelButton.setText("&Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				success = false;
				shell.close();
			}
		});

		Button okButton = new Button(shell, SWT.PUSH);
		FormData okData = new FormData();
		okData.top = new FormAttachment(box, 8);
		okData.right = new FormAttachment(cancelButton, -8);
		okButton.setLayoutData(okData);
		okButton.setText("&OK");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String processing = "xName";
				try {
					xName = xNameText.getText();
					processing = "yName";
					yName = yNameText.getText();
					processing = "x0";
					x0 = Double.parseDouble(x0Text.getText());
					processing = "y0";
					y0 = Double.parseDouble(y0Text.getText());
					processing = "pixelWidth";
					pixelWidth = Double.parseDouble(pixelWidthText.getText());
					processing = "pixelHeight";
					pixelHeight = Double.parseDouble(pixelHeightText.getText());
					success = true;
					shell.close();
				} catch (NumberFormatException ex) {
					FableUtils.excMsg(this, "Error processing " + processing,
							ex);
				}
			}
		});
		shell.setDefaultButton(okButton);
	}

	/**
	 * Helper function to set GridData for controls.
	 * 
	 * @param control
	 * @return
	 */
	private GridData setGridData(Control control) {
		GridData gd = new GridData();
		// Choices are LEFT, CENTER, RIGHT, FILL
		// Only matters if grabExcessHorizontalSpace = true
		gd.horizontalAlignment = SWT.FILL;
		// Set the Text width
		if (control instanceof Text) {
			gd.grabExcessHorizontalSpace = true;
			gd.widthHint = DEFAULT_TEXT_WIDTH;
		}
		control.setLayoutData(gd);
		return gd;
	}

	/**
	 * @return the x0
	 */
	public double getX0() {
		return x0;
	}

	/**
	 * @return the y0
	 */
	public double getY0() {
		return y0;
	}

	/**
	 * @return the pixelWidth
	 */
	public double getPixelWidth() {
		return pixelWidth;
	}

	/**
	 * @return the pixelHeight
	 */
	public double getPixelHeight() {
		return pixelHeight;
	}

	/**
	 * @return the xName
	 */
	public String getXName() {
		return xName;
	}

	/**
	 * @return the yName
	 */
	public String getYName() {
		return yName;
	}

}
