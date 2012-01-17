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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import fable.framework.views.FableMessageConsole;

/**
 * Redirect a file to System.out and/or FableMessageConsole. This class is used
 * to listen to python stdout. A current console must be created as output
 * message are redirect in FableMessageConsole. The class has setter methods to
 * select outputting to System.out and/or FableMessageConsole.
 * 
 * @author GOETZ SUCHET
 * 
 */

public class StdoutRead extends Thread {

    String outputMessage;

    BufferedReader outputread;
    public boolean stopped = false;
    private boolean outputSystemOut = false;
    private boolean outputFableConsole = true;
    public long lasttime;
    private String file = "";

    public StdoutRead(String filename) {
        try {
            lasttime = 0;
            file = filename;
            outputread = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.out.println("failed to open file " + file + " because "
                    + e.getMessage());
        }
    }

    public void run() {

        ///while (!stopped) {

        while (!stopped || outputMessage != null) {
            try {
                outputMessage = outputread.readLine();
                while (outputMessage == null && !stopped) {
                    /* give the cpu a break ! */
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    	// FIXME
                        e.printStackTrace();
                    }
                    outputMessage = outputread.readLine();
                }
                if (outputMessage != null) {
                    lasttime = System.currentTimeMillis();
                    if (outputSystemOut)
                        System.out.println(outputMessage);
                    if (outputFableConsole) {
                        if (!Display.getDefault().isDisposed()) {
                            Display.getDefault().syncExec(new Runnable() {
                                public void run() {
                                    if (FableMessageConsole.console != null) {
                                        FableMessageConsole.console
                                                .displayOut(outputMessage);
                                    } else {
                                        System.out.println(outputMessage);
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (IOException e) {
                if (FableMessageConsole.console != null) {
                    FableMessageConsole.console.displayIn(outputMessage);
                    FableMessageConsole.console.displayIn(e.getMessage());
                } else {
                    System.out.println(outputMessage);
                    System.out.println(e.getMessage());
                }
            }
        }
       
    }

    /**
     * set the flag to output to System.out to true or false
     * 
     * @param value
     *            - true (output to System.out) or false (do not output to
     *            System.out)
     */
    public void setOutputToSystemOut(boolean value) {
        outputSystemOut = value;
    }

    /**
     * set the flag to output to System.out to true or false
     * 
     * @param value
     *            - true (output to FableMessageConsole) or false (do not output
     *            to FableMessageConsole)
     */
    public void setOutputToFableConsole(boolean value) {
        outputFableConsole = value;
    }

}
