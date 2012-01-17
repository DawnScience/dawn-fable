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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * a class which is simply a placeholder for common methods for the fable splash
 * screens e.g. adding version number to splash screens
 * 
 * @author andy
 * 
 */
public class SplashScreen {

	/**
	 * add the specified plugin id's version number to the top of the splash
	 * screen
	 * 
	 * @param splash
	 *            - splash screen Shell
	 * @param pluginId
	 *            - plugin id to add version for
	 */
	static public void SplashAddVersion(Shell splash, String pluginId) {
		final Shell splashShell = splash;
		final String pluginName = ToolBox.getPluginName(pluginId);
		final String pluginVersion = ToolBox.getPluginVersion(pluginId);
		final Canvas canvas = new Canvas(splash, SWT.NONE);
		canvas.setBounds(0, 0, splash.getSize().x, splash.getSize().y);
		canvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				Display display = splashShell.getDisplay();
				Image image = splashShell.getBackgroundImage();
				GC gc = e.gc;
				Color brown = new Color(display, 100, 60, 0);
				Font font1 = new Font(display, "Arial", 14, SWT.NONE);
				gc.drawImage(image, 0, 0);
				gc.setForeground(brown);
				gc.setFont(font1);
				gc.drawString(pluginName + " " + pluginVersion, 30, 10, true);
				font1.dispose();
				brown.dispose();
			}
		});
	}
}
