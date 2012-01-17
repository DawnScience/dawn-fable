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

/**
 * This class is used to add static methods for plugins, i.e. get plugin path
 * 
 * @author SUCHET
 * 
 */
public class PluginUtils {
	/**
	 * 
	 * @param pluginName
	 * @param bundle
	 * @return pluginName directory
	 */
	// TODO
	/*
	 * public static String getPluginDirectory(String pluginName, Bundle
	 * bundle){
	 * 
	 * Path path = new Path("/"); String pluginPath ="''"; URL xmliniFile; try {
	 * xmliniFile = Platform.resolve(FileLocator.find(bundle, path, null));
	 * //System.out.println("spdURL:" + spdURL.getPath());
	 * 
	 * //URL spdURL = Platform.resolve(FileLocator.find(bundle, path, null));
	 * pluginPath = xmliniFile.getPath(); pluginPath = pluginPath.substring(0,
	 * pluginPath.lastIndexOf(pluginName)); //String os =
	 * System.getProperty("os.name"); String sep=
	 * System.getProperty("file.separator"); // if file: at beginning then
	 * remove it and assume we are in the //runtime case i.e. version part of
	 * plugin directory
	 * 
	 * if(System.getProperty("os.name").toLowerCase().contains("window")){
	 * pluginPath = pluginPath.replaceFirst("/", "");
	 * 
	 * }
	 * 
	 * } catch (IOException e) {
	 * 
	 * e.printStackTrace(); }
	 * 
	 * return pluginPath + pluginName; }
	 */

}
