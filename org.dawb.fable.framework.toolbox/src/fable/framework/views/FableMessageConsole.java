/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.views;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * A generic console to display the output of a program or display messages to
 * the user
 * 
 * @author Andy Gotz
 */
public class FableMessageConsole extends MessageConsole {

	private MessageConsoleStream inMessageStream;
	private MessageConsoleStream outMessageStream;
	private MessageConsoleStream errorMessageStream;
	private MessageConsoleStream infoMessageStream;

	public static FableMessageConsole console = null;

	public static final String ID = "fable.framework.views.fablemessageconsole";

	/**
	 * DebugConsole constructor. Instance of the same console is used by all
	 * perspectives which are using view.
	 * 
	 */
	public FableMessageConsole(String consoleTitle) {

		super(consoleTitle, null);
		// ConsolePlugin.getDefault().getConsoleManager().addConsoles(new
		// IConsole[]{ this });
		console = this;

		this.inMessageStream = newMessageStream();
		this.inMessageStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		this.outMessageStream = newMessageStream();
		// set out message stream color to blue
		this.outMessageStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_BLUE));
		this.errorMessageStream = newMessageStream();
		// set out message stream color to blue
		this.errorMessageStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_RED));
		this.infoMessageStream = newMessageStream();
		// set out message stream color to blue
		this.infoMessageStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_MAGENTA));

	}

	/**
	 * Public method for displaying input messages.
	 * 
	 * @param message
	 */
	public void displayIn(String message) {
		Date now = new Date();
		final String messageIn = now.toString() + " " + message;
		now.toString();
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					inMessageStream.println(messageIn);
				}
			});
		}

	}

	/**
	 * Public method for displaying output message.
	 * 
	 * @param message
	 */

	public void displayOut(String message) {
		Date now = new Date();
		final String messageOut = now.toString() + " " + message;
		now.toString();
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					outMessageStream.println(messageOut);
				}
			});
		}
	}

	/**
	 * Public method for displaying error messages.
	 * 
	 * @param message
	 */
	public void displayError(String message) {
		Date now = new Date();
		final String messageError = now.toString() + " " + message;
		now.toString();
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					errorMessageStream.println(messageError);
				}
			});
		}
	}

	/**
	 * Public method for displaying error messages.
	 * 
	 * @param message
	 */
	public void displayInfo(String message) {
		Date now = new Date();
		final String messageInfo = now.toString() + " " + message;
		now.toString();
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					infoMessageStream.println(messageInfo);
				}
			});
		}
	}
}
