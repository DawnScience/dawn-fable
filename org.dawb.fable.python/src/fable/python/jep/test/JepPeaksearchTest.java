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


public class JepPeaksearchTest {

	private Jep j;

	@Test
	public final void testJepPeakSearch() {
		try {
			j = FableJep.getFableJep().getJep();
			getDefaultPythonOptions();
			run();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("could not run peaksearch.py");
		}
	}

	private void getDefaultPythonOptions() throws JepException {
		// Fill out the defaults
		// Relies on the methods in ImageD11.peaksearcher
		j.eval("from ImageD11 import peaksearcher");
		j.eval("class o:\n" + "    def add_option(self, *a, **k):\n"
				+ "        setattr(self, k[\"dest\"], k[\"default\"])\n");
		j.eval("option_holder = o()");
		j.eval("peaksearcher.get_options( option_holder )");
	}

	private void transferOptionsJavaToPython() throws JepException {
		// Copy the choices across, following types from xml file
		j.eval("option_holder.stem = \"grid\""); // pass as string
		j.eval("option_holder.first = 0"); // pass as int
		j.eval("option_holder.last = 0");
		j.eval("option_holder.spline = \"frelon4m.spline\"");
		j.eval("option_holder.format = \".edf.gz\"");
		// Pass as list of floats - since "action"="append"
		// This is truncated image to compress better...
		j.eval("option_holder.thresholds = [100.0, 150.0] ");
	}

	private void run() throws JepException {
		transferOptionsJavaToPython();
		j.eval("peaksearcher.peaksearch_driver( option_holder , () )");
	}

}
