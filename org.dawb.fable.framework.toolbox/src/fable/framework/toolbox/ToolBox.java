/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * fable.preprocessor 
 * fable.preprocessor.toolBox
 * Feb 7, 2007
 */
package fable.framework.toolbox;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import fable.framework.toolboxpreferences.ConfigurationPreferencesPage;

/**
 * @author G. Suchet fable.preprocessor Feb 7, 2007
 * 
 */
public class ToolBox {

	/**
	 * This static method returns bundle path.
	 * 
	 * @return bundle path
	 * @throws IOException
	 *             if bundle is not found.
	 */
	public static String getPluginPath(String bundleName, String PluginId)
			throws IOException {
		Bundle bundle = Platform.getBundle(PluginId);
		// Path path = new Path("/");
		IPath xmliniFile = Platform.getStateLocation(bundle);
		// .resolve(FileLocator.find(bundle, path, null));
		String bundlePath = xmliniFile.toString();
		int lastIndex = bundlePath.lastIndexOf(bundleName);
		if (lastIndex >= 0 && lastIndex < bundlePath.length()) {
			bundlePath = bundlePath.substring(0, lastIndex);
		}
		return bundlePath;
	}

	/**
	 * @description check if a file exists
	 * @param the
	 *            name of the file
	 * @return true if the file exists
	 * */
	public static boolean checkIfFileExists(String fileName) {
		boolean bFileSet = false;
		if (fileName != null && !fileName.equals("")) {
			File myFile = new File(fileName);

			if (myFile.exists()) {
				bFileSet = true;
			}
		} else {
		//Gaelle 05/03/2009: comment this	bFileSet = true;
		}
		return bFileSet;
	}

	/**
	 * @description check file extension
	 * @param filename
	 *            the name of the file
	 * @param extension
	 *            extension to check
	 * */
	public static boolean checkExtension(String fileName, String extension) {

		String file = fileName.toLowerCase();
		String ext = extension.toLowerCase();
		return file.endsWith(ext);

	}

	/**
	 * @description check if the path is a directory
	 * 
	 * 
	 * */
	public static boolean checkIfIsDirectory(String path) {
		boolean bok = false;
		File dir = new File(path);
		if (dir.isDirectory()) {
			bok = true;
		}
		return bok;
	}

	/**
	 * @description return all files in a directory with defined extension
	 */
	public static String[] getFilesFromDirectory(String directoryPath,
			String extension) {

		File dir = new File(directoryPath);

		final String ext = extension.toLowerCase();
		FilenameFilter myFilter = new FilenameFilter() {

			public boolean accept(File directory, String name) {

				File dirfilter = new File(directory, name);
				if (dirfilter.isDirectory()) {

					return false;
				}
				name = name.toLowerCase();

				return name.endsWith(ext);

			}

		};

		return dir.list(myFilter);
	}

	/*
	 * @author Suchet
	 * 
	 * @date 2007-03-26
	 * 
	 * @description sort a list of files based on the Absolute path
	 */
	public static void quicksort(File[] list, int begin, int end) {
		if (end > begin) {
			int indexPivot = partition(list, begin, end);
			quicksort(list, begin, indexPivot);
			quicksort(list, indexPivot + 1, end);
		}
	}

	private static int partition(File[] list, int begin, int end) {
		int i;
		int indexPivot = begin + ((end - begin) / 2);
		File valuePivot = list[indexPivot];

		int k = begin;
		File temp;
		for (i = begin; i < end; i++) {
			if (list[i].getAbsolutePath().compareTo(
					valuePivot.getAbsolutePath()) < 0) {
				temp = list[i];
				list[i] = list[k];
				list[k] = temp;
				if (k == indexPivot) {
					indexPivot = i;
				}
				k++;

			}
		}

		if (k < end) {

			temp = list[k];
			list[k] = valuePivot;
			list[indexPivot] = temp;
		}

		return k;
	}

	/*
	 * @author Suchet
	 * 
	 * @date 2007-03-26
	 * 
	 * @description sort a list of strings
	 */
	public static void quicksort(String[] list, int begin, int end) {
		if (end > begin) {
			int indexPivot = partition(list, begin, end);
			quicksort(list, begin, indexPivot);
			quicksort(list, indexPivot + 1, end);
		}
	}

	private static int partition(String[] list, int begin, int end) {
		int i;
		int indexPivot = begin + ((end - begin) / 2);
		String valuePivot = list[indexPivot];
		int k = begin;
		String temp;
		for (i = begin; i < end; i++) {
			if (list[i].compareTo(valuePivot) < 0) {
				temp = list[i];
				list[i] = list[k];
				list[k] = temp;
				if (k == indexPivot) {
					indexPivot = i;
				}
				k++;

			}
		}

		if (k < end) {

			temp = list[k];
			list[k] = valuePivot;
			list[indexPivot] = temp;
		}

		return k;
	}

	/**
	 * @author Suchet
	 * @date March 26 2007
	 * @return a list of file name (no pathName)
	 * @description this function can be used to get all files that match a
	 *              regular expression if be retrieve is set to true ; if not,
	 *              get all files that do not contains this regex
	 * */
	public static File[] getFilesByEntireRegex(String directoryPath,
			String regex, boolean bRetreive) {

		File dir = new File(directoryPath);
		final String exp = regex;
		final boolean bGet = bRetreive;
		try {

			final Pattern pattern = Pattern.compile(exp);

			FilenameFilter myFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					File dirfilter = new File(dir, name);
					if (dirfilter.isDirectory()) {

						return false;
					}

					return pattern.matcher(dirfilter.getName()).matches() == bGet;

				}

			};

			return dir.listFiles(myFilter);

		} catch (PatternSyntaxException pe) {
			LoggerFactory.getLogger(ToolBox.class).error(pe.getMessage());
			System.out.println(pe.getMessage());
			return null;
		}
	}

	/**
	 * @author Suchet
	 * @date March 26 2007
	 * @return a list of file name (no pathName)
	 * @description this function can be used to get all files that match a
	 *              regular expression if be retrieve is set to true ; if not,
	 *              get all files that do not contains this regex
	 * */
	public static String[] getFileNamesByEntireRegex(String directoryPath,
			String regex, boolean bRetreive) {

		File dir = new File(directoryPath);
		final String exp = regex;
		final boolean bGet = bRetreive;
		try {
			final Pattern pattern = Pattern.compile(exp);

			FilenameFilter myFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					File dirfilter = new File(dir, name);
					if (dirfilter.isDirectory()) {

						return false;
					}

					return pattern.matcher(dirfilter.getName()).matches() == bGet;

				}

			};

			return dir.list(myFilter);

		} catch (PatternSyntaxException pe) {

			return null;
		}
	}

	/**
	 * @author Suchet
	 * @date March 26 2007
	 * @description this function can be used to get all files that match a
	 *              regular expression if be retreive is set to true ; if not,
	 *              get all files that do not contains this regex Added : files
	 *              created after date (for on line pre-processing)
	 * */
	public static String[] getFilesByEntireRegexAndTime(String directoryPath,
			String regex, boolean bRetreive, long date) {

		File dir = new File(directoryPath);
		final String exp = regex;
		final boolean bGet = bRetreive;
		final long f_downdate = date;
		try {
			final Pattern pattern = Pattern.compile(exp);

			FilenameFilter myFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					File dirfilter = new File(dir, name);
					if (dirfilter.isDirectory()) {

						return false;
					}
					if (f_downdate > dirfilter.lastModified()) {
						return false;
					}

					return pattern.matcher(dirfilter.getName()).matches() == bGet;
				}

			};

			return dir.list(myFilter);

		} catch (PatternSyntaxException pe) {
			return null;
		}
	}

	/**
	 * 
	 * 26 oct. 07
	 * 
	 * @author G. Suchet
	 * @return a random Color
	 */
	public static Color getRandomColor() {

		return new Color((int) (Math.random() * 256),
				(int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	/**
	 * @description Add "" for windows if file name contains spaces (c:\My
	 *              documents\myFile --> c:\"My documents"\myFile), sep="\\" for
	 *              windows, regex "[\\\\/]"
	 * @arguments String pathName,String regex "[\\\\/]", String
	 *            sep=System.getProperty("file.separator"),
	 * 
	 */
	public static String addQuotesForSpacesInName(String pathName,
			String regex, String sep) {
		String[] splitter = pathName.split(regex);
		String name = "";

		for (int i = 0; i < splitter.length - 1; i++) {

			if (splitter[i].contains(" ")) {
				splitter[i] = "\"" + splitter[i] + "\"";

			}
			name += splitter[i] + sep;
		}
		name += splitter[splitter.length - 1];

		return name;
	}

	/**
	 * 
	 * 13 d�c. 07
	 * 
	 * @author G. Suchet
	 * @param sDate
	 * @param sFormat
	 * @return
	 * @throws Exception
	 * @description converts a string to a date
	 */
	public static Date stringToDate(String sDate, String sFormat)
			throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.parse(sDate);
	}

	/**
	 * Gets the plug-in name from its ID.
	 * 
	 * @param pluginId
	 *            The plug-in's Activator ID.
	 * @return
	 */
	public static String getPluginName(String pluginId) {
		String name = "Unknown-Name";
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null)
			return name;
		String pluginName = bundle.getSymbolicName();
		if (pluginName != null)
			name = pluginName;
		return name;
	}

	/**
	 * Gets the plug-in version from its ID.
	 * 
	 * @param pluginId
	 *            The plug-in's Activator ID.
	 * @return
	 */
	public static String getPluginVersion(String pluginId) {
		String version = "Unknown-Version";
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null)
			return version;
		Dictionary<?, ?> bundleHeaders = bundle.getHeaders();
		if (bundleHeaders == null)
			return version;
		String pluginVersion = (String) bundleHeaders.get("Bundle-Version");
		if (pluginVersion != null)
			version = pluginVersion;
		return version;
	}

	/**
	 * 
	 * @param filename
	 * @return the stem of the file name
	 */
	public static String getStem(String filename) {
		String stem = new String();
		int index = filename.indexOf(".");
		int start = 0;
		if (index > -1) {
			stem = filename.substring(0, index);
		}
		if (!ToolBox.getFileType(filename).equals("bruker")) {
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(stem);
			while (matcher.find()) {
				start = matcher.start();
			}

			stem = stem.substring(0, start);
		}

		return stem;
	}

	public static String getFileType(String fileName) {
		String regex1 = ".+\\.\\d+.*"; // bruker type
		// String regex2 = ".+\\.\\D+";
		String type = "";
		Pattern pattern1 = Pattern.compile(regex1);
		// Pattern pattern2 = Pattern.compile(regex2);
		if (pattern1.matcher(fileName).matches()) {
			type = "bruker";
		} else {
			int index = fileName.indexOf(".");
			if (index > -1 && index + 1 < fileName.length()) {
				type = fileName.substring(index + 1);
			}
		}
		return type;
	}

	public static String getFileNumber(String filename) {
		String number = new String();
		int index = filename.indexOf(".");
		int start = 0;

		if (!ToolBox.getFileType(filename).equals("bruker")) {
			number = filename.substring(0, index);

			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(number);
			while (matcher.find()) {
				start = matcher.start();
			}

			number = number.substring(start);
		} else {
			// BRUKER PART
			if (index > -1 && index + 1 < filename.length()) {
				number = filename.substring(index + 1);
				// If bruker file is compress, file number is now 0001.gz for
				// example
				if (number.contains(".")) {
					int indexCompress = number.indexOf(".");
					if (indexCompress > -1) {
						number = number.substring(0, indexCompress);
					}
				}
			}
		}

		return number;
	}

	/**
	 * This method checks if small perspective option have been set in in
	 * configuration preference page (<code>
	 * fable.framework.toolboxpreferences.ConfigurationPreferencesPage</code>
	 * ) to display a perspective designed for small screen or for large screen,
	 * first depending on the screen size, and from user preference after.
	 * <p>
	 * example usage in your plugin <code>ApplicationWorkbenchAdvisor</code>:<br>
	 * <code>
	 * 		public String getInitialWindowPerspectiveId() {<br>
				smallPerspective = ToolBox.isSmallPerspectiveSet();<br>
					if (smallPerspective) {<br>
						return PERSPECTIVE_ID_SMALLSCREEN;<br>
						}<br>
					return PERSPECTIVE_ID;
				}
		</code>
	 * 
	 * @return true if preferred perspective is "Display a perspective that is
	 *         better for small screen".
	 */
	public static boolean isSmallPerspectiveSet() {
		boolean smallPerspective = true;
		String perspective = fable.framework.toolbox.Activator.getDefault()
				.getPreferenceStore().getString(
						ConfigurationPreferencesPage.perspectiveLayout);
		if (perspective != null
				&& perspective
						.equals(ConfigurationPreferencesPage.prefLargeScreen)) {
			smallPerspective = false;
		}
		return smallPerspective;
	}

	/**
	 * Test whether operating system is linux or not
	 * 
	 * @return true if operating system is linux
	 */
	public static boolean isOsLinux() {
		return System.getProperty("os.name").toLowerCase().contains("linux");
	}

	/**
	 * Test whether operating system is windows or not
	 * 
	 * @return true if operating system is windows
	 */
	public static boolean isOsWindows() {
		return System.getProperty("os.name").toLowerCase().contains("window");
	}

	/**
	 * This method returns the appropriate key to press to have context help
	 * depending on the operating System
	 * 
	 * @return <UL>
	 *         <LI>F1 if os is windows
	 *         <Li>Ctrl+F1 if os is linux
	 *         <Li>help key on the mac
	 *         </UL>
	 */
	public static String getHelpContextTooltip() {
		if (isOsLinux()) {
			return "CTRL+F1";
		} else if (isOsWindows()) {
			return "F1";
		} else {
			return "Help";
		}
	}
}
