/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package fable.python.jep;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jep.Jep;
import jep.JepException;

public class JavaToJepTools {

	/**
	 * 
	 * @param j
	 *            a jep object
	 * @param map
	 *            a hashmap, keys and value This function is used in
	 *            fable.transform.Transform.java class to update parameters
	 *            values with a dictionary It creates a dictionay object, used
	 *            as parameter is a python code
	 *            (j.eval("myObj.update(dictionary)")
	 * @throws JepException
	 */
	public static void javaHashMapToPyDictionary(Jep j,
			HashMap<String, Object> map) throws JepException {
		Set<Entry<String, Object>> entryS = map.entrySet();

		try {
			j.eval("dictionary={}");

			Iterator<Entry<String, Object>> it = entryS.iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> e = it.next();
				j.eval("a={'" + e.getKey() + "' : '" + e.getValue() + "'}");
				j.eval("dictionary.update(a)");
			}

		} catch (JepException e1) {

			throw e1;
		}
	}

	/**
	 * 
	 * @param j
	 *            a jep object
	 * @param Object
	 *            [] a tab of object This function is used in
	 *            fable.transform.Transform.java class to update vary list
	 *            values with a list It creates a list object, used as parameter
	 *            is a python code (j.eval("myObject.set_list(list)"))
	 * @throws JepException
	 */
	public static void javaTabToPythonList(Jep j, Object[] tabToConvert)
			throws JepException {

		try {
			j.eval("list=[]");

			for (int i = 0; i < tabToConvert.length; i++) {

				j.eval("list.append('" + tabToConvert[i] + "')");
			}

		} catch (JepException e1) {

			throw e1;
		}
	}
}
