/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.framework.logging.test;

import org.slf4j.Logger;
import org.junit.Test;

import fable.framework.logging.FableLogger;

public class FableLoggerTest {

	@Test
	public void testGetLogger() {
		Logger logger = FableLogger.getLogger();
		logger.info("Info test fable logger");
		logger.warn("Warning test fable logger");
		logger.error("Error test fable logger");
	}

	@Test
	public void testGetLoggerClass() {
		Logger logger = FableLogger.getLogger(FableLoggerTest.class);
		logger.info("Info test fable class logger");
		logger.warn("Warning test fable class logger");
		logger.error("Error test fable class logger");
	}

}
