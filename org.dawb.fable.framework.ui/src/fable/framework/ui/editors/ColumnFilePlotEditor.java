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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import fable.framework.internal.IPropertyVarKeys;
import fable.framework.ui.actions.SaveAsColumnFileEditorAction;
import fable.framework.ui.actions.SaveColumnFileEditorAction;
import fable.framework.ui.views.ColFileXYPlot;

/**
 * ColumnFilePlotEditor will create a plot Editor for a columnFile.
 * <p>
 * A Columnfile is a file containing columns of data to plot in ASCII format. <br>
 * Columnfiles are produced by Fable programs e.g. by ImageD11 and PolyXSim for
 * example.
 * </p>
 * 
 * @author Gaelle Suchet
 * 
 */
public class ColumnFilePlotEditor extends EditorPart implements
		ISelectionListener, org.eclipse.jface.util.IPropertyChangeListener,
		IColumnFileEditor {

	private ColFileXYPlot xyplot;
	/** Unique ID for this plot Editor */
	private static final String ID = "fable.framework.ui.ColumnFilePlotEditor";

	// private JLDataView current_dataView = null;
	// private JLDataView dataViewForUniccells;
	// private Vector<ColumnFile> array_columns = new Vector<ColumnFile>();
	// private HashMap<String, JLDataView> dataToPlot = new HashMap<String,
	// JLDataView>();
	/** Column editor input. */
	private ColumnFileEditorInput columnInput;
	private boolean dirty = false;

	private SaveColumnFileEditorAction saveAction;
	private SaveAsColumnFileEditorAction saveasAction;

	// ColumnFile columnFile=null;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof ColumnFileEditorInput) {
			columnInput = (ColumnFileEditorInput) input;
		} else if (input instanceof FileEditorInput) {
			IPath ipath = ((FileEditorInput) input).getPath();
			if (ipath != null) {
				columnInput = new ColumnFileEditorInput(ipath.toString());
			}

		}
		if (columnInput != null) {
			this.setPartName(columnInput.getColumn().getFileName());
			columnInput.getColumn().addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		// To do
		xyplot = new ColFileXYPlot(parent, "", "X", "Y");
		xyplot.addColumnFile(columnInput.getColumn());
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);

		makeActions();
		// contributetoActionBar();
	}

	/*
	 * private void contributetoActionBar() { IActionBars bars =
	 * this.getEditorSite().getActionBars();
	 * fillLocalToolBar(bars.getToolBarManager());
	 * 
	 * 
	 * }
	 * 
	 * 
	 * private void fillLocalToolBar(IToolBarManager toolBarManager) {
	 * toolBarManager.add(saveAction); toolBarManager.add(saveasAction); }
	 */

	private void makeActions() {
		saveAction = new SaveColumnFileEditorAction() {
			@Override
			public void run(IColumnFileEditor editor) {
				((ColumnFilePlotEditor) editor).doSave(null);
			}
		};
		saveAction.setProps("Save column file");
		saveasAction = new SaveAsColumnFileEditorAction() {
			@Override
			public void run(IColumnFileEditor editor) {
				((ColumnFilePlotEditor) editor).doSaveAs();
			}
		};

		saveasAction.setProps("Save column file as...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	/**
	 * @return The ColFileXYPlot.
	 */
	public ColFileXYPlot getxyPlot() {
		return xyplot;
	}

	/**
	 * This has been made for transform, add unit cell peaks
	 */
	/*
	 * public void addData(final float x[] ){
	 * if(!Display.getCurrent().isDisposed()){ Display.getCurrent().syncExec(new
	 * Runnable(){
	 * 
	 * public void run() {
	 * 
	 * if(x!= null ){ if(!xyplot.getChart().getY1Axis().equals("") &&
	 * !xyplot.getChart().getXAxis().equals("")){
	 * if(dataViewForUniccells==null){ dataViewForUniccells = new JLDataView();
	 * xyplot.getVectordataview().add(dataViewForUniccells);
	 * dataViewForUniccells.setMarker( JLDataView.MARKER_VERT_LINE);
	 * dataViewForUniccells.setLineWidth(0);
	 * dataViewForUniccells.setMarkerColor( java.awt.Color.MAGENTA); }else{
	 * dataViewForUniccells.reset(); //if we cleared plot, unit cell
	 * doesn'exists if(!xyplot.getVectordataview().contains(
	 * dataViewForUniccells)){ xyplot.getVectordataview().add(
	 * dataViewForUniccells); } } for(int i=0; i<x.length; i++){
	 * dataViewForUniccells.add(new Point2D.Double(x[i],0));
	 * dataViewForUniccells.setMarkerSize(10);
	 * dataViewForUniccells.setName("Unit cell"); }
	 * xyplot.getChart().getY1Axis().addDataView(dataViewForUniccells);
	 * xyplot.getChart().getXAxis().setName("tth"); String[] label={"tth", ""};
	 * xyplot.plotData(label); xyplot.getChart().getY1Axis().setName(""); }
	 * }else { xyplot.getChart().setNoValueString("No value to display"); }
	 * xyplot.repaint(); } }); } }
	 */

	/**
	 * open a new column file and add it to the list of column files to plot
	 * 
	 * @param fileName
	 *            - name of column file
	 */
	/*
	 * public void openColumnFile(String fileName) { try { columnFile = new
	 * ColumnFile(fileName); columnFile.addPropertyChangeListener(this);
	 * columnFile.setFileName(fileName); xyplot.addColumnFile(columnFile); }
	 * catch (JepException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */

	/*
	 * public void browseColumnFile() { IWorkbenchWindow activeWindow =
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow(); if (activeWindow !=
	 * null) { try { FileDialog fileDlg = new FileDialog(new Shell(),
	 * SWT.MULTI); // Change the title bar textfileDlg.setText(
	 * "Select one or more file(s) containing columns of data to plot"); String
	 * file = fileDlg.open(); if (file != null){ String[] files =
	 * fileDlg.getFileNames(); for (int i=0; i<files.length; i++) { file =
	 * fileDlg.getFilterPath()+File.separatorChar+files[i];
	 * openColumnFile(file); } } } catch (Exception e) {
	 * MessageDialog.openError(activeWindow.getShell(), "Error",
	 * "Error opening view:" + e.getMessage()); } }
	 * 
	 * }
	 */
	/**
	 * listen for property change events fired by the Column File since we use
	 * an input for Editors
	 */
	/*
	 * public void propertyChange(PropertyChangeEvent event) {
	 * if(((String)event.getProperty()).equals(IPropertyVarKeys.ADDCOLUMN)){
	 * xyplot.addColumnFile(((ColumnFile)event.getNewValue())); } else
	 * if(((String)event.getProperty()).equals(IPropertyVarKeys.UPDATECOLUMN)){
	 * xyplot.updateColumnFile(((ColumnFile)event.getNewValue())); } else
	 * if(((String)event.getProperty()).equals(IPropertyVarKeys.ADDUNITCELL)){
	 * //addData(((ColumnFile)event.getNewValue()).getUnitCell()); }else
	 * if(((String)event.getProperty()).equals("PlotData")){ //July, 8 2008 plot
	 * last columns computed, i.e. tth eta String[] xyLabel =
	 * (String[])event.getNewValue(); xyplot.plotData(xyLabel); }else
	 * if(((String)event.getProperty()).equals("removeAll")){ //July, 11 2008
	 * remove existing plots for transformer
	 * 
	 * xyplot.clearAllPlots(); }
	 * 
	 * }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		try {
			getSite().getWorkbenchWindow().getSelectionService()
					.removeSelectionListener((ISelectionListener) this);
			columnInput.getColumn().removePropertyrChangeListener(this);
			if (xyplot != null) {
				xyplot.dispose();
			}
			xyplot = null;
		} finally {
			super.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != this) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sSelection = (IStructuredSelection) selection;

				Object first = sSelection.getFirstElement();
				Object[] list = sSelection.toArray();
				// System.out.println("user has selected: " + first);
				if (first instanceof float[]) {
					float[] r = (float[]) first;
					double[] indices = null;
					if (columnInput.getColumn() != null) {
						int indexColumnId = columnInput.getColumn()
								.getColumnIDIndex();
						if (list.length > 1 && indexColumnId >= 0) {

							indices = new double[list.length];

							for (int j = 0; j < list.length; j++) {

								indices[j] = (double) ((float[]) list[j])[indexColumnId];
							}
						} else if (indexColumnId >= 0) {
							indices = new double[1];
							indices[0] = (double) r[indexColumnId];

						}

						// Now you have to color the selected spot
						// get its spot_id
						if (indices != null && indices.length > 0) {
							xyplot.markSelectedRows(indices, columnInput
									.getColumn().getColumnfileId());
						}
					}
				}

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		columnInput.save();
		dirty = false;
		firePropertyChange(PROP_DIRTY);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		if (columnInput.saveAs()) {
			dirty = false;
			firePropertyChange(PROP_DIRTY);
			this.setPartName(columnInput.getColumn().getFileName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return dirty;
	}

	/**
	 * 
	 * @return unique id
	 */
	public static String getId() {
		return ID;
	}

	/*
	 * private void fillLocalPullDown(IMenuManager manager) { //
	 * manager.add(saveAction); manager.add(saveasAction); }
	 * 
	 * private void fillLocalToolBar(IToolBarManager manager) {
	 * manager.add(saveasAction); // manager.add(saveAction);
	 * 
	 * }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveOnCloseNeeded()
	 */
	@Override
	public boolean isSaveOnCloseNeeded() {

		return true;
	}

	/*
	 * private void makeActions() {
	 * 
	 * saveasAction = new Action() { public void run() { doSaveAs(); } }; //
	 * saveasAction.setText("Save as");
	 * saveasAction.setToolTipText("Save this column file as ... ");
	 * saveasAction.setImageDescriptor(saveAs);
	 * 
	 * }
	 */

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent arg0) {
		if (arg0.getProperty().equals(IPropertyVarKeys.UPDATECOLUMN)) {
			dirty = true;
			firePropertyChange(PROP_DIRTY);
			xyplot.updateChart();

		} else if (arg0.getProperty().equals(IPropertyVarKeys.PROPDIRTY)) {
			dirty = (Boolean) arg0.getNewValue();
			firePropertyChange(PROP_DIRTY);
			this.setPartName(columnInput.getColumn().getFileName());

		} else if (arg0.getProperty().equals(IPropertyVarKeys.ADDCOLUMN)) {

			xyplot.addColumnFile(columnInput.getColumn());
		}

	}
}
