/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.central.reddeer.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.reddeer.core.util.FileUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author zcervink@redhat.com
 *
 */

public class CentralUtils {

	/**
	 * Reads and returns content of the blacklist errors file
	 *
	 * @return JSONObject content of the blacklist errors file
	 */
	// https://www.mkyong.com/java/json-simple-example-read-and-write-json
	public static JSONObject loadBlacklistErrorsFile(String blacklistErrorsFile) {
		JSONObject blacklistErrorsFileContents = null;

		if (!blacklistErrorsFile.isEmpty()) {
			String pathToFile = "";
			try {
				pathToFile = new File(blacklistErrorsFile).getCanonicalPath();
				JSONParser parser = new JSONParser();
				blacklistErrorsFileContents = (JSONObject) parser.parse(new FileReader(pathToFile));
			} catch (IOException ex) {
				fail("Blacklist file not found! Path is: " + pathToFile);
			} catch (ParseException e) {
				fail("ParseException: unable to parse file at path is: " + pathToFile);
			}
		}

		return blacklistErrorsFileContents;
	}

	/**
	 * Reads and returns content of the blacklist file
	 *
	 * @return String content of the blacklist file
	 */
	public static String loadBlacklistFile(String blacklistFile) {
		String pathToFile = "";
		String blacklistFileContents = "";

		try {
			pathToFile = new File(blacklistFile).getCanonicalPath();
			blacklistFileContents = FileUtil.readFile(pathToFile);
		} catch (IOException ex) {
			fail("Blacklist file not found! Path is: " + pathToFile);
		}

		return blacklistFileContents;
	}

}
