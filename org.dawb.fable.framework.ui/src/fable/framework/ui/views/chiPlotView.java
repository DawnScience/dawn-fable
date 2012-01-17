/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.ui.views;

import java.io.PrintStream;

import jep.JepException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.ViewPart;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import fable.framework.toolbox.ToolBox;
import fable.framework.ui.rcp.Activator;
import fable.framework.views.FableMessageConsole;
import fable.python.ChiFile;

public class chiPlotView extends ViewPart {

	/**
	 * Display an image as a 3d relief plot using opengl as an eclipse view. The
	 * data to display are updated by calling the setData() method.
	 * 
	 * @author andy gotz
	 * 
	 */

	public FableMessageConsole console;
	public static final String ID = "fable.framework.ui.views.chiPlotView";
	public static chiPlotView thisView = null;
	public static GLContext context = null;
	// public static GLU glu = new GLU();
	// public static GLUT glut = new GLUT();
	static private SceneGrip grip = null;
	private static GLCanvas canvas = null;
	private Spinner minimumSpinner, maximumSpinner;
	private Button freezeButton, autoscaleButton;
	private static boolean freeze = false, autoscale = true;
	private Button updateButton, resetButton;
	/* a simple lookup table to display a temperature-like lut */
	private static float[] blue = { 1.0f, 0.333f, 0.666f, 0.999f, 0.833f,
			0.666f, 0.5f, 0.333f, 0.166f, 0.0f, 0.0f };
	private static float[] green = { 1.0f, 0.0f, 0.111f, 0.222f, 0.333f,
			0.666f, 0.999f, 0.666f, 0.333f, 0.0f, 0.0f };
	private static float[] red = { 1.0f, 0.0f, 0.055f, 0.110f, 0.165f, 0.220f,
			0.275f, 0.333f, 0.666f, 0.999f, 1.0f };
	private static int COLOR_INDEX_MAX = 10;
	private float scaleMinimum, scaleMaximum;
	private static int pointSize = 4;
	// KE: Var iable that are not really used are commented out
	// private static float scale = 1;
	private static float[] image = null;
	private static int imageWidth = 0;
	private static int imageHeight = 0;
	// private static float zcenter;
	private float dots[] = null;
	// private float maximum;
	private float minimum, mean;
	private String fileName;
	// private float size;
	private static int canvasWidth = 0;
	private static int canvasHeight = 0;
	private static float zscale;
	private static boolean reliefListFirst = true;
	private static int reliefList;
	// private static Logger logger;
	// protected float aspect;
	static boolean firstImage = true;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createPartControl(Composite parent) {

		// logger = FableLogger.getLogger();
		console = new FableMessageConsole("Peaksearch console");
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(
				new IConsole[] { console });
		console.displayOut("Welcome to chiplotview "
				+ ToolBox.getPluginVersion(Activator.PLUGIN_ID));

		IOConsoleOutputStream stream = console.newOutputStream();

		// console_debug = new ConsoleLineTracker();

		System.setOut(new PrintStream(stream, true));
		System.setErr(new PrintStream(stream));

		thisView = this;
		parent.setLayout(new GridLayout());
		Composite controlPanelComposite = new Composite(parent, SWT.NULL);
		GridLayout controlGridLayout = new GridLayout();
		controlGridLayout.numColumns = 8;
		controlPanelComposite.setLayout(controlGridLayout);
		controlPanelComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, false));
		freezeButton = new Button(controlPanelComposite, SWT.CHECK);
		freezeButton.setText("Freeze");
		freezeButton.setToolTipText("freeze 3d relief, disable rotation");
		freezeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (freezeButton.getSelection())
					freeze = true;
				else
					freeze = false;
			}
		});
		resetButton = new Button(controlPanelComposite, SWT.NULL);
		resetButton.setText("Reset");
		resetButton
				.setToolTipText("reset 3d projection to be flat and fill the canvas");
		resetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grip.init();
			}
		});
		autoscaleButton = new Button(controlPanelComposite, SWT.CHECK);
		autoscaleButton.setText("Autoscale");
		autoscaleButton
				.setToolTipText("autoscale 3d relief between minimum and mean");
		autoscaleButton.setSelection(true);
		autoscaleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (autoscaleButton.getSelection()) {
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
		});
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
		updateButton = new Button(controlPanelComposite, SWT.NULL);
		updateButton.setText("Update");
		updateButton.setToolTipText("redraw 3d relief plot");
		updateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				scaleImage();
				drawReliefList();
				drawRelief();
			}
		});
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new FillLayout());
		GLData data = new GLData();
		data.doubleBuffer = true;
		canvas = new GLCanvas(comp, SWT.NONE, data);
		canvas.setSize(comp.getSize());
		canvas.setCurrent();
		// context =
		// GL11.GLDrawableFactory.getFactory().createExternalGLContext();
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle bounds = canvas.getBounds();
				canvas.setCurrent();
				try {
					GLContext.useContext(canvas);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				GL11.glViewport(0, 0, bounds.width, bounds.height);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				// GLU glu = new GLU();
				// aspect = (float) imageWidth / (float) imageHeight;
				// gl.glMatrixMode(GL.GL_MODELVIEW);
				// gl.glLoadIdentity();
				drawRelief();
				canvas.swapBuffers();
			}
		});
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glColor3f(1.0f, 0.0f, 0.0f);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		// gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		// gl.glClearDepth(1.0);
		// gl.glLineWidth(2);
		// / gl.glEnable(GL.GL_DEPTH_TEST);
		GL11.glOrtho(0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 1.0f);

		// create the grip for users to change the orientation, translation and
		// zoom
		grip = new SceneGrip();
		canvas.addMouseListener(grip);
		canvas.addMouseMoveListener(grip);
		canvas.addListener(SWT.MouseWheel, grip);
		canvas.addKeyListener(grip);
		// apparently opengl has to be redrawn constantly (why ?)
		Display.getCurrent().asyncExec(new Runnable() {

			public void run() {
				if (!canvas.isDisposed()) {
					canvas.setCurrent();
					// Rectangle bounds = canvas.getBounds();
					/*
					 * canvasWidth = bounds.width; canvasHeight = bounds.height;
					 */
					// context.makeCurrent();
					// GL gl = context.getGL ();
					// gl.glClear(GL.GL_COLOR_BUFFER_BIT |
					// GL.GL_DEPTH_BUFFER_BIT);
					// gl.glClearColor(.0f, .0f, .0f, 1.0f); // black
					// background*/
					drawRelief();
					canvas.swapBuffers();

				}
			}
		});
	}

	/**
	 * Display list (glCallList )created and compiled previously in
	 * drawReliefList.
	 * 
	 */
	static void drawRelief() {
		canvas.setCurrent();
		try {
			GLContext.useContext(canvas);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		if (!freeze)
			grip.adjust();
		if (image != null) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDisable(GL11.GL_LIGHTING);
			// Execute a display list here
			GL11.glCallList(reliefList);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	// private static void testDraw() {
	// if (canvas != null && context != null) {
	//
	// System.out.println("testDraw()");
	// // if (!freeze) grip.adjust();
	// // canvas.setCurrent();
	// // context.makeCurrent();
	// GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	// GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	// GL11.glColor3f(1.0f, 1.0f, 1.0f);
	// GL11.glOrtho(0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 1.0f);
	// GL11.glBegin(GL11.GL_POLYGON);
	// GL11.glVertex3f(0.25f, 0.25f, 0.0f);
	// GL11.glVertex3f(0.75f, 0.25f, 0.0f);
	// GL11.glVertex3f(0.75f, 0.75f, 0.0f);
	// GL11.glVertex3f(0.25f, 0.75f, 0.0f);
	// GL11.glEnd();
	// GL11.glFlush();
	// }
	// }

	/**
	 * Build the list to display here
	 */
	private static void drawReliefList() {

		if (!reliefListFirst) {
			GL11.glDeleteLists(reliefList, 1);
		}
		reliefListFirst = false;
		reliefList = GL11.glGenLists(1);
		GL11.glNewList(reliefList, GL11.GL_COMPILE);
		GL11.glColor3f(1.0f, 1.0f, 1.0f); // white
		GL11.glPointSize(pointSize);
		for (int i = 0; i < imageWidth - 1; i++) {
			GL11.glBegin(GL11.GL_LINE_STRIP);
			int j = i + 1;
			int color_index = (int) image[i];
			if (color_index < 0)
				color_index = 0;
			if (color_index > COLOR_INDEX_MAX)
				color_index = COLOR_INDEX_MAX;
			GL11.glColor3f(red[color_index], green[color_index],
					blue[color_index]); // temperature lut
			GL11.glVertex3f(i, image[i], image[j]);
			// System.out.println("i=" + i + ", j=" + j + " image[i]=" +
			// image[i] + " image[j]=" + image[j]);
			GL11.glEnd();
		}
		/*
		 * gl.glBegin(GL.GL_TRIANGLES);
		 * 
		 * gl.glVertex3f(-1.0f, -0.5f, 0.0f); // lower left vertex
		 * gl.glVertex3f( 1.0f, -0.5f, 0.0f); // lower right vertex
		 * gl.glVertex3f( 0.0f, 0.5f, 0.0f); // upper vertex
		 * 
		 * gl.glEnd();
		 */
		GL11.glEndList();
		GL11.glFlush();
	}

	/**
	 * 
	 * @param listdots
	 * @param _width
	 * @param _height
	 * @param _minimum
	 * @param _maximum
	 * @param _mean
	 * @param _fileName
	 */
	public void plot(float[] listdots, int _width, int _height, float _minimum,
			float _maximum, float _mean, String _fileName) {
		/*
		 * do not listen for new images - display on the selected image until a
		 * new image is set manually
		 */
		dots = listdots; /* save a copy for rescaling later */
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
		// this.size = (imageWidth + imageHeight) / 2;
		// aspect = (float) imageWidth / (float) imageHeight;
		setPartName("Plot " + fileName);
		if (firstImage) {
			grip.init();
			firstImage = false;
		}

		scaleImage();
		drawReliefList();
		// drawRelief();

		// System.out.println("affichage fini");

	}

	/**
	 * 
	 */
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

			float imageValue;
			imageValue = dots[i];
			image[i] = (imageValue - scaleMinimum) * zscale;

		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// canvas.setFocus();
	}

	/*******************************************************************************
	 * Copyright (c) 2005 Bo Majewski All rights reserved. This program and the
	 * accompanying materials are made available under the terms of the Eclipse
	 * Public License v1.0 which accompanies this distribution, and is available
	 * at http://www.eclipse.org/org/documents/epl-v10.html
	 * 
	 * Contributors: Bo Majewski - initial API and implementation
	 *******************************************************************************/

	/**
	 * Implements a scene grip, capable of rotating and moving a GL scene with
	 * the help of the mouse and keyboard.
	 * 
	 * @author Bo Majewski
	 */
	public class SceneGrip extends MouseAdapter implements MouseMoveListener,
			Listener, KeyListener {
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

		public SceneGrip() {
			this.init();
		}

		protected void init() {
			canvas.setCurrent();
			Rectangle bounds = canvas.getBounds();
			canvasWidth = bounds.width;
			canvasHeight = bounds.height;
			this.xrot = this.yrot = 0.0f;
			this.xoff = this.yoff = 0.0f;
			if (imageWidth != 0) {
				xscale = canvasWidth / imageWidth;
			}
			if (imageHeight != 0) {
				yscale = canvasHeight / imageHeight;
			}
			zoff = 1.0f;
			if (xscale < yscale)
				zoff = xscale;
			else
				zoff = yscale;
			// logger.debug("xscale = "+xscale+" yscale = "+yscale+" zoff = "+zoff);
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

		public void mouseUp(MouseEvent e) {
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
			// this.zoff += event.count/6.0f;
			this.zoff *= 1.1f;
		}

		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.ARROW_UP:
				if ((e.stateMask & SWT.CTRL) != 0) {
					this.xrot -= 5f;
				} else {
					this.yoff -= (float) canvasHeight / 10.f;
				}
				break;
			case SWT.ARROW_DOWN:
				if ((e.stateMask & SWT.CTRL) != 0) {
					this.xrot += 5f;
				} else {
					this.yoff += (float) canvasHeight / 10.f;
				}
				break;
			case SWT.ARROW_LEFT:
				if ((e.stateMask & SWT.CTRL) != 0) {
					this.yrot -= 5f;
				} else {
					this.xoff -= (float) canvasWidth / 10.f;
				}
				break;
			case SWT.ARROW_RIGHT:
				if ((e.stateMask & SWT.CTRL) != 0) {
					this.yrot += 5f;
				} else {
					this.xoff += (float) canvasWidth / 10;
				}
				break;
			case SWT.PAGE_UP:
				this.zoff *= 1.05f;
				break;
			case SWT.PAGE_DOWN:
				this.zoff *= .95f;
				break;
			case SWT.HOME:
				this.init();
				break;
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void adjust() {
			try {
				GLContext.useContext(canvas);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
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
			GL11.glTranslatef(-imageWidth / 2, -imageHeight / 2, 0);
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

	}

	public void openFile(String filename2) {

		try {
			ChiFile chifile = new ChiFile();
			chifile.loadfile(filename2);
			// float[] f = chifile.getlist();

			// plot(f, 2048,
			// 2048,chifile.getMin(),chifile.getMax(),chifile.getMean(),filename2);

		} catch (JepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
