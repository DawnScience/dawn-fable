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

import org.eclipse.swt.graphics.Point;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.examples.rcp.texteditor.actions.TextEditorActionBarAdvisor;


public class TextEditorWorkbenchAdvisor extends WorkbenchAdvisor {
	public TextEditorWorkbenchAdvisor() {
	}

    public String getInitialWindowPerspectiveId() {
        return "org.eclipse.ui.examples.rcp.texteditor.TextEditorPerspective"; //$NON-NLS-1$
    }
    
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    	return new WorkbenchWindowAdvisor(configurer) {
			public void preWindowOpen() {
				super.preWindowOpen();
		        getWindowConfigurer().setInitialSize(new Point(600, 450));
		        getWindowConfigurer().setShowCoolBar(true);
		        getWindowConfigurer().setShowStatusLine(true);
			}
			
			public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer abConfigurer) {
				return new TextEditorActionBarAdvisor(abConfigurer);
			}
		};
    }
}
