/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.installation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.equinox.p2.metadata.VersionRange;
import org.jboss.tools.central.JBossCentralActivator;

public class BundleFamilyExtension {

	//private String id;
	//private String label;
	private URL defaultListingFileResource;
	private String url;

	public BundleFamilyExtension(String id, String label, URL defaultListingFileResource, String url) {
		//this.id = id;
		//this.label = label;
		this.defaultListingFileResource = defaultListingFileResource;
		this.url = url;
	}

	public Map<String, Set<VersionRange>> loadBundleList() {
		InputStream stream = null;
		if (this.url != null) {
			try {
				URL toLoadURL = new URL(this.url);
				stream = loadStream(toLoadURL);
			} catch (MalformedURLException ex) {
				JBossCentralActivator.log(ex);
			}
		}
		if (stream == null) {
			stream = loadStream(this.defaultListingFileResource);
			if (stream == null) {
				return Collections.emptyMap();
			}
		}
		Properties props = new Properties();
		try {
			props.load(stream);
		} catch (IOException ex) {
			JBossCentralActivator.log(ex);
		}
		closeStream(stream);
		Map<String, Set<VersionRange>> res = new HashMap<String, Set<VersionRange>>();
		for (Entry<Object, Object> entry : props.entrySet()) {
			String id = (String) entry.getKey();
			String rangeAsString = (String) entry.getValue();
			Set<VersionRange> ranges = new HashSet<VersionRange>();
			for (String rangeString : rangeAsString.split(";")) {
				VersionRange range = new VersionRange(rangeString);
				ranges.add(range);
			}
			res.put(id, ranges);
		}
		return res;
	}
	
	private InputStream loadStream(URL url) {
		try {
			return url.openStream();
		} catch (IOException ex) {
			JBossCentralActivator.log(ex);
			return null;
		}
	}
	
	private void closeStream(InputStream stream) {
		try {
			stream.close();
		} catch (IOException ex) {
			JBossCentralActivator.log(ex);
		}
	}
}
