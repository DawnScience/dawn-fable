/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.GridUtils;
import fable.imageviewer.component.ActionsProvider;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.component.ImageComponentImage;
import fable.imageviewer.component.ImagePlay;
import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.internal.ZoomSelection;

/**
 * ImageView implements a view to display an image using the SWT Image widget.
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
public class ImageView extends ViewPart implements IImagesVarKeys, ActionsProvider {
	/**
	 * Plug-in ID.
	 */
	public static final String ID = "fable.imageviewer.views.ImageView";
	
	/**
	 * The object which does the work, can be used in different view parts.
	 */
	private ImageComponent imageComponent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		
		// Trap any view created with a secondary ID of null. These are created
		// by Eclipse e.g. via Window | Show View.
		String secondaryId = getSecondaryId();
		if (secondaryId == null) {
			// Hide this one and create one with a known secondary ID of
			// SECONDARY_ID_MAIN. We need to do it asynchronously as we can't
			// close the view until the create code has finished
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = getViewSite().getPage();
					if (page == null) {
						return;
					}
					// Destroy the one with null secondary ID
					page.hideView(ImageView.this);
					// Open one with SECONDARY_ID_MAIN
					try {
						page.showView(ImageView.ID, ImageComponent.SECONDARY_ID_MAIN,
								IWorkbenchPage.VIEW_CREATE);
					} catch (PartInitException ex) {
						FableUtils.excMsg(this,
								"Error creating view with known secondary ID",
								ex);
					}
				}
			});
			// Return now as we're done with the null secondary-id view
			return;
		}

		// Set the part name using the secondary ID
		setPartName(secondaryId + " Image View");
		
        this.imageComponent = new ImageComponent(this);
        imageComponent.createPartControl(parent);
        GridUtils.removeMargins(parent);


		// Save first main view as main view
		if (secondaryId.equals(ImageComponent.SECONDARY_ID_MAIN)) {
	        ImagePlay.setView(imageComponent);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (imageComponent!=null) imageComponent.setFocus();
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
	/**
	 * @return the secondary ID of this instance.
	 */
	public String getSecondaryId() {
		String id2 = null;
		try {
			id2 = getViewSite().getSecondaryId();
		} catch (Exception ex) {
			// Do nothing
		}
		return id2;
	}

	public ImageComponentImage getImage() {
		return imageComponent.getImage();
	}

	public void setPartName(final String name) {
		super.setPartName(name);
	}

	public void transferSelectedSettings(ImageComponent iv) {
		imageComponent.transferSelectedSettings(iv);
	}

	public ImageComponent getImageComponent() {
		return imageComponent;
	}

	@Override
	public IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}

	public void setZoomSelection(ZoomSelection area) {
		getImageComponent().setZoomSelection(area);
	}
}
