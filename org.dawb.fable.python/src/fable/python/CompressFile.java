/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.junit.Test;

public class CompressFile {

	public static void compress(String entryfile, String outputFile)
			throws IOException {

		FileOutputStream fout;

		fout = new FileOutputStream(outputFile);
		GZIPOutputStream zout = new GZIPOutputStream(fout);

		FileInputStream fin = new FileInputStream(entryfile);

		try {
			// DEBUG
			// System.out.println("Compressing " + entryfile);

			for (int c = fin.read(); c != -1; c = fin.read()) {
				zout.write(c);

			}
		} finally {
			fin.close();
		}

		zout.close();

	}

	@Test
	public void testcompressFile() {
		try {
			CompressFile
					.compress(
							"d:\\My Documents\\ANTC_quench_2_\\ANTC_quench_2_3537.cor",
							"d:\\\\My Documents\\\\ANTC_quench_2_\\\\ANTC_quench_2_3537.cor.gzip");

		} catch (IOException e) {
			fail("An error occured while testing java zip compression : "
					+ e.getMessage());
		}
	}

	@Test
	public void testReadCompressedFile() {

		try {
			FabioFile f = new FabioFile(
					"d:\\\\My Documents\\\\ANTC_quench_2_\\\\ANTC_quench_2_3537.edf");
			// DEBUG
			// System.out.println("Try to read");
			f.readImage();
			// DEBUG
			// System.out.println("end reading");
		} catch (FabioFileException e) {
			fail("Fabio is unable to load this file");
			fail(e.getMessage());

		} catch (Throwable e) {
			fail("Fabio is unable to read this file");
			fail(e.getMessage());
		}

	}
}
