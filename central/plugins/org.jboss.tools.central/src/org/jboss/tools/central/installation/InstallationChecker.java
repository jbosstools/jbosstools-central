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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;

public class InstallationChecker {
	
	private static InstallationChecker INSTANCE;
	private static String EARLY_ACCESS_FAMILY_ID = "earlyaccess";
	private static String EXTENSION_POINT_ID = "org.jboss.tools.central.iuFamilies"; //$NON-NLS-1$
	
	private Map<String, BundleFamilyExtension> iuFamilies;
	private Map<String, Set<IInstallableUnit>> installedUnitsPerFamily;
	
	private IProfile applicationProfile;
	
	private InstallationChecker() throws ProvisionException {
		this.iuFamilies = new HashMap<>();
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			String familyId = extension.getAttribute("familyId"); //$NON-NLS-1$
			String label = extension.getAttribute("label"); //$NON-NLS-1$
			String urlKey = extension.getAttribute("urlKey"); //$NON-NLS-1$
			String url = PropertiesHelper.getPropertiesProvider().getValue(urlKey);
			String contributingBundleId = extension.getContributor().getName();
			URL listingFileResource = Platform.getBundle(contributingBundleId).getEntry(extension.getAttribute("listingFile"));
			this.iuFamilies.put(familyId, new BundleFamilyExtension(familyId, label, listingFileResource, url));
			if (listingFileResource == null) {
				JBossCentralActivator.log("Could not load default listing file for " + familyId);
			}
		}
		
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) PlatformUI.getWorkbench().getService(IProvisioningAgentProvider.class);
		IProvisioningAgent agent;
		agent = provider.createAgent(null); // null = location for running system
		if (agent == null) {
			throw new ProvisionException("Location was not provisioned by p2");
		}
		IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		if (profileRegistry == null) {
			throw new ProvisionException("Unable to acquire the profile registry service.");
		}
		this.applicationProfile = profileRegistry.getProfile(IProfileRegistry.SELF);
		if(this.applicationProfile == null) {
			throw new ProvisionException("Current Eclipse instance does not support software installation.");
		}
		
		this.installedUnitsPerFamily = new HashMap<>();
	}
	
	public synchronized static InstallationChecker getInstance() throws ProvisionException {
		if (INSTANCE == null) {
			INSTANCE = new InstallationChecker();
		}
		return INSTANCE;
	}

	public boolean hasEarlyAccess() {
		return !getEarlyAccessUnits().isEmpty();
	}

	/**
	 * @return
	 */
	public Set<IInstallableUnit> getEarlyAccessUnits() {
		return getUnits(EARLY_ACCESS_FAMILY_ID);
	}
	
	public Set<IInstallableUnit> getUnits(String family) {
		if (! installedUnitsPerFamily.containsKey(family)) {
			Set<IInstallableUnit> foundFamilyUnits = Collections.emptySet();
			BundleFamilyExtension entry = this.iuFamilies.get(family);
			Map<String, Set<VersionRange>> iusForFamily = entry.loadBundleList();
			foundFamilyUnits = new HashSet<>();
			for (Entry<String, Set<VersionRange>> iuVersions : iusForFamily.entrySet()) {
				String iuId = iuVersions.getKey();
				for (VersionRange versionRange : iuVersions.getValue()) {
					IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(iuId, versionRange);
					// can use null progress monitor as querying against installed profile is immediate
					IQueryResult<IInstallableUnit> res = this.applicationProfile.query(query, new NullProgressMonitor());
					foundFamilyUnits.addAll(res.toSet());
				}
			}

			installedUnitsPerFamily.put(family, foundFamilyUnits);
		}
		return Collections.unmodifiableSet(installedUnitsPerFamily.get(family));
	}

}
