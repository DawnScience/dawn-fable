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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JApplet;

import jep.JepException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fable.framework.imageprint.JLChartActions;
import fable.framework.ui.object.ColumnFileController;
import fable.framework.ui.rcp.Activator;
import fable.python.ColumnFile;
import fr.esrf.tangoatk.widget.util.chart.IJLChartActionListener;
import fr.esrf.tangoatk.widget.util.chart.IJLChartListener;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLChartActionEvent;
import fr.esrf.tangoatk.widget.util.chart.JLChartEvent;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;

/*******************************************************************************
 * @description This class can be used to create an XY plot of a series of
 *              ColumnFiles with JLChart.<br>
 *              The plot currently displays plots only one ColumnFiles at a time
 *              because files can have different columns. ColumnFiles can be
 *              removed or added or edited. <br>
 *              ColumnFiles can be added by clicking on the Open Column File
 *              action button.
 *              <p>
 *              Usage :
 *              <p>
 *              ColFileXYPlot plot= new ColFileXYPlot( parent, "myName", "y",
 *              "x");
 *              <p>
 *              Use plot.populateChart() to fill the chart
 * 
 * @author Andy Gotz + Gaelle Suchet
 * 
 * @todo add support for plotting multiple files on same plot on y1 or y2
 * 
 * @since 19-05-2005 : keep or remove selected dots
 * 
 */
public class ColFileXYPlot implements IJLChartListener {
	private static final long serialVersionUID = 1L;
	/**
	 * A vector is kept of each column file currently loaded with its associated
	 * data view and a vector of additional data views (to be plotted on the y2
	 * axis)
	 */
	private Vector<ColumnFile> columnFileVector = new Vector<ColumnFile>();
	private Vector<JLDataView> dataViewVector = new Vector<JLDataView>();
	private java.awt.Frame chartFrame;
	private JApplet chartContainer;
	private JLChart chart;
	/**
	 * Extra actions for the chart.
	 */
	private JLChartActions actions;
	/**
	 * The parent Composite from the constructor.
	 */
	Composite parent;
	/**
	 * The SashForm that holds the plot and the options.
	 */
	SashForm sashForm;
	/**
	 * The Composite that holds the plot.
	 */
	private Composite plotSwtAwtComposite;
	/**
	 * The Composite that holds the options.
	 */
	private ScrolledComposite optionsComposite;
	/**
	 * Indicates when the plot options are visible.
	 */
	private boolean optionsShowing = true;
	/**
	 * Listener for items added to the plot context menu.
	 */
	private IJLChartActionListener chartActionListener;
	/**
	 * Paint listener for the parent composite.
	 */
	private PaintListener paintListener;
	private String chartName, syAxisLabel, sxAxisLabel;
	static int iDataColor = 0;
	Combo colFileCombo, xKeysCombo, yKeysCombo;
	public Button updateButton;
	private Button removeButton;
	private Button resetButton;
	private Button switchXYButton;
	private Group editGroup;
	private int currentXIndex = 0;
	private int currentYIndex = 1;
	double[] currentXData;
	double[] currentYData;
	private JLDataView currentDataView = null;
	ColumnFile currentColumnFile = null;
	private Text txtY1Min, txtY1Max, txtXMax, txtXMin;
	private Button removeDataButton, keepDataButton;
	ImageDescriptor widget_close = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "images/widget_close.gif");
	ImageDescriptor widget_open = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "images/widget_open.gif");
	private ImageDescriptor imgdescChartOpt = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"images/graphView.gif");
	private ImageDescriptor imgdescRefreshplot = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"images/refresh.gif");
	private ImageDescriptor imgdescdelete = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/delete.gif");
	private ImageDescriptor imgdescswitch = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/switch.gif");
	private ImageDescriptor imgdescSub = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, "images/subtract.gif");

	private java.awt.Color dataColors[] = { new java.awt.Color(51, 51, 255),
			new java.awt.Color(255, 51, 0), new java.awt.Color(51, 255, 255),
			new java.awt.Color(255, 204, 51), new java.awt.Color(153, 0, 153),
			new java.awt.Color(0, 255, 60) };

	private ImageDescriptor keep_descriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/check.gif");
	private ImageDescriptor remove_descriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/delete.gif");
	private Image img_delete, img_keep;

	// Gaelle add for manage Transformer and this columnFile plot as we can
	// add ColumnFile here
	private ColumnFileController colController;
	/** This dataview is used to display selected spot in table. */
	private JLDataView markedDataView;
	private int[] selectedRowsId;

	/**
	 * ColFileXYPlot constructor
	 * 
	 * @param parent
	 *            - composite
	 * @param name
	 *            -chart name
	 * @param yLabel
	 *            - y label name
	 * @param xLabel
	 *            - x label name
	 */
	public ColFileXYPlot(Composite parent, String name, String xLabel,
			String yLabel) {
		this.parent = parent;
		chartName = name;
		syAxisLabel = yLabel;
		sxAxisLabel = xLabel;
		img_keep = keep_descriptor.createImage();
		int width = img_keep.getBounds().width;
		int height = img_keep.getBounds().height;
		Image tmp = remove_descriptor.createImage();
		img_delete = new Image(Display.getDefault(), tmp.getImageData()
				.scaledTo(width, height));

		if (widget_open != null) {
			// openEdit = widget_open.createImage();
		}
		if (widget_close != null) {
			// hideEdit = widget_close.createImage();
		}
		tmp.dispose();

		parent.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		// Make a paint listener that paints the chart
		// KE: May not be necessary
		parent.addPaintListener(new PaintListener() {
			// @Override
			public void paintControl(PaintEvent arg0) {
				repaint();
			}
		});

		// Create a SashForm so the plot can be sized independently of the view
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);

		createChartFrame(sashForm);
		createChart();
		createOptionsGroup(sashForm);

		// Needs to be done after contents are defined
		sashForm.setWeights(new int[] { 2, 1 });

		/*
		 * do not listen to the default labels from the preference store, labels
		 * differ from one column file to the next one
		 */
		/*
		 * Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) { if ((String)
		 * event.getProperty() != null) { if (((String) event.getProperty())
		 * .equals(ColumnFilePlotPreferences.X_LABEL)) { setSxAxisLabel((String)
		 * event.getNewValue()); } else if (((String) event.getProperty())
		 * .equals(ColumnFilePlotPreferences.Y_LABEL)) { setSyAxisLabel((String)
		 * event.getNewValue()); } } } });
		 */

		colController = ColumnFileController.getColumnFileController();
		colController.setcolumnFileVector(columnFileVector);
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

	/** *********************************************************************** */
	// LIST KEYS
	/** *********************************************************************** */

	/**
	 * Initialise x and y keys combo box with values to plot
	 * 
	 * @since added job when columnFile is loaded, to avoid invalid thread acces
	 *        fill combo in display thread.
	 * @param colandValues
	 */
	public void populateListsKeys(final ColumnFile col) {
		// HashMap<String, double[]> values = col.getColumnstoPlot();

		Display.getDefault().syncExec(new Runnable() {
			// @Override
			public void run() {
				String[] colandValues = col.getTitles();
				xKeysCombo.removeAll();
				yKeysCombo.removeAll();

				for (int i = 0; colandValues != null && i < colandValues.length; i++) {
					String key = colandValues[i];
					/* do not store data as part of keys combo */
					/*
					 * xKeysCombo.setData(key, (double[]) values.get(key));
					 * yKeysCombo.setData(key, (double[]) values.get(key));
					 */
					yKeysCombo.setData("help" + key, key);
					xKeysCombo.setData("help" + key, key);
					xKeysCombo.add(key);
					yKeysCombo.add(key);
				}
				yKeysCombo.setEnabled(yKeysCombo.getItemCount() > 0);
				xKeysCombo.setEnabled(yKeysCombo.getItemCount() > 0);
				if (currentXIndex > xKeysCombo.getItemCount()) {
					resetPreferredCurrentXIndex();
				}
				if (currentYIndex > yKeysCombo.getItemCount()) {
					resetPreferredCurrentY1Index();
				}
				xKeysCombo.select(currentXIndex);
				yKeysCombo.select(currentYIndex);

			}
		});

	}

	/**
	 * Create the x keys combo box
	 * 
	 * @param parent
	 */
	private void createListsKeys_X(Composite parent) {

		new Label(parent, SWT.NONE).setText("X");
		xKeysCombo = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.LEFT, GridData.CENTER, true, false);
		xKeysCombo.setLayoutData(gdlist);
		xKeysCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				plotSelectedXAxis();

			}
		});

	}

	/**
	 * @author SUCHET
	 * @date Jul, 8 2008 Called when user select x key to plot, or when system
	 *       force data to plot for example after computation in transformer,
	 *       force y to display eta values
	 */
	private void plotSelectedXAxis() {
		String[] sel = xKeysCombo.getItems();
		currentXIndex = xKeysCombo.getSelectionIndex();
		if (currentXIndex < 0) {
			resetPreferredCurrentXIndex();
		}
		sxAxisLabel = sel[currentXIndex];
		chart.getXAxis().setName(sxAxisLabel);
		updateChart();
	}

	/**
	 * Create the y keys combo box
	 * 
	 * @param parent
	 */

	private void createListsKeys_Y(Composite parent) {
		new Label(parent, SWT.NONE).setText("Y");
		yKeysCombo = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.LEFT, GridData.CENTER, true, false);
		yKeysCombo.setLayoutData(gdlist);
		yKeysCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				plotSelectedY1Axis();

			}
		});

	}

	/**
	 * @author SUCHET
	 * @date Jul, 8 2008 Called when user select y key to plot, or when system
	 *       force data to plot for example after computation in transformer,
	 *       force y to display eta values
	 */
	private void plotSelectedY1Axis() {
		String[] sel = yKeysCombo.getItems();
		currentYIndex = yKeysCombo.getSelectionIndex();
		if (currentYIndex < 0) {
			resetPreferredCurrentY1Index();
		}
		syAxisLabel = sel[currentYIndex];
		chart.getY1Axis().setName(syAxisLabel);
		updateChart();
	}

	/**
	 * Create the Group of options for JLChart. These consist of a combo with a
	 * list of dataview. allowing user to remove or add column files top plot.
	 * There is also a clear all button and an option to enable the editor
	 * 
	 * @param parent
	 */
	private void createOptionsGroup(Composite parent) {
		optionsComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL);
		optionsComposite.setLayout(new GridLayout(1, false));

		Composite mainComposite = new Composite(optionsComposite, SWT.None);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData());
		mainComposite.pack();

		optionsComposite.setContent(mainComposite);

		Group groupChartOptions = new Group(mainComposite, SWT.NONE);
		groupChartOptions.setLayout(new GridLayout());
		((GridLayout) (groupChartOptions.getLayout())).numColumns = 2;
		GridData gridChartOpt = new GridData(SWT.CENTER | SWT.FILL, SWT.FILL,
				true, false);
		gridChartOpt.verticalSpan = 1;
		gridChartOpt.horizontalSpan = 1;
		groupChartOptions.setLayoutData(gridChartOpt);
		groupChartOptions.setText("Plot Options");
		/** **************************Column to plot************************* */
		createListsKeys_X(groupChartOptions);
		createListsKeys_Y(groupChartOptions);
		/** ***************************************************************** */

		new Label(groupChartOptions, SWT.None).setText("Data");
		colFileCombo = new Combo(groupChartOptions, SWT.READ_ONLY);
		colFileCombo.setEnabled(dataViewVector.size() > 0);
		colFileCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		((GridData) colFileCombo.getLayoutData()).horizontalSpan = 1;
		colFileCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				Combo cbo = ((Combo) e.widget);
				int index = cbo.getSelectionIndex();
				if (index >= 0) {
					/* cannot get two different data from the same object ?! */
					/*
					 * currentDataView = (JLDataView) cbo.getData(cbo
					 * .getItem(index));
					 */
					/*
					 * currentColumnFile = (ColumnFile) cbo.getData(cbo
					 * .getItem(index));
					 */
					currentColumnFile = columnFileVector.get(index);
					colController.setCurrentColumnFile(currentColumnFile);
					populateListsKeys(currentColumnFile);
					if (currentDataView != null)
						chart.removeDataView(currentDataView);
					currentDataView = dataViewVector.get(index);
					chart.getY1Axis().addDataView(currentDataView);

				}
				updateChart();
				repaint();
			}
		});
		createOptionsButtons(groupChartOptions);

		/* GROUP KEEP OR REMOVE DOTS */

		createEditPlotOptions(mainComposite);
		// parent.layout();
		optionsComposite.setMinHeight(Display.getDefault().getPrimaryMonitor()
				.getBounds().height);
		optionsComposite.setMinWidth(Display.getDefault().getPrimaryMonitor()
				.getBounds().width);
		optionsComposite.setExpandVertical(true);
		optionsComposite.setExpandHorizontal(true);
		optionsComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

	}

	/**
	 * This method instantiates buttons "Update", "Clear plot", and "Options"
	 * 
	 * @param groupChartOptions
	 */
	private void createOptionsButtons(Group groupChartOptions) {
		createRemoveButton(groupChartOptions);
		createShowDataOptionsButton(groupChartOptions);
		createSwitchXYButton(groupChartOptions);
		createUpdateButton(groupChartOptions);
		createClearAllButton(groupChartOptions);
	}

	/**
	 * This method instantiates the switch x y button and adds it to the group
	 * options.
	 * 
	 * @param groupChartOptions
	 */
	private void createSwitchXYButton(Group groupChartOptions) {
		switchXYButton = new Button(groupChartOptions, SWT.PUSH);
		switchXYButton.setText("Switch X Y");
		final Image removeimage = imgdescswitch.createImage();
		switchXYButton.setImage(removeimage);
		switchXYButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (removeimage != null && !removeimage.isDisposed()) {
					removeimage.dispose();
				}

			}
		});
		switchXYButton.setToolTipText("Switch the x and y axes");
		switchXYButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | SWT.RESIZE));
		switchXYButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentXIndex < 0 || currentYIndex < 0) {
					return;
				}
				int temp = currentYIndex;
				currentYIndex = currentXIndex;
				currentXIndex = temp;
				String tempString = syAxisLabel;
				syAxisLabel = sxAxisLabel;
				sxAxisLabel = tempString;
				xKeysCombo.select(currentXIndex);
				yKeysCombo.select(currentYIndex);
				chart.getXAxis().setName(sxAxisLabel);
				chart.getY1Axis().setName(syAxisLabel);
				updateChart();
			}
		});
	}

	/**
	 * This method instantiates the reset button and adds it to the group
	 * options.
	 * 
	 * @param groupChartOptions
	 */
	private void createClearAllButton(Group groupChartOptions) {
		resetButton = new Button(groupChartOptions, SWT.PUSH);
		resetButton.setText("Clear");
		final Image removeimage = imgdescdelete.createImage();
		resetButton.setImage(removeimage);
		resetButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (removeimage != null && !removeimage.isDisposed()) {
					removeimage.dispose();
				}

			}
		});
		resetButton.setToolTipText("Clear plot and remove all column files");
		resetButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| SWT.RESIZE));
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearAllPlots();
			}
		});
	}

	/**
	 * This method instantiates the update button and adds it to the group
	 * options.
	 * 
	 * @param groupChartOptions
	 */
	private void createUpdateButton(Group groupChartOptions) {
		updateButton = new Button(groupChartOptions, SWT.PUSH);
		updateButton.setText("Update");
		final Image imageRefresh = imgdescRefreshplot.createImage();
		updateButton.setImage(imageRefresh);
		updateButton.setToolTipText("Update chart frame and plot");
		updateButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageRefresh != null && !imageRefresh.isDisposed()) {
					imageRefresh.dispose();
				}

			}
		});

		updateButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| SWT.RESIZE));

		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				/** **************X***************************** */
				/* get data from column file rather than from combo */
				/*
				 * double[] listX = ((double[])
				 * xKeysCombo.getData(sxAxisLabel));
				 */
				double[] listX = ((double[]) currentColumnFile
						.getColumnstoPlot().get(sxAxisLabel));
				int len = listX != null ? listX.length : 0;
				if (len > 0) {
					currentXData = new double[len];
					/*
					 * currentXData = (double[])
					 * (xKeysCombo.getData(sxAxisLabel));
					 */
					currentXData = (double[]) currentColumnFile
							.getColumnstoPlot().get(sxAxisLabel);
				}
				/** ***************Y**************************** */
				/* get data from column file rather than from combo */
				/*
				 * double[] listY = ((double[])
				 * yKeysCombo.getData(syAxisLabel));
				 */
				double[] listY = ((double[]) currentColumnFile
						.getColumnstoPlot().get(syAxisLabel));

				len = listY != null ? listY.length : 0;
				if (len > 0) {

					currentYData = new double[len];
					/*
					 * currentYData = (double[])
					 * yKeysCombo.getData(syAxisLabel);
					 */
					currentYData = (double[]) currentColumnFile
							.getColumnstoPlot().get(syAxisLabel);
				}
				/** ****************UPDATE **************************** */
				updateChart(currentXData, currentYData);
			}
		});
	}

	/**
	 * This method instantiates the show data options button and adds it to the
	 * group options.
	 * 
	 * @param groupChartOptions
	 */
	private void createShowDataOptionsButton(Group groupChartOptions) {
		Button button = new Button(groupChartOptions, SWT.PUSH);
		button.setText("Data");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| SWT.RESIZE));
		final Image image_chartOptions = imgdescChartOpt.createImage();
		button.setImage(image_chartOptions);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (currentDataView != null) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chart.showDataOptionDialog(currentDataView);

						}
					});

				} else {
					MessageDialog.openInformation(Display.getCurrent()
							.getActiveShell(), "Column file plot",
							"No current dataview is available "
									+ "to display options.");
				}

			}
		});

		button.setToolTipText("Display current data options box.");
		button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (image_chartOptions != null
						&& !image_chartOptions.isDisposed()) {
					image_chartOptions.dispose();
				}

			}
		});
	}

	/**
	 * This methods instantiates the remove button and adds it to the group
	 * options.
	 * 
	 * @param groupChartOptions
	 */
	private void createRemoveButton(Group groupChartOptions) {
		removeButton = new Button(groupChartOptions, SWT.PUSH);
		final Image subtractimage = imgdescSub.createImage();
		removeButton.setImage(subtractimage);
		removeButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (subtractimage != null && !subtractimage.isDisposed()) {
					subtractimage.dispose();
				}

			}
		});
		removeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| SWT.RESIZE));
		removeButton.setText("Remove Data");
		removeButton
				.setToolTipText("Remove selected ColumnFile of the list from the plot");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = colFileCombo.getSelectionIndex();
				/** make removal simpler by using the index */
				/*
				 * JLDataView dt = (JLDataView)
				 * (dataViewCombo.getData(dataViewCombo .getItem(index)));
				 * removeDataView(dt); columnFileVector.remove((ColumnFile)
				 * dataViewCombo .getData(dataViewCombo.getItem(index)));
				 */
				columnFileVector.remove(index);
				// GS 18/07/08 : if this is the last dataview,
				// remove it from the chart
				JLDataView dataview = dataViewVector.elementAt(index);
				chart.removeDataView(dataview);
				xKeysCombo.removeAll();
				yKeysCombo.removeAll();
				//
				dataViewVector.remove(index);
				colFileCombo.remove(index);
				if (index > colFileCombo.getItemCount() - 1) {
					index = colFileCombo.getItemCount() - 1;
				}
				colFileCombo.select(index);
				/* use Gaelle's trick to redraw */
				colFileCombo.notifyListeners(SWT.Selection, new Event());

			}
		});
	}

	/**
	 * This method creates options group to keep or remove dots.
	 * 
	 * @param mainComposite
	 */
	private void createEditPlotOptions(Composite mainComposite) {
		editGroup = new Group(mainComposite, SWT.NONE);
		editGroup.setText("Plot Editor");
		editGroup.setLayout(new GridLayout());
		((GridLayout) (editGroup.getLayout())).numColumns = 2;
		GridData gdColumnDots = new GridData(SWT.CENTER | SWT.FILL, SWT.FILL,
				true, false);
		gdColumnDots.verticalSpan = 1;
		gdColumnDots.horizontalSpan = 1;
		editGroup.setLayoutData(gdColumnDots);

		// ------------------------X AXIS-------------------------------------/

		new Label(editGroup, SWT.None).setText("X Min");
		txtXMin = new Text(editGroup, SWT.BORDER);
		txtXMin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(editGroup, SWT.None).setText("X Max");
		txtXMax = new Text(editGroup, SWT.BORDER);
		txtXMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// ------------------------Y AXIS-------------------------------------/

		new Label(editGroup, SWT.None).setText("Y Min");
		txtY1Min = new Text(editGroup, SWT.BORDER);
		txtY1Min.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(editGroup, SWT.None).setText("Y Max");
		txtY1Max = new Text(editGroup, SWT.BORDER);
		txtY1Max.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// ------------------------BUTTONS-------------------------------------/

		keepDataButton = new Button(editGroup, SWT.PUSH);

		keepDataButton.setText("Keep");
		keepDataButton
				.setToolTipText("Keep all dots between min X, Y and max X, Y "
						+ "and remove others");
		keepDataButton.setImage(img_keep);
		keepDataButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (img_keep != null && !img_keep.isDisposed()) {
					img_keep.dispose();
				}

			}
		});
		keepDataButton.setEnabled(columnFileVector.size() > 0);

		keepDataButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL, GridData.CENTER, true, false,
				1, 1));
		keepDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double minX = 0;
				double minY = 0;
				double maxX = 0;
				double maxY = 0;

				if (!txtXMin.getText().trim().equals("")) {
					minX = Double.valueOf(txtXMin.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					minY = Double.valueOf(txtY1Min.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					maxX = Double.valueOf(txtXMax.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					maxY = Double.valueOf(txtY1Max.getText());
				}

				keepDots(minX, maxX, minY, maxY);
				updateChart();
				repaint();
			}
		});

		removeDataButton = new Button(editGroup, SWT.PUSH);
		removeDataButton.setText("Remove");
		removeDataButton
				.setToolTipText("Remove all dots between min X, Y and max X, Y and keep others");
		removeDataButton.setImage(img_delete);
		removeDataButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (img_delete != null && !img_delete.isDisposed()) {
					img_delete.dispose();
				}

			}
		});
		removeDataButton.setEnabled(columnFileVector.size() > 0);
		removeDataButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL, GridData.CENTER, true, false,
				1, 1));
		removeDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				double minX = 0;
				double minY = 0;
				double maxX = 0;
				double maxY = 0;
				if (!txtXMin.getText().trim().equals("")) {
					minX = Double.valueOf(txtXMin.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					minY = Double.valueOf(txtY1Min.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					maxX = Double.valueOf(txtXMax.getText());
				}
				if (!txtXMin.getText().trim().equals("")) {
					maxY = Double.valueOf(txtY1Max.getText());
				}
				removeDots(minX, maxX, minY, maxY);
				updateChart();
				repaint();
			}
		});
	}

	/**
	 * remove all dots selected in the border of the plots in column file
	 * 
	 * @param xmin
	 *            - min selected X axes in the plot
	 * @param xmax
	 *            - max selected X axes in the plot
	 * @param ymin
	 *            - min selected Y axes in the plot
	 * @param ymax
	 *            - max selected Y axes in the plot
	 */
	private void removeDots(double xmin, double xmax, double ymin, double ymax) {
		try {
			currentColumnFile.removeDots(sxAxisLabel, xmin, xmax, syAxisLabel,
					ymin, ymax);
		} catch (JepException e) {
			System.out.println(e.getMessage());

		}
	}

	/**
	 * keep all dots selected in the zone and remove others
	 * 
	 * @param xmin
	 *            - min selected X axes in the plot
	 * @param xmax
	 *            - max selected X axes in the plot
	 * @param ymin
	 *            - min selected Y axes in the plot
	 * @param ymax
	 *            - max selected Y axes in the plot
	 */
	private void keepDots(double xmin, double xmax, double ymin, double ymax) {
		try {
			currentColumnFile.keepDots(sxAxisLabel, xmin, xmax, syAxisLabel,
					ymin, ymax);
		} catch (JepException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * create an SWT_AWT frame to hold the JLChart
	 * 
	 * @param parent
	 */
	private void createChartFrame(Composite parent) {
		plotSwtAwtComposite = new Composite(parent, SWT.EMBEDDED
				| SWT.NO_BACKGROUND);

		GridData gdlist = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdlist.verticalSpan = 2;
		gdlist.horizontalSpan = 2;
		plotSwtAwtComposite.setLayout(new GridLayout());
		plotSwtAwtComposite.setLayoutData(gdlist);

		chartFrame = SWT_AWT.new_Frame(plotSwtAwtComposite);
		chartContainer = new JApplet();
		chartFrame.add(chartContainer);
	}

	/**
	 * Clear all plots
	 */
	public void clearAllPlots() {
		for (Iterator<JLDataView> it = dataViewVector.iterator(); it.hasNext();) {
			chart.removeDataView(it.next());
		}
		columnFileVector.removeAllElements();
		dataViewVector.removeAllElements();
		colFileCombo.removeAll();
		xKeysCombo.removeAll();
		yKeysCombo.removeAll();

		repaint();
		iDataColor = 0;
	}

	/**
	 * Create the JLChart and initialize it with default settings e.g.
	 * autoscale.
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
		chart.getXAxis().setName(sxAxisLabel);
		chart.getXAxis().setGridVisible(true);
		chart.getXAxis().setSubGridVisible(true);
		chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
		chart.getXAxis().setAutoScale(true);

		chartContainer.add(chart);
		Dimension chartSize = chartContainer.getSize();
		if (chartSize.height < chartSize.width) {
			chart.setSize(chartSize.height, chartSize.height);
		} else {
			chart.setSize(chartSize.width, chartSize.width);
		}

		// Create the actions
		actions = new JLChartActions(parent.getDisplay(), chart);

		// Remove the Swing print item and replace it with ours then add our
		// actions
		chart.removeMenuItem(JLChart.MENU_PRINT);
		chart.addUserAction("Print Setup");
		chart.addUserAction("Print Preview");
		chart.addUserAction("Print");
		chart.addUserAction("Copy");
		chart.addUserAction("Toggle Plot Options");

		chartActionListener = new IJLChartActionListener() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final JLChartActionEvent evt) {
				// Run in the SWT thread
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (evt.getName().equals("Print Setup")) {
							actions.printSetupAction.run();
						} else if (evt.getName().equals("Print Preview")) {
							actions.printPreviewAction.run();
						} else if (evt.getName().equals("Print")) {
							actions.printAction.run();
						} else if (evt.getName().equals("Copy")) {
							actions.copyAction.run();
						} else if (evt.getName().equals("Toggle Plot Options")) {
							setOptionsShowing(!optionsShowing);
						}
					}
				});
			}

			public boolean getActionState(JLChartActionEvent evt) {
				return false;
			}
		};
		chart.addJLChartActionListener(chartActionListener);

		// Since 15/07/2008 : to customize popup info : display label name
		chart.setJLChartListener(this);

		chart.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {

						double minX = chart.getXAxis().getMin();
						double minY = chart.getY1Axis().getMin();
						double maxX = chart.getXAxis().getMax();
						double maxY = chart.getY1Axis().getMax();

						if (chart.getY1Axis().getScale() == JLAxis.LOG_SCALE) {
							minY = Math.pow(10, minY);
							maxY = Math.pow(10, maxY);
						}

						if (chart.getXAxis().getScale() == JLAxis.LOG_SCALE) {
							minX = Math.pow(10, minX);
							maxX = Math.pow(10, maxX);
						}

						txtY1Min.setText(String.valueOf(minY));
						txtY1Max.setText(String.valueOf(maxY));
						txtXMax.setText(String.valueOf(maxX));
						txtXMin.setText(String.valueOf(minX));

					}
				});
			}

		});

	}

	/**
	 * This function customize the value popup (request July 2008) Seen in
	 * JLchart snippet
	 */
	public String[] clickOnChart(JLChartEvent e) {
		String[] ret = new String[2];
		ret[0] = sxAxisLabel + "=" + e.getXValue();
		ret[1] = syAxisLabel + "=" + e.getYValue();
		return ret;
	}

	/** ********************************************************************* */
	// Populate/
	/** ********************************************************************* */
	/**
	 * This method returns indices of the spot identified by its id and adds a
	 * new JLDatView to display theses spots in a different color
	 * 
	 * @param a
	 *            list of spot id
	 * @param name
	 *            the name of the id
	 * @return a list of index with all ids
	 */
	public int[] markSelectedRows(double[] id, String name) {
		selectedRowsId = new int[id.length];
		if (markedDataView == null) {
			markedDataView = new JLDataView();
			setDataViewOptions(markedDataView, JLDataView.MARKER_CROSS, null,
					7, 0);
		} else {
			markedDataView.reset();

		}
		double[] allid = currentColumnFile.getColumnstoPlot().get(name);
		for (int j = 0; j < id.length; j++) {
			for (int i = 0; i < allid.length; i++) {
				if (allid[i] == id[j]) {
					selectedRowsId[j] = i;
					if (currentDataView != null) {
						double x = currentDataView.getXValueByIndex(i);
						double y = currentDataView.getYValueByIndex(i);
						markedDataView.add(x, y);
					}
					break;
				}
			}
		}
		if (markedDataView.getDataLength() > 0) {
			markedDataView.setLabelVisible(true);
			markedDataView.setName("# " + id.length
					+ " selected dots in table : ");
			setDataViewOptions(markedDataView, markedDataView.getMarker(),
					markedDataView.getMarkerColor(), markedDataView
							.getMarkerSize(), markedDataView.getLineWidth());
			chart.getY1Axis().addDataView(markedDataView);
			this.repaint();
		}
		return selectedRowsId;
	}

	/**
	 * Update the plot with the data from the currently selected ColumnFile and
	 * the currently selected indices.
	 */
	public void updateChart() {
		if (currentDataView != null) {
			currentDataView.reset();
		}

		currentXData = currentYData = null;
		if (xKeysCombo.getItemCount() > currentXIndex && currentXIndex > -1) {
			sxAxisLabel = xKeysCombo.getItem(currentXIndex);
			chart.getXAxis().setName(sxAxisLabel);
		}

		/* double[] listX = ((double[]) xKeysCombo.getData(sxAxisLabel)); */
		int len = 0;
		if (sxAxisLabel != null && currentColumnFile != null) {
			double[] listX = currentColumnFile.getColumnstoPlot().get(
					sxAxisLabel);
			len = listX != null ? listX.length : 0;
		}
		if (len > 0) {
			currentXData = new double[len];
			currentXData = currentColumnFile.getColumnstoPlot()
					.get(sxAxisLabel);
		}
		/** ***************Y**************************** */
		if (yKeysCombo.getItemCount() > currentYIndex && currentYIndex > -1) {
			syAxisLabel = yKeysCombo.getItem(currentYIndex);
			chart.getY1Axis().setName(syAxisLabel);
		}

		/* double[] listY = ((double[]) yKeysCombo.getData(syAxisLabel)); */
		if (syAxisLabel != null && currentColumnFile != null) {
			double[] listY = currentColumnFile.getColumnstoPlot().get(
					syAxisLabel);
			len = listY != null ? listY.length : 0;
		}

		if (len > 0) {

			currentYData = new double[len];
			currentYData = currentColumnFile.getColumnstoPlot()
					.get(syAxisLabel);
		}
		if (currentXData != null && currentYData != null) {
			if (!syAxisLabel.equals("") && !sxAxisLabel.equals("")) {
				if (currentDataView == null) {
					currentDataView = new JLDataView();
				}
				currentDataView.setXDataSorted(false);
				currentDataView.setData(currentXData, currentYData);
			}
			chart.setHeader(currentColumnFile.getFileName());
		} else {
			chart.setNoValueString("No value to display");
		}
		/**/
		updateRowsMarked();

		/**/
		repaint();
	}

	/**
	 * new since we use Editor. To update selected spot in a tableviewer, when
	 * user plot other data.
	 */
	private void updateRowsMarked() {
		if (selectedRowsId != null && selectedRowsId.length > 0) {
			markedDataView.reset();
			double[] allid = currentColumnFile.getColumnstoPlot().get(
					currentColumnFile.getColumnfileId());
			for (int j = 0; j < selectedRowsId.length; j++) {
				for (int i = 0; i < allid.length; i++) {
					if (allid[i] == selectedRowsId[j]) {

						if (currentDataView != null) {
							double x = currentDataView.getXValueByIndex(i);
							double y = currentDataView.getYValueByIndex(i);
							markedDataView.add(x, y);
						}
						break;
					}
				}
			}
			if (markedDataView != null) {
				chart.getY1Axis().addDataView(markedDataView);
			}
		}
	}

	/**
	 * Add a new data set with x and y values
	 */
	public JLDataView updateChart(final double x[], final double y[]) {

		if (!Display.getCurrent().isDisposed()) {
			if (x != null && y != null) {
				if (!syAxisLabel.equals("") && !sxAxisLabel.equals("")) {
					if (currentDataView == null) {
						currentDataView = new JLDataView();
					}
					currentDataView.setXDataSorted(false);
					currentDataView.setData(x, y);
				}
			} else {
				chart.setNoValueString("No value to display");
			}
		}
		repaint();
		return currentDataView = null;
	}

	public void updateColumnFile(ColumnFile col) {
		populateListsKeys(col);
	}

	/**
	 * Add a new columnFile object in the plot if it doesn't exist. Otherwise,
	 * update plot.
	 * 
	 * @param col
	 */
	public void addColumnFile(ColumnFile col) {
		currentColumnFile = col;
		colController.setCurrentColumnFile(currentColumnFile);
		if (columnFileVector.lastIndexOf(col) < 0) {
			columnFileVector.add(col);
			JLDataView data = null;
			data = new JLDataView();
			data.setXDataSorted(false);
			data.setName(col.getFullFileName());
			addDataView(data);
			populateListsKeys(col);
			if (xKeysCombo.getItemCount() > 0)
				setSxAxisLabel(xKeysCombo.getItems()[currentXIndex]);
			if (yKeysCombo.getItemCount() > 0)
				setSyAxisLabel(yKeysCombo.getItems()[currentYIndex]);
			/*
			 * ignore preferences for now - does not make much sense because the
			 * column file can have totally different labels to the ones in the
			 * preferences rather force the plot to start with the current
			 * indices (cf. above)
			 */
			/*
			 * setSxAxisLabel(Activator.getDefault().getPreferenceStore()
			 * .getString(ColumnFilePlotPreferences.X_LABEL));
			 * setSyAxisLabel(Activator.getDefault().getPreferenceStore()
			 * .getString(ColumnFilePlotPreferences.Y_LABEL));
			 */
			updateChart();
		} else {
			populateListsKeys(col);
			repaint();
		}
		Display.getDefault().syncExec(new Runnable() {
			// @Override
			public void run() {
				removeDataButton.setEnabled(columnFileVector.size() > 0);
				keepDataButton.setEnabled(columnFileVector.size() > 0);

			}
		});

	}

	public void addDataView(JLDataView dtV) {
		if (!Display.getCurrent().isDisposed()) {
			if (dtV != null) {
				chart.getY1Axis().setName(syAxisLabel);
				chart.getXAxis().setName(sxAxisLabel);
				// Add a new dataview to vector if dataview doesn't
				// exist in the vector. otherwise, update plot
				if (dataViewVector.indexOf(dtV) < 0) {
					dataViewVector.add(dtV);
					colFileCombo.add(dtV.getName());
					/* cannot setData() twice on the same object ?! */
					// dataViewCombo.setData(dtV.getName(), dtV);
					colFileCombo.setData(dtV.getName(), columnFileVector
							.lastElement());
				}
				/* display only one plot at a time - remove the current one */
				if (currentDataView != null)
					chart.removeDataView(currentDataView);
				currentDataView = dtV;
				currentColumnFile = columnFileVector.lastElement();
				colController.setCurrentColumnFile(currentColumnFile);
				colFileCombo.select(colFileCombo.getItemCount() - 1);
				colFileCombo.setEnabled(dataViewVector.size() > 0);
				setDataViewOptions(dtV, JLDataView.MARKER_DOT, null, 7, 0);
				chart.getY1Axis().addDataView(dtV);
			} else {

				chart.setNoValueString("No value to display");
			}
			repaint();
		}
	}

	/**
	 * This methods set dots options for this JLDataview
	 * 
	 * @param dtV
	 *            a JLDataView
	 * @param marker
	 * @param markerSize
	 * @param lineWidth
	 */
	private void setDataViewOptions(JLDataView dtV, int marker, Color color,
			int markerSize, int lineWidth) {
		dtV.setMarker(marker);
		dtV.setMarkerSize(markerSize);
		dtV.setLineWidth(lineWidth);
		if (color == null) {
			dtV.setMarkerColor(dataColors[iDataColor]);

			if (iDataColor < dataColors.length - 1) {
				iDataColor += 1;
			} else {
				iDataColor = 0;
			}
		}
	}

	public void removeDataView(JLDataView dv) {
		chart.removeDataView(dv);
		dataViewVector.remove(dv);

		if (colFileCombo.getItemCount() > 0) {
			colFileCombo.remove(dv.getName());
			colFileCombo.select(colFileCombo.getItemCount() - 1);
		} else {
			colFileCombo.setEnabled(false);
		}
		repaint();

	}

	public void repaint() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// squareChartContainer();
				chartContainer.repaint();
			}
		});
	}

	/**
	 * To make a square if scale is the same.
	 * 
	 */
	/*
	 * private void squareChartContainer() {
	 * 
	 * if (chart.getY1Axis().getMax() == chart.getXAxis().getMax()) { Dimension
	 * chartSize = chartContainer.getSize(); if (chartSize.height >
	 * chartSize.width) { chartSize.height = chartSize.width; } else {
	 * chartSize.width = chartSize.height; } chart.setSize(chartSize); } }
	 */

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

	public Vector<JLDataView> getVectordataview() {
		return dataViewVector;
	}

	public JLDataView getCurrentDataView() {
		return currentDataView;
	}

	/**
	 * *************************************************************************
	 * **********
	 */
	class comboDataView_ContentProvider implements IContentProvider {

		public Object[] getElements(Vector<JLDataView> inputElement) {
			return inputElement.toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}
	}

	class comboDataView_LabelProvider implements IBaseLabelProvider {

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			return ((JLDataView) element).getName();
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}

	}

	/**
	 * @date Jul, 8 2008 For transformer to display the latest computation (for
	 *       example, plot tth vs eta)
	 * @param xyLabel
	 */
	public void plotData(String[] xyLabel) {
		boolean bfound = false;
		String xlabel = "";
		String ylabel = "";

		if (xyLabel.length == 2) {
			xlabel = xyLabel[0];
			ylabel = xyLabel[1];
		}
		for (int i = 0; !bfound && i < xKeysCombo.getItemCount(); i++) {
			if (xKeysCombo.getItem(i).equals(xlabel)) {
				bfound = true;
				xKeysCombo.select(i);

			}
		}
		bfound = false;
		for (int i = 0; !bfound && i < yKeysCombo.getItemCount(); i++) {
			if (yKeysCombo.getItem(i).equals(ylabel)) {
				bfound = true;
				yKeysCombo.select(i);

			}
		}
		plotSelectedXAxis();
		plotSelectedY1Axis();
	}

	/**
	 * Dispose method, even though this is not a SWT Control. Should be called
	 * by users of this class when they are finished with it. May not be
	 * implemented completely properly, but removes listeners.
	 */
	public void dispose() {
		if (chart != null && chartActionListener != null) {
			chart.removeJLChartActionListener(chartActionListener);
			chartActionListener = null;
		}
		if (parent != null && !parent.isDisposed() && paintListener != null) {
			parent.removePaintListener(paintListener);
			paintListener = null;
		}
		chart = null;
	}

	/**
	 * @return Whether plot options are showing or not.
	 */
	public boolean getOptionsShowing() {
		return optionsShowing;
	}

	/**
	 * Set whether plot options are showing or not.
	 * 
	 * @param optionsShowing
	 *            Whether to show plot options.
	 */
	public void setOptionsShowing(boolean optionsShowing) {
		this.optionsShowing = optionsShowing;
		if (sashForm == null)
			return;
		if (optionsShowing) {
			sashForm.setMaximizedControl(null);
		} else {
			sashForm.setMaximizedControl(plotSwtAwtComposite);
		}
	}

	private void resetPreferredCurrentXIndex() {

		currentXIndex = 0;

	}

	private void resetPreferredCurrentY1Index() {

		currentYIndex = 1;

	}
}
