/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.object;

import java.io.IOException;

import fable.framework.toolbox.StreamReaderThread;
import fable.framework.views.FableMessageConsole;

public class RunProgram implements Runnable {

	//private Runtime runtime;
	private String command;
	
	public RunProgram(String command) {
	 this.command = command;
	}

	//@Override
	public void run(){
		Runtime runTime = Runtime.getRuntime();
		
	
		try {
			Process process = runTime.exec(this.command);
			if(FableMessageConsole.console != null){
				FableMessageConsole.console.activate();
				FableMessageConsole.console.displayInfo("You are launching " + 
						this.command + " ");
			}
			StreamReaderThread outThread = 
				new StreamReaderThread(process.getInputStream());
			//create thread for reading errorStream (process' stderr)
			StreamReaderThread errThread = 
				new StreamReaderThread(process.getErrorStream());
			//start both threads
			outThread.start();
			errThread.start();

			//wait for process to end
			try {
				process.waitFor();
				//outThread.join();
				//errThread.join();
			} catch (InterruptedException e) {
				if(FableMessageConsole.console != null){
					FableMessageConsole.console.activate();
					FableMessageConsole.console.displayError("An error occured " + 
							" with :" + this.command + " ");
					FableMessageConsole.console.displayError(e.getMessage());
				}
			}
			
		} catch (IOException e) {
			if(FableMessageConsole.console != null){
				FableMessageConsole.console.activate();
				FableMessageConsole.console.displayError("An error occured " + 
						" with :" + this.command + " ");
				FableMessageConsole.console.displayError(e.getMessage());
			}
		}
	}
		
}
