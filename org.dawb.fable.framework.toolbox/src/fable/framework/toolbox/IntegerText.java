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
 * 19 avr. 07
 */
package fable.framework.toolbox;

import org.eclipse.swt.widgets.Composite;

/**
 * @author G. Suchet fable.framework.toolbox 19 avr. 07
 * 
 */
public class IntegerText extends TypedText {

	protected Integer _fieldValue;
	protected Integer _MinValue;
	protected Integer _MaxValue;
	protected boolean _bMaxIsIncluded = false;
	protected boolean _bMinIsIncluded = false;

	// Constructors
	public IntegerText(Composite parent, int style) {
		super(parent, style);
	}

	public IntegerText(Composite parent, int style, String label) {
		super(parent, style, label);

	}

	/**
	 * @param Composite
	 *            parent
	 * @param int style
	 * @param double minvalue : min inclusive value accceptable ; set minValue
	 *        equal to maxValue if no minValue should be define ;
	 * @param double maxValue ; max inclusive value acceptable ; set maxValue
	 *        lower than minValue if no upper bound should be set
	 * */
	public IntegerText(Composite parent, int style, String LabelText,
			int minValue, int maxValue) {

		this(parent, style, LabelText);
		setMinValue(minValue, true);
		setMaxValue(maxValue, true);

	}

	public void setMaxValue(int maxValue, boolean bIsInclude) {
		_bMaxIsIncluded = bIsInclude;
		_MaxValue = new Integer(maxValue);

	}

	public void setMinValue(int minValue, boolean bIsInclude) {
		_bMinIsIncluded = bIsInclude;
		_MinValue = new Integer(minValue);

	}

	/*********************************************************************************************/
	public boolean checkValue(String str) {
		boolean bok = true;
		if (str != null && !str.equals("")) {
			_errorMessage = "Field should contain an integer value ";
			bok = ControlField.isInteger(str);
			if (bok) {
				_fieldValue = Integer.valueOf(str);
				if (_MaxValue != null && _MinValue != null) {

					if (_bMinIsIncluded && _bMaxIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) >= 0
								&& Float.compare(_fieldValue, _MaxValue
										.floatValue()) <= 0;
						_errorMessage += "between " + _MinValue + " and "
								+ _MaxValue;
					} else if (!_bMinIsIncluded && _bMaxIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) > 0
								&& Float.compare(_fieldValue, _MaxValue
										.floatValue()) <= 0;
						_errorMessage += " greater than " + _MinValue
								+ " and less or equal to " + _MaxValue;
					} else if (_bMinIsIncluded && !_bMaxIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) >= 0
								&& Float.compare(_fieldValue, _MaxValue
										.floatValue()) < 0;
						_errorMessage += " greater or equal to " + _MinValue
								+ " and less than " + _MaxValue;
					} else if (!_bMinIsIncluded && !_bMaxIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) > 0
								&& Float.compare(_fieldValue, _MaxValue
										.floatValue()) < 0;
						_errorMessage += " greater than " + _MinValue
								+ " and less than " + _MaxValue;

					}
				} else if (_MinValue != null && _MaxValue == null) {
					// low boundary
					if (_bMinIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) >= 0;
						_errorMessage += " greater or equal to " + _MinValue;
					} else {
						// exlude value
						bok = Float
								.compare(_fieldValue, _MinValue.floatValue()) > 0;
						_errorMessage += " greater than " + _MinValue;
					}
				} else if (_MinValue == null && _MaxValue != null) {
					// upper boundary
					if (_bMaxIsIncluded) {
						bok = Float
								.compare(_fieldValue, _MaxValue.floatValue()) <= 0;
						_errorMessage += " and less or equal to " + _MaxValue;

					} else {
						bok = Float
								.compare(_fieldValue, _MaxValue.floatValue()) < 0;
						_errorMessage += " less than " + _MaxValue;
					}
				}
			}

		} else if (_isRequiredField) {
			bok = false;
		}
		return bok;
	}

	protected String getErrorFormatDescription() {
		return _errorMessage;
	}
}
