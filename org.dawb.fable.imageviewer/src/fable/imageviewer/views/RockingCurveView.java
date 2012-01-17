/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * source code to implement the ZoomLineView view for plotting a user selected line
 */

package fable.imageviewer.views;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.Vector;

import javax.swing.JApplet;

import jep.JepException;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.dawb.fabio.FableJep;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.navigator.controller.SampleController;
import fable.framework.toolbox.FableUtils;
import fable.python.Sample;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;

/**
 * The RockingCurveView class implements an eclipse view for plotting a rocking
 * curve of a user selected area in an image. A rocking curve is a plot of the
 * integrated intensity of an area in an image for a series of images usually
 * taken at different orientations of the sample. The sample orientation is
 * normally represented by the angle "omega".
 * 
 * It uses JLChart to do the plotting. It will plot the data set as a line. The
 * image view gathers the user's selection and sends the data to plot to the
 * RockingCurveView. The user can change the range of images over which to
 * integrate and plot. Fabio is used to read the image and integrate the
 * intensity.
 * 
 * @author goetz
 */

public class RockingCurveView extends ViewPart {

	public final static String ID = "fable.imageviewer.views.RockingCurveView";
	private JLDataView dataView;
	private JLChart chart;
	String title;
	private int rockStart = -1, rockCenter = -1, rockEnd = -1, rockRange;
	private int xAxis = 0, rockArea[] = new int[4];
	private float rockX[], rockIntensity[];
	private Spinner rockStartSpinner, rockEndSpinner;
	private Label rockStartLabel, rockEndLabel;
	private Button updateButton, averageButton;
	private Combo xAxisCombo;
	public static RockingCurveView view;
	private Composite swtAwtComponent;
	public SampleController controller = SampleController.getController();
	public Sample sample = null;
	private Frame chartFrame;
	private JApplet chartContainer;
	private static final Logger logger = LoggerFactory.getLogger(RockingCurveView.class);
	private IProgressMonitor jobMonitor;
	private final String[] imageNumber = { "image number" };
	private String[] xAxisItems = imageNumber;
	private boolean jobRunning = false;
	private boolean average = true;

	// private Composite parent;

	public RockingCurveView() {
		super();
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
		view = this;
		// this.parent = parent;
		parent.setLayout(new GridLayout());
		createChartFrame(parent);
		createChart();
		Composite controlPanelComposite = new Composite(parent, SWT.NULL);
		GridLayout controlGridLayout = new GridLayout();
		controlGridLayout.numColumns = 8;
		controlPanelComposite.setLayout(controlGridLayout);
		controlPanelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, false));
		Label xAxisLabel = new Label(controlPanelComposite, SWT.NULL);
		xAxisLabel.setText("X Axis");
		xAxisCombo = new Combo(controlPanelComposite, SWT.NULL);
		xAxisCombo.setItems(xAxisItems);
		xAxisCombo.select(0);
		xAxis = 0;
		xAxisCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				xAxis = xAxisCombo.getSelectionIndex();
			}
		});
		rockStartLabel = new Label(controlPanelComposite, SWT.NULL);
		rockStartLabel.setText("Start");
		rockStartSpinner = new Spinner(controlPanelComposite, SWT.NULL);
		rockEndLabel = new Label(controlPanelComposite, SWT.NULL);
		rockEndLabel.setText("End");
		rockEndSpinner = new Spinner(controlPanelComposite, SWT.NULL);
		averageButton = new Button(controlPanelComposite, SWT.CHECK);
		averageButton.setText("Average");
		averageButton.setSelection(true);
		average = true;
		averageButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (averageButton.getSelection()) {
					average = true;
				} else {
					average = false;
				}
			}
		});
		updateButton = new Button(controlPanelComposite, SWT.NULL);
		updateButton.setText("Update");
		updateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				rockStart = rockStartSpinner.getSelection();
				rockEnd = rockEndSpinner.getSelection();
				runIntegratePlotJob();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	private void createChartFrame(Composite parent) {
		swtAwtComponent = new Composite(parent, SWT.EMBEDDED
				| SWT.NO_BACKGROUND);
		swtAwtComponent.setLayout(new GridLayout());
		swtAwtComponent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL));
		chartFrame = SWT_AWT.new_Frame(swtAwtComponent);
		chartContainer = new JApplet();
		chartFrame.add(chartContainer);
	}

	public void createChart() {
		chart = new JLChart();
		chart.setHeader("Rocking Curve Plot");
		chart.setHeaderFont(new Font("Dialog", Font.BOLD, 18));
		chart.getY1Axis().setName("integrated intensity");
		chart.getY1Axis().setAutoScale(true);
		chart.getXAxis().setAutoScale(true);
		chart.getXAxis().setName("image number");
		chart.getXAxis().setGridVisible(true);
		chart.getXAxis().setSubGridVisible(true);
		chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
		chart.getY1Axis().setGridVisible(true);
		chart.getY1Axis().setSubGridVisible(true);
		dataView = new JLDataView();
		dataView
				.setName("integrated intensity [" + rockArea[0] + ","
						+ rockArea[1] + "] - [" + rockArea[2] + ","
						+ rockArea[3] + "]");
		chart.getY1Axis().addDataView(dataView);
		chartContainer.add(chart);
	}

	/**
	 * Set the new area, center and sample to integrate and display it as a line
	 * plot
	 * 
	 * @param title
	 *            - title to display with plot
	 * @param center
	 *            - center image
	 * @param area
	 *            - area to integrate
	 * @throws Throwable 
	 */
	public void setCenterArea(String _title, int center, int y1, int z1,
			int y2, int z2) throws Throwable {
		if (!jobRunning) {
			jobRunning = true;
			if (sample == null || sample != controller.getCurrentsample()) {
				sample = controller.getCurrentsample();
				if (sample == null) {
					FableUtils.errMsg(this, "Rocking curve requires a Sample."
							+ "  Cannot find a Sample.");
					return;
				}
				try {
					String[] headerItems = sample.getFilteredfiles().get(0)
							.getKeys();
					xAxisItems = new String[1 + headerItems.length];
					xAxisItems[0] = "image number";
					for (int i = 0; i < headerItems.length; i++) {
						xAxisItems[i + 1] = headerItems[i];
					}
				} catch (FabioFileException ex) {
					FableUtils.excNoTraceMsg(this, "Error setting center area",
							ex);
				} catch (JepException ex) {
					FableUtils.excNoTraceMsg(this, "Error setting center area",
							ex);
				}
				xAxisCombo.setItems(xAxisItems);
				xAxisCombo.select(0);
				xAxis = 0;
			}
			title = _title;
			rockStartSpinner.setMaximum(sample.getFilteredfiles().size() - 1);
			rockEndSpinner.setMaximum(sample.getFilteredfiles().size() - 1);
			rockArea[0] = y1;
			rockArea[1] = z1;
			rockArea[2] = y2;
			rockArea[3] = z2;
			rockCenter = center;
			rockRange = rockEnd - rockStart;
			if (rockRange <= 3) {
				rockStart = center - 3;
			} else {
				rockStart = center - rockRange / 2;
			}
			if (rockRange <= 3) {
				rockEnd = center + 3;
			} else {
				rockEnd = center + (rockRange + 1) / 2;
			}
			checkRockRange();
			runIntegratePlotJob();
		}
	}

	public void checkRockRange() {
		if (rockStart < 0)
			rockStart = 0;
		if (rockEnd > sample.getFilteredfiles().size() - 1)
			rockEnd = sample.getFilteredfiles().size() - 1;
		if (rockStart > rockEnd) {
			int temp = rockStart;
			rockStart = rockEnd;
			rockEnd = temp;
		}
		// logger.debug("rockCenter = "+rockCenter+" rockEnd = "+rockEnd+"
		// rockStart = "+rockStart);
		rockStartSpinner.setSelection(rockStart);
		rockEndSpinner.setSelection(rockEnd);
	}

	public void runIntegratePlotJob() {
		Job job = new Job("Integrate rocking curve for sample "
				+ sample.getDirectoryName() + " ... ") {
			protected IStatus run(IProgressMonitor monitor) {
				jobMonitor = monitor;
				monitor.beginTask("Integrate rocking curve for sample "
						+ sample.getDirectoryName() + " ... ", rockEnd
						- rockStart + 1);
				if (Integrate() != 0) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						Plot();
					}
				});
				monitor.done();
				jobRunning = false;
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();

	}

	// KE: Is this method name supposed to start with a cap?
	public int Integrate() {
		FableJep fableJep = null;
		try {
			fableJep = FableJep.getFableJep();
		} catch (Throwable ex) {
			FableUtils.excMsg(this, "Error in Integrate creating FableJep", ex);
		}
		Vector<FabioFile> fabioFiles = sample.getFilteredfiles();
		int width, height, npoints;
		FabioFile fabioFile;
		fabioFile = fabioFiles.get(rockCenter);
		try {
			width = fabioFile.getWidth();

			height = fabioFile.getHeight();

			npoints = (rockArea[2] - rockArea[0]) * (rockArea[3] - rockArea[1]);
			rockX = new float[rockEnd - rockStart + 1];
			rockIntensity = new float[rockEnd - rockStart + 1];
			int rockingCurveErrors = 0;
			for (int i = rockStart; i <= rockEnd; i++) {
				jobMonitor.worked(1);
				float imageFloat[];
				fabioFile = fabioFiles.get(i);
				imageFloat = fabioFile.getImageAsFloat(fableJep);
				/*
				 * only do rocking curve for those files which have the same
				 * size as the center file
				 */
				if (fabioFile.getWidth() == width
						&& fabioFile.getHeight() == height) {
					float sum = 0;
					for (int z = rockArea[1]; z < rockArea[3]; z++) {
						int zOffset;
						zOffset = width * z;
						for (int y = rockArea[0]; y < rockArea[2]; y++) {
							sum = sum + imageFloat[zOffset + y];
						}
					}
					if (average) {
						rockIntensity[i - rockStart] = sum / (float) npoints;
					} else {
						rockIntensity[i - rockStart] = sum;
					}
				} else {
					rockIntensity[i - rockStart] = Float.NaN;
					rockingCurveErrors++;
				}
				if (xAxis != 0) {
					rockX[i - rockStart] = Float.NaN;
					String keyValue = "";
					try {
						if (!fabioFile.headerRead)
							fabioFile.loadHeader(fableJep);
						keyValue = fabioFile.getValue(xAxisItems[xAxis]);
					} catch (FabioFileException ex) {
						FableUtils.excNoTraceMsg(this,
								"Error in Integrate loading header", ex);
					}
					try {
						rockX[i - rockStart] = Float.parseFloat(keyValue);
					} catch (NumberFormatException ex) {
						FableUtils.excNoTraceMsg(this,
								"Error in Integrate loading header", ex);
						rockingCurveErrors++;
					}
				} else {
					rockX[i - rockStart] = i;
				}
			}

			if (rockingCurveErrors != 0) {
				final String message;
				message = rockingCurveErrors
						+ " files will not be plotted on the rocking curve "
						+ "because their size differs or the key chosen for "
						+ "the x axis does not exist";
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openConfirm(Display.getDefault()
								.getActiveShell(), "Confirm", message);
					}
				});
				return 1;
			}
		} catch (Throwable ex) {
			FableUtils.excNoTraceMsg(this, "Error in Integrate", ex);
		}
		return 0;
	}

	public void Plot() {
		if (average) {
			chart.getY1Axis().setName("average intensity");
		} else {
			chart.getY1Axis().setName("integrated intensity");
		}
		chart.getXAxis().setName(xAxisItems[xAxis]);
		chart.setHeader(title);
		double[] x = new double[rockEnd - rockStart + 1];
		double[] y = new double[rockEnd - rockStart + 1];
		for (int i = 0; i < rockEnd - rockStart + 1; i++) {
			x[i] = rockX[i];
			y[i] = Double.valueOf(rockIntensity[i]);
		}
		dataView.setData(x, y);
		dataView
				.setName("integrated intensity [" + rockArea[0] + ","
						+ rockArea[1] + "] - [" + rockArea[2] + ","
						+ rockArea[3] + "]");
		Dimension chartSize = chartContainer.getSize();
		chart.setSize(chartSize);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chartContainer.repaint();
			}
		});
	}
}
