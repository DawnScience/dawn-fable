package fable.imageviewer.psf;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

/**
 * The <code>LogScale</code> class is <code>Scale</code> based class.
 * It supports logarithmic scaling, that is setting a logical value
 * of scale sets the log(base, value) physical value of scale.
 * <p>
 * </p>
 * 
 * @author  Gabor Naray
 * @version 1.00 20/12/2011
 * @since   20111220
 */
public class LogScale extends Scale {
	double logicalBase;
	double logicalMin;
	double logicalMax;
	
	protected void checkSubclass () {
		return; //Doing nothing, which permits subclassing
	}

	/** 
	 * LogScale's constructors.
	 */
	public LogScale(Composite parent, int style, double base) {
		super(parent, style);
		this.logicalBase = base;
		logicalMin = 0;
		logicalMax = 0;
	}

	public LogScale(Composite parent, int style) {
		this(parent, style, Math.E);
	}

	/** 
	 * LogScale's methods.
	 */
	public double getLogicalBase() {
		return logicalBase;
	}

	public void setLogicalBase(double v) {
		logicalBase = v;
	}

	public double getLogicalMinimum() {
		return logicalMin;
	}

	public void setLogicalMinimum(double v) {
		logicalMin = v;
		updateBase();
	}

	public double getLogicalMaximum() {
		return logicalMax;
	}

	public void setLogicalMaximum(double v) {
		logicalMax = v;
		updateBase();
	}

	public void setLogicalMinMax(double min, double max) {
		logicalMin = min;
		logicalMax = max;
		updateBase();
	}

	protected void updateBase() {
		if( getMaximum() - getMinimum() < 0 )
			throw new NegativeIntervalException("LogScale: maximum<minimum");
		if( logicalMax - logicalMin < 0 )
			throw new NegativeIntervalException("LogScale: logical maximum<logical minimum");
		/**
		 * The expression for logicalBase is the result of a condition:
		 * f(m+1)-f(m)=1. Difficult solve this equation, so this one is used:
		 * f'(m+1)>=1. Approximated solving it we get the expression.
		 */
		logicalBase = Math.pow(logicalMax - logicalMin, 1.0/( getMaximum() - getMinimum() +1 ));
	}

	public double getLogicalSelection() {
		/**
		 * f(x)=(logicalBase^(x-min)-1)/(logicalBase^(max-min)-1)*(logicalMax-logicalMin)+logicalMin
		 */
		return ( Math.pow(logicalBase, getSelection() - getMinimum()) - 1 )
				/ ( Math.pow(logicalBase, getMaximum() - getMinimum()) - 1 )
				* ( logicalMax - logicalMin ) + logicalMin;
	}

	public void setLogicalSelection(double vLog) {
		int v = (int)Math.round( Math.log10(
				( ( vLog - logicalMin ) / ( logicalMax - logicalMin )
				* ( Math.pow(logicalBase, getMaximum() - getMinimum()) - 1) + 1 )
				* Math.pow( logicalBase, getMinimum() ) )
				/ Math.log10( logicalBase ) );
		if( v < getMinimum() )
			v = getMinimum();
		if( v > getMaximum() )
			v = getMaximum();
		if( v == getSelection() )
			return;
		setSelection(v);
	}

}
