/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.discovery.core.internal.connectors.xpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.spi.IDynamicExtensionRegistry;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.Activator;
import org.eclipse.equinox.internal.p2.discovery.compatibility.Directory;
import org.eclipse.equinox.internal.p2.discovery.compatibility.Directory.Entry;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.osgi.framework.Bundle;

/**
 * This class was forked from {@link org.eclipse.mylyn.internal.discovery.core.model.DiscoveryRegistryStrategy}
 * to allow discovery of non default extension points. 
 * Contributor reference maps were modified to use the contributor id as a key.
 * 
 * @author David Green
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class DiscoveryRegistryStrategy extends RegistryStrategy {

	private final List<JarFile> jars = new ArrayList<JarFile>();

	private final Map<String, File> contributorToJarFile = new HashMap<String, File>();

	private final Map<String, Entry> contributorToDirectoryEntry = new HashMap<String, Entry>();

	private final Object token;

	private Map<File, Entry> bundleFileToDirectoryEntry;

	//private List<String> extensionPointProviderBundles;

	private String[] bundleIds;
	
	public DiscoveryRegistryStrategy(File[] storageDirs, boolean[] cacheReadOnly, Object token) {
		super(storageDirs, cacheReadOnly);
		this.token = token;
	}

	@Override
	public void onStart(IExtensionRegistry registry, boolean loadedFromCache) {
		super.onStart(registry, loadedFromCache);
		loadedFromCache = false;
		if (!loadedFromCache) {
			for (String bundleId : getExtensionPointProviderBundleIds()) {
				processExtensionPointProviderBundle(registry, bundleId);
			}
			processBundles(registry);
		}
	}
	
	@Override
	public boolean debug() {
		return false;
	}

	public void setExtensionPointProviderBundleIds(String ... bundleIds) {
		this.bundleIds = bundleIds;
	}
	
	public String[] getExtensionPointProviderBundleIds() {
		if (bundleIds == null) {
			bundleIds = new String[]{Activator.ID};
		}
		return bundleIds;
	}

	private void processExtensionPointProviderBundle(IExtensionRegistry registry, String bundleId) {
		try {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle == null) {
				DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
						"Cannot load bundle {0}", bundleId)));
				return;
			}
			IContributor contributor = new RegistryContributor(bundle.getSymbolicName(), bundle.getSymbolicName(),
					null, null);

			InputStream inputStream = bundle.getEntry("plugin.xml").openStream(); //$NON-NLS-1$
			try {
				registry.addContribution(inputStream, contributor, false, bundle.getSymbolicName(), null, token);
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	private void processBundles(IExtensionRegistry registry) {
		if (bundleFileToDirectoryEntry == null) {
			throw new IllegalStateException();
		}
		for (java.util.Map.Entry<File, Entry> bundleFile : bundleFileToDirectoryEntry.entrySet()) {
			try {
				processBundle(registry, bundleFile.getValue(), bundleFile.getKey());
			} catch (Exception e) {
				DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
						"Cannot load bundle {0} from url {1}: {2}", new Object[] {
								bundleFile.getKey().getName(), bundleFile.getValue().getLocation(), e.getMessage() }),
						e));
			}
		}
	}

	private void processBundle(IExtensionRegistry registry, Directory.Entry entry, File bundleFile) throws IOException {
		JarFile jarFile = new JarFile(bundleFile);
		jars.add(jarFile);

		ZipEntry pluginXmlEntry = jarFile.getEntry("plugin.xml"); //$NON-NLS-1$
		if (pluginXmlEntry == null) {
			throw new IOException("no plugin.xml in bundle");
		}
		IContributor contributor = new RegistryContributor(bundleFile.getName(), bundleFile.getName(), null, null);
		if (((IDynamicExtensionRegistry) registry).hasContributor(contributor)) {
			jarFile.close();
			return;
		}
		
		contributorToJarFile.put(contributor.getName(), bundleFile);
		contributorToDirectoryEntry.put(contributor.getName(), entry);

		ResourceBundle translationBundle = loadTranslationBundle(jarFile);

		InputStream inputStream = jarFile.getInputStream(pluginXmlEntry);
		try {
			registry.addContribution(inputStream, contributor, false, bundleFile.getPath(), translationBundle, token);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	private ResourceBundle loadTranslationBundle(JarFile jarFile) throws IOException {
		List<String> bundleNames = computeBundleNames("plugin"); //$NON-NLS-1$
		for (String bundleName : bundleNames) {
			ZipEntry entry = jarFile.getEntry(bundleName);
			if (entry != null) {
				InputStream inputStream = jarFile.getInputStream(entry);
				try {
					PropertyResourceBundle resourceBundle = new PropertyResourceBundle(inputStream);
					return resourceBundle;
				} finally {
					inputStream.close();
				}
			}
		}
		return null;
	}

	private List<String> computeBundleNames(String baseName) {
		String suffix = ".properties"; //$NON-NLS-1$
		String name = baseName;
		List<String> bundleNames = new ArrayList<String>();
		Locale locale = Locale.getDefault();
		bundleNames.add(name + suffix);
		if (locale.getLanguage() != null && locale.getLanguage().length() > 0) {
			name = name + '_' + locale.getLanguage();
			bundleNames.add(0, name + suffix);
		}
		if (locale.getCountry() != null && locale.getCountry().length() > 0) {
			name = name + '_' + locale.getCountry();
			bundleNames.add(0, name + suffix);
		}
		if (locale.getVariant() != null && locale.getVariant().length() > 0) {
			name = name + '_' + locale.getVariant();
			bundleNames.add(0, name + suffix);
		}
		return bundleNames;
	}

	@Override
	public void onStop(IExtensionRegistry registry) {
		try {
			super.onStop(registry);
		} finally {
			for (JarFile jar : jars) {
				try {
					jar.close();
				} catch (Exception e) {
				}
			}
			jars.clear();
		}
	}

	/**
	 * get the jar file that corresponds to the given contributor.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given contributor is unknown
	 */
	public File getJarFile(IContributor contributor) {
		File file = contributorToJarFile.get(contributor.getName());
		if (file == null) {
			throw new IllegalArgumentException(contributor.getName());
		}
		return file;
	}

	/**
	 * get the directory entry that corresponds to the given contributor.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given contributor is unknown
	 */
	public Entry getDirectoryEntry(IContributor contributor) {
		Entry entry = contributorToDirectoryEntry.get(contributor.getName());
		if (entry == null) {
			throw new IllegalArgumentException(NLS.bind("{0} is not a known contributor in the discovery registry", contributor.getName()));
		}
		return entry;
	}

	public void setBundles(Map<File, Entry> bundleFileToDirectoryEntry) {
		this.bundleFileToDirectoryEntry = bundleFileToDirectoryEntry;
	}
	
}