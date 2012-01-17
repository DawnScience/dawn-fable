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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import jep.JepException;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import fable.framework.toolbox.Activator;
import fable.framework.toolbox.IImagesKeys;
import fable.framework.toolbox.LookAndFeel;
import fable.framework.toolbox.StringText;
import fable.framework.toolbox.ToolBox;
import fable.python.Experiment;
import fable.python.ISampleListener;
import fable.python.Sample;
import fable.python.SampleEvent;
import fable.python.contentprovider.SampleTableContentProvider;
import fable.python.labelprovider.SampleTablelabelProvider;

public class FableSampleLoaderView extends ViewPart {

	public static final String ID = "fable.framework.views.FableSampleLoaderView";
	public static FableSampleLoaderView view;
	private Table table;

	private String[] files;
	private String fileDescription;
	private int numberFiles;
	private Color descriptionColor;

	private String[] selectedFiles;

	private Button selectFilesButton;
	// KE: This is never really uised
	// private Shell shell;
	private Display display;

	/* Experiment */
	private StringText stextSampleDirectory;
	String experimentName = "Experiment"; // current experiment name
	String sampleName = "Sample"; // current sample name
	String sSampleDirectory; // current sample directory
	private ArrayList<IPropertyChangeListener> array = new ArrayList<IPropertyChangeListener>();

	private ImageDescriptor GifDelete = AbstractUIPlugin
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.BTN_IMG_DELETE);
	private ImageDescriptor GifRemove = AbstractUIPlugin
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.BTN_IMG_SUBTRACT);
	private ImageDescriptor GifAdd = AbstractUIPlugin
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.BTN_IMG_ADD);

	private Button btnRemoveSelect, btnRemoveSample;
	private Sample sample; // current sample

	// private Combo cboKeyColumn;
	private List listKeys;
	private Button addKeyInTab, removeKeyInTable;
	private Vector<String> titles = new Vector<String>();
	private Listener sortListener;

	final Color light_blue = new Color(Display.getCurrent(), 228, 247, 248);
	final Color light_Red = new Color(Display.getCurrent(), 249, 172, 168);
	final Color light_Green = new Color(Display.getCurrent(), 168, 249, 200);

	private TableColumn columnSelected;
	public Text stextExperiment, stextSample;
	// private Group grp_experiment ;
	final Image imgDelete;
	final Image imgadd;
	final Image imgRemove;
	// private SampleListener sampleListener;
	private int columnIndexSelected = -1;
	// -------------------------------------------GS
	// 1.3.0----------------------------------------
	private Vector<Sample> listOfSamples;
	private TableViewer sampleTable;
	private SashForm sash;
	// Add a check box for online mode if the user wants to process previous
	// scaned files
	// private Button chkProcessPrevious;
	// private FabioFile f_fabio;// current selected file
	private int currSelection = 0;

	/**
	 * 
	 * @description constructor. Init images and sortListener to sort in table
	 *              files
	 */
	public FableSampleLoaderView() {

		imgDelete = GifDelete.createImage();
		imgadd = GifAdd.createImage();
		imgRemove = GifRemove.createImage();

		// GS 1.3.0
		listOfSamples = new Vector<Sample>();
		// KE: THis is not used
		// sampleListener = new SampleListener();

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
				columnSelected = (TableColumn) e.widget;
				// Collator collator =
				// Collator.getInstance(Locale.getDefault());

				int index = 0;
				for (int x = 0; index == 0 && x < cols.length; x++) {
					if (cols[x].getText().equals(column.getText())) {
						index = x;
						columnIndexSelected = index;
					}
				}

				final int direction = dir;
				for (int i = 1; i < items.length; i++) {
					String value1 = items[i].getText(index);
					for (int j = 1; j < i; j++) {
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
				// PART FOR REMOVE COLUMN : delete the firsts two rows is not
				// allowed
				if (columnIndexSelected > 1) {
					removeKeyInTable.setEnabled(true);
				} else {
					removeKeyInTable.setEnabled(false);
				}

			}
		};

	}

	@Override
	public void createPartControl(Composite parent) {
		// shell = parent.getShell();
		display = Display.getCurrent();

		view = this;
		view.setContentDescription("Select sample here");

		// init with most common standart used
		titles.add(0, "#");
		titles.add(1, "Name");
		GridLayout gdL = new GridLayout(1, true);
		parent.setLayout(gdL);

		createInfoExperiment(parent);
		createTabListe(parent);
	}

	/**
	 * 
	 * 19 nov. 07
	 * 
	 * @author G. Suchet
	 * @param parent
	 * @description create Group experimentInfo with allows user to select files
	 *              to process and displays experiment name and sample name
	 */
	private void createInfoExperiment(Composite parent) {
		Group experimentInfo = LookAndFeel.getGroup(parent,
				"Image files to process", 4, 1);

		GridData gd3 = new GridData(SWT.FILL, SWT.RESIZE, true, false);
		gd3.horizontalSpan = 3;
		stextSampleDirectory = new StringText(experimentInfo, SWT.FILL,
				"Directory");
		stextSampleDirectory.set_isRequiredField(true);
		stextSampleDirectory.setLayoutData(gd3);
		// select files button used in offline mode
		selectFilesButton = new Button(experimentInfo, SWT.PUSH | SWT.RESIZE
				| SWT.FILL);
		selectFilesButton.setText("Select files...");
		selectFilesButton.setEnabled(true);

		selectFilesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Trace-----------------------------------------------------

				System.out
						.println("ProcessingView :click on selectFilesButton");
				// -----------------------------------------------------

				// FileAndDirectoryDialog dialog = new FileAndDirectoryDialog
				// (shell,SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL |
				// SWT.MULTI);

				// dialog.setText("Sample selector");

				// If not dialog is canceled

				// selectedFiles = dialog.open(stextSampleDirectory.getText());
				if (selectedFiles != null) {
					clearAll();
					removeAllColumnsAdded();

					loadSamples(selectedFiles); // listOfSamples instantiated
					populateTreeSample(); // populate sample tree
					// 1.3.0 set current sample with the first sample of the
					// list
					if (listOfSamples != null && listOfSamples.size() > 0) {
						sample = listOfSamples.elementAt(0);

						// Show the files of the first element
						if (sampleTable.getTable().getItemCount() > 0) {
							sampleTable.getTable().setSelection(
									sampleTable.getTable().getItem(0));
							sampleTable.getTable().notifyListeners(
									SWT.Selection, new Event());
						}

					}

				}
			}
		});

		//
		Label lblExperiment = new Label(experimentInfo, SWT.NONE);
		lblExperiment.setText("Experiment:");
		stextExperiment = new Text(experimentInfo, SWT.NO_BACKGROUND
				| SWT.READ_ONLY | SWT.NO_FOCUS);
		GridData gd1 = new GridData(GridData.FILL, SWT.NONE, true, false);
		stextExperiment.setLayoutData(gd1);

		stextExperiment.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		stextExperiment.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));

		Label lblsSampName = new Label(experimentInfo, SWT.NONE);
		lblsSampName.setText("Sample:");
		GridData gd2sTextSample = new GridData(GridData.FILL, SWT.RESIZE, true,
				false);
		stextSample = new Text(experimentInfo, SWT.NO_BACKGROUND
				| SWT.READ_ONLY | SWT.NO_FOCUS);
		stextSample.setLayoutData(gd2sTextSample);
		stextSample.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		stextSample.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));

	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @param parent
	 * @description create the table that contains files to process and
	 *              sampleTable
	 */
	private void createTabListe(Composite parent) {

		Group grpListe = LookAndFeel.getGroup(parent, "Files", 4, 1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		grpListe.setLayoutData(gd2);
		Label lblKey = new Label(grpListe, SWT.NONE);

		lblKey.setText("Header Key");
		/***********************************/
		listKeys = new List(grpListe, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.FILL, GridData.CENTER, true, false);
		gdlist.verticalSpan = 1;
		gdlist.horizontalSpan = 1;
		int listHeigth = listKeys.getItemHeight() * 4;

		Rectangle trim = listKeys.computeTrim(0, 0, 0, listHeigth);
		gdlist.heightHint = trim.height;

		listKeys.setLayoutData(gdlist);

		listKeys.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int index = ((List) e.getSource()).getSelectionIndex();
				if (index == -1) {
					index = 0;
				}
				addKeyInTab.notifyListeners(SWT.Selection, new Event());

			}

		});
		/************************************/

		addKeyInTab = new Button(grpListe, SWT.PUSH);
		addKeyInTab.setImage(imgadd);
		addKeyInTab.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false));
		addKeyInTab.setToolTipText("Add selected key in table");
		addKeyInTab.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					addColumn();
				} catch (JepException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

		addKeyInTab.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgadd != null) {
					imgadd.dispose();
				}

			}

		});
		removeKeyInTable = new Button(grpListe, SWT.PUSH);
		removeKeyInTable.setImage(imgRemove);
		removeKeyInTable.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false));
		removeKeyInTable.setToolTipText("Remove selected column");
		removeKeyInTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				removeColumn();
			}

		});
		removeKeyInTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgRemove != null) {
					imgRemove.dispose();
				}

			}

		});
		// Init : remove key is disabled because no column has been selected yet
		removeKeyInTable.setEnabled(false);
		// -------------------------------------sampleTree
		// 1.3.0--------------------------------------
		sash = new SashForm(grpListe, SWT.HORIZONTAL);

		GridData gdExplorer = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		gdExplorer.horizontalAlignment = GridData.FILL;
		gdExplorer.verticalAlignment = GridData.FILL;
		gdExplorer.horizontalSpan = 4;
		sash.setLayoutData(gdExplorer);
		GridData gdTree = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTree.horizontalSpan = 1;

		sampleTable = new TableViewer(sash, SWT.BORDER);
		sampleTable.setContentProvider(new SampleTableContentProvider());
		sampleTable.setLabelProvider(new SampleTablelabelProvider());
		sampleTable.getTable().setHeaderVisible(true);
		sampleTable.getTable().setLinesVisible(true);
		// populate table files with sample files
		sampleTable.getTable().addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {

					// System.out.println("mouse down" + key );
					Menu menu = new Menu(Display.getCurrent().getActiveShell(),
							SWT.POP_UP);
					MenuItem mitemAdd = new MenuItem(menu, SWT.PUSH);

					mitemAdd.addListener(SWT.Selection, new Listener() {

						public void handleEvent(Event event) {
							btnRemoveSample.notifyListeners(SWT.Selection,
									new Event());
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

		sampleTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] ti = ((Table) e.widget).getSelection();

				if (ti != null && ti.length > 0) {

					sample = (Sample) ti[0].getData();
					System.out.println("Selected sample name : "
							+ sample.getDirectoryName());
					if (sample.hasFile()) {
						// load files and populate combo

						populateFileTable(
								"Files to process (entire directory)", sample
										.getFiles());
						System.out.println("Header key for this sample"
								+ sample.getDirectoryName()
								+ " has been loaded in the list");

					} else {
						populateFileTable(
								"No files to process found in sample", null);
					}
					populateCombo();
					// init constant header

				}
			}
		});

		// Files
		// TABLE-----------------------------------------------------------------------
		table = new Table(sash, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION
				| SWT.VIRTUAL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		GridData gd4 = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd4.horizontalSpan = 3;
		gd4.verticalIndent = 10;
		// table.setLayoutData(gd4);
		table.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseDown(MouseEvent e) {
				if (e.button == 3 && table.getSelectionIndex() > 0) {

					Menu menu = new Menu(Display.getCurrent().getActiveShell(),
							SWT.POP_UP);
					MenuItem mitemAdd = new MenuItem(menu, SWT.PUSH);
					mitemAdd.setText("Remove from list");
					mitemAdd.addListener(SWT.Selection, new Listener() {

						public void handleEvent(Event event) {

							btnRemoveSelect.notifyListeners(SWT.Selection,
									new Event());
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
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("setInfo");
				// KE: This is not used
				// f_fabio = (FabioFile) (e.widget).getData();// current
				// selected
				// fabio file
				fireCurrentFileHasChanged();

			}
		});

		initColumn();

		descriptionColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		/*
		 * BUTTON
		 * ----------------------------------------------------------------
		 * ----------
		 */
		// --------------------------------1.3.0--------------------------
		btnRemoveSample = new Button(grpListe, SWT.PUSH);
		GridData gdBtn_s = new GridData(SWT.BEGINNING, SWT.None, true, false);
		gdBtn_s.horizontalSpan = 3;
		btnRemoveSample.setLayoutData(gdBtn_s);
		btnRemoveSample.setImage(imgDelete);
		btnRemoveSample.setText("Remove sample");
		btnRemoveSample.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgDelete != null) {
					imgDelete.dispose();
				}

			}
		});
		// remove selected sample in the list
		btnRemoveSample.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedSample(sample);
				if (sampleTable.getTable().getItemCount() > 0) {
					sampleTable.getTable().setSelection(
							sampleTable.getTable().getItem(currSelection));
					sampleTable.getTable().notifyListeners(SWT.Selection,
							new Event());

				} else {
					clearFiles();
				}

			}
		});

		//
		btnRemoveSelect = new Button(grpListe, SWT.PUSH);
		GridData gdBtn = new GridData(SWT.END, SWT.None, true, false);
		gdBtn.horizontalSpan = 1;
		btnRemoveSelect.setLayoutData(gdBtn);
		btnRemoveSelect.setImage(imgDelete);
		btnRemoveSelect.setText("Remove files");
		btnRemoveSelect.setToolTipText("Remove selected files");
		btnRemoveSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedFiles();
				sampleTable.getTable().setSelection(
						sampleTable.getTable().getItem(currSelection));
				sampleTable.getTable().notifyListeners(SWT.Selection,
						new Event());

			}
		});
		btnRemoveSelect.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgDelete != null) {
					imgDelete.dispose();
				}

			}
		});
		sash.setWeights(new int[] { 20, 50 });

	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @description remove selected column except the two firsts (#, Name)
	 */
	private void removeColumn() {
		if (columnSelected != null
				&& (columnIndexSelected > 1 && columnIndexSelected < table
						.getColumnCount())) {
			columnSelected.dispose();
			// titles.remove(columnIndexSelected);
			titles.removeElementAt(columnIndexSelected);

			for (int i = 0; i < titles.size(); i++) {
				table.getColumn(i).pack();

			}
		}

	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @description remove selected column except the two firsts (#, Name)
	 */
	private void removeAllColumnsAdded() {
		int j = 2;
		while (table.getColumnCount() > 2) {
			table.getColumn(j).dispose();
			titles.removeElementAt(j);
		}

		for (int i = 0; i < titles.size(); i++) {
			table.getColumn(i).pack();

		}

	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @throws JepException
	 * @description add a 'key' column (key selected in the combo) in the table.
	 */

	private void addColumn() throws JepException {

		int index = listKeys.getSelectionIndex();
		if (index == -1) {
			index = 0;
		}
		String newHeader = (listKeys.getItem(index));
		titles.add(newHeader);
		TableColumn column = new TableColumn(table, table.getStyle());
		column.setText(titles.lastElement());
		column.addListener(SWT.Selection, sortListener);

		TableItem item;
		int col = titles.size() - 1;
		for (int i = 0; i < sample.getFiles().length; i++) {
			if (table.getItemCount() > i + 1) {
				item = table.getItem(i + 1);
			} else {
				item = new TableItem(table, SWT.NONE);
			}

			try {
				item.setText(col, sample.getFabioFiles().elementAt(i).getValue(
						titles.lastElement()));

			} catch (FabioFileException e) {
				System.out.println("Error while populate table ");
			}
		}

		populateFileTable("Files selected", sample.getFiles());
		for (int i = 0; i < titles.size(); i++) {
			table.getColumn(i).pack();

		}

	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 */
	private void initColumn() {

		for (int i = 0; i < titles.size(); i++) {
			int style = SWT.None;

			TableColumn column = new TableColumn(table, style);
			column.setText(titles.elementAt(i));
			column.addListener(SWT.Selection, sortListener);

		}
		// TableItem item = new TableItem(table, SWT.NONE);
		clearFiles();
		for (int i = 0; i < titles.size(); i++) {

			table.getColumn(i).pack();

		}

		table.setSortColumn(table.getColumn(1));
		table.setSortDirection(SWT.DOWN);
	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @description fill list with header keys
	 */
	private void populateCombo() {
		listKeys.removeAll();
		if (sample.hasFile()) {
			listKeys.setEnabled(true);
			addKeyInTab.setEnabled(true);
			// get keys with the first file...
			String[] list;
			try {

				list = sample.getFabioFiles().elementAt(0).getKeys();

				fable.framework.toolbox.ToolBox.quicksort(list, 0, list.length);
				listKeys.setData("HEADER_KEYS", list);
				for (int i = 0; i < list.length; i++) {
					listKeys.add(list[i]);
				}
				listKeys.select(0);
			} catch (FabioFileException e) {

				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			//

		} else {
			listKeys.setEnabled(false);
			addKeyInTab.setEnabled(false);
		}

	}

	/**
	 * return default processing view
	 * 
	 * @return default processing view
	 */
	public static FableSampleLoaderView getDefault() {
		return view;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 *@description populate sample tree with selected samples
	 */
	private void populateTreeSample() {

		// Experiment ex = new Experiment();
		sampleTable.setInput(new Experiment(listOfSamples));

	}

	/**
	 * update the list of selected files to display
	 * 
	 * @param String
	 *            [] newFiles - list of selected files
	 */
	public void setSelectedFiles(String[] newFiles) {

		descriptionColor = light_Green;
		if (newFiles != null && newFiles.length > 0) {
			populateFileTable("files selected", newFiles);
		}
	}

	/**
	 * update the list of processed files to display
	 * 
	 * @param String
	 *            [] newFiles - list of selected files
	 */
	public void setProcessingFiles(String[] newFiles) {
		descriptionColor = light_Red;
		// Add Gaelle 12-09-2007
		if (newFiles != null) {

			populateFileTable("files being processed", newFiles);
		}
	}

	/**
	 * 
	 * 19 sept. 07
	 * 
	 * @author G. Suchet
	 * @param listener
	 * @description store property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		array.add(listener);
	}

	/**
	 * 
	 * 19 sept. 07
	 * 
	 * @author G. Suchet
	 * @param listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		array.remove(listener);
	}

	/**
	 * 
	 * 19 sept. 07
	 * 
	 * @author G. Suchet
	 * @description send message to view listening to changes ; for instance,
	 *              spdView
	 */
	public void fireExperimentInfoChange() {
		stextExperiment.setText(experimentName);
		stextSample.setText(sampleName);
		for (Iterator<IPropertyChangeListener> it = array.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"ProcessingView_selectedFiles", null, selectedFiles));
				element.propertyChange(new PropertyChangeEvent(this,
						"ProcessingView_directory", null, stextSampleDirectory
								.getText()));
				element.propertyChange(new PropertyChangeEvent(this,
						"ProcessingView_sample", null, sampleName));
				element.propertyChange(new PropertyChangeEvent(this,
						"ProcessingView_experiment", null, experimentName));
			}
		}

	}

	public void fireSelectedFilesHasChange() {
		for (Iterator<IPropertyChangeListener> it = array.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element.propertyChange(new PropertyChangeEvent(this,
						"ProcessingView_selectedFiles", null, selectedFiles));
			}
		}
	}

	private void fireCurrentFileHasChanged() {
		for (Iterator<IPropertyChangeListener> it = array.iterator(); it
				.hasNext();) {
			IPropertyChangeListener element = (IPropertyChangeListener) it
					.next();
			if (element != null) {
				element
						.propertyChange(new PropertyChangeEvent(this,
								"ProcessingView_currentFabioFile", null,
								selectedFiles));
			}
		}
	}

	/**
	 * @description update the list of files in the processing table
	 * 
	 * @param String
	 *            [] newFiles - list of files to display
	 */
	private void populateFileTable(String description, String[] newFiles) {

		fileDescription = description;
		files = newFiles;

		Display.getDefault().syncExec(new Runnable() {

			public void run() {

				TableItem item;
				// GS table.clearAll();
				table.removeAll();
				if (table.getItemCount() > 0) {
					item = table.getItem(0);
				} else {
					item = new TableItem(table, SWT.NONE);
				}
				item.setText(1, fileDescription);
				item.setBackground(descriptionColor);
				if (files != null) {
					item.setText(0, Integer.toString(files.length));

					for (int i = 0; i < files.length; i++) {
						if (table.getItemCount() > i + 1) {
							item = table.getItem(i + 1);
						} else {
							item = new TableItem(table, SWT.NONE);
						}
						item.setText(0, Integer.toString(i + 1));

						String[] split = files[i].split("/");
						String myname = files[i];
						if (split.length > 1) {
							myname = split[split.length - 1];
						}
						item.setText(1, myname);
						try {
							FabioFile fabio = sample.getFabioFiles().elementAt(
									i);

							item.setData(fabio);
							for (int j = 2; j < titles.size(); j++) {

								try {
									item.setText(j, fabio.getValue(titles
											.elementAt(j)));

								} catch (FabioFileException e) {
									item.setText(j, "NA");
									System.out
											.println("Error while populate table");
								}
							}

							item.setBackground((i % 2 == 0) ? Display
									.getCurrent().getSystemColor(
											SWT.COLOR_WHITE) : light_blue);
						} catch (IndexOutOfBoundsException ie) {
							System.out.println("Error while populate table ");
						}

					}

				}
				for (int i = 0; i < table.getColumnCount(); i++) {
					table.getColumn(i).pack();
				}

			}

		});
	}

	/**
	 * set the table description with color
	 * 
	 * @param int number - number of files to process
	 * @param String
	 *            description - description to display in the first line of the
	 *            table
	 * @param int bkgColor - background color of description (can be used to
	 *        display state) e.g. SWT.COLOR_RED
	 */
	public void setDescription(int number, String description,
			final int backgroundColor) {
		if (backgroundColor == SWT.COLOR_RED) {
			descriptionColor = light_Red;
		} else if (backgroundColor == SWT.COLOR_GREEN) {
			descriptionColor = light_Green;
		} else {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					descriptionColor = Display.getCurrent().getSystemColor(
							backgroundColor);
				}
			});
		}
		setDescription(number, description);
	}

	/**
	 * set the table description
	 * 
	 * @param int number - number of files to process
	 * @param String
	 *            description - description to display in the first line of the
	 *            table
	 */
	public void setDescription(int number, String description) {
		numberFiles = number;
		fileDescription = description;
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				TableItem item;
				if (table.getItemCount() > 0) {
					item = table.getItem(0);
				} else {
					item = new TableItem(table, SWT.NONE);
				}
				item.setText(0, Integer.toString(numberFiles));
				item.setText(1, fileDescription);
				item.setBackground(descriptionColor);
			}

		});

	}

	/**
	 * clear the table of files to process
	 * 
	 */
	public void clearFiles() {
		fileDescription = "please select file(s) or start online mode";
		files = null;

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				descriptionColor = Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE);
				TableItem item;
				table.removeAll();

				if (table.getItemCount() > 0) {
					item = table.getItem(0);
				} else {
					item = new TableItem(table, SWT.NONE);
				}
				item.setText(0, "0");
				item.setText(1, fileDescription);
				// resetExperimentInfos();
			}

		});

	}

	/**
	 * 
	 */
	private void resetExperimentInfos() {
		// stextSampleDirectory.set_Text("");
		sample = null;
		stextSample.setText("");
		stextExperiment.setText("");

		resetCombo();
		removeAllColumnsAdded();

	}

	/**
	 * 
	 */
	private void resetCombo() {
		listKeys.removeAll();

	}

	private void removeSelectedSample(Sample s) {
		currSelection = listOfSamples.indexOf(s);
		listOfSamples.remove(s);

		populateTreeSample();
	}

	/**
	 * @Description clear selected files to process. Called when remove button
	 *              is pushed Off line mode
	 * 
	 */
	private void removeSelectedFiles() {
		fileDescription = "selected files";
		files = null;

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				descriptionColor = Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE);

				int[] selectionIndices = table.getSelectionIndices();
				Vector<String> newSelection = new Vector<String>();
				for (int i = 1; i < table.getItemCount(); i++) {
					boolean bFound = false;
					for (int j = 0; !bFound && j < selectionIndices.length; j++) {
						if (selectionIndices[j] != 0) {
							// Search for the index in the table of the selected
							// item
							if (selectionIndices[j] == i) {
								bFound = true;
								FabioFile fabio = (FabioFile) table.getItem(i)
										.getData();
								sample.removeFabioFile(fabio);

							}

						}

					}
					if (!bFound) {
						newSelection.add((table.getItem(i)).getText(1));
					}
				}
				if (newSelection != null && newSelection.size() >= 0) {
					selectedFiles = new String[newSelection.size()];
					for (int i = 0; i < newSelection.size(); i++) {
						selectedFiles[i] = (String) newSelection.elementAt(i);

					}

					sample.setFiles(selectedFiles); // set current files to
					// sample
				}
				if (selectedFiles.length == 0) {
					clearFiles();
					resetExperimentInfos();
					removeSelectedSample(sample);

				} else {
					currSelection = listOfSamples.indexOf(sample);
				}

				setSelectedFiles(selectedFiles);

				table.deselectAll();

			}

		});

	}

	/**
	 * @description clear all informations when user switch online-offline mode
	 */
	private void clearAll() {
		if (listOfSamples != null) {
			listOfSamples.removeAllElements();
		}
		sampleTable.getTable().removeAll();
		sampleTable.getTable().clearAll();
		table.removeAll();
		table.clearAll();
		resetExperimentInfos();

	}

	/*-----------------------------------------------------------------------------------------------------------*/
	public void enableGroupExperiment(final boolean bEnable) {
		// bEnable if preprocessor is Off line

		if (!display.isDisposed()) {
			display.syncExec(new Runnable() {
				public void run() {
					if (bEnable) {
						stextSampleDirectory.setBackground(display
								.getSystemColor(SWT.COLOR_WHITE));
					} else {
						stextSampleDirectory.setBackground(display
								.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
					}
					stextSampleDirectory.setEnabled(bEnable);
					selectFilesButton.setEnabled(bEnable);
					btnRemoveSelect.setEnabled(bEnable);
					btnRemoveSample.setEnabled(bEnable);
					// since 1.3.0
					// grp_selectloader.setEnabled(bEnable);
				}
			});
		}
	}

	/**
	 * /** To show required fields when a property change in another view (for
	 * instance, launched when preprocessor is launched without any files)
	 * 
	 * @param event
	 */
	public void propertyChange(PropertyChangeEvent event) {

		if (event.getProperty().equals("SpdView_showRequired")) {

			stextSampleDirectory.set_isRequiredField(true);
		}

	}

	/************************************** CLASS SAMPLE LISTENER *******************************************************************************/
	class SampleListener implements ISampleListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * fable.preprocessor.process.ISampleListener#newImages(fable.preprocessor
		 * .process.SampleEvent)
		 */
		public void newImages(SampleEvent se) {
			setProcessingFiles(((Sample) se.getSource()).getFiles());

		}

		public void newSample(SampleEvent se) {
			// TODO Auto-generated method stub

		}

		public void sampleHasChanged(SampleEvent se) {
			// TODO Auto-generated method stub

		}

	}

	/*************************************** 1.3.0 *********************************************************************************/
	/**
	 * 
	 * 23 oct. 07
	 * 
	 * @author G. Suchet
	 * @param dir
	 * @description load entire sample directory. Files loaded in the directory
	 *              have sample_name (getFilesByEntireRegex)
	 */
	public String[] loadSample(String dir) {
		String[] rFile = null;
		if (dir != null && !dir.equals("")) {
			String[] split = dir.split("[\\\\/]");
			if (split.length > 1) {
				experimentName = split[split.length - 2];
				sampleName = split[split.length - 1];
			} else {
				sampleName = split[split.length - 1];
				experimentName = "NA";
			}

			String ProcessedFileRegex = sampleName + "(\\d+)?\\.edf" + "|"
					+ sampleName + "\\.(\\d+)?"; // edf or bruker files

			rFile = fable.framework.toolbox.ToolBox.getFileNamesByEntireRegex(
					dir, ProcessedFileRegex, true);
			String sep = System.getProperty("file.separator");
			for (int i = 0; i < rFile.length; i++) {
				rFile[i] = dir + sep + rFile[i];
			}
		}
		return rFile;

	}

	private void setExperimentAndSampleName(String directory) {
		String[] split = directory.split("[\\\\/]");
		int len = split.length;
		if (split != null) {
			switch (len) {
			case 0:
				experimentName = directory;
				sampleName = directory;
				break;
			case 1:
				experimentName = split[0];
				sampleName = split[0];
				break;
			default:
				experimentName = split[len - 2];
				sampleName = split[len - 1];
				break;

			}

		} else {
			experimentName = directory;
			sampleName = directory;
		}

	}

	/**
	 * @name loadSamples
	 * @param file
	 *            and/Or Directory selected by user in FileAndDirectoryDialog.
	 *            path + file name/dir name
	 * 
	 */
	private void loadSamples(String[] fileOrDirectory) {

		Sample singleSample = null; // for selected files, we need to create a
		// sample
		int fileSize = 0; // the number of files selected
		Vector<String> listFiles = null; // the list of files selected
		String sep = System.getProperty("file.separator");
		if (listOfSamples == null) {
			listOfSamples = new Vector<Sample>();
		} else {
			listOfSamples.removeAllElements();
		}
		for (int i = 0; i < fileOrDirectory.length; i++) {
			// instantiate a new list of Sample

			// Get Sample Name
			// int lastIndexOfSep = fileOrDirectory[i].lastIndexOf(sep);
			// int len = fileOrDirectory[i].length();

			// String name = fileOrDirectory[i].substring(lastIndexOfSep + 1,
			// len);
			// Trace-----------------------------------------------------

			// -----------------------------------------------------

			// Check if the selected file is a file or a directory
			if (ToolBox.checkIfIsDirectory(fileOrDirectory[i])) {
				// create a new Sample and load all files
				Sample spl;
				setExperimentAndSampleName(fileOrDirectory[i]);
				try {
					spl = new Sample(experimentName, sampleName,
							fileOrDirectory[i], loadSample(fileOrDirectory[i]));
					// spl.set_files(loadSample(fileOrDirectory[i]));//Set
					// current files to sample without loading fabio Files
					// (which is not usefull)
					listOfSamples.add(spl);
					System.out.println("Create a new Sample for "
							+ fileOrDirectory[i]);

				} catch (FabioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				// if this is a file, add it to current sample
				if (singleSample == null) {
					int lastdirIndex = fileOrDirectory[i].lastIndexOf(sep);
					setExperimentAndSampleName(fileOrDirectory[i].substring(0,
							lastdirIndex));
					// name = fileOrDirectory[i].substring(lastdirIndex + 1,
					// fileOrDirectory[i].length());
					singleSample = new Sample(experimentName, sampleName,
							fileOrDirectory[i].substring(0, lastdirIndex));
					listFiles = new Vector<String>();
					listOfSamples.insertElementAt(singleSample, 0);
				}
				fileSize++;
				listFiles.add(fileOrDirectory[i]); // create the list of the
				// files so that they will
				// be added to the sample
			}

		}
		if (listFiles != null) {
			// create a tab of files and set them to the sample
			String[] files = new String[listFiles.size()];
			for (int j = 0; j < listFiles.size(); j++) {
				files[j] = listFiles.elementAt(j);
			}

			try {
				// GS 19/02/2008
				singleSample.addFabioFiles(files);
			} catch (FabioFileException e) {
				System.out
						.println("An error occured while loading files for fabio for this sample "
								+ singleSample.getDirectoryName());
				// console.displayIn("An error occured while loading files for fabio for this sample "
				// + singleSample.get_name());
			}

		}
	}

	public void buildSampleName(String filePath) {
		sampleName = filePath.substring(0, filePath.indexOf(".") - 4);
	}

}
