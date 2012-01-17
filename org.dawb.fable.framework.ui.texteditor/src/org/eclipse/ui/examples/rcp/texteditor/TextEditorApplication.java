/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.texteditor;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class TextEditorApplication implements IPlatformRunnable {

	public Object run(Object args) {
		WorkbenchAdvisor workbenchAdvisor = new TextEditorWorkbenchAdvisor();
		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					workbenchAdvisor);
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IPlatformRunnable.EXIT_RESTART;
			return IPlatformRunnable.EXIT_OK;
		} finally {
			display.dispose();
		}
	}
}
