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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This class is a general class for Text fields that have a numerical type:
 * float, integer for instance.
 * 
 * @author SUCHET
 *@Date 19/04/2007
 */
public class TypedText extends Composite {

	/**************************** Attributes **********************************/
	protected final static int SPACING = 5; // pixel value
	protected final static int MARGIN = 1; // pixel value
	private final String ID_REQUIRED = "required.field";
	private final String ID_FORMAT = "format.field";
	protected boolean _bValide = true;
	protected boolean _isRequiredField;
	protected Text textField;

	protected Label _label;
	protected ControlDecoration textFieldDecorated;
	protected FieldDecorationRegistry _registry;
	protected Image _imgRequired = Activator.imageDescriptorFromPlugin(
			fable.framework.toolbox.Activator.PLUGIN_ID,
			fable.framework.toolbox.IImagesKeys.IMG_REQUIRED).createImage();
	protected Image _imgError = Activator.imageDescriptorFromPlugin(
			fable.framework.toolbox.Activator.PLUGIN_ID,
			fable.framework.toolbox.IImagesKeys.IMG_ERROR).createImage();
	// protected boolean _bBoundariesDefined=false;
	protected String _errorMessage = "";
	private String name = "";

	/************************ Constructor *************************************/
	protected TypedText(Composite parent, int style) {
		super(parent, SWT.NONE);

		GridLayout gl = new GridLayout(3, false);
		this.setLayout(gl);
		this.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL,
				GridData.CENTER, true, true));
		createFields(style);

		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				// controlResize();
			}
		});

		textField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				_bValide = checkValue(((Text) e.widget).getText());
				showDecoration();
			}
		});
		textField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				_bValide = checkValue(((Text) e.widget).getText());
				showDecoration();
			}
		});
	}

	protected TypedText(Composite parent, int style, String LabelText) {
		this(parent, style);
		_label.setText(LabelText);
		name = LabelText;
	}

	/**
	 * @param field
	 *            the _txtField to set
	 */
	public boolean set_Text(String txt) {
		if (txt != null) {
			((Text) textFieldDecorated.getControl()).setText(txt);
			_bValide = checkValue(txt);
			showDecoration();
		}
		return _bValide;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics
	 * .Color)
	 */
	@Override
	public void setBackground(Color color) {
		// super.setBackground(color);
		((Text) textFieldDecorated.getControl()).setBackground(color);
	}

	@Override
	public void setToolTipText(String string) {
		textField.setToolTipText(string);
	}

	public Text getTextField() {
		return textField;// ((Text) textFieldDecorated.getControl());
	}

	/**
	 * @return the _bValide
	 */
	public boolean is_bValide() {
		return _bValide;
	}

	/**
	 * @param valide
	 *            the _bValide to set
	 */
	public void set_bValide(boolean valide) {
		_bValide = valide;
	}

	public String getText() {
		return textField.getText();// ((Text)
		// textFieldDecorated.getControl()).getText();
	}

	/**
	 * @return the _isRequiredField
	 */
	public boolean is_isRequiredField() {
		return _isRequiredField;
	}

	/**
	 * @param requiredField
	 *            the _isRequiredField to set
	 */
	public void set_isRequiredField(boolean requiredField) {
		_isRequiredField = requiredField;
		_bValide = checkValue(this.getText());
	}

	/**
	 * This method shows the decoration depending on the error. Otherwhise,
	 * decoration is hidden.
	 */
	public void showDecoration() {
		textFieldDecorated.hide();
		if (!showRequired()) {
			if (showError()) {
				textFieldDecorated.show();
			}
		} else {
			textFieldDecorated.show();
		}
	}

	// Comment for instance due to error when we close and reopen a window
	/*
	 * private void widgetDisposed(DisposeEvent e){ if(_imgError != null && !
	 * _imgError.isDisposed()){ _imgError.dispose();
	 * _registry.getFieldDecoration(ID_FORMAT).getImage().dispose();
	 * 
	 * } if(_imgRequired != null && !_imgRequired.isDisposed()){
	 * 
	 * 
	 * //
	 * _imgRequired.dispose();_registry.getFieldDecoration(ID_REQUIRED).getImage
	 * ().dispose(); }
	 * 
	 * }
	 */
	// private void controlResize() {
	// Point tExtent = ((Text) textFieldDecorated.getControl()).computeSize(
	// SWT.DEFAULT, SWT.DEFAULT, false);
	// Point lExtend = _label.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
	// _label.setBounds(MARGIN, MARGIN, lExtend.x, lExtend.y);
	// ((Text) textFieldDecorated.getControl()).setBounds(tExtent.x + SPACING,
	// MARGIN, tExtent.x, tExtent.y);
	// }
	private void createFields(int style) {
		_label = new Label(this, SWT.NONE);
		_label.setVisible(true);
		textField = new Text(this, SWT.BORDER | style);
		textFieldDecorated = new ControlDecoration(textField, SWT.LEFT
				| SWT.TOP);

		_registry = FieldDecorationRegistry.getDefault();
		_registry.registerFieldDecoration(ID_FORMAT,
				"This field should contain a typed value", _imgError);
		_registry.registerFieldDecoration(ID_REQUIRED,
				"This field is required", _imgRequired);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 1;
		gd.horizontalIndent = 8;
		textFieldDecorated.getControl().setLayoutData(gd);

		// add nothing, just to align text fields
		// this.set_Text("");
	}

	/**************************************************************************/
	/**
	 *@desription : this method should be overriden in inherited classes
	 */
	public boolean checkValue(String str) {
		return is_isRequiredField();
	}

	/**
	 * This method have been added for unit tests.
	 * 
	 * @return error message
	 */
	public String getErrorMessage() {
		if (showRequired()) {
			return "Field is required";
		}
		return getErrorFormatDescription();
	}

	private boolean showError() {
		boolean value = false;
		if (!is_bValide()) {
			textFieldDecorated.setDescriptionText(getErrorFormatDescription());
			textFieldDecorated.setImage(_registry.getFieldDecoration(ID_FORMAT)
					.getImage());
			// textFieldDecorated.show();
			value = true;
		} else {
			// textFieldDecorated.hide();
		}
		return value;
	}

	private boolean showRequired() {
		boolean value = false;
		if (is_isRequiredField()
				&& (textField.getText() == null || textField.getText().trim()
						.length() == 0)) {
			String description = _registry.getFieldDecoration(ID_REQUIRED)
					.getDescription();
			textFieldDecorated.setDescriptionText(description);
			textFieldDecorated.setImage(_registry.getFieldDecoration(
					ID_REQUIRED).getImage());
			// textFieldDecorated.show();
			value = true;
		} else {
			// _txtField.addFieldDecoration(_registry.getFieldDecoration(ID_REQUIRED),
			// SWT.LEFT, false);
			// textFieldDecorated.hide();
		}
		return value;
	}

	/**************************************************************************/

	protected String getErrorFormatDescription() {
		return _registry.getFieldDecoration(ID_FORMAT).getDescription();
	}

	public void setEnabled(boolean enable) {
		textField.setEnabled(enable);
		if (enable == false) {
			_bValide = true;
			showDecoration();
		} else {
			_bValide = checkValue(textField.getText());
			showDecoration();
		}
	}

	public String toString() {
		String s = "";
		if (_label != null && !_label.getText().equals("")) {
			s = _label.getText();
		} else {
			s = name;
		}
		return s;
	}

	public void setName(String n) {
		name = n;
	}

	public void setLabel(String label) {
		_label.setText(label);
	}

	public String getLabel() {
		return _label.getText();
	}

}
