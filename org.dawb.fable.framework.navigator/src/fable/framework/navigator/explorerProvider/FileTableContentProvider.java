/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.navigator.explorerProvider;
/*
 * @author Suchet
 * @date March 29, 2007
 * @decription : this class provides content in a table content provider
 */
import java.io.*;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileTableContentProvider implements IStructuredContentProvider {

	/*@arguments Object inputElement the root
	 *@return Object[] a list of child elements under the root element
	 * */
	public Object[] getElements(Object inputElement) {
		
		Object[] kids = ((File)inputElement).listFiles(new FileFilter(){

			public boolean accept(File pathname) {
				
				return !pathname.isHidden();
			}
			
		});
		/*new FileFilter(){

			public boolean accept(File arg0) {
				
				return arg0.isFile();
			}});
		*/
		return kids==null ? new Object[0] : kids;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
