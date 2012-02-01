/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.actions;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.dawb.common.ui.views.HeaderTableView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.imageprint.FableImagePrinter;
import fable.framework.imageprint.ImagePrintPreviewDialog;
import fable.framework.imageprint.ImagePrintSetupDialog;
import fable.framework.imageprint.ImagePrintUtils;
import fable.framework.imageprint.PrintSettings;
import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.ImageSelection;
import fable.framework.toolbox.SWTUtils;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.component.ImageComponentImage;
import fable.imageviewer.component.ImageComponentUI;
import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.rcp.Activator;

public class ImageViewActions implements IImagesVarKeys {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageViewActions.class);
	
	/**
	 * A reference to the instance of ImageViewer that owns this image.
	 */
	ImageComponent iv = null;
	/**
	 * A reference to the class that manages the SWT controls for this view.
	 */
	private ImageComponentUI controls;
	/**
	 * A reference to the class that manages the SWT image for this view.
	 */
	public ImageComponentImage image = null;
	public Action controlPanelAction;
	public Action drawLegendAction;
	public Action resetMinMaxAction;
	public ZoomApplyAction zoomApplyAction;
	public ZoomAreaAction zoomAreaAction;
	public ZoomLineAction zoomLineAction;
	public ZoomNoneAction zoomNone;
	public ZoomProfileAction zoomProfileAction;
	public ZoomReliefAction zoomReliefAction;
	public ZoomRockingAction zoomRockingAction;
	public Action showHeaderTableAction;
	public ResetZoomAction resetZoomAction;
	public ImageCopyAction copyImageAction;
	public SetDifferenceAction setDifferenceAction;
	public DisplayDifferenceAction displayDifferenceAction;
	public Slice1DAction slice1DAction;
	public Slice2DAction slice2DAction;
	public Action printSetupAction;
	public Action printPreviewAction;
	public Action printAction;
	public Action copyAction;
	public ImageInfoAction imageInfoAction;
	public InputSummaryAction inputSummaryAction;

	public ImageViewActions(ImageComponent ivIn, ImageComponentUI controlsIn,
			ImageComponentImage imageIn) {
		iv = ivIn;
		controls = controlsIn;
		image = imageIn;

		// Control panel
		controlPanelAction = new Action("Intensity Information",
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				controls.setControlCompositeShowing(!controls.getControlCompositeShowing());
				setChecked(controls.getControlCompositeShowing());
				
			}
		};
		controlPanelAction.setChecked(controls.getControlCompositeShowing());
		controlPanelAction.setToolTipText("Toggle intensity and title");
		controlPanelAction.setImageDescriptor(Activator
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						BTN_IMG_SETTINGS));

		// Legend on/off
		drawLegendAction = new Action("Show legend",
				IAction.AS_RADIO_BUTTON) {

			@Override
			public void run() {
				controls.setLegendShowing(!controls.getLegendShowing());				
				setChecked(controls.getLegendShowing());
				
			}
		};
		drawLegendAction.setChecked(controls.getLegendShowing());
		controlPanelAction.setToolTipText("Show legend");
		controlPanelAction.setImageDescriptor(Activator
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						BTN_IMG_SETTINGS));
		
		// Reset max min
		resetMinMaxAction = new Action("Reset Min Max Intensity") {
			public void run() {
				if (iv == null) {
					return;
				}
				iv.setUserMinimum(iv.getMinimum(), true);
				iv.setUserMaximum(iv.getMaximum(), true);
				if(iv.getImage() != null) {
					iv.getImage().displayImage();
				}
			}
		};
		resetMinMaxAction.setToolTipText("Reset the minimum and maximum "
				+ "for manual intensity scaling to be the image limits");

		// Reapply current zoom (when switching between editors)
		zoomApplyAction = new ZoomApplyAction("Reapply Zoom");
		zoomApplyAction.set(iv);
		zoomApplyAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_update.png"));
		zoomApplyAction.setToolTipText("Apply the zoom again. This updates the side plots with the last zoom function.");
		
		// Zoom area
		zoomAreaAction = new ZoomAreaAction("Zoom Area");
		zoomAreaAction.set(iv);
		ZoomSelection zoomSelection = iv.getZoomSelection();
		if (zoomSelection == ZoomSelection.AREA)
			zoomAreaAction.setChecked(true);
		zoomAreaAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_box.gif"));

		// Zoom line
		zoomLineAction = new ZoomLineAction("Zoom Line");
		zoomLineAction.set(iv);
		if (zoomSelection == ZoomSelection.LINE)
			zoomLineAction.setChecked(true);
		zoomLineAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_line.gif"));

		// Zoom Profile
		zoomProfileAction = new ZoomProfileAction("Zoom Profile");
		zoomProfileAction.set(iv);
		if (zoomSelection == ZoomSelection.PROFILE)
			zoomProfileAction.setChecked(true);
		zoomProfileAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_profile.gif"));

		// Zoom Relief
		zoomReliefAction = new ZoomReliefAction("Zoom Relief");
		zoomReliefAction.set(iv);
		if (zoomSelection == ZoomSelection.RELIEF)
			zoomReliefAction.setChecked(true);
		zoomReliefAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_relief.png"));

		// Zoom Rocking
		zoomRockingAction = new ZoomRockingAction("Zoom Rocking");
		zoomRockingAction.set(iv);
		if (zoomSelection == ZoomSelection.ROCKINGCURVE) zoomRockingAction.setChecked(true);
		zoomRockingAction.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_rocking.png"));
		
		zoomNone = new ZoomNoneAction("Zoom None");
		zoomNone.set(iv);
		if (zoomSelection == ZoomSelection.NONE) zoomNone.setChecked(true);
		zoomNone.setImageDescriptor(Activator.getImageDescriptor("/icons/zoom_none.gif"));

		// Reset Zoom
		resetZoomAction = new ResetZoomAction("Reset Zoom");
		resetZoomAction.set(iv);
		resetZoomAction.setToolTipText("Reset zoom to original image size");
		
		showHeaderTableAction = new Action("Show Meta Data") {
			public void run() {
				try {
					EclipseUtils.getPage().showView(HeaderTableView.ID);
				} catch (PartInitException e) {
					logger.error("Canot open "+HeaderTableView.ID, e);
				}
			}
		};
		showHeaderTableAction.setImageDescriptor(Activator.getImageDescriptor("/icons/header_table.gif"));

		// Copy Image
		copyImageAction = new ImageCopyAction("Copy Image");
		copyImageAction.set(iv);
		copyImageAction
				.setToolTipText("Show a copy of this view in another view");

		// Set Background
		setDifferenceAction = new SetDifferenceAction("Set Difference");
		setDifferenceAction.set(iv);
		setDifferenceAction
				.setToolTipText("Set current image as reference image to subtract");

		// Display Difference
		displayDifferenceAction = new DisplayDifferenceAction(
				"Display Difference");
		displayDifferenceAction.set(iv);
		displayDifferenceAction
				.setToolTipText("Display image with reference image subtracted");

		// 1D Slice
		slice1DAction = new Slice1DAction("1D Slice");
		slice1DAction.set(iv);
		slice1DAction
				.setToolTipText("Make a new 2D image by stacking the zoomed line of the selected images");

		// 2D Slice
		slice2DAction = new Slice2DAction("2D Slice");
		slice2DAction.set(iv);
		slice2DAction
				.setToolTipText("Make a new 2D image by stacking the zoomed area of the selected images");

		// Print Setup
		printSetupAction = new Action("Print Setup") {
			@Override
			public void run() {
				
				Image image = controls.getImage().getImage();
				if (image == null) {
					return;
				}
				
				ImagePrintSetupDialog dialog = new ImagePrintSetupDialog(iv.getDisplay().getActiveShell(), 
						                                                 image,
						                                                 FableImagePrinter.getSettings());
				PrintSettings settings = dialog.open();
				if (settings != null) {
					// Dialog was not canceled
					FableImagePrinter.setSettings(settings);
				}
			}
		};

		// Print Preview
		printPreviewAction = new Action("Print Preview") {
			@Override
			public void run() {
				Image image = controls.getImage().getImage();
				if (image == null) {
					return;
				}
				ImagePrintPreviewDialog dialog = new ImagePrintPreviewDialog(iv
						.getDisplay().getActiveShell(), image,
						FableImagePrinter.getSettings());
				PrintSettings settings = dialog.open();
				if (settings != null) {
					// Dialog was not canceled
					FableImagePrinter.setSettings(settings);
				}
			}
		};

		// Print
		printAction = new Action("Print") {
			@Override
			public void run() {
				Image image = controls.getImage().getImage();
				if (image == null) {
					return;
				}
				ImagePrintUtils.dialogPrintImage(iv.getDisplay()
						.getActiveShell(), image, iv.getDisplay()
						.getActiveShell().getDisplay().getDPI(),
						FableImagePrinter.getSettings());
			}
		};

		// Copy (to clipboard)
		copyAction = new Action("Copy") {
			@Override
			public void run() {
				Image image = controls.getImage().getImage();
				if (image == null) {
					return;
				}
				try {
					if (true) {
						// Use AWT
						final BufferedImage awtImage = SWTUtils
								.convertToAWT(image.getImageData());
						if (awtImage == null) {
							FableUtils.errMsg(this,
									"Could not convert SWT image to AWT image");
							return;
						}
						java.awt.datatransfer.Clipboard awtClipboard = Toolkit
								.getDefaultToolkit().getSystemClipboard();
						awtClipboard.setContents(new ImageSelection(awtImage),
								null);
//					} else {
//						// Use SWT (Doeesn't work well on most platforms besides
//						// Windows and needs this kludge for Windows)
//						// Make a new Image using the display defaults instead
//						// of this grayscale, indexed-palette image so it will
//						// be more acceptable by other applications
//						Rectangle bounds = image.getBounds();
//						Image image1 = new Image(iv.getDisplay(), bounds.width,
//								bounds.height);
//						GC gc = new GC(image1);
//						gc.drawImage(image, 0, 0);
//						gc.dispose();
//						ImageTransfer transfer = ImageTransfer.getInstance();
//						Clipboard clipboard = new Clipboard(iv.getDisplay());
//						clipboard.setContents(new Object[] { image1
//								.getImageData() }, new Transfer[] { transfer });
//						image1.dispose();
//						clipboard.dispose();
					}
				} catch (Throwable t) {
					FableUtils.excMsg(this, "Problem copying to clipboard", t);
				}
			}
		};

		// Image Info
		imageInfoAction = new ImageInfoAction("Image Info");
		imageInfoAction.set(iv);
		imageInfoAction.setToolTipText("Display image info");

		// Input Summary
		inputSummaryAction = new InputSummaryAction("Input Summary");
		inputSummaryAction.set(iv);
		inputSummaryAction
				.setToolTipText("Display a summary of mouse operations");
	}

}
