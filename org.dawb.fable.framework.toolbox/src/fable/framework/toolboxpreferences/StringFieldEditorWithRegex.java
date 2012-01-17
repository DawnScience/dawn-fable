/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolboxpreferences;

import java.util.regex.Pattern;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class StringFieldEditorWithRegex extends StringFieldEditor {

	private String _regex = "";

	public StringFieldEditorWithRegex() {
		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.StringFieldEditor#checkState()
	 */
	@Override
	protected boolean checkState() {
		boolean checkOk = true;
		final Pattern pattern = Pattern.compile(_regex);
		if (pattern.matcher(this.getStringValue()).find()) {
			showErrorMessage();
			checkOk = false;
		}
		return checkOk;
	}

	public StringFieldEditorWithRegex(String name, String labelText,
			Composite parent) {
		super(name, labelText, parent);

	}

	public void set_regex(String regex, String message) {
		this._regex = regex;
		this.setErrorMessage(message);
	}

}
