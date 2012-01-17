/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JApplet;

import org.slf4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import fable.framework.logging.FableLogger;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;

/**
 * PythonPlotArrayView plots a python array as a 1d plot. It uses the fast JLChart to draw the chart. 
 * Values can be plotted on the Y1 or Y2 axes. A clear button lets the user clear all the plots.
 * 
 * @author Andy Gotz
 *
 */
public class PythonPlotView extends ViewPart{

	public final static String ID = "fable.python.views.PythonPlotView"; //
	private Vector<JLDataView> dataViewSeries = new Vector<JLDataView>();
	private JLChart chart;
	Button y1Button, y2Button;
	int dataMarkers[] = {JLDataView.MARKER_BOX,JLDataView.MARKER_CIRCLE,JLDataView.MARKER_CROSS,
			JLDataView.MARKER_DIAMOND,JLDataView.MARKER_DOT, JLDataView.MARKER_STAR};
	static int iDataMarker=0;
	Color dataColors[] = {Color.RED,Color.BLUE,Color.CYAN,Color.GREEN, Color.MAGENTA, Color.ORANGE, 
			Color.YELLOW};
	static int iDataColor=0;
	public  static PythonPlotView view;
	private String title="Python Plot";
	private Composite swtAwtComponent;
	java.awt.Frame chartFrame;
	Logger logger;
	private JApplet chartContainer;
	private JLDataView dataView = null;

	/**
	 * 
	 * Initiate the super class and get a local copy of the logger.
	 */
	public PythonPlotView() {
		super();
		logger = FableLogger.getLogger(PythonPlotView.class);
	}

	/**
	 * Create the View part.
	 * 
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		view=this;
		parent.setLayout(new GridLayout());
		createChartFrame(parent);
		createChart();
		Composite controlComposite = new Composite(parent, SWT.NULL);
		GridLayout controlGridLayout = new GridLayout();
		controlGridLayout.numColumns = 3;
		controlComposite.setLayout(controlGridLayout);
		y1Button = new Button(controlComposite, SWT.RADIO); 
		y1Button.setText("add to Y1");
		y1Button.setToolTipText("add next plot to Y1 axis");
		y1Button.setSelection(true);
		y2Button = new Button(controlComposite, SWT.RADIO); 
		y2Button.setText("add to Y2");
		y2Button.setToolTipText("add next plot to Y2 axis");
		y2Button.setSelection(false);
		Button clearButton = new Button(controlComposite, SWT.PUSH);
		clearButton.setText("clear plot");
		clearButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				clearAllPlots();
			}
			public void widgetSelected(SelectionEvent e) {
				clearAllPlots();			
			}	
		});
		this.setPartName("Python Plot");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * create an SWT_AWT frame to hold the JLChart
	 * 
	 * @param parent
	 */
	private void createChartFrame(Composite parent){
		swtAwtComponent = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		swtAwtComponent.setLayout(new GridLayout());
		swtAwtComponent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL));
		chartFrame = SWT_AWT.new_Frame( swtAwtComponent );
		chartContainer = new JApplet();
		chartFrame.add(chartContainer);
	}

	/**
	 * Create the JLChart and initialise it with default settings e.g. autoscale etc.
	 */
	public void createChart(){
		chart = new JLChart();
		chart.setHeader(title);
		chart.setHeaderFont(new Font("Dialog",Font.BOLD,18));
		chart.getY1Axis().setName("value");
		chart.getY1Axis().setAutoScale(true);
		chart.getY2Axis().setAutoScale(true);
		chart.getXAxis().setAutoScale(true);
		chart.getXAxis().setName("index");
		chart.getXAxis().setGridVisible(true);
		chart.getXAxis().setSubGridVisible(true);
		chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
		chart.getY1Axis().setGridVisible(true);
		chart.getY1Axis().setSubGridVisible(true);
		chartContainer.add(chart);
	}


	/**
	 * plot data 
	 * @param _name - name of array to plot
	 * @param data - data to plot
	 */
	public void plotArray(String _arrayName, float _arrayData[]){
		//final String axis = _arrayName;

		if (dataView == null) {
			dataView =new JLDataView();
			dataView.setName(_arrayName);
			dataView.setMarker(dataMarkers[iDataMarker]);
			dataView.setMarkerColor(dataColors[iDataColor]);
			dataView.setColor(dataColors[iDataColor]);
			if (y1Button.getSelection())
				chart.getY1Axis().addDataView(dataView);
			else
				chart.getY2Axis().addDataView(dataView);
		}
		double x[]= new double[_arrayData.length];
		double y[] = new double[_arrayData.length];
		for(int i=0; i<_arrayData.length; i++){
			x[i] = i+1;
			y[i] = Double.valueOf(_arrayData[i]);
		}	
		dataView.setData(x, y);
		chart.repaint();
		iDataMarker++;
		if (iDataMarker >= dataMarkers.length) iDataMarker = 0;
		iDataColor++;
		if (iDataColor >= dataColors.length) iDataColor = 0;
		dataViewSeries.add(dataView);
		dataView = null;
	}


	/**
	 * Clear all plots
	 */
	public void clearAllPlots() {
		for (int i=0; i<dataViewSeries.size(); i++) {
			chart.removeDataView(dataViewSeries.get(i));			
		}
		Dimension chartSize = chartContainer.getSize();
		chart.setSize(chartSize);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chartContainer.repaint();
			}
		});
		dataViewSeries.clear();
	}

}
