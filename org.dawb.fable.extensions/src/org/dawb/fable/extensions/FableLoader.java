/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.fable.extensions;

import java.io.File;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import fable.framework.navigator.Activator;
import fable.framework.navigator.preferences.FabioPreferenceConstants;
import fable.imageviewer.model.IFableImage;
import fable.imageviewer.model.IFableLoader;

public class FableLoader implements IFableLoader {

    private static final Logger logger = LoggerFactory.getLogger(FableLoader.class);
	@Override
	public IFableImage loadFile(final String           path, 
			                    final String           name,
			                    final boolean          isFabioConfigured,
			                    final IProgressMonitor monitor) throws Exception {
		
		if (isFabioConfigured) {
			final String endings = Activator.getDefault().getPreferenceStore().getString(FabioPreferenceConstants.FILE_TYPE);
			String[] split = endings.split("\\|");

            for (int i = 0; i < split.length; i++) {
				if (path.endsWith("."+split[i])) return null;
			}
		}
		
		IDataset set = null;
		
		final long start = System.currentTimeMillis();
		final IMonitor mon = monitor!= null ? new ProgressMonitorWrapper(monitor) : null;
		
		try {
			if (name == null) {
				final IDataHolder dh = LoaderFactory.getData(path, mon);
				set = dh.getDataset(0);
			} else {
				
			    set = LoaderFactory.getDataSet(path, name, mon);
			    if (set==null) {
			    	final IDataHolder dh = LoaderFactory.getData(path, mon);
			    	set = dh.getDataset(name);
			    }
			
			}
		} catch (NullPointerException ne) {
			return null;
			
		} catch (Exception ne) {
			logger.error("Cannot load "+path+" with GDA classes, trying to fabio.", ne);
			return null;
		}
		
		return new FableImageWrapper((new File(path)).getName(), set, System.currentTimeMillis()-start);
	}

}
