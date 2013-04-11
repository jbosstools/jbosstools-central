/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat, Inc. - file based remote connectors, non default connector discovery
 *******************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards.xpl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.DiscoveryCore;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.Directory;
import org.eclipse.mylyn.internal.discovery.core.model.Directory.Entry;
import org.eclipse.mylyn.internal.discovery.core.model.JarDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.Policy;

/**
 * 
 * This class was forked from <a href=
 * "http://git.eclipse.org/c/mylyn/org.eclipse.mylyn.commons.git/commit/org.eclipse.mylyn.discovery.core/src/org/eclipse/mylyn/internal/discovery/core/model/RemoteBundleDiscoveryStrategy.java?id=7991a279dcbf705b49cbee255c32167a21507204"
 * >org.eclipse.mylyn.internal.discovery.core.model.
 * RemoteBundleDiscoveryStrategy</a> Changes include :
 * <ul>
 * <li>Allow non Connector Extension</li>
 * <li>Allow file based remote bundles</li>
 * </ul>
 * 
 * A discovery strategy that downloads a simple directory of remote jars. The
 * directory is first downloaded, then each remote jar is downloaded.
 * 
 * @author David Green
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class ExternalBundleDiscoveryStrategy extends BundleDiscoveryStrategy {

	private DiscoveryRegistryStrategy registryStrategy;

	private int maxDiscoveryJarDownloadAttempts = 1;

	private File storageFolder;

	private boolean deleteStorageFolderOnFailure;
	
	private boolean unzipJars;

	public File getStorageFolder() {
		return storageFolder;
	}

	public void setStorageFolder(File storageFolder) {
		this.storageFolder = storageFolder;
	}

	public void setDeleteStorageFolderOnFailure(boolean deleteOnFailure) {
		this.deleteStorageFolderOnFailure = deleteOnFailure;
	}

	public boolean  isDeleteStorageFolderOnFailure() {
		return deleteStorageFolderOnFailure;
	}

	@Override
	public void performDiscovery(IProgressMonitor monitor) throws CoreException {
		if (connectors == null || categories == null) {
			throw new IllegalStateException();
		}
		if (registryStrategy != null) {
			throw new IllegalStateException();
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		final int totalTicks = 100000;
		final int ticksTenPercent = totalTicks / 10;
		File storageDirectory = null;
		try {
			storageDirectory = getStorageFolder();
			if (storageDirectory == null) {
				throw new IllegalStateException("storageDirectory not defined");
			}
			
			if (!storageDirectory.exists()) {
				if (!storageDirectory.mkdirs()) {
					throw new CoreException(new Status(IStatus.ERROR,
							DiscoveryCore.ID_PLUGIN,
							"IO failure: cannot create storage area"));					
				}
			}

			File registryCacheFolder = getRegistryCacheFolder(storageDirectory);

			if (monitor.isCanceled()) {
				return;
			}

			Map<File, Entry> bundleFileToDirectoryEntry = loadRegistry(storageDirectory, monitor);

			try {
				registryStrategy = new DiscoveryRegistryStrategy(
						new File[] { registryCacheFolder },
						new boolean[] { false }, this);
				registryStrategy.setExtensionPointProviderBundleIds(getExtensionPointProviderBundleIds());
				registryStrategy.setBundles(bundleFileToDirectoryEntry);
				IExtensionRegistry extensionRegistry = new ExtensionRegistry(registryStrategy, this, this);
				try {
					IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(getExtensionPointId());
					if (extensionPoint != null) {
						IExtension[] extensions = extensionPoint.getExtensions();
						if (extensions.length > 0) {
							processExtensions(new SubProgressMonitor(monitor, ticksTenPercent * 3), extensions);
						}
					}
				} finally {
					extensionRegistry.stop(this);
				}
			} finally {
				registryStrategy = null;
			}
		}catch (CoreException e){
			if (deleteStorageFolderOnFailure && storageDirectory != null) {
				delete(storageDirectory);
			}
			throw e;
		} finally {
			monitor.done();
		}
	}

	protected File getRegistryCacheFolder(File storageFolder)
			throws CoreException {

		File registryCacheFolder = null;
		try {
			registryCacheFolder = new File(storageFolder, ".rcache"); //$NON-NLS-1$
			if (!registryCacheFolder.exists() && !registryCacheFolder.mkdirs()) {
				throw new IOException();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					DiscoveryCore.ID_PLUGIN,
					"IO failure: cannot create temporary registry area", e));
		}
		return registryCacheFolder;
	}

	protected Map<File, Entry> loadRegistry(File storageDirectory, IProgressMonitor monitor) throws CoreException {
		// new SubProgressMonitor(monitor, ticksTenPercent * 3);
		final int totalTicks = 100000;
		monitor.beginTask("Local discovery", totalTicks);

		Map<File, Directory.Entry> bundleFileToDirectoryEntry = new HashMap<File, Directory.Entry>();
		try {
			for (File f : storageDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			})) {
				Entry entry = new Directory.Entry();
				entry.setLocation(f.getAbsolutePath());
			    //FIXME set entry.setPermitCategories(...);
				bundleFileToDirectoryEntry.put(f, entry );
			}
			
		} finally {
		}
		return bundleFileToDirectoryEntry;
	}

	private void delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				if (children != null) {
					for (File child : children) {
						delete(child);
					}
				}
			}
			if (!file.delete()) {
				// fail quietly
			}
		}
	}

	@Override
	protected AbstractDiscoverySource computeDiscoverySource(
			IContributor contributor) {
		Entry directoryEntry = registryStrategy.getDirectoryEntry(contributor);
		Policy policy = new Policy(directoryEntry.isPermitCategories());
		JarDiscoverySource discoverySource = new JarDiscoverySource(
				contributor.getName(), registryStrategy.getJarFile(contributor));
		discoverySource.setPolicy(policy);
		return discoverySource;
	}

	/**
	 * indicate how many times discovyer jar downloads should be attempted
	 */
	public int getMaxDiscoveryJarDownloadAttempts() {
		return maxDiscoveryJarDownloadAttempts;
	}

	/**
	 * indicate how many times discovyer jar downloads should be attempted
	 * 
	 * @param maxDiscoveryJarDownloadAttempts
	 *            a number >= 1
	 */
	public void setMaxDiscoveryJarDownloadAttempts(
			int maxDiscoveryJarDownloadAttempts) {
		if (maxDiscoveryJarDownloadAttempts < 1
				|| maxDiscoveryJarDownloadAttempts > 2) {
			throw new IllegalArgumentException();
		}
		this.maxDiscoveryJarDownloadAttempts = maxDiscoveryJarDownloadAttempts;
	}
	

	protected String[] getExtensionPointProviderBundleIds() {
		return new String[] { DiscoveryCore.ID_PLUGIN };
	}
}