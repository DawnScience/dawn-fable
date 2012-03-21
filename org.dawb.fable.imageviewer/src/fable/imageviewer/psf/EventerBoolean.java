package fable.imageviewer.psf;

/*
 * @(#)EventerBoolean.java	1.00 07/12/2011
 *
 */

/**
 * The <code>EventerBoolean</code> class wraps a value of the primitive type 
 * <code>boolean</code> in an object. An object of type 
 * <code>EventerBoolean</code> contains a single field whose type is 
 * <code>boolean</code>. 
 * <p>
 * In addition, this class provides many methods for 
 * converting a <code>boolean</code> to a <code>String</code> and a 
 * <code>String</code> to a <code>boolean</code>, as well as other 
 * constants and methods useful when dealing with a 
 * <code>boolean</code>. 
 * <p>
 * This class is extension of <code>Boolean</code> class, but since
 * <code>Boolean</code> class is final, the <code>EventerBoolean</code>
 * class can not subclass <code>Boolean</code> class, thus it is a new
 * base class. The reason of extension is adding events to the class, that can
 * be processed by event handlers.
 *
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */

interface ValueChangedEvent {
  void handler(Object oldValue);
}

public class EventerBoolean implements java.io.Serializable,
									   Comparable<EventerBoolean>
{
    /** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = -7123983763376233817L;

	/** 
     * The <code>EventerBoolean</code> object corresponding to the primitive 
     * value <code>true</code>. 
     */
    public static final EventerBoolean TRUE = new EventerBoolean(true);

    /** 
     * The <code>EventerBoolean</code> object corresponding to the primitive 
     * value <code>false</code>. 
     */
    public static final EventerBoolean FALSE = new EventerBoolean(false);

    /**
     * The value of the EventerBoolean.
     *
     * @serial
     */
    protected boolean value;

    protected ValueChangedEvent onChanged;

    /**
     * Allocates a <code>EventerBoolean</code> object representing the 
     * <code>value</code> argument. 
     *
     * <p><b>Note: It is rarely appropriate to use this constructor.
     * Unless a <i>new</i> instance is required, the static factory
     * {@link #valueOf(boolean)} is generally a better choice. It is
     * likely to yield significantly better space and time performance.</b>
     * 
     * @param   value   the value of the <code>EventerBoolean</code>.
     */
    public EventerBoolean(boolean value) {
   		setValue( value );
    }

    public EventerBoolean(boolean value, ValueChangedEvent vce) {
   		setValue( value );
   		setOnChanged( vce );
    }

    public EventerBoolean(Boolean b) {
    	this( b.booleanValue() );
    }

    public EventerBoolean(Boolean b, ValueChangedEvent vce) {
    	this( b.booleanValue(), vce );
    }

    public EventerBoolean(EventerBoolean b) {
    	this( b.value );
    }

    public EventerBoolean(EventerBoolean b, ValueChangedEvent vce) {
    	this( b.value, vce );
    }

    /**
     * Allocates a <code>EventerBoolean</code> object representing the value 
     * <code>true</code> if the string argument is not <code>null</code> 
     * and is equal, ignoring case, to the string {@code "true"}. 
     * Otherwise, allocate a <code>EventerBoolean</code> object representing the 
     * value <code>false</code>. Examples:<p>
     * {@code new EventerBoolean("True")} produces a <tt>EventerBoolean</tt> object 
     * that represents <tt>true</tt>.<br>
     * {@code new EventerBoolean("yes")} produces a <tt>EventerBoolean</tt> object 
     * that represents <tt>false</tt>.
     *
     * @param   s   the string to be converted to a <code>EventerBoolean</code>.
     */
    public EventerBoolean(String s) {
    	this( toBoolean( s ) );
    }

    public EventerBoolean(String s, ValueChangedEvent vce) {
    	this( toBoolean( s ), vce );
    }

    public void setValue(boolean value) {
        boolean oldValue = this.value; 
    	this.value = value;
    	if( onChanged != null && oldValue != this.value )
    	  onChanged.handler( oldValue );
    }

    public void setValue(Boolean b) {
    	setValue( b.booleanValue() );
    }

    public void setValue(EventerBoolean b) {
    	setValue( b.value );
    }

    public void setValue(String s) {
    	setValue( toBoolean( s ) );
    }

    /**
     * Returns the value of this <tt>EventerBoolean</tt> object as a boolean 
     * primitive.
     *
     * @return  the primitive <code>boolean</code> value of this object.
     */
    public boolean getValue() {
    	return value;
    }

    public ValueChangedEvent getOnChanged() {
    	return onChanged;
    }

    public void setOnChanged(ValueChangedEvent vce) {
    	onChanged = vce;
    }

    /**
     * Parses the string argument as a boolean.  The <code>boolean</code> 
     * returned represents the value <code>true</code> if the string argument 
     * is not <code>null</code> and is equal, ignoring case, to the string 
     * {@code "true"}. <p>
     * Example: {@code EventerBoolean.parseBoolean("True")} returns <tt>true</tt>.<br>
     * Example: {@code EventerBoolean.parseBoolean("yes")} returns <tt>false</tt>.
     *
     * @param      s   the <code>String</code> containing the boolean
     *                 representation to be parsed
     * @return     the boolean represented by the string argument
     */
    public static boolean parseBoolean(String s) {
        return toBoolean(s);
    }

    /**
     * Returns a <tt>EventerBoolean</tt> instance representing the specified
     * <tt>boolean</tt> value.  If the specified <tt>boolean</tt> value
     * is <tt>true</tt>, this method returns <tt>EventerBoolean.TRUE</tt>;
     * if it is <tt>false</tt>, this method returns <tt>EventerBoolean.FALSE</tt>.
     * If a new <tt>EventerBoolean</tt> instance is not required, this method
     * should generally be used in preference to the constructor
     * {@link #EventerBoolean(boolean)}, as this method is likely to yield
     * significantly better space and time performance.
     *
     * @param  b a boolean value.
     * @return a <tt>EventerBoolean</tt> instance representing <tt>b</tt>.
     */
    public static EventerBoolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
    }

    /**
     * Returns a <code>Boolean</code> with a value represented by the
     * specified string.  The <code>EventerBoolean</code> returned represents a
     * true value if the string argument is not <code>null</code>
     * and is equal, ignoring case, to the string {@code "true"}.
     *
     * @param   s   a string.
     * @return  the <code>EventerBoolean</code> value represented by the string.
     */
    public static EventerBoolean valueOf(String s) {
    	return toBoolean(s) ? TRUE : FALSE;
    }

    /**
     * Returns a <tt>String</tt> object representing the specified
     * boolean.  If the specified boolean is <code>true</code>, then
     * the string {@code "true"} will be returned, otherwise the
     * string {@code "false"} will be returned.
     *
     * @param b	the boolean to be converted
     * @return the string representation of the specified <code>boolean</code>
     */
    public static String toString(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * Returns a <tt>String</tt> object representing this EventerBoolean's
     * value.  If this object represents the value <code>true</code>,
     * a string equal to {@code "true"} is returned. Otherwise, a
     * string equal to {@code "false"} is returned.
     *
     * @return  a string representation of this object. 
     */
    public String toString() {
    	return value ? "true" : "false";
    }

    /**
     * Returns a hash code for this <tt>EventerBoolean</tt> object.
     *
     * @return  the integer <tt>1231</tt> if this object represents 
     * <tt>true</tt>; returns the integer <tt>1237</tt> if this 
     * object represents <tt>false</tt>. 
     */
    public int hashCode() {
    	return value ? 1231 : 1237;
    }

    /**
     * Returns <code>true</code> if and only if the argument is not 
     * <code>null</code> and is a <code>Boolean</code> or <code>EventerBoolean</code> object that 
     * represents the same <code>boolean</code> value as this object. 
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the Boolean objects represent the 
     *          same value; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
    	if (obj instanceof EventerBoolean) {
    		return value == ((EventerBoolean)obj).value;
    	} else if (obj instanceof Boolean) {
    		return value == ((Boolean)obj).booleanValue();
    	} 
    	return false;
    }

    /**
     * Returns <code>true</code> if and only if the system property 
     * named by the argument exists and is equal to the string 
     * {@code "true"}. (Beginning with version 1.0.2 of the 
     * Java<small><sup>TM</sup></small> platform, the test of 
     * this string is case insensitive.) A system property is accessible 
     * through <code>getProperty</code>, a method defined by the 
     * <code>System</code> class.
     * <p>
     * If there is no property with the specified name, or if the specified
     * name is empty or null, then <code>false</code> is returned.
     *
     * @param   name   the system property name.
     * @return  the <code>boolean</code> value of the system property.
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static boolean getBoolean(String name) {
        boolean result = false;
        try {
            result = toBoolean(System.getProperty(name));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
        }
        return result;
    }

    /**
     * Compares this <tt>EventerBoolean</tt> instance with another.
     *
     * @param   b the <tt>EventerBoolean</tt> instance to be compared
     * @return  zero if this object represents the same boolean value as the
     *          argument; a positive value if this object represents true
     *          and the argument represents false; and a negative value if
     *          this object represents false and the argument represents true
     * @throws  NullPointerException if the argument is <tt>null</tt>
     * @see     Comparable
     */
    public int compareTo(EventerBoolean b) {
        return (b.value == value ? 0 : (value ? 1 : -1));
    }

    /**
     * Compares this <tt>EventerBoolean</tt> instance with a <tt>Boolean</tt> instance.
     *
     * @param   b the <tt>Boolean</tt> instance to be compared
     * @return  zero if this object represents the same boolean value as the
     *          argument; a positive value if this object represents true
     *          and the argument represents false; and a negative value if
     *          this object represents false and the argument represents true
     * @throws  NullPointerException if the argument is <tt>null</tt>
     * @see     Comparable
     */
    public int compareTo(Boolean b) {
        return (b.booleanValue() == value ? 0 : (value ? 1 : -1));
    }

    private static boolean toBoolean(String name) { 
    	return ((name != null) && name.equalsIgnoreCase("true"));
    }
}
