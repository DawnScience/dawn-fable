/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.console.ConsolePlugin;
import org.slf4j.Logger;

import fable.framework.logging.FableLogger;
import fable.framework.toolbox.FableUtils;
import fable.framework.views.FableMessageConsole;

/**
 * This is a class to manage a general-purpose test menu. It is expected to be
 * used for debug purposes and experimentation. Thus it is likely to modified on
 * a regular basis and should not be considered a permanent fixture.
 * 
 * Comment Matthew Gerring 20/12/2010 - This menu is no longer needed as 
 * LogBack view will be used.
 * 
 * @author evans
 * 
 */
public class TestMenu {
	/**
	 * List of methods that create actions for the menu. Comment out the ones
	 * you do not want. The methods must have no arguments and return an Action.
	 * They must be implemented in this class.
	 */
	private static final String[] actionMethods = {
			// "createConsoleTestAction",
			"createExceptionTestAction", "createLoggingTestAction",
			"createLoggingLevelTestAction", };

	/**
	 * Creates the menu.
	 * 
	 * @return
	 */
	static MenuManager createTestMenu() {
		MenuManager testMenu = new MenuManager("Test");

		// Loop over the Actions in actionMethods
		Method method;
		Action action;
		for (int i = 0; i < actionMethods.length; i++) {
			try {
				method = TestMenu.class.getDeclaredMethod(actionMethods[i],
						(Class[]) null);
				action = (Action) method.invoke(null, (Object[]) null);
				testMenu.add(action);
			} catch (Exception ex) {
				// Do nothing
			}
		}
		// Return something only if there are some items in the menu
		if (testMenu.getItems().length > 0) {
			return testMenu;
		} else {
			return null;
		}
	}

	/**
	 * An action to test consoles.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	// Compiler can't tell but it actually is used
	private static Action createConsoleTestAction() {
		// IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager()
		// .getConsoles();
		System.out
				.println("getConsoles().length: "
						+ ConsolePlugin.getDefault().getConsoleManager()
								.getConsoles().length);
		System.out.println("Exist: FableMessageConsole.console: "
				+ FableMessageConsole.console);
		// System.out.println("Exist: console: " + console);

		final FableMessageConsole console1 = (FableMessageConsole) ConsolePlugin
				.getDefault().getConsoleManager().getConsoles()[0];
		System.out.println("getConsoles[0]: console1: " + console1);
		final FableMessageConsole console2 = new FableMessageConsole(
				"Console 2");
		System.out.println("Created: console2: " + console2);
		System.out
				.println("getConsoles().length: "
						+ ConsolePlugin.getDefault().getConsoleManager()
								.getConsoles().length);
		System.out.println();
		Action action = new Action("Test Console") {
			@Override
			public void run() {
				String msg = "FableMessageConsole: "
						+ FableMessageConsole.console;
				System.out.println(msg);
				FableMessageConsole.console.displayInfo(msg);
				msg = "console1: " + console1;
				System.out.println(msg);
				console1.displayInfo(msg);
				msg = "console2: " + console2;
				System.out.println(msg);
				console2.displayInfo(msg);
			}
		};
		return action;
	}

	/**
	 * An action to test exception messages.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	// Compiler can't tell but it actually is used
	private static Action createExceptionTestAction() {
		Action action = new Action("Test Exception Messages") {
			@Override
			public void run() {
				try {
					Double.parseDouble("garbage");
				} catch (NumberFormatException ex) {
					// if (false) {
					// SWTUtils.excTraceMsgAsync(
					// "This operation has encountered an error", ex);
					// SWTUtils.excMsgAsync(
					// "This operation has encountered an error", ex);
					// SWTUtils
					// .errMsgAsync("This operation has encountered an error"
					// + "\n" + ex + "\n" + ex.getMessage());
					// }
					FableUtils.excTraceMsg(this,
							"This operation has used FableUtils.excTraceMsg",
							ex);
					FableUtils.excMsg(this,
							"This operation has used FableUtils.excMsg", ex);
					FableUtils.errMsg(this.getClass(),
							"This operation has used FableUtils.errMsg");
					FableUtils.warnMsg(TestMenu.class,
							"This operation has used FableUtils.warnMsg");
					FableUtils.infoMsg("XXX",
							"This operation has used FableUtils.infoMsg");
				}
			}
		};
		return action;
	}

	/**
	 * An action to test logging.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	// Compiler can't tell but it actually is used
	private static Action createLoggingTestAction() {
		Action action = new Action("Test Logging") {
			public void run() {
				// Can't use FableLoggerTest here as it isn't exported

				// Main logger
				Logger logger = FableLogger.getLogger();
				System.out.println("\nFableLogger.getLogger()");
				URL url = logger.getClass().getResource("/log4j.properties");
				printLoggerInfo(logger);
				System.out.println("Properties URL=" + url);
				System.out.println("  path=" + url.getPath());
				System.out.println("  file=" + url.getFile());
				System.out.println("  protocol=" + url.getProtocol());
				System.out.println("  contents=");
				try {
					InputStream is = url.openStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));
					String line = null;
					System.out.println("-------------------------------------");
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
					System.out.println("-------------------------------------");
				} catch (IOException ex) {
					System.err.println("Cannot open input stream for " + url);
				}
				String[] props = { "log4j.rootLogger",
						"log4j.appender.default",
						"log4j.appender.default.layout",
						"log4j.appender.default.layout.ConversionPattern" };
				String val;
				for (int i = 0; i < props.length; i++) {
					val = System.getProperty(props[i], "Not found");
					System.out.println(props[i] + "=" + val);
				}

				System.out.println("\nTests [5 total messages]");
				logger.trace("Trace Message");
				logger.debug("Debug Message");
				logger.info("Info Message");
				logger.warn("Warning Message");
				logger.error("Error Message");

				// Class logger
				System.out.printf("\nClass Logger (%s)\n", this.getClass()
						.toString());
				// logger = FableLogger.getLogger(TestMenu.class);
				logger = FableLogger.getLogger(this.getClass());
				printLoggerInfo(logger);

				System.out.println("\nTests [5 total messages]");
				logger.trace("Trace Message");
				logger.debug("Debug Message");
				logger.info("Info Message");
				logger.warn("Warning Message");
				logger.error("Error Message");

				// Root logger
				//logger = Logger.getRootLogger();
				//System.out.printf("\nRoot Logger\n");
				//printLoggerInfo(logger);
			}

			private void printLoggerInfo(Logger logger) {
//				System.out.println("logger=" + logger);
//				System.out.println("name=" + logger.getName());
//				//System.out.println("level=" + logger.getLevel());
//				//System.out.println("effectiveLevel="
//				//		+ logger.getEffectiveLevel());
//				System.out.println("Appenders for " + logger.getName());
//				Enumeration<?> e = logger.
//				Appender appender;
//				Layout layout;
//				while (e.hasMoreElements()) {
//					appender = (Appender) e.nextElement();
//					System.out.println("  name=" + appender.getName());
//					System.out.println("  class=" + appender.getClass());
//					layout = appender.getLayout();
//					System.out.println("    layout class=" + layout.getClass());
//					System.out.println("    layout content type="
//							+ layout.getContentType());
//					System.out.println("    layout header="
//							+ layout.getHeader());
//					System.out.println("    layout footer="
//							+ layout.getFooter());
//					if (layout instanceof PatternLayout) {
//						System.out.println("    layout conversion pattern="
//								+ ((PatternLayout) layout)
//										.getConversionPattern());
//					}
//				}
//
//				// Get all loggers
//				System.out.println("Repository");
//				LoggerRepository repos = logger.getLoggerRepository();
//				System.out.println("threshold=" + repos.getThreshold());
//				System.out.println("Current loggers");
//				e = repos.getCurrentLoggers();
//				Logger l;
//				while (e.hasMoreElements()) {
//					l = (Logger) e.nextElement();
//					System.out.println("  " + l.getName());
//				}
			}
		};
		return action;
	}

	/**
	 * An action to test logging.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	// Compiler can't tell but it actually is used
	private static Action createLoggingLevelTestAction() {
		Action action = new Action("Test Logging Level") {
			public void run() {
				// Main logger
				Logger logger = FableLogger.getLogger();
				System.out.println("\nFableLogger.getLogger()");
				System.out.println("logger=" + logger);
				System.out.println("name=" + logger.getName());
				//System.out.println("level=" + logger.getLevel());
				//System.out.println("effectiveLevel="
				//		+ logger.getEffectiveLevel());

//				logger = Logger.getRootLogger();
//				System.out.println("\nLogger.getRootLogger()");
//				System.out.println("logger=" + logger);
//				System.out.println("name=" + logger.getName());
//				System.out.println("level=" + logger.getLevel());
//				System.out.println("effectiveLevel="
//						+ logger.getEffectiveLevel());

				System.out.println("\nException test");
				try {
					Double.parseDouble("garbage");
				} catch (NumberFormatException ex) {
					FableLogger.error("Intentional exception generated", ex);
				}

				System.out.println("\nFable Logger Tests [6 total messages]");
				FableLogger.trace("Fable Logger Trace Message");
				FableLogger.debug("Fable Logger Debug Message");
				FableLogger.info("Fable Logger Info Message");
				FableLogger.warn("Fable Logger Warning Message");
				FableLogger.error("Fable Logger Error Message");

				System.out.println("\nRoot Logger Tests [6 total messages]");
				logger.trace("Root Logger Trace Message");
				logger.debug("Root Logger Debug Message");
				logger.info("Root Logger Info Message");
				logger.warn("Root Logger Warning Message");
				logger.error("Root Logger Error Message");
			}
		};
		return action;
	}

}
