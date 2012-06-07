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

import java.awt.event.MouseWheelEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.sound.sampled.Line;
import javax.swing.border.LineBorder;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;

import fable.framework.logging.FableLogger;
import fable.framework.navigator.controller.SampleController;
import fable.framework.toolbox.EclipseUtils;
import fable.framework.toolbox.FableUtils;
import fable.imageviewer.internal.IImagesVarKeys;
import fable.imageviewer.internal.ZoomSelection;
import fable.imageviewer.model.ImageModel;
import fable.imageviewer.model.ImageModelFactory;
import fable.imageviewer.preferences.PreferenceConstants;

import org.embl.cca.utils.imageviewer.FableSelection;
import org.embl.cca.utils.imageviewer.ImageDataUpscale;
import org.embl.cca.utils.imageviewer.ListenerList;
import org.embl.cca.utils.imageviewer.Statistics;
import fable.imageviewer.rcp.Activator;
import fable.imageviewer.component.PSF;
import fable.imageviewer.views.ImageView;
import fable.imageviewer.views.LineView;
import fable.imageviewer.views.ProfileView;
import fable.imageviewer.views.ReliefView;
import fable.imageviewer.views.RockingCurveView;

/**
 * This class manages the imageCanvas for the ImageView and things related to
 * it. These include the selections and the Rectangles describing the image, the
 * original image, the oriented image, and the oriented original image.
 */
public class ImageComponentImage implements IImagesVarKeys {
	private static final boolean debug = false;
	private static final boolean debug1 = false;
	private static final float ZOOMFACTOR_LARGE = .5f;
	/**
	 * Determines whether the LineView is plotted as a curve or histogram. (May
	 * be removed at a later date.)
	 */
	private static final boolean LINEVIEW_HISTOGRAM = true;
	/**
	 * A reference to the instance of ImageViewer that owns this image.
	 */
	ImageComponent iv = null;
	/**
	 * A reference to the class that manages the SWT controls for this view.
	 */
	private ImageComponentUI controls;
	/**
	 * The surface for drawing graphics for this image.
	 */
	private Canvas imageCanvas;
	/**
	 * The GC for this image.
	 */
	private GC selectedRectangle;
	/**
	 * The SWT image that is drawn on the Canvas.
	 */
	private Image image = null;
	/**
	 * The SWT Image legend that is drawn on the Canvas.
	 */
	private Image legend = null;
	
	/**
	 * The device-independent description of the image.
	 */
	private ImageData imageData;
	/**
	 * The device-independent description of the legend.
	 */
	private ImageData legendData;
	/**
	 * Rectangle representing the zoomed, non-oriented image. x and y are
	 * relative to the full non-oriented image. The float arrays correspond to
	 * this Rectangle.
	 */
	private Rectangle imageRect = new Rectangle(0, 0, 0, 0);
	/**
	 * Rectangle representing the full, non-oriented image. x and y are zero. Is
	 * the same as imageModel.getRectangle() and is included for convenience.
	 */
	private Rectangle origRect = new Rectangle(0, 0, 0, 0);
	/**
	 * Rectangle representing the full, oriented image. x and y are zero.
	 */
	private Rectangle orientedOrigRect = new Rectangle(0, 0, 0, 0);
	/**
	 * Rectangle representing the zoomed, oriented image. x and y are relative
	 * to the full oriented image.
	 */
	private Rectangle orientedRect = new Rectangle(0, 0, 0, 0);
	/**
	 * Parameter specifying the orientation of the coordinate system: TL=(0,0),
	 * TR=(0,0), BR=(0,0), BL=(0,0), or Custom.
	 */
	/**
	 * The horizontal scaling factor to get from the screen image pixels to the
	 * file image pixels.
	 */
	double xScale;
	/**
	 * The vertical scaling factor to get from the screen image pixels to the
	 * file image pixels.
	 */
	double yScale;
	/**
	 * Indicates when a selection rectangle is being defined by dragging the
	 * mouse.
	 */
	private Boolean selectingOn = false;
	/**
	 * The canvas width.
	 */
	private int canvasWidth;
	/**
	 * The canvas height.
	 */
	private int canvasHeight;
	/**
	 * Indicates whether to draw the selection rectangle or not.
	 */
	private Boolean selectOn = false;
	/**
	 * Indicates the image has changed owing to a mouse up event. Is set to
	 * false when the necessary changes have been made. Used to prevent looping.
	 */
	private boolean imageChanged = true;
	/**
	 * Indicates the image has changed owing to a mouse up event. Used to do
	 * showView() when true. Could probably be combined with imageChanged.
	 */
	private Boolean newSelection = true;
	/**
	 * Keeps track of where the mouse went down.
	 */
	private int xSelectionStart;
	/**
	 * Keeps track of where the mouse went down.
	 */
	private int ySelectionStart;
	/**
	 * The last selected Rectangle in screen coordinates.
	 */
	private Rectangle selectedArea = new Rectangle(0, 0, 0, 0);
	/**
	 * The Display for this view.
	 */
	private Display display;
	/**
	 * A reference to the logger for this class.
	 */
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ImageComponentImage.class);

	private ImageView zoomAreaView;
	private LineView lineView;
	private ProfileView profileView;
	private ReliefView zoomReliefView;
	private RockingCurveView zoomRockingCurveView;
	boolean use1d = false; // TODO delete this
	PSF psf;
	private Boolean legendDraw = false;
	private Canvas canvasLegend;
	private GC legendCanvasGC;
	private GC imageCanvasGC;
	private boolean intoselection=false;
	private int secondSelectionX;
	private int secondSelectionY;
	private int selectionWidth;
	private int selectionHeight;
	private int startX;
	private int startY;
	private boolean keydownOnSelection;
	private boolean clickonselection;
	private int nbBoxSelected=0;
	private int varX;
	private int varY;
	private Rectangle RectangleSelectionLine=null;
	private Rectangle RectangleLinePts1=null;
	private Rectangle RectangleLinePts2=null;
	private boolean movePointAvailable1=false;
	private boolean movePointAvailable2=false;
	private boolean keydonwonselectionPTS1=false;
	private boolean keydonwonselectionPTS2=false;
	private int secondSelectionLineX;// coordinate of the second line point 
	private int secondSelectionLineY;
	private boolean movingpts1 =false; // know if pts1 is moving on
	private boolean movingpts2 =false; // know if pts1 is moving on
	private int firstSelectionLineX;
	private int firstSelectionLineY;
	private int coordinateFirstPtsX ;
	private int coordinateFirstPtsY;
	private int coordinateSecondPtsX ;
	private int coordinateSecondPtsY;
	private int movedX;
	private int movedY;
	private int tempWidth;
	private int tempHeight;
	private int tempWidthArea;
	private int tempHeightArea;
	
	private Rectangle RectangleSelection;

	/**
	 * Constructor.
	 * 
	 * @param iv
	 * @param controls
	 * @param imageCanvas
	 */
	public ImageComponentImage(ImageComponent iv, ImageComponentUI controls) {
		// Get the logger
		this.iv = iv;
		this.controls = controls;
		this.psf = new PSF( 6 );
		// Set the reference to this image in the ImageView before anything else
		// is done
		iv.setImage(this);
		controls.setImage(this);
	}

	/**
	 * Does initial setup for the canvas. The imageCanvas in ImageViewControls
	 * must be created first.
	 */
	public void initializeCanvas() {
		if (iv == null || controls == null)
			return;


		
		imageCanvas = controls.getImageCanvas();
		display = iv.getDisplay();
		imageCanvas.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		imageCanvas.setLayoutData(gridData);
		
		canvasLegend = controls.getCanvaslegend();
		canvasLegend.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		//
		legendCanvasGC = new GC(canvasLegend);

		//
		imageCanvasGC = new GC(imageCanvas);
		Rectangle bounds = imageCanvas.getBounds();
		canvasWidth = bounds.width;
		canvasHeight = bounds.height;

		imageCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageCanvasGC.dispose();
			}
		});
		imageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (image == null)
					return;
				Rectangle bounds = imageCanvas.getBounds();
				event.gc.fillRectangle(0, 0, bounds.width, bounds.height);
				if (canvasWidth != bounds.width
						|| canvasHeight != bounds.height) {
					double imageXScale = xScale;
					double imageYScale = yScale;
					createScreenImage(imageData);
					// xScale and yScale have changed. Scale the selected area
					// to the new image size so as to keep the same area
					// selected in the image. Round the new scaled area so as
					// not to lose pixels
					imageXScale /= xScale;
					imageYScale /= yScale;
					// Change the selectedArea to track width and height
					// changes (is subject to roundoff)
					selectedArea.width = (int) Math
							.round((double) selectedArea.width * imageXScale);
					selectedArea.height = (int) Math
							.round((double) selectedArea.height * imageYScale);
					selectedArea.x = (int) Math.round((double) selectedArea.x
							* imageXScale);
					selectedArea.y = (int) Math.round((double) selectedArea.y
							* imageYScale);
					canvasWidth = bounds.width;
					canvasHeight = bounds.height;
					if (debug1) {
						System.out
								.println("\npaintControl calling showSelection");
						System.out.printf("  \"%s\"\n", iv.getPartName());
					}
				}
				displayImage();
			}
		});
		
		
		
	

		imageCanvas.addMouseMoveListener(new MouseMoveListener() {
			// KE: setXORMode doesn't work on some Macs. There is no easy
			// workaround, so use @SuppressWarnings to avoid warnings that
			// can't be fixed.
			@SuppressWarnings("deprecation")
			public void mouseMove(MouseEvent event) {
				if (image != null) {
					
					
					showPixelAtCursor(event.x, event.y);
					 Cursor cursor = display.getSystemCursor(SWT.CURSOR_ARROW);						
					// imageCanvas.setCursor(cursor);
					
					
					if (selectingOn && !keydownOnSelection) {
						cursor = display.getSystemCursor(SWT.CURSOR_HAND);
						imageCanvas.setCursor(cursor);
						int width = event.x - xSelectionStart;
						int height = event.y - ySelectionStart;
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						ZoomSelection zoomSelection = iv.getZoomSelection();
						if (zoomSelection == ZoomSelection.AREA
								|| zoomSelection == ZoomSelection.PROFILE
								|| zoomSelection == ZoomSelection.RELIEF
								|| zoomSelection == ZoomSelection.ROCKINGCURVE) {
							
							Rectangle selectedRectangle = new Rectangle(
									xSelectionStart, ySelectionStart, width,
									height);
							
							RectangleSelection=selectedRectangle;									
							imageCanvasGC.setLineWidth(1);
							imageCanvasGC.setXORMode(true);
							imageCanvasGC.drawRectangle(selectedRectangle);
							imageCanvasGC.setXORMode(false);
						} else if ((zoomSelection == ZoomSelection.LINE) && !keydonwonselectionPTS1 && !keydonwonselectionPTS2 && !clickonselection)  {
							// imageCanvasGC.setLineWidth(iv.getLinePeakWidth());
							imageCanvasGC.setXORMode(true);
							imageCanvasGC.drawLine(xSelectionStart,
									ySelectionStart, event.x, event.y);
							imageCanvasGC.setXORMode(false);
							
							
						} else if (zoomSelection == ZoomSelection.NONE) {
							// Do nothing
						}					
					}
					
					
					
					
					if ((iv.getZoomSelection() == ZoomSelection.AREA) && inselectbox(event,RectangleSelection) ){
						cursor = display.getSystemCursor(SWT.CURSOR_HAND);
						imageCanvas.setCursor(cursor);
						intoselection=true;												
					}
					
					else if ((iv.getZoomSelection() == ZoomSelection.RELIEF) && inselectbox(event,RectangleSelection) ){
						cursor = display.getSystemCursor(SWT.CURSOR_HAND);
						imageCanvas.setCursor(cursor);
						intoselection=true;												
					}
					
					else if  (((iv.getZoomSelection() == ZoomSelection.LINE)||(iv.getZoomSelection() == ZoomSelection.PROFILE)) && inselectbox(event,RectangleSelectionLine) && !inselectbox(event,RectangleLinePts1) 
							 && !inselectbox(event,RectangleLinePts2) && !keydonwonselectionPTS1 && !keydonwonselectionPTS2){
						cursor = display.getSystemCursor(SWT.CURSOR_HAND);
						imageCanvas.setCursor(cursor);
						intoselection=true;												
					}
					
				
					else if ((iv.getZoomSelection() == ZoomSelection.LINE) && inselectbox(event,RectangleLinePts1) ){ //detect first point
						cursor = display.getSystemCursor(SWT.CURSOR_SIZEALL);
						imageCanvas.setCursor(cursor);			
						movePointAvailable1=true;
						movePointAvailable2=false;
						intoselection=false;
						
						
					}
					
					else if ((iv.getZoomSelection() == ZoomSelection.LINE) && inselectbox(event,RectangleLinePts2) ){ //detect second point
						cursor = display.getSystemCursor(SWT.CURSOR_SIZEALL);
						imageCanvas.setCursor(cursor);		
						movePointAvailable1=false;
						movePointAvailable2=true;
						intoselection=false;
					}

					
					else {
						
						 cursor = display.getSystemCursor(SWT.CURSOR_ARROW);
						 imageCanvas.setCursor(cursor);
						 intoselection=false;
						 movePointAvailable1=false;
						 movePointAvailable2=false;
					}
			
					
					if(keydonwonselectionPTS1 && !clickonselection){ //if keydonwonselectionPTS1, the point number 1 can be moved

						drawImage(false);
						cursor = display.getSystemCursor(SWT.CURSOR_SIZEALL);
						imageCanvas.setCursor(cursor);								
						imageCanvasGC.setXORMode(true);
						imageCanvasGC.drawLine( secondSelectionLineX, secondSelectionLineY,event.x, event.y);  //Remember : change values
						imageCanvasGC.drawRectangle(event.x-4, event.y-4,
								8, 8);
						imageCanvasGC.drawRectangle(secondSelectionLineX-4, secondSelectionLineY-4,
								8, 8);
						imageCanvasGC.setXORMode(false);
						movingpts1=true;
						movingpts2=false;
						
						tempWidth=selectionWidth;//record coordinate
						tempHeight=selectionHeight;
						tempWidthArea=selectedArea.width;
						tempHeightArea=selectedArea.height;
						
					 	selectedArea.x=event.x;
						selectedArea.y = event.y;
						selectedArea.width = coordinateSecondPtsX-event.x;
						selectionWidth=selectedArea.width;
						selectedArea.height = coordinateSecondPtsY-event.y ;
						selectionHeight=selectedArea.height;	
						
						imageChanged = true;
						newSelection = true;										
						showSelectedLine();
						
						selectionWidth=tempWidth; //Re-place old coordinate for the next
						selectionHeight=tempHeight;
						selectedArea.width=tempWidthArea;
						selectedArea.height=tempHeightArea;
						setSelection(selectedArea);
						
					}
					
					else if(keydonwonselectionPTS2 && !clickonselection){//else if keydonwonselectionPTS2, the point number 2 can be moved

						cursor = display.getSystemCursor(SWT.CURSOR_SIZEALL);
						imageCanvas.setCursor(cursor);		
						drawImage(false);
						imageCanvasGC.setXORMode(true);
						imageCanvasGC.drawLine( firstSelectionLineX,firstSelectionLineY,event.x, event.y);//Remember : change values
						imageCanvasGC.drawRectangle(event.x-4, event.y-4,
								8, 8);
						imageCanvasGC.drawRectangle(firstSelectionLineX-4, firstSelectionLineY-4,
								8, 8);
						imageCanvasGC.setXORMode(false);						
						movingpts1=false;
						movingpts2=true;
							
						
						tempWidth=selectionWidth;//record coordinate
						tempHeight=selectionHeight;
						tempWidthArea=selectedArea.width;
						tempHeightArea=selectedArea.height;
						
						selectedArea.x=coordinateFirstPtsX;		//change coordinate to display			
						selectedArea.y = coordinateFirstPtsY;						
						selectedArea.width = event.x-coordinateFirstPtsX;
						selectionWidth=selectedArea.width;
						selectedArea.height = event.y-coordinateFirstPtsY ;
						selectionHeight=selectedArea.height;
			
						imageChanged = true;
						newSelection = true;										
						showSelectedLine();
						
						selectionWidth=tempWidth; //Re-place old coordinate for the next
						selectionHeight=tempHeight;
						selectedArea.width=tempWidthArea;
						selectedArea.height=tempHeightArea;
						setSelection(selectedArea);
		
					}
				
					//if click on box
					if(clickonselection && !(iv.getZoomSelection() == ZoomSelection.RELIEF)){

						
						//if moves again the box re calculate coordinates
						if(nbBoxSelected>=2){
							secondSelectionX=varX;
							secondSelectionY=varY;
						}
									
						drawImage(false);
						Rectangle selectedRectangle = null;
						selectedArea.x=event.x+secondSelectionX-startX;
					 	selectedArea.y=event.y+secondSelectionY-startY;
			
						
						 selectedRectangle = new Rectangle(
								selectedArea.x, selectedArea.y, selectionWidth,
								selectionHeight);
					
						RectangleSelection=selectedRectangle;									

						//box redrew each moves
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						showSelection(false); //redraw the canvas each moves			
						setSelection(selectedArea);
					}		
					
					else if(clickonselection && (iv.getZoomSelection() == ZoomSelection.RELIEF)){
				
						//if moves again the box re calculate coordinates
						if(nbBoxSelected>=2){
							secondSelectionX=varX;
							secondSelectionY=varY;
						}
									
						drawImage(false);
						Rectangle selectedRectangle = null;
						selectedArea.x=event.x+secondSelectionX-startX;
					 	selectedArea.y=event.y+secondSelectionY-startY;
			
						
						 selectedRectangle = new Rectangle(
								selectedArea.x, selectedArea.y, selectionWidth,
								selectionHeight);
					
						RectangleSelection=selectedRectangle;									

						//box redrew each moves
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						
							IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
						//System.out.println(prefs.getString(fable.imageviewer.preferences.PreferenceConstants.P_RELIEFMOVE));
						if (prefs.getString(fable.imageviewer.preferences.PreferenceConstants.P_RELIEFMOVE) == "true"){
						showSelection(false); //redraw the canvas each moves			
						}
						else{
							
							imageCanvasGC.setXORMode(true);
							imageCanvasGC.drawRectangle(selectedArea);
							imageCanvasGC.setXORMode(false);
						}
						setSelection(selectedArea);
						
					}
				}			
			}
		});
		
		
		
		
		
		imageCanvas.addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event event) {
				controls.setStatusText("");
				if (selectingOn) {
					drawImage(false);
					selectingOn = false;
					RectangleSelection=null;
					nbBoxSelected=0;
				}
			}
		});

		// Add listener for ESC to abort selection
		imageCanvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ev) {
				switch (ev.keyCode) {
				case SWT.ESC:
					if (selectingOn) {
					
						drawImage(false);
						selectingOn = false;
						RectangleSelection=null;
						nbBoxSelected=0;
						
					}
					break;
				}
			}
		});

		
		
	
		
		
		imageCanvas.addMouseListener(new MouseAdapter() {
			
			public void mouseDoubleClick(MouseEvent event) {
				if (selectingOn) {
					
					drawImage(false);
					selectingOn = false;
					RectangleSelection=null;
					selectOn = false;
					nbBoxSelected=0;
				}
			}
			
		

			public void mouseDown(MouseEvent ev) {
	
			
				if (image == null)
					return;
				
				if (movePointAvailable1) //if first point is available to move 
				keydonwonselectionPTS1=true;
				
				else if (movePointAvailable2)//if second point is available to move 
				keydonwonselectionPTS2=true;	
				
			
				if (ev.button == 1 && !intoselection  ) {
					// Btn1
					// Only allow these actions for Btn1. If allowed for Btn3,
					// they happen when the context menu is selected. In the
					// future Btn2 may do something different, so don't let
					// users get used to using it.
					if ((ev.stateMask & SWT.CTRL) != 0) {
						selectingOn = false;
						selectOn = false;
						//showZoom(ev, true);
					} else if ((ev.stateMask & SWT.SHIFT) != 0) {
						selectingOn = false;
						selectOn = false;
						//showZoom(ev, false);
					} else if ((ev.stateMask & SWT.ALT) != 0) {
						selectingOn = false;
						selectOn = false;
						resetZoom();
					} else {
						nbBoxSelected=0;
						selectingOn = true;
						xSelectionStart = ev.x;
						ySelectionStart = ev.y;
					}
					keydownOnSelection=false;
				}
				
				
				else if (intoselection){
					nbBoxSelected=nbBoxSelected+1;
					startX=ev.x;
					startY=ev.y;
					keydownOnSelection=true;
					clickonselection=true; 					
				}
				
				
				
				
			}

			public void mouseUp(MouseEvent ev) {
				
				//calculate the original coordinate of the first and the second point
				if (!keydonwonselectionPTS1 && !keydonwonselectionPTS2 ){
			
				firstSelectionLineX=xSelectionStart;
				firstSelectionLineY=ySelectionStart;
				secondSelectionLineX=ev.x;
				secondSelectionLineY=ev.y;		
				}
		
				
				if(nbBoxSelected>=2){ // recalculate the point after re selecting the boxes
					secondSelectionX=varX;
					secondSelectionY=varY;
				}				
				
				clickonselection=false; 
				 if (keydownOnSelection) {			//for ZOOM.AREA		
					 

					 movedX=ev.x-startX;
					 movedY=ev.y-startY;
					 
					 	selectedArea.x=ev.x+secondSelectionX-startX;
					 	varX=selectedArea.x;
					 	selectedArea.y=ev.y+secondSelectionY-startY;
					 	varY=selectedArea.y;			

						selectedArea.width = selectionWidth;						
						selectedArea.height = selectionHeight;		
						
						coordinateFirstPtsX=coordinateFirstPtsX+movedX; 		//recalculate point after moving			
						coordinateFirstPtsY=coordinateFirstPtsY+movedY;				
						coordinateSecondPtsX=coordinateSecondPtsX+movedX;					
						coordinateSecondPtsY=coordinateSecondPtsY+movedY;
												
						firstSelectionLineX=coordinateFirstPtsX; //point to display 
						firstSelectionLineY=coordinateFirstPtsY;						
						secondSelectionLineX=coordinateSecondPtsX;
						secondSelectionLineY=coordinateSecondPtsY;
						
				
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						showSelection(false);
						setSelection(selectedArea);
					
				 }
					
				 else if ((xSelectionStart != ev.x || ySelectionStart != ev.y) && !keydownOnSelection && !movingpts1 && !movingpts2){ // on selecting AREA or LINE
					 

					 
						selectedArea.x = xSelectionStart; 
						secondSelectionX=xSelectionStart;
						selectedArea.y = ySelectionStart;
						secondSelectionY=ySelectionStart;
						selectedArea.width = ev.x - selectedArea.x;
						selectionWidth=selectedArea.width;
						selectedArea.height = ev.y - selectedArea.y;
						selectionHeight=selectedArea.height;	
						
					
						coordinateFirstPtsX=xSelectionStart; //coordinate of the first point
						coordinateFirstPtsY=ySelectionStart;
						coordinateSecondPtsX=xSelectionStart+selectionWidth;//coordinate of the second point
						coordinateSecondPtsY=ySelectionStart+selectionHeight;
												
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						showSelection(false);
						setSelection(selectedArea);
					
					}
				 
				 else if ((xSelectionStart != ev.x || ySelectionStart != ev.y) && !keydownOnSelection && movingpts1){ // on selecting AREA or LINE / if  moving first point
		

					 
					 	selectedArea.x=ev.x;
					 	secondSelectionX=ev.x;
						selectedArea.y = ev.y;
						secondSelectionY=ev.y;	
						selectedArea.width = coordinateSecondPtsX-ev.x;
						selectionWidth=selectedArea.width;
						selectedArea.height = coordinateSecondPtsY-ev.y ;
						selectionHeight=selectedArea.height;
												
						coordinateFirstPtsX=ev.x; //coordinate of the first point
						coordinateFirstPtsY=ev.y;
						
						firstSelectionLineX=ev.x;//recalculate the first point to display it when moving it
						firstSelectionLineY=ev.y;
					
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						showSelection(false);
						setSelection(selectedArea);
					
					}
				 
				 else if ((xSelectionStart != ev.x || ySelectionStart != ev.y) && !keydownOnSelection && movingpts2){ // on selecting AREA or LINE

				
						selectedArea.x=coordinateFirstPtsX;
					 	secondSelectionX=coordinateFirstPtsX;
						selectedArea.y = coordinateFirstPtsY;
						secondSelectionY=coordinateFirstPtsY;	
						selectedArea.width = ev.x-coordinateFirstPtsX;
						selectionWidth=selectedArea.width;
						selectedArea.height = ev.y-coordinateFirstPtsY ;
						selectionHeight=selectedArea.height;
						 
						secondSelectionY=coordinateFirstPtsY;
						secondSelectionX=coordinateFirstPtsX;
					
						coordinateSecondPtsX=ev.x; //coordinate of the second point
						coordinateSecondPtsY=ev.y;
						
						secondSelectionLineX=coordinateSecondPtsX;//recalculate the second point to display it when moving it
						secondSelectionLineY=coordinateSecondPtsY;
					 			 
						imageCanvasGC.setForeground(display
								.getSystemColor(SWT.COLOR_WHITE));
						drawImage(false);
						imageChanged = true;
						newSelection = true;
						if (debug1) {
							System.out.println("\nmouseUp calling showSelection "
									+ "imageChanged=" + imageChanged);
							System.out.printf("  \"%s\"\n", iv.getPartName());
						}
						showSelection(false);
						setSelection(selectedArea);
					
					}
				 

				
					
				 keydonwonselectionPTS1=false;// stop moving
				 keydonwonselectionPTS2=false;
			 				 
				if (image == null)
					return;
				if (!selectingOn)
					return;			
				/* only update the selection if something has been selected */
			
			
				selectingOn = false;
			}
		});
		createDropTarget();
	}

	/**
	 * Called when the View is to be disposed
	 */
	public void dispose() {
		if (imageCanvas != null) imageCanvas.dispose();
		if (image != null) image.dispose();
		if (legend != null) legend.dispose();
		
		// We help the garbage collector out a litte:
		zoomAreaView         = null;
		lineView             = null;
		profileView          = null;
		zoomReliefView       = null;
		zoomRockingCurveView = null;

	}

	/**
	 * Calculates the origRect, orientedOrigRect, and orientedRect from the
	 * imageRect and the imageModel.
	 */
	public void calculateMainRectangles() {
		origRect = iv.getImageModel().getRect(); // Only used here
		orientedOrigRect.x = 0; // KE: x,y not used -> this could be a Point
		orientedOrigRect.y = 0;
		switch (iv.getOrientation()) {
		default:
		case O_MOOM:
			orientedRect.x = imageRect.x;
			orientedRect.y = imageRect.y;
			orientedRect.width = imageRect.width;
			orientedRect.height = imageRect.height;
			orientedOrigRect.width = origRect.width;
			orientedOrigRect.height = origRect.height;
			break;
		case O_MOOP:
			orientedRect.x = origRect.width - imageRect.width - imageRect.x;
			orientedRect.y = imageRect.y;
			orientedRect.width = imageRect.width;
			orientedRect.height = imageRect.height;
			orientedOrigRect.width = origRect.width;
			orientedOrigRect.height = origRect.height;
			break;
		case O_POOM:
			orientedRect.x = imageRect.x;
			orientedRect.y = origRect.height - imageRect.height - imageRect.y;
			orientedRect.width = imageRect.width;
			orientedRect.height = imageRect.height;
			orientedOrigRect.width = origRect.width;
			orientedOrigRect.height = origRect.height;
			break;
		case O_POOP:
			orientedRect.x = origRect.width - imageRect.width - imageRect.x;
			orientedRect.y = origRect.height - imageRect.height - imageRect.y;
			orientedRect.width = imageRect.width;
			orientedRect.height = imageRect.height;
			orientedOrigRect.width = origRect.width;
			orientedOrigRect.height = origRect.height;
			break;
		case O_OMMO:
			orientedRect.x = imageRect.y;
			orientedRect.y = imageRect.x;
			orientedRect.width = imageRect.height;
			orientedRect.height = imageRect.width;
			orientedOrigRect.width = origRect.height;
			orientedOrigRect.height = origRect.width;
			break;
		case O_OMPO:
			orientedRect.x = imageRect.y;
			orientedRect.y = origRect.width - imageRect.width - imageRect.x;
			orientedRect.width = imageRect.height;
			orientedRect.height = imageRect.width;
			orientedOrigRect.width = origRect.height;
			orientedOrigRect.height = origRect.width;
			break;
		case O_OPMO:
			orientedRect.x = origRect.height - imageRect.height - imageRect.y;
			orientedRect.y = imageRect.x;
			orientedRect.width = imageRect.height;
			orientedRect.height = imageRect.width;
			orientedOrigRect.width = origRect.height;
			orientedOrigRect.height = origRect.width;
			break;
		case O_OPPO:
			orientedRect.x = origRect.height - imageRect.height - imageRect.y;
			orientedRect.y = origRect.width - imageRect.width - imageRect.x;
			orientedRect.width = imageRect.height;
			orientedRect.height = imageRect.width;
			orientedOrigRect.width = origRect.height;
			orientedOrigRect.height = origRect.width;
			break;
		}
		if (debug) {
			System.out.printf("\ncalculateMainRectangles: \n");
			System.out.println("origRect=" + origRect);
			System.out.println("imageRect=" + imageRect);
			System.out.println("orientedOrigRect=" + orientedOrigRect);
			System.out.println("orientedRect=" + orientedRect);
		}
	}

	/**
	 * Draws the current selection on the screen and calls showSelectedArea,
	 * showSelectedLine, or showProfile, as appropriate. Thus when it is called,
	 * other views will be updated.
	 */
	// KE: setXORMode doesn't work on some Macs. There is no easy workaround, so
	// use @SuppressWarnings to avoid warnings that can't be fixed.
	@SuppressWarnings("deprecation")
	public void showSelection(final boolean force) {
		if (debug1) {
			System.out.println("showSelection imageChanged=" + imageChanged);
			System.out.printf("  \"%s\"\n", iv.getPartName());
		}
		// Only draw the selection if we are currently selecting or have
		// selected something already
		if (!force) if (!selectingOn && !selectOn) return;
		
		ZoomSelection zoomSelection = iv.getZoomSelection();
		if (debug) {
			logger.debug("selectingOn " + selectingOn + " selectOn " + selectOn
					+ " zoomSelection " + zoomSelection.getName());
		}
		if (zoomSelection == ZoomSelection.AREA
				|| zoomSelection == ZoomSelection.RELIEF
				|| zoomSelection == ZoomSelection.ROCKINGCURVE) {
			if (!force) {
				imageCanvasGC.setXORMode(true);
				imageCanvasGC.drawRectangle(selectedArea);
				imageCanvasGC.setXORMode(false);
			}
			if (imageChanged || force) showSelectedArea(selectedArea, true);
			selectOn = true;
		} else if (zoomSelection == ZoomSelection.LINE) {
			// setXORMode is not supported on some platforms
			if (!force) {
			
				imageCanvasGC.setXORMode(true);
				imageCanvasGC.drawLine(selectedArea.x, selectedArea.y,
						selectedArea.x + selectedArea.width, selectedArea.y
						+ selectedArea.height);
				RectangleSelectionLine = new Rectangle(selectedArea.x, selectedArea.y,
						selectedArea.width, selectedArea.height);
				RectangleLinePts1 =  new Rectangle(selectedArea.x -4, selectedArea.y-4,
						8, 8);
				
				imageCanvasGC.drawRectangle(selectedArea.x -4, selectedArea.y-4,
						8, 8);
				RectangleLinePts2 =  new Rectangle(selectedArea.x+selectedArea.width -4, selectedArea.y+selectedArea.height-4,
						8, 8);		
				imageCanvasGC.drawRectangle(selectedArea.x+selectedArea.width -4, selectedArea.y+selectedArea.height-4,
						8, 8);		
				imageCanvasGC.setLineWidth(1);
				imageCanvasGC.setXORMode(false);
				movingpts1=false;
				movingpts2=false;
			
			}
			if (imageChanged || force) showSelectedLine();
			selectOn = true;
		} else if (zoomSelection == ZoomSelection.PROFILE) {
			if (!force) {
				imageCanvasGC.drawRectangle(selectedArea);
			}
			if (imageChanged || force) showProfile();
			selectOn = true;
		}
		newSelection = false;
		// KE: Test 4 April 2009
		imageChanged = false;
	}

	/**
	 * Zooms in or out centered on the mouse coordinates.
	 * 
	 * @param ev
	 * @param in
	 *            True to zoom in, false to zoom out.
	 */
	public void showZoom(MouseEvent ev, boolean in) {
		// Do not allow zoom for the default view
		if (iv.getSecondaryId().equals(ImageComponent.SECONDARY_ID_MAIN)) {
			return;
		}
		// Turn on off any selection in the zoom area
		setSelectOn(false);
		float scale = in ? 1 / ZOOMFACTOR_LARGE : ZOOMFACTOR_LARGE;
		Rectangle bounds = imageCanvas.getBounds();
		int width = (int) (bounds.width * scale + .5f);
		int height = (int) (bounds.height * scale + .5f);
		int x = ev.x - width / 2;
		int y = ev.y - height / 2;
		Rectangle rect = new Rectangle(x, y, width, height);
		// Convert to original image coordinates. We want the rectangle to be
		// ordered. Use true for ordered.
		Rectangle origRect = screenRectangleToImageRectangle(rect, true);
		float[] zoomAreaAsFloat;
		if (!iv.isImageDiffOn()) {
			zoomAreaAsFloat = iv.getImageModel().getData(origRect);
		} else {
			zoomAreaAsFloat = iv.getImageDiffModel().getData(origRect);
		}
		// Determine the area to display
		if (!iv.isImageDiffOn()) {
			changeImageRect(origRect, zoomAreaAsFloat, iv.getImageModel()
					.getFileName(), iv.getImageModel());
		} else {
			changeImageRect(origRect, zoomAreaAsFloat, iv.getImageModel()
					.getFileName(), iv.getImageDiffModel());
		}
	}

	/**
	 * Resets the zoom to full size. This affects the zoom area whether done in
	 * the main view or the zoom view.
	 */
	public void resetZoom() {
		if (image == null)
			return;
		selectingOn = false;
		selectOn = false;
		showSelectedArea(origRect, false);
	}

	/**
	 * Draws red squares around all peaks using the currentLinePeakWidth.
	 */
	public void showPeaks() {
		Vector<Float> peaks = iv.getPeaks();
		if (peaks != null) {
			Rectangle rect;
			Color color = display.getSystemColor(SWT.COLOR_RED);
			imageCanvasGC.setForeground(color);
			imageCanvasGC.setLineWidth(1);
			int x, y;
			Point point = new Point(0, 0);
			int peakMarkerSize = iv.getPeakMarkerSize();
			for (int i = 0; i < peaks.size() / 2; i++) {
				// Convert TotalCrys to screen coordinates
				// Add .5 as the pixel values are effectively at the upper left
				point = tcToScreen(peaks.elementAt(i * 2) + .5f, peaks
						.elementAt(i * 2 + 1) + .5f);
				x = point.x;
				y = point.y;
				x -= (peakMarkerSize / 2);
				y -= (peakMarkerSize / 2);
				rect = new Rectangle(x, y, peakMarkerSize, peakMarkerSize);
				imageCanvasGC.drawRectangle(rect);
			}
		}
	}

	/**
	 * First calls showPeaks to draw red squares, then draws green squares
	 * around all peaks in the array using the currentLinePeakWidth.
	 * 
	 * @param peaksToColor
	 *            An array of TotalCryst coordinates [y0,z0,y1,z1,...] to have a
	 *            green square. Used in PeakSearchSptView to show the selected
	 *            peaks in green.
	 */
	public void showSelectedPeaks(float[] peaksToColor) {
		showPeaks();
		if (peaksToColor != null) {
			Rectangle rect;
			Color color = display.getSystemColor(SWT.COLOR_GREEN);
			imageCanvasGC.setForeground(color);
			imageCanvasGC.setLineWidth(1);
			int x, y, index;
			Point point = new Point(0, 0);
			int peakMarkerSize = iv.getPeakMarkerSize();
			for (int i = 0; i < peaksToColor.length / 2; i++) {
				index = i * 2;
				// Add .5 as the pixel values are effectively at the upper left
				point = tcToScreen(peaksToColor[index] + .5f,
						peaksToColor[index + 1] + .5f);
				// Convert TotalCrys to screen coordinates
				x = point.x;
				y = point.y;
				x -= (peakMarkerSize / 2);
				y -= (peakMarkerSize / 2);
				rect = new Rectangle(x, y, peakMarkerSize, peakMarkerSize);
				imageCanvasGC.drawRectangle(rect);
			}
		}
	}

	/**
	 * Called when the mouse moves in the image canvas. Show the coordinates of
	 * the image at the point under the mouse and the value.
	 * 
	 * @param mx
	 *            Mouse x.
	 * @param my
	 *            Mouse y.
	 */
	private void showPixelAtCursor(int mx, int my) {
		if (iv.getImageModel() != null) {
			// Get unscaled coordinates in the zoomed, oriented image
			int x4, y4;
			x4 = (int) (mx * xScale);
			y4 = (int) (my * yScale);
			if (x4 < 0 || x4 >= orientedRect.width || y4 < 0
					|| y4 >= orientedRect.height) {
				if (controls != null) {
					controls.setStatusText("");
					if (debug) {
						controls.setStatusText("Out of bounds 1");
					}
				}
				return;
			}
			// Change to original image coordinates to get the pixel value
			Point p = orientedToImage(new Point(x4, y4));
			int x1 = p.x;
			int y1 = p.y;
			// if (false && debug) {
			// // Makes lots of printout
			// boolean out = x1 < 0 || x1 >= imageRect.width || y1 < 0
			// || y1 >= imageRect.height;
			// System.out.printf("[1] x1=%d y1=%d %s\n", x1, y1,
			// out ? "Out of bounds" : "");
			// }
			// Check again in case roundoff put coordinates out of bounds
			if (x1 < 0 || x1 >= iv.getImageModel().getWidth() || y1 < 0
					|| y1 >= iv.getImageModel().getHeight()) {
				if (controls != null) {
				}
				if (debug) {
					controls.setStatusText("Out of bounds 2");
				} else {
					controls.setStatusText("");
				}
				return;
			}
			// Get the pixel value from the image model
			float pixel = 0;
			if (!iv.isImageDiffOn()) {
				pixel = iv.getImageModel().getData(y1, x1);
			} else {
				pixel = iv.getImageDiffModel().getData(y1, x1);
			}

			// Get the coordinate string and display it
			if (controls != null) {
				controls.setStatusText(iv.getCoordinates().getCoordinateString(
						x4 + orientedRect.x, y4 + orientedRect.y, pixel));
			}
			// if (false && debug) {
			// System.out.printf("x4=%d y4=%d x1=%d y1=%d x5=%d y5=%d\n", x4,
			// y4, x1, y1, x4 + orientedRect.x, y4 + orientedRect.x);
			// System.out.println("imageRect: " + imageRect);
			// System.out.println("origRect: " + origRect);
			// System.out.println("orientedRect: " + orientedRect);
			// System.out.println("orientedOrigRect: " + orientedOrigRect);
			// }
		}
	}

	/**
	 * Opens a new view or resets the existing one to display the given
	 * Rectangle.
	 * 
	 * @param rect
	 *            The Rectangle in screen coordinates.
	 * @param screen
	 *            True if rect is in screen coordinates, false if rect is in
	 *            original image coordinates.
	 */
	private void showSelectedArea(Rectangle rect, boolean screen) {
		// if (debug || debug1) {
		// System.out.printf(
		// "showSelectedArea: x=%d y=%d width=%d height=%d\n",
		// selectedArea.x, selectedArea.y, selectedArea.width,
		// selectedArea.height);
		// if (rect == selectedArea) {
		// System.out.printf("  selectedArea: "
		// + "xSelectionStart=%d ySelectionStart=%d\n",
		// xSelectionStart, ySelectionStart);
		// }
		// System.out.println("imageChanged=" + imageChanged);
		// }
		// Convert to original image coordinates
		Rectangle origRect;
		if (screen) {
			// We want the rectangle to be ordered. Use true for ordered.
			origRect = screenRectangleToImageRectangle(rect, true);
		} else {
			origRect = rect;
		}
		// if (false && debug) {
		if (debug1) {
			System.out.printf(
					"  image:          x=%d y=%d width=%d height=%d\n",
					origRect.x, origRect.y, origRect.width, origRect.height);
			System.out.printf("  \"%s\"\n", iv.getPartName());
		}
		try {
			float[] zoomAreaAsFloat;
			// Calculate zoomAreaAsFloat
			// TODO: KE: statistics currently necessary for relief and rocking
			// curve. NG: Calculate it on demand, that is where it is used.
			if (!iv.isImageDiffOn()) {
				zoomAreaAsFloat = iv.getImageModel().getData(origRect);
			} else {
				zoomAreaAsFloat = iv.getImageDiffModel().getData(origRect);
			}
			// Set the appropriate view
			ZoomSelection zoomSelection = iv.getZoomSelection();
			// DEBUG
			// System.out.println("ImageViewImage.showSelectedArea: "
			// + iv.getSecondaryId() + " " + zoomSelection.getName());
			
			
			IViewPart viewPart = zoomSelection.getViewPart();
			if (viewPart == null) return;
			EclipseUtils.getPage().bringToTop(viewPart);
			
			if (zoomSelection == ZoomSelection.AREA) {
				
					zoomAreaView = (ImageView)viewPart;
					if (zoomAreaView != null) {
						// Turn on off any selection in the zoom area
						zoomAreaView.getImage().setSelectOn(false);
						if (!iv.isImageDiffOn()) {
							zoomAreaView.getImage().changeImageRect(origRect,
									zoomAreaAsFloat,
									iv.getImageModel().getFileName(),
									iv.getImageModel());
							zoomAreaView.setPartName(zoomAreaView
									.getSecondaryId()
									+ " " + iv.getFileName());
						} else {
							zoomAreaView.getImage().changeImageRect(origRect,
									zoomAreaAsFloat,
									iv.getImageModel().getFileName(),
									iv.getImageDiffModel());
							zoomAreaView.setPartName(zoomAreaView
									.getSecondaryId()
									+ " Diff " + iv.getFileName());
						}
						zoomAreaView.transferSelectedSettings(iv);
					}
				
			} else if (zoomSelection == ZoomSelection.RELIEF) {
				
						zoomReliefView = (ReliefView) viewPart;
						if (zoomReliefView != null) {
							Statistics areaStatistics = null;
							if (!iv.isImageDiffOn()) {
								areaStatistics = iv.getImageModel().getStatistics(origRect);
							} else {
								areaStatistics = iv.getImageDiffModel().getStatistics(origRect);
							}
						zoomReliefView.setImageAsFloat(zoomAreaAsFloat,
								origRect.width, origRect.height,
								areaStatistics.getMinimum(), areaStatistics.getMaximum(),
								areaStatistics.getMean(),
								iv.getImageModel().getFileName());
						}
				
			} else if (zoomSelection == ZoomSelection.ROCKINGCURVE) {
				
				
					zoomRockingCurveView = (RockingCurveView)viewPart;
					if (zoomRockingCurveView != null) {
						// KE: TODO Check using y2, z2
						int x2 = (origRect.width == 0) ? origRect.x
								: origRect.x + origRect.width - 1;
						int y2 = (origRect.height == 0) ? origRect.y
								: origRect.y + origRect.height - 1;
						zoomRockingCurveView.setCenterArea("Rocking Curve "
								+ iv.getFileName(), SampleController
								.getController().getCurrentFileIndex(),
								origRect.x, origRect.y, x2, y2);
					}
			}
			
		} catch (Throwable ex) {
			FableUtils.excMsg(this, "Unable to show selected area", ex);
		}
	}

	/**
	 * Open ZoomLineView and plot the selected line profile.
	 */
    private void showSelectedLine() {
		int x1, x2, y1, y2;
		String xTitle, yTitle;
		// Convert to oriented image coordinates. We want the rectangle to be
		// unordered so the line can tilt either way. Use false for ordered.
		Rectangle lineRect = screenRectangleToOrientedImageRectangle(
				selectedArea, false);
		// These should be in bounds and ordered properly
		x1 = lineRect.x;
		y1 = lineRect.y;
		x2 = lineRect.x + lineRect.width - 1;
		y2 = lineRect.y + lineRect.height - 1;
		// if (false) {
		// Level oldLevel = logger.getLevel();
		// logger.setLevel(Level.DEBUG);
		// logger.debug("integrate for selectedArea x=" + selectedArea.x
		// + " y=" + selectedArea.y + " width=" + selectedArea.width
		// + " height=" + +selectedArea.height);
		// logger.debug("integrate for x1=" + x1 + " y1=" + y1 + " to x2="
		// + x2 + " y2=" + y2);
		// logger.setLevel(oldLevel);
		// }
		// Define a generic Point and set its values to avoid memory allocation
		// and GC
		Point p = new Point(0, 0);
		int x, y, idx, temp;
		double[] vals = new double[] { 0, 0 };
		float val, half;
		boolean inverted = false;
		try {
			float slope = Float.NaN;
			float[] pixels;
			float[] intensity;
			if (Math.abs(x2 - x1) >= Math.abs(y2 - y1)) {
				// Area is more aligned to the x axis
				xTitle = "Pixels in " + iv.getCoordinates().getXName();
				yTitle = "Intensity";
				half = .5f * (float) iv.getCoordinates().getPixelWidth();
				inverted = iv.getCoordinates().isXInverted();
				// Order so x increases
				if (x2 < x1) {
					temp = x1;
					x1 = x2;
					x2 = temp;
					temp = y1;
					y1 = y2;
					y2 = temp;
				}
				int len = x2 - x1 + 1;
				if (LINEVIEW_HISTOGRAM) {
					pixels = new float[2 * len + 2];
					intensity = new float[2 * len + 2];
					intensity[0] = intensity[2 * len + 1] = 0;
				} else {
					pixels = new float[len];
					intensity = new float[len];
				}
				if ((x2 - x1) != 0)
					slope = (float) (y2 - y1) / (float) (x2 - x1);
				logger.debug("slope " + slope);
				// Calculate the arrays
				// KE: I don't think we want to integrate over
				// iv.getLinePeakWidth()
				// here. This will lead to problems. Just take the closest
				// pixel. Leave the iv.getLinePeakWidth() as a GUI feature.
				for (int i = 0; i < len; i++) {
					idx = 2 * i + 1;
					x = x1 + i;
					y = (int) ((float) y1 + (slope * (float) i) + 0.5);
					if (y < 0)
						y = 0;
					if (y > orientedOrigRect.height - 1)
						y = orientedOrigRect.height - 1;
					// Get x in the current coordinates
					p.x = x + orientedRect.x;
					p.y = y + orientedRect.y;
					vals = iv.getCoordinates().getCoordinatesFromOriented(p);
					val = (float) vals[0];
					if (LINEVIEW_HISTOGRAM) {
						if (i == 0) {
							pixels[0] = val - half;
						}
						pixels[idx] = val - half;
						pixels[idx + 1] = val + half;
						if (i == len - 1) {
							pixels[idx + 2] = val + half;
						}
					} else {
						pixels[i] = val;
					}
					// Convert to original coordinates to get intensity
					p.x = x;
					p.y = y;
					p = orientedToImage(p);
					// Note the image is in row, col order and x is the col
					if (!iv.isImageDiffOn()) {
						val = iv.getImageModel().getData(p.y, p.x);
					} else {
						val = iv.getImageDiffModel().getData(p.y, p.x);
					}
					if (LINEVIEW_HISTOGRAM) {
						intensity[idx] = val;
						intensity[idx + 1] = val;
					} else {
						intensity[i] = val;
					}
				}
			} else {
				// Line is more aligned to the vertical axis
				xTitle = "Pixels in " + iv.getCoordinates().getYName();
				yTitle = "Intensity";
				inverted = iv.getCoordinates().isYInverted();
				half = .5f * (float) iv.getCoordinates().getPixelHeight();
				// Order so y increases
				if (y2 < y1) {
					temp = x1;
					x1 = x2;
					x2 = temp;
					temp = y1;
					y1 = y2;
					y2 = temp;
				}
				int len = y2 - y1 + 1;
				if (LINEVIEW_HISTOGRAM) {
					pixels = new float[2 * len + 2];
					intensity = new float[2 * len + 2];
					intensity[0] = intensity[2 * len + 1] = 0;
				} else {
					pixels = new float[len];
					intensity = new float[len];
				}
				if ((x2 - x1) != 0)
					slope = (float) (y2 - y1) / (float) (x2 - x1);
				logger.debug("slope " + slope);
				for (int i = 0; i < len; i++) {
					idx = 2 * i + 1;
					y = y1 + i;
					if ((x2 - x1) != 0) {
						x = (int) ((float) x1 + ((float) i / slope) + 0.5);
					} else {
						x = x1;
					}
					if (x < 0)
						x = 0;
					if (x > orientedOrigRect.width - 1)
						x = orientedOrigRect.width - 1;
					// Get y in the current coordinates
					p.x = x + orientedRect.x;
					p.y = y + orientedRect.y;
					vals = iv.getCoordinates().getCoordinatesFromOriented(p);
					val = (float) vals[1];
					if (LINEVIEW_HISTOGRAM) {
						if (i == 0) {
							pixels[0] = val - half;
						}
						pixels[idx] = val - half;
						pixels[idx + 1] = val + half;
						if (i == len - 1) {
							pixels[idx + 2] = val + half;
						}
					} else {
						pixels[i] = val;
					}
					// Convert to original coordinates to get intensity
					p.x = x;
					p.y = y;
					p = orientedToImage(p);
					// Note the image is in row, col order and x is the col
					if (!iv.isImageDiffOn()) {
						val = iv.getImageModel().getData(p.y, p.x);
					} else {
						val = iv.getImageDiffModel().getData(p.y, p.x);
					}
					if (LINEVIEW_HISTOGRAM) {
						intensity[idx] = val;
						intensity[idx + 1] = val;
					} else {
						intensity[i] = val;
					}
				}
			}
			// Construct the xTitle and set the data
			if (newSelection) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(LineView.ID, null,
								IWorkbenchPage.VIEW_ACTIVATE);
			}
			IViewReference viewReference = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findViewReference(LineView.ID);
			if (viewReference != null) {
				lineView = (LineView) viewReference.getView(true);
				if (lineView != null) {
					// Get the coordinate strings for the axis label
					String from = "unknown", to = "unknown";
					p.x = x1 + orientedRect.x;
					p.y = y1 + orientedRect.y;
					from = iv.getCoordinates().getCoordinateString(p.x, p.y);
					p.x = x2 + orientedRect.x;
					p.y = y2 + orientedRect.y;
					to = iv.getCoordinates().getCoordinateString(p.x, p.y);
					xTitle = xTitle + " from (" + from + ") to (" + to
							+ ") for " + iv.getCoordinatesName();
					lineView.setData(iv.getFileName(), xTitle, inverted,
							pixels, yTitle, intensity);
				}
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Unable to show selected line", ex);
		}
	}

	/**
	 * Integrate the selected area in both dimensions and plot the resulting
	 * profiles with ZoomLineView.
	 */
    private void showProfile() {
		int x1, x2, y1, y2;
		// We want the rectangle to be ordered. Use true for ordered.
		Rectangle lineRect = screenRectangleToOrientedImageRectangle(
				selectedArea, true);
		// These should be in bounds and ordered properly
		x1 = lineRect.x;
		y1 = lineRect.y;
		x2 = lineRect.x + lineRect.width - 1;
		y2 = lineRect.y + lineRect.height - 1;
		if (x1 > x2) {
			int temp = y1;
			x1 = x2;
			x2 = temp;
		}
		if (y1 > y2) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}
		/*
		 * if (x1 < 0) x1 = 0; if (x1 >= orientedRect.width) x1 =
		 * orientedRect.width - 1; if (x2 <= y1) x2 = y1 + 1; if (x2 >=
		 * orientedRect.width) x2 = orientedRect.width - 1; if (y1 < 0) y1 = 0;
		 * if (y1 >= orientedRect.height) y1 = orientedRect.height - 1; if (y2 <
		 * 0) y2 = 0; if (y2 >= orientedRect.height) y2 = orientedRect.height -
		 * 1;
		 */
		try {
			// Integrate the selected area in both dimensions and plot the line
			// profile
			String xTitle = "Pixels in " + iv.getCoordinates().getXName();
			String yTitle = "Pixels in " + iv.getCoordinates().getYName();
			boolean xInverted = iv.getCoordinates().isXInverted();
			boolean yInverted = iv.getCoordinates().isYInverted();
			double[] vals = new double[] { 0, 0 };
			float val, half;
			int idx;
			int lenx = x2 - x1 + 1;
			float pixels_x[] = new float[2 * lenx + 2];
			float intensity_x[] = new float[2 * lenx + 2];
			half = .5f * (float) iv.getCoordinates().getPixelWidth();
			pixels_x[0] = x1 - half;
			pixels_x[2 * lenx + 1] = x2 + half;
			intensity_x[0] = intensity_x[2 * lenx + 1] = 0;
			Point p = new Point(0, 0);
			Point po = new Point(0, 0);
			// x
			for (int i = 0; i < lenx; i++) {
				idx = 2 * i + 1;
				p.x = x1 + i + orientedRect.x;
				p.y = y1 + orientedRect.y;
				vals = iv.getCoordinates().getCoordinatesFromOriented(p);
				val = (float) vals[0];
				if (i == 0) {
					pixels_x[0] = val - half;
				}
				pixels_x[idx] = val - half;
				pixels_x[idx + 1] = val + half;
				if (i == lenx - 1) {
					pixels_x[idx + 2] = val + half;
				}
				intensity_x[idx] = intensity_x[idx + 1] = 0;
				p.x = x1 + i;
				for (int j = y1; j <= y2; j++) {
					p.y = j;
					po = orientedToImage(p);
					if (!iv.isImageDiffOn()) {
						val = iv.getImageModel().getData(po.y, po.x);
					} else {
						val = iv.getImageDiffModel().getData(po.y, po.x);
					}
					intensity_x[idx] += val;
					intensity_x[idx + 1] += val;
				}
			}
			// y
			int leny = y2 - y1 + 1;
			float pixels_y[] = new float[2 * leny + 2];
			float intensity_y[] = new float[2 * leny + 2];
			half = .5f * (float) iv.getCoordinates().getPixelHeight();
			pixels_y[0] = y1 - half;
			pixels_y[2 * leny + 1] = y2 + half;
			intensity_y[0] = intensity_y[2 * leny + 1] = 0;
			for (int i = 0; i < leny; i++) {
				idx = 2 * i + 1;
				p.x = x1 + orientedRect.x;
				p.y = y1 + i + orientedRect.y;
				vals = iv.getCoordinates().getCoordinatesFromOriented(p);
				val = (float) vals[1];
				if (i == 0) {
					pixels_y[0] = val - half;
				}
				pixels_y[idx] = val - half;
				pixels_y[idx + 1] = val + half;
				if (i == leny - 1) {
					pixels_y[idx + 2] = val + half;
				}
				intensity_y[idx] = intensity_y[idx + 1] = 0;
				p.y = y1 + i;
				for (int j = x1; j <= x2; j++) {
					p.x = j;
					po = orientedToImage(p);
					if (!iv.isImageDiffOn()) {
						val = iv.getImageModel().getData(po.y, po.x);
					} else {
						val = iv.getImageDiffModel().getData(po.y, po.x);
					}
					intensity_y[idx] += val;
					intensity_y[idx + 1] += val;
				}
			}
			if (newSelection) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(ProfileView.ID, null,
								IWorkbenchPage.VIEW_ACTIVATE);
			}
			IViewReference viewReference = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findViewReference(ProfileView.ID);
			if (viewReference != null) {
				profileView = (ProfileView) viewReference.getView(true);
				if (profileView != null) {
					profileView.setData(iv.getFileName(), xTitle, yTitle,
							xInverted, yInverted, pixels_x, intensity_x,
							pixels_y, intensity_y);
				}
			}
		} catch (PartInitException ex) {
			FableUtils.excMsg(this, "Unable to show profile", ex);
		}
	}

	/**
	 * Sets a new image rectangle to display along with the new data and name of
	 * the file.
	 * 
	 * @param rect
	 *            The new Rectangle.
	 * @param areaAsFloat
	 *            The new data.
	 * @param fileName
	 *            The new name of the file.
	 * @param _imageModel
	 *            The new ImageModel. If null, then make a new ImageModel.
	 */
	public void changeImageRect(final Rectangle  rect, 
								final float[]    areaAsFloat,
							    final String     fileName, 
							    final ImageModel _imageModel) {

		// Clear the canvas
		if (imageRect.width != rect.width || imageRect.height != rect.height) {
			clearCanvas();
		} else {
			// Set selecting off even so
			selectingOn = false;
			selectOn = false;
		}
		if (_imageModel == null) {
			// This currently comes from the ImageUtils run methods. The new
			// ImageModel could be created there.
			iv.setImageModel(ImageModelFactory.getImageModel(fileName, rect.width, rect.height,
					areaAsFloat));
		} else if (_imageModel != iv.getImageModel()) {
			iv.setImageModel(_imageModel);
		}

		imageRect = rect;
		calculateMainRectangles();
		iv.resetCoordinates();
		// Calculate maximum and minimum, which causes displayImage() 
		iv.setStatistics( imageRect );
//		Statistics statistics = iv.getImageModel().getStatistics(imageRect);
		// This causes displayImage()
//		iv.setStatistics(statistics);
		if (debug) {
			System.out.printf("\nchangeImageRect [at end]: "
					+ "imageRect.width=%d " + "imageRect.height=%d "
					+ "imageRect.x=%d imageRect.y=%d\n", imageRect.width,
					imageRect.height, imageRect.x, imageRect.y);
		}
	}

	/**
	 * Init and update the image display.
	 */
	public void initAndDisplayImage() {
		// Abort if there is no image model or data. Note the first check is not
		// necessary if we use reset for the ImageModel
		imageData = null;
		if (iv.getImageModel() == null || iv.getImageModel().getData() == null) {
			return;
		}
//		long t0 = System.nanoTime(), t1, t2, t3, t4, t5;
		// Create the Byte array and ImageData
		float min, max;
//		final boolean isAutoScale = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSCALE);
//		if (isAutoScale) {
//			min = iv.getMinimum();
//			max = 3*iv.getMean();
//			if (max > iv.getMaximum())	
//				max = iv.getMaximum();
//		} else {
//			min = iv.getUserMinimum();
//			max = iv.getUserMaximum();
//		}
		min = iv.getUserMinimum();
		max = iv.getUserMaximum();
//		t1 = System.nanoTime();
//		System.out.println( "ICI: displayImage.begin.dt [msec]= " + ( t1 - t0 ) / 1000000 );
		float[] imageValues = psf.applyPSF( iv, imageRect );
//		t2 = System.nanoTime();
//		System.out.println( "ICI: displayImage.applyPSF.dt [msec]= " + ( t2 - t1 ) / 1000000 );
//		imageData = createImageData(imageValues, min, max, iv.getPalette());
		imageData = createImageData2( imageValues, min, max, iv.getPalette() );
//		t3 = System.nanoTime();
//		System.out.println( "ICI: displayImage.createImageData.dt [msec]= " + ( t3 - t2 ) / 1000000 );
		displayImage();
	}

	/**
	 * Update the image display.
	 */
	public void displayImage() {
		// Abort if there is no imageData prepared.
		if( imageData == null ) {
			return;
		}
//		long t0 = System.nanoTime(), t4, t5;
		createScreenImage( imageData );
//		t4 = System.nanoTime();
//		System.out.println( "ICI: displayImage.createScreenImage.dt [msec]= " + ( t4 - t0 ) / 1000000 );
		drawImage(true);
		if (legendDraw) {
			float min = iv.getUserMinimum();
			float max = iv.getUserMaximum();
			legendData = createLegendData(min, max, iv.getPalette());
			createScreenLegend(legendData);
			drawLegend(min, max);
		}
		imageChanged = false;
//		t5 = System.nanoTime();
//		System.out.println( "ICI: displayImage.drawImage.dt [msec]= " + ( t5 - t4 ) / 1000000 );
//		System.out.println( "ICI: displayImage.dt [msec]= " + ( t5 - t0 ) / 1000000 );
	}

	public ImageData createImageData2(float[] imageValues,
			final float _minimum, final float _maximum,
            final PaletteData palette) {
		// Check for zero length
		int len = imageValues.length;
		if (len == 0)
			return null;
		// Calculate the oriented rectangles
		// TODO: Are these needed here?
		calculateMainRectangles();
		iv.resetCoordinates();

		// Loop over pixels
		float scale_8bit;
		float maxPixel;
		if (_maximum > _minimum) {
			scale_8bit = 254f / (_maximum - _minimum);
			maxPixel = _maximum - _minimum;
		} else {
			scale_8bit = 1f;
			maxPixel = 254;
		}
		byte[] scaledImageAsByte = new byte[len];
		float scaled_pixel;
		byte pixel;
		Point p1;
		Point p2 = new Point(0, 0);
		int index;
		for (int i = 0; i < len; i++) {
			if (imageValues[i] < 0) {
				scaled_pixel = 0; // Reserved for not measured values (like -1)
			} else {
				if (imageValues[i] < _minimum) {
					scaled_pixel = 0;
				} else if (imageValues[i] >= _maximum) {
					scaled_pixel = maxPixel;
				} else {
					scaled_pixel = imageValues[i] - _minimum;
				}
				scaled_pixel = scaled_pixel * scale_8bit + 1;
			}
			p2.x = i % imageRect.width;
			p2.y = i / imageRect.width;
			// Keep it in bounds
			pixel = (byte) (0x000000FF & ((int) scaled_pixel));
			p1 = imageToOriented(p2);
			index = p1.y * orientedRect.width + p1.x;
			scaledImageAsByte[index] = pixel;
		}
		ImageData imageData = new ImageData(orientedRect.width,
				orientedRect.height, 8, palette, 1, scaledImageAsByte);
		return imageData;
	}

	/**
	 * Creates an ImageData with pixels scaled between minimum and maximum,
	 * oriented according to the current orientation, and using the specified
	 * palette. Also calculates Rectangles for the oriented image and the
	 * original oriented image.
	 * 
	 * @param minimum
	 *            Maximum data value.
	 * @param maximum
	 *            Minimum data value.
	 * @param palette
	 *            PaletteData to use for ImageData.
	 * @return Scaled and oriented ImageData.
	 */
	public ImageData createImageData(final float _minimum, 
			                         final float _maximum,
			                         final PaletteData palette) {
		int kernel1D[] = {
				7, 10, 15, 18, 20, 18, 15, 10,  7,
				10, 16, 25, 35, 40, 35, 25, 16, 10,
				15, 25, 45, 70, 80, 70, 45, 25, 15,
				18, 35, 70, 85, 95, 85, 70, 35, 18,
				20, 40, 80, 95, 99, 95, 80, 40, 20,
				18, 35, 70, 85, 95, 85, 70, 35, 18,
				15, 25, 45, 70, 80, 70, 45, 25, 15,
				10, 16, 25, 35, 40, 35, 25, 16, 10,
				7, 10, 15, 18, 20, 18, 15, 10,  7 };
		int kernel2D[][] = {
				{ 7, 10, 15, 18, 20, 18, 15, 10,  7 },
				{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
				{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
				{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
				{ 20, 40, 80, 95, 99, 95, 80, 40, 20 },
				{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
				{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
				{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
				{ 7, 10, 15, 18, 20, 18, 15, 10,  7 } };
		int kernelJCenter;
		int kernelKCenter;
		int iXY;
		float psfValue, valueJK;
		long t0 = System.nanoTime();
		// Check for zero length
		float[] screenImageData;
		if (!iv.isImageDiffOn()) {
			screenImageData = iv.getImageModel().getData(imageRect);
		} else {
			screenImageData = iv.getImageDiffModel().getData(imageRect);
		}

		int len = screenImageData.length;
		if (len == 0)
			return null;
		// Calculate the oriented rectangles
		// TODO: Are these needed here?
		calculateMainRectangles();
		iv.resetCoordinates();

		// Loop over pixels
		float scale_8bit;
		float maxPixel;
		if (_maximum > _minimum) {
			scale_8bit = 254f / (_maximum - _minimum);
			maxPixel = _maximum - _minimum;
		} else {
			scale_8bit = 1f;
			maxPixel = 254;
		}
		byte[] scaledImageAsByte = new byte[len];
		float scaled_pixel;
		byte pixel;
		Point p1;
		Point p2 = new Point(0, 0);
		int index;
		boolean psfOn = false; //iv.isPSFOn();
//		int[] intImageData = new int[screenImageData.length];
//		for( int d = screenImageData.length - 1; d>=0; d-- )
//			intImageData[d] = (int)screenImageData[d];
		long t1 = System.nanoTime();
		logger.debug( "createimagedata.begin.dt [msec]= " + ( t1 - t0 ) / 1000000 );
//		int psfRunmax=2;
//		float[] tmpScreenImageData=null;
//		float[] newScreenImageData=screenImageData.clone();
//		for (int psfRun=0;psfRun<psfRunmax;psfRun++) {
//			tmpScreenImageData=screenImageData;
//			screenImageData=newScreenImageData;
//			newScreenImageData=tmpScreenImageData;
			for (int i = 0; i < len; i++) {
				if( screenImageData[i] < 0 ) {
					scaled_pixel = 0; //Reserved for not measured values (like -1)
				} else {
					if (screenImageData[i] < _minimum) {
						scaled_pixel = 0;
					} else if (screenImageData[i] >= _maximum) {
						scaled_pixel = maxPixel;
					} else {
						scaled_pixel = screenImageData[i] - _minimum;
					}
					scaled_pixel = scaled_pixel * scale_8bit + 1;
				}
				p2.x = i % imageRect.width;
				p2.y = i / imageRect.width;
				if( psfOn ) {
//					int kernel[][] = {
//							{  7, 10, 15, 18, 20, 18, 15, 10,  7 },
//							{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//							{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//							{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//							{ 20, 40, 80, 95, 99, 95, 80, 40, 20 },
//							{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//							{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//							{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//							{  7, 10, 15, 18, 20, 18, 15, 10,  7 } };
//					int kernelJCenter = kernel.length / 2;
//					int jMin = -kernelJCenter; //max(-p2.y, -kernelJCenter)
//					if( jMin < -p2.y )
//						jMin = -p2.y;
//					int jMax = kernelJCenter + 1; //min(imageRect.height-p2.y, kernelJCenter + 1)
//					if( jMax > imageRect.height - p2.y )
//						jMax = imageRect.height - p2.y;
//					int iY = i + jMin * imageRect.width;
//					int kernelKCenter = kernel[kernelJCenter].length / 2;
//					int kMin = -kernelKCenter; //max(-p2.x, -kernelKCenter)
//					if( kMin < -p2.x )
//						kMin = -p2.x;
//					int kMax = kernelKCenter + 1; //min(imageRect.width-p2.x, kernelKCenter + 1)
//					if( kMax > imageRect.width - p2.x )
//						kMax = imageRect.width - p2.x;
//					float psfValue = 0;
//					float weight = 0;
//					jMin += kernelJCenter;
//					jMax += kernelJCenter;
//					kMin += kernelKCenter;
//					kMax += kernelKCenter;
//					for( int j = jMin; j < jMax; iY += imageRect.width, j++ ) {
//						int iX = iY + kMin - kernelKCenter;
//						for( int k = kMin; k < kMax; k++ ) {
//							float value = screenImageData[ iX++ ];
//							//weight += kernel[j + kernelJCenter][k + kernelKCenter];
//							//						psfValue += kernel[j + kernelJCenter][k + kernelKCenter] * ( ( value < _minimum ? 0 : ( value > _maximum ? maxPixel : value - _minimum ) ) * scale_8bit + 1 );
//							//						psfValue += kernel[j + kernelJCenter][k + kernelKCenter] * value;
//							float valueJK = kernel[j][k] * value;
//							if( valueJK > psfValue )
//								psfValue = valueJK;
//						}
//					}
//					//				scaled_pixel = psfValue / weight;
//					//				psfValue /= weight;
//					psfValue /= 99; //middle kernel
//					scaled_pixel = ( psfValue < _minimum ? 0 : ( psfValue > _maximum ? maxPixel : psfValue - _minimum ) ) * scale_8bit + 1;
					kernelJCenter = 4;
					kernelKCenter = 4;
					psfValue = 0;
					if( p2.x >= kernelKCenter && imageRect.width - p2.x > kernelKCenter
						&& p2.y >= kernelJCenter && imageRect.height - p2.y > kernelJCenter ) {
						int jMax = 2*kernelJCenter + 1;
						iXY = i - kernelJCenter * imageRect.width - kernelKCenter;
						int kMax = 2*kernelKCenter + 1;
						int l = 0;
						int dXY = imageRect.width - kMax;
//						if( use1d ) {
//						} else {
							for( int j = 0; j < jMax; iXY += dXY, j++ ) {
//								for( int k = 0; k < kMax; k++ ) {
									valueJK = kernel2D[j][0] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][1] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][2] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][3] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][4] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][5] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][6] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][7] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;
									valueJK = kernel2D[j][8] * screenImageData[ iXY++ ];
									if( valueJK > psfValue )
										psfValue = valueJK;

//								}
							}
//						}
						//				scaled_pixel = psfValue / weight;
						//				psfValue /= weight;
						psfValue /= 99; //middle kernel
						scaled_pixel = ( psfValue < _minimum ? 0 : ( psfValue > _maximum ? maxPixel : psfValue - _minimum ) ) * scale_8bit + 1;
					}
				}
//				if( psfOn ) {				
//					int kernel[][] = {
//							{  7, 10, 15, 18, 20, 18, 15, 10,  7 },
//							{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//							{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//							{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//							{ 20, 40, 80, 95, 99, 95, 80, 40, 20 },
//							{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//							{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//							{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//							{  7, 10, 15, 18, 20, 18, 15, 10,  7 } };
//
//					int kernelJCenter = kernel.length / 2;
//					int jMin = -kernelJCenter; //max(-p2.y, -kernelJCenter)
//					int jMax = jMin;
//					int kernelKCenter = kernel[kernelJCenter].length / 2;
//					int kMin = -kernelKCenter; //max(-p2.x, -kernelKCenter)
//					int kMax = kMin;
//					int iY=0;
//					if (psfRun==0) {
//						if( jMin < -p2.y )
//							jMin = -p2.y;
//						jMax = kernelJCenter + 1; //min(imageRect.height-p2.y, kernelJCenter + 1)
//						if( jMax > imageRect.height - p2.y )
//							jMax = imageRect.height - p2.y;
//						iY = i + jMin * imageRect.width;
//						kMin = 0;
//						kMax = 1;
//					} else {
//						jMin = 0; //min(imageRect.height-p2.y, kernelJCenter + 1)
//						jMax = 1; //min(imageRect.height-p2.y, kernelJCenter + 1)
//						iY = i + jMin * imageRect.width;
//						if( kMin < -p2.x )
//							kMin = -p2.x;
//						kMax = kernelJCenter + 1; //min(imageRect.width-p2.x, kernelKCenter + 1)
//						if( kMax > imageRect.width - p2.x )
//							kMax = imageRect.width - p2.x;
//					}
//					float psfValue = 0;
//					float weight = 0;
//					int orig_iY=iY;
//					for( int j = jMin; j < jMax; iY += imageRect.width, j++ ) {
//						int iX = iY + kMin;
//						for( int k = kMin; k < kMax; k++ ) {						
//							float value = screenImageData[ iX++ ];
//							//weight += kernel[j + kernelJCenter][k + kernelKCenter];
//							//						psfValue += kernel[j + kernelJCenter][k + kernelKCenter] * ( ( value < _minimum ? 0 : ( value > _maximum ? maxPixel : value - _minimum ) ) * scale_8bit + 1 );
//							//						psfValue += kernel[j + kernelJCenter][k + kernelKCenter] * value;
//							float valueJK = kernel[j + kernelJCenter][k + kernelKCenter] * value;
//							if( valueJK > psfValue )
//								psfValue = valueJK;
//						}
//					}
//					//				scaled_pixel = psfValue / weight;
//					//				psfValue /= weight;
//					psfValue /= 99; //middle kernel
//					newScreenImageData[ orig_iY ]=psfValue;
//					scaled_pixel = ( psfValue < _minimum ? 0 : ( psfValue > _maximum ? maxPixel : psfValue - _minimum ) ) * scale_8bit + 1;
//				}
				// Keep it in bounds
				pixel = (byte) (0x000000FF & ((int) scaled_pixel));
				p1 = imageToOriented(p2);
				index = p1.y * orientedRect.width + p1.x;
				// if (false) {
				// // If this is needed, we have a problem
				// if (index >= len)
				// index = len - 1;
				// if (index < 0)
				// index = 0;
				// }
				scaledImageAsByte[index] = pixel;
			}
//		}
		logger.debug("use1d=" + use1d);
		use1d = !use1d;
		
		long t2 = System.nanoTime();
		logger.debug( "floatarray2bytearray.dt [msec]= " + ( t2 - t1 ) / 1000000 );
		
		ImageData imageData = new ImageData(orientedRect.width,
				orientedRect.height, 8, palette, 1, scaledImageAsByte);

		long t3 = System.nanoTime();
		logger.debug( "bytearray2imagedata.dt [msec]= " + ( t3 - t2 ) / 1000000 );
		return imageData;
	}

	/**
	 * Converts an ImageData into an Image with the appropriate scaling and sets
	 * the global scaling parameters.
	 * 
	 * @param data
	 *            The ImageData.
	 * @return The Image.
	 */
	public Image createScreenImage(ImageData data1) {
		ImageDataUpscale data = new ImageDataUpscale( data1 );
		if (image != null && !image.isDisposed()) {
			image.dispose();
			image = null;
		}
		// clearCanvas();
		Rectangle bounds = imageCanvas.getBounds();
	//	if (legendDraw)
			// use only 90% of width to leave space for plot of scale
		//	bounds.width = (int)(bounds.width-45); 
		
		// determine how much each dimension needs to be scaled by
		xScale = (double) orientedRect.width / (double) bounds.width; //problem
		
		yScale = (double) orientedRect.height / (double) bounds.height;
		// choose the largest to scale both dimensions by if preserve the image
		// aspect ratio is selected
		final boolean isKeepAspect = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_KEEPASPECT);
		if (isKeepAspect) {
			if (xScale > yScale) {
				yScale = xScale;
			} else {
				xScale = yScale;
			}
		}
		// check that the scaled image is not less than 1 pixel in one direction
		// e.g. for images 2048x2 pixels
		// in this case scale the axis in question back up to the full width or
		// height
		if (orientedRect.width / xScale < 1) {
			xScale = (double) orientedRect.width / (double) bounds.width;
		}
		if (orientedRect.height / yScale < 1) {
			yScale = (double) orientedRect.height / (double) bounds.height;
		}
		int xscaledTo = (int) ((double) orientedRect.width / xScale);
		int yscaledTo = (int) ((double) orientedRect.height / yScale);
		if (data == null) {
			logger.debug("data is null !");
			image = null;
		} else {
			image = new Image(Display.getCurrent(), data.scaledTo(xscaledTo,
					yscaledTo).getImageData());
		}
		return image;
	}

	/**
	 * Draws the image on the canvas and adds the peaks. Calls showSelection if
	 * selectOn is true. Note that showSelection does more than just draw the
	 * selection rectangle. It updates other views. To avoid this while
	 * selecting, draw the selection rectangle independently and call drawImage
	 * with doSelection = false.
	 */
	/**
	 * @param doSelection
	 *            Whether to do the selection or not.
	 */
	private void drawImage(boolean doSelection) {
		if (imageCanvasGC == null || image == null) return;
		imageCanvasGC.drawImage(image, 0, 0);
		
		if (iv.isPeaksOn()) {
			showPeaks();
		}
		if (doSelection && selectOn) {
			if (debug1) {
				System.out.println("\ndrawImage calling showSelection "
						+ "imageChanged=" + imageChanged);
				System.out.printf("  \"%s\"\n", iv.getPartName());
			}
			showSelection(false);
		}
	}

	/**
	 * Creates an ImageData legend between minimum and maximum,
	 * 
	 * @param minimum
	 *            Maximum data value.
	 * @param maximum
	 *            Minimum data value.
	 * @param palette
	 *            PaletteData to use for ImageData.
	 * @return Scale ImageData.
	 */
	public ImageData createLegendData(final float _minimum, 
			                         final float _maximum,
			                         final PaletteData palette) {
 
	
		byte[] legendAsByte = new byte[100];
		float scaled_pixel;
		byte pixel;
		float scaled=255;
		for (int i = 0; i < 100; i++) {
			
			scaled_pixel=scaled;
			scaled=scaled-2.56f;
				
			// Keep it in bounds
			pixel = (byte) (0x000000FF & ((int) scaled_pixel));
			
			
			legendAsByte[i] = pixel;
		}

		
		

		ImageData imageData = new ImageData(1, 100, 8, palette, 1, legendAsByte);
		return imageData;
	}	
	
	/**
	 * Converts an ImageData of the legend into an Image with the appropriate scaling.
	 * 
	 * @param data
	 *            The ImageData.
	 * @return The Image.
	 */
	public Image createScreenLegend(ImageData data) {
		if (legend != null && !legend.isDisposed()) {
			legend.dispose();
			legend = null;
		}
		// clearCanvas();
		Rectangle bounds = imageCanvas.getBounds();
		// use only 9% of width to leave space between image and legend
//		bounds.width = (int)(bounds.width*.09); 
//		bounds.height = (int)(bounds.height*.99); 
		bounds.width = ImageComponentUI.getCanvaslegendsize(); 
		bounds.height = bounds.height-2; 

		// determine how much each dimension needs to be scaled by
	/*	xScale = (double) 1 / (double) bounds.width;
		yScale = (double) 100 / (double) bounds.height;
		int xscaledTo = (int) ((double) 1 / xScale);
		int yscaledTo = (int) ((double) 100 / yScale);*/
		int xscaledTo=bounds.width;
		int yscaledTo=bounds.height;

		if (data == null) {
			logger.debug("data is null !");
			legend = null;
		} else {
			legend = new Image(Display.getCurrent(), data.scaledTo(xscaledTo,
					yscaledTo));
	
		}
		return legend;
	}

	/**
	 * Draws the legend on the canvas.
	 */
	private void drawLegend(float min, float max) {
		if (imageCanvasGC == null || legend == null) return;
		Rectangle bounds = imageCanvas.getBounds();
		// draw the legend offset 10% of the width and height of the legend area to leave some space
		// between the legend and the image
		// int legend_width = (int)((float)bounds.width*.905f);
		//int legend_width = bounds.width-43;
		int legend_height = 1;		

		legendCanvasGC.drawImage(legend, 0, 0);		
		Font font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
		legendCanvasGC.setFont(font);
		legendCanvasGC.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		//legendCanvasGC.drawText(Integer.toString((int)max), 0, 0);
		//legendCanvasGC.drawText(Integer.toString((int)min), 0, bounds.height-legend_height-20);

	
		String maxConvertInt ;
		maxConvertInt=Integer.toString((int)max);
		String maxformated = null;
		NumberFormat formatter = new DecimalFormat();		
		formatter=new DecimalFormat("0.##E0");
		int maxLenghInt = maxConvertInt.length();
		int maxLenghString;
		maxLenghString=Float.toString((float)max).length();
		maxformated=formatter.format(max);
	
		
		//legendCanvasGC.drawText("test", 0, (bounds.height/2)-14);
	
		if (maxLenghInt<2 && max !=0){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Float.toString((float)max), 0, 0);
			System.out.println("iiiiii");
		}
			
		 if (maxLenghInt>=2 && maxLenghInt<6|| max==0){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Integer.toString((int)max), 0, 0);	
			System.out.println("ooooo");
		}
		else if (6<=maxLenghInt){				
			font = new Font(display,"Arial",11,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(maxformated, 0, 0);
		}	
		
		String minConvertInt ;
		minConvertInt=Integer.toString((int)min);
		String minformated = null;
		int minLenghInt = minConvertInt.length();
		minformated=formatter.format(min);
		float middle= (max-min)/2;
		
		String middleConvertInt;
		float MiddleValue=(max-min)/2;
		middleConvertInt=Integer.toString((int)MiddleValue);
		int middleLenghtInt=middleConvertInt.length();
		String middleformated=null;
		middleformated=formatter.format(MiddleValue);
		
		
		
		if (minLenghInt<2  && min !=0){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Float.toString((float)min), 0, bounds.height-legend_height-20);	
		}
		
		if ((minLenghInt>=2 &&  minLenghInt<6)|| min==0){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Integer.toString((int)min), 0, bounds.height-legend_height-20);	
			
		}
		else if (6<=minLenghInt){		
		
			font = new Font(display,"Arial",11,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(minformated, 0, bounds.height-legend_height-20);
		
		}
		
		/*******************************/
		
		if (middleLenghtInt<2  && middleLenghtInt !=0 && middle !=0.0 ){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Float.toString((float)middle), 0, (bounds.height/2)-14);
		}
		
		if (middleLenghtInt>=2 && middleLenghtInt<6 || middleLenghtInt==0 || middle ==0.0){
			font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(Integer.toString((int)middle), 0, (bounds.height/2)-14);
			
		}
		else if (6<=middleLenghtInt){		
		
			font = new Font(display,"Arial",11,SWT.BOLD | SWT.ITALIC); 
			legendCanvasGC.setFont(font);
			legendCanvasGC.drawText(middleformated, 0, (bounds.height/2)-14);
		
		}
		
		
		
		/*******************************/
		
		
		
		font.dispose();
	}
	
	
	
	
	
	/**
	 * Creates a drop target on the imageCanvas. TextTransfers and FileTransfers
	 * are allowed. Only the first fileName in an array is used.
	 */
	private void createDropTarget() {
		DropTarget dropTarget = new DropTarget(imageCanvas, DND.DROP_COPY
				| DND.DROP_DEFAULT);
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance() };
		dropTarget.setTransfer(types);
		dropTarget.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			public void dragLeave(DropTargetEvent event) {
			}

			public void dragOperationChanged(DropTargetEvent event) {
			}

			public void dragOver(DropTargetEvent event) {
			}

			public void drop(DropTargetEvent event) {
				// DEBUG
				// if (true) {
				// System.out.println("\nImageViewImage.DropTarget: "
				// + iv.getSecondaryId());
				// System.out.println("  event=" + event);
				// System.out.println("  source=" + event.getSource());
				// System.out.println("  currentDataType="
				// + event.currentDataType.type);
				// System.out.println("  currentDataType="
				// + event.currentDataType.toString());
				// }
				String fileName = null;
				if (TextTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
					fileName = (String) event.data;
				} else if (FileTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
					String[] fileNames = (String[]) event.data;
					fileName = fileNames[0];
				}
				if (fileName == null) {
					return;
				}
				// DEBUG
				// if (true) {
				// System.out.println("  fileName=" + fileName);
				// }
				try {
					iv.setEditorInput(fileName);
				} catch (Throwable e) {
					FableLogger.error("Cannot load image "+fileName, e);
				}
			}

			public void dropAccept(DropTargetEvent event) {
			}
		});
	}

	/**
	 * Returns the original image coordinates for the specified unscaled,
	 * oriented image coordinates.
	 * 
	 * @param point
	 *            The coordinates of the oriented image.
	 * @return The coordinates of the original image.
	 */
	public Point orientedToImage(Point point) {
		// Convert to oriented original coordinates
		int x3 = point.x + orientedRect.x;
		int y3 = point.y + orientedRect.y;
		int x2 = x3, y2 = y3;
		// Convert to original coordinates
		switch (iv.getOrientation()) {
		default:
		case O_MOOM:
			break;
		case O_MOOP:
			x2 = orientedOrigRect.width - x3 - 1;
			break;
		case O_POOM:
			y2 = orientedOrigRect.height - y3 - 1;
			break;
		case O_POOP:
			x2 = orientedOrigRect.width - x3 - 1;
			y2 = orientedOrigRect.height - y3 - 1;
			break;
		case O_OMMO:
			x2 = y3;
			y2 = x3;
			break;
		case O_OMPO:
			x2 = orientedOrigRect.height - y3 - 1;
			y2 = x3;
			break;
		case O_OPMO:
			x2 = y3;
			y2 = orientedOrigRect.width - x3 - 1;
			break;
		case O_OPPO:
			x2 = orientedOrigRect.height - y3 - 1;
			y2 = orientedOrigRect.width - x3 - 1;
			break;
		}
		Point converted = new Point(x2, y2);
		return converted;
	}

	/**
	 * Returns the unscaled, oriented image coordinates for the specified
	 * original image coordinates given as a Point.
	 * 
	 * @param point
	 *            The coordinates of the original image (x, y}.
	 * @return The coordinates of the oriented image {x, y}.
	 */
	public Point imageToOriented(Point point) {
		// Original image coordinates
		int x2 = point.x;
		int y2 = point.y;
		// Convert to original oriented coordinates
		int x3 = x2, y3 = y2;
		switch (iv.getOrientation()) {
		default:
		case O_MOOM:
			break;
		case O_MOOP:
			x3 = imageRect.width - x2 - 1;
			break;
		case O_POOM:
			y3 = imageRect.height - y2 - 1;
			break;
		case O_POOP:
			x3 = imageRect.width - x2 - 1;
			y3 = imageRect.height - y2 - 1;
			break;
		case O_OMMO:
			x3 = y2;
			y3 = x2;
			break;
		case O_OPMO:
			x3 = imageRect.height - y2 - 1;
			y3 = x2;
			break;
		case O_OMPO:
			x3 = y2;
			y3 = imageRect.width - x2 - 1;
			break;
		case O_OPPO:
			x3 = imageRect.height - y2 - 1;
			y3 = imageRect.width - x2 - 1;
			break;
		}
		return new Point(x3, y3);
	}

	/**
	 * Returns the unscaled, oriented image coordinates for the specified
	 * original image coordinates given as a float[]. The input and output are
	 * float arrays to allow fractional values.
	 * 
	 * @param point
	 *            The coordinates of the original image {x, y}.
	 * @return The coordinates of the oriented image {x, y}.
	 */
	public float[] imageToOriented(float[] point) {
		// Original image coordinates
		float x2 = point[0];
		float y2 = point[1];
		// Convert to original oriented coordinates
		float x3 = x2, y3 = y2;
		switch (iv.getOrientation()) {
		default:
		case O_MOOM:
			break;
		case O_MOOP:
			x3 = imageRect.width - x2 - 1;
			break;
		case O_POOM:
			y3 = imageRect.height - y2 - 1;
			break;
		case O_POOP:
			x3 = imageRect.width - x2 - 1;
			y3 = imageRect.height - y2 - 1;
			break;
		case O_OMMO:
			x3 = y2;
			y3 = x2;
			break;
		case O_OPMO:
			x3 = imageRect.height - y2 - 1;
			y3 = x2;
			break;
		case O_OMPO:
			x3 = y2;
			y3 = imageRect.width - x2 - 1;
			break;
		case O_OPPO:
			x3 = imageRect.height - y2 - 1;
			y3 = imageRect.width - x2 - 1;
			break;
		}
		return new float[] { x3, y3 };
	}

	/**
	 * Converts a Rectangle in screen coordinates to a Rectangle in the original
	 * image coordinates and insures it is in bounds and has non-negative width
	 * and height.
	 * 
	 * @param screenRect
	 * @param ordered
	 *            Whether the returned Rectangle is ordered so x + width > x and
	 *            y + height > y.
	 * @return
	 */
	public Rectangle screenRectangleToImageRectangle(Rectangle screenRect,
			boolean ordered) {
		int x1, x2, y1, y2, temp;
		// Convert to non-scaled, oriented coordinates
		// KE: Don't use roundoff here, it doesn't work right.
		// KE: imageData.x and imageData.y are always 0
		int x0 = screenRect.x - imageData.x;
		int y0 = screenRect.y - imageData.y;
		x1 = (int) (x0 * xScale);
		y1 = (int) (y0 * yScale);
		// Calculate x2, y2
		if (screenRect.width != 0) {
			x2 = (int) ((x0 + screenRect.width - 1) * xScale);
		} else {
			x2 = x1;
		}
		if (screenRect.height != 0) {
			y2 = (int) ((y0 + screenRect.height - 1) * yScale);
		} else {
			y2 = y1;
		}
		// Convert to the original image coordinates
		Point p1 = orientedToImage(new Point(x1, y1));
		Point p2 = orientedToImage(new Point(x2, y2));
		x1 = p1.x;
		y1 = p1.y;
		x2 = p2.x;
		y2 = p2.y;
		// Insure it is ordered correctly
		if (ordered) {
			if (x1 > x2) {
				temp = x1;
				x1 = x2;
				x2 = temp;
			}
			if (y1 > y2) {
				temp = y1;
				y1 = y2;
				y2 = temp;
			}
		}
		// Insure it is in bounds
		if (x1 < 0)
			x1 = 0;
		if (x1 >= origRect.width)
			x1 = origRect.width - 1;
		if (x2 < 0)
			x2 = 0;
		if (x2 >= origRect.width)
			x2 = origRect.width - 1;
		if (y1 < 0)
			y1 = 0;
		if (y1 >= origRect.height)
			y1 = origRect.height - 1;
		if (y2 < 0)
			y2 = 0;
		if (y2 >= origRect.height)
			y2 = origRect.height - 1;

		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		Rectangle newRect = new Rectangle(x1, y1, width, height);
		return newRect;
	}

	/**
	 * Converts a Rectangle in screen coordinates to a Rectangle in the oriented
	 * image coordinates and insures it is in bounds and has non-negative width
	 * and height.
	 * 
	 * @param screenRect
	 * @param ordered
	 *            Whether the returned Rectangle is ordered so x + width > x and
	 *            y + height > y.
	 * @return
	 */
	public Rectangle screenRectangleToOrientedImageRectangle(
			Rectangle screenRect, boolean ordered) {
		int x1, x2, y1, y2, temp;
		// Convert to non-scaled, oriented coordinates
		// KE: Don't use roundoff here, it doesn't work right.
		// KE: imageData.x and imageData.y are always 0
		int x0 = screenRect.x - imageData.x;
		int y0 = screenRect.y - imageData.y;
		x1 = (int) (x0 * xScale);
		y1 = (int) (y0 * yScale);
		// Calculate x2, y2
		if (screenRect.width != 0) {
			x2 = (int) ((x0 + screenRect.width - 1) * xScale);
		} else {
			x2 = x1;
		}
		if (screenRect.height != 0) {
			y2 = (int) ((y0 + screenRect.height - 1) * yScale);
		} else {
			y2 = y1;
		}
		// Insure it is ordered correctly
		if (ordered) {
			if (x1 > x2) {
				temp = x1;
				x1 = x2;
				x2 = temp;
			}
			if (y1 > y2) {
				temp = y1;
				y1 = y2;
				y2 = temp;
			}
		}
		// Insure it is in bounds
		if (x1 < 0)
			x1 = 0;
		if (x1 >= orientedRect.width)
			x1 = orientedRect.width - 1;
		if (x2 < 0)
			x2 = 0;
		if (x2 >= orientedRect.width)
			x2 = orientedRect.width - 1;
		if (y1 < 0)
			y1 = 0;
		if (y1 >= orientedRect.height)
			y1 = orientedRect.height - 1;
		if (y2 < 0)
			y2 = 0;
		if (y2 >= orientedRect.height)
			y2 = orientedRect.height - 1;

		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		Rectangle newRect = new Rectangle(x1, y1, width, height);
		return newRect;
	}

	/**
	 * Returns the current oriented and scaled screen coordinates for a point
	 * (y, z) in the TotalCrys coordinate system. It is assumed the orientation
	 * represents the correct view of the detector, so that the origin of the
	 * TotalCrys coordinates is at the lower right of the image.
	 * 
	 * @param tcY
	 *            The value of y in the TotalCrys system.
	 * @param tcZ
	 *            The value of z in the TotalCrys system.
	 * @return Point representing the location in the current screen image.
	 */
	public Point tcToScreen(float tcY, float tcZ) {
		// Switch from TotalCryst coordinates to image coordinates.
		float x = tcY - imageRect.x;
		float y = tcZ - imageRect.y;
		// Switch to oriented coordinates
		float[] oriented = imageToOriented(new float[] { x, y });
		// Scale
		Point point = new Point((int) (oriented[0] / xScale),
				(int) (oriented[1] / yScale));
		return point;
	}

	/**
	 * Clear the imageCanvas.
	 */
	public void clearCanvas() {
		if (imageCanvas != null && imageCanvasGC != null) {
			Rectangle bounds = imageCanvas.getBounds();
			imageCanvasGC.fillRectangle(0, 0, bounds.width, bounds.height);
		}
		// TODO
		// Set it to not defining a selection rectangle and not showing the
		// selection rectangle
		// Commented out so that the current selection is kept when a new image
		// is displayed - andy 12mar09
		// selectingOn = false;
		// selectOn = false;
	}

	public Rectangle getSelectedArea() {
		return selectedArea;
	}

	public void selectZoom(ZoomSelection zoomselect) {
		logger.debug("set zoom to " + zoomselect.getName());
		iv.setZoomSelection(zoomselect);
		selectingOn = false;
		selectOn = false;
	}
	
	
	private boolean inselectbox(MouseEvent event,Rectangle RectangleSelection){
		 if( RectangleSelection == null )
			 return false;

		 if(		
				RectangleSelection.x<(RectangleSelection.width+RectangleSelection.x)
				&&	RectangleSelection.y<(RectangleSelection.height+RectangleSelection.y)
				&&	event.x>RectangleSelection.x
				&& 	event.y>RectangleSelection.y
				&& 	event.x<(RectangleSelection.x+RectangleSelection.width)
				&& 	event.y<(RectangleSelection.y+RectangleSelection.height)){
			 return true;
		
		}
		
		else if(
				RectangleSelection.x>(RectangleSelection.width+RectangleSelection.x)	
				&&	RectangleSelection.y<(RectangleSelection.height+RectangleSelection.y)
				&&	event.x<RectangleSelection.x
				&& event.y>RectangleSelection.y
				&& event.x>(RectangleSelection.x+RectangleSelection.width)
				&& event.y<(RectangleSelection.y+RectangleSelection.height)){
			return true;
				
	
		}
		
		
		else if(
				RectangleSelection.x<(RectangleSelection.width+RectangleSelection.x)
				&&	RectangleSelection.y>(RectangleSelection.height+RectangleSelection.y)
				&&	event.x>RectangleSelection.x
				&& 	event.y<RectangleSelection.y
				&& 	event.x<(RectangleSelection.x+RectangleSelection.width)
				&& 	event.y>(RectangleSelection.y+RectangleSelection.height)){
			return true;
		}
		
		
		else if(
				RectangleSelection.x>(RectangleSelection.width+RectangleSelection.x)	
				&&	RectangleSelection.y>(RectangleSelection.height+RectangleSelection.y)
				&&	event.x<RectangleSelection.x
				&& event.y<RectangleSelection.y
				&& event.x>(RectangleSelection.x+RectangleSelection.width)
				&& event.y>(RectangleSelection.y+RectangleSelection.height)){
			return true;
		}
		else return false;
	}

	// Getters and setters

	/**
	 * @return the imageChanged
	 */
	public boolean isImageChanged() {
		return imageChanged;
	}

	/**
	 * @param imageChanged
	 *            the imageChanged to set
	 */
	public void setImageChanged(boolean imageChanged) {
		this.imageChanged = imageChanged;
	}

	/**
	 * @return the imageRect
	 */
	public Rectangle getImageRect() {
		return imageRect;
	}

	/**
	 * @param imageRect
	 *            the imageRect to set
	 */
	public void setImageRect(Rectangle imageRect) {
		this.imageRect = imageRect;
	}

	/**
	 * @return the origRect
	 */
	public Rectangle getOrigRect() {
		return origRect;
	}

	/**
	 * @return the orientedOrigRect
	 */
	public Rectangle getOrientedOrigRect() {
		return orientedOrigRect;
	}

	/**
	 * @return the orientedRect
	 */
	public Rectangle getOrientedRect() {
		return orientedRect;
	}

	/**
	 * @return the selectOn
	 */
	public Boolean getSelectOn() {
		return selectOn;
	}

	/**
	 * @param selectOn
	 *            the selectOn to set
	 */
	public void setSelectOn(Boolean selectOn) {
		this.selectOn = selectOn;
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}
	
	public Image getImageLegend() {
		return legend;
	}

	public boolean isDisposed() {
		if (imageCanvasGC==null||imageCanvasGC.isDisposed()) return true;
		if (image==null) return false;
		return image.isDisposed();
	}

	public void setLegendOn(Boolean drawLegendOn) {
		legendDraw = drawLegendOn;
		displayImage();
	}

	public GC getSelectedRectangle() {
		return selectedRectangle;
	}

	public void setSelectedRectangle(GC selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}

	protected void setSelection(Rectangle rect) {
		if( imageData != null )
			iv.getParentPart().getSite().getSelectionProvider().setSelection(
				new FableSelection( screenRectangleToImageRectangle( rect, true ), (EditorPart)iv.getParentPart() ) );
	}
}
