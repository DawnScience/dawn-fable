/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.component;

import java.util.Vector;

import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.fabio.FableJep;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.PlatformUI;

import fable.framework.logging.FableLogger;
import fable.framework.navigator.controller.SampleController;
import fable.framework.navigator.toolBox.IVarKeys;
import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.internal.Coordinates;
import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.model.ImageModel;
import fable.imageviewer.model.ImageModelFactory;
import fable.imageviewer.preferences.PreferenceConstants;
import fable.imageviewer.psf.Statistics;
import fable.imageviewer.rcp.Activator;
import fable.imageviewer.views.ImageView;
import fable.python.Peak;
import fable.python.PeakSearchSpt;

/**
 * ImageComponent implements a view to display an image using the SWT Image widget.
 * The image is automatically fitted to the window size. The main features are
 * the display is fast, a variety of controls allow the user to zoom a box,
 * line, integrated profile or 3d relief, change lookup tables, autoscale or
 * manually scale the range. The image is treated as a floating point image so
 * that images scaled between 0 and 1 are displayed correctly. Images can be
 * loaded using loadFile(FabioFile) or via changeImageData().
 * <p>
 * ImageView allows multiple Views. These are distinguished by the secondary ID.
 * The secondary ID is typically set by IWorkbenchPage.showView(String viewId,
 * String secondaryId, int mode). The viewId to use is ImageView.ID. Any plug-in
 * can call this method and is free to specify the secondary ID as it wishes.
 * ImageView manages 5 secondary IDs, for the main, zoom, slice1D, slice2D, and
 * copy views. These IDs are determined by the public fields SECONDARY_ID_xxx.
 * If you wish to call these views, then you should use these fields by name.
 * Except for the copy views, there is expected to be zero or one of the others.
 * On workspace shutdown ImageView hides (i.e. removes) all of the views that it
 * manages, except the main one with SECONDARY_ID_MAIN. This is to prevent
 * workspace clutter on restarting the workspace. It does not remove others.
 * Other plug-ins are responsible for removing ones they created if desired.
 * When ImageView is first created and does not have an image, then the part
 * name (name on the tab) is set to the secondary ID + "Image View". This should
 * help to distinguish empty Image Views created by ImageView and other
 * plug-ins. Eclipse can create ImageViews with a null secondary ID, e.g. via
 * Window | Show View. ImageView hides these and creates a new one with
 * SECONDARY_ID_MAIN to insure all views have a non-null secondary ID.
 * <p>
 * New images are typically loaded to the Main view. For example, only the Main
 * view listens for property change events from the SampleController associated
 * with the Image Navigator. Loading into the Main view will cause existing
 * Zoom, Slice1, and Slice2 views to update, depending on if there is a
 * selection and the current selection mode. That is, if there is a selection
 * and the current mode is Area then the Zoom view will update, but not Slice1
 * or Slice2. Drag and Drop supports dropping on any view.
 * <p>
 * ImageView is a large View and is implemented in several classes. The
 * principal ones are (1) ImageView which manages the usual view things and
 * holds the values of most of the settings, (2) ImageViewControls which manages
 * the SWT controls except for the imageCanvas, and (3) ImageViewImage which
 * manages the imageCanvas and things, such as selections, related to it.
 * <p>
 * <b>Dependencies</b> : <br>
 * FabioFile - for loading FabioFiles <br>
 * ZoomLineView - for plotting lines and integrated profiles<br>
 * <br>
 * The image can be viewed in 8 orientations. These orientations are associated
 * with the TotalCrys o parameters (o11, o12, o21, o22). The parameters that
 * result in an image oriented as it would be looking at the detector in the
 * direction of the beam are the o parameters to specify in other Fable
 * software, such as ImageD11. <br>
 * <br>
 * The coordinate origin can be selected as TL=(0,0) (typical image
 * coordinates), TR=(0,0), BR=(0,0) (TotalCrys coordinates), and BL=(0,0) (usual
 * xy coordinate system). They can also be specified as custom. The only place
 * the coordinates appear is in the display of the mouse position. Note that the
 * coordinate system and the image orientation may be specified independently. <br>
 * <br>
 * The TotalCryst project (cf.
 * http://fable.wiki.sourceforge.net/space/showimage/Geometry_version_1.0.2.pdf)
 * defines the coordinate system as follows: <br>
 * (o) Horizontal axis as Y with zero on the right and positive to the left<br>
 * (o) Vertical axis as Z with zero at the bottom and positive up.<br>
 * 
 * @author Andy Gotz (ESRF), Ken Evans (APS)
 * 
 */
public class ImageComponent implements IPropertyChangeListener,
		                               IImagesVarKeys, 
		                               ISelectionListener {
	/**
	 * Secondary ID for the main ImageView. There should be zero or one of
	 * these.
	 */
	public static final String SECONDARY_ID_MAIN = "Main";

	/**
	 * Secondary ID for the Zoom ImageView. There should be zero or one of
	 * these.
	 * 
	 * Do not change the string is referenced in projects outside Fable
	 * (for reasons of avoiding direct connections).
	 */
	public static final String SECONDARY_ID_ZOOM = "Zoom";
	/**
	 * Secondary ID for the slice 1D ImageView. There should be zero or one of
	 * these.
	 */
	public static final String SECONDARY_ID_SLICE1D = "Slice 1D";
	/**
	 * Secondary ID for the slice 2D ImageView. There should be zero or one of
	 * these.
	 */
	public static final String SECONDARY_ID_SLICE2D = "Slice 2D";
	/**
	 * Number to use for the secondary ID for the first copy ImageView. Do not
	 * use this explicitly as a secondary ID. Use zoomSecondaryID and increment
	 * it afterward.
	 */
	public static int SECONDARY_ID_COPY_START = 100;
	/**
	 * Number to use for the secondary ID for the next copy ImageView. It should
	 * be incremented when used so each copy appears in a different view.
	 */
	public static int copySecondaryID = SECONDARY_ID_COPY_START;

	private SampleController controller = SampleController.getController();

	/**
	 * A reference to the class that manages the SWT controls for this view.
	 */
	private ImageComponentUI controls = null;
	/**
	 * A reference to the class that manages the SWT image for this view.
	 */
	public ImageComponentImage image = null;
	/**
	 * The Display for this view.
	 */
	private Display display;
	/**
	 * The ImageModel. We have two choices: (1) keep one ImageModel and reset
	 * it, or (2) create new ones as needed. (1) allows the use of listeners for
	 * the RESET event. (2) allows an ImageModel to be used in two views, saving
	 * storage. Currently we are not using events, so we go with (2). We could
	 * use (2) anyway if we take care of adding and removing the listeners
	 * whenever an ImageModel is changed. This could be done in a
	 * changeImageModel method, for instance.
	 */
	private ImageModel imageModel = null;
	private ImageModel imageDiffModel = null;
	private ImageModel imageSavedModel = null;
	// KE: TODO: This is not set anywhere
	private String fileNameSaved = "";
	private Vector<Float> peaks;
	/**
	 * Coordinates representing the current coordinate system. Note that this
	 * system is related to the oriented image.
	 */
	private Coordinates coordinates = new Coordinates();
	/**
	 * The value of x0 for custom coordinates last used.
	 */
	double x0Save = 0;
	/**
	 * The value of y0 for custom coordinates last used.
	 */
	double y0Save = 0;
	/**
	 * The value of pixelWidth for custom coordinates last used.
	 */
	double pixelWidthSave = 1;
	/**
	 * The value of pixelHeight for custom coordinates last used.
	 */
	double pixelHeightSave = 1;
	/**
	 * The value of xName for custom coordinates last used.
	 */
	String xNameSave = "x";
	/**
	 * The value of yName for custom coordinates last used.
	 */
	String yNameSave = "y";
	/**
	 * Parameter representing which of the eight orientations is used:
	 * <ul>
	 * <li>Original Image (1 0 0 1)</li>
	 * <li>Flip H (1 0 0 -1)</li>
	 * <li>Flip V (-1 0 0 1)</li>
	 * <li>Flip H and V (-1 0 0 -1)</li>
	 * <li>90 deg CW, Flip H (0 1 1 0)</li>
	 * <li>90 deg CW (0 1 -1 0)</li>
	 * <li>90 deg CWW (0 -1 1 0)</li>
	 * <li>90 deg CW, Flip V (0 -1 -1 0)</li>
	 * </ul>
	 */
	private int orientation = 0;
	/**
	 * The current selection choice for zooming.
	 */
	private ZoomSelection zoomSelection = ZoomSelection.AREA;
	private int coordOrigin = 0;
	/**
	 * Flag that indicates whether peaks are to be shown.
	 */
	private boolean peaksOn = false;
	/**
	 * Flag that indicates whether PSF is to be applied.
	 */
	private boolean psfOn = false;
	/**
	 * Sets the size of the rectangle used to mark peaks, Should be odd.
	 */
	private int peakMarkerSize = ImageComponentUI.DEFAULT_MARKER_SIZE;
	/**
	 * The statistics from the current file, for the zoomed area,
	 * e.g. minimum, maximum, mean intensity.
	 */
	private Statistics zoomStatistics = null;
	/**
	 * The minimum intensity currently used. May be from the file or
	 * user-specified, depending on the setting of autoscale.
	 */
	private Float userMinimum = Float.NaN;
	/**
	 * The maximum intensity currently used. May be from the file or
	 * user-specified, depending on the setting of autoscale.
	 */
	private Float userMaximum = Float.NaN;
	/**
	 * The current palette.
	 */
	private PaletteData palette = null;
	/**
	 * The index of the current palette in
	 */
	private int paletteIndex = 0;

	/**
	 * Flag indicating if the image displayed is a single file image or a
	 * difference image.
	 */
	protected boolean imageDiffOn = false;
	private boolean jobRunning = false;
	/**
	 * Listener to listen for workspace shutdown so we can remove views with
	 * selected secondary IDs on shutdown to avoid clutter on restart.
	 */
	private IWorkbenchListener workbenchListener = null;
	
	/**
	 * The parent part, either a view or an editor.
	 */
	private IWorkbenchPart3 parentPart;
	private ActionsProvider provider;
	private Text            statusLabel;

	public ImageComponent(final IWorkbenchPart3 parentPart) {
        this(parentPart, (ActionsProvider)parentPart);
	}

	public ImageComponent(final IWorkbenchPart3 parentPart, final ActionsProvider provider) {
		this.parentPart = parentPart;
		this.provider   = provider;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createPartControl(Composite parent) {

		// Get the display
		display = parent.getDisplay();
		// Get the logger
		// logger = FableLogger.getLogger(this.getClass());
		controls = new ImageComponentUI(this);
		controls.setStatusLabel(statusLabel);
		controls.createControls(parent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (controls!=null) controls.setFocus();
		
		// Get the right side plot to show
		if (getSecondaryId()!=ImageComponent.SECONDARY_ID_MAIN) return;
		
		final ZoomSelection sel = getZoomSelection();
		if (sel!=null) sel.bringToTop();
		
	}

	/**
	 * This method receives selectionEvents from the workbench. This is
	 * currently used for PeakSearch and its spt output file. When a user
	 * selects a peak in a TableViewer and if the selection is a instance of
	 * <code>PeakSearchSpt</code> or an instance of <code>Peak</code>, spots are
	 * displayed in the image.
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// DEBUG
		// System.out.println("\n>>>Entering selectionChanged");
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			Object first = sSelection.getFirstElement();
			Object[] selections = sSelection.toArray();
			// DEBUG
			// System.out.println("Check selection");
			if (first instanceof PeakSearchSpt) {
				// Only do this for the main or zoom secondaryId
				String id2 = getSecondaryId();
				if (id2 == null
						|| (!id2.equals(ImageComponent.SECONDARY_ID_MAIN) && !id2
								.equals(SECONDARY_ID_ZOOM))) {
					return;
				}
				PeakSearchSpt peakFile = ((PeakSearchSpt) first);
				if (peakFile.getImageFile() != null) {
					if (id2.equals(SECONDARY_ID_MAIN)) {
						try {
							this.loadModel(ImageModelFactory.getImageModel(peakFile.getImageFile()));
						} catch (Throwable e) {
							FableLogger.error("Cannot load file "+peakFile.getFabioFileName(), e);
						}
					}
					setPeaksOn(true);
					image.initAndDisplayImage();
					// Show peaks if there are some peaks
					Vector<Float> vals = peakFile.getTabChildren();
					if (vals != null) {
						setPeaks(vals);
						image.showPeaks();
					}
				}
			} else if (first instanceof Peak) {
				// Only do this for the main or zoom secondaryId
				String id2 = getSecondaryId();
				if (id2 == null
						|| (!id2.equals(SECONDARY_ID_MAIN) && !id2
								.equals(SECONDARY_ID_ZOOM))) {
					return;
				}
				PeakSearchSpt parent = ((Peak) first).getParent();
				int nPeaksToDisplay = selections.length;

				float[] coloredPeak = new float[nPeaksToDisplay * 2];
				int j;
				int k = 0;
				for (int ip = 0; ip < selections.length; ip++) {
					Peak peak = (Peak) selections[ip];
					if (peak.isVisible()) {
						j = k + 1;
						coloredPeak[k] = Float.valueOf(peak.getS());
						coloredPeak[j] = Float.valueOf(peak.getF());
						k += 2;
					}
				}
				Vector<Float> vals = parent.getTabChildren();
				if (vals != null) {
					setPeaks(vals);
				}
				setPeaksOn(true);
				// Draw peaks in red with selected peaks in green
				image.showSelectedPeaks(coloredPeak);
			}
		}

	}
	
	/**
	 * 
	 * @param fileName
	 * @throws Throwable 
	 */
	public void loadFile(String fileName) throws Throwable {
		loadModel(ImageModelFactory.getImageModel(fileName));
	}

	/**
	 * Loads a new image from the specified image model.
	 * 
	 * @param imageModel
	 *            The image model to use.
	 */
	public void loadModel(ImageModel imageModel) {
		
		final long start = System.currentTimeMillis();
		try {
			if (imageModel == null) {
			    image.clearCanvas();
				FableUtils.errMsg(this, "Unable to load null image model");
				return;
			}
			// DEBUG
			// if (true) {
			// System.out.println("loadFile: " + " [" + getSecondaryId() + "] "
			// + getPartName());
			// System.out.printf(" imageModel: %d %d\n", imageModel.getWidth(),
			// imageModel.getHeight());
			// System.out.printf(" imageRect (before): %d %d %d %d\n", image
			// .getImageRect().x, image.getImageRect().y, image
			// .getImageRect().width, image.getImageRect().height);
			// }
			this.imageModel = imageModel;
			
			if (controls != null) {
				controls.setStatusText("Loading... ");
			}
	
			peaks = null;
			if (image.isDisposed()) return;
			
			image.setImageChanged(true);
			// To reduce flashing only clear the image background if the size
			// changes
			if (image.getImageRect().width != imageModel.getWidth()
					|| image.getImageRect().height != imageModel.getHeight()) {
				image.clearCanvas();
			}
			// Reset the image rect. In the case of a zoomed image, the old zoom
			// area may or may not be appropriate to the new image. We choose to
			// make the new zoom area be in the same proportions to the new
			// image as the old one was to the old image. Other choices could be
			// made.
			int newWidth = imageModel.getWidth();
			int newHeight = imageModel.getHeight();
			int newX = 0;
			int newY = 0;
			double widthFactor = 1;
			double heightFactor = 1;
			if (image != null && image.getOrigRect() != null
					&& image.getImageRect() != null) {
				if (image.getOrigRect().width != 0) {
					widthFactor = (double) newWidth / image.getOrigRect().width;
				}
				if (image.getOrigRect().height != 0) {
					heightFactor = (double) newHeight / image.getOrigRect().height;
				}
				newX = (int) (widthFactor * image.getImageRect().x);
				newY = (int) (heightFactor * image.getImageRect().y);
				// If the width or height is 0 this indicates it has not been
				// initialized, (or something else is wrong) so don't scale
				if (image.getImageRect().width > 0) {
					newWidth = (int) (widthFactor * image.getImageRect().width);
				}
				if (image.getImageRect().height > 0) {
					newHeight = (int) (heightFactor * image.getImageRect().height);
				}
			}
			// Insure it is in bounds
			if (newWidth > imageModel.getWidth()) {
				newWidth = imageModel.getWidth();
			}
			if (newHeight > imageModel.getHeight()) {
				newHeight = imageModel.getHeight();
			}
			if (newX + newWidth > imageModel.getWidth()) {
				newX = 0;
			}
			if (newY + newHeight > imageModel.getHeight()) {
				newY = 0;
			}
			image.setImageRect(new Rectangle(newX, newY, newWidth, newHeight));
			// DEBUG
			// if (true) {
			// System.out.printf(" imageRect (after set): %d %d %d %d\n", image
			// .getImageRect().x, image.getImageRect().y, image
			// .getImageRect().width, image.getImageRect().height);
			// }
			image.calculateMainRectangles();
			// DEBUG
			// if (true) {
			// System.out.printf(" imageRect (after calc): %d %d %d %d\n", image
			// .getImageRect().x, image.getImageRect().y, image
			// .getImageRect().width, image.getImageRect().height);
			// System.out.println();
			// }
			resetCoordinates();
			if (imageDiffOn) {
				calcImageDiff();
			} else {
				setPartName(getSecondaryId() + " " + getFileName());
			}
			// Use the original rect here, not the image rect. They are the same
			// for the main view but not for the a zoom view.
//			setStatistics(imageModel.getStatistics(image.getOrigRect()));
			setStatistics( image.getOrigRect() );
			
		} finally {
			if (imageModel.getTimeToReadImage()>0) {
				updateStatusLabel("Loaded in "
						+ imageModel.getTimeToReadImage() + " ms");
			} else {
				final long end = System.currentTimeMillis();
				updateStatusLabel("Loaded in "
						+ (end-start) + " ms");
			}
		}
	}

	/**
	 * Calculate / image
	 */

	public void calcImageDiff() {
		long start = System.currentTimeMillis();
		if (imageSavedModel == null) {
			setImageDiffOn(false);
			updateStatusLabel("Cannot create difference image - no difference image set");
			FableUtils.errMsg(this, "Cannot create difference: "
					+ "no difference image set\n");
			setPartName(null);
			return;
		}
		// Take difference of image
		FableLogger.debug("Calculate difference of " + imageModel.getFileName()
				+ " and background image " + imageSavedModel.getFileName());
		float[] newArray = imageModel.getData();
		float[] savedArray = imageSavedModel.getData();
		int newLen = newArray.length;
		int savedLen = savedArray.length;
		if (newLen != savedLen) {
			FableUtils.errMsg(this, "Cannot create difference:\n"
					+ "newWidth=%d savedWidth=%d\n"
					+ "newHeight=%d savedHeight=%d");
			setPartName(getSecondaryId() + " " + getFileName());
			return;
		}
		float[] imageDiffArray = new float[savedLen];
		for (int i = 0; i < savedLen; i++) {
			imageDiffArray[i] = newArray[i] - savedArray[i];
		}
		imageDiffModel = ImageModelFactory.getImageModel(imageModel.getFileName() + " - "
				+ imageSavedModel.getFileName(), imageModel.getWidth(),
				imageModel.getHeight(), imageDiffArray);
		imageDiffModel.reset(imageModel.getFileName() + " - "
				+ imageSavedModel.getFileName(), imageModel.getWidth(),
				imageModel.getHeight(), imageDiffArray);
		// TODO: KE: Consider keeping the full statistics so the zoomed
		// image looks the same as the unzoomed
		zoomStatistics = imageDiffModel.getStatistics(image.getImageRect());
		long elapsed = System.currentTimeMillis() - start;
		updateStatusLabel(getFileName() + " - " + fileNameSaved + " took "
				+ elapsed + " ms");
		setPartName("Difference " + getFileName());
		// Put the min and max in the text boxes only if Autoscale

	}

	private void setPartName(String name) {
		if (parentPart instanceof ImageView) {
			if (name == null) name = getSecondaryId() + " " + getFileName();
			((ImageView)parentPart).setPartName(name);
		}
	}

	/**
	 * update status label asynchronously
	 * 
	 * @param _status
	 *            - status to display
	 */
	public void updateStatusLabel(String _status) {
		final String status = _status;
		display.asyncExec(new Runnable() {
			public void run() {
				if (controls != null)
					controls.setStatusText(status);
			}
		});
	}

	/**
	 * set the list of peaks to display
	 * 
	 * @param vals
	 *            - list of peaks as pairs of [y,z] coordinates
	 */
	public void setPeaks(Vector<Float> vals) {
		peaks = vals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// These events come from the SampleController and the names are defined
		// in fable.framework.navigator.toolBox.IVarKeys
		// DEBUG
		// System.out.println("ImageView.propertyChange: " + getSecondaryId());
		if (evt.getProperty().equals(IVarKeys.SET_CURRENTFILE_EVENT)) {
			// DEBUG
			// System.out.println("  SET_CURRENTFILE_EVENT");
			Object val = evt.getNewValue();
			
			/*
			 * first load current file and display it
			 */
			try {
				loadModel(ImageModelFactory.getImageModel(val));
			} catch (Throwable e) {
				FableLogger.error("Cannot load image "+val, e);
			}
			image.initAndDisplayImage();
			controls.setFileNumberText(Integer.toString(controller
					.getCurrentFileIndex()));
			/*
			 * gain time by trying to read the files around the current
			 * selection +-3 into memory already as a system job (i.e. invisible
			 * to the user) I don't know if this is responsible for a memory
			 * leak ... there seems to be one
			 */
			final int sampleIndex = controller.getCurrentsample()
					.getFilteredfiles().indexOf(val);
			int sampleSize = controller.getCurrentsample().getFilteredfiles()
					.size();

			if (sampleIndex >= 0 && sampleIndex < sampleSize && !jobRunning) {
				// logger.debug("read files ahead");
				final int fileReadFrom = Math.max(0, sampleIndex - 3);
				final int fileReadTo = Math
						.min(sampleIndex + 3, sampleSize - 1);
				jobRunning = true;
				Job job = new Job("Read files ahead ") {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Read files ahead", 7);
						// logger.debug("start job to read file " + fileReadFrom
						// + " to " + fileReadTo + " ahead");
						/*
						 * start off with a sleep to give the main thread time
						 * to read the file and display the image
						 */
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
							FableUtils.excNoTraceMsg(this,
									"Reading files ahead interrupted", ex);
						}
//						FableJep fableJep;
						try {
//							fableJep = FableJep.getFableJep();
							for (int i = fileReadFrom; i <= fileReadTo; i++) {
								/*
								 * do not read the current file in the
								 * imageview, it could be in the process of
								 * being read anyway it should be loaded by the
								 * main thread.
								 */
								if (i >= 0
										&& i < (controller.getCurrentsample()
												.getFilteredfiles().size())
										&& i != sampleIndex) {
									controller.getCurrentsample()
											.getFilteredfiles().get(i)
											.readImageAsFloat();
								}
								monitor.worked(1);
							}
//							fableJep.close();
						} catch (Throwable ex) {
							/*
							 * FableUtils.excNoTraceMsg(this,
							 * "Unable to access fabioFile while " +
							 * "reading files ahead", ex);
							 */
						}
						monitor.done();
						jobRunning = false;
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		
		if (image != null) {
			image.dispose();
		}
		// Remove this instance from the controller's listener list. It doesn't
		// matter if it is not there for this instance.
		controller.removePropertyChangeListener(this);
		if (workbenchListener != null) {
			PlatformUI.getWorkbench()
					.removeWorkbenchListener(workbenchListener);
			workbenchListener = null;
		}
		if (parentPart.getSite() != null) {
			parentPart.getSite().getWorkbenchWindow().getSelectionService()
					.removeSelectionListener(this);
		}
		if (controls!=null) controls.dispose();
	}

	/**
	 * Transfer some of settings from another instance to this one. Typically
	 * used when zooming, copying, doing slices, etc. The values transferred
	 * are:
	 * <ul>
	 * <li>orientation</li>
	 * <li>coordinates</li>
	 * <li>palette</li>
	 * <li>statistics (minimum, maximum, mean)</li>
	 * <li>autoScale</li>
	 * <li>userMinimum, userMaximum</li>
	 * <li>peaks and peaksOn</li>
	 * </ul>
	 * 
	 * @param source
	 *            The source instance.
	 */
	public void transferSelectedSettings(ImageComponent src) {
		this.setOrientation(src.getOrientation());
		this.setCoordOrigin(src.getCoordOrigin());
		if (src.getCoordOrigin() == COORD_CUSTOM) {
			this.getCoordinates().reset(src.getCoordOrigin(),
					src.getCoordinates().getX0(), src.getCoordinates().getY0(),
					src.getCoordinates().getPixelWidth(),
					src.getCoordinates().getPixelHeight(),
					src.getCoordinates().getXName(),
					src.getCoordinates().getYName());
		} else {
			this.resetCoordinates();
		}
		this.setPalette(src.getPalette());
		// This breaks the min and max in the side plot,
		// since this method runs after the side plot it updated.
		//this.setUserMinimum(src.getUserMinimum());
		//this.setUserMaximum(src.getUserMaximum());
		this.setPeaks(src.getPeaks());
		this.setPeaksOn(src.isPeaksOn());
		
		// This must be done last
		//this.setStatistics(src.getStatistics(), src.getAutoscale());
	
	}

	/**
	 * @author SUCHET
	 * @date Feb, 11 2008
	 * @description : called from peaksearch to show peaks and to force button
	 *              peaks to be selected
	 */
	public void initWithShowPeaks() {
		if (controls == null)
			return;
		setPeaksOn(true);
		controls.firePeaksUpate();
	}

	/**
	 * Reset the coordinates to reflect the current image and orientation. Does
	 * not reset custom coordinates.
	 */
	public void resetCoordinates() {
		if (coordOrigin != COORD_CUSTOM) {
			coordinates.reset(coordOrigin, image.getOrientedOrigRect().width,
					image.getOrientedOrigRect().height);
		}
	}

	/**
	 * @return the coordOrigin
	 */
	public int getCoordOrigin() {
		return coordOrigin;
	}

	/**
	 * @param coordOrigin
	 *            the coordOrigin to set
	 */
	public void setCoordOrigin(int coordOrigin) {
		if (coordOrigin < 0 || coordOrigin >= coordNameValues.length) {
			return;
		}
		if (this.coordOrigin != coordOrigin) {
			this.coordOrigin = coordOrigin;
			if (controls != null) {
				controls.setCoordinate(coordOrigin);
			}
		}
	}

	/**
	 * @return the orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation
	 *            the orientation to set
	 */
	public void setOrientation(int orientation) {
		if (orientation < 0 || orientation >= 8) return;
		if (orientation == this.orientation) return;
		this.orientation = orientation;
		if (controls != null) {
			controls.setOrientation(orientation);
		}
		// Reset the coordinates
		resetCoordinates();
		// Clear canvas because image dimensions could change
		image.clearCanvas();
		image.initAndDisplayImage();
		
		final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(PreferenceConstants.P_ORIENT, orientation);
	}

	/**
	 * @return the palette
	 */
	public PaletteData getPalette() {
		return palette;
	}

	/**
	 * Creates a palette in the palettes array if it has not been created yet,
	 * sets that palette, and redisplays the image. New palettes must be added
	 * here, in IImagesVarKeys, and in PaletteUtils.
	 * 
	 * @see fable.imageviewer.internal#IImagesVarKeys for possible indices.
	 * @param index
	 *            The index of the palette in the palettes array.
	 */
	public void setPalette(int index) {

		this.palette = PaletteFactory.getPallete(index);
		if (controls != null) {
			controls.setImageScheme(index);
		}
		paletteIndex = index;
		// clearCanvas();
		image.initAndDisplayImage(); //Using palette is in init, so not enough to display image
		
		final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(PreferenceConstants.P_PALETTE, index);

	}

	/**
	 * Gets the statistics.
	 * 
	 * @return The statistics.
	 * @remark as float[] {min, max, mean}.
	 */
	public Statistics getStatistics() {
		return zoomStatistics;
	}

	/**
	 * Sets the statistics and autoScale.
	 * 
	 * @param statistics
	 *            The new values.
	 * @remark as float[] {min, max, mean}.
	 */
	public void setStatistics(Rectangle imageRect, boolean autoScale) {
		if (controls != null) {
			controls.setAutoScale(autoScale);
		}
		setStatistics( imageRect );
	}

	/**
	 * Sets the statistics.
	 * 
	 * @param statistics
	 *            The new values.
	 * @remark as float[] {min, max, mean}.
	 */
	public void setStatistics(Rectangle imageRect) {
		zoomStatistics = getImageModel().getStatistics( imageRect );
		// Set the UI controls
		if (controls != null) {
			controls.setMinMaxValueText2(zoomStatistics);
		}
		if (image != null) {
			image.initAndDisplayImage();
		}
	}

	/**
	 * @return the filename
	 */
	public String getFileName() {
		String fileName = null;
		if (imageModel != null) {
			fileName = imageModel.getFileName();
		}
		return (fileName == null) ? "" : fileName;
	}

	/**
	 * @return the imageModel
	 */
	public ImageModel getImageModel() {
		return imageModel;
	}

	/**
	 * @param imageModel
	 *            The imageModel to set.
	 */
	public void setImageModel(ImageModel imageModel) {
		this.imageModel = imageModel;
	}

	/**
	 * @return the imageDiffModel
	 */
	public ImageModel getImageDiffModel() {
		return imageDiffModel;
	}

	/**
	 * @param imageDiffModel
	 */
	public void setImageDiffModel(ImageModel imageDiffModel) {
		this.imageDiffModel = imageDiffModel;
	}

	/**
	 * @return the imageSavedModel
	 */
	public ImageModel getImageSavedModel() {
		return imageSavedModel;
	}

	/**
	 * @param imageSavedModel
	 *            the imageSavedModel to set
	 */
	public void setImageSavedModel(ImageModel imageSavedModel) {
		this.imageSavedModel = imageSavedModel;
	}

	/**
	 * @return the coordinates
	 */
	public Coordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Gets the short name of the current coordinate origin.
	 * 
	 * @return the short name.
	 */
	public String getCoordinatesName() {
		if (coordOrigin < 0 || coordOrigin > 4) {
			return "Unknown";
		}
		// Take the part up to the first whitespace or .
		return coordNameValues[coordOrigin][0].split("[\\s\\.]", 2)[0];
	}

	/**
	 * @return the controller
	 */
	public SampleController getController() {
		return controller;
	}

	/**
	 * @return the peaksOn
	 */
	public boolean isPeaksOn() {
		return peaksOn;
	}

	/**
	 * @param peaksOn
	 *            the peaksOn to set
	 */
	public void setPeaksOn(boolean peaksOn) {
		this.peaksOn = peaksOn;
		// Set the button
		controls.setPeaks(peaksOn);
	}

	/**
	 * @return the psfOn
	 */
	public boolean isPSFOn() {
		return psfOn;
	}

	/**
	 * @param psfOn
	 *            the psfOn to set
	 */
	public void setPSFOn(boolean psfOn) {
		this.psfOn = psfOn;
		// Set the button
		controls.setPSF(psfOn);
	}

	/**
	 * @return the peakMarkerSize
	 */
	public int getPeakMarkerSize() {
		return peakMarkerSize;
	}

	/**
	 * @param peakMarkerSize
	 *            the peakMarkerSize to set
	 */
	public void setPeakMarkerSize(int peakMarkerSize) {
		this.peakMarkerSize = peakMarkerSize;
	}

	/**
	 * @return the controls
	 */
	public ImageComponentUI getControls() {
		return controls;
	}

	/**
	 * @param controls
	 *            the controls to set
	 */
	public void setControls(ImageComponentUI controls) {
		this.controls = controls;
	}

	/**
	 * @return the image
	 */
	public ImageComponentImage getImage() {
		return image;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(ImageComponentImage image) {
		this.image = image;
	}

	/**
	 * Sets the zoomSelection.
	 * 
	 * @param zoomSelection
	 */
	public void setZoomSelection(ZoomSelection zoomSelection) {
		this.zoomSelection = zoomSelection;
	}

	/**
	 * @return the zoomSelection.
	 */
	public ZoomSelection getZoomSelection() {
		return zoomSelection;
	}

	/**
	 * @return the peaks
	 */
	public Vector<Float> getPeaks() {
		return peaks;
	}

	/**
	 * @return the imageDiffOn
	 */
	public boolean isImageDiffOn() {
		return imageDiffOn;
	}

	/**
	 * @param imageDiffOn
	 *            the imageDiffOn to set
	 */
	public void setImageDiffOn(boolean imageDiffOn) {
		this.imageDiffOn = imageDiffOn;
		if (imageDiffOn)
			calcImageDiff();
		else if (parentPart instanceof ImageView ){
			 
			setPartName(getSecondaryId() + " " + getFileName());
		}
		image.setImageChanged(true);
		image.initAndDisplayImage();
	}

	/**
	 * @return the minimum
	 */
	public float getMinimum() {
		return zoomStatistics.getMinimum();
	}

	/**
	 * @return the maximum
	 */
	public float getMaximum() {
		return zoomStatistics.getMaximum();
	}

	/**
	 * @return the mean
	 */
	public float getMean() {
		return zoomStatistics.getMean();
	}

	/**
	 * @return the userMinimum
	 */
	public Float getUserMinimum() {
		return userMinimum;
	}

	/**
	 * @param userMinimum
	 *            the userMinimum to set
	 * @return false if value is not changed 
	 */
	public boolean setUserMinimum(float userMinimum) {
		if( this.userMinimum == userMinimum )
			return false;
		this.userMinimum = userMinimum;
		if (controls != null) {
			controls.setUserMinimumText(userMinimum);
			controls.setUserMinimumScale(userMinimum);
		}
		return true;
	}

	/**
	 * @return the userMaximum
	 */
	public Float getUserMaximum() {
		return userMaximum;
	}

	/**
	 * @param userMaximum
	 *            the userMaximum to set
	 * @return false if value is not changed 
	 */
	public boolean setUserMaximum(float userMaximum) {
		if( this.userMaximum == userMaximum )
			return false;
		this.userMaximum = userMaximum;
		if (controls != null) {
			controls.setUserMaximumText(userMaximum);
			controls.setUserMaximumScale(userMaximum);
		}
		return true;
	}

	/**
	 * @return the x0Save
	 */
	public double getX0Save() {
		return x0Save;
	}

	/**
	 * @param save
	 *            the x0Save to set
	 */
	public void setX0Save(double save) {
		x0Save = save;
	}

	/**
	 * @return the y0Save
	 */
	public double getY0Save() {
		return y0Save;
	}

	/**
	 * @param save
	 *            the y0Save to set
	 */
	public void setY0Save(double save) {
		y0Save = save;
	}

	/**
	 * @return the pixelWidthSave
	 */
	public double getPixelWidthSave() {
		return pixelWidthSave;
	}

	/**
	 * @param pixelWidthSave
	 *            the pixelWidthSave to set
	 */
	public void setPixelWidthSave(double pixelWidthSave) {
		this.pixelWidthSave = pixelWidthSave;
	}

	/**
	 * @return the pixelHeightSave
	 */
	public double getPixelHeightSave() {
		return pixelHeightSave;
	}

	/**
	 * @param pixelHeightSave
	 *            the pixelHeightSave to set
	 */
	public void setPixelHeightSave(double pixelHeightSave) {
		this.pixelHeightSave = pixelHeightSave;
	}

	/**
	 * @return the xNameSave
	 */
	public String getXNameSave() {
		return xNameSave;
	}

	/**
	 * @param nameSave
	 *            the xNameSave to set
	 */
	public void setXNameSave(String nameSave) {
		xNameSave = nameSave;
	}

	/**
	 * @return the yNameSave
	 */
	public String getYNameSave() {
		return yNameSave;
	}

	/**
	 * @param nameSave
	 *            the yNameSave to set
	 */
	public void setYNameSave(String nameSave) {
		yNameSave = nameSave;
	}

	/**
	 * @return the paletteIndex
	 */
	public int getPaletteIndex() {
		return paletteIndex;
	}

	/**
	 * @param paletteIndex
	 *            the paletteIndex to set
	 */
	public void setPaletteIndex(int paletteIndex) {
		this.paletteIndex = paletteIndex;
	}

	/**
	 * @param palette
	 *            the palette to set
	 */
	public void setPalette(PaletteData palette) {
		this.palette = palette;
	}

	/**
	 * @return the display
	 */
	public Display getDisplay() {
		return display;
	}

	/**
	 * @return the secondary ID of this instance.
	 */
	public String getSecondaryId() {
		String id2 = null;
		try {
			if (parentPart instanceof IViewPart) {
				id2  = ((IViewPart)parentPart).getViewSite().getSecondaryId();
			} else {
				id2 = ImageComponent.SECONDARY_ID_MAIN;
			}
		} catch (Exception ex) {
			// Do nothing
		}
		return id2;
	}
	
	public Object getPartName() {
		return parentPart.getPartName();
	}
	
	
	public IActionBars getActionBars() {
		return provider.getActionBars();
	}
	
	public String toString() {
		return "Image Component for "+this.parentPart.getPartName();
	}

	public IWorkbenchPart3 getParentPart() {
		return this.parentPart;
	}

	public void setStatusLabel(Text statusLabel) {
		this.statusLabel = statusLabel;
	}
	
	public void setPlotTitle(final String title) {
		controls.setTitle(title);
	}

	public void setEditorInput(String filePath) throws Throwable {
		
		if (parentPart instanceof IReusableEditor) {
			
			final IEditorInput in = EclipseUtils.getEditorInput(filePath);
			IReusableEditor ed = (IReusableEditor) parentPart;
			ed.setInput(in);
			
		} else {
			ImageModel model = ImageModelFactory.getImageModel(filePath);
			loadModel(model);
		}
	}
}
