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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import fable.framework.toolbox.FableUtils;
import fable.framework.toolbox.GridUtils;
import fable.imageviewer.rcp.Activator;

/**
 * Display an image as a 3d relief plot using opengl as an eclipse view. The
 * data to display are updated by calling the setData() method.
 * 
 * @author andy gotz
 * 
 */
public class ReliefView extends ViewPart implements IImageSizeProvider{
	

	public static final String ID = "fable.imageviewer.views.ReliefView";
	
	/* a simple lookup table to display a temperature-like lut */
	private static float[] blue = { 1.0f, 0.333f, 0.666f, 0.999f, 0.833f,
			0.666f, 0.5f, 0.333f, 0.166f, 0.0f, 0.0f };
	private static float[] green = { 1.0f, 0.0f, 0.111f, 0.222f, 0.333f,
			0.666f, 0.999f, 0.666f, 0.333f, 0.0f, 0.0f };
	private static float[] red = { 1.0f, 0.0f, 0.055f, 0.110f, 0.165f, 0.220f,
			0.275f, 0.333f, 0.666f, 0.999f, 1.0f };

	
	public ReliefView thisView = null;
	public GLContext context;
	// public static GL11 gl;
	public GLU glu = new GLU();
	// public static GLUT glut = new GLUT();
	private SceneGrip grip;
	private GLCanvas canvas;
	private Spinner minimumSpinner, maximumSpinner;
	private Action freezeButton, autoscaleButton;
	private boolean freeze = false, autoscale = true;
	private Action updateButton, highZReset;
	private int colorIndexMax = 10;
	private float scaleMinimum, scaleMaximum;
	private int pointSize = 4;
	// private static float scale=1;
	private float[] image = null;
	// private static float zcenter;
	private float imageAsFloat[] = null;
	// private float maximum;
	private float minimum, mean;
	private String fileName;
	// private float size;
	private float zscale;
	private boolean reliefListFirst = true;
	private int reliefList;
	private boolean firstImage = true;
	private int imageWidth = 0;
	private int imageHeight = 0;

	private void drawRelief() {
		canvas.setCurrent();
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException ex) {
			FableUtils.excMsg(ReliefView.class,
					"Error in drawRelief using GLContext.useContext", ex);
		}
		// context.makeCurrent();
		// gl = context.getGL ();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		if (!freeze)
			grip.adjust();
		if (image != null) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glCallList(reliefList);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	private void drawReliefList() {
		// long started = System.currentTimeMillis();
		if (!reliefListFirst) {
			GL11.glDeleteLists(reliefList, 1);
		}
		reliefListFirst = false;
		reliefList = GL11.glGenLists(1);
		GL11.glNewList(reliefList, GL11.GL_COMPILE);
		GL11.glColor3f(1.0f, 1.0f, 1.0f); // white
		GL11.glPointSize(pointSize);
		for (int i = 0; i < imageWidth; i++) {
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int j = 0; j < imageHeight; j++) {
				int color_index;
				color_index = (int) image[j * imageWidth + i];
				if (color_index < 0)
					color_index = 0;
				if (color_index > colorIndexMax)
					color_index = colorIndexMax;
				GL11.glColor3f(red[color_index], green[color_index],
						blue[color_index]); // temperature lut
				GL11.glVertex3f(i, j, image[j * imageWidth + i]);
			}
			GL11.glEnd();
		}
		for (int i = 0; i < imageHeight; i++) {
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int j = 0; j < imageWidth; j++) {
				int color_index;
				color_index = (int) image[i * imageWidth + j];
				if (color_index < 0)
					color_index = 0;
				if (color_index > colorIndexMax)
					color_index = colorIndexMax;
				GL11.glColor3f(red[color_index], green[color_index],
						blue[color_index]); // temperature lut
				GL11.glVertex3f(j, i, image[i * imageWidth + j]);
			}
			GL11.glEnd();
		}
		GL11.glEndList();
		// long elapsed = System.currentTimeMillis()-started;
		// logger.debug("time to draw relief list "+elapsed+" ms");
	}

	public void setImageAsFloat(float[] _imageAsFloat, int _width, int _height,
			float _minimum, float _maximum, float _mean, String _fileName) {
		/*
		 * do not listen for new images - display on the selected image until a
		 * new image is set manually
		 */
		imageAsFloat = _imageAsFloat; /* save a copy for rescaling later */
		image = new float[_width * _height];
		imageWidth = _width;
		imageHeight = _height;
		minimum = _minimum;
		// maximum = _maximum;
		mean = _mean;
		if (autoscale) {
			scaleMinimum = _minimum;
			scaleMaximum = _mean;
		}
		fileName = _fileName;
		// this.size = (imageWidth+imageHeight)/2;
		setPartName("Relief " + fileName);
		if (firstImage) {
			grip.init();
			firstImage = false;
		}
		scaleImage();
		drawReliefList();
		drawRelief();
	}

	public void scaleImage() {
		if (autoscale) {
			scaleMinimum = minimum;
			scaleMaximum = mean;
			minimumSpinner.setSelection((int) (scaleMinimum));
			maximumSpinner.setSelection((int) (scaleMaximum));
		} else {
			scaleMinimum = minimumSpinner.getSelection();
			scaleMaximum = maximumSpinner.getSelection();
		}
		if (scaleMinimum == scaleMaximum)
			scaleMaximum = scaleMinimum + 1;
		zscale = 10.0f / (2.f * (scaleMaximum - scaleMinimum));
		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight; j++) {
				float imageValue;
				int imageIndex;
				imageIndex = j * imageWidth + i;
				imageValue = imageAsFloat[j * imageWidth + i];
				image[imageIndex] = (imageValue - scaleMinimum) * zscale;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		thisView = this;
		parent.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(parent);
		
		createActions();

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new FillLayout());
		GLData data = new GLData();
		data.doubleBuffer = true;
		canvas = new GLCanvas(comp, SWT.NONE, data);
		canvas.setSize(comp.getSize());
		canvas.setCurrent();
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException ex) {
			FableUtils
					.excMsg(
							ReliefView.class,
							"Error in createPartControl using GLContext.useContext",
							ex);
		}
		// context = GLDrawableFactory.getFactory().createExternalGLContext();
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle bounds = canvas.getBounds();
				// float fAspect = (float) bounds.width / (float) bounds.height;
				canvas.setCurrent();
				try {
					GLContext.useContext(canvas);
				} catch (LWJGLException ex) {
					FableUtils
							.excMsg(
									ReliefView.class,
									"Error in resize listener using GLContext.useContext",
									ex);
				}
				// context.makeCurrent();
				// GL11 gl = context.getGL ();
				GL11.glViewport(0, 0, bounds.width, bounds.height);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				// GLU glu = new GLU();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				drawRelief();
				canvas.swapBuffers();
				// context.release();
			}
		});
		canvas.setCurrent();
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException ex) {
			FableUtils
					.excMsg(
							ReliefView.class,
							"Error in createPartControl using GLContext.useContext",
							ex);
		}
		// GL11 gl = context.getGL ();
		GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glColor3f(1.0f, 0.0f, 0.0f);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		GL11.glClearDepth(1.0);
		GL11.glLineWidth(2);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		// context.release();
		// create the grip for users to change the orientation, translation and
		// zoom
		grip = new SceneGrip(canvas, this);
		canvas.addMouseListener(grip);
		canvas.addMouseMoveListener(grip);
		canvas.addListener(SWT.MouseWheel, grip);
		canvas.addKeyListener(grip);
		canvas.addMouseWheelListener(grip);
		// apparently opengl has to be redrawn constantly (why ?)
		Display.getCurrent().asyncExec(new Runnable() {
			// int rot = 0;
			public void run() {
				if (canvas==null) return;
				if (!canvas.isDisposed()) {
					canvas.setCurrent();
					Rectangle bounds = canvas.getBounds();
					grip.setBounds(bounds);
					canvas.setCurrent();
					try {
						GLContext.useContext(canvas);
					} catch (LWJGLException ex) {
						FableUtils.excMsg(ReliefView.class,
								"Error in createPartControl using "
										+ "GLContext.useContext", ex);
					}
					// GL11 gl = context.getGL ();
					GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
							| GL11.GL_DEPTH_BUFFER_BIT);
					GL11.glClearColor(.0f, .0f, .0f, 1.0f); // black background
					drawRelief();
					canvas.swapBuffers();
					// context.release();
					Display.getCurrent().timerExec(200, this);
				}
			}
		});
		
		createImageInformationPanel(parent);
	}
	
	public void dispose() {
		super.dispose();
		canvas = null;
	}

	private void createImageInformationPanel(Composite parent) {
		
		Composite controlPanelComposite = new Composite(parent, SWT.NULL);
		controlPanelComposite.setLayout(new GridLayout(4, false));
		controlPanelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		
		Label minLabel = new Label(controlPanelComposite, SWT.NULL);
		minLabel.setText("Minimum");
		minimumSpinner = new Spinner(controlPanelComposite, SWT.NULL);
		minimumSpinner.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		minimumSpinner.setMinimum(0);
		minimumSpinner.setMaximum(Integer.MAX_VALUE);
		minimumSpinner.setEnabled(false);
		Label maxLabel = new Label(controlPanelComposite, SWT.NULL);
		maxLabel.setText("Maximum");
		maximumSpinner = new Spinner(controlPanelComposite, SWT.NULL);
		maximumSpinner.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		maximumSpinner.setMinimum(0);
		maximumSpinner.setMaximum(Integer.MAX_VALUE);
		maximumSpinner.setEnabled(false);
	}
	

	private void createActions() {	

		final IContributionManager man = getViewSite().getActionBars().getToolBarManager();
		
		freezeButton = new Action("Freeze", IAction.AS_CHECK_BOX) {
			public void run() {
				freeze = freezeButton.isChecked();
			}
		};
		freezeButton.setToolTipText("Freeze 3d relief, disable rotation");
		freezeButton.setImageDescriptor(Activator.getImageDescriptor("/icons/freeze.png"));
		man.add(freezeButton);
		
		man.add(new Separator(getClass().getName()+".zoom"));
		
		final Action zoomIn = new Action("Zoom In", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.zoomIn();
			}
		};
		zoomIn.setToolTipText("Zoom in (Page Up)");
		zoomIn.setImageDescriptor(Activator.getImageDescriptor("/icons/magnifier_zoom_in.png"));
		man.add(zoomIn);
		
		final Action zoomOut = new Action("Zoom Out", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.zoomOut();
			}
		};
		zoomOut.setToolTipText("Zoom out (Page Down)");
		zoomOut.setImageDescriptor(Activator.getImageDescriptor("/icons/magnifier_zoom_out.png"));
		man.add(zoomOut);
		
		man.add(new Separator(getClass().getName()+".translate"));
	
		Action action = new Action("Translate up", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.translate(SWT.ARROW_UP);
			}
		};
		action.setToolTipText("Translate up (Up Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_up.png"));
		man.add(action); 
		
		action = new Action("Translate down", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.translate(SWT.ARROW_DOWN);
			}
		};
		action.setToolTipText("Translate down (Down Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_down.png"));
		man.add(action);

		action = new Action("Translate left", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.translate(SWT.ARROW_LEFT);
			}
		};
		action.setToolTipText("Translate left (Left Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_left.png"));
		man.add(action);
		
		action = new Action("Translate right", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.translate(SWT.ARROW_RIGHT);
			}
		};
		action.setToolTipText("Translate right (Right Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_right.png"));
		man.add(action);

		man.add(new Separator(getClass().getName()+".rotate"));
		
		action = new Action("Rotate X, high Y", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.rotate(SWT.ARROW_UP);
			}
		};
		action.setToolTipText("Rotate X, high Y (Control + Up Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/rotate_up.png"));
		man.add(action); 
		
		action = new Action("Rotate X, low Y", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.rotate(SWT.ARROW_DOWN);
			}
		};
		action.setToolTipText("Rotate X, low Y (Contol + Down Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/rotate_down.png"));
		man.add(action);

		action = new Action("Rotate Y, low X", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.rotate(SWT.ARROW_LEFT);
			}
		};
		action.setToolTipText("Rotate Y, low X (Control + Left Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/rotate_left.png"));
		man.add(action);

		action = new Action("Rotate Y, high X", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.rotate(SWT.ARROW_RIGHT);
			}
		};
		action.setToolTipText("Rotate Y, high X (Control + Right Arrow)");
		action.setImageDescriptor(Activator.getImageDescriptor("/icons/rotate_right.png"));
		man.add(action);
		
			
		man.add(new Separator(getClass().getName()+".reset"));
		highZReset = new Action("Reset", IAction.AS_PUSH_BUTTON) {
			public void run() {
				grip.init();
			}
		};
		highZReset.setText("Reset");
		highZReset.setToolTipText("Reset 3d projection to be flat and fill the canvas and use X/Y view.");
		highZReset.setImageDescriptor(Activator.getImageDescriptor("/icons/z-high.png"));
		man.add(highZReset);
		
		autoscaleButton = new Action("Autoscale", IAction.AS_CHECK_BOX) {
			public void run() {
				if (autoscaleButton.isChecked()) {
					if (!autoscale) {
						autoscale = true;
						scaleImage();
						drawReliefList();
					}
					minimumSpinner.setEnabled(false);
					maximumSpinner.setEnabled(false);
				} else {
					if (autoscale) {
						autoscale = false;
						scaleImage();
						drawReliefList();
					}
					minimumSpinner.setEnabled(true);
					maximumSpinner.setEnabled(true);
				}
			}
		};
		autoscaleButton.setText("Autoscale");
		autoscaleButton.setToolTipText("Autoscale 3d relief between minimum and mean");
		autoscaleButton.setImageDescriptor(Activator.getImageDescriptor("/icons/autoscale.png"));
		autoscaleButton.setChecked(true);
		man.add(autoscaleButton);

		updateButton = new Action("Refresh", IAction.AS_PUSH_BUTTON) {
			public void run() {
				scaleImage();
				drawReliefList();
				drawRelief();			
			}
		};
		updateButton.setText("Refresh");
		updateButton.setToolTipText("Redraw 3d relief plot");
		updateButton.setImageDescriptor(Activator.getImageDescriptor("/icons/update.png"));
		man.add(updateButton);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (canvas!=null && !canvas.isDisposed()) {
			try {
			    canvas.setFocus();
			} catch (Throwable ignored) {
				// The inability to focus is ignored
			}
		}
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

}
