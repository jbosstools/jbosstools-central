/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizard;
import org.jboss.tools.central.model.FeedsEntry;
import org.jboss.tools.project.examples.model.ProjectExample;

public class JsonUtil {

	private JsonUtil() {}
	
	public static String jsonifyWizards(Collection<ProxyWizard> proxyWizards) {
		StringBuilder json = new StringBuilder("[");
		if (proxyWizards != null) {
			boolean addComma = false;
			for (ProxyWizard pw : proxyWizards) {
				if (addComma) {
					json.append(",");
				}
				addComma = true;
				json.append("{");
				append("id", pw.getId(), json);
				append("label", pw.getLabel(), json);
				append("description", pw.getDescription(), json);
				String localIconUrl = "";
				try {
					localIconUrl = ImageUtil.getImageAsLocalUrl(pw.getIconUrl().toString());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				append("iconUrl", localIconUrl, json);
				json.append("}");
			}
		}
		json.append("]");
		return json.toString();
	}
	
	public static String jsonifyBuzz(Collection<FeedsEntry> buzzFeed) {
		StringBuilder json = new StringBuilder("[");
		if (buzzFeed != null) {
			boolean addComma = false;
			for (FeedsEntry feed : buzzFeed) {
				if (addComma) {
					json.append(",");
				}
				addComma = true;
				json.append("{");
				append("link", feed.getLink(), json);
				append("description", feed.getTitle(), json);
				json.append("}");
			}
		}
		json.append("]");
		return json.toString();
	}
	

	private static void append(String key, Object value, StringBuilder json) {
		quote(key, json).append(":");
		if (value instanceof Collection) {
			json.append("[");
			boolean addComma = false;
			for (Object o : (Collection<?>)value) {
				if (addComma) {
					json.append(",");
				}
				addComma = true;
				quote(sanitize(o), json);
			}
			json.append("]");
		} else {
			quote(sanitize(value), json).append(",");
		}
	}
	
	private static String sanitize(Object value) {
		return value == null ? "" : value.toString().replace("\n", "\\n");
	}
	
	private static StringBuilder quote(String value, StringBuilder json) {
		String quote = "\"";
		json.append(quote).append(value).append(quote);
		return json;
	}
	
	public static String jsonifyExamples(Collection<ProjectExample> examples) {
		StringBuilder json = new StringBuilder("[");
		if (examples != null) {
			boolean addComma = false;
			for (ProjectExample ex : examples) {
				if (addComma) {
					json.append(",");
				}
				addComma = true;
				json.append("{");
				append("id", ex.getId(), json);
				append("label", ex.getName(), json);
				append("title", ex.getHeadLine(), json);
				append("description", ex.getDescription(), json);
				append("tags", ex.getTags(), json);
				json.append("}");
			}
		}
		json.append("]");
		return json.toString();
	}
	
	public static String jsonify(Collection<String> strings) {
		StringBuilder json = new StringBuilder("[");
		if (strings != null) {
			boolean addComma = false;
			for (String s : strings) {
				if (addComma) {
					json.append(",");
				}
				addComma = true;
				quote(s, json);
			}
		}
		json.append("]");
		return json.toString();
	}
}
