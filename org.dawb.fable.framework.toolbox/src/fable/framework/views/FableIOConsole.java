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


import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;


/**
 * A generic console to display the output of a program or display messages to the user
 * and read input from the keyboard
 * 
 * @author Andy Gotz
 */
public class FableIOConsole extends IOConsole {

	private IOConsoleOutputStream outputStream;

	private IOConsoleOutputStream errorStream;

	private IOConsoleInputStream inputStream;

	public static FableIOConsole console = null;

	public static final String ID = "fable.framework.views.fableioconsole";

	/**
	 * DebugConsole constructor. Instance of the same console
	 * is used by all perspectives which are using view.
	 *
	 */
	public FableIOConsole(String consoleTitle) {

		super(consoleTitle, null);
//
// this creates a console, it is called from the place where you want the console to appear e.g. Activator
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ this });
		console = this;

		this.outputStream = newOutputStream();
		// set out message stream color to blue
		this.outputStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_BLUE));

		this.errorStream = newOutputStream();
		// set out message stream color to blue
		this.errorStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_RED));

		this.inputStream = getInputStream();
//		set out message stream color to red
		this.inputStream.setColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GREEN));
	}

	/**
	 * Public method for setting output message.
	 * @param message
	 */

	public void displayOut(String message){
		final String messageOut = message;
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						outputStream.write(messageOut);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	/**
	 * Public method for setting error message.
	 * @param message
	 */

	public void displayError(String message){
		final String messageOut = message;
		if (!Display.getDefault().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						errorStream.write(messageOut);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

}
