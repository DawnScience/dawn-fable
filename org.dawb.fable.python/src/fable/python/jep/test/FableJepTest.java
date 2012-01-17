/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.jep.test;

import static org.junit.Assert.fail;
import jep.Jep;
import jep.JepException;

import org.dawb.fabio.FableJep;
import org.junit.Test;

import fable.python.jep.StdoutRead;

public class FableJepTest {

	@Test
	public final void testGetJep() {
		Jep jep = null;
		try {
			jep = FableJep.getFableJep().getJep();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (jep == null) {
			fail("could not get Jep object");
		}
	}

	@Test
	public final void testJepEval() {
		Jep jep = null;
		try {
			jep = FableJep.getFableJep().getJep();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (jep == null) {
			fail("could not get Jep object");
		}
		try {
			jep.eval("print 'hello world'");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * test redirecting of stdout by FableJep
	 * 
	 */
	@Test
	public final void testRedirectStdout() {
		System.out
				.println("testRedirectStdout(): test redirection of stdout ...");
		FableJep jep = null;
		try {
			jep = FableJep.getFableJep();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (jep == null) {
			fail("could not get Jep object");
		}
		try {
			jep.redirectStdout("/tmp/stdout.test");
			long printtime = System.currentTimeMillis();
			jep.eval("print 'hello world 1';print 'hello world 2'");
			StdoutRead stdoutThread = new StdoutRead("/tmp/stdout.test");
			stdoutThread.setOutputToSystemOut(true);
			stdoutThread.setOutputToFableConsole(false);
			stdoutThread.start();
			jep.eval("sys.stdout.flush()");
			jep.eval("import sys");
			jep.eval("import time");
			jep.eval("time.sleep(10)");
			System.out.println("print was received "
					+ (stdoutThread.lasttime - printtime)
					+ " millisecond(s) later");
			/*
			 * success will depend on when the message was received by the
			 * stdout reader thread
			 */
			if (Math.abs(stdoutThread.lasttime - printtime) > 1000) {
				fail("print was received at least "
						+ (printtime - stdoutThread.lasttime) / 1000
						+ " seconds late !");
			}
			stdoutThread.lasttime = 0;
			jep.eval("print 'goodbye world 1';print 'goodbye world 2'");
			printtime = System.currentTimeMillis();
			jep.eval("sys.stdout.flush()");
			jep.eval("time.sleep(10)");
			System.out.println("print was received "
					+ (printtime - stdoutThread.lasttime) / 1000
					+ " seconds later");
			/*
			 * success will depend on when the message was received by the
			 * stdout reader thread
			 */
			if (Math.abs(stdoutThread.lasttime - printtime) > 1000) {
				fail("print was received at least "
						+ (stdoutThread.lasttime - printtime)
						+ " millisecond(s) late !");
			}
			stdoutThread.stopped = true;
			stdoutThread.join();
		} catch (JepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public final void testSysArgv() {
		System.out.println("testSysArgv(): test sys.argv is set to jep ...");
		FableJep jep = null;
		try {
			jep = FableJep.getFableJep();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (jep == null) {
			fail("could not get Jep object");
		}
		try {
			jep.eval("print sys.argv");
			String sysargv = jep.getStringValue("sys.argv[0]");
			System.out.println("sys.argv[0] = " + sysargv);
			if (!sysargv.equalsIgnoreCase("jep")) {
				fail("sys.argv[0] != jep");
			}
		} catch (JepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
