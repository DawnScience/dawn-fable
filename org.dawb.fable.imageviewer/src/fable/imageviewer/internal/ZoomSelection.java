/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.internal;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.toolbox.EclipseUtils;
import fable.imageviewer.component.ImageComponent;
import fable.imageviewer.views.ImageView;
import fable.imageviewer.views.LineView;
import fable.imageviewer.views.ProfileView;
import fable.imageviewer.views.ReliefView;
import fable.imageviewer.views.RockingCurveView;

/**
 * ZoomSelection is a local class to implement the ZoomSelection enum type as a
 * type-safe pattern
 * 
 * @author Andy Gotz
 * 
 */

public class ZoomSelection {

	private final Logger logger = LoggerFactory.getLogger(ZoomSelection.class);
	
	private final String name;
	private final String viewId;
	private final String secondId;

	private ZoomSelection(String name, final String viewId) {
		this(name,viewId,null);
	};

	public ZoomSelection(String name, String viewId, String secondId) {
		this.name     = name;
		this.viewId   = viewId;
		this.secondId = secondId;
	}

	public static final ZoomSelection NONE         = new ZoomSelection("none",         null);
	public static final ZoomSelection AREA         = new ZoomSelection("area",         ImageView.ID, ImageComponent.SECONDARY_ID_ZOOM);
	public static final ZoomSelection LINE         = new ZoomSelection("line",         LineView.ID);
	public static final ZoomSelection PROFILE      = new ZoomSelection("profile",      ProfileView.ID);
	public static final ZoomSelection RELIEF       = new ZoomSelection("relief",       ReliefView.ID);
	public static final ZoomSelection ROCKINGCURVE = new ZoomSelection("rockingcurve", RockingCurveView.ID);

	public String getName() {
		return name;
	}
	
	public void bringToTop() {
		if (!PlatformUI.isWorkbenchRunning()) return;
		if (viewId!=null) {
			if (EclipseUtils.getActivePage()==null) return;
			IViewPart part = EclipseUtils.getActivePage().findView(viewId);
			if (part==null) {
				final IViewReference ref = EclipseUtils.getActivePage().findViewReference(viewId, secondId);
				part = ref != null ? ref.getView(false) : null;
			}
			if (part!=null) {
				try {
				    EclipseUtils.getActivePage().bringToTop(part);
				} catch (Exception ignored) {
					logger.debug("Cannot bring page to top!", ignored);
				}
			}
		}
	}

	public IViewPart getViewPart() throws PartInitException {
		if (secondId==null) {
			IViewPart part = EclipseUtils.getActivePage().findView(viewId);
			if (part==null) part = EclipseUtils.getActivePage().showView(viewId);
			return part;
		} else {
			final IViewReference ref = EclipseUtils.getActivePage().findViewReference(viewId, secondId);
			if (ref ==null) return EclipseUtils.getActivePage().showView(viewId, secondId, IWorkbenchPage.VIEW_ACTIVATE);
		    return ref != null ? ref.getView(true) : null;
		}
	}
	
	public String toString() {
		return name;
	}

}
