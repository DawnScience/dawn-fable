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
 * This class represents ImageViewer perspective for small screen. <br>
 * Main views, file selector (<code>SampleNavigator</code>), image (set on top)
 * and console are put on each other. <br>
 * Maximum height to display this perspective is defined in <code>
 *  fable.framework.toolbox.internal.IVarKeys.SCREENHEIGHT</code>
 */
public class PerspectiveSmall implements IPerspectiveFactory {

	public static final String ID = "fable.imageviewer.perspectiveSmall";

	public void createInitialLayout(IPageLayout layout) {
		// Add perspective-specific action sets
		layout.addActionSet("fable.imageviewer.actionset");
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		IFolderLayout folder = layout.createFolder("ImageViewer",
				IPageLayout.TOP, 1f, editorArea);
		folder.addView(ImageView.ID + ":" + ImageComponent.SECONDARY_ID_MAIN);
		layout.addPerspectiveShortcut(Perspective.ID);
		layout.getViewLayout(ImageView.ID + ":" + ImageComponent.SECONDARY_ID_MAIN)
				.setCloseable(false);

		IFolderLayout playFolder = layout.createFolder("Play",
				IPageLayout.BOTTOM, .9f, "ImageViewer");
		playFolder.addPlaceholder(ImagePlay.ID + ":*");
		playFolder.addView(ImagePlay.ID);

		folder.addPlaceholder(SampleNavigatorView.ID + ":*");
		folder.addView(SampleNavigatorView.ID);
		layout.getViewLayout(SampleNavigatorView.ID).setCloseable(false);

		folder.addPlaceholder(HeaderTableView.ID + ":*");
		folder.addView(HeaderTableView.ID + ":0");

		folder.addPlaceholder(HeaderPlotView.ID + ":*");
		folder.addPlaceholder(LineView.ID + ":*");
		folder.addPlaceholder(ProfileView.ID + ":*");
		folder.addPlaceholder(RockingCurveView.ID + ":*");
		folder.addPlaceholder(ReliefView.ID + ":*");

		layout.addShowViewShortcut(ImagePlay.ID);
		layout.addShowViewShortcut(HeaderTableView.ID);
		layout.addShowViewShortcut(HeaderPlotView.ID);
		layout.addShowViewShortcut(LineView.ID);
		layout.addShowViewShortcut(ProfileView.ID);
		layout.addShowViewShortcut(RockingCurveView.ID);
		layout.addShowViewShortcut(ReliefView.ID);
	}

}
