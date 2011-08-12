package org.jboss.tools.maven.jbosspackaging.configurators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class SarArchiverConfigurator extends JBossPackagingArchiverConfigurator {

	@Override
	protected String getGoal() {
		return "sar";
	}
}
