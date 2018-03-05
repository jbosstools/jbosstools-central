/*************************************************************************************
 * Copyright (c) 2008-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.preferences.PreferenceKeys;
import org.jboss.tools.foundation.core.digest.DigestUtils;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;
import org.jboss.tools.project.examples.internal.UnArchiver;
import org.osgi.framework.Bundle;

public class CentralHelper {
	
	public static final String JBOSS_CENTRAL_WEBPAGE_URL_KEY = "jboss.central.webpage.url";

	private static final String LATEST_WEBPAGE_URL = "https://repository.jboss.org/nexus/service/local/artifact/maven/redirect?r=public-jboss&g=org.jboss.tools.central&a=jbosstools-central-webpage&v=2.0.0-SNAPSHOT&e=zip&mustEndWith=.zip";

	private CentralHelper() {}
	
	/**
	 * Returns the url to the Central webpage, defined by the key <code>jboss.central.webpage.url</code> in 
	 * <a href="http://download.jboss.org/jbosstools/configuration/ide-config.properties">http://download.jboss.org/jbosstools/configuration/ide-config.properties</a>.
	 *  Note this url can be overriden by starting Eclipse with the system property <code>-Djboss.central.webpage.url=...</code>
	 * <ul>
	 * <li>if the <code>jboss.central.webpage.url</code> value ends with the <code>.zip</code> extension, the target file will be downloaded if necessary and extracted locally. The url to the local index.html will be returned.
	 * <li>else the url defined by <code>jboss.central.webpage.url</code> will be returned as-is</li>
	 * </ul>
	 * @throws CoreException
	 */
	public static String getCentralUrl(IProgressMonitor monitor) throws CoreException {
		String remoteUrl = getCentralUrlPropertyValue();
		return getCentralPageUrl(remoteUrl, "index.html", monitor);
	}
	
	private static String getCentralUrlPropertyValue() {
		String remoteUrl = System.getProperty(JBOSS_CENTRAL_WEBPAGE_URL_KEY);
		if (remoteUrl == null) {
			remoteUrl = PropertiesHelper.getPropertiesProvider().getValue(JBOSS_CENTRAL_WEBPAGE_URL_KEY, LATEST_WEBPAGE_URL);
		}
		return remoteUrl;
	}

	/**
	 * Returns the url to the Central webpage, defined by the <code>remoteUrl</code> parameter.
	 * <ul>
	 * <li>if the <code>remoteUrl</code> ends with the <code>.zip</code>, the target file will be downloaded if necessary and extracted locally.
	 * <li>else the <code>remoteUrl</code> will be returned as-is</li>
	 * </ul>
	 * @throws CoreException
	 */
	public static String getCentralUrl(String remoteUrl, IProgressMonitor monitor) throws CoreException {
		return getCentralPageUrl(remoteUrl, "index.html", monitor);
	}

	private static String getCentralPageUrl(String remoteUrl, String page, IProgressMonitor monitor) throws CoreException {
		StringBuilder url = new StringBuilder();
		if (remoteUrl.endsWith(".zip")) {
			//download it
			URI uri = null;
			Path zip = null;
			try {
				uri = new URI(remoteUrl);
			} catch (URISyntaxException e) {
				JBossCentralActivator.logWarning("Central page URL ("+remoteUrl+") is invalid. Falling back to embedded version");
				zip = getEmbeddedCentralZipPath();
			}
			if (uri != null) {
				if (uri.getScheme() == null){
					zip = Paths.get(remoteUrl).toAbsolutePath();
				} else if ("file".equals(uri.getScheme())) {
					zip = Paths.get(uri).toAbsolutePath();
				} else {
					//download it if needed
					zip = downloadIfNeeded(uri, monitor);
				}
			}
			
			Path centralFolder =  getCentralFolder();
			Path localCentralPage = null;
			try {
				if (zip != null) {
					try {
						localCentralPage = extractIfNeeded(zip, centralFolder, false, monitor);
					} catch (Exception e) {
						JBossCentralActivator.log(e, "An Error occured while extracting "+zip);
					}
				}
				if (localCentralPage == null) {
					zip = getEmbeddedCentralZipPath();
					if (!Files.exists(zip)) {
						//we're ****ed
						throw new IOException("Can't find embedded central zip");
					}
					localCentralPage = extractIfNeeded(zip, centralFolder, false, monitor);
				}
			} catch (IOException e) {
				IStatus status = new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID, "Unable to open "+zip, e);
				throw new CoreException(status);
			}
			url.append(localCentralPage);
		} else {
			url.append(remoteUrl);
		}
		String _url = url.toString();
		//is the url pointing at the expected page?
		if (_url.endsWith(page)) {
			return _url;
		}
		//is the url pointing at a different page? then start again from parent dir
		if (_url.endsWith(".html")) {
			url = new StringBuilder(_url.substring(0, _url.lastIndexOf("/")));
		}
		
		if (!_url.endsWith("/")) {
			url.append("/");
		}
		url.append(page); 
		return url.toString();
	}

	private static Path getEmbeddedCentralZipPath() {
		return getEmbeddedFilePath(JBossCentralActivator.getDefault().getBundle(), "resources/jbosstools-central-webpage.zip");
	}
	
	/**
	 * public for testing purposes
	 */
	public static Path getEmbeddedFilePath(Bundle bundle, String internalPath) {
		URL zip = bundle.getEntry(internalPath);
		if (zip == null) {
			throw new IllegalArgumentException(internalPath + " is not a valid file path");
		}
		try {
		  URL url = FileLocator.toFileURL(zip);
		  File f = new File(url.getPath());
		  return f.toPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Path getCentralFolder() {
		IPath location = JBossCentralActivator.getDefault().getStateLocation();
		String path = location.append("central").toOSString();
		return Paths.get(path);
	}

	private static Path downloadIfNeeded(URI uri, IProgressMonitor monitor) throws CoreException {
		String url = uri.toString();
		int lifespan = URLTransportUtility.CACHE_FOREVER;//url.contains("-SNAPSHOT")?URLTransportUtility.CACHE_UNTIL_EXIT:;
		File zip = new URLTransportUtility().getCachedFileForURL(url, "Download central", lifespan, monitor);
		if (zip != null && zip.exists()) {
			return zip.toPath();
		}
		return null;
	}

	private static Path extractIfNeeded(Path zip, Path centralFolder, boolean overwrite, IProgressMonitor monitor) throws IOException {
		String sha1 = DigestUtils.sha1(zip).substring(0, 7);
		Path destinationFolder = centralFolder.resolve(sha1);
		//if already extracted :
		if (overwrite) {
			FileUtils.deleteDirectory(destinationFolder.toFile());
		}
		boolean extracted = Files.isDirectory(destinationFolder);
		if (!extracted) {
			UnArchiver unarchiver = UnArchiver.create(zip.toFile(),  destinationFolder.toFile());
			unarchiver.extract(monitor);
		}
		Path extractedFile = destinationFolder.resolve("index.html");
		if (!Files.isRegularFile(extractedFile)) {
			if (extracted && !overwrite) {
				extractIfNeeded(zip, centralFolder, true, monitor);
			} else {
				throw new IOException(extractedFile + " can not be found");
			}
		}
		return extractedFile ;
	}

	public static String getLoadingPageUrl() {
		Path loadingPage = getLoadingPage();
		try {
			if (!Files.exists(loadingPage) //file doesn't exit 
				|| loadingPage.getFileName().toString().contains(".qualifier")) { //or during development
				String packageFolder = CentralHelper.class.getPackage().getName().replace('.', '/');
				URL scriptUrl = new URL("platform:/plugin/"+JBossCentralActivator.PLUGIN_ID+"/"+ packageFolder +"/loading.html"); //$NON-NLS-1$
				URL sourceUrl = FileLocator.resolve(scriptUrl);
				FileUtils.copyURLToFile(sourceUrl, loadingPage.toFile());
			}
		} catch (IOException e) {
			JBossCentralActivator.log(e, "Unable to extract loading.html");
			return null;
		}
		return loadingPage.toUri().toString();
	}
	
	public static Path getLoadingPage() {
		Path centralFolder = getCentralFolder();
		Path loadingPage = centralFolder.resolve("loading_"+JBossCentralActivator.getVersion()+".html");
		return loadingPage;
	}
	
	public static boolean isShowOnStartup() {
		IEclipsePreferences preferences = JBossCentralActivator.getDefault().getPreferences();
		return preferences.getBoolean(PreferenceKeys.SHOW_JBOSS_CENTRAL_ON_STARTUP, true);
	}
	
	public static void setShowOnStartup(boolean value) {
		IEclipsePreferences preferences = JBossCentralActivator.getDefault().getPreferences();
		preferences.putBoolean(PreferenceKeys.SHOW_JBOSS_CENTRAL_ON_STARTUP, value);
		JBossCentralActivator.getDefault().savePreferences();	
	}
	
	public static Collection<String> getInstalledBundleIds() {
		Bundle[] bundles = JBossCentralActivator.getDefault().getBundleContext().getBundles();
		HashSet<String> bundleIds = new HashSet<>(bundles.length);
		for (Bundle bundle : bundles) {
			bundleIds.add(bundle.getSymbolicName());
		}
		return bundleIds;
	}
}
