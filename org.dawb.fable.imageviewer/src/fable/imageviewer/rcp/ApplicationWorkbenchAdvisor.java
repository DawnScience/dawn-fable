/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.rcp;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui
	 * .application.IWorkbenchConfigurer)
	 */
	@Override
	// KE: Doesn't seem to get called
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		/*
		 * do not save the workbench layout - it causes too many hassles with
		 * the multiple image views (because I cannot find a way to determine
		 * their secondary id's at runtime) and it is generally confusing. It
		 * could be a preference later on
		 * 
		 * configurer.setSaveAndRestore(true);
		 */
	}

	private static final String PERSPECTIVE_ID = "fable.imageviewer.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	// KE: Tried to implement preStutdown to close views with secondary IDs that
	// are not 0 here, but it didn't seem to get called.

}
