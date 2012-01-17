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

import java.util.HashMap;

import jep.Jep;
import jep.JepException;

import org.dawb.fabio.FableJep;
import org.junit.Test;


public class JepTransformTest {
	private Jep j;

	@Test
	public final void testJepTransform() {
		try {
			j = FableJep.getFableJep().getJep();
			getDefaultPythonOptions();
			testTransfertDictionnary();
			// run();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("could not run transformer.py");
		}
	}

	private void getDefaultPythonOptions() throws Throwable {
		// Fill out the defaults
		// Relies on the methods in ImageD11.peaksearcher
		FableJep.getFableJep().jepImportSpecificDefinition("ImageD11", "transformer");
		j.eval("t=transformer.transformer()");
		j
				.set(
						"filename",
						"C:\\Documents and Settings\\suchet.ESRF\\workspace\\fable.test.data\\data\\imaged11.transform\\jon_mx666_HEWL_7_crystalls_4.par");
		j.eval("t.loadfileparameters(filename)");
	}

	public void testTransfertDictionnary() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("titi", 10);
		map.put("toto", 100);

		try {
			fable.python.jep.JavaToJepTools.javaHashMapToPyDictionary(j, map);
		} catch (JepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			System.out.println(j.eval("dictionary"));
		} catch (JepException e) {

			e.printStackTrace();
		}
	}

	private void transferOptionsJavaToPython() throws JepException {
		// Copy the choices across, following types from xml file

		System.out.println(j.eval("t.pars"));
		// System.out.println("Initial value wedge=" +
		// j.getValue("t.parameterobj.parameters['wedge']"));

		j.eval("dictionary={\"wedge\": 10, \"o12\":9}");
		System.out.println(j.eval("t.parameterobj.set_parameters(dictionary)"));
		// System.out.println(j.eval("t.parameterobj.parameters['wedge']=10"));
		// System.out.println("After update wedge= "
		// +j.getValue("t.parameterobj.parameters['wedge']"));
		System.out.println(j.eval("t.pars"));
	}

	public void run() throws JepException {
		transferOptionsJavaToPython();
	}
}
