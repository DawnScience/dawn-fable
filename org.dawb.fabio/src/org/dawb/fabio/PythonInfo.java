/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.fabio;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to get various kinds of information about Python running from Jep.
 */
public class PythonInfo {
	
	private static Logger logger = LoggerFactory.getLogger(PythonInfo.class);
	
	// Used to have import "OpenGL.Tk" and "from OpenGL import Tk". These are
	// needed for the ImageD11 GUI but not for Fable.
	private static final String[] checkCommands = { "import matplotlib",
			"import numpy", "import numpy.oldnumeric", "import PIL",
			"import OpenGL", "import FitAllB", "import Fabric",
			"from ImageD11 import peaksearcher", "import fabio.openimage",
			"import polyxsim" };

	/**
	 * Gets information about Python.
	 * 
	 * @return
	 */
	public static String getPythonInfo() {
		String info = "";
		info += "Python: \n";
		// This can be confusing and not of much use
		info += "  sys.executable: " + getPythonSysValue("  ", "executable")
				+ "\n";
		// // This has been proposed but doesn't appear to be implemented.
		// info += "  sys.python_executable: "
		// + getPythonSysValue("  ", "python_executable") + "\n";
		info += "  sys.prefix: " + getPythonSysValue("  ", "prefix") + "\n";
		info += "  sys.exec_prefix: " + getPythonSysValue("  ", "exec_prefix")
				+ "\n";
		info += "  sys.version: " + getPythonSysValue("  ", "version") + "\n";

		info += "\n";
		info += "PYTHONPATH from Environment:\n";
		info += getPythonPathFromEnvironment("  ");

		info += "\n";
		info += "PYTHONPATH from Python:\n";
		info += getPythonPathFromPython("  ");

		info += "\n";
		info += "PATH:\n";
		info += getPathFromEnvironment("  ");

		for (String cmd : checkCommands) {
			info += "\n";
			info += "Check '" + cmd + "':\n";
			info += "  " + (checkCommand(cmd) ? "Succeeded" : "Failed") + "\n";
		}

		return info;
	}

	/**
	 * Gets a variable from sys in Python, using Jep.
	 * 
	 * @param var
	 *            The variable (xxx in sys.xxx).
	 * @return
	 */
	public static String getPythonSysValue(String linePrefix, String var) {
		String info = "";
		FableJep jep = null;
		try {
			jep = FableJep.getFableJep();
			info = linePrefix + (String) jep.getValue("sys." + var);
		} catch (Throwable ex) {
			info += linePrefix + "Error running Jep:\n\n" + linePrefix
					+ ex.getMessage();
		} finally {
			if (jep != null)
				try {
					jep.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		return info;
	}

	/**
	 * Gets the PYTHONPATH from the environment as a list with one line for each
	 * element in the path.
	 * 
	 * @return
	 */
	public static String getPythonPathFromEnvironment() {
		return getPythonPathFromEnvironment("");
	}

	/**
	 * Gets the PYTHONPATH from the environment as a list with one line for each
	 * element in the path.
	 * 
	 * @param linePrefix
	 *            String to add before each line in the path list.
	 * @return
	 */
	public static String getPythonPathFromEnvironment(String linePrefix) {
		String info = "";
		String path = System.getenv("PYTHONPATH");
		if (path != null) {
			// Break it down into elements, one element per line
			String[] elements = path.split(File.pathSeparator);
			for (String element : elements) {
				info += linePrefix + element + "\n";
			}
		} else {
			info += linePrefix + "Not Found" + "\n";
		}
		return info;
	}

	/**
	 * Gets the PATH from the environment as a list with one line for each
	 * element in the path.
	 * 
	 * @return
	 */
	public static String getPathFromEnvironment() {
		return getPathFromEnvironment("");
	}

	/**
	 * Gets the PATH from the environment as a list with one line for each
	 * element in the path.
	 * 
	 * @param linePrefix
	 *            String to add before each line in the path list.
	 * @return
	 */
	public static String getPathFromEnvironment(String linePrefix) {
		String info = "";
		String path = System.getenv("PATH");
		if (path != null) {
			// Break it down into elements, one element per line
			String[] elements = path.split(File.pathSeparator);
			for (String element : elements) {
				info += linePrefix + element + "\n";
			}
		} else {
			info += linePrefix + "Not Found" + "\n";
		}
		return info;
	}

	/**
	 * Gets the PYTHONPATH from Python, using Jep, as a list with one line for
	 * each element in the path.
	 * 
	 * @return
	 */
	public static String getPythonPathFromPython() {
		return getPythonPathFromEnvironment("");
	}

	/**
	 * Gets the PYTHONPATH from Python, using Jep, as a list with one line for
	 * each element in the path.
	 * 
	 * @param linePrefix
	 *            String to add before each line in the path list.
	 * @return
	 */
	private static String getPythonPathFromPython(String linePrefix) {
		String info = "";
		FableJep jep = null;
		try {
			jep = FableJep.getFableJep();
			jep.eval("import sys");
			jep.set("linePrefix", linePrefix);
			jep.set("delimiter", "\n" + linePrefix);
			info = (String) jep
					.getValue("linePrefix + delimiter.join(sys.path)");
		} catch (Throwable ex) {
			info += linePrefix + "Error running Jep:\n\n" + linePrefix
					+ ex.getMessage();
		} finally {
			if (jep != null)
				try {
					jep.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
		return info + "\n";
	}

	/**
	 * Check if a Python command gives an exception or not.
	 * 
	 * @param cmd
	 *            The command to evaluate.
	 * @return Whether the command succeeded or not.
	 */
	private static Boolean checkCommand(String cmd) {
		Boolean retVal = true;
		FableJep jep = null;
		try {
			jep = FableJep.getFableJep();
			jep.eval(cmd);
		} catch (Throwable ex) {
			logger.error("Checking Python command \"" + cmd
					+ "\" failed: " + ex.getMessage());
			retVal = false;
		} finally {
			if (jep != null)
				try {
					jep.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
		return retVal;
	}

}
