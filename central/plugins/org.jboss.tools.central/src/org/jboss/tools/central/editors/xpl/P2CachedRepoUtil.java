package org.jboss.tools.central.editors.xpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.JBossCentralActivator;

@SuppressWarnings("restriction")
public class P2CachedRepoUtil {

	/**
	 * Cache resolved p2 repositories as it's a very long operation
	 */
	private static Map<String, IMetadataRepository> cachedRepo = new HashMap<>();
	

	/**
	 * @param connector
	 * @return
	 */
	public static IMetadataRepository getRepoForConnector(CatalogItem connector) {
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) PlatformUI.getWorkbench().getService(IProvisioningAgentProvider.class);
		IMetadataRepository repo = cachedRepo.get(connector.getSiteUrl());
		if (repo == null) {
			try {
				IProvisioningAgent agent = provider.createAgent(null); // null = for running system
				if (agent == null)
					throw new RuntimeException("Location was not provisioned by p2");
				IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
				repo = metadataManager.loadRepository(new URI(connector.getSiteUrl()), new NullProgressMonitor());
				cachedRepo.put(connector.getSiteUrl(), repo);
			} catch (ProvisionException ex) {
				JBossCentralActivator.getDefault().getLog().log(new Status(
						IStatus.ERROR,
						JBossCentralActivator.PLUGIN_ID,
						ex.getMessage(),
						ex));
			} catch (URISyntaxException ex) {
				JBossCentralActivator.getDefault().getLog().log(new Status(
						IStatus.ERROR,
						JBossCentralActivator.PLUGIN_ID,
						ex.getMessage(),
						ex));
			}
		}
		return repo;
	}
}
