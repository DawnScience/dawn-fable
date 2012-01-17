/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * fable.framework.toolbox 
 * fable.framework.toolbox
 * 15 mai 07
 */
package fable.framework.toolbox;

import org.eclipse.swt.widgets.Composite;

/**
 * @author G. Suchet fable.framework.toolbox 15 mai 07
 * 
 */
public class FileText extends TypedText {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fable.framework.toolbox.TypedText#checkValue(java.lang.String)
	 */
	@Override
	public boolean checkValue(String str) {
		boolean value = true;
		if(str != null && !str.trim().equals("")){
			value = ToolBox.checkIfFileExists(str);
		}
		
		return isEnabled()?value:true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fable.framework.toolbox.TypedText#getErrorFormatDescription()
	 */
	@Override
	protected String getErrorFormatDescription() {

		return _errorMessage;
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FileText(Composite parent, int style) {
		super(parent, style);
		this._errorMessage = "File " + this.getText() + " not found";
	}

	/**
	 * @param parent
	 * @param style
	 * @param LabelText
	 */
	public FileText(Composite parent, int style, String LabelText) {
		super(parent, style, LabelText);
		this._errorMessage = "File " + this.getText() + " not found";
	}

	public void set_errorMessage(String string) {
		this._errorMessage = string;
		
	}

}
