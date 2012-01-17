/*******************************************************************************
 * Copyright 2007, UChicago Argonne, LLC
 * 
 * All Rights Reserved
 * 
 * X-Ray Analysis Software (XRAYS)
 * 
 * OPEN SOURCE LICENSE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. Software changes,
 * modifications, or derivative works, should be noted with comments and the
 * author and organizations name.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the names of UChicago Argonne, LLC or the Department of Energy nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * 4. The software and the end-user documentation included with the
 * redistribution, if any, must include the following acknowledgment:
 * 
 * "This product includes software produced by UChicago Argonne, LLC under
 * Contract No. DE-AC02-06CH11357 with the Department of Energy."
 * 
 * ***************************************************************************
 * 
 * DISCLAIMER
 * 
 * THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND.
 * 
 * NEITHER THE UNITED STATES GOVERNMENT, NOR THE UNITED STATES DEPARTMENT OF
 * ENERGY, NOR UCHICAGO ARGONNE, LLC, NOR ANY OF THEIR EMPLOYEES, MAKES ANY
 * WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR
 * RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY
 * INFORMATION, DATA, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS
 * THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 * 
 ******************************************************************************/

/*
 * Program to 
 * Created on Nov 25, 2006
 * By Kenneth Evans, Jr.
 */

package fable.imageviewer.model;

import java.beans.PropertyChangeEvent;

/**
 * ImageModelEvent is currently the same as a propertyChange Event. It may be
 * expanded in the future.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ImageModelEvent extends PropertyChangeEvent
{
  private static final long serialVersionUID = 1L;

  public ImageModelEvent(Object source, String propertyName, Object oldValue,
    Object newValue) {
    super(source, propertyName, oldValue, newValue);
    // TODO Auto-generated constructor stub
  }

}
