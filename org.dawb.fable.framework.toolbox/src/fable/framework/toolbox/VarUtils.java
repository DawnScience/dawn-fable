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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

import fable.framework.internal.IVarKeys;

public class VarUtils {

	private static final Collection<String> IMAGES;
	static {
		Collection<String> set = new HashSet<String>(31);
		set.add(IVarKeys.TIFFORMAT);
		set.add(IVarKeys.TIFFORMATBz2);
		set.add(IVarKeys.TIFFORMATGZip);
		set.add(IVarKeys.TIFF_FORMAT);
		set.add(IVarKeys.TIFF_FORMATBz2);
		set.add(IVarKeys.TIFF_FORMATGZip);
		set.add(IVarKeys.ADSCFORMAT);
		set.add(IVarKeys.ADSCFORMATBz2);
		set.add(IVarKeys.ADSCFORMATGZip);
		set.add(IVarKeys.EDFFORMAT);
		set.add(IVarKeys.EDFFORMATBz2);
		set.add(IVarKeys.EDFFORMATGZip);
		set.add(IVarKeys.CORFORMAT);
		set.add(IVarKeys.CORFORMATBz2);
		set.add(IVarKeys.CORFORMATGZip);
		set.add(IVarKeys.BRUKERFORMAT);
		set.add(IVarKeys.BRUKERFORMATBz2);
		set.add(IVarKeys.MCCDFORMAT);
		set.add(IVarKeys.MCCDFORMATBz2);
		set.add(IVarKeys.MCCDFORMATGZip);
		set.add(IVarKeys.MAR2300FORMAT);
		set.add(IVarKeys.MAR2300FORMATBz2);
		set.add(IVarKeys.mar2300FormatGZip);
		set.add(IVarKeys.CCDFORMAT);
		set.add(IVarKeys.PNMFORMAT);
		set.add(IVarKeys.PGMFORMAT);
		set.add(IVarKeys.PBMFORMAT);
		set.add("jpg");
		set.add("jpeg");
		set.add("png");
		set.add("gif");
		set.add("cbf");
		set.add("cbf.bz2");
		
		IMAGES = Collections.unmodifiableCollection(set);
	}
	
	public static final boolean isImage(final String fileName) {
		int posExt = fileName.lastIndexOf(".");
		String ext = posExt < 0 ? fileName : fileName.substring(posExt+1);
        if (IMAGES.contains(ext.toLowerCase())) return true;
        
        try {
	        posExt = fileName.lastIndexOf('.', posExt-1);
	        ext = posExt < 0 ? fileName : fileName.substring(posExt+1);
	        return IMAGES.contains(ext.toLowerCase());
        } catch (Throwable ne) {
        	return false;
        }
	}
	
	/**
	 * Return true if adsc or compressed adsc - img
	 * @param path
	 * @return
	 */
	public static boolean isTiff(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(IVarKeys.TIFFORMAT))       return true;
		if (path.toLowerCase().endsWith(IVarKeys.TIFFORMATGZip))   return true;
		if (path.toLowerCase().endsWith(IVarKeys.TIFFORMATBz2))    return true;
		if (path.toLowerCase().endsWith(IVarKeys.TIFF_FORMAT))     return true;
		if (path.toLowerCase().endsWith(IVarKeys.TIFF_FORMATGZip)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.TIFF_FORMATBz2))  return true;
		return false;
	}
	/**
	 * Return true if adsc or compressed adsc - img
	 * @param path
	 * @return
	 */
	public static boolean isImg(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(IVarKeys.ADSCFORMAT)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.ADSCFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.ADSCFORMATBz2)) return true;
		return false;
	}
	/**
	 * Return true if edf or compressed edf
	 * @param path
	 * @return
	 */
	public static boolean isEdf(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(IVarKeys.EDFFORMAT)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.EDFFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.EDFFORMATBz2)) return true;
		return false;
	}
	
	/**
	 * Return true if cor or compressed cor
	 * @param path
	 * @return
	 */
	public static boolean isCor(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(IVarKeys.CORFORMAT)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.CORFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(IVarKeys.CORFORMATBz2)) return true;
		return false;
	}
	
	/**
	 * Return true if cor or compressed cor
	 * @param path
	 * @return
	 */
	public static boolean isBruker(final String path) {
		if (path==null) return false;
		if (Pattern.compile(".*"+IVarKeys.BRUKERFORMAT).matcher(path).matches())    return true;
		if (Pattern.compile(".*"+IVarKeys.BRUKERFORMATBz2).matcher(path).matches()) return true;
		return false;
	}

}
