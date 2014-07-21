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

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
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

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import fable.framework.navigator.controller.SampleController;

/**
 * The ProfileView class implements an Eclipse View for plotting a user selected
 * x,y profile. It uses the fast JLChart to do the plotting. It will plot the
 * data set as two line plots. The data is set by the ImageView based on the
 * user selection.
 * 
 * @author andy.gotz
 */
public class ProfileView extends ViewPart {
	public final static String ID = "fable.imageviewer.views.ProfileView";
	public static ProfileView view;
	public SampleController controller = SampleController.getController();
	// private Display display;
	private static final Logger logger = LoggerFactory.getLogger(ProfileView.class);
	private IPlottingSystem plottingSystem_x;
	private IPlottingSystem plottingSystem_y;
	private String title = "Profile Line Plot";

	public ProfileView() {
		super();
		try {
	        this.plottingSystem_x = PlottingFactory.createPlottingSystem();
	        plottingSystem_x.setColorOption(ColorOption.NONE);
	        this.plottingSystem_y = PlottingFactory.createPlottingSystem();
	        plottingSystem_y.setColorOption(ColorOption.NONE);
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
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
		view = this;
		// display = parent.getDisplay();
		parent.setLayout(new GridLayout());
		final Composite plot = new Composite(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		plot.setLayout(fillLayout);
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		try {
	        final IActionBars bars = this.getViewSite().getActionBars();
			plottingSystem_x.createPlotPart(plot, title, bars, PlotType.XY, this);
			plottingSystem_y.createPlotPart(plot, title, bars, PlotType.XY, this);
			
			// No toolbar or menubar required. Discussed with Andy Gotz
            bars.getToolBarManager().removeAll();
            bars.getMenuManager().removeAll();
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
	 * Set the new x and y profile data to plot and display as two XY plots
	 * 
	 * @param title
	 *            - title to display with plot
	 * @param pixel_x
	 *            - x pixel array (integers)
	 * @param profile_x
	 *            - x profile to plot as f(x)
	 * @param pixel_y
	 *            - y pixel array (integers)
	 * @param profile_y
	 *            - y profile to plot as f(x)
	 */
	public void setData(String title, String xTitle, String yTitle,
			boolean xInverted, boolean yInverted, float pixel_x[],
			float profile_x[], float pixel_y[], float profile_y[]) {
		double[] x_x = new double[pixel_x.length];
		double[] x_y = new double[pixel_x.length];
		for (int i = 0; i < pixel_x.length; i++) {
			x_x[i] = pixel_x[i];
			if (xInverted) {
				// Reverse the y array to go with the inverted x axis
				// Seems to be the way JLChart works
				x_y[i] = profile_x[pixel_x.length - 1 - i];
			} else {
				x_y[i] = profile_x[i];
			}
		}
		DoubleDataset x_xAxis = new DoubleDataset(x_x, pixel_x.length);
		DoubleDataset x_yData = new DoubleDataset(x_y, pixel_x.length);
		x_xAxis.setName("X pixel");
		x_yData.setName("Intensity");
		final List<IDataset> x_yDataSets = new ArrayList<IDataset>(1);
		x_yDataSets.add(x_yData);
		plottingSystem_x.clear();
		plottingSystem_x.createPlot1D(x_xAxis, x_yDataSets, null);
		double[] y_x = new double[pixel_y.length];
		double[] y_y = new double[pixel_y.length];
		for (int i = 0; i < pixel_y.length; i++) {
			y_x[i] = pixel_y[i];
			if (yInverted) {
				// Reverse the y array to go with the inverted x axis
				// Seems to be the way JLChart works
				y_y[i] = profile_y[pixel_y.length - 1 - i];
			} else {
				y_y[i] = profile_y[i];
			}
		}
		DoubleDataset y_xAxis = new DoubleDataset(y_x, pixel_y.length);
		DoubleDataset y_yData = new DoubleDataset(y_y, pixel_y.length);
		y_xAxis.setName("Y pixel");
		y_yData.setName("Intensity");
		final List<IDataset> y_yDataSets = new ArrayList<IDataset>(1);
		y_yDataSets.add(y_yData);
		plottingSystem_y.createPlot1D(y_xAxis, y_yDataSets, null);
	}
}
