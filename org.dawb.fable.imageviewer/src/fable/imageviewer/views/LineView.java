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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.navigator.controller.SampleController;

/**
 * The ZoomLineView class implements an eclipse view for plotting a user
 * selected cut in an image. It uses the dawb plotting system to do the plotting. It
 * will plot the data set as a line. The image view gathers the user's selection
 * and sends the data to plot to the ZoomLineView.
 * 
 * @author Andy Gotz
 */

public class LineView extends ViewPart {

	public final static String ID = "fable.imageviewer.views.LineView";
	private IPlottingSystem plottingSystem;
	public static LineView view;
	private String title = "Zoom Line Plot";
	public SampleController controller;
	/**
	 * Extra actions for the chart.
	 */

	private static final Logger logger = LoggerFactory.getLogger(LineView.class);
	Composite parent;

	public LineView() {
		super();
		controller = SampleController.getController();
		try {
	        this.plottingSystem = PlottingFactory.createPlottingSystem();
	        plottingSystem.setColorOption(ColorOption.NONE);
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
	}

	/**
	 * Required if you want to make tools work.
	 */
	public Object getAdapter(final Class clazz) {

		if (clazz == IToolPageSystem.class) {
			return plottingSystem;
		}

		return super.getAdapter(clazz);
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
		this.parent = parent;
		parent.setLayout(new GridLayout());
		
		final Composite plot = new Composite(parent, SWT.NONE);
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plot.setLayout(new FillLayout());
		try {
	        IActionBars wrapper = this.getViewSite().getActionBars();
			plottingSystem.createPlotPart(plot, title, wrapper, PlotType.XY, this);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Remove the previous plot, set the new data to plot, and display it as a
	 * line plot.
	 * 
	 * @param title
	 *            Title to display with plot.
	 * @param xTitle
	 *            Label for x axis.
	 * @param xInverted
	 *            Whether to invert the x axis.
	 * @param pixel
	 *            x values.
	 * @param yTitle
	 *            Label for y axis.
	 * @param intensity
	 *            Intensity to plot as f(x).
	 */
	public void setData(String title, String xTitle, boolean xInverted,
			float pixel[], String yTitle, float intensity[]) {
		double[] x = new double[pixel.length];
		double[] y = new double[pixel.length];
		for (int i = 0; i < pixel.length; i++) {
			x[i] = pixel[i];
			if (xInverted) {
				// Reverse the y array to go with the inverted x axis
				// Seems to be the way JLChart works
				y[i] = intensity[pixel.length - 1 - i];
			} else {
				y[i] = intensity[i];
			}
		}
		DoubleDataset xAxis = new DoubleDataset(x, pixel.length);
		DoubleDataset yData = new DoubleDataset(y, pixel.length);
		xAxis.setName("pixel");
		yData.setName("Intensity");
		final List<IDataset> yDataSets = new ArrayList<IDataset>(1);
		yDataSets.add(yData);
		plottingSystem.clear();
		plottingSystem.createPlot1D(xAxis, yDataSets, null);
		plottingSystem.setTitle(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
     	if (plottingSystem!=null) plottingSystem.dispose();
     	plottingSystem   = null;
		super.dispose();
	}

}
