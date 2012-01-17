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

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import fable.framework.logging.FableLogger;

/**
 * FableUtils is a class with error handling and possibly other methods that are
 * specific to Fable and are designed for easy use and to provide a consistent
 * user experience.
 * 
 * @author evans
 * 
 */
public class FableUtils {
	public static final String LS = System.getProperty("line.separator");

	/**
	 * Generates a message with an ID based on the Object. The ID is determined
	 * based on the class of the object:<br>
	 * <br>
	 * String: Use the string<br>
	 * Class: Use the name of the class<br>
	 * Otherwise: Use the name of the class of the object<br>
	 * <br>
	 * The expected usage is to pass the <b>this</b> keyword. Note that
	 * anonymous inner classes will have a number appended to the class name.
	 * You can use MyClass.<b>class</b> for static classes.
	 * 
	 * @param object
	 *            The Object from which the ID is generated. If the object is
	 *            null, then the msg is returned.
	 * @param msg
	 *            The text part of the message. May be null in which case just
	 *            the ID is returned.
	 * @return The returned string of the form "ID: msg", e.g.<br>
	 *         "fable.imageviewer.views.ImageView: Invalid operation" If msg is
	 *         null, then just the ID without the colon is returned.
	 */
	public static String generateIDMessage(Object object, String msg) {
		if (object == null)
			return msg;
		String id;
		if (object instanceof String) {
			// If it is a String, use the String
			id = (String) object;
		} else if (object instanceof Class<?>) {
			// If it is a class, use its name
			try {
				id = ((Class<?>) object).getName();
			} catch (Throwable t) {
				id = object.toString();
			}
		} else {
			// Use the name of its class
			try {
				id = object.getClass().getName();
			} catch (Throwable t) {
				id = object.toString();
			}
		}
		if (id.length() == 0) {
			return msg;
		}
		if (msg == null) {
			return id;
		}
		return id + ": " + msg;
	}

	/**
	 * Displays an ExceptionMessageDialog using asyncExec and writes an error
	 * entry to the Fable log using the throwable message and stack trace. Note
	 * that the ExceptionMessageDialog also includes the stack trace. This
	 * method provides the most information of the three methods associated with
	 * exceptions.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display. Information from the throwable will be
	 *            appended.
	 * @see #generateIDMessage
	 * @see fable.framework.toolbox.ExceptionMessageDialog
	 * @see fable.framework.toolbox.FableUtils#excMsg
	 * @see fable.framework.toolbox.FableUtils#excNoTraceMsg
	 */
	public static void excTraceMsg(Object id, final String msg,
			final Throwable t) {
		final String throwableName;
		if (t instanceof Error) {
			throwableName = "Error";
		} else {
			throwableName = "Exception";
		}
		final String fullMsg = msg + LS + LS + throwableName + ": " + t + LS
				+ "Message: " + t.getMessage();
		FableLogger.error(generateIDMessage(id, fullMsg), t);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ExceptionMessageDialog.openException(null, throwableName, msg,
						t);
			}
		});
	}

	/**
	 * Displays an error MessageDialog using asyncExec and writes an error entry
	 * to the Fable log using the throwable message and stack trace. This method
	 * provides the middle amount of information of the three methods associated
	 * with exceptions.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display. Information from the throwable will be
	 *            appended.
	 * @see #generateIDMessage
	 * @see fable.framework.toolbox.ExceptionMessageDialog
	 * @see fable.framework.toolbox.FableUtils#excTraceMsg
	 * @see fable.framework.toolbox.FableUtils#excNoTraceMsg
	 */
	public static void excMsg(Object id, String msg, Throwable t) {
		final String throwableName;
		if (t instanceof Error) {
			throwableName = "Error";
		} else {
			throwableName = "Exception";
		}
		final String fullMsg = msg + LS + LS + throwableName + ": " + t + LS
				+ "Message: " + t.getMessage();
		FableLogger.error(generateIDMessage(id, fullMsg), t);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(null, throwableName, fullMsg);
			}
		});
	}

	/**
	 * Displays an error MessageDialog using asyncExec and writes an error entry
	 * to the Fable log using the throwable message but not the stack trace.
	 * This method provides the least amount of information of the three methods
	 * associated with exceptions.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display. Information from the throwable will be
	 *            appended.
	 * @see #generateIDMessage
	 * @see fable.framework.toolbox.ExceptionMessageDialog
	 * @see fable.framework.toolbox.FableUtils#excMsg
	 * @see fable.framework.toolbox.FableUtils#excTraceMsg
	 */
	public static void excNoTraceMsg(Object id, String msg, Throwable t) {
		final String throwableName;
		if (t instanceof Error) {
			throwableName = "Error";
		} else {
			throwableName = "Exception";
		}
		final String fullMsg = msg + LS + LS + throwableName + ": " + t + LS
				+ "Message: " + t.getMessage();
		FableLogger.error(generateIDMessage(id, fullMsg));
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(null, throwableName, fullMsg);
			}
		});
	}

	/**
	 * Displays an error MessageDialog using asyncExec and writes an error entry
	 * to the Fable log.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display.
	 * @see #generateID(Object)
	 */
	public static void errMsg(Object id, final String msg) {
		FableLogger.error(generateIDMessage(id, msg));
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(null, "Error", msg);
			}
		});
	}

	/**
	 * Displays a warning MessageDialog using asyncExec and writes a warning
	 * entry to the Fable log.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display.
	 * @see #generateIDMessage
	 */
	public static void warnMsg(Object id, final String msg) {
		FableLogger.warn(generateIDMessage(id, msg));
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(null, "Warning", msg);
			}
		});
	}

	/**
	 * Displays an information MessageDialog using asyncExec and writes an info
	 * entry to the Fable log.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display.
	 * @see #generateIDMessage
	 */
	public static void infoMsg(Object id, final String msg) {
		FableLogger.info(generateIDMessage(id, msg));
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, "Info", msg);
			}
		});
	}

	/**
	 * Displays an confirm MessageDialog.
	 * 
	 * @param id
	 *            An object used to generate an ID for the logging message. Will
	 *            not be used in the MessageDialog. May be null for no ID.
	 * @param msg
	 *            The message to display.
	 * @see #generateIDMessage
	 * @see fable.framework.toolbox.ExceptionMessageDialog
	 * @return true if user confirms its decision
	 * @author SUCHET
	 */
	public static boolean confirmMsg(Object id, final String msg) {
		boolean confirm = false;
		// if (false) {
		// // KE: I don't think we really want to log this sort of thing. It
		// // would be better to log when the file is actually written. In any
		// // event a warning level is not appropriate.
		// FableLogger.warn(generateIDMessage(id, msg));
		// }
		confirm = MessageDialog.openConfirm(null, "Confirm", msg);
		return confirm;
	}

	/**
	 * Get the short part of a full file name.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * @return
	 */
	public static String getShortName(String fileName) {
		File file = new File(fileName);
		if (file == null)
			return null;
		return file.getName();
	}

	/**
	 * Get the extension of a File.
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(File file) {
		return getExtension(file.getName());
	}

	/**
	 * Get the extension of a file name.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * @return
	 */
	public static String getExtension(String fileName) {
		String ext = null;
		int i = fileName.lastIndexOf('.');
		if (i > 0 && i < fileName.length() - 1) {
			ext = fileName.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Changes the extension of a file name or adds the extension if the file
	 * name does not have one.
	 * 
	 * @param fileName
	 * @param newExtension
	 * @return
	 */
	public static String changeFileExtension(String fileName,
			String newExtension) {
		if (fileName == null || newExtension == null) {
			return null;
		}
		int lastDot = fileName.lastIndexOf(".");
		if (lastDot != -1) {
			return fileName.substring(0, lastDot) + newExtension;
		} else {
			return fileName + newExtension;
		}
	}

	/**
	 * Displays an question MessageDialog.
	 * 
	 * @param message
	 *            The question to ask.
	 * @return 0 if the user presses OK button, 1 for Cancel, 2 for No.
	 */
	public static int questionMsg(final String message) {
		String[] buttons = { "OK", "Cancel", "No" };
		MessageDialog dialog = new MessageDialog(null, "Question", null,
				message, MessageDialog.QUESTION, buttons, 0);
		return dialog.open();
	}
}
