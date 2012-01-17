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

import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class StringText extends TypedText {

	private String _regex; // the regular expression
	private String _sFormat; // a sentence for output message ; for example :

	// "no punctuation allowed"
	// private String _regexAllowed;

	public StringText(Composite parent, int style, String LabelText) {
		super(parent, style, LabelText);

	}

	public StringText(Composite parent, int style, String LabelText,
			String regex, String format) {
		super(parent, style, LabelText);
		_regex = regex;
		_sFormat = format; // 

	}

	public StringText(Composite parent, int style) {
		super(parent, style);

	}

	/**
	 * return current string displayed
	 * 
	 * @return
	 */
	public String getValue() {
		return ((Text) textFieldDecorated.getControl()).getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fable.framework.toolbox.TypedText#checkValue(java.lang.String)
	 */
	@Override
	public boolean checkValue(String str) {

		boolean bok = true;
		if (str != null && !str.equals("")) {
			bok = checkByRegex();
			// if expression is found,
			if (!bok) {
				_errorMessage = _sFormat;
			}

		} else if (_isRequiredField) {
			bok = false;
		}
		return bok;

	}

	protected String getErrorFormatDescription() {
		return _errorMessage;
	}

	/*
	 * return false if not authorize regex is found
	 */
	private boolean checkByRegex() {
		boolean bok = true;
		if (_regex != null) {
			final Pattern pattern = Pattern.compile(_regex);
			if (pattern.matcher(this.getText()).find()) {
				bok = false;
			}
		}

		return bok;
	}

}
