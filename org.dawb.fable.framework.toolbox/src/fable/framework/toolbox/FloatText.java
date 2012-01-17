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
 * 17 avr. 07
 */
package fable.framework.toolbox;

import org.slf4j.Logger;
import org.eclipse.swt.widgets.Composite;

import fable.framework.logging.FableLogger;

/**
 * @author G. Suchet fable.framework.toolbox 17 avr. 07
 * @since February, the 25th : This text field should respect the locale.
 */
public class FloatText extends TypedText {

	protected Float fieldValue = null;
	protected Float minValue;
	protected Float maxValue;
	protected boolean maxIsIncluded = false;
	protected boolean minIsIncluded = false;
	private Logger logger;

	public FloatText(Composite parent, int style) {
		super(parent, style);

	}

	public FloatText(Composite parent, int style, String LabelText) {
		super(parent, style, LabelText);
		logger = FableLogger.getLogger();
	}

	/**
	 * @param Composite
	 *            parent
	 * @param int style
	 * @param Float
	 *            minvalue : min inclusive value accceptable ;
	 * @param Float
	 *            maxValue ; max inclusive value acceptable ;
	 * */
	public FloatText(Composite parent, int style, String LabelText,
			float minValue, float maxValue) {

		this(parent, style, LabelText);

		setMinValue(minValue, true);
		setMaxValue(maxValue, true);

	}

	/**
	 * get floating point value of FloatText
	 * 
	 * @return - floating point value
	 */
	public Float getValue() {
		return fieldValue;
	}

	public void setMaxValue(float max, boolean bIsInclude) {
		maxIsIncluded = bIsInclude;
		maxValue = new Float(max);

	}

	public void setMinValue(float min, boolean bIsInclude) {
		minIsIncluded = bIsInclude;
		minValue = new Float(min);

	}

	@Override
	/*
	 * Set text with the respect of Locale
	 */
	public boolean set_Text(String txt) {
		/*
		 * DecimalFormat form = new DecimalFormat();
		 * 
		 * if (!txt.trim().equals("")) { try { Float f = Float.valueOf(txt);
		 * 
		 * txt = form.format(f); } catch (NumberFormatException e) {
		 * logger.error(e.getMessage()); } }
		 */
		return super.set_Text(txt);
	}

	@Override
	/*
	 * This method gets the text in the text field but convert it into a String
	 * that can be treated by our programs (, converted to .)
	 */
	public String getText() {
		// NumberFormat d = new DecimalFormat();

		String s = this.getTextField().getText();
		/*
		 * if(s.toLowerCase().contains("e")){
		 * //d.applyPattern("#,##0.##E0;(#,##0.##E0)"); } if
		 * (!s.trim().equals("")) { try {
		 * 
		 * Number n = d.parse(s); s = n.toString(); } catch (ParseException e) {
		 * FableLogger.getLogger().error(e.getMessage()); } } //
		 * logger.debug("getText :" + s);
		 */
		return s;
	}

	/**
	 * Check value with respect of locale.
	 */
	/*****************************************************************************************/
	public boolean checkValue(String str) {
		boolean bok = true;
		if (str != null && !str.equals("")) {
			_errorMessage = "Field should contain a float value ";
			// bok = ControlField.isFloat(str);
			try {

				// DecimalFormat decimalFormatNumber = new DecimalFormat();

				// Number n = decimalFormatNumber.parse(str);

				// fieldValue = n.floatValue();
				fieldValue = Float.valueOf(str);
				// logger.debug("checkvalue :" + fieldValue);
				if (maxValue != null && minValue != null) {

					if (minIsIncluded && maxIsIncluded) {
						bok = Float.compare(fieldValue, minValue.floatValue()) >= 0
								&& Float.compare(fieldValue, maxValue
										.floatValue()) <= 0;
						_errorMessage += "between " + minValue + " and "
								+ maxValue;
					} else if (!minIsIncluded && maxIsIncluded) {
						bok = Float.compare(fieldValue, minValue.floatValue()) > 0
								&& Float.compare(fieldValue, maxValue
										.floatValue()) <= 0;
						_errorMessage += " greater than " + minValue
								+ " and less or equal to " + maxValue;
					} else if (minIsIncluded && !maxIsIncluded) {
						bok = Float.compare(fieldValue, minValue.floatValue()) >= 0
								&& Float.compare(fieldValue, maxValue
										.floatValue()) < 0;
						_errorMessage += " greater or equal to " + minValue
								+ " and less than " + maxValue;
					} else if (!minIsIncluded && !maxIsIncluded) {
						bok = Float.compare(fieldValue, minValue.floatValue()) > 0
								&& Float.compare(fieldValue, maxValue
										.floatValue()) < 0;
						_errorMessage += " greater than " + minValue
								+ " and less than " + maxValue;

					}
				} else if (minValue != null && maxValue == null) {
					// low boundary
					if (minIsIncluded) {
						bok = Float.compare(fieldValue, minValue.floatValue()) >= 0;
						_errorMessage += " greater or equal to " + minValue;
					} else {
						// exlude value
						bok = Float.compare(fieldValue, minValue.floatValue()) > 0;
						_errorMessage += " greater than " + minValue;
					}
				} else if (minValue == null && maxValue != null) {
					// upper boundary
					if (maxIsIncluded) {
						bok = Float.compare(fieldValue, maxValue.floatValue()) <= 0;
						_errorMessage += " and less or equal to " + maxValue;

					} else {
						bok = Float.compare(fieldValue, maxValue.floatValue()) < 0;
						_errorMessage += " less than " + maxValue;

					}

				}

			} catch (NumberFormatException e) {
				bok = false;
				logger.error(e.getMessage());

			}

		} else if (_isRequiredField) {
			bok = false;
		}
		return bok;
	}

	protected String getErrorFormatDescription() {
		return _errorMessage;
	}

	public void setText(String string) {
		set_Text(string);

	}

}
