package org.jboss.tools.discovery.core.internal.connectors;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author Steffen Pingel
 */
public abstract class AbstractInstallJob implements IRunnableWithProgress {

	public abstract Set<String> getInstalledFeatures(IProgressMonitor monitor);

	public abstract IStatus uninstall(UninstallRequest request, IProgressMonitor progressMonitor)
			throws InvocationTargetException, InterruptedException;

}