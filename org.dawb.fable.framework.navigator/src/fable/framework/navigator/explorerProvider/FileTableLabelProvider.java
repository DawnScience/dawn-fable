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

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import fable.framework.navigator.Activator;
import fable.framework.navigator.toolBox.IImagesKeys;
/*
 * @author Suchet
 * @date March 29, 2007
 * @decription : this class provides labels in a table content provider
 */
public class FileTableLabelProvider implements ITableLabelProvider {
	private Image _file, _dir, _esrf, _sample;;
	private ImageDescriptor _ImageDescDir = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_DIR);
	private ImageDescriptor _ImageDescFile = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_FILE);
	private ImageDescriptor _ImageEDF = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_ESRF);
	private ImageDescriptor _ImageSample = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImagesKeys.IMG_SAMPLE);

	private Vector<ILabelProviderListener> _listener ;
	/**
	 * 
	 */
	public FileTableLabelProvider() {
		_file = _ImageDescFile.createImage();
		_dir = _ImageDescDir.createImage();
		_esrf = _ImageEDF.createImage();
		_sample = _ImageSample.createImage();
		_listener=new Vector<ILabelProviderListener>();
	}

	public Image getColumnImage(Object element, int columnIndex) {
	
		/*if(columnIndex==0){
			if(((File)element).isDirectory()){
				image= _dir;
			}else{
				image= _file;
			}
		}*/
		Image img=null;
		if(columnIndex==0){
		File f= (File)element;
		if(f.getName().endsWith(".edf")){
			img=  _esrf;
		}else{
		 
			if(f.isDirectory() ){
			
				String[] list = null;//ToolBox.getFilesFromDirectory((String)f.getPath(), ".edf") ;
				if(list !=null && list.length >0){
					img=_sample;
				}else{
					img=_dir;
				}
			}else{
				img=_file;
			}
			
		}
		}
		return img;
		
	}

	public String getColumnText(Object element, int columnIndex) {
		String text="";
		switch (columnIndex){
		case 0:text=((File)element).getName();
			   if(text==null || text.length()==0){
				   	text=((File)element).getPath();
			   	}
			break;
		case 1:
			if( ((File)element).isDirectory()){
				text= "File Folder";
			}
			else{
				String[] ext=((File)element).getName().split("\\.");
				if(ext.length>0){
					String s=ext[ext.length-1];
					if(s.length()>3){
					try{
					text="BRUKER File";
					}catch(NumberFormatException n){
						text= s.toUpperCase()  + " File";
					}
					}else{
						text= s.toUpperCase()  + " File";
					}
					
				}
				}
			break;
		case 2:
			Date dte = new Date(((File)element).lastModified());
			text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(dte);
			break;
		default:
			break;
			
		}
		
		return text;
	}

	public void addListener(ILabelProviderListener listener) {
		_listener.add(listener);

	}

	public void dispose() {
		if(_dir!=null){
			_dir.dispose();}
		if(_file!=null){
			_file.dispose();}
		if(_esrf!=null){
			_esrf.dispose();}
		if(_sample!=null){
			_sample.dispose();}
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		_listener.remove(listener);

	}

}
