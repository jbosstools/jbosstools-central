/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.jboss.tools.discovery.core.internal.connectors.ChainedDiscoveryStrategy;


/**
 * Manager for {@link ProxyWizard} metadata.
 * 
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class ProxyWizardManager {
	
	public static final ProxyWizardManager INSTANCE = new ProxyWizardManager();

	private ProxyWizardUpdateJob updateJob;

	private List<ProxyWizard> proxyWizards;
	
	private List<ProxyWizardManagerListener> listeners = new CopyOnWriteArrayList<>();

	private String discoveryUrl;

	private IPath rootCacheFolderPath;
	
	public void setRootCacheFolderPath(IPath rootCacheFolderPath) {
		this.rootCacheFolderPath = rootCacheFolderPath;
	}

	private ProxyWizardManager() {
		updateJob = new ProxyWizardUpdateJob(this);
		//Use -Djboss.discovery.directory.url=... to override the discovery url value
	}

	/**
	 * Loads {@link ProxyWizard} metadata using different discovery strategies.
	 * The order in which the discovery is performed is : remote (if <code>searchRemote</code> is true), local cache, installed plugins. 
	 * The first strategy discovering metadata wins.
	 * <br/><br/>
	 * After a remote search is performed, attached {@link ProxyWizardManagerListener}s are notified of the data update and old cache folders are deleted.
	 * 
	 * @param searchRemote 
	 * @param monitor
	 */
	void loadWizards(boolean searchRemote, IProgressMonitor monitor) {
		//long start = System.currentTimeMillis();
		ConnectorDiscovery connectorDiscovery = getConnectorDiscovery();
		
		ProxyWizardDataCollector dataCollector = new ProxyWizardDataCollector();

		ChainedDiscoveryStrategy proxyWizardDiscoveryStrategy = new ChainedDiscoveryStrategy(dataCollector);
		
		File currentCacheFolder = getCurrentCacheFolder();
		
		if (monitor.isCanceled()) {
			return;
		}
		if (searchRemote) {
			//First look online
			RemoteProxyWizardDiscoveryStrategy remoteDiscoveryStrategy = new RemoteProxyWizardDiscoveryStrategy();
			remoteDiscoveryStrategy.setStorageFolder(getNewCacheFolder());
			remoteDiscoveryStrategy.setDirectoryUrl(getDiscoveryUrl());
			proxyWizardDiscoveryStrategy.addStrategy(remoteDiscoveryStrategy);
		}

		if (currentCacheFolder != null) {
			//Second, look last cached data
			CachedProxyWizardDiscoveryStrategy cachedDiscoveryStrategy = new CachedProxyWizardDiscoveryStrategy();
			cachedDiscoveryStrategy.setStorageFolder(currentCacheFolder);
			proxyWizardDiscoveryStrategy.addStrategy(cachedDiscoveryStrategy);
		}

		//Finally, look for installed data
		proxyWizardDiscoveryStrategy.addStrategy(new InstalledProxyWizardDiscoveryStrategy());
		
		connectorDiscovery.getDiscoveryStrategies().add(proxyWizardDiscoveryStrategy);

		if (monitor.isCanceled()) {
			return;
		}
		connectorDiscovery.performDiscovery(monitor);
		if (monitor.isCanceled()) {
			return;
		}
		proxyWizards = dataCollector.getProxyWizards();
		if (searchRemote) {
			if (monitor.isCanceled()) {
				return;
			}
			notifyListeners(proxyWizards);
			purgeOldCacheFolders(2, monitor);
		}
		//long elapsed = System.currentTimeMillis() - start;
		//System.err.println("loading proxyWizards took "+ (searchRemote?"(including remote) ":" ") + elapsed + " ms");
	}
	
	/**
	 * Returns the {@link ProxyWizard} metadata. The metadata lookup is local (data is searched in installed or locally cached plugins discovery connectors). 
	 * If <code>triggerUpdate</code> is true, a background job is scheduled to update the cached metadata with remote data.
	 *   
	 * @param triggerUpdate trigger
	 * @param monitor
	 * @return non-null list of {@link ProxyWizard}
	 */
	public List<ProxyWizard> getProxyWizards(boolean triggerUpdate, IProgressMonitor monitor) {
		loadWizards(false, monitor);
		if (triggerUpdate) {
			updateJob.schedule();
		}
		return proxyWizards;
	}

	
	void updateWizards(IProgressMonitor monitor) {
		updateWizards(getDiscoveryUrl(), monitor);
	}
	
	void updateWizards(String remoteDiscoveryUrl, IProgressMonitor monitor) {
		loadWizards(true, monitor);
	}

	String getDiscoveryUrl() {
		if (discoveryUrl == null) {
			discoveryUrl = DiscoveryActivator.getDefault().getJBossDiscoveryDirectory(); 
		}
		return discoveryUrl;
	}

	//For testing purposes
	public void setDiscoveryUrl(String discoveryUrl) {
		this.discoveryUrl = discoveryUrl;
	}
	
	/**
	 * Creates and return a new cache folder under <i>workspace/.metadata/plugins/org.jboss.tools.central/proxyWizards</i>.
	 * The folder name corresponds to the System timestamp at the folder creation.
	 */
	private File getNewCacheFolder() {
		IPath root = getRootCacheFolderPath();
		File newCacheFolder = new File(root.toFile(), System.currentTimeMillis()+"");
		if (newCacheFolder.exists()) {
			newCacheFolder.delete();
		}
		newCacheFolder.mkdirs();
		return newCacheFolder;
	}

	/**
	 * Returns the root folder under which discovery jars are cached. The location corresponds to <i>workspace/.metadata/plugins/org.jboss.tools.central/proxyWizards</i>.
	 */
	private IPath getRootCacheFolderPath() {
		if (rootCacheFolderPath == null) {
			rootCacheFolderPath = JBossCentralActivator.getDefault().getStateLocation().append("proxyWizards");
		}
		return rootCacheFolderPath;
	}


	/**
	 * Returns the most recent cache folder under <i>workspace/.metadata/plugins/org.jboss.tools.central/proxyWizards</i>.
	 */
	private File getCurrentCacheFolder() {
		File[] files = getCacheFolders(false);		
		return files == null || files.length == 0? null : files[0];
	}

	/**
	 * Deletes the oldest cache folders
	 * 
	 * @param nbKeptFolders the number of most recent cache folders to keep 
	 * @param monitor
	 */
	void purgeOldCacheFolders(int nbKeptFolders, IProgressMonitor monitor) {		
		File[] files = getCacheFolders(true);
		if (files == null || files.length <= nbKeptFolders) {
			return;
		}
		File currentCache = getCurrentCacheFolder();
		int i = files.length;
		while (i > nbKeptFolders && i > 0) {
			if (monitor.isCanceled()) {
				return;
			}
			File f = files[--i];
			if (!f.equals(currentCache)) {
				FileUtils.deleteQuietly(f);
			}
		}
	}
	
	private File[] getCacheFolders(final boolean includeAll) {
		IPath root = getRootCacheFolderPath();
		File rootFolder = root.toFile();
		File[] files = rootFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File cacheCandidate) {
				return cacheCandidate.isDirectory() && (includeAll || containsCacheData(cacheCandidate));
			}
			
			private boolean containsCacheData(File folder) {
				final boolean[] hasCache = new boolean[2];
				folder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File f) {
						if (f.isDirectory() && f.getName().endsWith(".rcache")) {
							hasCache[0] = true;
							return true; 
						}
						if (f.isFile() && f.getName().endsWith(".jar")) {
							hasCache[1] = true;
							return true; 
						}
						return false;
					}
				});
				
				//If folder contains both .rcache and *.jar, then it's a cache folder
				return /*hasCache[0] &&*/ hasCache[1];
				
				
			}
		});

		if (files == null || files.length == 0) {
			return null;
		}
		//Put most recent folders first
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2){
		    	return f2.getName().compareTo(f1.getName());
		        //return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		    } 
		});
		
		return files;
	}

	
	private ConnectorDiscovery getConnectorDiscovery() {
		ConnectorDiscovery connectorDiscovery = new ConnectorDiscovery();
		connectorDiscovery.setEnvironment(createEnvironment());
		connectorDiscovery.setVerifyUpdateSiteAvailability(false);
		return connectorDiscovery;
	}
	
	private void notifyListeners(List<ProxyWizard> newProxyWizards) {
		UpdateEvent event = new UpdateEvent(newProxyWizards);
		for (ProxyWizardManagerListener listener : listeners) {
			try {
				listener.onProxyWizardUpdate(event);
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		}
	}

	private Dictionary<Object, Object> createEnvironment() {
		Hashtable<Object, Object> environment = new Hashtable<>(System.getProperties());
		return environment;
	}

	public class UpdateEvent {
		
		private List<ProxyWizard> proxyWizards;
		
		public UpdateEvent(List<ProxyWizard> newProxyWizards) {
			proxyWizards = newProxyWizards;
		}
		
		public List<ProxyWizard> getProxyWizards() {
			return proxyWizards;
		}
	}

	public interface ProxyWizardManagerListener {
		void onProxyWizardUpdate(UpdateEvent event) throws CoreException;
	}
	
	public void registerListener(ProxyWizardManagerListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}
	
	public void unRegisterListener(ProxyWizardManagerListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}
	
}
