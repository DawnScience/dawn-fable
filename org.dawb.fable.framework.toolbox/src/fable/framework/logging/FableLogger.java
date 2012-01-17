/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * wrapper for the Log4j logging classes, to create and return a singleton Log4j
 * Logger
 * 
 * @author andy
 * 
 */

public class FableLogger {

	private static Logger logger = null;

	/**
	 * gets the singleton copy of the log4j logger, if it doesn't exist create
	 * it
	 * 
	 * @return log4j fable logger singleton i.e. only one per application
	 */
	static public Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(FableLogger.class);
		}
		return logger;
	}

	/**
	 * returns a possibly new copy of a log4j logger for the specified class
	 * 
	 * @return log4j logger for the specified class, can be more than one per
	 *         application
	 */
	public static Logger getLogger(Class<?> _class) {
		return LoggerFactory.getLogger(_class);
	}

	// The order of levels is:
	// trace
	// debug
	// info
	// warn
	// error
	// fatal

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 */
	public static void trace(String message) {
		if (logger == null) getLogger();
		logger.trace(message);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 * @param t
	 */
	public static void trace(String message, Throwable t) {
		if (logger == null) getLogger();
		logger.trace(message, t);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 */
	public static void debug(String message) {
		if (logger == null) getLogger();
		logger.debug(message);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 * @param t
	 */
	public static void debug(String message, Throwable t) {
		if (logger == null) getLogger();
		logger.debug(message, t);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 */
	public static void info(String message) {
		if (logger == null) getLogger();
		logger.info(message);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 * @param t
	 */
	public static void info(String message, Throwable t) {
		if (logger == null) getLogger();
		logger.info(message, t);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 */
	public static void warn(String message) {
		if (logger == null) getLogger();
		logger.warn(message);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 * @param t
	 */
	public static void warn(String message, Throwable t) {
		if (logger == null) getLogger();
		logger.warn(message, t);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 */
	public static void error(String message) {
		if (logger == null) getLogger();
		logger.error(message);
	}

	/**
	 * Convenience wrapper for using the Fable main logger.
	 * 
	 * @param message
	 * @param t
	 */
	public static void error(String message, Throwable t) {
		if (logger == null) getLogger();
		logger.error(message, t);
	}

}
