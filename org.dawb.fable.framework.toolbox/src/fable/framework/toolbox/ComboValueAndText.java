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

public class ComboValueAndText {

	private String _text;
	private String _value;
	
	public ComboValueAndText(String text, String value){
		super();
		_text = text;
		_value = value;
	}
	public String toString(){
		return _text;
	}
	public String getValue(){
		return _value;
	}
}
