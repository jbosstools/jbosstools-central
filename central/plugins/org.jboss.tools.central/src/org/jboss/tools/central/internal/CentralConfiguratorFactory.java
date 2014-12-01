package org.jboss.tools.central.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.configurators.IJBossCentralConfigurator;

public class CentralConfiguratorFactory {

	private static final String CONFIGURATORS_EXTENSION_ID = "org.jboss.tools.central.configurators"; //$NON-NLS-1$

	private static final Object CONFIGURATOR = "configurator"; //$NON-NLS-1$

	public static IJBossCentralConfigurator createConfigurator() {
		IJBossCentralConfigurator configurator = null;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(CONFIGURATORS_EXTENSION_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions.length > 1) {
			for (IExtension e :extensions) {
				logIgnoredExtensionPoint(e);
			}
		}
		if (extensions.length > 0) {
			for (IConfigurationElement configurationElement : extensions[0]
					.getConfigurationElements()) {
				if (CONFIGURATOR.equals(configurationElement.getName())) {
					try {
						configurator = (IJBossCentralConfigurator) configurationElement
								.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						JBossCentralActivator.log(e);
						continue;
					}
					break;
				}
			}

		}
		if (configurator == null) {
			configurator = new DefaultJBossCentralConfigurator();
		}
		return configurator;
	}

	private static void logIgnoredExtensionPoint(IExtension extension) {
		String className = null;
		IConfigurationElement[] configurationElements = extension
				.getConfigurationElements();
		for (int j = 0; j < configurationElements.length; j++) {
			IConfigurationElement configurationElement = configurationElements[j];
			if (CONFIGURATOR.equals(configurationElement.getName())) {
				className = configurationElement.getAttribute("class");
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append("The configurators extension point is ignored: classname=");
		builder.append(className);
		if (extension.getContributor() != null
				&& extension.getContributor().getName() != null) {
			builder.append(",pluginId=");
			builder.append(extension.getContributor().getName());
		}
		JBossCentralActivator.log(builder.toString());
	}
}
