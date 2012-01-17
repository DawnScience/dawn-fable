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
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Suchet
 * @date March 28, 2007
 * @description This class provides the content for the tree in FileTree
 * */
public class FileTreeContentProvider implements ITreeContentProvider {

	private boolean root_visible = false;

	public Object[] getChildren(Object parentElement) {

		Comparator<File> alphabeticalComparator = new AlphabeticalComparator();

		if (!root_visible) {
			root_visible = true;

			return File.listRoots();
		} else {
			File[] file_list = ((File) parentElement)
					.listFiles(new FileFilter() {

						public boolean accept(File pathname) {

							return !pathname.isHidden();
						}
					});
			if (file_list != null) {
				Arrays.sort(file_list, alphabeticalComparator);
				// for (File oneEntry : file_list) {
				// System.out.println("FileTreeContentProvider:getChildren(): file_list[]  "
				// + oneEntry.getName());
				// }
			}

			return file_list;
		}

	}

	public Object getParent(Object element) {

		return ((File) element).getParentFile();
	}

	public boolean hasChildren(Object element) {
		boolean haschildren = false;
		Object[] obj = getChildren(element);
		haschildren = obj == null ? false : obj.length > 0;
		return haschildren;
	}

	public Object[] getElements(Object inputElement) {

		return getChildren(inputElement);

	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	// ////////////////////////////////////////////////DirAlphaComparator
	// To sort directories before files, then alphabetically.
	class AlphabeticalComparator implements Comparator<File> {

		// Comparator interface requires defining compare method.
		public int compare(File filea, File fileb) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			return filea.getName().compareToIgnoreCase(fileb.getName());
		}
	}

}
