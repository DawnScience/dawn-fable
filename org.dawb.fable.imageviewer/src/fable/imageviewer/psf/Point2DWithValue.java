package fable.imageviewer.psf;

import java.io.Serializable;

/**
 * Instances of this class represent places on the (x, y, z)
 * coordinate plane. The x and y coordinates are type of int,
 * and the z coordinate is type of double. In this class the
 * z coordinate is considered as the value of a function of
 * x and y coordinates. z = f( x, y ).
 * <p>
 * The order of points is defined by the order of z coordinates,
 * when using <code>compareTo</code> method. 
 * </p>
 * <p>
 * The hashCode() method in this class uses the values of the public
 * fields to compute the hash value. When storing instances of the
 * class in hashed collections, do not modify these fields after the
 * object has been inserted.  
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see Rectangle
 * 
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */

public class Point2DWithValue implements Serializable, Comparable<Point2DWithValue> {
		
	/**
	 * the x coordinate of the point
	 */
	public int x;
	
	/**
	 * the y coordinate of the point
	 */
	public int y;
	
	/**
	 * the z coordinate of the point
	 */
	public double z;
	
	static final long serialVersionUID = 3257002163938146354L;
		
	/**
	 * Constructs a new point with the given x, y and z coordinates.
	 *
	 * @param x the x coordinate of the new point
	 * @param y the y coordinate of the new point
	 * @param z the z coordinate of the new point
	 */
	public Point2DWithValue( int x, int y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Compares the argument to the receiver, and returns true
	 * if they represent the <em>same</em> object using a class
	 * specific comparison.
	 *
	 * @param object the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
	 *
	 * @see #hashCode()
	 */
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof Point2DWithValue)) return false;
		Point2DWithValue p = (Point2DWithValue)object;
		return (p.x == this.x) && (p.y == this.y) && (p.z == this.z);
	}

	/**
	 * Returns an integer hash code for the receiver. Any two 
	 * objects that return <code>true</code> when passed to 
	 * <code>equals</code> must return the same value for this
	 * method.
	 *
	 * @return the receiver's hash
	 *
	 * @see #equals(Object)
	 */
	public int hashCode() {
		return x ^ y ^ (int)z;
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the point
	 */
	public String toString() {
		return "Point {" + x + ", " + y + ", " + z + "}";
	}

	@Override
	public int compareTo(Point2DWithValue o) {
		return (int)Math.signum( this.z - o.z );
	}

}
