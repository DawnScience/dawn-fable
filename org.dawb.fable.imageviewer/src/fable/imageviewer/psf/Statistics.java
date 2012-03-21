package fable.imageviewer.psf;

import java.awt.Dimension;

import org.eclipse.swt.graphics.Rectangle;

/**
 * The <code>Statistics</code> class contains statistical values
 * of an image, by which calibration can be done.
 * <p>
 * </p>
 *
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */
public class Statistics {
	boolean readOnly;
	float min = Float.MAX_VALUE;
	float max = -Float.MAX_VALUE;
	float mean = 0.0f;
	float suggestedMin = mean;
	float suggestedMax = mean;
	Histogram histogram = null;
	//The PSF points that are highlighted.
	Point2DWithValue[] psfPoints = null;

	public Statistics( float min, float max, float mean, float suggestedMin, float suggestedMax,
			Histogram histogram, Point2DWithValue[] PSFPoints, boolean readOnly ) {
		setMinimum( min );
		setMaximum( max );
		setMean( mean );
		setSuggestedMinimum( suggestedMin );
		setSuggestedMaximum( suggestedMax );
		setHistogram( histogram );
		setPSFPoints( PSFPoints );
		setReadOnly( readOnly );
	}

	public Statistics( float min, float max, float mean,
			Histogram histogram, Point2DWithValue[] PSFPoints ) {
		this( min, max, mean, min, max, histogram, PSFPoints, true );
	}

	public Statistics( float min, float max, float mean, float suggestedMin, float suggestedMax, boolean readOnly ) {
		this( min, max, mean, suggestedMin, suggestedMax, null, null, readOnly );
	}

	public Statistics( float min, float max, float mean, boolean readOnly ) {
		this( min, max, mean, min, max, null, null, readOnly );
	}

	public Statistics( float min, float max, float mean ) {
		this( min, max, mean, min, max, null, null, true );
	}

	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly( boolean readOnly) {
		this.readOnly = readOnly;
	}

	public float getMinimum() {
		return min;
	}

	public void setMinimum( float min ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.min = min;
	}

	public float getMaximum() {
		return max;
	}

	public void setMaximum( float max ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.max = max;
	}

	public float getMean() {
		return mean;
	}

	public void setMean( float mean ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.mean = mean;
	}
	
	public float getSuggestedMinimum() {
		return suggestedMin;
	}

	public void setSuggestedMinimum( float suggestedMin ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.suggestedMin = suggestedMin;
	}

	public float getSuggestedMaximum() {
		return suggestedMax;
	}

	public void setSuggestedMaximum( float suggestedMax ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.suggestedMax = suggestedMax;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public void setHistogram( Histogram histogram ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.histogram = histogram;
	}

	public Point2DWithValue[] getPSFPoints() {
		return psfPoints;
	}

	public void setPSFPoints( Point2DWithValue[] PSFPoints ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Statistics");
		this.psfPoints = PSFPoints;
	}

	public static Statistics calculateMinMaxMean( float[] data, Dimension dataDim, Rectangle rect, boolean readOnly ) {
		int valueAmountTotal = rect.width * rect.height;
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float sum = 0.0f;
		float val;
		int xyOffset;
		for( int j = 0; j < rect.height; j++ ) {
			xyOffset = (rect.y + j) * dataDim.width + rect.x; 
			for( int i = 0; i < rect.width; i++ ) {
				val = data[ xyOffset++ ];
				if( val <= 0 )
					valueAmountTotal--;
				else
					sum += val;
				if (val < min) min = val;
				if (val > max) max = val;
			}
		}
		float mean = sum / valueAmountTotal;
		float suggestedMin = min;
		float suggestedMax = Math.min(3*mean, max);
		return new Statistics( min, max, mean, suggestedMin, suggestedMax, readOnly );
	}

	public static Statistics calculateMinMaxMean( float[] data, Dimension dataDim, Rectangle rect ) {
		return calculateMinMaxMean( data, dataDim, rect, true );
	}
	
	public float[] normalize( float[] data, Dimension dataDim, Rectangle rect, float norMaxValue ) {
		float[] result = new float[ data.length ];
		int xyOffset;
		for( int j = 0; j < rect.height; j++ ) {
			xyOffset = (rect.y + j) * dataDim.width + rect.x; 
			for( int i = 0; i < rect.width; xyOffset++, i++ ) {
				result[ xyOffset ] = norMaxValue * ( data[ xyOffset ] - min ) / ( max - min );
			}
		}
		return result;
	}

}
