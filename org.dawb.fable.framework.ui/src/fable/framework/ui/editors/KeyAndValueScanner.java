/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.ui.examples.rcp.texteditor.editors.xml.ColorManager;

import fable.framework.ui.internal.IVarKeys;

public class KeyAndValueScanner  extends RuleBasedScanner {

	ColorManager manager;
	
	public KeyAndValueScanner(ColorManager mg) {
		manager = mg;
		IToken valueNumber = new Token(new TextAttribute(manager
				.getColor(IVarKeys.DEFAULT)));
		IToken valueString = new Token(new TextAttribute(manager
				.getColor(IVarKeys.COLOR_GREEN)));

		IToken option = new Token(new TextAttribute(manager
				.getColor(IVarKeys.COLOR_BLUE), null, SWT.BOLD));
		IToken comment = new Token(new TextAttribute(manager
				.getColor(IVarKeys.COLOR_GREY), null, SWT.ITALIC));
		IToken step = new Token(new TextAttribute(manager
				.getColor(IVarKeys.COLOR_GREY), null, SWT.BOLD));

		IRule[] rules = new IRule[5];
		IWordDetector w = new WordDetector() ;
		//Numbers
		rules[0] = new NumberRule(valueNumber);
		//String
		rules[2] = new WordRule(w, valueString);
		//Keys
		rules[1] = new WordRule(w, option);
		
		((WordRule) rules[1]).setColumnConstraint(0);
		
		//step
		rules[3] = new SingleLineRule("#", null, step, (char) 0, true);
		((SingleLineRule) rules[3]).setColumnConstraint(0);
		//comment
		rules[4] = new SingleLineRule("#", null, comment, (char) 0, true);
		
		setRules(rules);
	}
	private static final class WordDetector implements IWordDetector {
		public boolean isWordPart(char c) {
			c = Character.toLowerCase(c);
			return Character.isUnicodeIdentifierPart(c);
		}

		public boolean isWordStart(char c) {
			c = Character.toLowerCase(c);
			return Character.isJavaIdentifierStart(c);
		}
	}
}
