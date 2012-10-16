/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.internal.identification;

import static org.jboss.tools.maven.core.identification.IdentificationUtil.getSHA1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.osgi.util.NLS;
import org.jboss.dmr.ModelNode;

public class MavenCentralIdentifier extends AbstractArtifactIdentifier {


	private String SHA1_SEARCH_QUERY = "http://search.maven.org/solrsearch/select?q=1:%22{0}%22&rows=1&wt=json";

	private String NAME_VERSION_SEARCH_QUERY = "http://search.maven.org/solrsearch/select?q=a:%22{0}%22%20AND%20v:%22{1}%22&rows=2&wt=json";

	@Deprecated
	public ArtifactKey identify(File file) throws CoreException {
		return identify(file, null);
	}

	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		ArtifactKey key = sha1Search(file, monitor);
		if (key == null) {
			//System.out.println("Can't identify "+file + " falling back on name+version search" );
			key = nameVersionSearch(file, monitor);
		}
		return key;
	}

	private ArtifactKey sha1Search(File file, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			return null;
		}
		String sha1;
		try {
			sha1 = getSHA1(file);
		} catch (Exception e) {
			return null;
		}
		String searchUrl = NLS.bind(SHA1_SEARCH_QUERY, sha1);
		return find(searchUrl);
	}
	
	private ArtifactKey nameVersionSearch(File file, IProgressMonitor monitor) {
		
		JarFile jar = null;
		Manifest manifest = null;
		try {
			jar = new JarFile(file);
			manifest = jar.getManifest();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (Exception e) {
					//Ignore
				}
			}
		}
		if (manifest == null) {
			return null;
		}
		
		String version = manifest.getMainAttributes().getValue("Implementation-Version");
		String name = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		if (version == null || version.trim().isEmpty() || name == null || name.trim().isEmpty()) {
			return null;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			return null;
		}
		
		String searchUrl = NLS.bind(NAME_VERSION_SEARCH_QUERY, name, version);
		return find(searchUrl);
	}
	
	
	
	private ArtifactKey find(String searchUrl) {
		HttpURLConnection connection = null;//TODO use eclipse connections to handle proxies
		InputStream is = null;
		try {
			connection = (HttpURLConnection) new URL(searchUrl).openConnection();
			connection.setConnectTimeout(5*1000);
			connection.connect();
			int status = connection.getResponseCode();
	        switch (status) {
	            case 200:
	            case 201: {
	            	is = connection.getInputStream();
	            	ModelNode modelNode = ModelNode.fromJSONStream(is);
	            	if (modelNode.isDefined()) {
	            		return extractKey(modelNode);
	            	}
	            }    
	            default:
	            		
	        }
		} catch (UnknownHostException uhe) {
			System.err.println("Can't connect to search.maven.org:"+ uhe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(is);
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private ArtifactKey extractKey(ModelNode modelNode) {
		ModelNode response = modelNode.get("response");
		if (response != null) {
			int num = response.get("numFound").asInt();
			if (num > 0) {
				ModelNode docs = response.get("docs");
				if (docs.isDefined()) {
					String a = null, g = null, v = null;
					for (ModelNode n : docs.asList()) {
						if (n.hasDefined("a") 
						&&  n.hasDefined("g")
						&&  n.hasDefined("v")) {
							a = n.get("a").asString();
							g = n.get("g").asString();
							v = n.get("v").asString();
							if (a != null && g != null && v != null) {
								return new ArtifactKey(g, a, v, null);
							}
						}
					}
				}
			}
		}
		return null;
	}

}
