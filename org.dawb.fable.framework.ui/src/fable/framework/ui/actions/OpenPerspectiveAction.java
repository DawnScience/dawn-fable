/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.actions;

import java.util.Properties;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

public class OpenPerspectiveAction implements IIntroAction {
	public void run(final IIntroSite site, final Properties params) {
		final String perspectiveID = params.getProperty(
				"perspectiveId"); //$NON-NLS-1$
		if (perspectiveID != null) {
			try {
				// Close the intro view ...
				final IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
				PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
				// Open the perspective ....
				final IWorkbench wb = PlatformUI.getWorkbench();
				wb.showPerspective(perspectiveID,wb.getActiveWorkbenchWindow());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
