/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.editor;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.TreeSet;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.datahandling.file.WildCardFileFilter;
import org.embl.cca.utils.imageviewer.FilenameCaseInsensitiveComparator;
import org.embl.cca.utils.imageviewer.MemoryImageEditorInput;
import org.embl.cca.utils.threading.TrackableJob;
import org.embl.cca.utils.threading.TrackableRunnable;

import fable.framework.logging.FableLogger;
import fable.imageviewer.component.ActionsProvider;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.component.ImageComponentImage;
import fable.imageviewer.component.ImagePlay;
import fable.imageviewer.model.ImageModel;
import fable.imageviewer.model.ImageModelFactory;
import fable.imageviewer.rcp.Activator;

/**
 * ImageEditor
 * 
 * @author Matthew Gerring
 * 
 */
public class ImageEditor extends EditorPart implements IReusableEditor, ActionsProvider, IShowEditorInput {
	/**
	 * Plug-in ID.
	 */
	public static final String ID = "fable.imageviewer.editor.ImageEditor";
	
	/**
	 * The object which does the work, can be used in different view parts.
	 */
	private ImageComponent imageComponent; ////earlier AbstractPlottingSystem plottingSystem

	/**
	 * Redirects the action bars so that we can show local actions to the
	 * user
	 */
	private ActionBarWrapper actionBarsWrapper;

	private Label totalSliderImageLabel;
	private Slider imageSlider;
	private Text imageFilesWindowWidthText;
	private int imageFilesWindowWidth; //aka batchAmount
	private File[] allImageFiles;
	private TreeSet<File> loadedImageFiles; //Indices in loadedImagesFiles which are loaded
	boolean autoFollow;
	Button imageFilesAutoLatestButton;
	ImageModel resultImageModel = null;
	static private NumberFormat decimalFormat = NumberFormat.getNumberInstance();

	ExecutableManager imageLoaderManager = null;
	Thread imageFilesAutoLatestThread = null;

/*
	private void initSlider( int amount ){ 
		//if(!label.isDisposed() && label !=null){  
		imageSlider.setValues( 1, 1, amount+1, 1, 1, Math.max(1, amount/5) );
		totalSliderImageLabel.setText( "1" + "/" + amount );
		totalSliderImageLabel.getParent().pack();
		//}  
	}  
*/
	private void updateSlider( int sel ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
			final int min = 1;
			final int total = allImageFiles.length;
			final int selection = Math.max(Math.min(sel,total + 1),min);
			
			try {  
//			if( imageSlider.getSelection() == selection )
//				return;
				imageFilesWindowWidth = imageSlider.getThumb();
				imageSlider.setValues(selection, min, total+1, imageFilesWindowWidth, 1, Math.max(imageFilesWindowWidth, total/5));
				totalSliderImageLabel.setText( "" + selection + "/" + total + "   ");
				totalSliderImageLabel.getParent().pack();
				sliderMoved( selection );
			} catch (SWTException e) {  
				//eat it!  
			}  
		}
	}
	
	private void updateBatchAmount( int amount ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
			if( imageFilesWindowWidth == amount )
				return;
			int oldSel = imageSlider.getSelection();
			int newSel = oldSel;
			if( amount < 1 )
				amount = 1;
			else if( amount > imageSlider.getMaximum() - oldSel && oldSel > 1 ) {
				newSel = imageSlider.getMaximum() - amount;
				if( newSel < 1 ) {
					newSel = 1;
					amount = imageSlider.getMaximum() - newSel;
				}
//				amount = imageSlider.getMaximum() - imageSlider.getSelection();
			}
			imageSlider.setThumb( amount );
			if( oldSel != newSel )
				imageSlider.setSelection( newSel );
			else
				updateSlider( newSel );
/*
			imageSlider.setSelection(imageSlider.getSelection());

			imageFilesWindowWidth = amount;
			imageFilesWindowWidthText.setText( "" + amount );
			imageFilesWindowWidthText.getParent().pack();
			sliderMoved( imageSlider.getSelection() ); //Updates loaded files and draw image
*/
		}
	}

	private void sliderMoved( int pos ) {
		File[] toLoadImageFiles = null;
		synchronized (allImageFiles) {
			int iMax = imageFilesWindowWidth;
			toLoadImageFiles = new File[iMax];
			for( int i = 0; i < iMax; i++ )
				toLoadImageFiles[ i ] = allImageFiles[ pos - 1 + i ];
		}
		createPlot(toLoadImageFiles);
	}

	public void onImageFilesAutoLatestButtonSelected() {
		if( autoFollow != imageFilesAutoLatestButton.getSelection() ) {
			autoFollow = imageFilesAutoLatestButton.getSelection();
			if( autoFollow ) {
	//			imageSlider.setEnabled( false );
				imageFilesAutoLatestThread = new Thread() {
					ExecutableManager imageFilesAutoLatestManager = null;
					protected boolean checkDirectory() {
						final IPath imageFilename = getPath( getEditorInput() );
						final File[] currentAllImageFiles = listIndexedFilesOf( imageFilename );
						TreeSet<File> currentAllImageFilesSet = new TreeSet<File>( Arrays.asList(currentAllImageFiles) );
						TreeSet<File> allImageFilesSet = new TreeSet<File>( Arrays.asList(allImageFiles) );
						if( currentAllImageFilesSet.containsAll(allImageFilesSet)
								&& allImageFilesSet.containsAll(currentAllImageFilesSet) )
							return false;
						if( imageLoaderManager.isAlive() )
							return false;
						final TrackableRunnable runnable = new TrackableRunnable(imageFilesAutoLatestManager) {
							@Override
							public void runThis() {
								synchronized (imageSlider) {
									allImageFiles = currentAllImageFiles; 
									updateSlider( allImageFiles.length - imageFilesWindowWidth + 1 );
								}
							}
						};
						imageFilesAutoLatestManager = ExecutableManager.addRequest(runnable);
						return true;
					}
					@Override
					public void run() {
						do {
							int sleepTime = 10; //Sleeping some even if directory updated, so user can move slider (and abort this thread)
							if( !checkDirectory() )
								sleepTime = 100;
							try {
								sleep(sleepTime);
							} catch (InterruptedException e) {
								break;
							}
						} while( true );
					}
					@Override
					public void interrupt() {
						if( imageFilesAutoLatestManager != null )
							imageFilesAutoLatestManager.interrupt();
						super.interrupt();
					}
				};
				imageFilesAutoLatestThread.start();
			} else {
				imageFilesAutoLatestThread.interrupt();
	//			imageSlider.setEnabled( true );
			}
		}
	}

	private void createImageSelectorUI(Composite parent) {
		final Composite sliderMain = new Composite(parent, SWT.NONE);
		sliderMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		sliderMain.setLayout(new GridLayout(5, false));
		GridUtils.removeMargins(sliderMain);
		
		imageSlider = new Slider(sliderMain, SWT.HORIZONTAL);
		imageSlider.setThumb(imageFilesWindowWidth);
//		imageSlider.setBounds(115, 50, 25, 15);
		totalSliderImageLabel = new Label(sliderMain, SWT.NONE);
		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
		totalSliderImageLabel.setText("0/0");
		imageSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//				imageSlider.setSelection( imageSlider.getSelection() );
				if( autoFollow ) {
					imageFilesAutoLatestButton.setSelection( false );
					//setSelection does not trigger the Selection event because we are in Selection event here already,
					onImageFilesAutoLatestButtonSelected(); //so we have to call it manually, which is lame.
				}
				updateSlider( imageSlider.getSelection() );
			}
		});
		final Label imageFilesWindowWidthLabel = new Label(sliderMain, SWT.NONE);
		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
		imageFilesWindowWidthLabel.setText("Batch Amount");
		imageFilesWindowWidthText = new Text(sliderMain, SWT.BORDER | SWT.RIGHT);
		imageFilesWindowWidthText.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
		imageFilesWindowWidthText.setText( "" + imageFilesWindowWidth );
		imageFilesWindowWidthText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		imageFilesWindowWidthText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if( imageFilesWindowWidthText == null || imageFilesWindowWidthText.isDisposed() ) return;
				if( !imageFilesWindowWidthText.isEnabled() || imageFilesWindowWidthText.getText().isEmpty() )
					return;
				try {
					updateBatchAmount( decimalFormat.parse( imageFilesWindowWidthText.getText() ).intValue() );
				} catch (ParseException exc) {
					FableLogger.error("Unable to parse batch amount value: " + imageFilesWindowWidthText.getText(), exc);
				}
			}
		});
		imageFilesAutoLatestButton = new Button(sliderMain, SWT.CHECK);
		imageFilesAutoLatestButton.setText("Auto latest");
		imageFilesAutoLatestButton.setToolTipText("Automatically scan directory and display last batch");
		autoFollow = false;
		imageFilesAutoLatestButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onImageFilesAutoLatestButtonSelected();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	protected IPath getPath( IEditorInput editorInput ) {
		final IPath imageFilename;
		if( editorInput instanceof FileEditorInput )
			imageFilename = new Path( ((FileEditorInput)editorInput).getURI().getPath() ); 
		else if( editorInput instanceof FileStoreEditorInput )
			imageFilename = new Path( ((FileStoreEditorInput)editorInput).getURI().getPath() ); 
		else {
			IFile iF = (IFile)editorInput.getAdapter(IFile.class);
			if( iF != null )
				imageFilename = iF.getLocation().makeAbsolute();
			else {
				FableLogger.error("Cannot determine full path of requested file");
				return null;
			}
		}
		return imageFilename;
	}

	protected File[] listIndexedFilesOf( IPath imageFilename ) {
		File[] result = null;
		String q = imageFilename.removeFileExtension().lastSegment().toString();
		String r = q.replaceAll("[0-9]*$", "");
		int len = q.length() - r.length();
		for( int i = 0; i < len; i++ )
		  r += "?";
		r += "." + imageFilename.getFileExtension();
		result = new File(imageFilename.removeLastSegments(1).toString()).listFiles( new WildCardFileFilter(r, false) );
		Arrays.sort( result, new FilenameCaseInsensitiveComparator() );
		return result;
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
		
		parent.setLayout(new GridLayout(1,false)); ////earlier main
		
		final Composite top    = new Composite(parent, SWT.NONE); ////earlier tools
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Text point = new Text(top, SWT.LEFT);
		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		point.setEditable(false);
		GridUtils.setVisible(point, true);
		point.setBackground(top.getBackground());

		final MenuManager    menuMan = new MenuManager();
	    final ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar toolBar = toolMan.createControl(top);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
		loadedImageFiles = new TreeSet<File>();
		imageFilesWindowWidth = 1;
		/* Top line containing image selector sliders */
		createImageSelectorUI(top);

	    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
	        @Override
	        public void run() {
                final Menu   mbar = menuMan.createContextMenu(toolBar);
       		    mbar.setVisible(true);
	        }
	    };
	
		final IActionBars bars = this.getEditorSite().getActionBars();
        this.actionBarsWrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
        
        final Composite plotComposite = new Composite(parent, SWT.NONE);
        plotComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        imageComponent = new ImageComponent(this);
        imageComponent.setStatusLabel(point);
        imageComponent.createPartControl(plotComposite);
        
        ImagePlay.setView(this.getImageComponent());
        
		editorInputChanged();

		GridUtils.removeMargins(plotComposite);
        GridUtils.removeMargins(top);
        GridUtils.removeMargins(parent);
        
        toolMan.add(menuAction);
        toolMan.update(true);
   	}

	private void editorInputChanged() {
		if (getEditorInput() instanceof MemoryImageEditorInput) {
			MemoryImageEditorInput miei = (MemoryImageEditorInput)getEditorInput();
			ImageModel imageModel = new ImageModel("", miei.getWidth(), miei.getHeight(), miei.getData(), 0);
			if ("ExpSimImgInput".equals(getEditorInput().getName())) {
			} else {
/*
				System.out.println("First block of received image (imageModel):");
				for( int j = 0; j < 10; j++ ) {
					for( int i = 0; i < 10; i++ ) {
						System.out.print( " " + Integer.toHexString( (int)imageModel.getData(i, j) ) );
					}
					System.out.println();
				}
*/
			}
			createPlot(imageModel);
		} else {
			final IPath imageFilename = getPath( getEditorInput() );
			allImageFiles = listIndexedFilesOf( imageFilename );
			String actFname = imageFilename.lastSegment().toString();
			int pos;
			for (pos = 0; pos < allImageFiles.length; pos++ )
				if (allImageFiles[pos].getName().equals(actFname))
					break;				
			updateSlider( pos + 1 ); //it calls (and must call) createPlot()
		}
 	}

	private void createPlot(final ImageModel imageModel) {
		if( imageComponent != null ) { //async call because we are not in UI thread, and for loadModel must be
	        getSite().getShell().getDisplay().asyncExec(new Runnable() {
	        	public void run() {
	    			imageComponent.loadModel(imageModel);
	        	}
	        });
		}
	}

	private void createPlot(final File[] toLoadImageFiles) {
		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
			TreeSet<File> toLoadImageFilesJob = new TreeSet<File>( Arrays.asList(toLoadImageFiles) );
			ImageModel imageModel = null;

			public IStatus processImage(File imageFile, boolean add) {
				if( add || loadedImageFiles.size() > 1 ) {
					final String filePath = imageFile.getAbsolutePath();
					try {
						imageModel = ImageModelFactory.getImageModel(filePath);
					} catch (Throwable e) {
						FableLogger.error("Cannot load file "+filePath, e);
						return Status.CANCEL_STATUS;
					}
					if (imageModel==null) {
						FableLogger.error("Cannot read file "+getEditorInput().getName());
						return Status.CANCEL_STATUS;
					}
					if( isAborting() )
						return Status.OK_STATUS;
					if( loadedImageFiles.size() == 0 ) {
						if( add )
							resultImageModel = imageModel;
					} else {
						if( add )
							resultImageModel.addImageModel( imageModel );
						else
							resultImageModel.subImageModel( imageModel );
					}
				}
				if( add )
					loadedImageFiles.add( imageFile );
				else {
					loadedImageFiles.remove( imageFile );
					if( loadedImageFiles.size() == 0 )
						resultImageModel = null;
				}
				return Status.OK_STATUS;
			}
				
			public IStatus runThis(IProgressMonitor monitor) {
				/* Since running this and others aswell through imageLoaderManager,
				 * the single access of loading data is guaranteed.
				 */
				IStatus result = Status.CANCEL_STATUS;
				do {
					TreeSet<File> adding = new TreeSet<File>( toLoadImageFilesJob );
					adding.removeAll( loadedImageFiles );
					TreeSet<File> removing = new TreeSet<File>( loadedImageFiles );
					removing.removeAll( toLoadImageFilesJob );
					if( adding.size() + removing.size() > toLoadImageFilesJob.size() ) {
						adding = toLoadImageFilesJob;
						removing.clear();
						loadedImageFiles.clear();
					}
					for( File i : adding ) {
						if( isAborting() )
							break;
						result = processImage(i, true);
						if( result != Status.OK_STATUS )
							break;
					}
					for( File i : removing ) {
						if( isAborting() )
							break;
						result = processImage(i, false);
						if( result != Status.OK_STATUS )
							break;
					}
					if( isAborting() )
						break;
					ImageModel resultImageModelDivided = resultImageModel;
					if( loadedImageFiles.size() > 1 ) {
						resultImageModelDivided = resultImageModel.clone();
						float[] fsetdata = resultImageModelDivided.getData();
						int divider = loadedImageFiles.size();
						int jMax = fsetdata.length;
						for( int j = 0; j < jMax; j++ )
							fsetdata[ j ] /= divider;
					}

					if( isAborting() )
						break;
					createPlot(resultImageModelDivided);
					if( loadedImageFiles.size() > 0 ) { //Checking for sure
						Display.getDefault().syncExec(new Runnable(){  
							public void run() {  
								setPartName(loadedImageFiles.first().getName());
							}  
						});
					}
					result = Status.OK_STATUS;
				} while( false );
				if( isAborting() ) {
					setAborted();
					return Status.CANCEL_STATUS;
				}
				return result;
			}
		};
		job.setUser(false);
		job.setPriority(Job.BUILD);
		imageLoaderManager = ExecutableManager.setRequest(job);
	}
	

	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (imageComponent!=null) {
			imageComponent.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (imageComponent!=null) imageComponent.dispose();
	}

	public ImageComponentImage getImage() {
		return imageComponent.getImage();
	}

	public void setPartName(final String name) {
		super.setPartName(name);
	}

	public ImageComponent getImageComponent() {
		return imageComponent;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		editorInputChanged();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		this.setInput(editorInput);		
	}

	public IActionBars getActionBars() {
		return actionBarsWrapper;
	}
}
