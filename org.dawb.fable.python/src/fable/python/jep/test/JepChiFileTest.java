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
import jep.JepException;

import org.junit.Test;

import fable.python.ChiFile;

public class JepChiFileTest {

	@Test
	public void testChiReader() {
		try {
			// KE: This is not used
			// Jep jep = new FableJep().getJep();
			ChiFile chifile = new ChiFile();
			chifile
					.loadfile("C:\\Test_files\\z31_comp_extrax1\\az31_comp_extrax10001.chi");
			chifile.getlist();

		} catch (JepException e) {
			e.printStackTrace();
			fail("could not run chi_reader.py : " + e.getMessage());
		}
	}
}
