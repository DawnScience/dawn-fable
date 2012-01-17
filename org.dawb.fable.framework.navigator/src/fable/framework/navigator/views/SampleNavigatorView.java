/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;

import fable.framework.logging.FableLogger;
import fable.framework.navigator.Activator;
import fable.framework.navigator.actions.OpenFilesAction;
import fable.framework.navigator.actions.OpensampleAction;
import fable.framework.navigator.controller.SampleController;
import fable.framework.navigator.preferences.FabioPreferenceConstants;
import fable.framework.navigator.toolBox.IImagesKeys;
import fable.framework.navigator.toolBox.IVarKeys;
import fable.framework.toolbox.StringText;
import fable.framework.toolbox.ToolBox;
import fable.framework.views.FableMessageConsole;
import fable.python.Experiment;
import fable.python.Sample;
import fable.python.SampleException;
import fable.python.contentprovider.SampleTableContentProvider;
import fable.python.labelprovider.SampleTablelabelProvider;

import org.dawb.fabio.FabioFile;
import org.dawb.fabio.FabioFileException;

public class SampleNavigatorView extends ViewPart implements
		IPropertyChangeListener {

	public static final String ID = "fable.framework.navigator.views.SampleNavigatorViewid";
	public static final String INITIAL_DIR_DATA = "initialDirData";
	/** Image delete used in button remove. */
	final Image imgDelete;
	/** Image add used in button add a header key. */
	final Image imgadd;
	/** Image remove used to remove a column header key in the table. */
	final Image imgRemove;
	/** Image update to update files in the current directory. */
	final Image imgUpdate;
	/** Image apply for button apply filter. */
	final Image imgApply;
	/** Image clear for button clear filter. */
	final Image imgClear;
	/** Image open files for the view action. */
	final Image imgOpenFiles;
	/** Image open directory for menu open directory. */
	final Image imgOpenDirectory;
	/** Color blue for file table <code>fileTable</code> */
	final Color light_blue = new Color(Display.getCurrent(), 228, 247, 248);

	/** Uniq instance of this view <code>SampleNavigatorView</code>. */
	public static SampleNavigatorView view;
	/** This singleton is the model controller. */
	public static SampleController controller = SampleController
			.getController();

	/** Image descriptor for delete. */
	private ImageDescriptor GifDelete = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.BTN_IMG_DELETE);
	/** Image descriptor for remove. */
	private ImageDescriptor GifRemove = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.BTN_IMG_SUBTRACT);
	/** Image descriptor for add. */
	private ImageDescriptor GifAdd = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.BTN_IMG_ADD);
	/** Image descriptor for update. */
	private ImageDescriptor updateImgDescriptor = Activator
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					IImagesKeys.BTN_IMG_UPDATE);
	/** Image descriptor for open files menu. */
	private ImageDescriptor imageFiles = Activator
			.getImageDescriptor("images/openFiles.gif");
	/** Image descriptor for open directory menu. */
	private ImageDescriptor imageSample = Activator
			.getImageDescriptor("images/openFolder.gif");
	/** Image descriptor for apply filter. */
	private ImageDescriptor imageApply = Activator.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, IImagesKeys.BTN_IMG_APPLY);

	/** The current display. */
	private Display display;
	/** The initial directory used in <code>memento</code>. */
	private static String initialDirData = null;
	/** this is the name of the parent directory of a selected directory. */
	String experimentName = "";
	/** This is the name of the current directory. */
	String sampleName = "";
	/** This is the directory fullpath. */
	String sSampleDirectory; // current sample directory
	/** An array list to register SampleNavigator change event listener. */
	private ArrayList<IPropertyChangeListener> array = new ArrayList<IPropertyChangeListener>();
	/**
	 * This is the current directory selected in the table
	 * <code>sampleTable</code>.
	 */
	private Sample currentSample;
	/** This is a list of keys added to the table <code>fileTable</code>. */
	private List listKeys;
	/** This is a vector of titles for table <code>fileTable</code>. */
	private Vector<String> titles = new Vector<String>();
	/** A sort listener for table <code>fileTable</code> */
	private Listener sortListener;
	/** Current selected column for files table. */
	private TableColumn columnSelected;
	/** A button to add a key in <code>fileTable</code> */
	private Button addKeyInTab;
	/** A button to remove a key in <code>fileTable</code> */
	private Button removeKeyInTable;
	/** This table list files of the current directory. */
	private Table fileTable;
	private TableViewer fileTableViewer;
	/** This button removes selected file or selected directory. */
	private Button btnRemoveSelect;
	/** This button updates files in the table for the current directory. */
	private Button btnUpdate;
	/**
	 * This button automatically updates files in the table for the current
	 * directory.
	 */
	private Button btnAutoUpdate;
	/** A table viewer for directory. On the let side of the sash. */
	private TableViewer sampleTable;
	/** The sash the */
	private SashForm sash;
	/** Index of selected column for table <code>fileTable</code> */
	private int columnIndexSelected = -1;
	/**
	 * This is a list of directories loaded in the view. This is the same
	 * instance as controller.vsample
	 */
	private Vector<Sample> listOfSamples;
	/**
	 * This is a vector of fabio files of the current directory (selected
	 * directory) sorted by name. <br>
	 * <code>sort = currentSample.getSortedFiles();</code>
	 */
	private Vector<FabioFile> sort;
	/** This is the current selected file. */
	private FabioFile f_fabio;// current selected file
	/** This job i used to add directory to this view. */
	private Job job;
	/** A logger to track problems. */
	private Logger logger;
	/**
	 * Complete regular expression based on preferences to load only files with
	 * extension that composed this expresion. <br>
	 * For example: <code>expression = "edf | bruker | edf.gz"</code>. <BR>
	 * Usage: regularExpressionForExtension =
	 * fable.framework.navigator.Activator.getDefault()
	 * .getPreferenceStore().getString( SampleNavigatorPreferences.FILE_TYPE);//
	 * Activator.getDefault().getPreferenceStore().getString(;
	 * PeaksearchPreferencesPage .FILE_INI));
	 */
	String regularExpressionForExtension;
	/** A listener on preferences property change. */
	private IPropertyChangeListener preferencesListener;
	/***/
	org.eclipse.core.runtime.IStatus job_status = Status.OK_STATUS;

	/** Open directory action for local menu. */
	private OpensampleAction actionOpensample;
	/** Open files action for local menu. */
	private OpenFilesAction actionOpenFiles;
	/** Current filter on file table. */
	private String filter = "";
	/** Text field for filter. */
	private StringText textFilter;
	/** Button to apply filter. */
	private Button btnApplyfilter;
	/** Clear button to reset filter. */
	private Button btnClearfilter;
	/**
	 * Text field, disabled to display filter message info. For example 12/15
	 * files.
	 */
	private Text labelTextFilter;
	/** A global boolean to know if filter is applied or not. */
	private boolean applyFilter = false;
	/** a global flag to indicate if automatic update has been selected or not */
	private boolean autoUpdate = false;
	private Job autoUpdateJob;
	// private Slice1DAction slice1DAction;
	/**
	 * Context menu manager for the file table.
	 */
	MenuManager contextMenuMgr;

	/**
	 * 
	 * @description constructor. Init images and sortListener to sort in table
	 *              files
	 */
	public SampleNavigatorView() {

		imgDelete = GifDelete.createImage();
		imgadd = GifAdd.createImage();
		imgRemove = GifRemove.createImage();
		imgUpdate = updateImgDescriptor.createImage();
		imgApply = imageApply.createImage();
		imgClear = GifDelete.createImage();
		imgOpenDirectory = imageSample.createImage();
		imgOpenFiles = imageFiles.createImage();

		logger = FableLogger.getLogger();
		// GS 1.3.1
		controller.addPropertyChangeListener(this);
		listOfSamples = controller.getSamples();
		//

		sortListener = new Listener() {
			// Function called to move items while sorting
			public void handleEvent(Event e) {
				TableColumn sortColumn = fileTable.getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;

				// determine new sort column and direction

				int dir = fileTable.getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					fileTable.setSortColumn(currentColumn);
					dir = SWT.DOWN;
				}

				// sort the data based on column and direction
				TableColumn[] cols = fileTable.getColumns();
				// TableItem[] items = fileTable.getItems();

				final TableColumn column = ((TableColumn) e.widget);

				columnSelected = (TableColumn) e.widget;
				//
				boolean columnIndexFound = false;
				for (int n = 0; !columnIndexFound && cols != null
						&& n < cols.length; n++) {
					if (columnSelected == cols[n]) {
						columnIndexSelected = n;
						columnIndexFound = true;
					}
				}
				// Collator collator =
				// Collator.getInstance(Locale.getDefault());

				currentSample.setComparator((String) columnSelected
						.getData("key"), dir);

				// PART FOR REMOVE COLUMN : delete the firsts two rows is not
				// allowed
				if (columnIndexSelected > 1) {
					removeKeyInTable.setEnabled(true);
				} else {
					removeKeyInTable.setEnabled(false);
				}
				final int dirForThread = dir;
				new Thread(new Runnable() {
					public void run() {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								sort = currentSample.getSortedFiles();
								populateFileTable();
								// update data displayed in table
								fileTable.setSortDirection(dirForThread);
								fileTable.setSortColumn(column);
								// Color rows
								for (int n = 0; n < fileTable.getItemCount(); n++) {
									fileTable.getItem(n).setBackground(
											(n % 2 == 0) ? Display.getCurrent()
													.getSystemColor(
															SWT.COLOR_WHITE)
													: light_blue);
								}
							}
						});
					}
				});
			}
		};

		preferencesListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						FabioPreferenceConstants.FILE_TYPE)) {
					regularExpressionForExtension = Activator.getDefault()
							.getPreferenceStore().getString(
									FabioPreferenceConstants.FILE_TYPE);
				} else if (event.getProperty().equals(
						FabioPreferenceConstants.STEM_NAME)) {
					filter = Activator.getDefault().getPreferenceStore()
							.getString(FabioPreferenceConstants.STEM_NAME);
					textFilter.set_Text(filter);
					setFilter();
				}
			}
		};
	}

	/**
	 * Init attributes.
	 * 
	 * @param parent
	 */
	private void initValues(Composite parent) {
		display = Display.getCurrent();
		view = this;
		// view.setContentDescription("Load images for your data analysis here");
		// view.setPartName("Sample chooser");

		// init with most common standard used
		titles.add(0, "#");
		titles.add(1, "Name");
		/**
		 * Init values with
		 */
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
				preferencesListener);
		regularExpressionForExtension = Activator.getDefault()
				.getPreferenceStore().getString(
						FabioPreferenceConstants.FILE_TYPE);
		filter = Activator.getDefault().getPreferenceStore().getString(
				FabioPreferenceConstants.STEM_NAME);
	}

	@Override
	public void createPartControl(Composite parent) {
		initValues(parent);

		GridLayout gdL = new GridLayout(1, true);
		parent.setLayout(gdL);
		createContextMenu();
		createTabListe(parent);
		makeActions();
		contributeToActionBars();
	}

	/**
	 * Create the context menu.
	 */
	private void createContextMenu() {
		// Create menu manager.
		contextMenuMgr = new MenuManager();
		contextMenuMgr.setRemoveAllWhenShown(true);
		contextMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillLocalMenu(mgr);
			}
		});

		// Register menu for extension.
		// getSite().registerContextMenu(menuMgr,
		// canvasComposite.getAccessible());
	}

	/**
	 * Fill the local menus. Used for the local view menu and the context menu
	 * so they will be the same.
	 * 
	 * @param manager
	 */
	private void fillLocalMenu(IMenuManager manager) {
		manager.add(new Action("Remove Selected Files", GifDelete) {
			@Override
			public void run() {
				removeSelectedFiles();
			}

			@Override
			public void setToolTipText(String toolTipText) {
				super.setToolTipText("Remove the selected file from the table");
			}

			@Override
			public void setImageDescriptor(ImageDescriptor newImage) {
				super.setImageDescriptor(GifDelete);
			}
		});
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * 
	 * 3 oct. 07
	 * 
	 * @author G. Suchet
	 * @param parent
	 * @description create Sample table and its files table seperated by a sash
	 */
	private void createTabListe(Composite parent) {
		final int COLUMN_NUMBER = 5;
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
				SWT.V_SCROLL | SWT.H_SCROLL);
		Composite grpListe = new Composite(scrolledComposite, SWT.None);
		scrolledComposite.setContent(grpListe);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		grpListe.setLayout(new GridLayout(COLUMN_NUMBER, false));
		grpListe.setLayoutData(new GridData());
		grpListe.pack();

		Composite container = new Composite(grpListe, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		((GridData) container.getLayoutData()).horizontalSpan = COLUMN_NUMBER;

		Label lblKey = new Label(container, SWT.NONE);
		lblKey.setText("Header Key");
		listKeys = new List(container, SWT.BORDER | SWT.V_SCROLL);
		GridData gdlist = new GridData(SWT.FILL, GridData.CENTER, true, false);
		gdlist.verticalSpan = 1;
		gdlist.horizontalSpan = 1;
		int listHeigth = listKeys.getItemHeight() * 2;

		Rectangle trim = listKeys.computeTrim(0, 0, 0, listHeigth);
		gdlist.heightHint = trim.height;

		listKeys.setLayoutData(gdlist);
		listKeys.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				addColumn();
			}
		});
		listKeys.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					addColumn();
				}
			}
		});
		addKeyInTab = new Button(container, SWT.PUSH);
		addKeyInTab.setImage(imgadd);
		// addKeyInTab.setLayoutData(new GridData( SWT.FILL, GridData.CENTER,
		// false, false));
		addKeyInTab.setToolTipText("Add selected key in table");
		addKeyInTab.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				addColumn();
			}
		});

		addKeyInTab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imgadd != null) {
					imgadd.dispose();
				}
			}
		});
		removeKeyInTable = new Button(container, SWT.PUSH);
		removeKeyInTable.setImage(imgRemove);
		// removeKeyInTable.setLayoutData(new GridData( SWT.FILL,
		// GridData.CENTER, false, false));
		removeKeyInTable.setToolTipText("Remove selected column");
		removeKeyInTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				removeColumn();
			}

		});
		removeKeyInTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgRemove != null && !imgRemove.isDisposed()) {
					imgRemove.dispose();
				}

			}

		});
		// Init : remove key is disabled because no column has been selected yet
		removeKeyInTable.setEnabled(false);
		//

		// 07/22/2008 : add a textfilter
		GridData gdForFiltering = new GridData(SWT.FILL, GridData.CENTER, true,
				false);
		gdForFiltering.horizontalSpan = 1;
		textFilter = new StringText(grpListe, SWT.NONE, "Filter");
		textFilter.getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					String s = ((Text) e.widget).getText();
					filter = s;
					setFilter();
				}
			}
		});

		textFilter.setLayoutData(gdForFiltering);

		textFilter.set_Text(filter);
		btnApplyfilter = new Button(grpListe, SWT.PUSH);

		btnApplyfilter.setImage(imgApply);
		btnApplyfilter.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imgApply != null && !imgApply.isDisposed()) {
					imgApply.dispose();
				}
			}
		});
		btnApplyfilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filter = textFilter.getText();
				setFilter();
			}
		});
		GridData gdforApplyFilter = new GridData(SWT.FILL, SWT.CENTER, false,
				false);
		btnApplyfilter.setLayoutData(gdforApplyFilter);
		btnClearfilter = new Button(grpListe, SWT.PUSH);
		btnClearfilter.setImage(imgClear);
		btnClearfilter.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imgClear != null && !imgClear.isDisposed()) {
					imgClear.dispose();
				}
			}
		});

		btnClearfilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filter = "";
				textFilter.set_Text("");
				setFilter();
			}
		});

		GridData gdforClearFilter = new GridData(SWT.FILL, SWT.CENTER, false,
				false);
		btnClearfilter.setLayoutData(gdforClearFilter);

		labelTextFilter = new Text(grpListe, SWT.NO_BACKGROUND | SWT.READ_ONLY
				| SWT.NO_FOCUS);
		labelTextFilter.setEditable(false);
		labelTextFilter.setBackground(Display.getDefault().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		labelTextFilter.setForeground(Display.getDefault().getSystemColor(
				SWT.COLOR_DARK_YELLOW));
		GridData gdForFilteringTextlabel = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		labelTextFilter.setLayoutData(gdForFilteringTextlabel);

		/*
		 * btnApplyForAll = new Button(grpListe, SWT.CHECK);
		 * btnApplyForAll.setText("All");
		 * btnApplyForAll.setToolTipText("Apply filter for all directories");
		 * btnApplyForAll.addSelectionListener(new SelectionAdapter(){
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) {
		 * applyFilter=((Button)e.widget).getSelection();
		 * filter=textFilter.getText();
		 * 
		 * setFilter();
		 * 
		 * } }); applyFilter=btnApplyForAll.getSelection(); GridData
		 * gdforApplyck = new GridData( SWT.FILL, SWT.CENTER, false, false);
		 * btnApplyForAll.setLayoutData(gdforApplyck);
		 * 
		 * if(btnApplyForAll.getSelection()){
		 * btnClearfilter.setToolTipText("Clear filter for all directories"); }
		 * else{ String name=""; if(currentSample != null){ name =
		 * currentSample.get_name();
		 * btnClearfilter.setToolTipText("Clear filter for selected directory: "
		 * + name);
		 * 
		 * } }
		 */

		sash = new SashForm(grpListe, SWT.HORIZONTAL);

		GridData gdExplorer = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		gdExplorer.horizontalAlignment = GridData.FILL;
		gdExplorer.verticalAlignment = GridData.FILL;
		gdExplorer.horizontalSpan = COLUMN_NUMBER;
		sash.setLayoutData(gdExplorer);
		GridData gdTree = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTree.horizontalSpan = 1;

		sampleTable = new TableViewer(sash, SWT.BORDER);
		sampleTable.setContentProvider(new SampleTableContentProvider());
		sampleTable.setLabelProvider(new SampleTablelabelProvider());
		sampleTable.getTable().setHeaderVisible(true);
		sampleTable.getTable().setLinesVisible(true);
		// populate table files with sample files
		sampleTable.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					Menu menu = new Menu(Display.getCurrent().getActiveShell(),
							SWT.POP_UP);
					MenuItem mitemAdd = new MenuItem(menu, SWT.PUSH);
					final Image img = GifDelete.createImage();
					mitemAdd.setImage(img);
					mitemAdd.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							if (img != null) {
								img.dispose();
							}
						}
					});
					mitemAdd.setText("Remove from list");
					mitemAdd.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							removeSelectedSample(currentSample);
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
		});

		sampleTable.getTable().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] ti = ((Table) e.widget).getSelection();

				if (ti != null && ti.length > 0) {

					currentSample = (Sample) ti[0].getData();

					btnUpdate.setEnabled(currentSample.isDirectory());

					setCurrentSample();

				} else {
					currentSample = null;
				}
			}
		});

		createFileTable();
		Composite tablebtnCont = new Composite(grpListe, SWT.NONE);
		tablebtnCont.setLayout(new GridLayout(3, false));
		tablebtnCont.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		((GridData) tablebtnCont.getLayoutData()).horizontalSpan = COLUMN_NUMBER;
		/*
		 * do we need these buttons here - they are already actions of the view
		 * bar ?
		 */
		/*
		 * btnOpenfiles = new Button(tablebtnCont, SWT.PUSH );
		 * btnOpenfiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
		 * false,1, 1)); btnOpenfiles.setImage(imgOpenFiles);
		 * btnOpenfiles.setText("Open files");
		 * btnOpenfiles.addDisposeListener(new DisposeListener(){ public void
		 * widgetDisposed(DisposeEvent e) { if(imgOpenFiles!= null &&
		 * !imgOpenFiles.isDisposed()){ imgOpenFiles.dispose(); }
		 * 
		 * }}); btnOpenfiles.setToolTipText("Open one or more files");
		 * btnOpenfiles.addSelectionListener(new SelectionAdapter( ){
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) { new
		 * OpenFilesAction().run(); }
		 * 
		 * } ); btnOpendirectory = new Button(tablebtnCont, SWT.PUSH);
		 * btnOpendirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
		 * false, false,1, 1));
		 * 
		 * btnOpendirectory.setImage(imgOpenDirectory);
		 * btnOpendirectory.setText("Open directory");
		 * btnOpendirectory.setToolTipText("Open a directory");
		 * btnOpendirectory.addDisposeListener(new DisposeListener(){
		 * 
		 * public void widgetDisposed(DisposeEvent e) { if(imgOpenDirectory !=
		 * null && !imgOpenDirectory.isDisposed()){ imgOpenDirectory.dispose();
		 * }
		 * 
		 * }
		 * 
		 * }); btnOpendirectory.addSelectionListener(new SelectionAdapter(){
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) { new
		 * OpensampleAction().run(); } });
		 */

		btnRemoveSelect = new Button(tablebtnCont, SWT.PUSH);
		btnRemoveSelect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		btnRemoveSelect.setImage(imgDelete);
		btnRemoveSelect.setText("Remove");
		btnRemoveSelect.setToolTipText("Remove selection from the table");
		btnRemoveSelect.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				if (sampleTable.getTable().getSelectionCount() > 0) {

					removeSelectedSample(currentSample);
				} else if (fileTable.getSelectionCount() > 0) {

					removeSelectedFiles();

				}
			}
		});
		btnRemoveSelect.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imgDelete != null && !imgDelete.isDisposed()) {
					imgDelete.dispose();
				}

			}
		});

		// Add update button
		btnUpdate = new Button(tablebtnCont, SWT.PUSH);
		btnUpdate
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		btnUpdate.setImage(imgUpdate);
		btnUpdate.setText("Refresh");
		btnUpdate.setToolTipText("Refresh files for directory");
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentSample != null) {
					updateDirectory();
				}
			}
		});
		btnUpdate.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (imgUpdate != null && !imgUpdate.isDisposed()) {
					imgUpdate.dispose();
				}
			}
		});

		btnAutoUpdate = new Button(tablebtnCont, SWT.CHECK);
		btnAutoUpdate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		btnAutoUpdate.setText("Auto Refresh");
		btnAutoUpdate
				.setToolTipText("Automatically refresh files for directory");
		btnAutoUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				autoUpdate = !autoUpdate;
				if (autoUpdate) {
					autoUpdateJobStart();
				}
			}
		});
		sash.setWeights(new int[] { 20, 50 });

		scrolledComposite.setMinHeight(Display.getDefault().getPrimaryMonitor()
				.getBounds().height);
		scrolledComposite.setMinWidth(Display.getDefault().getPrimaryMonitor()
				.getBounds().width);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinSize(grpListe.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		// Init filter
		clearFilter();
	}

	/**
	 * This function is called when a file is selected in a table or when
	 * selection is set for fileTable (after removing files for example);
	 */
	private void fileSelecionInTable() {
		int index = fileTable.getSelectionIndex();
		if (index >= 0) {
			TableItem item = fileTable.getItem(index);
			f_fabio = (FabioFile) item.getData();// current selected fabio file

			if (currentSample != null && currentSample.hasFile()) {
				int currentIndex = currentSample.getFilteredfiles().indexOf(
						f_fabio);
				if (currentIndex >= 0) {
					controller.setCurrentFileIndex(currentIndex);
					// New request from John 02/07/2008 : load keys for selected
					// files
					f_fabio = currentSample.getCurrentFabioFile();
					populateCombo();
				}
				// Unselect sample from sample table since we have only
				// one button to remove files or sample
				sampleTable.getTable().setSelection(-1);
			}
		}
	}

	/**
	 * Add file table on the right side of the sample table
	 */
	private void createFileTable() {
		fileTable = new Table(sash, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION
				| SWT.VIRTUAL);
		fileTable.setLinesVisible(true);
		fileTable.setHeaderVisible(true);
		fileTableViewer = new TableViewer(fileTable, SWT.BORDER);

		// Create menu.
		Menu menu = contextMenuMgr.createContextMenu(fileTable.getAccessible()
				.getControl());
		fileTable.getAccessible().getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenuMgr, fileTableViewer);

		createDragSource();
		fileTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int i = event.index;

				try {
					FabioFile fabio = ((FabioFile) sort.elementAt(i));
					// To avoid reading header while loading
					item.setText(0, "" + i);
					item.setText(1, fabio.getFileName());
					item.setData(fabio);
					for (int j = 2; j < titles.size(); j++) {
						try {
							String txt = fabio.getValue(titles.elementAt(j));
							item.setText(j, txt);

						} catch (FabioFileException e) {
							item.setText(j, "NA");
							logger.error("Error while adding files in table."
									+ "" + e.getMessage());
						}
					}
					item.setBackground((i % 2 == 0) ? Display.getCurrent()
							.getSystemColor(SWT.COLOR_WHITE) : light_blue);
				} catch (IndexOutOfBoundsException ie) {
					logger.error("Error while adding files in table."
							+ ie.getMessage());
				}
			}
		});
		fileTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileSelecionInTable();
			}
		});
		initColumn();
	}

	/**
	 * Create a drag source for the file table. The data will be a FileTransfer
	 * with a String[] of the full names of the selected files. Note that when
	 * dragging to the Image Navigator you will be making a selection, that will
	 * itself cause the ImageView to update. The drag will actually cause this
	 * to happen twice, and so there isn't any utility in dragging to the Image
	 * Viewer, but it is an intuitive thing to do. Dragging elsewhere should
	 * work as normal.
	 */
	private void createDragSource() {
		final DragSource source = new DragSource(fileTable, DND.DROP_COPY);
		source.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		final DragSourceListener dragListener = new DragSourceListener() {
			public void dragFinished(DragSourceEvent event) {

			}

			public void dragSetData(DragSourceEvent event) {
				// Get the selected file names and return that array as the data
				TableItem[] items = fileTable.getSelection();
				if (items == null || items.length == 0) {
					return;
				}
				int nItems = items.length;
				String[] fileNames = new String[nItems];
				TableItem item = null;
				FabioFile fabioFile = null;
				for (int i = 0; i < nItems; i++) {
					item = items[i];
					fabioFile = (FabioFile) item.getData();
					if (fabioFile != null
							&& fabioFile.getFullFilename() != null) {
						fileNames[i] = fabioFile.getFullFilename();
					} else {
						// Shouldn't happen
						fileNames[i] = "Image Navigator Drag Error";
					}
				}
				event.data = fileNames;
			}

			public void dragStart(DragSourceEvent event) {
				event.doit = fileTable.getSelection().length > 0;
			}
		};
		source.addDragListener(dragListener);
		source.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				source.removeDragListener(dragListener);
			}
		});
	}

	/**
	 * Get the list of selected files in the sample navigator's file table and
	 * return the indices corresponding to their position in the vector of
	 * FabioFiles.
	 * 
	 * @return - list of file indices as an array of int
	 */
	public Vector<Integer> getSelectedFilesIndex() {
		final Vector<Integer> selectedFilesIndex = new Vector<Integer>();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				TableItem[] items = fileTable.getSelection();
				for (int i = 0; i < items.length; i++) {
					selectedFilesIndex.addElement(Integer.parseInt(items[i]
							.getText()));
				}
			}
		});
		return selectedFilesIndex;
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionOpensample);
		manager.add(actionOpenFiles);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionOpensample);
		manager.add(actionOpenFiles);
	}

	private void makeActions() {
		actionOpenFiles = new OpenFilesAction();
		actionOpensample = new OpensampleAction();
		actionOpensample.setText("Open Directory");
		actionOpensample.setToolTipText("Select a directory to load.");
		actionOpensample.setImageDescriptor(imageSample);
		actionOpenFiles.setText("Open Files");
		actionOpenFiles.setToolTipText("Select image file(s) to load.");
		actionOpenFiles.setImageDescriptor(imageFiles);
		// slice1DAction = new Slice1DAction("1D Slice");
		// // slice1DAction.init(iv);
		// slice1DAction
		// .setToolTipText("Make a new 2D image by stacking the zoomed line of the selected images");
	}

	/**
	 * 
	 */
	private void setCurrentSample() {
		// Put current filterto current Sample if selected Sample has changed
		if (!controller.getSamples().contains(currentSample)) {
			controller.addSample(currentSample);
		}
		controller.setCurrentSample(currentSample);
		// Show the files of the first element
		if (sampleTable.getTable().getItemCount() > 0) {
			// Since we have only one button to remove selection
			// sampleTable.getTable().setSelection(sampleTable.getTable().getItem(i));
			if (currentSample.hasFile()) {
				sort = currentSample.getFilteredfiles();
				updateFilterInfo();
			}
			if (sort.size() > 0) {
				controller.setCurrentFileIndex(0);
				f_fabio = currentSample.getCurrentFabioFile();
			}
			populateCombo();
			populateFileTable();
		}
	}

	/**
	 * This function is called to update the number of files displayed in
	 * comparison with the number of files that have been loaded.
	 */
	private void updateFilterInfo() {
		if (currentSample != null) {

			if (currentSample.getFilter().equals("")) {
				clearFilter();
			} else {
				int total = currentSample.getFabioFiles().size();
				int n = 0;
				if (sort != null) {
					n = sort.size();
				}
				labelTextFilter.setText(n + "/" + total + " files");
				textFilter.set_Text(currentSample.getFilter());
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
	private void removeColumn() {
		if (columnSelected != null
				&& (columnIndexSelected > 1 && columnIndexSelected < fileTable
						.getColumnCount())) {
			columnSelected.dispose();
			// titles.remove(columnIndexSelected);
			titles.removeElementAt(columnIndexSelected);

			for (int i = 0; i < titles.size()
					&& !Activator.getDefault().getPreferenceStore().getBoolean(
							FabioPreferenceConstants.FIX_COLUMN_SIZE); i++) {
				fileTable.getColumn(i).pack();
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
		while (fileTable.getColumnCount() > 2) {
			fileTable.getColumn(j).dispose();
			titles.removeElementAt(j);
		}

		for (int i = 0; i < titles.size()
				&& !Activator.getDefault().getPreferenceStore().getBoolean(
						FabioPreferenceConstants.FIX_COLUMN_SIZE); i++) {
			fileTable.getColumn(i).pack();
		}
	}

	private void addColumn() {
		int index = listKeys.getSelectionIndex();
		if (index == -1) {
			index = 0;
		}
		String newHeader = (listKeys.getItem(index));
		titles.add(newHeader);

		TableColumn column = new TableColumn(fileTable, fileTable.getStyle());
		column.setText(titles.lastElement());
		column.addListener(SWT.Selection, sortListener);
		column.setData("key", newHeader);
		column.setData("index", titles.size() - 1);

		Job job_addColumn = new Job("Wait while getting " + newHeader
				+ " stored in file header") {
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("get " + titles.lastElement(), currentSample
						.getFiles().length);
				if (monitor.isCanceled()) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				display.syncExec(new Runnable() {

					// @Override
					public void run() {
						// int col = titles.size() - 1;
						// TableItem item;
						// if (fileTable.getItemCount() > titles.size()) {
						// item = fileTable.getItem(titles.size());
						// } else {
						// item = new TableItem(fileTable, SWT.NONE);
						// }

						setSelectedFiles(currentSample.getFiles());
					}
				});
				display.syncExec(new Runnable() {
					public void run() {
						for (int i = 0; i < titles.size()
								&& !Activator
										.getDefault()
										.getPreferenceStore()
										.getBoolean(
												FabioPreferenceConstants.FIX_COLUMN_SIZE); i++) {
							fileTable.getColumn(i).pack();
						}
					}
				});
				monitor.worked(100);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job_addColumn.setUser(true);
		job_addColumn.schedule();

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
			TableColumn column = new TableColumn(fileTable, style);
			column.setText(titles.elementAt(i));
			column.addListener(SWT.Selection, sortListener);
			column.setData("key", titles.elementAt(i));
			column.setData("index", i);
		}
		new TableItem(fileTable, SWT.NONE);
		clearFiles();
		for (int i = 0; i < titles.size(); i++) {
			// please pack the two first columns
			fileTable.getColumn(i).pack();

		}
		fileTable.setSortColumn(fileTable.getColumn(1));
		fileTable.setSortDirection(SWT.DOWN);
	}

	/**
	 * 
	 * 16 Jan. 08
	 * 
	 * @author G. Suchet
	 * @description fill list with header keys with a progress dialog
	 */
	private void populateCombo() {
		if (currentSample.getFilteredfiles().size() > 0) {
			listKeys.setEnabled(true);
			addKeyInTab.setEnabled(true);
			display.asyncExec(new Runnable() {

				public void run() {
					try {
						String[] list = controller.getKeys();
						listKeys.removeAll();
						listKeys.setData("HEADER_KEYS", list);

						for (int i = 0; i < list.length; i++) {
							listKeys.add(list[i]);
						}
						listKeys.select(0);

					} catch (SampleException e) {
						logger.error(e.getMessage());
					}
				}
			});

		} else // Sample has no files
		{
			listKeys.removeAll();
			listKeys.setEnabled(false);
			addKeyInTab.setEnabled(false);
		}
	}

	/**
	 * return default processing view
	 * 
	 * @return default processing view
	 */
	public static SampleNavigatorView getDefault() {
		return view;
	}

	@Override
	public void setFocus() {

	}

	/**
	 *@description populate sample tree with selected samples
	 */
	private void populateTreeSample() {
		sampleTable.setInput(new Experiment(listOfSamples));
		// fireSamplesChoosen();
	}

	/**
	 * update the list of selected files to display
	 * 
	 * @param String
	 *            [] newFiles - list of selected files
	 */
	public void setSelectedFiles(String[] newFiles) {

		if (newFiles != null && newFiles.length > 0) {
			populateFileTable();
		}
	}

	/**
	 * Update sample with this filter.
	 */
	private void setFilter() {
		if (currentSample != null) {
			if (applyFilter) {
				Vector<Sample> directories = controller.getSamples();
				for (int i = 0; i < directories.size(); i++) {
					directories.elementAt(i).setFilter(filter);
				}
			}
			currentSample.setFilter(filter);
			setCurrentSample();
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
	 * @description update the list of files in the processing table
	 * 
	 * @param String
	 *            [] newFiles - list of files to display
	 */
	private void populateFileTable() {
		fileTable.clearAll();
		int nbItemsForFiles = currentSample.getFilteredfiles().size();
		if (currentSample != null && currentSample.hasFile()) {
			fileTable.setItemCount(nbItemsForFiles);
			// table.setSelection(0);
		} else {
			fileTable.setItemCount(0);
		}
		for (int i = 0; i < fileTable.getColumnCount()
				&& !Activator.getDefault().getPreferenceStore().getBoolean(
						FabioPreferenceConstants.FIX_COLUMN_SIZE); i++) {
			fileTable.getColumn(i).pack();
		}
	}

	/**
	 * clear the table of files to process
	 * 
	 */
	public void clearFiles() {
		display.syncExec(new Runnable() {
			public void run() {
				TableItem item;
				fileTable.removeAll();
				if (fileTable.getItemCount() > 0) {
					item = fileTable.getItem(0);
				} else {
					item = new TableItem(fileTable, SWT.NONE);
				}
				item.setText(0, "0");
			}
		});
	}

	/**
	 * 
	 */
	private void resetExperimentInfos() {
		// stextSampleDirectory.set_Text("");
		// currentSample=null;
		// stextSample.setText("");
		// stextExperiment.setText("");
		resetCombo();
		removeAllColumnsAdded();

	}

	/**
	 * 
	 */
	private void resetCombo() {
		listKeys.removeAll();
	}

	/**
	 * 
	 * @param s
	 *            selected sample
	 */
	private void removeSelectedSample(Sample s) {
		if (s != null) {
			int i = listOfSamples.indexOf(s);//

			controller.removeSample(s);
			// int i=listOfSamples.size() > 0?listOfSamples.size()-1 :0;
			if (i >= listOfSamples.size()) {
				i = listOfSamples.size() - 1 >= 0 ? listOfSamples.size() - 1
						: 0;
			}
			if (listOfSamples != null && listOfSamples.size() > 0) {
				currentSample = listOfSamples.elementAt(i);
				sampleTable.getTable().setSelection(i);
			} else {
				currentSample = null;
				clearAll();
			}
			populateTreeSample();
			if (currentSample != null) {
				setCurrentSample();
			} else {
				clearFilter();
			}
		}
	}

	private void clearFilter() {
		labelTextFilter.setText("no filter");
		textFilter.set_Text("");
	}

	/**
	 * @Description clear selected files to process. Called when remove button
	 *              is pushed Off line mode
	 * 
	 */
	private void removeSelectedFiles() {

		/*
		 * this is not compatible with auto update therefore first switch
		 * autoUpdate off
		 */
		if (autoUpdate) {
			autoUpdateJob.cancel();
			autoUpdate = false;
			return;
		}
		display.syncExec(new Runnable() {
			public void run() {
				TableItem[] itemSelected = fileTable.getSelection();
				if (itemSelected.length > 0) {
					int firstSelection = fileTable.indexOf(itemSelected[0]);
					for (int sel = 0; sel < itemSelected.length; sel++) {
						currentSample
								.removeFabioFile((FabioFile) itemSelected[sel]
										.getData());
					}
					// update sort
					sort = currentSample.getFilteredfiles();
					// update filter label text
					labelTextFilter.setText("");
					populateFileTable();
					int nItems = fileTable.getItemCount();
					if (nItems > firstSelection) {
						fileTable.setSelection(firstSelection);
						fileSelecionInTable();
					} else if (nItems > 0) {
						fileTable.setSelection(nItems - 1);
						fileSelecionInTable();
					}
					if (currentSample.getFabioFiles().size() == 0) {
						clearFiles();
						removeSelectedSample(currentSample);
						resetExperimentInfos();
					}
					updateFilterInfo();
				}
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
		fileTable.removeAll();
		fileTable.clearAll();
		resetExperimentInfos();
	}

	/**
	 * 
	 * 23 oct. 07
	 * 
	 * @author G. Suchet
	 * @param dir
	 * @description load entire sample directory. Files loaded in the directory
	 *              have sample_name (getFilesByEntireRegex)
	 */
	public File[] loadSample(String dir) {
		File[] rFile = null;

		if (dir != null && !dir.equals("")) {
			/*
			 * String[] split=dir.split("[\\\\/]"); if(split.length >1){
			 * experimentName=split[split.length-2];
			 * sampleName=split[split.length-1]; }else if(split.length==1){
			 * sampleName=split[split.length-1]; experimentName="NA"; }
			 */
			initDirectoryName(dir);
			rFile = getFiles(dir);

		}
		return rFile;
	}

	/**
	 * This function get the name of the directory and its parent.
	 */
	private void initDirectoryName(String dir) {
		if (dir != null) {
			File fileObj = new File(dir);
			if (fileObj != null) {
				if (fileObj.isDirectory()) {
					sampleName = fileObj.getName();
					String parent = fileObj.getParent();
					if (parent != null) {
						experimentName = new File(parent).getName();
					}
				}
			}
		}
	}

	/**
	 * This function returns a table of files in the directory with the
	 * extension fixed in the preference.
	 * <p>
	 * Condition: directory musn't be null.
	 * <p>
	 * This function is called for the first time in addDirectory and when user
	 * wants to update a sample if new files have been added (for example during
	 * an experiment on line).
	 */
	private File[] getFiles(String dir) {
		File[] rFile = null;
		if (regularExpressionForExtension.toLowerCase().contains("bruker")) {
			// Pattern p = Pattern.compile("\\.*bruker\\.*",
			// Pattern.CASE_INSENSITIVE);
			Pattern p = Pattern.compile("bruker", Pattern.CASE_INSENSITIVE);
			p.matcher(regularExpressionForExtension).matches();

			regularExpressionForExtension = p.matcher(
					regularExpressionForExtension)
					.replaceAll("\\\\\\.\\\\\\d+");
		}
		/*
		 * AG : removed the dot in the expression which forces the filter to be
		 * after a dot this covers the Claudio case where file names have not
		 * dot in them but end in ccd
		 */
		// String expression = ".+\\.(" + regularExpressionForExtension + ")";
		String expression = ".+(" + regularExpressionForExtension + ")";
		rFile = fable.framework.toolbox.ToolBox.getFilesByEntireRegex(dir,
				expression, true);

		// Sort (Important to sort for peakSearch)
		ToolBox.quicksort(rFile, 0, rFile.length);

		return rFile;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public void setExperimentAndSampleName(String directory) {
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

	/************************************ REPLACE FILEANDDIRECTORYDIALOG *****************************************/
	public void addFiles(final String[] files, final String directory) {
		setExperimentAndSampleName(directory);
		job = new Job("Loading sample " + sampleName) {
			boolean bAdded;

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				job_status = Status.OK_STATUS;
				setInitialDirectory(directory);
				if (listOfSamples == null) {
					listOfSamples = new Vector<Sample>();
				}
				for (int i = 0; i < files.length; i++) {
					files[i] = directory + System.getProperty("file.separator")
							+ files[i];
				}
				initDirectoryName(directory);
				/*
				 * if(files.length>0){ final String firstFile = files[0];
				 * display.syncExec(new Runnable(){ public void run() { try {
				 * FabioFile f= new FabioFile(firstFile);
				 * sampleName=f.getStem(); } catch (FabioFileException e) {
				 * job_status=Status.CANCEL_STATUS;
				 * //System.out.println("Fable can't load file: " + firstFile
				 * +". " + e.getMessage());
				 * logger.error("Fable can't load file: " + firstFile +". " +
				 * e.getMessage()); }};
				 * 
				 * 
				 * }); }
				 */
				if (job_status == Status.OK_STATUS) {
					final Sample spl = new Sample("files selected", sampleName,
							directory);
					spl.setDirectory(false);
					controller.addSample(spl);
					monitor.beginTask(sampleName, files.length);
					if (monitor.isCanceled()) {
						listOfSamples.remove(spl);
						return Status.CANCEL_STATUS;
					}
					for (int i = 0; i < files.length; i++) {
						final int j = i;
						display.syncExec(new Runnable() {
							public void run() {
								try {
									bAdded = spl.addFabioFile(new FabioFile(
											files[j]));
									if (monitor.isCanceled()) {
										listOfSamples.remove(spl);
										job_status = Status.CANCEL_STATUS;
									}
								} catch (FabioFileException e) {
									logger.error(e.getMessage());
								}
							}
						});
						if (bAdded && job_status == Status.OK_STATUS) {
							monitor.worked(1);
							int percentage = (int) (((j + 1) * 100) / files.length);
							monitor.subTask(percentage + "% done:" + files[j]
									+ " added");
						}
						if (monitor.isCanceled()) {
							listOfSamples.remove(spl);
							return Status.CANCEL_STATUS;
						}
					}
					if (sort == null) {
						sort = new Vector<FabioFile>();
					}
					if (applyFilter) {
						spl.setFilter(filter);
					} else {
						spl.setFilter("");
					}

					sort = spl.getFilteredfiles();
					if (monitor.isCanceled()) {
						listOfSamples.remove(spl);
						return Status.CANCEL_STATUS;
					}
					listOfSamples.lastElement().setCurrentFilesFromFabio();
					display.asyncExec(new Runnable() {
						public void run() {
							populateTreeSample();
						}
					});
					if (monitor.isCanceled()) {
						listOfSamples.remove(spl);
						return Status.CANCEL_STATUS;
					}
					display.asyncExec(new Runnable() {
						public void run() {
							// setCurrentSample(listOfSamples.size()-1);
							if (listOfSamples != null
									&& listOfSamples.size() > 0) {
								int index = listOfSamples.size() - 1;
								currentSample = listOfSamples.elementAt(index);
								sampleTable.getTable().setSelection(index);
								// Disable update
								btnUpdate.setEnabled(false);
							} else {
								currentSample = null;
							}
							setCurrentSample();
							logger
									.debug("Create a new Sample for "
											+ directory);
						}
					});
					monitor.done();
				} else {
					return job_status;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK())
					logger.debug("Job completed successfully");
				else
					logger.error("Job did not complete successfully");
			}
		});
	}

	/**
	 * This function update a directory if new files have been added. New since
	 * 29/07/2008.
	 */
	public void updateDirectory() {
		if (currentSample != null) {
			try {
				boolean indexLastFile = false;
				/* is the last file in the list selected ? */
				// logger.debug(" current index "+controller.getCurrentFileIndex()+" last index "+(controller
				// .getCurrentsample().getFilteredfiles().size()-1));
				if (controller.getCurrentFileIndex() >= controller
						.getCurrentsample().getFilteredfiles().size() - 1)
					indexLastFile = true;
				File[] files = getFiles(currentSample.getDirectoryPath());
				currentSample.updateFabioFiles(files);
				if (currentSample.hasFile()) {
					sort = currentSample.getFilteredfiles();
					updateFilterInfo();
				}
				if (sort.size() > 0) {
					f_fabio = currentSample.getCurrentFabioFile();
				}
				populateFileTable();
				populateCombo();
				/*
				 * if the last file was being displayed make sure this is still
				 * the case
				 */
				if (indexLastFile) {
					controller.setCurrentFileIndex(controller
							.getCurrentsample().getFilteredfiles().size());
					fileTable.deselectAll();
					fileTable.select(controller.getCurrentFileIndex());
					fileTable.showSelection();
				}
			} catch (FabioFileException e) {
				if (FableMessageConsole.console != null) {
					FableMessageConsole.console
							.displayError("An error occured while updating the directory");
					logger
							.debug("An error occured while updating the directory in sample chooser : "
									+ e.getMessage());
				}
			}
		}
	}

	/**
	 * This function is called when a directory is selected.
	 * <p>
	 * It gets all files with the type set in preferences to fill left table. If
	 * a filter exists and if the user has checked "set filter for all samples"
	 * a filter on the name is set.
	 * 
	 * @param directory
	 */
	public void addDirectory(final String directory) {
		setExperimentAndSampleName(directory);
		job = new Job("Loading sample " + sampleName) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (listOfSamples == null) {
					listOfSamples = new Vector<Sample>();
				}
				job_status = Status.OK_STATUS;
				final File[] filesInDir = loadSample(directory);
				final Sample spl;
				// reset sample name to get files_stem
				setInitialDirectory(directory);
				// initialDirData=Directory;
				try {
					spl = new Sample(experimentName, sampleName, directory,
							filesInDir);

					spl.setDirectory(true);
					listOfSamples.add(spl);
					monitor.beginTask(sampleName, filesInDir.length);

					if (monitor.isCanceled()) {
						listOfSamples.remove(spl);
						return Status.CANCEL_STATUS;
					}

					currentSample = spl;
					/*
					 * for (int i = 0; job_status == Status.OK_STATUS && i <
					 * filesInDir.length; i++) { final int j = i;
					 * 
					 * display.syncExec(new Runnable() { public void run() { try
					 * { bAdded = currentSample .addFabioFile(filesInDir[j]); if
					 * (monitor.isCanceled()) { listOfSamples.remove(spl);
					 * currentSample = listOfSamples.lastElement(); job_status =
					 * Status.CANCEL_STATUS; } } catch (FabioFileException e) {
					 * job_status = Status.CANCEL_STATUS;
					 * logger.error(e.getMessage()); }
					 * 
					 * } }); if (bAdded && job_status == Status.OK_STATUS) { int
					 * percentage = (int) (((j + 1) 100) / filesInDir.length);
					 * monitor.subTask(percentage + "% done:" + filesInDir[j] +
					 * " added"); } else if (job_status == Status.CANCEL_STATUS)
					 * { return job_status; }
					 * 
					 * monitor.worked(1); if (monitor.isCanceled()) {
					 * listOfSamples.remove(currentSample); currentSample =
					 * listOfSamples.lastElement(); return Status.CANCEL_STATUS;
					 * }
					 * 
					 * }
					 */
					if (sort == null) {
						sort = new Vector<FabioFile>();
					}
					initSampleFilteredFiles(currentSample);

					display.asyncExec(new Runnable() {
						public void run() {
							populateTreeSample();
						}
					});
					if (monitor.isCanceled()) {
						listOfSamples.remove(currentSample);
						currentSample = listOfSamples.lastElement();
						return Status.CANCEL_STATUS;
					}
					display.asyncExec(new Runnable() {
						public void run() {
							// setCurrentSample(listOfSamples.size()-1);
							if (listOfSamples != null
									&& listOfSamples.size() > 0) {
								sampleTable.getTable().setSelection(
										listOfSamples.indexOf(currentSample));
								btnUpdate.setEnabled(true);
							} else {
								currentSample = null;
							}
							setCurrentSample();
							logger
									.debug("Create a new Sample for "
											+ directory);
						}
					});
				} catch (FabioFileException e) {
					FableMessageConsole.console
							.displayError("An error occured while adding directory : "
									+ e.getMessage());
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK())
					logger.debug("Job completed successfully");
				else
					logger.error("Job did not complete successfully");
			}
		});
	}

	/*
	 * private void buildSampleName(String filePath) { sampleName =
	 * filePath.substring(0, filePath.indexOf(".") - 4); }
	 */
	@Override
	public void dispose() {
		Activator.getDefault().getPreferenceStore()
				.removePropertyChangeListener(preferencesListener);
		super.dispose();
	}

	public static String getInitialDirectory() {
		if (initialDirData == null) {
			loadInitialDirectory();
		}
		return initialDirData;
	}

	public static void setInitialDirectory(String _initialDirectory) {
		initialDirData = _initialDirectory;
		saveInitialDirectory();
	}

	/**
	 * restore the initial directory from the directory.xml memento store
	 */
	public static void loadInitialDirectory() {
		FileReader reader = null;
		try {
			reader = new FileReader(Activator.getDefault().getStateLocation()
					.append("directory.xml").toFile());
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			initialDirData = memento.getString(IVarKeys.INITIAL_DIRECTORY);
			reader.close();
		} catch (FileNotFoundException e) {
			// ignore
		} catch (WorkbenchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * save the initial directory in the directory.xml memento store
	 */
	public static void saveInitialDirectory() {
		FileWriter writer = null;
		try {
			XMLMemento memento = XMLMemento
					.createWriteRoot(IVarKeys.INITIAL_DIRECTORY);
			memento.putString(IVarKeys.INITIAL_DIRECTORY, initialDirData);
			writer = new FileWriter(Activator.getDefault().getStateLocation()
					.append("directory.xml").toFile());
			memento.save(writer);
			writer.close();
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * To show required fields when a property change in another view (for
	 * instance, launched when preprocessor is launched without any files)
	 * 
	 * @param event
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IVarKeys.SET_CURRENTFILE_EVENT)) {
			// Get current selection index. If different from what has been
			// set in the controller, please select it in table
			if (fileTable.isDisposed()) {
				return;
			}
			int index = fileTable.getSelectionIndex();
			int selection = controller.getCurrentFileIndex();
			if (currentSample != null
					&& currentSample == controller.getCurrentsample()) {
				f_fabio = currentSample.getCurrentFabioFile();
				if (index != selection) {
					int nbItems = fileTable.getItemCount();
					if (nbItems > 0 && selection < nbItems) {
						fileTable.setSelection(selection);
					}
				}
			}
		} else if (event.getProperty()
				.equals(IVarKeys.UPDATE_SAMPLEFILES_EVENT)) {
			this.sort = controller.getCurrentsample().getFilteredfiles();

		}
	}

	public Sample getCurrentSample() {
		return currentSample;
	}

	public Job getJob() {
		return job;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * This method init fabio filtered peaks file in Sample object.
	 */
	public void initSampleFilteredFiles(Sample sample) {

		if (applyFilter) {
			sample.setFilter(filter);

		} else {
			sample.setFilter("");
		}
		if (sort == null) {
			sort = new Vector<FabioFile>();
		}
		sort = sample.getFilteredfiles();
		sample.setCurrentFilesFromFabio();
	}

	public Vector<FabioFile> getSortedfiles() {

		return sort;
	}

	/**
	 * Start a system job to update the current directory automatically every 10
	 * seconds This is useful for surveying directories on the beamline where
	 * files are constantly being created or where a data analysis program is
	 * producing new files
	 * 
	 * @author andy
	 */
	private void autoUpdateJobStart() {
		logger.info("start job to automatically update the directory");
		autoUpdateJob = new Job(
				"Automatically update files in current directory") {
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					if (autoUpdate && !monitor.isCanceled()) {
						display.syncExec(new Runnable() {
							// @Override
							public void run() {
								if (currentSample != null) {
									logger.info("update directory");
									updateDirectory();
								}
							}
						});
					}
					if (!autoUpdate || monitor.isCanceled()) {
						logger.info("cancel auto update job");
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				} finally {
					if (autoUpdate)
						schedule(10000); // start again in 10 seconds
				}
			}
		};
		autoUpdateJob.setSystem(true);
		autoUpdateJob.schedule();
	}
}
