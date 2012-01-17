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

import jep.JepException;

import org.dawb.fabio.FableJep;

/**
 * This class represents a chi file. A chi file is the result file after
 * integration in fit2d. Parsing file is done in python module chi_reader.py.
 * 
 * @author SUCHET
 * 
 */

public class ChiFile {

	// private String chiFile=""; //the name of the chi file
	// private FabioFile file=null; // if the edf file has been set in the chi
	// file
	private FableJep fableJep;
	private float mintth, maxtth, meantth;
	private float minIntensity, maxIntensity, meanIntensity;
	private float[] dots = null;
	private int numDots = 0;

	public ChiFile() {
		try {
			fableJep = FableJep.getFableJep();
			fableJep.jepImportModules("numpy");
			fableJep.jepImportSpecificDefinition("FableFacility", "chi_file");
			fableJep.eval("reader =chi_file.chi_file()");

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadfile(String chifile) throws JepException {
		// this.chiFile = chifile;
		fableJep.set("filename", chifile);
		fableJep.eval("reader.loadfile(filename)");

	}

	public float[] getlist() throws JepException {
		// fableJep.eval("print reader.fabiofile");
		// fableJep.eval("reader.dots.astype(numpy.float32).tostring()");
		numDots = fableJep.getIntegerValue("len(reader.dots)");
		// float[] f = null;
		if (numDots > 0) {
			numDots = numDots / 2;
			dots = new float[numDots];
			dots = (float[]) fableJep
					.getValue_floatarray("reader.dots.astype(numpy.float32).tostring()");
			setMinIntensity(fableJep.getFloatValue("reader.getminIntensity()"));
			setMaxIntensity(fableJep.getFloatValue("reader.getmaxIntensity()"));
			setMeanIntensity(fableJep
					.getFloatValue("reader.getmeanIntensity()"));
			setMaxtth(fableJep.getFloatValue("reader.getmaxAngle()"));
			setMintth(fableJep.getFloatValue("reader.getminAngle()"));
			setMeantth(fableJep.getFloatValue("reader.getmeanAngle()"));

		}
		return dots;
	}

	public float getMinIntensity() {
		return minIntensity;
	}

	private void setMinIntensity(float min) {
		this.minIntensity = min;
	}

	public float getMaxIntensity() {
		return maxIntensity;
	}

	private void setMaxIntensity(float max) {
		this.maxIntensity = max;
	}

	public float getMeanIntensity() {
		return meanIntensity;
	}

	private void setMeanIntensity(float mean) {
		this.meanIntensity = mean;
	}

	public int getNumDots() {
		return numDots;
	}

	public float getMintth() {
		return mintth;
	}

	public void setMintth(float mintth) {
		this.mintth = mintth;
	}

	public float getMaxtth() {
		return maxtth;
	}

	public void setMaxtth(float maxtth) {
		this.maxtth = maxtth;
	}

	public float getMeantth() {
		return meantth;
	}

	public void setMeantth(float meantth) {
		this.meantth = meantth;
	}

}
