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
 * 
 */
package fable.framework.ui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import fable.framework.internal.IPropertyVarKeys;
import fable.python.ColumnFile;

/**
 * This class is a table view of column file. A Column File is a space separated
 * columns in ascii format. The column data are displayed in a table. An
 * alternative view of a column file is an XY plot implemented in the
 * ColumnFilePlotView.
 * 
 * @author Gaelle Suchet
 * 
 */
public class ColumnFileContentView extends ViewPart implements
		IPropertyChangeListener {

	/** View id. */
	public static final String ID = "fable.framework.ui.views.ColumnContentView";
	/**
	 * Color blue for rows.
	 */
	final org.eclipse.swt.graphics.Color light_blue = new org.eclipse.swt.graphics.Color(
			Display.getCurrent(), 228, 247, 248);
	/** A sort listener for table. */
	private Listener sortListener;// listener to sort columns
	/** A tableViewer */
	TableViewer tableViewer; // Virtual table
	/** Table content in tableViewer */
	Table table;
	/** This view. */
	public static ColumnFileContentView view;
	/** ColumnFile. */
	private ColumnFile columns = null;

	/** A Logger. */
	// private Logger logger;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// display=Display.getCurrent();
		view = this;

		parent.setLayout(new GridLayout(1, false));
		GridData gdExplorer = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		gdExplorer.horizontalAlignment = GridData.FILL;
		gdExplorer.verticalAlignment = GridData.FILL;
		gdExplorer.horizontalSpan = 1;
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.VIRTUAL);
		//
		tableViewer.setContentProvider(new ArrayContentProvider());

		getSite().setSelectionProvider(tableViewer);
		table = tableViewer.getTable();

		table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;

				item.setBackground((index % 2 == 0) ? Display.getCurrent()
						.getSystemColor(SWT.COLOR_WHITE) : light_blue);

				for (int j = 0; j < columns.getNCols(); j++) {
					String key = table.getColumns()[j].getText();
					item.setText(j, "" + columns.getColumnFileCell(index, key));

				}

			}
		});

		//
		createSortListener();

	}

	private void createSortListener() {
		sortListener = new Listener() {
			// Function called to move items while sorting
			public void handleEvent(Event e) {
				TableColumn sortColumn = table.getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;

				// determine new sort column and direction

				int dir = table.getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					table.setSortColumn(currentColumn);
					dir = SWT.UP;
				}

				// sort the data based on column and direction
				TableColumn[] cols = table.getColumns();
				TableItem[] items = table.getItems();

				TableColumn column = (TableColumn) e.widget;
				int index = 0;
				for (int x = 0; index == 0 && x < cols.length; x++) {
					if (cols[x].getText().equals(column.getText())) {
						index = x;

					}
				}

				final int direction = dir;
				for (int i = 0; i < items.length; i++) {
					String value1 = items[i].getText(index);
					for (int j = 0; j < i; j++) {
						String value2 = items[j].getText(index);
						try {
							float fValue1 = Float.parseFloat(value1);
							float fValue2 = Float.parseFloat(value2);

							if (direction == SWT.UP) {
								if (fValue1 < fValue2) {
									String[] values = new String[cols.length];
									for (int n = 0; n < cols.length; n++) {
										values[n] = items[i].getText(n);
									}
									items[i].dispose();
									TableItem item = new TableItem(table,
											SWT.NONE, j);
									item.setText(values);
									items = table.getItems();
									break;
								}
							} else {
								if (fValue1 > fValue2) {
									String[] values = new String[cols.length];
									for (int n = 0; n < cols.length; n++) {
										values[n] = items[i].getText(n);
									}
									items[i].dispose();
									TableItem item = new TableItem(table,
											SWT.NONE, j);
									item.setText(values);
									items = table.getItems();
									break;
								}
							}
						} catch (NumberFormatException ne) {
							if (direction == SWT.UP) {
								if (value1.compareTo(value2) < 0) {
									String[] values = new String[cols.length];
									for (int n = 0; n < cols.length; n++) {
										values[n] = items[i].getText(n);
									}
									items[i].dispose();
									TableItem item = new TableItem(table,
											SWT.NONE, j);
									item.setText(values);
									items = table.getItems();

									break;
								}
							} else {
								if (value1.compareTo(value2) > 0) {
									String[] values = new String[cols.length];
									for (int n = 0; n < cols.length; n++) {
										values[n] = items[i].getText(n);
									}
									items[i].dispose();
									TableItem item = new TableItem(table,
											SWT.NONE, j);
									item.setText(values);
									items = table.getItems();

									break;
								}
							}
						}

					}
				}

				// update data displayed in table
				table.setSortDirection(dir);
				table.setSortColumn(column);
				// Color rows
				for (int n = 0; n < table.getItemCount(); n++) {
					table.getItem(n).setBackground(
							(n % 2 == 0) ? Display.getCurrent().getSystemColor(
									SWT.COLOR_WHITE) : light_blue);
				}

			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * update the column file with a new Column File and re-populate the table
	 * 
	 * @param newValue
	 */
	private void updateColumnFile(ColumnFile newValue) {
		columns = newValue;
		// Listen to changes in columnFile
		columns.addPropertyChangeListener(this);
		this.setPartName(columns.getFullFileName());
		populateTable();
	}

	/**
	 * Gaelle 24/06/2008 15h24
	 * 
	 * @param col
	 *            load a columnfile in this view
	 */
	public void setColumnFile(ColumnFile col) {
		columns = col;
		tableViewer.setInput(columns);
		// Listen to changes in columnFile
		columns.addPropertyChangeListener(this);
		this.setPartName(columns.getFullFileName());
		populateTable();
	}

	private void addColumnFile(ColumnFile newValue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange
	 * (org.eclipse.jface.util.PropertyChangeEvent)
	 */
	/**
	 * @description fill table with flt file
	 */
	private void populateTable() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {

				table.removeAll();
				table.clearAll();

				while (table.getColumnCount() > 0) {
					table.getColumn(0).removeListener(SWT.Selection,
							sortListener);
					table.getColumn(0).dispose();
				}
				// Set columns
				columns.loadRows();
				String[] keys = columns.getTitles();

				for (int i = 0; i < columns.getNCols(); i++) {

					TableColumn tb = new TableColumn(table, SWT.LEFT);
					tb.setText(keys[i]);
					tb.addListener(SWT.Selection, sortListener);

				}

				table.setItemCount(columns.getNRows());
				for (int i = 0; i < table.getColumnCount(); i++) {
					table.getColumn(i).pack();
				}

			}
		});
	}

	@Override
	public void dispose() {
		if (light_blue != null) {
			if (!light_blue.isDisposed()) {
				light_blue.dispose();
			}
		}
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (((String) event.getProperty()).equals(IPropertyVarKeys.ADDCOLUMN)) {

			addColumnFile(((ColumnFile) event.getNewValue()));
			populateTable();

		} else if (((String) event.getProperty())
				.equals(IPropertyVarKeys.UPDATECOLUMN)) {
			updateColumnFile(((ColumnFile) event.getNewValue()));
			populateTable();
		}

	}

}
