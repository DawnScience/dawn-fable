/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.toolbox;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import fable.framework.toolbox.IImagesKeys;



public class LookAndFeel {

	private static ImageDescriptor imageRequired = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_REQUIRED);
	
	public static Label getLabelError(Composite parent){
		Label lblError= new Label(parent, SWT.NONE);
		final Image imgExp= imageRequired.createImage();
		lblError.setImage(imageRequired.createImage());
		lblError.setVisible(false);
		lblError.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				imgExp.dispose();
				
			}
			
		});
		return lblError;
	}
	public static Button getValidatePushButton(Composite parent){
		Button btn = new Button(parent, SWT.PUSH);
		try {
			//Platform.getBundle("YourPluginID").getEntry("/") 
			ImageDescriptor imageRequired = ImageDescriptor.createFromURL(new URL(IImagesKeys.BTN_IMG_FORWARD));
			btn.setToolTipText("Validate View");
			btn.setImage(imageRequired.createImage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//btn.setImage(image);
		return btn;
	}
	
	/**
	 * @param Composite Parent : Parent composite
	 * @param String grpText : Label for the group box
	 * @param int numCols : columns number
	 * @param int horizontalSpan : horizontal span for the group (compared to a gridLayout)
	 * @return Group : the new group
	 * @Description : This function build a new Group 
	 */
	public static Group getGroup(Composite parent, String grpText, int numCols, int horizontalSpan){

		Group group  = new Group(parent, SWT.FILL | SWT.RESIZE);
		GridLayout gd = new GridLayout();
		gd.numColumns=numCols;
		group.setLayout(gd);		
		GridData gridData = new GridData(SWT.FILL,SWT.RESIZE ,true,false);
		gridData.horizontalSpan=horizontalSpan;
		group.setLayoutData(gridData);
		group.setText(grpText);
		return group;
	}

	public static Group getGroupGlobal(Composite container){
		GridLayout gd = new GridLayout();
		gd.numColumns=1;
		
		gd.makeColumnsEqualWidth=true;
		Group grp_Global  = new Group(container, SWT.FILL|SWT.RESIZE);
		//grp_Global  = new Group(container,SWT.RESIZE);
		grp_Global.setLayout(gd);			
		GridData gdData = new GridData( SWT.FILL,SWT.RESIZE,true,true);
		gdData.grabExcessVerticalSpace=true;
		gdData.verticalAlignment=GridData.FILL;
		
		grp_Global.setLayoutData(gdData);	
		return grp_Global;
	}
}
