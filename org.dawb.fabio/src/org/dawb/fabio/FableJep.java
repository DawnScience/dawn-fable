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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jep.Jep;
import jep.JepException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for Java embedded Python (Jep) to create and return a Jep
 * interpreter.
 * 
 * @author andy
 * 
 */
public class FableJep {
	/**
	 * The PYTHONPATH which is prepended to sys.path when starting FableJep.
	 */
	private static String pythonPath = "";
	
	private Jep     jep = null;
	private String  jepError;
	private long    jepLastError = 0;
	private Logger  logger;
	private String  filename = "";
	private boolean requireErrorMessage;
	/**
	 * This value is set to true to record Python calls into a file.
	 */
	private static boolean bRecord = false;

	public Jep getJep() {
		return jep;
	}

	private static Map<Long, FableJep> threadCache;
	/**
	 * This method will return a FableJep instance unique for the calling Thread.
	 * 
	 */
	public static synchronized FableJep getFableJep() throws Throwable {
		
		if (threadCache==null) threadCache = new HashMap<Long, FableJep>(7);
		
		final long id = Thread.currentThread().getId();
		if (threadCache.containsKey(id)) return threadCache.get(id);
		
		final FableJep fj = new FableJep(false);
		threadCache.put(id, fj);
		
		return fj;
	}
	
	
	public static synchronized void closeFableJep() throws Throwable {
		
		if (threadCache==null) return;
		
		final long id = Thread.currentThread().getId();
		if (threadCache.containsKey(id)) {
			FableJep jj = threadCache.get(id);
			jj.close();
		}
	}


	
	/**
	 * Close Jep. This releases the Python subinterpreter and Python will
	 * garbage collect any object created during the script's execution. This
	 * must be called when needed or you may see large memory usage.
	 * 
	 * You must call this method to avoid memory leaks
	 * @throws Throwable 
	 */
	public void close() throws Throwable {
		
		if (jep != null) {
			jep.isValidThread();
			jep.close();
			jep = null;
		}
		threadCache.remove(Thread.currentThread().getId());
	}

	/**
	 * 
	 * @return a new jep object
	 * @throws JepException
	 */
	private FableJep(final boolean requireErrorMessage) throws Throwable {
		try {
			this.requireErrorMessage = requireErrorMessage;
			this.jep = new Jep(true, null, Thread.currentThread().getContextClassLoader());
			jepImportModules("sys", requireErrorMessage);
			jep.eval("if not hasattr(sys,'argv'):\n\tsys.argv = ['fable']");
			jepSetPythonPath(jep);
			logger = LoggerFactory.getLogger(FableJep.class);
			// Only switch to Level.INFO when really necessary (i.e. debugging)
			//logger.setLevel(Level.ERROR);
		} catch (JepException ex) {
			throw ex;
		} catch (UnsatisfiedLinkError e) {
			if (System.currentTimeMillis() - jepLastError > 5000) {
				jepLastError = System.currentTimeMillis();
				String os = System.getProperty("os.name");
				if (os.toLowerCase().contains("windows")) {
					jepError = "Failed to create the Java embedded Python "
							+ "interpreter (jep).";
					jepError += "The error was :\n\n";
					jepError += e.getMessage() + "\n\n";
					jepError += "Make sure Python is installed. ";
					jepError += "You can (partially) test your environment"
							+ " by typing python";
				} else if (os.toLowerCase().contains("mac")) {
					jepError = "Failed to create the Java embedded Python"
							+ " interpreter (jep).";
					jepError += "The error was :\n\n";
					jepError += e.getMessage() + "\n\n";
					jepError += "Make sure Python is installed and and"
							+ " that you have a ";
					jepError += ""
							+ "symbolic link "
							+ "from $IMAGEVIEWER_HOME/plugins/jep_2.0.1/lib.macosx ";
					jepError += "to /Library/Java/Extensions e.g.\n\n";
					jepError += "sudo ln -s "
							+ "$IMAGEVIEWER_HOME/plugins/jep_2.0.1/lib/macosx/libjep.dylib   "
							+ "/Library/Java/Extensions/libjep.jnilib";
				} else {
					jepError = "Failed to create the Java embedded Python "
							+ "interpreter (jep). ";
					jepError += "The error was :\n\n";
					jepError += e.getMessage() + "\n\n";
					jepError += "Make sure Python is installed and "
							+ "that the environment ";
					jepError += "variable LD_PRELOAD is pointing to your "
							+ "Python shared object interpreter ";
					jepError += "e.g. LD_PRELOAD=/usr/lib/libpython2.6.so.1.0. ";
					jepError += "Your current LD_PRELOAD is :\n\n"
							+ System.getenv("LD_PRELOAD") + "\n\n";
					jepError += "You can (partially) test your environment "
							+ "by typing python ";
				}
				if (requireErrorMessage) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openConfirm(Display.getDefault()
									.getActiveShell(), "Confirm", jepError);
						}
					});
				} else {
					throw new Exception(jepError, e);
				}
				jepLastError = System.currentTimeMillis();
			}
		}
	}

	/**
	 * Prepend the pythonPath to sys.path.
	 * 
	 * @throws JepException
	 */
	public void jepSetPythonPath(Jep jep) throws JepException {
		String ps = System.getProperty("path.separator", "|");
		String path = pythonPath.replace("\\", "\\\\");
		String[] paths = path.split(ps);
		String cmd;
		for (int i = paths.length - 1; i >= 0; i--) {
			cmd = "sys.path.insert(0, '" + paths[i] + "')";
			jep.eval(cmd);
		}
	}

	public void jepImportModules(String _modules) throws JepException {
		jepImportModules(_modules, true);
	}
	/**
	 * Import a module for the jep instance.
	 * 
	 * @param _jep
	 * @param _modules
	 * @throws JepException
	 * @throws JepException
	 */
	public void jepImportModules(String _modules, boolean requireErrorMessage) throws JepException {
		try {
			final Jep _jep = getJep();
			_jep.eval("import " + _modules);
			writeScript("import " + _modules);
		} catch (JepException e) {
			if (System.currentTimeMillis() - jepLastError > 5000) {
				jepLastError = System.currentTimeMillis();
				String os = System.getProperty("os.name");
				jepError = "Failed to import the modules " + _modules
						+ " into "
						+ "the Java embedded Python interpreter (jep). ";
				jepError += "The error was :\n\n";
				jepError += e.getMessage() + "\n\n";
				if (os.toLowerCase().contains("windows")) {
					jepError += "Make sure Python is installed and jep.dll"
							+ " is in your PATH ";
				} else {
					jepError += "Make sure Python is installed and "
							+ "that the environment ";
					jepError += "variable LD_PRELOAD is pointing to your "
							+ "Python shared object interpreter ";
					jepError += "e.g. LD_PRELOAD=/usr/lib/libpython2.6.so.1.0. ";
					jepError += "Your current LD_PRELOAD is :\n\n"
							+ System.getenv("LD_PRELOAD") + "\n\n";
				}
				jepError += "and that the Python modules " + _modules
						+ " are installed and in your PYTHONPATH. ";
				jepError += "Your current PYTHONPATH is :\n\n"
						+ PythonInfo.getPythonPathFromEnvironment("  ") + "\n";
				jepError += "You can test your environment by starting "
						+ "python and then typing :\n\nimport " + _modules;
				if (requireErrorMessage) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openConfirm(Display.getDefault()
									.getActiveShell(), "Confirm", jepError);
							jepLastError = System.currentTimeMillis();
						}

					});
				} else {
					throw new JepException(jepError);
				}
			}
			throw e;
		}
	}

	public void jepImportSpecificDefinition(String _from, String _import) throws JepException {

		jepImportSpecificDefinition(_from, _import, true);
	}
	/**
	 * Import a specific definition into the current namespace, ie "from
	 * ImageD11 import peaksearcher" and add the module path to sys.path.
	 * 
	 * @param _jep
	 *            The jep instance.
	 * @param _from
	 *            The module to import.
	 * @param _import
	 *            The specific definition to import.
	 * @throws JepException
	 * @author SUCHET
	 */
	public void jepImportSpecificDefinition(String _from, String _import, final boolean requireMessage) throws JepException {
		try {
			Jep _jep=getJep();
			_jep.eval("from " + _from + " import " + _import);
			writeScript("from " + _from + " import " + _import);
		} catch (JepException e) {
			if (System.currentTimeMillis() - jepLastError > 5000) {
				jepLastError = System.currentTimeMillis();
				jepError = "Failed to import the modules " + _from
						+ " into the"
						+ " Java embedded Python interpreter (jep).";
				jepError += "The error was :\n\n";
				jepError += e.getMessage() + "\n\n";
				jepError += "Make sure Python is installed and jep.dll"
						+ " is in your PATH and that the Python modules "
						+ _from + " are installed and in your PYTHONPATH. ";
				jepError += "Your current PYTHONPATH is :\n\n"
						+ PythonInfo.getPythonPathFromEnvironment("  ") + "\n";
				jepError += "You can test your environment by starting "
						+ "python and then typing :\n\nimport " + _from;
				
				if (requireMessage) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openConfirm(Display.getDefault()
									.getActiveShell(), "Confirm", jepError);
							jepLastError = System.currentTimeMillis();
						}
	
					});
				} else {
					throw new JepException(jepError);
				}
				jepLastError = System.currentTimeMillis();
			}
			throw e;
		}
	}

	public boolean eval(String str) throws JepException {
		logger.info(str);
		writeScript(str);
		return jep.eval(str);
	}

	public void set(String str1, String str2) throws JepException {
		logger.info(str1 + "= " + str2);
		writeScript(str1 + "= \"" + str2 + "\"");
		jep.set(str1, str2);
	}

	public void set(String str1, double dbl) throws JepException {
		logger.info(str1 + "= " + dbl);
		writeScript(str1 + "= " + dbl);
		jep.set(str1, dbl);
	}

	public void set(String str1, int str2) throws JepException {
		logger.info(str1 + "= \"" + str2 + "\"");
		writeScript(str1 + "= " + str2);
		jep.set(str1, str2);
	}

	public void set(String str, float f1) throws JepException {
		logger.info(str + "= " + f1);
		writeScript(str + "= " + f1);
		jep.set(str, f1);
	}

	/**
	 * Return a python variable as a Java Object type using Jep.
	 * <P>
	 * Be careful this methods returns any of the following Java types e.g.
	 * Float, Integer or String, depending on the Python variable. This can
	 * cause problems at run time.
	 * 
	 * @param str
	 *            The name of python variable e.g. "a".
	 * @return The Java object (could be Float or Int or String).
	 * @throws JepException
	 */
	public Object getValue(String str) throws JepException {
		logger.info(str);
		Object value = null;
		try {
			value = jep.getValue(str);
		} catch (JepException e) {
		} catch (Exception e) {
			/*
			 * ignore all exceptions, this handles the case when the python
			 * variable isn't a string
			 */
		}
		return value;
	}

	/**
	 * Return the value as a Boolean using Jep. Returns false if the value is
	 * not defined in Python.
	 * 
	 * @param str
	 *            The name of the Python variable e.g. "a".
	 * @return The value cast to Boolean.
	 * @throws JepException
	 */
	public Boolean getBooleanValue(String str) {
		Boolean value = false;
		try {
			value = (Boolean) jep.getValue(str);
		} catch (JepException e) {
		} catch (Exception e) {
			/*
			 * ignore all exceptions, this handles the case when the python
			 * variable isn't a string
			 */
		}
		return value;
	}

	/**
	 * Cast a variable in python to int and return the value as an Integer using
	 * Jep. Returns null if the value is not defined in Python.
	 * 
	 * @param str
	 *            The name of the Python variable e.g. "a".
	 * @return The value cast to Integer.
	 */
	public Integer getIntegerValue(String str) {
		Integer value = null;
		try {
			value = (Integer) jep.getValue("int(" + str + ")");
		} catch (JepException e) {
		} catch (Exception e) {
			/*
			 * ignore all exceptions, this handles the case when the python
			 * variable isn't a string
			 */
		}
		return value;
	}

	/**
	 * Cast a variable in Python to float and return the value as a Float using
	 * Jep. Returns null if the value is not defined in Python.
	 * 
	 * @param str
	 *            The name of the Python variable e.g. "a".
	 * @return The value cast to Float.
	 * @throws JepException
	 */
	public Float getFloatValue(String str) {
		Float value = null;
		try {
			value = (Float) jep.getValue("float(" + str + ")");
		} catch (JepException e) {
		} catch (Exception e) {
			/*
			 * ignore all exceptions, this handles the case when the python
			 * variable isn't a string
			 */
		}
		return value;
	}

	/**
	 * Return the value as a String using Jep. Returns null if the value is not
	 * defined in Python.
	 * 
	 * @param str
	 *            The name of the Python variable e.g. "a".
	 * @return The value cast to Float.
	 * @throws JepException
	 */
	public String getStringValue(String str) {
		String value = null;
		try {
			value = (String) jep.getValue(str);
		} catch (JepException e) {
		} catch (Exception e) {
			/*
			 * ignore all exceptions, this handles the case when the python
			 * variable isn't a string
			 */
		}
		return value;
	}

	/**
	 * Return a float array from Python using Jep.
	 * 
	 * @param str
	 *            The name of the Python float array.
	 * @return The float[].
	 * @throws JepException
	 */
	public float[] getValue_floatarray(String str) throws JepException {
		logger.info("getValue_floatarray");
		return jep.getValue_floatarray(str);
	}

	/**
	 * Redirect stdout in a python program to a file.
	 * 
	 * @usage new FableJep().redirectStdout()
	 * @throws JepException
	 */
	public void redirectStdout(String filename) throws JepException {
		set("filename", filename);
		jep.eval("logstdout=open(filename, 'w')");
		// redirect sys.stdout to mystdout so we can pick up the output in
		// interactive mode
		jep.eval("sys.stdout=logstdout");
		flushStdout();
	}

	/**
	 * Redirect stdout in a python program to a file.
	 * 
	 * @usage new FableJep().redirectStdout()
	 * @throws JepException
	 */
	public void redirectStderr(String filename) throws JepException {
		set("filename", filename);
		jep.eval("logstderr=open(filename, 'w')");
		// redirect sys.stderr to mystdout so we can pick up the output in
		// interactive mode
		jep.eval("sys.stderr=logstderr");
		flushStdout();
	}

	/**
	 * Flush sys.stdout
	 * 
	 * @throws JepException
	 */
	public void flushStdout() throws JepException {
		jep.eval("sys.stdout.flush()");
	}

	/**
	 * Insert a line in script file.
	 * 
	 * @param addline
	 */
	private void writeScript(String addline) {
		if (bRecord) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(
						filename, true));
				out.write(addline);
				out.newLine();
				out.close();
			} catch (IOException ex) {
				logger.error("Error writing script", ex);
			}
		}
	}

	public static void record(boolean b) {
		// TODO Auto-generated method stub
		bRecord = b;
	}

	public void setScriptFileName(String name) {
		filename = name;
	}

	/**
	 * @return the pythonpath
	 */
	public static String getPythonPath() {
		return pythonPath;
	}

	/**
	 * @param pythonpath
	 *            the pythonpath to set
	 */
	public static void setPythonPath(String pythonPath) {
		FableJep.pythonPath = pythonPath;
	}

	/**
	 * Replaces escape sequences in the given string with a \ plus the escape
	 * sequence character. For example: "\t" -> "\\t" (printed as TAB -> \t).
	 * This is needed when returning filenames from Python using Jep.
	 * 
	 * @param input
	 * @return
	 */
	public static String replaceEscapeSequences(String input) {
		if (input == null) {
			return null;
		}
		String output = input;
		output = output.replaceAll("\t", "\\\\t");
		output = output.replaceAll("\b", "\\\\b");
		output = output.replaceAll("\n", "\\\\n");
		output = output.replaceAll("\r", "\\\\r");
		output = output.replaceAll("\f", "\\\\f");
		output = output.replaceAll("\'", "\\\\'");
		output = output.replaceAll("\"", "\\\\\"");
		return output;
	}

	public boolean isRequireErrorMessage() {
		return requireErrorMessage;
	}

	public void setRequireErrorMessage(boolean requireErrorMessage) {
		this.requireErrorMessage = requireErrorMessage;
	}
}
