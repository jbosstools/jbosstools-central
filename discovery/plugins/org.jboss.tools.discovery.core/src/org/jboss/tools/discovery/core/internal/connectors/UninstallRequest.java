package org.jboss.tools.discovery.core.internal.connectors;

/**
 * @author Steffen Pingel
 */
public abstract class UninstallRequest {

	public abstract boolean select(@SuppressWarnings("rawtypes") InstalledItem item);

}