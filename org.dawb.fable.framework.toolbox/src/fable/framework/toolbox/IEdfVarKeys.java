/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolbox;

public interface IEdfVarKeys {

	public final static String HEADER_DARKFILE="DarkFileName" ;
	public final static String HEADER_FLOODFILE="FloodFileName" ;
	public final static String HEADER_DISTORTIONFILE="DistortionFileName" ;	
	
	public final static String EDF_HEADER_BEGIN="{";
	public final static String EDF_HEADER_END="}";
	public final static String EDF_HEADER_SEP="=";
	public final static String EDF_HEADER_ENDOFLINE=";";
	
	//sOME KEY WORDS FOR EDF HEADER
	public final static String KEY_HEADER_TITLE="title"; 
	public final static String DIM_1="Dim_1"; 
	public final static String DIM_2="Dim_2"; 
	
	//
	public final static String FILE_FORMAT_BRUKER="bruker";

	
}
