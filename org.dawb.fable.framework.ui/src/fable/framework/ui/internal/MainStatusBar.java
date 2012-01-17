/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * This is a status bar for fable.
 * This class allows fable to add text and images to the main status bar.
 * @author SUCHET
 *
 */
public class MainStatusBar extends ContributionItem {
	public final static int DEFAULT_CHAR_WIDTH = 40;
	public final static int DEFAULT_CHAR_HEIGHT = 40;
	
	private CLabel label;

	private Image image;

	private String text = ""; //$NON-NLS-1$

	private int widthHint = -1;

	private int heightHint = -1;

	private Listener listener;

	private int eventType;

	private String tooltip;

	/**
	 * @param id
	 */
	public MainStatusBar(String id) {
		this(id, DEFAULT_CHAR_WIDTH);
	}

	public MainStatusBar(String id, int default_char_width2) {
		super(id);
		this.widthHint = default_char_width2;
		
		setVisible(false); // no text to start with
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	public void fill(Composite parent) {
		Label sep = new Label(parent, SWT.SEPARATOR);
		label = new CLabel(parent, SWT.SHADOW_NONE);

		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fm = gc.getFontMetrics();
		Point extent = gc.textExtent(text);
		
		int nwidthHint;
		
		if (widthHint > 0) {
			nwidthHint = fm.getAverageCharWidth() * widthHint;
		} else {
			nwidthHint = extent.x;
		}
		heightHint = fm.getHeight();
		gc.dispose();

		StatusLineLayoutData statusLineLayoutData = new StatusLineLayoutData();
		statusLineLayoutData.widthHint = nwidthHint;
		statusLineLayoutData.heightHint = heightHint;
		label.setLayoutData(statusLineLayoutData);
		label.setText(text);
		label.setImage(image);
		if (listener != null) {
			label.addListener(eventType, listener);
		}
		if (tooltip != null) {
			label.setToolTipText(tooltip);
		}

		statusLineLayoutData = new StatusLineLayoutData();
		statusLineLayoutData.heightHint = heightHint*2;
		sep.setLayoutData(statusLineLayoutData);
	}

	public void setText(String txt) {
		if (txt != null){
		this.text = txt;

		if (label != null && !label.isDisposed())
			label.setText(this.text);

		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null)
					contributionManager.update(true);
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null)
					contributionManager.update(true);
			}
		}
		}
	}

	public void setTooltip(String tooltip) {
		if (tooltip == null)
			throw new NullPointerException();

		this.tooltip = tooltip;

		if (label != null && !label.isDisposed()) {
			label.setToolTipText(this.tooltip);
		}
	}

	public void setImage(Image image) {
		if (image == null)
			throw new NullPointerException();

		this.image = image;

		if (label != null && !label.isDisposed())
			label.setImage(this.image);

		if (!isVisible()) {
			setVisible(true);
			IContributionManager contributionManager = getParent();

			if (contributionManager != null)
				contributionManager.update(true);
		}
	}
}
