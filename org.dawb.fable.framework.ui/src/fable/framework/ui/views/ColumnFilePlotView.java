/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.views;

import java.awt.geom.Point2D;
import java.io.File;

import jep.JepException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import fable.framework.imageprint.JLChartActions;
import fable.framework.internal.IPropertyVarKeys;
import fable.framework.toolbox.FableUtils;
import fable.framework.ui.rcp.Activator;
import fable.python.ColumnFile;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;

/**
 * ColumnFilePlotView will create a view with an xy plot of a columnfile. A
 * columnfile is a file containing columns of data to plot in ascii format.
 * Columnfiles are produced by many Fable programs e.g. by ImageD11 and PolyXSim
 * for example. The view is very simple, all the work is done in the
 * ColFileXYPlot class. The view can load a new file, and listen for changes to
 * a ColumnFile.
 * 
 * @author Andy Gotz + Gaelle Suchet
 * 
 */
public class ColumnFilePlotView extends ViewPart implements
		IPropertyChangeListener {

	/***************** Attributes **********************************************/
	private ColFileXYPlot xyplot;
	public static ColumnFilePlotView view;
	public static final String ID = "fable.framework.ui.views.ColumnFilePlotView";
	public static int viewCount = 0;
	// private JLDataView current_dataView = null;
	// private Vector<ColumnFile> array_columns = new Vector<ColumnFile>();
	// private HashMap<String, JLDataView> dataToPlot = new HashMap<String,
	// JLDataView>();
	private JLDataView dataViewForUniccells;
	private ColumnFile columnFile = null;
	private Action showOptionsAction;
	/**
	 * Extra actions for the chart.
	 */
	private JLChartActions actions;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		view = this;
		setPartName("Column File Plot " + Integer.toString(viewCount));
		xyplot = new ColFileXYPlot(parent, "Column File Plot "
				+ Integer.toString(viewCount), "X", "Y");

		// Create the actions
		actions = new JLChartActions(parent.getDisplay(), xyplot.getChart());

		makeActions();
		contributeToActionBars();
	}

	/**
	 * Make the actions used by this view.
	 */
	private void makeActions() {
		// Plot options. Implemented as a toggle since it can also be done via
		// the plot context menu and this avoids keeping track of the state
		// between AWT and SWT actions.
		showOptionsAction = new Action("Toggle Plot Options") {
			public void run() {
				if (xyplot == null)
					return;
				xyplot.setOptionsShowing(!xyplot.getOptionsShowing());
			}
		};
		showOptionsAction.setToolTipText("Toggle showing plot options or "
				+ "having plot occupy entire area");
		showOptionsAction.setImageDescriptor(Activator
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						"images/settings.gif"));
	}

	/**
	 * Fill the local menu and tool bar.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalMenu(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill the local menus.
	 * 
	 * @param manager
	 */
	private void fillLocalMenu(IMenuManager manager) {
		MenuManager subMenuManager = new MenuManager("Print");
		manager.add(subMenuManager);
		subMenuManager.add(actions.printSetupAction);
		subMenuManager.add(actions.printPreviewAction);
		subMenuManager.add(actions.printAction);

		subMenuManager = new MenuManager("Edit");
		manager.add(subMenuManager);
		subMenuManager.add(actions.copyAction);

		manager.add(new Separator());

		manager.add(showOptionsAction);
	}

	/**
	 * Fills the local tool bar.
	 * 
	 * @param manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showOptionsAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (xyplot != null) {
			xyplot.dispose();
		}
		xyplot = null;

		if (columnFile != null) {
			columnFile.removePropertyrChangeListener(this);
		}
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
	public void addData(final float x[]) {
		if (!Display.getCurrent().isDisposed()) {
			Display.getCurrent().syncExec(new Runnable() {

				public void run() {

					if (x != null) {
						if (!xyplot.getChart().getY1Axis().equals("")
								&& !xyplot.getChart().getXAxis().equals("")) {
							if (dataViewForUniccells == null) {
								dataViewForUniccells = new JLDataView();
								xyplot.getVectordataview().add(
										dataViewForUniccells);
								dataViewForUniccells
										.setMarker(JLDataView.MARKER_VERT_LINE);
								dataViewForUniccells.setLineWidth(0);
								dataViewForUniccells
										.setMarkerColor(java.awt.Color.MAGENTA);
							} else {
								dataViewForUniccells.reset();
								// if we cleared plot, unit cell doesn'exists
								if (!xyplot.getVectordataview().contains(
										dataViewForUniccells)) {
									xyplot.getVectordataview().add(
											dataViewForUniccells);
								}
							}
							for (int i = 0; i < x.length; i++) {
								dataViewForUniccells.add(new Point2D.Double(
										x[i], 0));
								dataViewForUniccells.setMarkerSize(10);
								dataViewForUniccells.setName("Unit cell");
							}
							xyplot.getChart().getY1Axis().addDataView(
									dataViewForUniccells);
							xyplot.getChart().getXAxis().setName("tth");
							String[] label = { "tth", "" };
							xyplot.plotData(label);
							xyplot.getChart().getY1Axis().setName("");
						}
					} else {
						xyplot.getChart().setNoValueString(
								"No value to display");
					}
					xyplot.repaint();
				}
			});
		}
	}

	/**
	 * open a new column file and add it to the list of column files to plot
	 * 
	 * @param fileName
	 *            - name of column file
	 */
	public void openColumnFile(String fileName) {
		try {
			columnFile = new ColumnFile(fileName);
			columnFile.addPropertyChangeListener(this);
			// columnFile.setFileName(fileName);
			xyplot.addColumnFile(columnFile);
		} catch (Throwable ex) {
			FableUtils.excMsg(this, "Error creating ColumnFile", ex);
		}
	}

	/**
	 * set column file and add it to the plot
	 * 
	 * @param _columnFile
	 *            - new column file
	 */
	public void setColumnFile(ColumnFile _columnFile) {
		columnFile = _columnFile;
		columnFile.addPropertyChangeListener(this);
		if (columnFile.getFileName() != null) {
			xyplot.addColumnFile(columnFile);
			setPartName(columnFile.getFileName());
		}
	}

	public void browseColumnFile() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			try {
				FileDialog fileDlg = new FileDialog(new Shell(), SWT.MULTI);
				// Change the title bar text
				fileDlg
						.setText("Select one or more file(s) containing columns of data to plot");
				String file = fileDlg.open();
				if (file != null) {
					String[] files = fileDlg.getFileNames();
					for (int i = 0; i < files.length; i++) {
						file = fileDlg.getFilterPath() + File.separatorChar
								+ files[i];
						openColumnFile(file);
					}
				}
			} catch (Exception ex) {
				FableUtils.excMsg(this, "Error opening ColumnFile", ex);
			}
		}

	}

	/**
	 * listen for property change events fired by the Column File
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (((String) event.getProperty()).equals(IPropertyVarKeys.ADDCOLUMN)) {
			xyplot.addColumnFile(((ColumnFile) event.getNewValue()));
		} else if (((String) event.getProperty())
				.equals(IPropertyVarKeys.UPDATECOLUMN)) {
			xyplot.updateColumnFile(((ColumnFile) event.getNewValue()));
		} else if (((String) event.getProperty())
				.equals(IPropertyVarKeys.ADDUNITCELL)) {
			addData(((ColumnFile) event.getNewValue()).getUnitCell());
		} else if (((String) event.getProperty()).equals("PlotData")) {
			// July, 8 2008 plot last columns computed, i.e. tth eta
			String[] xyLabel = (String[]) event.getNewValue();
			xyplot.plotData(xyLabel);
		} else if (((String) event.getProperty()).equals("removeAll")) {
			// July, 11 2008 remove existing plots for transformer

			xyplot.clearAllPlots();
		}

	}

	public void saveAs() {
		FileDialog fileDlg = new FileDialog(Display.getCurrent()
				.getActiveShell(), SWT.SAVE);

		String[] filterExt = { "*.flt", "*.*" };
		String[] filterNames = { "filtered files (*.flt)", "All Files (*.*)" };
		fileDlg.setFilterExtensions(filterExt);
		fileDlg.setFilterNames(filterNames);
		// fileDlg.setFilterPath();

		// Change the title bar text
		fileDlg.setText("Save file");
		String file = fileDlg.open();

		if (file != null) {
			file = fileDlg.getFilterPath()
					+ System.getProperty("file.separator")
					+ fileDlg.getFileName();
			if (!xyplot.currentColumnFile.saveColumnFile(file)) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Save file", "Column file couldn't be saved");
			}

		}

	}

}
