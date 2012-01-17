/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolbox;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * ExceptionMessageDialog is a class to display an Exception dialog similar to a
 * MessageDialog but with a stack trace. Even though the name is
 * ExceptionMessageDialog, it will handle a Throwable, which can be an Exception
 * or Error.
 * 
 * @author evans
 * 
 */
public class ExceptionMessageDialog extends MessageDialog {
	public static final String LS = System.getProperty("line.separator");
	String throwableText = "";

	/**
	 * Constructor similar to MessageDialog with the Throwable as an additional
	 * argument.
	 * 
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogTitleImage
	 * @param dialogMessage
	 * @param dialogImageType
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 * @param t
	 *            The throwable.
	 */
	ExceptionMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex, Throwable t) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		setThrowableText(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		Text text = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		text.setEditable(false);
		text.setText(throwableText);
		return text;
	}

	/**
	 * Convenience method to open a standard ExceptionMessageDialog.
	 * 
	 * @param parent
	 *            The parent shell of the dialog, or <code>null</code> if none.
	 * @param title
	 *            The dialog's title, or <code>null</code> if none.
	 * @param message
	 *            The message.
	 * @param t
	 *            The Throwable.
	 */
	public static void openException(Shell parent, String title, String msg,
			Throwable t) {
		String throwableName = "Exception";
		if (t instanceof Error) {
			throwableName = "Error";
		}
		final String fullMsg = msg + LS + LS + throwableName + ": " + t + LS
				+ "Message: " + t.getMessage();
		ExceptionMessageDialog dialog = new ExceptionMessageDialog(parent,
				title, null, fullMsg, ERROR,
				new String[] { IDialogConstants.OK_LABEL }, 0, t);
		dialog.open();
		return;
	}

	/**
	 * Sets the text in the custom area.
	 * 
	 * @param ex
	 */
	private void setThrowableText(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		throwableText = sw.toString();
	}
}
