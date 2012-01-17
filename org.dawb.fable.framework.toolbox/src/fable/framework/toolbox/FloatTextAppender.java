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

import org.eclipse.swt.widgets.Composite;

/**
 * This class allows to set more than one float in a text field and to do the
 * good conversion for the decimal depending on the locale.
 * 
 * @author SUCHET
 * 
 */
public class FloatTextAppender extends FloatText {

	protected Float[] _fieldValue;

	protected String separator = " ";

	// private Logger logger;
	public FloatTextAppender(Composite parent, int style) {
		super(parent, style);
		// logger = FableLogger.getLogger();
	}

	public FloatTextAppender(Composite parent, int style, String LabelText,
			String Separator) {
		super(parent, style, LabelText);
		separator = Separator;
	}

	public FloatTextAppender(Composite container, int style, String separator) {
		super(container, style);
		this.separator = separator;
	}

	/*****************************************************************************************/
	public boolean checkValue(String str) {
		boolean bok = true;
		if (str != null && !str.equals("")) {
			_errorMessage = "Field should contain a float value seperated by "
					+ separator;
			if (separator != null && separator.equals(" ")) {
				_errorMessage += " a space.";
			}
			String[] split = str.split(separator);
			// _fieldValue = new Float[split.length];
			int i = 0;
			while (bok && i < split.length) {
				String s = split[i];
				bok = ControlField.isFloat(s);
				// DecimalFormat decimalFormatNumber = new DecimalFormat();

				// try {
				// Number n = decimalFormatNumber.parse(s);
				// } catch (ParseException e) {
				// bok = false;
				// }
				i++;
			}
		} else {
			if (_isRequiredField) {
				bok = false;
			}
		}
		return bok;
	}

	protected String getErrorFormatDescription() {
		return _errorMessage;
	}

	@Override
	/*
	 * This method gets the text in the text field but convert it into a String
	 * that can be treated by our programs (, converted to .)
	 */
	public String getText() {
		// DecimalFormat d = new DecimalFormat();
		String str = this.getTextField().getText();
		/*
		 * if (!str.trim().equals("")) { try { String[] split =
		 * str.split(separator); int i = 0; //reset str str = ""; while ( i <
		 * split.length) { String s = split[i]; Number n = d.parse(s); str +=
		 * n.toString() + separator; i++; }
		 * 
		 * } catch (ParseException e) {
		 * FableLogger.getLogger().error(e.getMessage()); } }
		 */
		// logger.debug("getText :" + s);
		return str;
	}

	public void setText(String text) {
		if (textField != null) {
			/*
			 * String[] split = text.split(separator); String newString = text;
			 * if(split.length > 1){ newString = ""; }
			 * 
			 * DecimalFormat form = new DecimalFormat(); for (int i = 0 ; i <
			 * split.length ; i ++){ try { Float f = Float.valueOf(split[i]);
			 * newString += form.format(f) + separator; } catch
			 * (NumberFormatException e) { logger.error(e.getMessage()); } }
			 */
			textField.setText(text);
		}
	}

	@Override
	public boolean set_Text(String txt) {
		if (textField != null) {
			textField.setText(txt);
		}
		return true;
	}

}
