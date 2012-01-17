/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jep.Jep;
import jep.JepException;

import org.dawb.fabio.FableJep;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import fable.framework.views.FableIOConsole;

/**
 * a Python console for starting a Python interpreter from Java using Jep
 * 
 * @author goetz
 * 
 */
public class PythonConsole {

	static FableIOConsole pythonConsole;
	RunConsoleThread runConsoleThread = new RunConsoleThread();
	Jep jep = null;
	boolean consoleError = false;

	public PythonConsole() {
		pythonConsole = new FableIOConsole("Python Console");
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(
				new IConsole[] { pythonConsole });
	}

	public void run() {
		new Thread(runConsoleThread).start();
	}

	class RunConsoleThread implements Runnable {
		final Runtime runtime = Runtime.getRuntime();
		String outputMessage;

		public void run() {
			try {
				jep = FableJep.getFableJep().getJep();
				// this seems to be necessary in order to capture the sys.stdout
				jep.setInteractive(true);
				jep.eval("import sys\n");
				// a simple class which can be used to redirect sys.stdout to a
				// variable
				// called stdouttext, the text is appended until a call to clear
				// is made
				jep.eval("class mystdout:\n" + "	def write(self, text):\n"
						+ "		self.stdouttext += text\n" + "	def clear(self):\n"
						+ "		self.stdouttext = \"\"\n");
				jep.eval("mystdout=mystdout()\n");
				// redirect sys.stdout to mystdout so we can pick up the output
				// in interactive mode
				jep.eval("sys.stdout=mystdout");
				jep.eval(null);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pythonConsole.getInputStream();
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(pythonConsole
						.getInputStream(), "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String scriptFile = null;
			File file = null;
			FileInputStream fileStream = null;
			BufferedReader bufferedStream = null;
			boolean scriptInput = false, scriptCommand = false;
			while (true && !consoleError) {
				try {
					String str, command;
					str = "";
					command = "";
					int indent = 0;
					scriptCommand = false;
					if (!scriptInput) {
						pythonConsole.displayOut(">>> ");
						str = in.readLine();
					} else {
						str = bufferedStream.readLine();
						if (str == null) {
							str = "";
							scriptInput = false;
						}
						pythonConsole.displayOut(scriptFile + ":>>> " + str
								+ "\n");
					}
					if (str != null && str.length() > 10) {
						if (str.substring(0, 10).equals("script.run")) {
							scriptFile = str.substring(str.indexOf('(') + 1,
									str.indexOf(')'));
							file = new File(scriptFile);
							if (file.exists()) {
								fileStream = new FileInputStream(file);
								bufferedStream = new BufferedReader(
										new InputStreamReader(fileStream));
								if (fileStream.available() > 0) {
									scriptInput = true;
									scriptCommand = true;
								}
							}
						}
					}
					if (!scriptCommand) {
						command = str + "\n";
						/* does this command require indentation ? */
						if (str != null && str.length() > 1)
							if (str.charAt(str.length() - 1) == ':')
								indent++;
						while (indent != 0) {
							int noSpaces = 0;
							if (!scriptInput) {
								pythonConsole.displayOut("... ");
								str = in.readLine();
							} else {
								str = bufferedStream.readLine();
								if (str == null) {
									str = "";
									scriptInput = false;
								}
								pythonConsole.displayOut(scriptFile + ":... "
										+ str + "\n");
							}
							command = command + str + "\n";
							for (int i = 0; i < str.length(); i++) {
								if (str.charAt(i) != ' ')
									break;
								noSpaces++;
							}
							indent = noSpaces / 4;
						}
						evaluateCommand(command);
					}
				} catch (IOException e) {
					consoleError = true;
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * evaluate a command by sending it to the Python interpreter
	 * 
	 * @param command
	 *            - python command e.g. a=1
	 */
	void evaluateCommand(String command) {

		if (jep == null) {
			pythonConsole
					.displayOut("There is no Jep Python object, please fix the problem and then try again !");
		} else {
			boolean plotCommand = false;
			/* treat plot as a special keyword */
			if (command.length() > 7) {
				if (command.substring(0, 7).equalsIgnoreCase("plot.1d")) {
					String arrayToPlot;
					arrayToPlot = command.substring(command.indexOf('(') + 1,
							command.indexOf(')'));
					plotArray(arrayToPlot);
					plotCommand = true;
				}
			}
			if (!plotCommand) {
				try {
					jep.eval("mystdout.clear()");
					jep.eval(command);
					jep.eval("res=mystdout.stdouttext");
					jep.eval(null);
					String commandOut = (String) jep.getValue("res");
					pythonConsole.displayOut(commandOut);
				} catch (JepException e) {
					pythonConsole.displayError(e.getMessage().substring(
							e.getMessage().indexOf('>') + 3)
							+ "\n");
				}
			}
		}
	}

	/*
	 * get the array from python, activate the python plot and plot the data
	 */
	private void plotArray(String _arrayName) {
		final String arrayName = _arrayName;
		final float arrayData[];
		try {
			jep
					.eval("res = " + arrayName
							+ ".astype(numpy.float32).tostring()");
			arrayData = (float[]) jep.getValue_floatarray("res");
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							PythonPlotView pythonPlot = (PythonPlotView) (PlatformUI
									.getWorkbench().getActiveWorkbenchWindow()
									.getActivePage().showView(
									PythonPlotView.ID, "0",
									IWorkbenchPage.VIEW_ACTIVATE));
							if (pythonPlot != null)
								pythonPlot.plotArray(arrayName, arrayData);
						} catch (PartInitException e2) {
							e2.printStackTrace();
						}
					}
				});
			}
		} catch (JepException e) {
			pythonConsole.displayError(e.getMessage().substring(
					e.getMessage().indexOf('>') + 3)
					+ "\n");
		}
	}

}
