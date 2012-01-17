/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.editors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import fable.framework.internal.IPropertyVarKeys;
import fable.framework.toolbox.Activator;
import fable.framework.toolbox.IImagesKeys;
import fable.framework.ui.actions.SaveAsColumnFileEditorAction;
import fable.framework.ui.actions.SaveColumnFileEditorAction;

public class ColumnFileEditor extends EditorPart implements
		IPropertyChangeListener, IColumnFileEditor {

	private static final String ID = "fable.framework.ui.ColumnFileEditor";
	/** Column editor input. */
	private ColumnFileEditorInput columnInput;
	/** Boolean to get if editor have been changed. */
	private boolean dirty = false;
	/** tableViewer. */
	private TableViewer tableViewer;
	/**
	 * Color blue for rows.
	 */
	final org.eclipse.swt.graphics.Color light_blue = new org.eclipse.swt.graphics.Color(
			Display.getCurrent(), 228, 247, 248);
	/**
	 * Table that makes <code>tableViewer<code>. We are filling data via a 
	 * SetData event because, contentProvider and labelProvider are not 
	 * implemented here since we don' t have rows.
	 */
	private Table table;
	/** A sort listener set on columns to sort values. */
	private Listener sortListener;// listener to sort columns
	/** An image for popup menu to remove a selected file from the table. */
	private Image imgMenuRemove;
	/** An image descriptor for button delete row. */
	private ImageDescriptor GifDelete = fable.framework.toolbox.Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.BTN_IMG_DELETE);
	private ImageDescriptor keep_descriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "images/check.gif");
	/** This image is set to menu on table to keep selected rows. */
	private Image imageKeep;

	private SaveColumnFileEditorAction saveAction;
	private SaveAsColumnFileEditorAction saveasAction;

	/**
	 * Save as action for this editor
	 */
	// private IAction saveasAction;
	/*
	 * private ImageDescriptor saveAs = fable.framework.ui.rcp.Activator
	 * .imageDescriptorFromPlugin( fable.framework.ui.rcp.Activator.PLUGIN_ID,
	 * "images/saveas.gif");
	 */

	// SaveColumnFileEditorAction action;
	public ColumnFileEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		columnInput.save();
		dirty = false;
		firePropertyChange(PROP_DIRTY);

	}

	@Override
	public void doSaveAs() {
		if (columnInput.saveAs()) {
			dirty = false;
			firePropertyChange(PROP_DIRTY);
			this.setPartName(columnInput.getColumn().getFileName());
		}

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof ColumnFileEditorInput) {
			columnInput = (ColumnFileEditorInput) input;
		} else if (input instanceof FileEditorInput) {
			// create a ColumnFileEditorInput
			IPath ipath = ((FileEditorInput) input).getPath();
			if (ipath != null) {
				columnInput = new ColumnFileEditorInput(ipath.toString());
			}

		}
		if (columnInput != null) {
			this.setPartName(columnInput.getColumn().getFileName());
			initSortListener();
			columnInput.getColumn().addPropertyChangeListener(this);

		}
		// makeActions();
	}

	@Override
	public boolean isDirty() {

		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {

		return dirty;
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));
		GridData gdExplorer = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		gdExplorer.horizontalAlignment = GridData.FILL;
		gdExplorer.verticalAlignment = GridData.FILL;
		gdExplorer.horizontalSpan = 1;
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.VIRTUAL);
		//
		tableViewer.setContentProvider(new ColumnFileContentProvider(
				tableViewer));
		tableViewer.setUseHashlookup(true);
		tableViewer.setLabelProvider(new ColumnFileLabelProvider());
		getSite().setSelectionProvider(tableViewer);
		tableViewer.setInput(columnInput.getColumn());
		table = tableViewer.getTable();

		table.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {

					// System.out.println("mouse down" + key );
					Menu menu = new Menu(Display.getCurrent().getActiveShell(),
							SWT.POP_UP);
					MenuItem mitemRemove = new MenuItem(menu, SWT.PUSH);
					MenuItem mitemKeep = new MenuItem(menu, SWT.PUSH);
					imgMenuRemove = GifDelete.createImage();
					mitemRemove.setImage(imgMenuRemove);
					mitemRemove.setText("Throw");
					mitemRemove.addDisposeListener(new DisposeListener() {
						// @Override
						public void widgetDisposed(DisposeEvent arg0) {
							if (imgMenuRemove != null
									&& !imgMenuRemove.isDisposed()) {
								imgMenuRemove.dispose();
							}

						}
					});
					mitemRemove.addListener(SWT.Selection, new Listener() {

						public void handleEvent(Event event) {
							// remove rows

							TableItem[] selection = table.getSelection();
							if (selection.length > 0
									&& table.getItemCount() > 0) {
								table.getColumnCount();
								// a table of id
								Object[] idlist = new Object[selection.length];

								for (int i = 0; i < selection.length; i++) {
									idlist[i] = selection[i].getText(table
											.getColumnCount() - 1);
								}
								columnInput.getColumn().removeRow(
										idlist,
										columnInput.getColumn()
												.getColumnfileId());
								dirty = true;
								firePropertyChange(PROP_DIRTY);
							}
						}
					});

					imageKeep = keep_descriptor.createImage();
					mitemKeep.setImage(imageKeep);
					mitemKeep.setText("Keep");
					mitemKeep.addDisposeListener(new DisposeListener() {

						// @Override
						public void widgetDisposed(DisposeEvent arg0) {
							if (imageKeep != null && !imageKeep.isDisposed()) {
								imageKeep.dispose();
							}

						}

					});
					mitemKeep.addListener(SWT.Selection, new Listener() {

						public void handleEvent(Event event) {
							// keep rows
							TableItem[] selection = table.getSelection();
							if (selection.length > 0
									&& table.getItemCount() > 0) {
								table.getColumnCount();
								// a table of id
								Object[] idlist = new Object[selection.length];

								for (int i = 0; i < selection.length; i++) {
									idlist[i] = selection[i].getText(table
											.getColumnCount() - 1);
								}
								columnInput.getColumn().keepRow(
										idlist,
										columnInput.getColumn()
												.getColumnfileId());
								dirty = true;
								firePropertyChange(PROP_DIRTY);
							}

						}
					});

					menu.setVisible(true);

					while (!menu.isDisposed() && menu.isVisible()) {
						if (!Display.getCurrent().readAndDispatch())
							Display.getCurrent().sleep();
					}
					menu.dispose();

				}

			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

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

			}
		});
		populateTable();

		// getEditorSite().getActionBarContributor().setActiveEditor(this);

		/*
		 * getEditorSite().getActionBars().setGlobalActionHandler(
		 * IVarKeys.SAVE_AS_ACTION, saveasAction);
		 */
		/*
		 * table.addListener(SWT.SetData,new Listener(){ public void
		 * handleEvent(Event event) { TableItem item = (TableItem)event.item;
		 * int index = event.index;
		 * 
		 * item.setBackground((index%2==0)? Display.getCurrent().
		 * getSystemColor(SWT.COLOR_WHITE) : light_blue );
		 * 
		 * for(int j=0; j<columnInput.getColumn().getNCols(); j++){ String key =
		 * table.getColumns()[j].getText(); item.setText(j,""+
		 * columnInput.getColumn(). getColumnFileCell(index, key));
		 * 
		 * }
		 * 
		 * }});
		 */
		makeActions();
		// contributetoActionBar();

	}

	/*
	 * private void contributetoActionBar() { IActionBars bars =
	 * this.getEditorSite().getActionBars();
	 * fillLocalToolBar(bars.getToolBarManager());
	 * 
	 * 
	 * }
	 * 
	 * 
	 * private void fillLocalToolBar(IToolBarManager toolBarManager) {
	 * toolBarManager.add(saveAction); toolBarManager.add(saveasAction); }
	 */

	private void makeActions() {
		saveAction = new SaveColumnFileEditorAction() {
			@Override
			public void run(IColumnFileEditor editor) {
				((ColumnFileEditor) editor).doSave(null);
			}
		};
		saveAction.setProps("Save column file");
		saveasAction = new SaveAsColumnFileEditorAction() {
			@Override
			public void run(IColumnFileEditor editor) {
				((ColumnFileEditor) editor).doSaveAs();
			}
		};

		saveasAction.setProps("Save column file as...");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static String getId() {
		return ID;
	}

	/**
	 * This methods set columns names and init the number of items in table for
	 * virtual tableViewer.<br>
	 * <code>table.setItemCount( columnInput.getColumn().getNRows());</code>
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
				String[] keys = columnInput.getColumn().getTitles();

				for (int i = 0; i < columnInput.getColumn().getNCols(); i++) {

					TableColumn tb = new TableColumn(table, SWT.LEFT);
					tb.setText(keys[i]);
					tb.addListener(SWT.Selection, sortListener);

				}

				table.setItemCount(columnInput.getColumn().getNRows());

				for (int i = 0; i < table.getColumnCount(); i++) {
					table.getColumn(i).pack();
				}

			}
		});
	}

	/**
	 * This methods initializes sort listener when user select a column.
	 */
	private void initSortListener() {
		sortListener = new Listener() {
			public void handleEvent(Event e) {

				TableColumn sortColumn = table.getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;

				TableColumn[] cols = table.getColumns();
				// tableViewer.update(element, properties)
				TableColumn column = (TableColumn) e.widget;
				int index = 0;
				for (int x = 0; index == 0 && x < cols.length; x++) {
					if (cols[x].getText().equals(column.getText())) {
						index = x;

					}
				}
				int dir = table.getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					table.setSortColumn(currentColumn);
					dir = SWT.UP;
				}

				columnInput.getColumn().setSortedIndex(index, dir);
				tableViewer.setInput(columnInput.getColumn());

				table.setSortDirection(dir);
				table.setSortColumn(column);

			}
		};

	}

	/*
	 * private void makeActions() {
	 * 
	 * saveasAction = new Action("saveAs") { public void run() { doSaveAs(); }
	 * 
	 * }; saveasAction.setId(IVarKeys.SAVE_AS_ACTION);
	 * saveasAction.setImageDescriptor(saveAs);
	 * saveasAction.setToolTipText("Save this column file as ... ");
	 * 
	 * }
	 */
	@Override
	public boolean isSaveOnCloseNeeded() {

		return true;
	}

	@Override
	public void dispose() {
		columnInput.getColumn().removePropertyrChangeListener(this);
	}

	// @Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getProperty().equals(IPropertyVarKeys.UPDATECOLUMN)) {
			dirty = true;
			firePropertyChange(PROP_DIRTY);
			// columnInput.getColumn().setRows();
			table.setItemCount(columnInput.getColumn().getNRows());
			tableViewer.setInput(columnInput.getColumn());
			populateTable();

		} else if (arg0.getProperty().equals(IPropertyVarKeys.PROPDIRTY)) {
			dirty = (Boolean) arg0.getNewValue();
			firePropertyChange(PROP_DIRTY);
			this.setPartName(columnInput.getColumn().getFileName());
		}

	}

	/*
	 * public IAction getAction() {
	 * 
	 * return saveasAction; }
	 */
}
