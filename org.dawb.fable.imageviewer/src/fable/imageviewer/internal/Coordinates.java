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

import java.text.NumberFormat;

import org.eclipse.swt.graphics.Point;

/**
 * Class to manage the coordinates used to display the mouse position. These
 * coordinates apply to the oriented image, not the raw image. They use the
 * form:<br>
 * <br>
 * x1 = pixelWidth * (x - x0)<br>
 * y1 = pixelHeight * (y - y0)<br>
 * <br>
 * where x and y are the image coordinates with origin at top left.
 * 
 * @author evans
 * 
 */
public class Coordinates implements IImagesVarKeys {
	private double x0;
	private double y0;
	private double pixelWidth;
	private double pixelHeight;
	private String xName;
	private String yName;
	private int type;

	/**
	 * Constructor that uses the default values.
	 */
	public Coordinates() {
		resetToDefault();
	}

	/**
	 * Constructor that sets the parameters based on the width and height of the
	 * oriented image. Used with all but custom.
	 * 
	 * @param type
	 * @param width
	 * @param height
	 */
	public Coordinates(int type, double width, double height) {
		reset(type, width, height);
	}

	/**
	 * Constructor that sets all the parameters. Used for custom.
	 * 
	 * @param type
	 * @param x0
	 * @param y0
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param xName
	 * @param yName
	 */
	public Coordinates(int type, double x0, double y0, double pixelWidth,
			double pixelHeight, String xName, String yName) {
		reset(type, x0, y0, pixelWidth, pixelHeight, xName, yName);
	}

	/**
	 * Set the values to the defaults.
	 */
	public void resetToDefault() {
		x0 = 0;
		y0 = 0;
		pixelWidth = 1;
		pixelHeight = 1;
		xName = "x";
		yName = "y";
		type = COORD_TL;
	}

	/**
	 * Resets the values based on the width and height of the oriented image.
	 * Used with all but custom. Should be used when the orientation changes or
	 * the image width and height change.
	 * 
	 * @param type
	 * @param width
	 * @param height
	 */
	public void reset(int type, double width, double height) {
		this.type = type;
		if (type == COORD_CUSTOM) {
			// Do nothing
			return;
		}
		xName = "x";
		yName = "y";
		switch (type) {
		case COORD_TL:
			x0 = y0 = 0;
			pixelWidth = 1;
			pixelHeight = 1;
			break;
		case COORD_TR:
			x0 = width == 0 ? 0 : width - 1;
			y0 = 0;
			pixelWidth = -1;
			pixelHeight = 1;
			break;
		case COORD_BR:
			x0 = width == 0 ? 0 : width - 1;
			y0 = height == 0 ? 0 : height - 1;
			pixelWidth = -1;
			pixelHeight = -1;
			break;
		case COORD_BL:
			x0 = 0;
			y0 = height == 0 ? 0 : height - 1;
			pixelWidth = 1;
			pixelHeight = -1;
			break;
		}
	}

	/**
	 * Resets all the parameters. Used for custom.
	 * 
	 * @param type
	 * @param x0
	 * @param y0
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param xName
	 * @param yName
	 */
	public void reset(int type, double x0, double y0, double pixelWidth,
			double pixelHeight, String xName, String yName) {
		if (type != COORD_CUSTOM) {
			reset(type, pixelWidth, pixelHeight);
			return;
		}
		this.type = type;
		this.x0 = x0;
		this.y0 = y0;
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.xName = xName;
		this.yName = yName;
	}

	/**
	 * Gets the double coordinates given the integer oriented image coordinates.
	 * 
	 * @param point
	 *            The oriented image coordinates (x,y).
	 * @return
	 */
	public double[] getCoordinatesFromOriented(Point point) {
		double x1 = pixelWidth * (point.x - x0);
		double y1 = pixelHeight * (point.y - y0);
		return new double[] { x1, y1 };
	}

	/**
	 * Gets a String with the coordinates given the oriented image coordinates.
	 * Does not include the pixel value.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getCoordinateString(int x, int y) {
		double x1 = pixelWidth * (x - x0);
		double y1 = pixelHeight * (y - y0);
		if (type == COORD_CUSTOM) {
			return String.format("%s=%g, %s=%g", xName, x1, yName, y1);
		} else {
			return String.format("%s=%d, %s=%d", xName, (int) (x1 + .5), yName,
					(int) (y1 + .5));
		}
	}

	/**
	 * Gets a String with the coordinates given the oriented image coordinates
	 * and the pixel value.
	 * 
	 * @param x
	 * @param y
	 * @param pixelValue
	 * @return
	 */
	public String getCoordinateString(int x, int y, float pixelValue) {
		double x1 = pixelWidth * (x - x0);
		double y1 = pixelHeight * (y - y0);
		
		final StringBuilder buf = new StringBuilder();
		if (type == COORD_CUSTOM) {
			buf.append( String.format("%s=%g %s=%g value=", xName, x1, yName,
					y1));
		} else {
			buf.append(  String.format("%s=%d %s=%d value=", xName,
					(int) (x1 + .5), yName, (int) (y1 + .5)));
		}
		
		buf.append(NumberFormat.getNumberInstance().format(pixelValue));
		return buf.toString();
	}

	/**
	 * Determines whether the x axis is inverted, that is, whether values
	 * increase to the left or not.
	 * 
	 * @return
	 */
	public boolean isXInverted() {
		return pixelWidth < 0;
	}

	/**
	 * Determines whether the y axis is inverted, that is, whether values
	 * increase upward or not.
	 * 
	 * @return
	 */
	public boolean isYInverted() {
		return pixelHeight < 0;
	}

	/**
	 * @return the x0
	 */
	public double getX0() {
		return x0;
	}

	/**
	 * @return the y0
	 */
	public double getY0() {
		return y0;
	}

	/**
	 * @return the pixelWidth
	 */
	public double getPixelWidth() {
		return pixelWidth;
	}

	/**
	 * @return the pixelHeight
	 */
	public double getPixelHeight() {
		return pixelHeight;
	}

	/**
	 * @return the xName
	 */
	public String getXName() {
		return xName;
	}

	/**
	 * @return the yName
	 */
	public String getYName() {
		return yName;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

}
