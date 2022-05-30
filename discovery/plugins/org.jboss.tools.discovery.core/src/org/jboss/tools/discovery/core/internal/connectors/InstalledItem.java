package org.jboss.tools.discovery.core.internal.connectors;

import org.osgi.framework.Version;

/**
 * @author Steffen Pingel
 */
public class InstalledItem<T> {

	private final String id;

	private final Version version;

	private final T data;

	public InstalledItem(T data, String id, Version version) {
		this.data = data;
		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public Version getVersion() {
		return version;
	}

	public T getData() {
		return data;
	}

}