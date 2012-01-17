/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.imageviewer.component;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import fable.framework.navigator.controller.SampleController;
import fable.framework.navigator.views.SampleNavigatorView;
import fable.framework.toolbox.FableUtils;

/**
 * ImagePlay implements a view to step through images and/or play images in a
 * loop. The play controls view does not allow multiple views. The view can be
 * detached for ease of use.
 * 
 * @author Andy Gotz (ESRF)
 * 
 */
public class ImagePlay extends ViewPart {

	public static final String ID = "fable.imageviewer.views.ImagePlay";
	private Button imageNextButton, imagePreviousButton;
	private Button imageFirstButton, imageLastButton;
	private Button imagePauseButton;
	/**
	 * Flag indicating whether it is playing or paused.
	 */
	private boolean advancePlay = false;
	private int currentFileIndex = 0, playStep = 1, playWait = 3000;
	private Thread playThread = null;
	private static ImageComponent iv = null;

	public ImagePlay() {
	}

	@Override
	public void createPartControl(Composite parent) {
		int buttonAlignH = SWT.FILL;
		int buttonAlignV = SWT.FILL;
		boolean buttonGrabH = true;
		boolean buttonGrabV = false;
		GridLayout parentField = new GridLayout();
		parentField.numColumns = 5;
		parentField.makeColumnsEqualWidth = true;
		parent.setLayout(parentField);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		imageFirstButton = new Button(parent, SWT.PUSH);
		imageFirstButton.setText("<<");
		imageFirstButton.setLayoutData(new GridData(buttonAlignH, buttonAlignV,
				buttonGrabH, buttonGrabV));
		imageFirstButton.setToolTipText("Go to first image");
		imageFirstButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (isAdvancePlay()) {
					startPlay(-1, 300);
				} else {
					if (iv == null) {
						FableUtils.errMsg(this,
								"ImagePlay: Unable to find a main ImageView");
						return;
					}
					iv.getController().setCurrentFileIndex(0);

					iv.getController().getFirstImage();
				}
			}
		});
		imagePreviousButton = new Button(parent, SWT.PUSH);
		imagePreviousButton.setText("<");
		imagePreviousButton.setLayoutData(new GridData(buttonAlignH,
				buttonAlignV, buttonGrabH, buttonGrabV));
		imagePreviousButton.setToolTipText("Go to previous image");
		imagePreviousButton.setEnabled(true);
		imagePreviousButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (isAdvancePlay()) {
					startPlay(-1, 3000);
				} else {
					if (iv == null) {
						FableUtils.errMsg(this,
								"ImagePlay: Unable to find a main ImageView");
						return;
					}
					int fileIndex = iv.getController().getCurrentFileIndex() - 1;
					/* if first image then wrap around to last image */
					if (fileIndex >= 0) {
						iv.getController().setCurrentFileIndex(fileIndex);
					} else {
						if (iv.getController().getCurrentsample() != null) {
							iv.getController().setCurrentFileIndex(
									iv.getController().getCurrentsample()
											.getFilteredfiles().size() - 1);
						}
						iv.getController().getNext(-1);
					}
				}
			}
		});
		imagePauseButton = new Button(parent, SWT.PUSH);
		imagePauseButton.setText("Play");
		imagePauseButton.setLayoutData(new GridData(buttonAlignH, buttonAlignV,
				buttonGrabH, buttonGrabV));
		imagePauseButton.setToolTipText("Play");
		imagePauseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAdvancePlay(!isAdvancePlay());
				if (isAdvancePlay()) {
					startPlay(1, 3000);
					imagePauseButton.setText("||");
					imagePauseButton.setToolTipText("Pause");
				} else {
					imagePauseButton.setText("Play");
					imagePauseButton.setToolTipText("Play");
				}
			}
		});
		imageNextButton = new Button(parent, SWT.PUSH);
		imageNextButton.setText(">");
		imageNextButton.setLayoutData(new GridData(buttonAlignH, buttonAlignV,
				buttonGrabH, buttonGrabV));
		imageNextButton.setToolTipText("Go to next image");
		imageNextButton.setEnabled(true);
		imageNextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// widgetDefaultSelected is not called, so do nothing.
			}

			public void widgetSelected(SelectionEvent e) {
				if (isAdvancePlay()) {
					startPlay(1, 3000);
				} else {
					if (iv == null) {
						FableUtils.errMsg(this,
								"ImagePlay: Unable to find a main ImageView");
						return;
					}
					int fileIndex = iv.getController().getCurrentFileIndex() + 1;
					/* if last image then wrap around to first image */
					if (iv.getController().getCurrentsample() != null) {
						if (fileIndex >= iv.getController().getCurrentsample()
								.getFilteredfiles().size())
							fileIndex = 0;
						iv.getController().setCurrentFileIndex(fileIndex);
					}
					iv.getController().getNext(1);
				}
			}
		});
		imageLastButton = new Button(parent, SWT.PUSH);
		imageLastButton.setText(">>");
		imageLastButton.setLayoutData(new GridData(buttonAlignH, buttonAlignV,
				buttonGrabH, buttonGrabV));
		imageLastButton.setToolTipText("Go to last image");
		imageLastButton.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// widgetDefaultSelected is not called, so do nothing.
			}

			public void widgetSelected(SelectionEvent e) {
				if (isAdvancePlay()) {
					startPlay(1, 300);
				} else {
					if (iv == null) {
						FableUtils.errMsg(this,
								"ImagePlay: Unable to find a main ImageView");
						return;
					}
					if (iv.getController().getCurrentsample() != null) {
						int lastFileIndex = iv.getController()
								.getCurrentsample().getFilteredfiles().size();
						iv.getController().setCurrentFileIndex(
								lastFileIndex - 1);
					}
					iv.getController().getLastImage();
				}
			}
		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the advancePlay
	 */
	public boolean isAdvancePlay() {
		return advancePlay;
	}

	/**
	 * @param advancePlay
	 *            the advancePlay to set
	 */
	public void setAdvancePlay(boolean advancePlay) {
		this.advancePlay = advancePlay;
	}

	public void startPlay(int step, int wait) {
		playStep = step;
		playWait = wait;
		if (playThread == null) {
			playThread = PlayThread();
			playThread.start();
		}
	}

	/**
	 * This method will return a thread to advance the file index in play mode.
	 * It should be called by the play buttons back and forward. The thread is
	 * ready to be run with start(). The thread will stop when the play flag is
	 * set to false.
	 * 
	 * @return play thread
	 */
	public Thread PlayThread() {
		return new Thread() {
			public void run() {
				if (iv == null) {
					FableUtils.errMsg(this,
							"PlayThread: Unable to find a main ImageView");
					return;
				}
				SampleController controller = iv.getController();
				Vector<Integer> selectedFiles = SampleNavigatorView.view
						.getSelectedFilesIndex();
				int selectedFilesIndex = 0;
				if (selectedFiles.size() <= 1) {
					currentFileIndex = controller.getCurrentFileIndex();
				} else {
					currentFileIndex = selectedFiles.firstElement();
				}
				while (advancePlay) {
					try {
						if (selectedFiles.size() <= 1) {
							currentFileIndex += playStep;
						} else {
							selectedFilesIndex += playStep;
							if (selectedFilesIndex >= selectedFiles.size())
								selectedFilesIndex = 0;
							currentFileIndex = selectedFiles
									.elementAt(selectedFilesIndex);
						}
						/** Added by Gaelle for 2d peaks file. */
						controller.getNext(playStep);
						/***/
						if (controller.getCurrentsample() != null) {
							if (currentFileIndex >= controller
									.getCurrentsample().getFilteredfiles()
									.size()) {
								currentFileIndex = 0;
							} else if (currentFileIndex < 0) {
								currentFileIndex = controller
										.getCurrentsample().getFilteredfiles()
										.size() - 1;
							}

							/*
							 * why does the controller need to be in the SWT
							 * thread to set the sample file index ?
							 */
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									iv.getController().setCurrentFileIndex(
											currentFileIndex);
								}
							});
						}
						Thread.sleep(playWait);
					} catch (InterruptedException ex) {
						// KE: Is this an unexpected occurrence. If not, better
						// to use a SWTUtils dialog.
						FableUtils.excTraceMsg(this, "PlayThread Interrupted",
								ex);
					}
				}
				playThread = null;
			}
		};
	}

	/**
	 * Set the view to which the play controls will apply (usually the main
	 * image view)
	 * 
	 * @param view
	 *            - ImageView to attach play controls to
	 */
	public static void setView(ImageComponent view) {
		iv = view;
	}

}
