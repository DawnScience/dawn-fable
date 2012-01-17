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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class KeyAndValueFileDocumentProvider extends FileDocumentProvider {

	/**
	 * The input file partitioning. It contains two partition types: {@link #INPUT_KEYS} and
	 * {@link #input_COMMENT}.
	 */
	public static final String INPUTFILE_PARTITIONING= "fable.fitallb.partitioning"; //$NON-NLS-1$

	/**
	 * The identifier of the comment body type.
	 */
	public static final String INPUT_DEFAULT= IDocument.DEFAULT_CONTENT_TYPE;
	
	/**
	 * The identifier of the comment partition type.
	 */
	private static final String[] CONTENT_TYPES= {
			
			IDocument.DEFAULT_CONTENT_TYPE
			
	};

	protected void setupDocument(Object element,IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 ext= (IDocumentExtension3) document;
			IDocumentPartitioner partitioner= createPartitioner();
			ext.setDocumentPartitioner(INPUTFILE_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}

	private IDocumentPartitioner createPartitioner() {
		IPredicateRule[] rule= new IPredicateRule[1];
		rule[0]= new SingleLineRule("#", null, new Token(INPUT_DEFAULT), (char) 0, true, false) ; //$NON-NLS-1$
		RuleBasedPartitionScanner scanner= new RuleBasedPartitionScanner();
		scanner.setPredicateRules(rule);
		
		return new FastPartitioner(scanner, CONTENT_TYPES);
	}
}
