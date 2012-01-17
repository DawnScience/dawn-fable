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

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * TreeWithAddRemove manages a tree of file system items, either folders or
 * files. It is similar to the one used in PyDev for the PYTHONPATH.
 * 
 * @author evans
 * 
 */
public class TreeWithAddRemove extends Composite {
	private static String lastDirectoryDialogPath = null;
	private static String lastFileDialogPath = null;
	private Tree tree;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The Composite parent.
	 * @param style
	 *            Passed to the Composite parent.
	 * @param initialItems
	 *            The items to be displayed initially given as a single string
	 *            separated with the path separator as returned by
	 *            System.getProperty("path.separator").
	 */
	public TreeWithAddRemove(Composite parent, int style, String initialItems) {
		super(parent, style);
		if (initialItems == null) {
			initialItems = "";
		}
		final Shell shell = parent.getShell();
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		this.setLayout(layout);

		tree = new Tree(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);
		resetTreeItems(initialItems);

		// Make a composite for the buttons
		Composite composite = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
				.applyTo(composite);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Add Source Folder");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
				false).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setFilterPath(lastDirectoryDialogPath);
				String filePath = dialog.open();
				if (filePath != null) {
					lastDirectoryDialogPath = filePath;
				}
				addTreeItem(filePath);
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setText("Add ZIP/JAR/EGG");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
				false).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell);
				dialog.setFilterPath(lastFileDialogPath);
				dialog.open();
				String[] fileNames = dialog.getFileNames();
				if (fileNames != null && fileNames.length > 0) {
					lastFileDialogPath = dialog.getFilterPath();
					StringBuffer buf;
					for (String fileName : fileNames) {
						buf = new StringBuffer(lastFileDialogPath);
						if (buf.charAt(buf.length() - 1) != File.separatorChar) {
							buf.append(File.separatorChar);
						}
						buf.append(fileName);
						addTreeItem(buf.toString());
					}
				}
			}
		});

		button = new Button(composite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
				false).applyTo(button);
		button.setText("Remove");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] selection = tree.getSelection();
				for (int i = 0; i < selection.length; i++) {
					selection[i].dispose();
				}
			}
		});
	}

	private static String[] stringToStringArray(String items) {
		String ps = System.getProperty("path.separator", "|");
		return items.split(ps);
	}

	/**
	 * Add an item to the tree.
	 * 
	 * @param AsString
	 */
	private void addTreeItem(String AsString) {
		if (AsString != null && AsString.trim().length() > 0) {
			TreeItem item = new TreeItem(tree, 0);
			item.setText(AsString);
			File file = new File(AsString);
			if (file.isDirectory()) {
				item.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJ_FOLDER));
			} else if (file.isFile()) {
				item.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJ_FILE));
			} else {
				item.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
			}
		}
	}

	/**
	 * Get the tree items as a single string, separated by the path separator.
	 * 
	 * @return
	 */
	public String getTreeItemsAsString() {
		String ps = System.getProperty("path.separator", "|");
		StringBuffer ret = new StringBuffer();
		TreeItem[] items = tree.getItems();
		for (int i = 0; i < items.length; i++) {
			String text = items[i].getText();
			if (text != null && text.trim().length() > 0) {
				if (ret.length() > 0) {
					ret.append(ps);
				}
				ret.append(text);
			}
		}
		return ret.toString();
	}

	/**
	 * Reset the tree items using the given new items.
	 * 
	 * @param items
	 *            The items to be displayed given as a single string separated
	 *            with the path separator as returned by
	 *            System.getProperty("path.separator").
	 */
	public void resetTreeItems(String items) {
		tree.removeAll();
		String[] arrayItems = stringToStringArray(items);
		for (String arrayItem : arrayItems) {
			addTreeItem(arrayItem);
		}
	}

}
