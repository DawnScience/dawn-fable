/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.jputenv;

/**
 * putenv method
 */
public class Putenv {
  //----------------------------------------------------------------
  // static initializer
  //----------------------------------------------------------------
  static boolean loaded = false;
  static {
    init();
  }
  //----------------------------------------------------------------
  // private static methods
  //----------------------------------------------------------------
  /**
   * Initialize the class.
   */
  public static void init() {
	    if(!loaded) {
	        System.loadLibrary("jputenv");
	        loaded = true;
	    }	  
  }

  /**
   * Set an environment variable using the native function `putenv' defined in POSIX and ANSI C.
   * @param name the name of an environment variable.
   * @param value value of an environment variable.
   * @return 0 on success, or -1 on failure.
   */
  public static synchronized native int putenv(String name, String value);

  //----------------------------------------------------------------
  // constructors and finalizers
  //----------------------------------------------------------------
  /**
   * Dummy constructor.
   */
  private Putenv() throws NoSuchMethodException {
    throw new NoSuchMethodException();
  }
}
