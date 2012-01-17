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

import java.text.NumberFormat;
import java.text.ParseException;

import org.dawb.common.ui.image.PaletteFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import fable.framework.logging.FableLogger;
import fable.framework.toolbox.CheckableActionGroup;
import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.GridUtils;
import fable.framework.toolbox.MenuAction;
import fable.framework.toolbox.SWTUtils;
import fable.imageviewer.actions.ImageViewActions;
import fable.imageviewer.internal.Coordinates;
import fable.imageviewer.internal.CustomCoordinatesDialog;
import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.preferences.PreferenceConstants;
import fable.imageviewer.rcp.Activator;

/**
 * This class manages the SWT controls for the ImageView. It holds the
 * imageCanvas, but does not manage things related to the imageCanvas. That is
 * done in the ImageViewImage class.
 */
public class ImageComponentUI implements IImagesVarKeys {
	/**
	 * Flag to add extra actions to the context menu for debugging.
	 */
	private static final boolean ADD_DEBUG_ACTIONS = false;
	public static final int DEFAULT_MARKER_SIZE = 3;
	/**
	 * A reference to the instance of ImageViewer that owns these controls.
	 */
	ImageComponent iv = null;
	/**
	 * A reference to the class that manages the SWT image for this view.
	 */
	private ImageComponentImage image = null;
	/**
	 * The Composite that is the control panel.
	 */
	private Composite controlComposite = null;
	/**
	 * Indicates when the control panel is visible.
	 */
	private boolean controlCompositeShowing = true;
	/**
	 * Indicates when the legend is visible.
	 */
	private boolean legendShowing = false;
	/**
	 * The Display for this view.
	 */
	private Display display;
	private MenuAction coordCombo, orientCombo, lutCombo;
	private Action aspectButton,peaksButton,autoscaleButton;
	
	private CCombo peakMarkerSizeText = null;
	private Text userMinimumText = null;
	private Text userMaximumText = null;
	private Text fileNumberText = null;
	private Text statusLabel = null;
	private CLabel titleLabel;
	private Canvas imageCanvas;
	private Composite statusGroup;
	static private NumberFormat decimalFormat = NumberFormat
			.getNumberInstance();
	/**
	 * Flag that indicates whether the custom saved parameters have been
	 * initialized or not. For internal use.
	 */
	static boolean customSavedParametersInitialized = false;
	private ImageViewActions actions;

	/**
	 * Constructor.
	 * 
	 * @param imageView
	 */
	public ImageComponentUI(ImageComponent imageView) {
		this.iv = imageView;
	}
	
	public String toString() {
		return "Image UI for "+this.iv.getParentPart().getPartName();
	}
	
	private boolean off = false;
	private Composite titleComponent;

	/**
	 * Creates all the controls.
	 * 
	 * @param parent
	 */
	public void createControls(Composite parent) {
		
		if (iv == null) return;
		
		// Create the ImageViewImage here, but do not initialize it until the
		// imageCanvas is created
		image = new ImageComponentImage(iv, this);

		// Create the actions
		actions = new ImageViewActions(iv, this, image);

		iv.setZoomSelection(ZoomSelection.AREA); /* default is area selection */

		display = iv.getDisplay();
		parent.setLayout(new GridLayout(1, false));
		
		this.titleComponent = new Composite(parent, SWT.LEFT);
		titleComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleComponent.setLayout(new GridLayout(2, false));
		
		// Add a title and set it invisible
		this.titleLabel = new CLabel(titleComponent, SWT.LEFT);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Link hide = new Link(titleComponent, SWT.NONE);
		hide.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		hide.setText("<a>hide</a>");
		hide.setToolTipText("Hide title and intensity information.");
		hide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				actions.controlPanelAction.run();
			}
		});
		
		GridUtils.setVisible(titleComponent, false);

		if (statusLabel==null) {
			statusLabel = new Text(parent, SWT.LEFT);
			statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
			statusLabel.setEditable(false);
			statusLabel.setBackground(parent.getBackground());
		}
		
		// Add a listener to the Image Navigator Sample Controller only for the
		// Main view
		if (iv.getSecondaryId().equals(ImageComponent.SECONDARY_ID_MAIN)) {
			iv.getController().addPropertyChangeListener(iv);
		}
		
		createZoomActions(iv.getActionBars());
		createImageControlSwitches(iv.getActionBars());
		createImageControlMenus(iv.getActionBars());
       
		GridLayout grid2Cols = new GridLayout();
		// KE: Why 2 cols ?
		// grid2Cols.numColumns = 2;
		Composite canvasComposite = new Composite(parent, SWT.NULL);
		canvasComposite.setLayout(grid2Cols);
		canvasComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		GridUtils.removeMargins(canvasComposite);

		// The imageCanvas is a field in this class but it is managed in
		// ImageViewImage
		imageCanvas = new Canvas(canvasComposite, SWT.NONE);
		// Initialize the ImageViewImage here
		image.initializeCanvas();

		/* bottom line containing status and load image controls */
		createImageControlUI(parent);

		// Create the menus
		contributeToActionBars();
		createContextMenu();
		iv.getParentPart().getSite().getWorkbenchWindow().getSelectionService()
				                               .addSelectionListener(iv);
		
	}
	
	public void setTitle(final String title) {
		if (titleLabel.isDisposed()) return;
		if (title==null) {
			GridUtils.setVisible(titleComponent, false);
		} else {
			titleLabel.setText(title);
			GridUtils.setVisible(titleComponent, true);
		}
		titleComponent.getParent().layout(new Control[]{titleComponent});
	}
	
	public void dispose() {
		if (coordCombo!=null)  coordCombo.dispose();
		if (orientCombo!=null) orientCombo.dispose();
		
		if (peakMarkerSizeText!=null) peakMarkerSizeText.dispose();
		if (userMinimumText!=null) userMinimumText.dispose();
		if (userMaximumText!=null) userMaximumText.dispose();
		if (fileNumberText!=null) fileNumberText.dispose();
		if (statusLabel!=null) statusLabel.dispose();
		if (imageCanvas!=null) imageCanvas.dispose();
		if (statusGroup!=null) statusGroup.dispose();
	}

	private void createZoomActions(IActionBars iActionBars) {
		
		final IToolBarManager toolMan = iActionBars.getToolBarManager();
		toolMan.add(new Separator(getClass().getName()+".zoomapply"));
		toolMan.add(actions.zoomApplyAction);
		toolMan.add(new Separator(getClass().getName()+".zoom"));
				
		toolMan.add(actions.zoomAreaAction);
		toolMan.add(actions.zoomLineAction);
		toolMan.add(actions.zoomProfileAction);
		toolMan.add(actions.zoomReliefAction);
		//toolMan.add(actions.zoomRockingAction);
		toolMan.add(actions.zoomNone);
	}

	private void createImageControlUI(Composite parent) {

		controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		controlComposite.setLayout(new GridLayout(7,false));
		GridUtils.removeMargins(controlComposite);
		
		// Minimum
		Label label = new Label(controlComposite, SWT.NULL);
		label.setText("Min Intensity ");
		label.setToolTipText("The minimum intensity used by the palette");
		
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		data.widthHint = 60;

		userMinimumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT);
		userMinimumText.setText("0");
		userMinimumText.setLayoutData(data);
		userMinimumText.setToolTipText("The minimum intensity used by the palette");
		userMinimumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		userMinimumText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				if (userMinimumText==null||userMinimumText.isDisposed()) return;
				if (!userMinimumText.isEnabled() || off) return;
				updateIntensity();
			}
		});
		
		final boolean isAutoScale = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSCALE);
		userMinimumText.setEnabled(!isAutoScale);

		// Maximum
		label = new Label(controlComposite, SWT.BORDER | SWT.RIGHT);
		label.setText("  Max Intensity ");
		label.setToolTipText("The maximum intensity used by the palette");
		userMaximumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT);
		userMaximumText.setLayoutData(data);
		userMaximumText.setToolTipText("The maximum intensity used by the palette");
		userMaximumText.setText("0");
		userMaximumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		userMaximumText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				if (userMaximumText==null||userMaximumText.isDisposed()) return;
				if (!userMaximumText.isEnabled() || off) return;
				updateIntensity();
			}
		});
		userMaximumText.setEnabled(!isAutoScale);
		
		// Peak marker size
		label = new Label(controlComposite, SWT.NULL);
		label.setText("  Marker Size ");
		label.setToolTipText("Set the size of the markers used to mark "
				+ "peaks (must be odd)");

		peakMarkerSizeText = new CCombo(controlComposite, SWT.RIGHT|SWT.BORDER|SWT.READ_ONLY);
		peakMarkerSizeText.setItems(new String[]{"1","3","5","7","9"});
		peakMarkerSizeText.setText(Integer.toString(iv.getPeakMarkerSize()));
		peakMarkerSizeText.setToolTipText("Set the size of the markers used to mark peaks (must be odd)");
		peakMarkerSizeText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		
		peakMarkerSizeText.setLayoutData(data);
		//peakMarkerSizeText.setOrientation(SWT.RIGHT);
		
		peakMarkerSizeText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				if (peakMarkerSizeText==null||peakMarkerSizeText.isDisposed()) return;
				if (!peakMarkerSizeText.isEnabled() || off) return;
				try {
					off = true;
					final String value = peakMarkerSizeText.getText();
					int val = Integer.parseInt(value);
					if (val <= 0) {
						SWTUtils.errMsg(display.getActiveShell(),
								"Marker size must be positive");
						iv.setPeakMarkerSize(val);
						peakMarkerSizeText.setText(Integer
								.toString(DEFAULT_MARKER_SIZE));
						return;
					}
					if (val % 2 == 0) {
						SWTUtils.errMsg(display.getActiveShell(),
								"Marker size must be odd to center the marker");
						iv.setPeakMarkerSize(DEFAULT_MARKER_SIZE);
						peakMarkerSizeText.setText(Integer
								.toString(DEFAULT_MARKER_SIZE));
						return;
					}
					iv.setPeakMarkerSize(val);
				} catch (NumberFormatException ex) {
					// KE: This can cause error storms
					// FableUtils.excNoTraceMsg(this,
					// "Unable to parse peak marker size", ex);
					display.beep();
				} finally {
					off = false;
				}
				image.displayImage();
			}
		});
	}
	
	private void updateIntensity() {
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				resetAutoscale(false);
				image.displayImage();
			}
		});
	}
	
	private void createImageControlSwitches(final IActionBars iActionBars) {
		
		final IContributionManager man = iActionBars.getToolBarManager();
		man.add(new Separator(getClass().getName()+".switches1"));
		man.add(actions.showHeaderTableAction);
		
		man.add(new Separator(getClass().getName()+".switches2"));
		
		// Row 2
		autoscaleButton = new Action("Autoscale Intensity", IAction.AS_CHECK_BOX ) {
			public void run() {
				resetAutoscale(true);
				image.displayImage();
			}
		};
		man.add(autoscaleButton);
		autoscaleButton.setText("Autoscale Intensity");
		autoscaleButton.setToolTipText("Scale the palette between the "
				+ "minimum and maximum intensity in the data");
		autoscaleButton.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSCALE));
		autoscaleButton.setImageDescriptor(Activator.getImageDescriptor("/icons/autoscale.png"));


		// Row 3
		aspectButton = new Action("Keep Aspect", IAction.AS_CHECK_BOX ) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_KEEPASPECT, isChecked());
				image.clearCanvas();
				image.displayImage();
			}
		};
		man.add(aspectButton);
		aspectButton.setText("Keep Aspect");
		aspectButton.setToolTipText("Keep aspect ratio when displaying image");
		
		final boolean isKeepAspect = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_KEEPASPECT);
		aspectButton.setChecked(isKeepAspect);
		aspectButton.setImageDescriptor(Activator.getImageDescriptor("/icons/aspect.gif"));

		peaksButton = new Action("Show Peaks", IAction.AS_CHECK_BOX ) {
			public void run() {
				iv.setPeaksOn(peaksButton.isChecked());
				image.displayImage();
			}
		};
		man.add(peaksButton);
		peaksButton.setText("Show Peaks");
		peaksButton.setToolTipText("Display peaks");
		peaksButton.setChecked(iv.isPeaksOn());
		peaksButton.setImageDescriptor(Activator.getImageDescriptor("/icons/chart_curve_go.png"));

	}

	private void createImageControlMenus(final IActionBars iActionBars) {
		
		final IContributionManager man = iActionBars.getMenuManager();
		man.add(new Separator(getClass().getName()+"menus"));
				
		final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		int orientation = prefs.getInt(PreferenceConstants.P_ORIENT);
		iv.setOrientation(orientation);

		// TODO Icons
		orientCombo = new MenuAction("Orientation", false);
		orientCombo.setId(getClass().getName()+orientCombo.getText());
		man.add(orientCombo);
		
		CheckableActionGroup group = new CheckableActionGroup();
		for (int i = 0; i < 8; i++) {
			final int index = i;
			final Action action = new Action(orientNameValues[i][0], IAction.AS_CHECK_BOX) {
				public void run() {
					iv.setOrientation(index);
				}
			};
			group.add(action);
			orientCombo.add(action);
			action.setChecked(index==orientation);
		}
		orientCombo.setToolTipText("Adjust the orientation with O parameters "
				+ "(o11 o12 o21 o22)");		

		orientCombo.setImageDescriptor(Activator.getImageDescriptor("/icons/orientation.gif"));
		man.add(new Separator(getClass().getName()+"orient"));
		
		// Initialize the custom saved parameters
		if (!customSavedParametersInitialized) {
			// Keep the item being processed for the possible error message
			String processing = "xName";
			try {
				iv.setXNameSave(prefs
						.getString(PreferenceConstants.P_COORD_XNAME));
				processing = "yName";
				iv.setYNameSave(prefs
						.getString(PreferenceConstants.P_COORD_YNAME));
				processing = "x0";
				iv.setX0Save(Double.parseDouble(prefs
						.getString(PreferenceConstants.P_COORD_X0)));
				processing = "y0";
				iv.setY0Save(Double.parseDouble(prefs
						.getString(PreferenceConstants.P_COORD_Y0)));
				processing = "pixelHeight";
				iv.setPixelWidthSave(Double.parseDouble(prefs
						.getString(PreferenceConstants.P_COORD_PIXELWIDTH)));
				processing = "pixelHeight";
				iv.setPixelHeightSave(Double.parseDouble(prefs
						.getString(PreferenceConstants.P_COORD_PIXELHEIGHT)));
				customSavedParametersInitialized = true;
			} catch (NumberFormatException ex) {
				FableUtils.excMsg(this, "Error setting custom coordinates"
						+ " from preferences for " + processing, ex);
			}
		}

		// Orientation
		int coordOrigin = prefs.getInt(PreferenceConstants.P_COORD);
		coordCombo = new MenuAction("Coordinates", false);
		coordCombo.setImageDescriptor(Activator.getImageDescriptor("/icons/coords.png"));
		coordCombo.setId(getClass().getName()+coordCombo.getText());
		man.add(coordCombo);
		group      = new CheckableActionGroup();
		for (int i = 0; i < 5; i++) {
			final int index = i;
			final Action action = new Action(coordNameValues[i][0], IAction.AS_CHECK_BOX) {
				public void run() {
					setCoordinateChoice(index);
				}
			};
			group.add(action);
			coordCombo.add(action);
			action.setChecked(index==coordOrigin);
		}
		coordCombo.setToolTipText("Select the origin of the coordinate system for mouse movement");
		man.add(new Separator(getClass().getName()+"coord"));

		// Default (will be zero if not found)
		if (coordOrigin == COORD_CUSTOM) {
			Coordinates coordinates = iv.getCoordinates();
			try {
				coordinates.reset(coordOrigin, iv.getX0Save(), iv.getY0Save(),
						iv.getPixelWidthSave(), iv.getPixelHeightSave(), iv
								.getXNameSave(), iv.getYNameSave());
			} catch (NumberFormatException ex) {
				FableUtils.excMsg(this, "Error setting custom coordinates", ex);
				coordinates.resetToDefault();
			}
		} else {
			iv.resetCoordinates();
		}

		
		// Palette
		int paletteIndex = prefs.getInt(PreferenceConstants.P_PALETTE);
		iv.setPalette(paletteIndex);

		lutCombo = new MenuAction("Color", false);
		lutCombo.setId(getClass().getName()+lutCombo.getText());
		lutCombo.setImageDescriptor(Activator.getImageDescriptor("/icons/color_wheel.png"));
		man.add(lutCombo);
		group      = new CheckableActionGroup();
		for (final String paletteName : PaletteFactory.PALETTES.keySet()) {
			final Action action = new Action(paletteName, IAction.AS_CHECK_BOX) {
				public void run() {
					int paletteIndex = PaletteFactory.PALETTES.get(paletteName);
					iv.setPalette(paletteIndex);
					setChecked(PaletteFactory.PALETTES.get(paletteName)==paletteIndex);
				}
			};
			group.add(action);
			lutCombo.add(action);
			action.setChecked(PaletteFactory.PALETTES.get(paletteName)==paletteIndex);
		}
		lutCombo.setToolTipText("Set the Color Map");

		man.add(new Separator(getClass().getName()+"menusend"));

	}

	protected void setCoordinateChoice(int idx) {

		int coordOriginSave = iv.getCoordOrigin();
		iv.setCoordOrigin(idx);
		if (idx == COORD_CUSTOM) {
			CustomCoordinatesDialog dlg;
			Coordinates coordinates = iv.getCoordinates();
			if (coordinates.getType() == COORD_CUSTOM) {
				// Use the current values as initial values
				dlg = new CustomCoordinatesDialog(display
						.getActiveShell(), coordinates.getX0(),
						coordinates.getY0(), coordinates
								.getPixelWidth(), coordinates
								.getPixelHeight(), coordinates
								.getXName(), coordinates.getYName());
			} else {
				// Use the static saved values as initial values
				dlg = new CustomCoordinatesDialog(display
						.getActiveShell(), iv.getX0Save(), iv
						.getY0Save(), iv.getPixelWidthSave(), iv
						.getPixelHeightSave(), iv.getXNameSave(), iv
						.getYNameSave());
			}
			boolean result = dlg.open();
			if (result) {
				iv.setX0Save(dlg.getX0());
				iv.setY0Save(dlg.getY0());
				iv.setPixelWidthSave(dlg.getPixelWidth());
				iv.setPixelHeightSave(dlg.getPixelHeight());
				iv.setXNameSave(dlg.getXName());
				iv.setYNameSave(dlg.getYName());
				coordinates.reset(iv.getCoordOrigin(), iv.getX0Save(),
						iv.getY0Save(), iv.getPixelWidthSave(), iv
								.getPixelHeightSave(), iv
								.getXNameSave(), iv.getYNameSave());
			} else {
				// Set it back to what it was (necessary for combo box)
				coordCombo.setSelected(coordOriginSave);
				// iv.setCoordOrigin(coordOriginSave);
			}
		} else {
			iv.resetCoordinates();
		}
		
		final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(PreferenceConstants.P_COORD, idx);

	}


	/**
	 * Does the logic for changing any of the autoscale parameters. Does not
	 * cause the image to be redrawn.
	 */
	public void resetAutoscale(final boolean requireTextUpdate) {
		
		final boolean isAutoScale = autoscaleButton.isChecked();
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_AUTOSCALE, isAutoScale);
		
		// Get all the current values from the controls
		userMinimumText.setEnabled(true);
		userMaximumText.setEnabled(true);
		try {
			iv.setUserMinimum(decimalFormat.parse(userMinimumText.getText()).floatValue(), requireTextUpdate);
		} catch (ParseException ex) {
			// KE: This can cause error storms
			// FableUtils.excNoTraceMsg(this,
			// "resetAutoscale: Unable to parse minimum value",
			// ex);
			FableLogger.warn("Unable to parse maximum value: "
					+ userMinimumText.getText());
		}
		try {
			iv.setUserMaximum(decimalFormat.parse(userMaximumText.getText()).floatValue(), requireTextUpdate);
		} catch (ParseException ex) {
			// KE: This can cause error storms
			// FableUtils.excNoTraceMsg(this,
			// "resetAutoscale: Unable to parse maximum value",
			// ex);
			FableLogger.warn("Unable to parse maximum value: "
					+ userMaximumText.getText());
		}
		
		if (isAutoScale) {
			// Auto scale is selected
			userMinimumText.setEnabled(false);
			userMaximumText.setEnabled(false);
		} else {
			// Auto scale is not selected
			userMinimumText.setEnabled(true);
			userMaximumText.setEnabled(true);
		}
	}

	/**
	 * Create file number text. Should only be created on the main image view.
	 * 
	 * TODO: currently this method is not being called, still have to find the
	 * best way to call it ...
	 */
	public void createFileNumberText() {
		if (fileNumberText == null) {
			fileNumberText = new Text(statusGroup, SWT.BORDER);
			fileNumberText.setToolTipText("Go to image number");
			fileNumberText.setEnabled(true);
			fileNumberText.addListener(SWT.DefaultSelection, new Listener() {
				public void handleEvent(Event event) {
					int fileIndex = Integer.parseInt(fileNumberText.getText());
					int size = iv.getController().getCurrentsample()
							.getFilteredfiles().size();
					if (fileIndex > size) {
						fileIndex = size - 1;
					}
					iv.getController().setCurrentFileIndex(fileIndex);
				}
			});
		}
	}

	/**
	 * Fill the local menus. Used for the local view menu and the context menu
	 * so they will be the same.
	 * 
	 * @param manager
	 */
	private void fillLocalMenu(IMenuManager manager) {
		manager.add(actions.controlPanelAction);
		
		manager.add(new Separator());

		manager.add(actions.drawLegendAction);

		manager.add(new Separator());

		manager.add(actions.zoomAreaAction);
		manager.add(actions.zoomLineAction);
		manager.add(actions.zoomProfileAction);
		manager.add(actions.zoomReliefAction);
		//manager.add(actions.zoomRockingAction);
		manager.add(actions.zoomNone);
		manager.add(actions.resetZoomAction);

		manager.add(new Separator());

// TODO Get this working and understand it.
//		manager.add(actions.copyImageAction);
//		manager.add(actions.setDifferenceAction);
//		manager.add(actions.displayDifferenceAction);
//
//		manager.add(new Separator());
//
//		manager.add(actions.slice1DAction);
//		manager.add(actions.slice2DAction);

		MenuManager subMenuManager = new MenuManager("Print");
		manager.add(subMenuManager);
		subMenuManager.add(actions.printSetupAction);
		subMenuManager.add(actions.printPreviewAction);
		subMenuManager.add(actions.printAction);

		subMenuManager = new MenuManager("Edit");
		manager.add(subMenuManager);
		subMenuManager.add(actions.copyAction);

		manager.add(new Separator());

		manager.add(actions.resetMinMaxAction);
		manager.add(actions.imageInfoAction);
		manager.add(actions.inputSummaryAction);

		// DEBUG
		if (ADD_DEBUG_ACTIONS) {
			manager.add(new Separator());
			Action action = new Action() {
				public void run() {
					String id2 = iv.getSecondaryId();
					SWTUtils.infoMsgAsync(null, "Secondary ID: " + id2);
				}
			};
			action.setText("Secondary ID");
			action.setToolTipText("Get secondary ID for this view");

			manager.add(action);
		}
	}

	/**
	 * Fills the local tool bar.
	 * 
	 * @param manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actions.controlPanelAction);
	}

	/**
	 * Fill the local menu and tool bar.
	 */
	private void contributeToActionBars() {
		IActionBars bars = iv.getActionBars();
		if (bars!=null) fillLocalMenu(bars.getMenuManager());
		if (bars!=null) fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Create the context menu on the image canvas.
	 */
	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillLocalMenu(mgr);
			}
		});

		// Create menu.
		Menu menu = menuMgr.createContextMenu(imageCanvas.getAccessible()
				.getControl());
		imageCanvas.getAccessible().getControl().setMenu(menu);

		// Register menu for extension.
		// getSite().registerContextMenu(menuMgr,
		// canvasComposite.getAccessible());
	}

	// Getters and setters

	/**
	 * Sets the text for the fileNumberText.
	 * 
	 * @param text
	 */
	public void setFileNumberText(String text) {
		if (fileNumberText != null)
			fileNumberText.setText(text);
	}

	/**
	 * Sets minimumText and maximumText to the given values. If the input is
	 * Statistics in the form float[] {min, max, mean}, the mean is ignored.
	 * Does not cause the image to be redisplayed.
	 * 
	 * @param minmax
	 *            The new values as float[] {min, max}.
	 */
	public void setMinMaxText(float[] minmax) {
		setUserMinimumText(Float.toString(minmax[0]));
		setUserMaximumText(Float.toString(minmax[1]));
	}

	/**
	 * Gets the current values of minimumText and maximumText.
	 * 
	 * @return The current values of minimumText and maximumText.
	 */
	public float[] getMinMaxText() {
		float[] vals = new float[2];
		try {
			vals[0] = decimalFormat.parse(userMinimumText.getText())
					.floatValue();
			vals[1] = decimalFormat.parse(userMaximumText.getText())
					.floatValue();
		} catch (ParseException ex) {
			FableUtils.excNoTraceMsg(this,
					"Unable to read minimum and/or maximum", ex);
			vals[0] = vals[1] = Float.NaN;
		}
		return vals;
	}

	public void setStatusText(String text) {
		if (statusLabel != null && !statusLabel.isDisposed()) {
			statusLabel.setText(" "+text);
			controlComposite.redraw();
		}
	}

	/**
	 * @return the imageCanvas
	 */
	public Canvas getImageCanvas() {
		return imageCanvas;
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
	 * @return the controlCompositeShowing
	 */
	public boolean getControlCompositeShowing() {
		return controlCompositeShowing;
	}

	/**
	 * @param controlCompositeShowing
	 *            the controlCompositeShowing to set
	 */
	public void setControlCompositeShowing(boolean controlCompositeShowing) {
		this.controlCompositeShowing = controlCompositeShowing;
		GridUtils.setVisible(this.controlComposite, controlCompositeShowing);
		controlComposite.getParent().layout(new Control[]{controlComposite});
		
		if (!controlCompositeShowing || (titleLabel.getText()!=null && !"".equals(titleLabel.getText()))) {
			GridUtils.setVisible(this.titleComponent, controlCompositeShowing);
			titleComponent.getParent().layout(new Control[]{titleComponent});
		}
	}

	/**
	 * @return the legendShowing
	 */
	public boolean getLegendShowing() {
		return legendShowing;
	}
	/**
	 * @param legendShowing
	 *            the legendShowing to set
	 */
	public void setLegendShowing(boolean legendShowing) {
		this.legendShowing = legendShowing;
		this.image.setLegendOn(legendShowing);
	}
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	public void setAspectSelection(boolean isChecked) {
		if (aspectButton!=null) this.aspectButton.setChecked(isChecked);
	}

	public void firePeaksUpate() {
		if (peaksButton!=null) peaksButton.run();
	}

	public void setCoordinate(int index) {
		if (coordCombo!=null) coordCombo.setSelected(index);
	}
	
	public void setOrientation(int index) {
		if (orientCombo!=null) orientCombo.setSelected(index);
	}
	
	public void setImageScheme(int index) {
		if (lutCombo!=null) lutCombo.setSelected(index);
	}

	public void setAutoScale(boolean autoScale) {
		if (autoscaleButton!=null) this.autoscaleButton.setChecked(autoScale);
	}

	public void setPeaks(boolean peaksOn) {
		if (peaksButton!=null) this.peaksButton.setChecked(peaksOn);
	}

	public void setUserMinimumText(String text) {
		if (userMinimumText!=null&&!userMinimumText.isDisposed()) {
			try {
				off = true;
				userMinimumText.setText(text);
			} finally {
				off = false;
			}
		}
	}
	public void setUserMaximumText(String text) {
		if (userMaximumText!=null&&!userMaximumText.isDisposed()) {
			try {
				off = true;
				userMaximumText.setText(text);
			} finally {
				off = false;
			}
		}
	}

	public void setStatusLabel(Text statusLabel) {
		this.statusLabel = statusLabel;
	}

}
