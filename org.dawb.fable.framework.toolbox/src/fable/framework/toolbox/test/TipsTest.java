/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolbox.test;


import junit.framework.Assert;

import org.junit.Test;

import fable.framework.toolbox.ToolBox;
public class TipsTest {
	@Test
	public void osArchTest(){
		System.out.println(System.getProperty("os.arch"));
	}
	@Test
	public void stemTest(){
		String filename = "test0006.edf";
		String filename1 = "test2_1205.edf.gz";
		String filename2 = "testbruker.0001";
		String filename3 = "testBrukerziped.2001.gz" ;
		String filename4 = "randomeTes_14001.tif.gz" ;
		
		//Check getStem 
		
		Assert.assertEquals("stem in " + filename + " not found",  "test", ToolBox.getStem(filename));
		Assert.assertEquals("stem in " + filename1 + " not found", "test2_", ToolBox.getStem(filename1));
		Assert.assertEquals("stem in " + filename2 + " not found", "testbruker", ToolBox.getStem(filename2));
		Assert.assertEquals("stem in " + filename3 + " not found", "testBrukerziped", ToolBox.getStem(filename3));
		Assert.assertEquals("stem in " + filename4 + " not found", "randomeTes_",ToolBox.getStem(filename4));
		//check getType
		Assert.assertEquals("type in " + filename + " not found", "edf",ToolBox.getFileType(filename));
		Assert.assertEquals("type in " + filename1 + " not found", "edf.gz",ToolBox.getFileType(filename1));
		Assert.assertEquals("type in " + filename2 + " not found", "bruker",ToolBox.getFileType(filename2));
		Assert.assertEquals("type in " + filename3 + " not found", "bruker",ToolBox.getFileType(filename3));
		Assert.assertEquals("type in " + filename4 + " not found", "tif.gz",ToolBox.getFileType(filename4));
		//Now check filenumber
		Assert.assertEquals("File number in " + filename + " not found", "0006",ToolBox.getFileNumber(filename));
		Assert.assertEquals("File number  in " + filename1 + " not found", "1205",ToolBox.getFileNumber(filename1));
		Assert.assertEquals("File number  in " + filename2 + " not found", "0001",ToolBox.getFileNumber(filename2));
		Assert.assertEquals("File number  in " + filename3 + " not found", "2001",ToolBox.getFileNumber(filename3));
		Assert.assertEquals("File number  in " + filename4 + " not found", "14001",ToolBox.getFileNumber(filename4));
	
	}
}
