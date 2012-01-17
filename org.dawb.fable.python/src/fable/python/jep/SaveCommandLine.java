/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.jep;

import java.io.FileWriter;
import java.util.concurrent.Semaphore;

/**
 * This class save a command line in a text file for instance
 * 
 * @author SUCHET
 * 
 */
public class SaveCommandLine {
	FileWriter fileWriter;
	private Semaphore semaphore = new Semaphore(1);

	public SaveCommandLine(final String fileName, final String commandLine) {

		/*
		 * Thread t=new Thread(new Runnable(){ public void run() {
		 * 
		 * 
		 * try{ fileWriter = new FileWriter(fileName, true);
		 * if(fileWriter==null){ fileWriter=new FileWriter(fileName); }
		 * fileWriter.write(commandLine,0,commandLine.length());
		 * }catch(IOException ex){ ex.printStackTrace();
		 * 
		 * }finally{ if(fileWriter != null){ try { fileWriter.close(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace();
		 * 
		 * } } }
		 * 
		 * 
		 * }});
		 * 
		 * t.start(); try { t.wait(); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	/************************************** THREAD ACCESS ***********************/
	public synchronized void acquire() {
		semaphore.acquireUninterruptibly();
	}

	public synchronized void release() {
		semaphore.release();
	}
}
