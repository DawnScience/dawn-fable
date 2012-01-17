/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/*
 * Program to provide SWT utilities
 * Created on May 12, 2006
 * By Kenneth Evans, Jr.
 */

package fable.framework.toolbox;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.rcp.texteditor.editors.PathEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class SWTUtils {
	public static final String LS = System.getProperty("line.separator");

	/**
	 * Generates a timestamp.
	 * 
	 * @return String timestamp with the current time
	 */
	public static String timeStamp() {
		Date now = new Date();
		final SimpleDateFormat defaultFormatter = new SimpleDateFormat(
				"MMM dd, yyyy HH:mm:ss.SSS");
		return defaultFormatter.format(now);
	}

	/**
	 * Generates a timestamp given a pattern
	 * 
	 * @param pattern
	 *            appropriate for SimpleDateFormat
	 * @return String timestamp with the current time
	 */
	public static String timeStamp(String pattern) {
		Date now = new Date();
		final SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
		return dateFormatter.format(now);
	}

	/**
	 * Displays an error MessageDialog. Same as errMsg(null, msg).
	 * 
	 * @param msg
	 */
	public static void errMsg(String msg) {
		errMsg(null, msg);
	}

	/**
	 * Displays an error MessageDialog.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void errMsg(final Shell shell, final String msg) {
		MessageDialog.openError(shell, "Error", msg);
	}

	/**
	 * Same as errMsgAsync(null, msg). Displays an error MessageDialog using
	 * asyncExec.
	 * 
	 * @param msg
	 */
	public static void errMsgAsync(String msg) {
		errMsgAsync(null, msg);
	}

	/**
	 * Displays an error MessageDialog using asyncExec.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void errMsgAsync(final Shell shell, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, "Error", msg);
			}
		});
	}

	/**
	 * Displays a warning MessageDialog. Same as warnMsg(null, msg).
	 * 
	 * @param msg
	 */
	public static void warnMsg(String msg) {
		warnMsg(null, msg);
	}

	/**
	 * Displays a warning MessageDialog.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void warnMsg(final Shell shell, final String msg) {
		MessageDialog.openWarning(shell, "Warning", msg);
	}

	/**
	 * Displays a warning MessageDialog using asyncExec. Same as
	 * warnMsgAsync(null, msg).
	 * 
	 * @param msg
	 */
	public static void warnMsgAsync(String msg) {
		warnMsgAsync(null, msg);
	}

	/**
	 * Displays a warning MessageDialog using asyncExec.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void warnMsgAsync(final Shell shell, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(shell, "Warning", msg);
			}
		});
	}

	/**
	 * Displays an information MessageDialog. Same as infoMsg(null, msg).
	 * 
	 * @param msg
	 */
	public static void infoMsg(String msg) {
		infoMsg(null, msg);
	}

	/**
	 * Displays an information MessageDialog.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void infoMsg(final Shell shell, final String msg) {
		MessageDialog.openInformation(shell, "Information", msg);
	}

	/**
	 * Same as infoMsgAsync(null, msg). Displays an information MessageDialog
	 * using asyncExec.
	 * 
	 * @param msg
	 */
	public static void infoMsgAsync(String msg) {
		infoMsgAsync(null, msg);
	}

	/**
	 * Displays an information MessageDialog using asyncExec.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 */
	public static void infoMsgAsync(final Shell shell, final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, "Information", msg);
			}
		});
	}

	/**
	 * Displays an exception MessageDialog. Same as excMsg(null, msg, ex).
	 * 
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excMsg(String msg, Exception ex) {
		excMsg(null, msg, ex);
	}

	/**
	 * Displays an exception MessageDialog.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excMsg(final Shell shell, final String msg, Exception ex) {
		String fullMsg = msg + LS + LS + "Exception: " + ex + LS + "Message: "
				+ ex.getMessage();
		MessageDialog.openError(shell, "Exception", fullMsg);
	}

	/**
	 * Displays an exception MessageDialog using asyncExec. Same as
	 * excMsgAsync(null, msg, ex).
	 * 
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excMsgAsync(String msg, Exception ex) {
		excMsgAsync(null, msg, ex);
	}

	/**
	 * Displays an exception MessageDialog using asyncExec.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excMsgAsync(final Shell shell, final String msg,
			Exception ex) {
		final String fullMsg = msg + LS + LS + "Exception: " + ex + LS
				+ "Message: " + ex.getMessage();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, "Exception", fullMsg);
			}
		});
	}

	/**
	 * Displays an ExceptionMessageDialog. Same as excTraceMsg(null, msg, ex).
	 * 
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excTraceMsg(String msg, Exception ex) {
		excTraceMsg(null, msg, ex);
	}

	/**
	 * Displays an exception ExceptionMessageDialog.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excTraceMsg(final Shell shell, final String msg,
			Exception ex) {
		ExceptionMessageDialog.openException(shell, "Exception", msg, ex);
	}

	/**
	 * Displays an ExceptionMessageDialog using asyncExec. Same as
	 * excTraceMsgAsync(null, msg, ex).
	 * 
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excTraceMsgAsync(String msg, Exception ex) {
		excTraceMsgAsync(null, msg, ex);
	}

	/**
	 * Displays an ExceptionMessageDialog using asyncExec.
	 * 
	 * @param shell
	 *            Can be null.
	 * @param msg
	 *            The first part of the message, to which exception information
	 *            is added.
	 * @param ex
	 */
	public static void excTraceMsgAsync(final Shell shell, final String msg,
			final Exception ex) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ExceptionMessageDialog.openException(shell, "Exception", msg,
						ex);
			}
		});
	}

	/**
	 * If the SWT Text DELIMITER is CRLF, then it converts LF not preceeded by
	 * CR to DELIMITER. Otherwise it just returns the string. This is necessary
	 * for mixed CRLF and LF to appear as new lines in Text. The Swing TextArea
	 * does not have this problem.
	 * 
	 * @param string
	 *            The string to convert.
	 * @return
	 */
	public static String convertText(String string) {
		String delimiter = Text.DELIMITER;
		if (delimiter.equals(LS))
			return string;
		String out = "";
		int lf = 0xa;
		int cr = 0xd;
		int prev = 0;
		int len = string.length();
		for (int i = 0; i < len; i++) {
			char chr = string.charAt(i);
			int val = (int) chr;
			if (val != lf) {
				// Regular character
				out += chr;
			} else {
				// Is a LF
				if (i > 0) {
					prev = (int) string.charAt(i - 1);
					if (prev == cr) {
						// Previous was a CR, add the LF
						out += chr;
					} else {
						// LF without preceding CR, replace with delimiter
						// (CRLF)
						out += delimiter;
					}
				} else {
					// LF at start of line (no preceding CR) replace with
					// delimiter (CRLF)
					out += delimiter;
				}
			}
		}
		return out;
	}

	/**
	 * Creates a file resource given the file handle and contents. From
	 * org.eclipse.ui.dialogs.WizardnewFileCreationPage.
	 * 
	 * @param fileHandle
	 *            the file handle to create a file resource with
	 * @param contents
	 *            the initial contents of the new file resource, or
	 *            <code>null</code> if none (equivalent to an empty stream)
	 * @param monitor
	 *            the progress monitor to show visual progress with
	 * @exception CoreException
	 *                if the operation fails
	 * @exception OperationCanceledException
	 *                if the operation is canceled
	 */
	public static void createFileFromIFile(IFile fileHandle,
			InputStream contents, IProgressMonitor monitor)
			throws CoreException {
		if (contents == null) {
			contents = new ByteArrayInputStream(new byte[0]);
		}

		try {
			// Create a new file resource in the workspace
			IPath path = fileHandle.getFullPath();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			int numSegments = path.segmentCount();
			if (numSegments > 2
					&& !root.getFolder(path.removeLastSegments(1)).exists()) {
				// If the direct parent of the path doesn't exist, try to create
				// the
				// necessary directories.
				for (int i = numSegments - 2; i > 0; i--) {
					IFolder folder = root.getFolder(path.removeLastSegments(i));
					if (!folder.exists()) {
						folder.create(false, true, monitor);
					}
				}
			}
			fileHandle.create(contents, false, monitor);
		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			} else {
				throw e;
			}
		}

		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Returns the IPath for a variety of IEditorInput's. From the IMP project,
	 * but modified. Note that given the IPath, you can find the fileName and
	 * other information by using IPath.toFile().
	 * 
	 * @return the IPath corresponding to the given input, or null if none.
	 */
	public static IPath getPath(IEditorInput editorInput) {
		IPath path = null;

		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			path = fileEditorInput.getFile().getLocation();
		} else if (editorInput instanceof IPathEditorInput) {
			IPathEditorInput pathInput = (IPathEditorInput) editorInput;
			path = pathInput.getPath();
		} else if (editorInput instanceof IStorageEditorInput) {
			IStorageEditorInput storageEditorInput = (IStorageEditorInput) editorInput;
			try {
				// Can be null
				path = storageEditorInput.getStorage().getFullPath();
			} catch (CoreException ex) {
				// do nothing; return null;
			}
		} else if (editorInput instanceof FileStoreEditorInput) {
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) editorInput;
			path = new Path(fileStoreEditorInput.getURI().getPath());
		}
		return path;
	}

	/**
	 * From the IMP project.
	 * 
	 * @return the IFile corresponding to the given input, or null if none.
	 */
	public static IFile getFile(IEditorInput editorInput) {
		IFile file = null;

		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			file = fileEditorInput.getFile();
		} else if (editorInput instanceof IPathEditorInput) {
			IPathEditorInput pathInput = (IPathEditorInput) editorInput;
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

			if (wsRoot.getLocation().isPrefixOf(pathInput.getPath())) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(
						pathInput.getPath());
			} else {
				// Can't get an IFile for an arbitrary file on the file system;
				// return null
			}
		} else if (editorInput instanceof IStorageEditorInput) {
			// IStorageEditorInput storageEditorInput = (IStorageEditorInput)
			// editorInput;
			// Can't get an IFile for an arbitrary IStorageEditorInput
			file = null;
		} else if (editorInput instanceof FileStoreEditorInput) {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) editorInput;
			URI uri = fileStoreEditorInput.getURI();
			String path = uri.getPath();
			// Bug 526: uri.getHost() can be null for a local file URL
			if (uri.getScheme().equals("file")
					&& (uri.getHost() == null || uri.getHost().equals(
							"localhost"))
					&& path.startsWith(wsRoot.getLocation().toOSString())) {
				file = wsRoot.getFile(new Path(path));
			}
		}
		return file;
	}

	/**
	 * @return the name extension (e.g., "java" or "cpp") corresponding to this
	 *         input, if known, or the empty string if none. Does not include a
	 *         leading ".". From the IMP project.
	 */
	public static String getNameExtension(IEditorInput editorInput) {
		return getPath(editorInput).getFileExtension();
	}

	/**
	 * Recursively enables or disables this control and all its children.
	 * 
	 * @param control
	 *            Control to be enabled or disabled.
	 * @param enabled
	 *            Whether to enable or disable.
	 */
	public static void enableControlTree(Control control, boolean enabled) {
		if (control == null)
			return;
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] controls = composite.getChildren();
			for (Control control1 : controls) {
				enableControlTree(control1, enabled);
			}
		}
		control.setEnabled(enabled);
	}

	/**
	 * Converts an SWT ImageData to an AWT BufferedImage.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
					palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					bufferedImage.setRGB(x, y, rgb.red << 16 | rgb.green << 8
							| rgb.blue);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * Converts an AWT BufferedImage to an SWT ImageData.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF,
							(rgb >> 8) & 0xFF, rgb & 0xFF));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	/**
	 * Finds the width for a Text control given the number of columns desired.
	 * 
	 * @param text
	 * @param cols
	 * @return
	 */
	public static int getTextWidth(Text text, int cols) {
		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		int width = cols * fm.getAverageCharWidth();
		gc.dispose();
		return width;
	}

	/**
	 * Creates a new IEditorInput from a File. Tries to create a FileEditorInput
	 * if the file is in the workbench, otherwise creates a PathEditorInput.
	 * 
	 * @param file
	 * @return
	 */
	public static IEditorInput createEditorInput(File file) {
		IPath iPath = new Path(file.getAbsolutePath());
		IFile iFile = null;
		IEditorInput input = null;

		// Try a FileEditorInput, resource in the workbench, has persistence
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (root != null && iPath != null) {
			iFile = root.getFileForLocation(iPath);
		}
		if (iFile != null) {
			input = new FileEditorInput(iFile);
		}
		if (input != null) {
			return input;
		}

		// Try a FileStoreInput, resource in the local file system, has
		// persistence
		IFileStore fileStore = null;
		String parentPath = null;
		File parent = file.getParentFile();
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		}
		if (parentPath != null) {
			fileStore = EFS.getLocalFileSystem().getStore(new Path(parentPath));
		}
		if (fileStore != null) {
			fileStore = fileStore.getChild(file.getName());
		}
		if (fileStore != null) {
			input = new FileStoreEditorInput(fileStore);
		}
		if (input != null) {
			return input;
		}

		// Try a pathEditorInput. no persistence
		// KE: This not part of the Eclipse IDE and requires
		// fable.framework.ui.texteditor, where it is implemented. This branch
		// should not be reached, and there seems to be no reason to use a
		// PathEditorInput. It originally did not have persistence but now does.
		// If eliminated, this plug-in does not need the dependency on
		// fable.framework.ui.texteditors.
		input = new PathEditorInput(iPath);
		return input;
	}

	/**
	 * Checks if the file is in the workspace. If so, tries to refresh the
	 * project. Fails silently if this cannot be done.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * @return If [apparently] successful or not
	 */
	public static boolean tryToRefreshProject(String fileName) {
		if (fileName == null) {
			return false;
		}
		Path iPath = new Path(fileName);
		IFile iFile = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (root != null && iPath != null) {
			iFile = root.getFileForLocation(iPath);
		}
		if (iFile != null) {
			// Refresh the project if possible
			try {
				IProject project = iFile.getProject();
				project.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			} catch (Exception ex) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the file is in the workspace. If so tries to open an editor on
	 * it. Fails silently if this cannot be done.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * 
	 * @param editorID
	 *            The ID of the editor to open; e.g.,
	 *            "org.eclipse.ui.DefaultTextEditor"
	 * @return If [apparently] successful or not
	 */
	public static boolean tryToOpenWorkspaceFileEditor(String fileName,
			String editorID) {
		if (fileName == null) {
			return false;
		}
		Path iPath = new Path(fileName);
		IFile iFile = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (root != null && iPath != null) {
			iFile = root.getFileForLocation(iPath);
		}
		if (iFile != null) {
			try {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IFileEditorInput newInput = new FileEditorInput(iFile);
				page.openEditor(newInput, editorID);
			} catch (Exception ex) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Tries to open an editor on the given external file. Fails silently if
	 * this cannot be done.
	 * 
	 * @param fileName
	 *            External file name.
	 * @return If [apparently] successful or not
	 */
	public static boolean tryToOpenExternalFileEditor(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			return false;
		}
		try {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(
					file.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();

			IDE.openEditorOnFileStore(page, fileStore);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

}
