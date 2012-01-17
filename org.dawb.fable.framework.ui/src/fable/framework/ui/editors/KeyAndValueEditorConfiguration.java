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
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.examples.rcp.texteditor.editors.xml.ColorManager;

public class KeyAndValueEditorConfiguration extends TextSourceViewerConfiguration {
	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler pr = new PresentationReconciler();
		ColorManager manager = new ColorManager();
		DefaultDamagerRepairer defaultDamageR = new DefaultDamagerRepairer(
				new KeyAndValueScanner(manager));
		pr.setDamager(defaultDamageR, IDocument.DEFAULT_CONTENT_TYPE);
		pr.setRepairer(defaultDamageR, IDocument.DEFAULT_CONTENT_TYPE);
		return pr;
	}
	
	

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return KeyAndValueFileDocumentProvider.INPUTFILE_PARTITIONING;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
				IDocument.DEFAULT_CONTENT_TYPE
		};
	}
	
	
	
}
