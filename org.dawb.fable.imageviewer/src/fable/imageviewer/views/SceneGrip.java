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

import java.awt.event.MouseWheelEvent;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fable.framework.toolbox.FableUtils;
import fable.framework.toolboxpreferences.PreferenceConstants;
import fable.imageviewer.preferences.ImageviewerPreferencePage;
import fable.imageviewer.rcp.Activator;


/**************************************************************************
 * Copyright (c) 2005 Bo Majewski All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available
 * at http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors: Bo Majewski - initial API and implementation
 *************************************************************************/

/**
 * Implements a scene grip, capable of rotating and moving a GL scene with
 * the help of the mouse and keyboard.
 * 
 * @author Bo Majewski
 */
public class SceneGrip extends MouseAdapter implements MouseMoveListener,
		Listener, KeyListener, MouseWheelListener  {
	
	private static Logger logger = LoggerFactory.getLogger(SceneGrip.class);

	private int canvasWidth = 0;
	private int canvasHeight = 0;
	private GLCanvas canvas;

	private float xrot;
	private float yrot;
	private float zoff;
	private float xoff;
	private float yoff;
	private float xcpy;
	private float ycpy;
	private float xscale;
	private float yscale;
	private boolean move;
	private int xdown;
	private int ydown;
	private int mouseDown;

	private IImageSizeProvider prov;

	public SceneGrip(final GLCanvas canvas, IImageSizeProvider prov) {
		this.canvas = canvas;
		this.prov   = prov;
		this.init();
	}

	protected void init() {
		canvas.setCurrent();
		Rectangle bounds = canvas.getBounds();
		canvasWidth = bounds.width;
		canvasHeight = bounds.height;
		this.xrot = this.yrot = 0.0f;
		this.xoff = this.yoff = 0.0f;
		if (prov.getImageWidth() != 0) {
			xscale = canvasWidth / prov.getImageWidth();
		}
		if (prov.getImageHeight() != 0) {
			yscale = canvasHeight / prov.getImageHeight();
		}
		zoff = 1.0f;
		if (xscale < yscale)
			zoff = xscale;
		else
			zoff = yscale;
		logger.debug("xscale = " + xscale + " yscale = " + yscale
				+ " zoff = " + zoff);
	}

	public void mouseDown(MouseEvent e) {
		if (++this.mouseDown == 1) {
			if ((this.move = e.button == 3)) {
				this.xcpy = xoff;
				this.ycpy = yoff;
				((Control) e.widget).setCursor(e.widget.getDisplay()
						.getSystemCursor(SWT.CURSOR_HAND));
			} else {
				this.xcpy = xrot;
				this.ycpy = yrot;
				((Control) e.widget).setCursor(e.widget.getDisplay()
						.getSystemCursor(SWT.CURSOR_SIZEALL));
			}

			this.xdown = e.x;
			this.ydown = e.y;
		}
	}
	


	public void mouseUp( MouseEvent e) {
	//	IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
	//System.out.println(prefs.getString(fable.imageviewer.preferences.PreferenceConstants.P_RELIEFMOVE));
		if (--this.mouseDown == 0) {
			((Control) e.widget).setCursor(e.widget.getDisplay()
					.getSystemCursor(SWT.CURSOR_ARROW));
		}
	}
	


	public void mouseMove(MouseEvent e) {
		
		Point p = ((Control) e.widget).getSize();

		if (this.mouseDown > 0) {
			int dx = e.x - this.xdown;
			int dy = e.y - this.ydown;

			if (this.move) {
				yoff = this.ycpy + ((canvasHeight / 2.f) * dy)
						/ (2.0f * p.y);
				xoff = this.xcpy + ((canvasWidth / 2.f) * dx)
						/ (2.0f * p.x);
			} else {
				xrot = this.xcpy + dy / 2.0f;
				yrot = this.ycpy + dx / 2.0f;
			}
		}
	}

	public void handleEvent(Event event) {	
		//this.zoff *= 1.1f; dosent work
	}
	
	public void zoomIn() {
		
		this.zoff *= 1.05f;
	}
	public void zoomOut() {
		this.zoff *= .95f;
	}
	
	public void rotate(final int direction) {
		
		switch(direction) {
		case SWT.ARROW_UP:
			this.xrot -= 5f;

			break;
		case SWT.ARROW_DOWN:
			this.xrot += 5f;

			break;
		case SWT.ARROW_LEFT:
			this.yrot += 5f;

			break;
		case SWT.ARROW_RIGHT:
			this.yrot -= 5f;
			break;
		default:
			return;
		}
	}
	
	public void translate(final int direction) {
		switch (direction) {
		case SWT.ARROW_UP:
			this.yoff -= (float) canvasHeight / 10.f;
			break;
		case SWT.ARROW_DOWN:
			this.yoff += (float) canvasHeight / 10.f;
			break;
		case SWT.ARROW_LEFT:
			this.xoff -= (float) canvasWidth / 10.f;
			break;
		case SWT.ARROW_RIGHT:
			this.xoff += (float) canvasWidth / 10;
			break;
		default:
			return;
		}
	}

	public void keyPressed(KeyEvent e) {
		
		if ((e.stateMask & SWT.CONTROL) != 0) {
			rotate(e.keyCode);
			return;
		}

		translate(e.keyCode);
		switch (e.keyCode) {
				
		case SWT.PAGE_UP:
			zoomIn();
			break;
		case SWT.PAGE_DOWN:
			zoomOut();
			break;
		case SWT.HOME:
			this.init();
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Warning called constantly in display loop - change with care.
	 */
	public void adjust() {
		canvas.setCurrent();
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException ex) {
			FableUtils.excMsg(ReliefView.class,
					"Error in adjust using GLContext.useContext", ex);
		}
		// gl = context.getGL ();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		/* set the orthogonal projection to the size of the window */
		GL11.glOrtho(0, canvasWidth, canvasHeight, 0, -1.0e5f, 1.0e5f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(canvasWidth / 2 + this.xoff, canvasHeight / 2
				+ this.yoff, 0);
		GL11.glScalef(zoff, zoff, zoff);
		/*
		 * zoff has no effect on the orthogonal projection therefore zoom by
		 * passing zoff to scale
		 */
		GL11.glRotatef(this.xrot, 1f, 0.0f, 0.0f);
		GL11.glRotatef(this.yrot, 0.0f, 1f, 0.0f);
		GL11.glTranslatef(-prov.getImageWidth() / 2, -prov.getImageHeight() / 2, 0);
	}

	public void setOffsets(float x, float y, float z) {
		this.xoff = x;
		this.yoff = y;
		this.zoff = z;
	}

	public void setRotation(float x, float y) {
		this.xrot = x;
		this.yrot = y;
	}

	public void setBounds(Rectangle bounds) {
		canvasWidth = bounds.width;
		canvasHeight = bounds.height;
	}

	public void mouseScrolled(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e);
		}

	@Override
	public void mouseScrolled(MouseEvent e) {
		// TODO Auto-generated method stub
		
		if (e.count>0)zoomIn();
		else zoomOut();
	}
		
	

	
	
	
}
