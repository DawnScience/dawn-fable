package fable.imageviewer.psf;

/**
 * The <code>Histogram</code> class contains the histogram
 * of an image, by which calibration can be done even better
 * than by simple minimum, maximum values.
 * <p>
 * </p>
 *
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */
public class Histogram {
	boolean readOnly;
	int[] binValues = null;
	float minimumValue;
	float binWidth;
	int valueAmountTotal;

	public Histogram( int[] binValues, float minimumValue, float binWidth, int valueAmountTotal, boolean readOnly ) {
		setBinValues( binValues );
		setMinimum( minimumValue );
		setBinWidth( binWidth );
		setReadOnly( readOnly );
	}

	public Histogram( int[] binValues, float minimumValue, float binWidth, int valueAmountTotal ) {
		this( binValues, minimumValue, binWidth, valueAmountTotal, true );
	}

	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly( boolean readOnly) {
		this.readOnly = readOnly;
	}

	public int[] getBinValues() {
		return binValues;
	}

	public void setBinValues( int[] binValues) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Histogram");
		this.binValues = binValues;
	}

	public float getMinimum() {
		return minimumValue;
	}

	public void setMinimum( float minimumValue ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Histogram");
		this.minimumValue = minimumValue;
	}

	public float getBinWidth() {
		return binWidth;
	}

	public void setBinWidth( float binWidth ) {
		if( readOnly )
			throw new SecurityException("Attempt to modify read only Histogram");
		this.binWidth = binWidth;
	}

}
