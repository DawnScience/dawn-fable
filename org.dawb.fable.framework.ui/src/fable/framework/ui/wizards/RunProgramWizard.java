/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import fable.framework.ui.object.RunProgram;
/**
 * This class is a wizard to launch a program with an input file.
 * It can be used for Grainspotter and fitallB for example.
 */
public class RunProgramWizard extends Wizard implements INewWizard {

	/**This is the first page for the wizard. For instance, only one page.*/
	private RunProgramWizardPage runProgramPage;
	/**program to run is the name of the program to launch*/
	private String programToRun;
	/**inputfile is the ini file for the selected program.*/
	private String inputFile;
	@Override
	public boolean performFinish() {
		//Launch the program with a command
		 programToRun = runProgramPage.getProgram();
		 inputFile = runProgramPage.getInputFile();
			if (System.getProperty("os.name").toLowerCase().contains("window"))
			{
				programToRun = "cmd.exe /C " + programToRun;
			}
			programToRun += " " + inputFile;
		 new RunProgram(programToRun).run();
		return true;
	}

	//@Override
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
		// TODO Auto-generated method stub

	}
	@Override
	public void addPages() {
	
		super.addPages();
		runProgramPage = new RunProgramWizardPage("Run program");
		addPage(runProgramPage);
	}

	
	
	

}
