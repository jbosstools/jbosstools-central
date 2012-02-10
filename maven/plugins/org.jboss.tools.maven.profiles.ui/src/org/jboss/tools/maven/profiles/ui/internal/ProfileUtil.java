/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.profiles.ui.internal;

import java.util.Collection;

public class ProfileUtil {

	private ProfileUtil(){} 
	
	private static final String COMMA = ", "; 
	
	public static String toString(Collection<ProfileSelection> profiles) {
		StringBuilder sb = new StringBuilder();
		if(profiles != null && !profiles.isEmpty()) {
			boolean addComma = false;
			for (ProfileSelection ps : profiles) {
				if (ps !=null && Boolean.TRUE.equals(ps.getSelected())) {
					if (addComma) {
						sb.append(COMMA);
					}
					sb.append(ps.toMavenString());
					addComma = true;
				}
			}
		}
		return sb.toString();
	}
	
}
