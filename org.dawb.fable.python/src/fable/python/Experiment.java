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

import java.util.Vector;

public class Experiment {

	private Vector<Sample> samples;
	/**********************************************************************************************/
	/**                                          CONSTRUCTOR                                     **/
	/**                                                                                          **/
	/**********************************************************************************************/

	/**
	 * 
	 */
	public Experiment(){
		
	}
	
	/**
	 * 
	 * @param s
	 */
	public Experiment(Vector<Sample> s){
		samples=s;
	}
	
	/**********************************************************************************************/
	/**                                          GETTER 			                             **/
	/**                                                                                          **/
	/**********************************************************************************************/
	public Vector<Sample> getSamples(){
		return samples;
	}
	
	public Object[] getSample_toArray(){
		return samples.toArray();
	}
	/**********************************************************************************************/
	/**                                          SETTER			                                 **/
	/**                                                                                          **/
	/**********************************************************************************************/

	/**
	 * 
	 * 4 dc. 07
	 * @author G. Suchet
	 * 
	 */
	public void addSample(Sample s){
		if(samples==null){
			samples=new Vector<Sample>();
		}
		samples.add(s);
	}
	/**********************************************************************************************/
	/**                                          REMOVE                                          **/
	/**                                                                                          **/
	/**********************************************************************************************/
	/**
	 * 
	 */
	public boolean removeSample(Sample s){
		return samples.removeElement(s);
	}
	
	/**
	 * 
	 */
	public void removeSampleAt(int index){
		try{
			samples.remove(index);
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Can not remove sample. Index not found");
		}
	}
	public void removeAll(){
		samples.removeAllElements();
	}
}
