/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import fable.framework.ui.views.ColFileXYPlot;
import fable.framework.ui.views.ColumnFilePlotView;
import fr.esrf.tangoatk.widget.util.chart.JLChart;

/**
 * This class is used to open JLChart option dialogue from a view option if
 * ColFileXYPlot and JL chart are not null.<br>
 * 
 * @author SUCHET
 * 
 */
public class OpenChartOptions implements IViewActionDelegate {
	ColFileXYPlot xyplot;

	public void init(IViewPart view) {
		xyplot = ColumnFilePlotView.view.getxyPlot();

	}

	public void run(IAction action) {
		if (xyplot != null) {

			final JLChart chart = xyplot.getChart();

			if (chart != null) {
				chart.showOptionDialog();
			}
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
