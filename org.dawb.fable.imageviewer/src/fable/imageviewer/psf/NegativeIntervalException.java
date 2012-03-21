/*
 * @(#)NegativeIntervalException.java	1.0 20/12/2011
 *
 */
package fable.imageviewer.psf;

/**
 * Thrown if an application tries to set an interval with negative size.
 *
 * @author  Gabor Naray
 * @version 1.00, 20/12/2011
 * @since   20111220
 */
public class NegativeIntervalException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1944139991879221056L;

	/**
     * Constructs a <code>NegativeIntervalException</code> with no 
     * detail message. 
     */
	public NegativeIntervalException() {
		super();
	}

    /**
     * Constructs a <code>NegativeIntervalException</code> with the 
     * specified detail message. 
     *
     * @param   s   the detail message.
     */
    public NegativeIntervalException(String s) {
    	super(s);
    }

}
