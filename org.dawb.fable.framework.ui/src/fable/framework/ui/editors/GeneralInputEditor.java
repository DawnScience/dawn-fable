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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Use this editor to display keys and values in different colors
 * 
 * @author SUCHET
 * 
 */
public class GeneralInputEditor extends TextEditor implements
		IResourceChangeListener {

	/** The ID of this editor as defined in plugin.xml */
	public static String ID = "fable.framework.ui.editors.GeneralInputEditor";

	public GeneralInputEditor() {
		super();
		setSourceViewerConfiguration(new KeyAndValueEditorConfiguration());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		// provider
		if (input instanceof IFileEditorInput) {
			setDocumentProvider(new KeyAndValueFileDocumentProvider());
		}
		super.doSetInput(input);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) getEditorInput()).getFile()
								.getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i]
									.findEditor(getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			// A general resource has changed
			IFileEditorInput fileInput = null;
			IPath path = null;
			IResourceDelta delta = null;
			IResource res = null;
			if (getEditorInput() != null
					&& getEditorInput() instanceof IFileEditorInput) {
				fileInput = (IFileEditorInput) getEditorInput();
				path = fileInput.getFile().getFullPath();
			}
			if (path != null) {
				delta = event.getDelta().findMember(path);
			}
			if (delta != null)
				res = delta.getResource();
			if (res != null) {
				int kind = delta.getKind();
				if (kind == IResourceDelta.REMOVED) {
					// Close the editor
					final IFileEditorInput input = fileInput;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage[] pages = getSite()
									.getWorkbenchWindow().getPages();
							for (int i = 0; i < pages.length; i++) {
								IEditorPart editorPart = pages[i]
										.findEditor(input);
								pages[i].closeEditor(editorPart, true);
							}
						}
					});
				} else {
					// TODO Handle other changes such as the file changed
					// externally
//					SWTUtils.infoMsgAsync("Got POST_CHANGE  [" + delta.getKind()
//							+ "] for " + path.toOSString());
				}
			}
		}

	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

	}

	
	
}
