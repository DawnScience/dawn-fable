/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.test;

import static org.junit.Assert.fail;
import jep.JepException;

import org.junit.Test;

import fable.python.ColumnFile;

public class ColumnFileTest {

	@Test
	public final void testOpenColumnFile() {
		ColumnFile columnFile = null;
		long elapsed = System.currentTimeMillis();
		try {
			columnFile = new ColumnFile("data/columnfile/peaks_t10000.flt");
		} catch (Throwable e) {
			e.printStackTrace();
			fail("could not open column file peaks_t10000.flt");
		}
		if (columnFile == null) {
			fail("column file null");
		}
		columnFile.loadRows();
		elapsed = System.currentTimeMillis() - elapsed;
		System.out.println("peaks_t10000.flt columnfile rows loaded in "
				+ elapsed / 1000 + " seconds");
		elapsed = System.currentTimeMillis();
		try {
			columnFile = new ColumnFile(
					"../fable.test.data/data/columnfile/peaks_t100.flt");
		} catch (Throwable e) {
			e.printStackTrace();
			fail("could not open column file peaks_t100.flt");
		}
		if (columnFile == null) {
			fail("column file null");
		}
		columnFile.loadRows();
		elapsed = System.currentTimeMillis() - elapsed;
		System.out.println("peaks_t100.flt columnfile rows loaded in "
				+ elapsed / 1000 + " seconds");
	}
}
