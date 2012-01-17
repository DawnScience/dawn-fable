/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.jep;

import org.dawb.fabio.PythonInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Class to implement a dialog to display Python info. Very little is specific
 * to Python info, so this class could be generalized to display a modal (by
 * default) dialog with a given dialog title, group title, and text to display.
 */
public class PythonInfoDialog extends Dialog {
	private static final int TEXT_WIDTH = 400;
	private static final int TEXT_HEIGHT = 300;

	public PythonInfoDialog(Shell parent) {
		// We want this to be modeless
		this(parent, SWT.DIALOG_TRIM | SWT.NONE);
	}

	public PythonInfoDialog(Shell parent, int style) {
		super(parent, style);
	}

	public String open() {
		Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText("Python Information");
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
		}
		if (image != null)
			shell.setImage(image);
		// It can take a long time to do this so use a wait cursor
		Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		if (waitCursor != null)
			getParent().setCursor(waitCursor);
		createContents(shell);
		getParent().setCursor(null);
		waitCursor.dispose();
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}

	private void createContents(final Shell shell) {
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		shell.setLayout(grid);

		Group box = new Group(shell, SWT.BORDER);
		box.setText("Python Information");
		grid = new GridLayout();
		grid.numColumns = 1;
		box.setLayout(grid);
		GridData gridData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		box.setLayoutData(gridData);

		Text text = new Text(box, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText(PythonInfo.getPythonInfo());
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = TEXT_WIDTH;
		gridData.heightHint = TEXT_HEIGHT;
		text.setLayoutData(gridData);

		Button close = new Button(shell, SWT.PUSH);
		close.setText("Close");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		close.setLayoutData(gridData);

		close.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		shell.setDefaultButton(close);
	}

}
