/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.clipboard;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;

/**
 * FableClipboard manages the common Clipboard for Fable. The public member
 * should not be modified or disposed until Fable shuts down.
 * 
 * @author evans
 * 
 */
final public class FableClipboard {
	/**
	 * The common Clipboard for Fable. It should not be modified or disposed
	 * until Fable shuts down.
	 */
	public static final Clipboard clipboard = new Clipboard(Display
			.getDefault());
}
