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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JApplet;
import javax.swing.JPanel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.util.Logger;

import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.GridUtils;

/**
 * JmolEditor is a view for displaying 3d chemical structures using Jmol.
 * 
 * TODO
 * 
 * add controls to change view add support for scripting
 * 
 * @author Andy Gotz, Matthew Gerring
 * 
 */
public class JmolEditor extends EditorPart implements IReusableEditor {
	
	public static final String ID = "fable.framework.ui.editors.JmolEditor";
	final static String strScript = "delay; move 360 0 0 0 0 0 0 0 4;";
	public static int viewCount = 0;

	private Composite swtAwtComponent;
	private JApplet container;
	private Frame frame;
	private JmolPanel jmolPanel;
	private JmolSimpleViewer viewer;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {	
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}
	
	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		if (input!=null) setPartName(input.getName());
		updateInput();
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));		
		GridUtils.removeMargins(parent);
		
		swtAwtComponent = new Composite(parent, SWT.EMBEDDED
				| SWT.NO_BACKGROUND);
		
		GridData gdlist = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdlist.verticalSpan = 1;
		gdlist.horizontalSpan = 1;
		swtAwtComponent.setLayout(new GridLayout());
		GridUtils.removeMargins(swtAwtComponent);
		swtAwtComponent.setLayoutData(gdlist);
		frame = SWT_AWT.new_Frame(swtAwtComponent);
		container = new JApplet();
		frame.add(container);
		/* the following code was taken from Integration.java in the jmol svn */
		Container contentPane = container.getContentPane();
		jmolPanel = new JmolPanel();

		contentPane.add(jmolPanel);
		frame.setVisible(true);
		viewer = jmolPanel.getViewer();
		// this initial ZAP command is REQUIRED in Jmol 11.4.
		viewer.evalString("zap");
		// viewer.openFile("/home/goetz/jmol/caffeine.xyz");
		// viewer.openFile("http://chemapps.stolaf.edu/jmol/docs/examples-11/data/caffeine.xyz");
		// viewer.evalString(strScript);
		// viewer.openStringInline(strXyzHOH);
		
		updateInput();
	}
	
	private void updateInput() {
		if (getEditorInput()!=null) {
			final File file = EclipseUtils.getFile(getEditorInput());
			openFile(file.getAbsolutePath());
		}		
	}
	
	/**
	 * Open a structure file and display it with the Jmol viewer. Display the
	 * file name in the view part name.
	 * 
	 * @param fullFileName
	 */
	private void openFile(String fullFileName) {
		if (viewer == null) return;
		viewer.openFile(fullFileName);
		String strError = viewer.getOpenFileError();
		if (strError != null) Logger.error(strError);
	}

	@Override
	public void dispose() {
		super.dispose();
		
		// Not really necessary normally but nullifying things
		// helps the garbage collector.
		if (swtAwtComponent!=null) swtAwtComponent.dispose();
		swtAwtComponent=null;
		if (container!=null) container.destroy();
		container      =null;
		if (frame!=null) frame.dispose();
		frame          =null;
		jmolPanel      =null;
		viewer         =null;
	}

	final static String strXyzHOH = "3\n" + "water\n" + "O  0.0 0.0 0.0\n"
			+ "H  0.76923955 -0.59357141 0.0\n"
			+ "H -0.76923955 -0.59357141 0.0\n";

	static class JmolPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JmolSimpleViewer viewer;
		JmolAdapter adapter;

		JmolPanel() {
			adapter = new SmarterJmolAdapter();
			viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
		}

		public JmolSimpleViewer getViewer() {
			return viewer;
		}

		final Dimension currentSize = new Dimension();
		final Rectangle rectClip = new Rectangle();

		public void paint(Graphics g) {
			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}
	}

	@Override
	public void setFocus() {
		if (swtAwtComponent!=null) swtAwtComponent.setFocus();
	}
	

	/**
	 * @return the id
	 */
	public static String getID() {
		return ID;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Nothing to do
	}

	@Override
	public void doSaveAs() {
		// Nothing to do
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
}
