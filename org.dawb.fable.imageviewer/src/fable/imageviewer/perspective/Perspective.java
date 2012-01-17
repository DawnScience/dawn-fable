/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.perspective;

import org.dawb.common.ui.views.HeaderTableView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import fable.framework.navigator.views.SampleNavigatorView;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.component.ImagePlay;
import fable.imageviewer.views.HeaderPlotView;
import fable.imageviewer.views.ImageView;
import fable.imageviewer.views.LineView;
import fable.imageviewer.views.ProfileView;
import fable.imageviewer.views.ReliefView;
import fable.imageviewer.views.RockingCurveView;

/**
 * Perspective for the fable ImageViewer. The perspective contains two windows :
 * on the left the sample navigator and header and line plot is displayed. On
 * the right (the main window) displays the image and profile and zoomed area
 * and relief plots. The windows cannot be closed to make sure they are always
 * there.
 * 
 * @author Andy Gotz
 * 
 */
public class Perspective implements IPerspectiveFactory {

	public static final String ID = "fable.imageviewer.perspective";

	public void createInitialLayout(IPageLayout layout) {
		// Add perspective-specific action sets
		layout.addActionSet("fable.imageviewer.actionset");
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.addPerspectiveShortcut(PerspectiveSmall.ID);
		IFolderLayout folder_left = layout.createFolder("Sample",
				IPageLayout.LEFT, .3f, editorArea);
		folder_left.addPlaceholder("org.eclipse.ui.views.ResourceNavigator"
				+ ":*");
		folder_left.addView("org.eclipse.ui.views.ResourceNavigator");
		folder_left.addPlaceholder(SampleNavigatorView.ID + ":*");
		folder_left.addView(SampleNavigatorView.ID);
		layout.getViewLayout(SampleNavigatorView.ID).setCloseable(false);
		IFolderLayout folder_left_bottom = layout.createFolder("Play",
				IPageLayout.BOTTOM, .85f, "Sample");
		folder_left_bottom.addPlaceholder(ImagePlay.ID + ":*");
		folder_left_bottom.addView(ImagePlay.ID);
		folder_left.addPlaceholder(HeaderTableView.ID + ":*");
		folder_left.addView(HeaderTableView.ID + ":0");
		folder_left.addPlaceholder(HeaderPlotView.ID + ":*");
		folder_left.addPlaceholder(LineView.ID + ":*");
		folder_left.addPlaceholder(ProfileView.ID + ":*");
		folder_left.addPlaceholder(RockingCurveView.ID + ":*");
		folder_left.addPlaceholder(ReliefView.ID + ":*");
		IFolderLayout folder_right = layout.createFolder("Image",
				IPageLayout.RIGHT, .3f, editorArea);
		folder_right.addView(ImageView.ID + ":" + ImageComponent.SECONDARY_ID_MAIN);
		folder_left.addPlaceholder(ImageView.ID + ":*");
		layout.addShowViewShortcut(ImagePlay.ID);
		layout.addShowViewShortcut(HeaderTableView.ID);
		layout.addShowViewShortcut(HeaderPlotView.ID);
		layout.addShowViewShortcut(LineView.ID);
		layout.addShowViewShortcut(ProfileView.ID);
		layout.addShowViewShortcut(RockingCurveView.ID);
		layout.addShowViewShortcut(ReliefView.ID);
	}

}
