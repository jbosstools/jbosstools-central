/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TokenizerUtil {

	private TokenizerUtil() {}
	
	public static List<String> splitToList(String valueString) {
		if (valueString == null) {
			return null;
		}
		ArrayList<String> result = new ArrayList<>();
		splitToCollection(result, valueString);
		return result;
	}
	
	public static Set<String> splitToSet(String valueString) {
		if (valueString == null) {
			return null;
		}
		Set<String> result = new LinkedHashSet<>();
		splitToCollection(result, valueString);
		return result;
	}
	
	private static void splitToCollection(Collection<String> collection, String valueString) {
		String[] splitValues = valueString.split(",");//$NON-NLS-1$
		for (String value : splitValues) {
			value = value.trim();
			if (!value.isEmpty()) {
				collection.add(value);
			}
		}
	}
}
