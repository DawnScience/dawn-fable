/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JApplet;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import fable.framework.navigator.controller.SampleController;
import fable.framework.navigator.toolBox.IVarKeys;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.preferences.PreferenceConstants;
import fable.imageviewer.rcp.Activator;
import fable.python.Sample;
import fable.python.SampleException;
import fr.esrf.tangoatk.widget.util.chart.IJLChartListener;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLChartEvent;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;

/**
 * This class plots header keys of selected samples in navigator. For each
 * sample, a several plots can be done. X and Y are selected in combos. X and Y
 * combos are loaded with current sample and its keys.
 * 
 * @author SUCHET
 */
public class HeaderPlotView extends ViewPart implements
		IPropertyChangeListener, IJLChartListener {
	private static final long serialVersionUID = 1L;
	public static HeaderPlotView view;
	public static final String ID = "fable.imageviewer.views.HeaderPlotView";
	// An array of c1olumn file. With JLChart, can only have 3 axis(X, Y, Y1)?
	// A list of keys to plot
	// A JLDataView is associated with a columnfile
	HashMap<String, JLDataView> dataToPlot = new HashMap<String, JLDataView>();
	// HeaderFile header=null;

	SampleController controller;
	/***************************** PLOT ******************************************/
	private static Composite swtAwtComponent;
	private static java.awt.Frame chartFrame;
	private static JApplet chartContainer;
	private JLChart chart;
	/*
	 * This hashmap contains a list of samples available in navigator It allows
	 * us to associate a sample with a list of JLdataView. HashMap<String,
	 * ArrayList<JLDataView>> : keys is either "Y1" or "Y2" to get the list of
	 * data associated with Y1 or Y2 axis
	 */
	private HashMap<Sample, HashMap<JLAxis, ArrayList<JLDataView>>> sampleAndItsData = new HashMap<Sample, HashMap<JLAxis, ArrayList<JLDataView>>>();

	private JLDataView currentdataview;
	private String chartName, syAxisLabel, sxAxisLabel;
	static int iDataColor = 0;
	Combo sampleCombo, xKeysCombo, yKeysCombo, y1dataviews, y2dataviews;
	public Button plotDiff, resetButton;
	public Button removeDataViewY1, removeDataViewY2;
	Group grpPlot;
	int currentXIndex = 0;
	int currentYIndex = 0;
	Button y1Button, y2Button;
	double[] currentXData;
	double[] currentYData;
	private Sample currentSample;
	int dataMarkers[] = { JLDataView.MARKER_BOX, JLDataView.MARKER_CIRCLE,
			JLDataView.MARKER_CROSS, JLDataView.MARKER_DIAMOND,
			JLDataView.MARKER_DOT, JLDataView.MARKER_STAR };
	static int iDataMarker = 0;
	Color dataColors[] = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.YELLOW };
	ImageDescriptor img = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "icons/subtractplot.gif");
	ImageDescriptor imgchartDataviewOptions = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/graphView.gif");

	Image imagePlotsubtract, imagePlotsubtractY2, imageDataViewOptions1,
			imageDataViewOptions2;
	ImageDescriptor imgadd = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "icons/addplot.gif");
	Image imagePlotadd;

	ImageDescriptor imgClear = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "icons/delete.gif");
	Image imagePlotclear;

	@Override
	public void createPartControl(Composite parent) {
		view = this;
		/*************************** PLOT ************************************/
		createHeaderPlot(parent, "Header keys", "X", "Y");
		/**********************************************************************/

		controller = SampleController.getController();
		controller.addPropertyChangeListener(this);

		currentSample = controller.getCurrentsample();
		if (currentSample != null) {
			populateListsKeys();
		}
		Vector<Sample> vsample = controller.getSamples();
		if (vsample != null && vsample.size() > 0) {
			for (Iterator<Sample> it = vsample.iterator(); it.hasNext();) {
				Sample s = (Sample) it.next();
				sampleAndItsData.put(s, null);

			}

		}

		populateCombosample();
		enableGroupDataViews();

	}

	/**
	 * Create plot and its option group
	 * 
	 * @param parent
	 * @param name
	 * @param xLabel
	 * @param yLabel
	 */
	private void createHeaderPlot(Composite parent, String name, String xLabel,
			String yLabel) {
		// dataView=new JLDataView();
		chartName = name;
		syAxisLabel = yLabel;
		sxAxisLabel = xLabel;

		parent.setLayoutData(new GridData());
		parent.setLayout(new GridLayout(2, false));
		parent
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
						6));
		createChartFrame(parent);
		createChart();
		createOptionsGroup(parent);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
				new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						if ((String) event.getProperty() != null) {
							String value = (String) event.getNewValue();
							if (((String) event.getProperty())
									.equals(PreferenceConstants.P_XLABEL)) {
								if (value != null && !value.equals("")) {
									setSxAxisLabel(value);
								}
							} else if (((String) event.getProperty())
									.equals(PreferenceConstants.P_XLABEL)) {
								if (value != null && !value.equals("")) {
									setSyAxisLabel(value);
								}
							}
						}
					}
				});
	}

	/**
	 * Create the JLChart and initialise it with default settings e.g. autoscale
	 */
	public void createChart() {

		chart = new JLChart();
		chart.setToolTipText("Zoom: Ctrl + select zone with mouse ");
		chart.setHeader(chartName);
		chart.setHeaderFont(new Font("Dialog", Font.BOLD, 18));
		chart.getY1Axis().setName(syAxisLabel);
		chart.getY1Axis().setAutoScale(true);
		chart.getY1Axis().setGridVisible(true);
		chart.getY1Axis().setSubGridVisible(true);

		chart.getY2Axis().setName(syAxisLabel);
		chart.getY2Axis().setAutoScale(true);
		chart.getY2Axis().setGridVisible(true);
		chart.getY2Axis().setSubGridVisible(true);

		chart.getXAxis().setName(sxAxisLabel);
		chart.getXAxis().setGridVisible(true);
		chart.getXAxis().setSubGridVisible(true);
		chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
		chart.getXAxis().setAutoScale(true);
		chartContainer.add(chart);
		chart.setJLChartListener(this);

	}

	/*
	 * create an SWT_AWT frame to hold the JLChart
	 * 
	 * @param parent
	 */
	private static void createChartFrame(Composite parent) {
		swtAwtComponent = new Composite(parent, SWT.EMBEDDED
				| SWT.NO_BACKGROUND);
		GridData gdlist = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdlist.verticalSpan = 6;
		gdlist.horizontalSpan = 2;
		swtAwtComponent.setLayout(new GridLayout());
		swtAwtComponent.setLayoutData(gdlist);
		chartFrame = SWT_AWT.new_Frame(swtAwtComponent);
		chartContainer = new JApplet();
		chartFrame.add(chartContainer);
	}

	/**
	 * Group options for JLChart. For instance, a combo with a list of dataview.
	 * allow user to delete selected dataview. And a clear all button
	 * 
	 * @param parent
	 */
	private void createOptionsGroup(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
				SWT.V_SCROLL | SWT.H_SCROLL);
		Composite mainComposite = new Composite(scrolledComposite, SWT.None);
		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false, 1, 1));
		mainComposite.setLayout(new GridLayout(2, false));
		mainComposite.setLayoutData(new GridData());
		mainComposite.pack();

		Group groupChartOptions = new Group(mainComposite, SWT.NONE);
		groupChartOptions.setLayout(new GridLayout(5, false));
		// ((GridLayout) (groupChartOptions.getLayout())).numColumns = 6;
		GridData gridChartOpt = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridChartOpt.verticalSpan = 1;
		gridChartOpt.horizontalSpan = 1;
		groupChartOptions.setLayoutData(gridChartOpt);
		groupChartOptions.setText("Samples");
		/** ****************************Column to plot *************************/
		new Label(groupChartOptions, SWT.NONE).setText("Samples");
		sampleCombo = new Combo(groupChartOptions, SWT.READ_ONLY);

		sampleCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		((GridData) sampleCombo.getLayoutData()).horizontalSpan = 4;
		sampleCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				Combo cbo = ((Combo) e.widget);
				String text = cbo.getText();
				if (text != null) {
					currentSample = (Sample) cbo.getData(text);
				}
				// populate jlDataviews y1 and y2 with it datas and header keys
				// too
				populateJLDataViews(currentSample);
				populateListsKeys();

			}
		});
		createListsKeys_X(groupChartOptions);
		createListsKeys_Y(groupChartOptions);

		plotDiff = new Button(groupChartOptions, SWT.CHECK);
		plotDiff.setText("Difference");
		plotDiff
				.setToolTipText("plot of the difference between the adjacent values");
		plotDiff.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		((GridData) plotDiff.getLayoutData()).horizontalSpan = 1;
		plotDiff.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				repaint();
			}
		});

		// LEFT GROUP that lists VataViews for selected sample in combo
		grpPlot = new Group(mainComposite, SWT.NONE);
		grpPlot.setText("Data");
		grpPlot.setLayout(new GridLayout(5, false));
		GridData gdPlot = new GridData(SWT.FILL, SWT.CENTER, true, false);

		grpPlot.setLayoutData(gdPlot);

		y1Button = new Button(grpPlot, SWT.PUSH);
		// y1Button.setText("+ Y1");
		imagePlotadd = imgadd.createImage();
		y1Button.setImage(imagePlotadd);
		y1Button.setToolTipText("add next plot to Y1 axis");

		y1Button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imagePlotadd != null && !imagePlotadd.isDisposed()) {
					imagePlotadd.dispose();
				}

			}
		});
		y1Button.setSelection(true);

		y1Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (currentSample == null) {
					currentSample = getSelectedSample();
				}
				addDataView(currentSample, y1dataviews, chart.getY1Axis());
				repaint();

			}
		});
		y1dataviews = new Combo(grpPlot, SWT.READ_ONLY);
		GridData gdDataviews = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdDataviews.horizontalSpan = 1;
		y1dataviews.setLayoutData(gdDataviews);

		removeDataViewY1 = new Button(grpPlot, SWT.PUSH);
		removeDataViewY1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		imagePlotsubtract = img.createImage();
		removeDataViewY1.setToolTipText("Remove from plot");
		removeDataViewY1.setImage(imagePlotsubtract);
		removeDataViewY1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = y1dataviews.getSelectionIndex();
				// Get jlDataView for selected sample
				currentSample = getSelectedSample();
				ArrayList<JLDataView> dataviews = getJLdataView(currentSample,
						chart.getY1Axis());
				if (dataviews != null && index >= 0) {
					removeDataViewFromChart(chart.getY1Axis(), dataviews
							.get(index));
					dataviews.remove(index);
					y1dataviews.remove(index);
					if (iDataMarker > 0) {
						iDataMarker -= 1;
					}
					if (iDataColor > 0) {
						iDataColor -= 1;
					}

				}
				if (index < y1dataviews.getItemCount()) {
					y1dataviews.select(index);
				} else if (y1dataviews.getItemCount() > 0) {
					y1dataviews.select(0);
				}

			}

		});

		removeDataViewY1.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imagePlotsubtract != null
						&& !imagePlotsubtract.isDisposed()) {
					imagePlotsubtract.dispose();
				}

			}
		});

		Button dataViewOption1 = new Button(grpPlot, SWT.PUSH);

		imageDataViewOptions1 = imgchartDataviewOptions.createImage();
		dataViewOption1.setToolTipText("Show data view options");
		GridData gdDatabutton1 = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gdDatabutton1.horizontalSpan = 2;

		dataViewOption1.setLayoutData(gdDatabutton1);
		dataViewOption1.setImage(imageDataViewOptions1);

		dataViewOption1.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageDataViewOptions1 != null
						&& !imageDataViewOptions1.isDisposed()) {
					imageDataViewOptions1.dispose();
				}

			}
		});

		dataViewOption1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = y1dataviews.getSelectionIndex();

				// Get jlDataView for selected sample
				currentSample = getSelectedSample();
				ArrayList<JLDataView> dataviews = getJLdataView(currentSample,
						chart.getY1Axis());

				if (dataviews != null && index >= 0) {
					final JLDataView dataview = dataviews.get(index);
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chart.showDataOptionDialog(dataview);

						}
					});
				}
			}
		});

		// Y2
		y2Button = new Button(grpPlot, SWT.PUSH);
		y2Button.setImage(imagePlotadd);

		// y2Button.setText("+ Y2");
		y2Button.setToolTipText("add next plot to Y2 axis");
		y2Button.setSelection(false);
		y2Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (currentSample == null) {
					currentSample = getSelectedSample();
				}
				addDataView(currentSample, y2dataviews, chart.getY2Axis());

				repaint();

			}
		});
		y2Button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imagePlotadd != null && !imagePlotadd.isDisposed()) {
					imagePlotadd.dispose();
				}

			}
		});
		GridData gdDataviews2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdDataviews2.horizontalSpan = 1;

		y2dataviews = new Combo(grpPlot, SWT.READ_ONLY);
		y2dataviews.setLayoutData(gdDataviews2);

		removeDataViewY2 = new Button(grpPlot, SWT.PUSH);
		removeDataViewY2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		imagePlotsubtractY2 = img.createImage();
		removeDataViewY2.setToolTipText("Remove from plot");
		removeDataViewY2.setImage(imagePlotsubtract);
		removeDataViewY2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				int index = y2dataviews.getSelectionIndex();

				// Get jlDataView for selected sample
				currentSample = getSelectedSample();
				ArrayList<JLDataView> dataviews = getJLdataView(currentSample,
						chart.getY2Axis());
				if (dataviews != null && index >= 0) {
					removeDataViewFromChart(chart.getY2Axis(), dataviews
							.get(index));

					dataviews.remove(index);
					y2dataviews.remove(index);
					if (iDataMarker > 0) {
						iDataMarker -= 1;
					}
					if (iDataColor > 0) {
						iDataColor -= 1;
					}

				}
				if (index < y2dataviews.getItemCount()) {
					y2dataviews.select(index);
				} else if (y2dataviews.getItemCount() > 0) {
					y2dataviews.select(0);
				}
			}

		});
		removeDataViewY2.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imagePlotsubtractY2 != null
						&& !imagePlotsubtractY2.isDisposed()) {
					imagePlotsubtractY2.dispose();
				}

			}
		});
		Button dataViewOption2 = new Button(grpPlot, SWT.PUSH);
		dataViewOption2.setToolTipText("Show data view options");
		imageDataViewOptions2 = imgchartDataviewOptions.createImage();
		dataViewOption2.setImage(imageDataViewOptions1);
		dataViewOption2.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageDataViewOptions2 != null
						&& !imageDataViewOptions2.isDisposed()) {
					imageDataViewOptions2.dispose();
				}

			}
		});
		dataViewOption2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		dataViewOption2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = y2dataviews.getSelectionIndex();
				// Get jlDataView for selected sample
				currentSample = getSelectedSample();
				ArrayList<JLDataView> dataviews = getJLdataView(currentSample,
						chart.getY2Axis());

				if (dataviews != null && index >= 0) {
					final JLDataView dataview = dataviews.get(index);
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chart.showDataOptionDialog(dataview);

						}
					});
				}
			}
		});
		// CLEAR DATAVIEWS FOR SELECTED SAMPLE
		resetButton = new Button(grpPlot, SWT.PUSH);

		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.verticalSpan = 1;

		resetButton.setLayoutData(gd);
		imagePlotclear = imgClear.createImage();
		resetButton.setImage(imagePlotclear);
		// resetButton.setText("Clear");
		resetButton.setToolTipText("Clear plot");
		resetButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false));
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearAllPlots();
			}
		});
		resetButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imagePlotclear != null && !imagePlotclear.isDisposed()) {
					imagePlotclear.dispose();

				}

			}
		});

		scrolledComposite.setMinHeight(Display.getDefault().getPrimaryMonitor()
				.getBounds().height);
		scrolledComposite.setMinWidth(Display.getDefault().getPrimaryMonitor()
				.getBounds().width);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

	}

	/**
	 * Creates combo with header keys for Y axis
	 * 
	 * @param parent
	 */
	private void createListsKeys_X(Composite parent) {

		new Label(parent, SWT.NONE).setText("X axis");
		xKeysCombo = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.LEFT, GridData.CENTER, true, false);
		xKeysCombo.setLayoutData(gdlist);
		xKeysCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String[] sel = ((Combo) e.widget).getItems();
				currentXIndex = ((Combo) e.widget).getSelectionIndex();
				if (currentXIndex < 0) {
					currentXIndex = 0;
				}

				sxAxisLabel = sel[currentXIndex];
				chart.getXAxis().setName(sxAxisLabel);

			}
		});

	}

	/**
	 * Creates combo with header keys for Y axis
	 * 
	 * @param parent
	 */
	private void createListsKeys_Y(Composite parent) {
		new Label(parent, SWT.NONE).setText("Y axis");
		yKeysCombo = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.LEFT, GridData.CENTER, true, false);
		yKeysCombo.setLayoutData(gdlist);
		yKeysCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String[] sel = ((Combo) e.widget).getItems();
				currentYIndex = ((Combo) e.widget).getSelectionIndex();
				if (currentYIndex < 0) {
					currentYIndex = 1;
				}
				syAxisLabel = sel[currentYIndex];

			}
		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		controller.removePropertyChangeListener(this);
		iDataMarker = 0;
		iDataColor = 0;
		super.dispose();
	}

	private Sample getSelectedSample() {
		String text = sampleCombo.getText();
		return (Sample) sampleCombo.getData(text);
	}

	/**
	 * Set values of headers for x and y label to dataview
	 * 
	 * @param dataview
	 */
	public void setData(Sample sample, JLDataView dataview) {
		double[] listX;
		double[] listY;
		try {
			listX = sample.getHeaderValues(sxAxisLabel);
			if (plotDiff.getSelection()) {
				listY = sample.getHeaderValuesDiff(syAxisLabel);
			} else {
				listY = sample.getHeaderValues(syAxisLabel);
			}
			if (listX != null && listY != null) {
				dataview.setXDataSorted(false);
				dataview.setData(listX, listY);
			}
		} catch (SampleException ex) {
			FableUtils.excMsg(this, "Error setting header vlaues", ex);
		}

	}

	/**
	 * Set y axis label
	 * 
	 * @param syAxisLabel
	 */
	public void setSyAxisLabel(String syAxisLabel) {
		if (syAxisLabel != null) {
			this.syAxisLabel = syAxisLabel;
			selectLabelinYCombo(syAxisLabel);
		}
	}

	/**
	 * Set x axis label
	 * 
	 * @param sxAxisLabel
	 */
	public void setSxAxisLabel(String sxAxisLabel) {
		if (sxAxisLabel != null) {
			this.sxAxisLabel = sxAxisLabel;
			selectLabelinXCombo(sxAxisLabel);
		}
	}

	/**
	 * Set x and y list keys with values to plot
	 * 
	 * @param colandValues
	 */
	public void populateListsKeys() {
		xKeysCombo.removeAll();
		yKeysCombo.removeAll();
		String[] titles;
		if (currentSample != null) {
			try {
				titles = currentSample.getKeys();

				for (int i = 0; titles != null && i < titles.length; i++) {
					String key = titles[i];
					xKeysCombo.add(key);
					yKeysCombo.add(key);
				}
				yKeysCombo.setEnabled(yKeysCombo.getItemCount() > 0);
				xKeysCombo.setEnabled(yKeysCombo.getItemCount() > 0);
				if (currentXIndex > xKeysCombo.getItemCount()) {
					currentXIndex = 0;// xKeysCombo.getItemCount() - 1;
				}
				if (currentYIndex > yKeysCombo.getItemCount()) {
					currentYIndex = 0;// yKeysCombo.getItemCount() - 1;
				}
				if (xKeysCombo.getItemCount() > 0) {

					xKeysCombo.select(currentXIndex);
					sxAxisLabel = xKeysCombo.getItem(currentXIndex);
				}
				if (yKeysCombo.getItemCount() > 0) {

					yKeysCombo.select(currentYIndex);
					syAxisLabel = yKeysCombo.getItem(currentYIndex);
				}

			} catch (SampleException ex) {
				FableUtils.excMsg(this, "Unable to load keys for current file",
						ex);
			}
		}
	}

	private ArrayList<JLDataView> getJLdataView(Sample sample, JLAxis axis) {

		HashMap<JLAxis, ArrayList<JLDataView>> jldataAndAxis = sampleAndItsData
				.get(sample);
		ArrayList<JLDataView> dataview = null;
		if (jldataAndAxis != null) {
			dataview = jldataAndAxis.get(axis);

		}
		return dataview;
	}

	/**
	 * Clear all plots
	 */
	public void clearAllPlots() {

		chart.removeAll();
		currentSample = getSelectedSample();

		ArrayList<JLDataView> arrayY1 = getJLdataView(currentSample, chart
				.getY1Axis());
		if (arrayY1 != null) {
			for (Iterator<JLDataView> it = arrayY1.iterator(); it.hasNext();) {
				chart.getY1Axis().removeDataView((JLDataView) it.next());
			}
			arrayY1.removeAll(arrayY1);
		}
		ArrayList<JLDataView> arrayY2 = getJLdataView(currentSample, chart
				.getY2Axis());
		if (arrayY2 != null) {
			for (Iterator<JLDataView> it = arrayY2.iterator(); it.hasNext();) {
				chart.getY2Axis().removeDataView((JLDataView) it.next());
			}
			arrayY2.removeAll(arrayY2);
		}
		repaint();
		iDataColor = 0;
		iDataMarker = 0;
		y1dataviews.removeAll();
		y2dataviews.removeAll();
		populateJLDataViews(currentSample);
	}

	/** *********************************************************************** */
	// Populate/
	/** *********************************************************************** */

	public void addDataView(Sample sample, Combo jlcombo, JLAxis axis) {
		// System.out.println("Adddataview()");
		if (!Display.getCurrent().isDisposed() && sample != null) {
			JLDataView dataview = new JLDataView();
			dataview.setXDataSorted(false);

			if (plotDiff.getSelection()) {

				dataview.setName("Diff: " + sample.getDirectoryName() + " :"
						+ syAxisLabel + " =f(" + sxAxisLabel + ")");
			} else {

				dataview.setName(sample.getDirectoryName() + " :" + syAxisLabel
						+ " =f(" + sxAxisLabel + ")");
			}

			axis.setName(syAxisLabel);
			chart.getXAxis().setName(sxAxisLabel);
			// Add a new dataview to vector if dataview doesn't
			// exist in the vector. otherwise, update plot

			currentdataview = dataview;

			HashMap<JLAxis, ArrayList<JLDataView>> axisAndData = sampleAndItsData
					.get(sample);
			if (axisAndData == null) {
				axisAndData = new HashMap<JLAxis, ArrayList<JLDataView>>();
			}
			;
			// get list of dataviews
			ArrayList<JLDataView> jlDataList = axisAndData.get(axis);
			if (jlDataList == null) {
				jlDataList = new ArrayList<JLDataView>();
			}
			int indexData = jlDataList.size();
			jlDataList.add(dataview);

			axisAndData.put(axis, jlDataList);
			sampleAndItsData.put(sample, axisAndData);

			dataview.setMarkerSize(7);
			dataview.setLineWidth(0);
			dataview.setMarkerColor(dataColors[iDataColor]);
			dataview.setMarker(dataMarkers[iDataMarker]);
			if (iDataColor + 1 < dataColors.length - 1) {
				iDataColor += 1;
			} else {
				iDataColor = 0;
			}/**/
			if (iDataMarker + 1 < dataMarkers.length - 1) {
				iDataMarker += 1;
			} else {
				iDataMarker = 0;
			}/**/

			axis.addDataView(dataview);

			/** **************X***************************** */

			jlcombo.add(dataview.getName());

			jlcombo.select(indexData);
			setData(sample, dataview);
			repaint();
		}

	}

	/**
	 * remove current dataview corresponding to selected sample in dataviewcombo
	 * and Y axis selected
	 */
	private void removeDataViewFromChart(JLAxis axis, JLDataView dataview) {

		axis.removeDataView(dataview);
		repaint();

	}

	/**
	 * 
	 * @param s
	 *            remove all dataviews for this sample (Y1 and Y2 axis)
	 */
	public void removeDataViews(Sample s) {

		// remove dataviews for this sample
		removeJLDataViews(s);
		sampleAndItsData.remove(s);

		populateCombosample();
		String name = sampleCombo.getText();
		if (name != null) {
			currentSample = (Sample) sampleCombo.getData(name);
			populateJLDataViews(currentSample);
		}

		populateListsKeys();

		repaint();

	}

	private void populateCombosample() {
		Set<Sample> sampleAndViews = sampleAndItsData.keySet();
		Iterator<Sample> sampleIterator = sampleAndViews.iterator();
		sampleCombo.removeAll();
		while (sampleIterator.hasNext()) {
			Sample sampleKey = (Sample) sampleIterator.next();
			sampleCombo.add(sampleKey.getDirectoryName());
			sampleCombo.setData(sampleKey.getDirectoryName(), sampleKey);

		}

		sampleCombo.select(0);
		sampleCombo.setEnabled(sampleCombo.getItemCount() > 0);

	}

	private void populateJLDataViews(Sample s) {

		y1dataviews.removeAll();
		y2dataviews.removeAll();
		HashMap<JLAxis, ArrayList<JLDataView>> jldataAndAxis = (HashMap<JLAxis, ArrayList<JLDataView>>) sampleAndItsData
				.get(s);
		// Set set_jldataAndAxis=jldataAndAxis.entrySet();
		// Iterator it_jldataAndAxis=set_jldataAndAxis.iterator();
		if (jldataAndAxis != null) {
			Set<JLAxis> key_Axis = jldataAndAxis.keySet();
			Iterator<JLAxis> it_axis = key_Axis.iterator();
			while (it_axis.hasNext()) {
				JLAxis axis = (JLAxis) it_axis.next();
				if (axis.equals(chart.getY1Axis())) {
					// populate combo y1 with jlDataviews
					ArrayList<JLDataView> dataview = jldataAndAxis.get(axis);
					for (int j = 0; dataview != null && j < dataview.size(); j++) {
						String name = dataview.get(j).getName();

						y1dataviews.add(name);
					}

				} else {
					// populate combo y2
					ArrayList<JLDataView> dataview = jldataAndAxis.get(axis);
					for (int j = 0; dataview != null && j < dataview.size(); j++) {
						String name = dataview.get(j).getName();
						y2dataviews.add(name);
					}
				}
			}
		}

		if (y1dataviews.getItemCount() > 0) {
			y1dataviews.select(0);
		}
		if (y2dataviews.getItemCount() > 0) {
			y2dataviews.select(0);
		}

	}

	private void removeJLDataViews(Sample s) {
		boolean bfound = false;
		Set<Sample> sampleAndViews = sampleAndItsData.keySet();
		Iterator<Sample> sampleIterator = sampleAndViews.iterator();
		while (sampleIterator.hasNext() && !bfound) {
			Sample sampleKey = (Sample) sampleIterator.next();
			if (sampleKey.equals(s)) {
				bfound = true;
				HashMap<JLAxis, ArrayList<JLDataView>> jldataAndAxis = (HashMap<JLAxis, ArrayList<JLDataView>>) sampleAndItsData
						.get(sampleKey);
				// Set set_jldataAndAxis=jldataAndAxis.entrySet();
				// Iterator it_jldataAndAxis=set_jldataAndAxis.iterator();
				if (jldataAndAxis != null) {
					Set<JLAxis> key_Axis = jldataAndAxis.keySet();
					Iterator<JLAxis> it_axis = key_Axis.iterator();
					while (it_axis.hasNext()) {
						JLAxis axis = (JLAxis) it_axis.next();
						ArrayList<JLDataView> dataview = jldataAndAxis
								.get(axis);
						for (int j = 0; dataview != null && j < dataview.size(); j++) {
							axis.removeDataView(dataview.get(j));
						}

					}
				}
			}
		}

	}

	public void repaint() {
		Dimension chartSize = chartContainer.getSize();
		chart.setSize(chartSize);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chartContainer.repaint();
			}
		});
	}

	/**
	 * Force x axis with this label. Label should exist in combo
	 */
	private void selectLabelinXCombo(String label) {
		boolean found = false;
		int i = 0;
		for (i = 0; xKeysCombo != null && !found
				&& i < xKeysCombo.getItemCount(); i++) {
			if (xKeysCombo.getItems()[i].equals(label)) {
				found = true;
				xKeysCombo.select(i);
				currentXIndex = i;
				xKeysCombo.notifyListeners(SWT.Selection, new Event());
			}
		}
	}

	/**
	 * Force x axis with this label. Label should exist in combo
	 */
	private void selectLabelinYCombo(String label) {
		boolean found = false;
		int i = 0;
		for (i = 0; yKeysCombo != null && !found
				&& i < yKeysCombo.getItemCount(); i++) {
			if (yKeysCombo.getItems()[i].equals(label)) {
				found = true;
				yKeysCombo.select(i);
				currentYIndex = i;
				yKeysCombo.notifyListeners(SWT.Selection, new Event());
			}
		}
	}

	/**
	 * 
	 * @return this chart
	 */
	public JLChart getChart() {
		return chart;
	}

	public JApplet getChartContainer() {
		return chartContainer;
	}

	/**
	 * sample should have fabio files
	 * 
	 * @param key
	 * @param s
	 * @return
	 * @throws SampleException
	 */
	public void plotHeaderValues(final String key, final Sample s)
			throws SampleException {

		final Job job = new Job("Loading file header") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Wait while getting values for " + key
						+ " in sample " + s.getDirectoryName(), s
						.getFilteredfiles().size());

				if (s.hasFile()) {

					int i = 0;
					// get all header values if it has not been done for this
					// key\
					for (Iterator<FabioFile> it = s.getFilteredfiles()
							.iterator(); it.hasNext();) {
						if (monitor.isCanceled()) {

							return Status.CANCEL_STATUS;
						}

						final FabioFile f = ((FabioFile) it.next());

						try {
							if (i < s.getHeaderValues(key).length) {
								final int j = i;

								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										double[] fillValues;
										try {
											fillValues = s.getHeaderValues(key);
											fillValues[j] = Double.valueOf(f
													.getValue(key));
										} catch (SampleException ex) {
											FableUtils
													.excMsg(
															this,
															"Unable to load file headers",
															ex);
										} catch (NumberFormatException ex) {
											FableUtils
													.excMsg(
															this,
															"Unable to load file headers",
															ex);
											monitor.setCanceled(true);
										} catch (FabioFileException ex) {
											FableUtils
													.excMsg(
															this,
															"Unable to load file headers",
															ex);
											monitor.setCanceled(true);
										}
									}
								});

								i++;
								monitor.worked(1);

							}
						} catch (SampleException e) {

							e.printStackTrace();
						}

					}
				}
				monitor.done();

				return Status.OK_STATUS;
			}

		};
		int size;
		if (s.hasFile()) {
			size = s.getFilteredfiles().size();
			double[] values = s.getHeaderValues(key);
			job.setUser(true);
			if (values == null || values.length != size) {
				values = new double[size];
				s.addHeaderValues(key, values);
				// Load header values
				new Thread() {
					@Override
					public void run() {
						job.schedule();
						while (job.getResult() == null) {
							HeaderPlotView.view.showBusy(true);

						}
						HeaderPlotView.view.showBusy(false);

					}
				}.start();

			}
		}

	}

	/**
	 * Customize chart tooltip with labels.
	 */
	public String[] clickOnChart(JLChartEvent e) {
		String[] ret = new String[2];
		ret[0] = sxAxisLabel + "=" + e.getTransformedXValue();
		ret[1] = syAxisLabel + "=" + e.getTransformedYValue();

		return ret;
	}

	private void enableGroupDataViews() {
		Control[] children = grpPlot.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setEnabled(sampleCombo.getItemCount() > 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// These events come from the SampleController and the names are defined
		// in fable.framework.navigator.toolBox.IVarKeys
		if (evt.getProperty().equals(IVarKeys.CURRENT_SAMPLE_EVENT)) {
			currentSample = controller.getCurrentsample();
			populateListsKeys();
			populateJLDataViews(currentSample);
			String[] listSamples = sampleCombo.getItems();
			sampleCombo.setEnabled(sampleCombo.getItemCount() > 0);
			boolean bfound = false;
			for (int i = 0; listSamples != null && !bfound
					&& i < listSamples.length; i++) {
				if (listSamples[i].equals(currentSample.getDirectoryName())) {
					sampleCombo.select(i);
					bfound = true;
				}
			}

		} else if (evt.getProperty().equals(IVarKeys.NEW_SAMPLE_EVENT)) {
			final Sample s = (Sample) evt.getNewValue();
			// if sample is still loading in a job, make theses actions in
			// default display thread
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					sampleAndItsData.put(s, null);
					populateCombosample();
					populateJLDataViews(s);
					populateListsKeys();
					enableGroupDataViews();
				}
			});

		} else if (evt.getProperty().equals(IVarKeys.REMOVE_SAMPLE_EVENT)) {
			Sample s = (Sample) evt.getNewValue();
			removeDataViews(s);
			enableGroupDataViews();
		} else if (evt.getProperty().equals(IVarKeys.UPDATE_PLOT_EVENT)) {
			// update dataview with list keys which is read in a job
			setData(currentSample, currentdataview);
			repaint();
		} else if (evt.getProperty().equals(IVarKeys.SET_CURRENTFILE_EVENT)) {
			populateListsKeys();
		}
		//	
	}
}
