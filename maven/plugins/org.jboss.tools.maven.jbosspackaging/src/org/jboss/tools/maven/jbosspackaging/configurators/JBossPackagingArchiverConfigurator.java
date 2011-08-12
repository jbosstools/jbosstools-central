package org.jboss.tools.maven.jbosspackaging.configurators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.sonatype.m2e.mavenarchiver.internal.JarArchiverConfigurator;

public abstract class JBossPackagingArchiverConfigurator extends JarArchiverConfigurator {

	@Override
	protected MojoExecutionKey getExecutionKey() {
		MojoExecutionKey key = new MojoExecutionKey("org.codehaus.mojo", "jboss-packaging-maven-plugin", "", getGoal(),
				null, null);
		return key;
	}

	protected abstract String getGoal();

	@Override
	public void generateManifest(IMavenProjectFacade mavenFacade, IFile manifest, IProgressMonitor monitor)
			throws CoreException {
		super.generateManifest(mavenFacade, manifest, monitor);
	}
	
	@Override
	protected boolean needsNewManifest(IFile manifest, IMavenProjectFacade oldFacade, IMavenProjectFacade newFacade,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return super.needsNewManifest(manifest, oldFacade, newFacade, monitor);
	}
}
