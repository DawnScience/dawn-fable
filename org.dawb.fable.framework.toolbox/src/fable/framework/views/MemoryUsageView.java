/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.views;

import java.awt.Container;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import fable.framework.toolbox.Activator;
import fable.framework.toolbox.JLChartMemoryUsage;
import fable.framework.toolbox.SWTUtils;
import fable.framework.toolboxpreferences.PreferenceConstants;

/**
 * MemoryUsageView. Creates a view with a JLChart MemoryUsage JPanel.
 * 
 * @author Kenneth Evans, Jr.
 */
public class MemoryUsageView extends ViewPart {
	public static final String ID = "fable.framework.views.memoryusageview";
	private IPreferenceStore prefs = null;
	Composite awtComposite = null;
	private java.awt.Frame frame = null;
	private static JLChartMemoryUsage panel = null;

	private int interval = JLChartMemoryUsage.DEFAULT_INTERVAL;
	private int maxAge = JLChartMemoryUsage.DEFAULT_AGE;
	private boolean showMax = JLChartMemoryUsage.DEFAULT_SHOW_MAX;
	private boolean showLegend = JLChartMemoryUsage.DEFAULT_SHOW_LEGEND;

	protected Action showMaxAction;
	protected Action showLegendAction;
	protected Action setIntervalAction;
	protected Action setMaxAgeAction;

	public MemoryUsageView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite,
	 * org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		// Create actions
		makeActions();
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
		try {
			contributeToActionBars();

			// Make a Frame in a Composite
			awtComposite = new Composite(parent, SWT.EMBEDDED);
			frame = SWT_AWT.new_Frame(awtComposite);

			// Make the job run in the AWT thread
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						// Perform necessary magic
						// See "Swing/SWT Integration" by Gordon Hirsch
						JApplet applet = new JApplet();
						applet.setFocusCycleRoot(false);
						frame.add(applet);
						Container contentPane = applet.getRootPane()
								.getContentPane();
						panel = new JLChartMemoryUsage();
						contentPane.add(panel);

						// Set the preferences
						prefs = Activator.getDefault().getPreferenceStore();
						showLegend = prefs
								.getBoolean(PreferenceConstants.P_MU_SHOW_LEGEND);
						panel.setShowLegend(showLegend);
						showMax = prefs
								.getBoolean(PreferenceConstants.P_MU_SHOW_MAX);
						panel.setShowMax(showMax);
						interval = prefs
								.getInt(PreferenceConstants.P_MU_INTERVAL);
						panel.setInterval(interval);
						interval = panel.getInterval();
						maxAge = prefs.getInt(PreferenceConstants.P_MU_MAX_AGE);
						panel.setMaxAge(maxAge);
						maxAge = panel.getMaxAge();

						// Start the timer
						panel.start();
					} catch (Throwable t) {
						SWTUtils
								.errMsgAsync("Unable to create MemoryUsageView:\n"
										+ t + "\n" + t.getMessage());
						t.printStackTrace();
					}
				}
			});
		} catch (Throwable t) {
			SWTUtils.errMsgAsync("Unable to create MemoryUsageView:\n" + t
					+ "\n" + t.getMessage());
			t.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Is this OK?
		if (awtComposite != null)
			awtComposite.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (panel != null) {
			panel.stop();
		}
		if (frame != null) {
			if (panel != null)
				frame.remove(panel);
			frame = null;
		}
		panel = null;
		if (awtComposite != null && !awtComposite.isDisposed()) {
			awtComposite.dispose();
			awtComposite = null;
		}
		super.dispose();
	}

	/**
	 * Makes actions to be used by all views.
	 */
	protected void makeActions() {
		// Toggle show max
		showMaxAction = new Action() {
			public void run() {
				if (panel != null) {
					panel.toggleMax();
				}
			}
		};
		showMaxAction.setText("Toggle Max Memory");
		showMaxAction.setToolTipText("Toggle showing maxium memory available");

		// Toggle legend
		showLegendAction = new Action() {
			public void run() {
				if (panel != null) {
					showLegend = !showLegend;
					panel.setShowLegend(showLegend);
				}
			}
		};
		showLegendAction.setText("Toggle Legend");
		showLegendAction.setToolTipText("Toggle legend.");

		// Set interval
		setIntervalAction = new Action() {
			public void run() {
				if (panel == null)
					return;
				// Prompt for value
				InputDialog dialog = new InputDialog(Display.getCurrent()
						.getActiveShell(), "Input",
						"Enter update interval in ms:", String
								.valueOf(interval), null);
				int rc = dialog.open();
				if (rc == Window.OK) {
					String stringVal = dialog.getValue();
					int newVal;
					try {
						newVal = Integer.parseInt(stringVal);
						if (panel != null && newVal > 0) {
							panel.setInterval(newVal);
							interval = panel.getInterval();
						}
					} catch (Exception ex) {
						SWTUtils.excMsgAsync(Display.getCurrent()
								.getActiveShell(), "Invalid value", ex);
					}
				}
			}
		};
		setIntervalAction.setText("Set Interval");
		setIntervalAction.setToolTipText("Set the update interval in ms.");

		// Set max age
		setMaxAgeAction = new Action() {
			public void run() {
				if (panel == null)
					return;
				// Prompt for value
				InputDialog dialog = new InputDialog(Display.getCurrent()
						.getActiveShell(), "Input",
						"Enter the maximum age in ms:", String.valueOf(maxAge),
						null);
				int rc = dialog.open();
				if (rc == Window.OK) {
					String stringVal = dialog.getValue();
					int newVal;
					try {
						newVal = Integer.parseInt(stringVal);
						if (panel != null && newVal > 0) {
							panel.setMaxAge(newVal);
							maxAge = panel.getMaxAge();
						}
					} catch (Exception ex) {
						SWTUtils.excMsgAsync(Display.getCurrent()
								.getActiveShell(), "Invalid value", ex);
					}
				}
			}
		};
		setMaxAgeAction.setText("Set Max Age");
		setMaxAgeAction.setToolTipText("Set the update interval in ms.");
	}

	/**
	 * Contributes to the action bars. Subclasses must call this method in
	 * createPartControl for them to appear. Subclasses should not override but
	 * should implement fillLocalPullDown and fillLocalToolBar.
	 */
	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fills the local pulldown menu on the View. Subclasses can override.
	 * 
	 * @param manager
	 */
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(showLegendAction);
		manager.add(showMaxAction);
		manager.add(setIntervalAction);
		manager.add(setMaxAgeAction);
	}

	/**
	 * Fills the local toolbar menu on the View. Subclasses can override.
	 * 
	 * @param manager
	 */
	protected void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(showMaxAction);
		// manager.add(showLegendAction);
	}

}
