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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fable.framework.views.FableMessageConsole;

/**
 * class to create a thread and read from an InputStream
 * 
 * used by the command line processor to read the output from a command
 * 
 * @author goetz, class created here by Gaelle
 *
 */
public class StreamReaderThread extends Thread {
	String outputMessage;
	InputStream inStream;

	public StreamReaderThread(InputStream in) {
	    inStream = in;
	}
	/**
	 * This method launches selected program in page with selected input file.
	 */
	public void run() {
		
			BufferedReader outputread = new BufferedReader(
					new InputStreamReader(inStream));

	    try {
		while ((outputMessage = outputread.readLine()) != null) {
			if (FableMessageConsole.console != null) {
				FableMessageConsole.console.displayOut(
						outputMessage);
			}
		   
		  
		}
	    } catch (IOException e) {
	    	if (FableMessageConsole.console != null) {
	    		FableMessageConsole.console.displayError(outputMessage);
			}
		
	    }
	}

}
