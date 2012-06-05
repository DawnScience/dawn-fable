/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.model;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import jep.JepException;

import org.dawb.fabio.FabioFile;
import org.eclipse.swt.graphics.Rectangle;

import org.embl.cca.utils.imageviewer.Histogram;
import org.embl.cca.utils.imageviewer.PointWithValueFFF;
import org.embl.cca.utils.imageviewer.PointWithValueIIF;
import org.embl.cca.utils.imageviewer.QuickSort;
import org.embl.cca.utils.imageviewer.Statistics;
import org.slf4j.Logger;

import fable.imageviewer.component.ImageComponentImage;

/**
 * This class implements a simple image model that stores the the width, height,
 * and the pixel data. The data are stored as a float[index] with index = col +
 * row * width. It calculates the statistics (min, max, and mean) when requested
 * and then stores the values.
 * 
 * @author evans
 * 
 */
public class ImageModel implements Cloneable {
	// Note: Change the ImageInfoAction if more fields are added
	private EventListenerList listenerList = null;
	private String fileName = null;
	private int width = 0;
	private int height = 0;
	private float[] data = null;
	private Statistics statistics = null;
	private long time;

	// Property change names
	/**
	 * Denotes that the data and statistics changed but not the other
	 * parameters.
	 */
	public static final String DATA_CHANGED = ImageModel.class.getName()
			+ ".DataChanged";
	/**
	 * Denotes the data and parameters changed.
	 */
	public static final String RESET = ImageModel.class.getName() + ".Reset";

	/**
	 * Empty constructor. Sets the listenerList.
	 */
	public ImageModel() {
		listenerList = new EventListenerList();
	}

	/**
	 * Constructor that sets the model based on the given FabioFile. Calls
	 * reset(fabioFile). Note that any events fired will have no listeners, yet.
	 * If you need to be informed of events, create an ImageModel, add the
	 * listeners, and use reset instead of constructing a new ImageModel.
	 * 
	 * @param fabioFile
	 * @throws JepException
	 */
	public ImageModel(FabioFile fabioFile) throws Throwable {
		this();
		set(fabioFile);
	}

	/**
	 * Constructor that sets the model based on the given parameters. Calls
	 * reset(fileName, width, height, data). Note that any events fired will
	 * have no listeners, yet. If you need to be informed of events, create an
	 * ImageModel, add the listeners, and use reset instead of constructing a
	 * new ImageModel.
	 * 
	 * @param fileName
	 * @param width
	 * @param height
	 * @param data
	 * @param time
	 */
	public ImageModel(String fileName, int width, int height, float[] data, long time) {
		this();
		reset(fileName, width, height, data);
		this.time = time;
	}

    public ImageModel clone() {
    	ImageModel clone = new ImageModel();
		if( fileName != null )
			clone.fileName = fileName;
		clone.width = width;
		clone.height = height;
		if( data != null )
			clone.data = data.clone();
		if( statistics != null )
			clone.statistics = statistics.clone();
		clone.time = time;
    	return clone;
    }

    /**
	 * Adds the listener.
	 * 
	 * @param l
	 */
	public void addImageModelListener(ImageModelListener l) {
		listenerList.add(ImageModelListener.class, l);
	}

	/**
	 * Removes the listener.
	 * 
	 * @param l
	 */
	public void removeImageModelListener(ImageModelListener l) {
		listenerList.remove(ImageModelListener.class, l);
	}

	/**
	 * Removes all the listeners.
	 * 
	 * @param l
	 */
	public void removeAllImageModelListeners(ImageModelListener l) {
		EventListener[] listeners = listenerList
				.getListeners(ImageModelListener.class);
		for (EventListener listener : listeners) {
			listenerList.remove(ImageModelListener.class,
					(ImageModelListener) listener);
		}
	}

	/**
	 * Fires an ImageModelEvent with the given parameters.
	 * 
	 * @param name
	 *            Should be one of the ImageModel.xxx_CHANGED names.
	 * @param oldValue
	 * @param newValue
	 */
	protected void fireImageModelEvent(String name, Object oldValue,
			Object newValue) {
		EventListener[] listeners = listenerList
				.getListeners(ImageModelListener.class);
		for (EventListener listener : listeners) {
			ImageModelEvent imageModelEvent = new ImageModelEvent(this, name,
					oldValue, newValue);
			((ImageModelListener) listener).propertyChange(imageModelEvent);
		}
	}

	/**
	 * Resets the model based on the given FabioFile;
	 * 
	 * @param fabioFile
	 * @throws JepException
	 */
	public void set(FabioFile fabioFile) throws Throwable {
		try {
			statistics = null;
			this.fileName = fabioFile.getFileName();
			this.data     = fabioFile.getImageAsFloat();
			this.width    = fabioFile.getWidth();
			this.height   = fabioFile.getHeight();
			this.time     = fabioFile.getTimeToReadImage();
		} finally {
			fireImageModelEvent(RESET, this, this);
		}
	}

	/**
	 * Resets the model based on the given parameters. Will cause a RESET
	 * ImageModelEvent but not a DATA_CHANGED event to be fired.
	 * 
	 * @param fileName
	 * @param width
	 * @param height
	 * @param mean
	 * @param data
	 */
	public void reset(String fileName, int width, int height, float[] data) {
		statistics = null;
		this.fileName = fileName;
		this.width = width;
		this.height = height;
		this.data = data;
		fireImageModelEvent(RESET, this, this);
	}

	/**
	 * Calculates the statistics (min, max, mean) for the whole image and stores
	 * it.
	 */
	private void calculateStatistics() {
		statistics = getStatistics(new Rectangle(0,0,width,height));
	}

	// Getters and setters

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	public Rectangle getRect() {
		return new Rectangle(0, 0, width, height);
	}

	/**
	 * Get the statistics (min, max, mean) for the whole image. The values are
	 * cached after the first time they are calculated.
	 * 
	 * @return The statistics
	 * @remark as float[3] = {min, max, mean}.
	 */
	public Statistics getStatistics() {
		if (statistics == null) {
			calculateStatistics();
		}
		return statistics;
	}

    /**
     * Searches the specified bins vector for the bin (range of keys)
     * containing the specified key using the binary search algorithm. The
     * vector must be sorted into ascending order prior to making this call.
     * If it is not sorted, the results are undefined. If the vector contains
     * multiple elements containing the specified key, there is no guarantee
     * which one will be found.
     *
     * <p>This method runs in log(n) time for a "random access" vector (which
     * provides near-constant-time positional access).
     *
     * @param  bins the bins vector to be searched.
     * @param  key the value of which bin to be searched for.
     * @return the index of the search key, if it is contained in the vector;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the vector: the index of the first
     *	       element greater than the key, or <tt>vector.size()</tt> if all
     *	       elements in the vector are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     */
	public int searchBin( Vector<PointWithValueFFF> bins, float key ) {
		int low = 0;
		int high = bins.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if( key < bins.get(mid).x )
                high = mid - 1;
            else {
            	if( key > bins.get(mid).y )
            		low = mid + 1;
            	else
            		return mid; // key found
            }
        }
        return -(low + 1);  // key not found
	}
/*
	public Statistics getStatistics(Rectangle rect) {
		final Logger logger = org.slf4j.LoggerFactory.getLogger(ImageModel.class);
		Rectangle imageRect = new Rectangle( 0, 0, width, height );
		Rectangle constrained = imageRect.intersection(rect);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		float[] rectData = new float[ constrained.width * constrained.height ];
		int d = 0;
		int s = constrained.y * width + constrained.x;
		float sum = 0.0f;
		long t0 = System.nanoTime();
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				sum += data[ s ];
				rectData[ d++ ] = data[ s++ ];
			}
			s = sj + width;
		}

		long t1 = System.nanoTime();
		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		float[] sortedData = QuickSort.sort( rectData );
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec

//		final Dimension dataDim = new Dimension( width, height );
		Statistics minMaxMean = new Statistics( sortedData[0], sortedData[sortedData.length - 1], sum / sortedData.length, false );

		final int binAmountMax = 100; 
		Vector<PointWithValueFFF> bins = new Vector<PointWithValueFFF>(binAmountMax);
		bins.add( new PointWithValueFFF(Float.MIN_VALUE, Float.MAX_VALUE, 0) );
		iMax = constrained.width;
		jMax = constrained.height;
		for( int j = 0; j < jMax; j++ ) {
			int xyOffset = j * width; 
			for ( int i = 0; i < iMax; i++ ) {
				int binIndex = searchBin(bins, rectData[ xyOffset++ ]);
				PointWithValueFFF bin = bins.get(binIndex);
				bins.get(binIndex).z++;
				if( )
			}
		}
		long t3 = System.nanoTime();
		logger.debug( "binning.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		

//		float min = minMaxMean.getMinimum();
//		int[] histogram = new int[ (int) ( minMaxMean.getMaximum() - min + 1 ) ];
//		for (int j = 0; j < rect.height; j++) {
//			int xyOffset = (rect.y + j) * width + rect.x; 
//			for (int i = 0; i < rect.width; i++) {
//				histogram[ (int)( data[ xyOffset++ ] - min ) ]++;
//			}
//		}

//		minMaxMean.setHistogram( new Histogram( histogram, minMaxMean.getMinimum(), binWidth, valueAmountTotal ) );
//		minMaxMean.setPSFPoints( psfPoints );
		minMaxMean.setReadOnly( true );
		return minMaxMean;
	}
*/
	/**
	 * Get the statistics (min, max, mean) for a sub Rectangle. These are
	 * calculated each time this method is called.
	 * 
	 * @param rect
	 * @return The statistics
	 * @remark as float[3] = {min, max, mean}.
	 */
	public Statistics getStatistics(Rectangle rect) {
		final Dimension dataDim = new Dimension( width, height );
		Statistics minMaxMean = Statistics.calculateMinMaxMean( data, dataDim, rect, false );

		final int binAmountMax = 0x100000; 
//		float[] normData = minMaxMean.normalize( data, dataDim, rect, binAmountMax ); //Maybe in the future

		/**
		 * Creating a histogram for a wide range, that is binAmountMax bins.
		 * To make it fast, array is used which takes a lot of space. Then it
		 * is packed in a dynamic histogram.
		 * How? Using a binWidthMax and binHeightLimiter (anyway binWidthMin=1).
		 * If adding next bin to this bin would exceed binHeightLimiter, then next bin
		 * must be a separated bin, and this bin is closed. However if adding does
		 * not exceed binAmountLimiter, but the united binWidth would exceed binWidthMax,
		 * then next bin must be a separated bin, and this bin is closed.
		 * For example, binWidthMax = binAmountMax/100, binHeightLimiter = binAmountMax/binWidthMax.
		 * To store this dynamic histogram, the min and max of bin, and amount of values
		 * in that bin must be stored for each bin. Searching a bin can be done by binary
		 * search, assuming the bins are stored ordered.
		 */
		float min = minMaxMean.getMinimum();
		int[] histogram = new int[ (int) ( minMaxMean.getMaximum() - min + 1 ) ];
		for (int j = 0; j < rect.height; j++) {
			int xyOffset = (rect.y + j) * width + rect.x; 
			for (int i = 0; i < rect.width; i++) {
				histogram[ (int)( data[ xyOffset++ ] - min ) ]++;
			}
		}
		// Creating histogram
		// At most 1% of maximum amount of bins can be the bin width
		final int binWidthMax = binAmountMax / 100; //Value by experience
		// The preferred bin size
		final int binHeightLimiter = binAmountMax / binWidthMax; //Expression by experience
		int binIndex = 0;
		int binStart = 0;
//		int binValueAmount = 0;
		PointWithValueIIF[] dynHistogram = null;
		dynHistogram = new PointWithValueIIF[ histogram.length ];
		int sum = 0;
		int iH;
		for( iH = 0; iH < histogram.length; iH++ ) {
			int val = histogram[ iH ];
			if( ( sum > 0 && sum + val > binHeightLimiter ) || iH - binStart > binWidthMax ) {
				dynHistogram[ binIndex++ ] = new PointWithValueIIF( binStart, iH - 1, sum );
				binStart = iH;
				sum = val;
			} else
				sum += val;
		}
		dynHistogram[ binIndex++ ] = new PointWithValueIIF( binStart, iH - 1, sum );
		PointWithValueIIF[] dynHistogramPacked = new PointWithValueIIF[ binIndex ];
		System.arraycopy( dynHistogram, 0, dynHistogramPacked, 0, binIndex );

		/**
		 */
		// Searching for the 1% (but >=valueAmountMin) of values to be highlighted by PSF
		final int valueAmountTotal = rect.width * rect.height;
		final int valueAmountMin = 10000; //Value by experience
		int valueAmountMax = Math.max( valueAmountTotal / 100, valueAmountMin );
		int valueAmountPartial = 0;
		for( iH = dynHistogramPacked.length - 1; iH >= 0; iH-- ) {
			valueAmountPartial += dynHistogramPacked[ iH ].z;
			if( valueAmountPartial > valueAmountMax )
				break;
		}
		if( iH >= 0 )
			valueAmountPartial -= dynHistogramPacked[ iH ].z;
		PointWithValueIIF[] psfPoints = new PointWithValueIIF[ valueAmountPartial ];
		// Searching for the values >= highlightValueMin to be highlighted by PSF
		if( iH + 1 < dynHistogramPacked.length ) { //else way too many values in last bin
			float highlightValueMin = dynHistogramPacked[ iH + 1 ].x + minMaxMean.getMinimum();
			iH = 0;
			for( int j = 0; j < rect.height; j++ ) {
				int xyOffset = (rect.y + j) * width + rect.x; 
				for( int i = 0; i < rect.width; i++ ) {
					float val = data[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						psfPoints[ iH++ ] = new PointWithValueIIF( i, j, val );
					}
				}
			}
		}

		Arrays.sort( psfPoints );
		//TODO passing dynHistogram instead of(?) histogram
		float binWidth = 0; //TODO Set value, Width of bins
		minMaxMean.setHistogram( new Histogram( histogram, minMaxMean.getMinimum(), binWidth, valueAmountTotal ) );
		minMaxMean.setPSFPoints( psfPoints );
		minMaxMean.setReadOnly( true );
		return minMaxMean;
	}

	/**
	 * @return the data
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * Return the value of the data corresponding to the given row and column of
	 * the stored data.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public float getData(int row, int col) {
		if (data == null) {
			return Float.NaN;
		}
		return data[col + row * width];
	}

	/**
	 * return the value of the data corresponding to given row and column of the
	 * given Rectangle.
	 * 
	 * @param row
	 * @param col
	 * @param rect
	 * @return
	 */
	public float getData(int row, int col, Rectangle rect) {
		if (data == null) {
			return Float.NaN;
		}
		int index1 = col + row * rect.width;
		int col1 = index1 % width;
		int row1 = index1 / width;
		return data[col1 + row1 * width];
	}

	/**
	 * Returns a sub array of the data corresponding to the given Rectangle.
	 * 
	 * @param rect
	 * @return
	 */
	public float[] getData(Rectangle rect) {
		if (data == null) {
			return null;
		}
		float[] array = new float[rect.width * rect.height];
		for (int j = 0; j < rect.height; j++) {
			for (int i = 0; i < rect.width; i++) {
				array[i + j * rect.width] = data[rect.x + i + (rect.y + j)
						* width];
			}
		}
		return array;
	}

	/**
	 * Sets a new value for the data and cause a DATA_CHANGED ImageModelEvent to
	 * be fired.
	 * 
	 * @param data
	 *            the data to set
	 */
	public void setData(float[] data) {
		float[] oldValue = this.data;
		if (data != oldValue) {
			statistics = null;
			this.data = data;
			fireImageModelEvent(DATA_CHANGED, oldValue, data);
		}
	}

	public long getTimeToReadImage() {
		return time;
	}

	//Assuming the width and height is same in this and in imageModel
	public void addImageModel( ImageModel imageModel ) {
		float[] fthisdata = getData();
		float[] fdata = imageModel.getData();
		int jMax = Math.min( fthisdata.length, fdata.length ); //If assumption is right, the two lengths are same
		for( int j = 0; j < jMax; j++ )
			fthisdata[j] += fdata[j];
	}

	//Assuming the width and height is same in this and in imageModel
	public void subImageModel( ImageModel imageModel ) {
		float[] fsetdata = getData();
		float[] fdata = imageModel.getData();
		int jMax = Math.min( fsetdata.length, fdata.length ); //If assumption is right, the two lengths are same
		for( int j = 0; j < jMax; j++ )
			fsetdata[j] -= fdata[j];
	}
}
