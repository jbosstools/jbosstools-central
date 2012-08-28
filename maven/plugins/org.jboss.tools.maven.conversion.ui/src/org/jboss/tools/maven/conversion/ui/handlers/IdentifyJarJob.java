package org.jboss.tools.maven.conversion.ui.handlers;

import java.io.File;

import org.apache.maven.model.Dependency;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;

public class IdentifyJarJob extends Job {

	private File file;
	
	private IFileIdentificationManager fileIdentificationManager;

	private Dependency dependency;

	public IdentifyJarJob(String name, IFileIdentificationManager fileIdentificationManager, File file) {
		super(name);
		this.fileIdentificationManager = fileIdentificationManager;
		this.file = file;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		ArtifactKey artifactKey;
		try {
			artifactKey = fileIdentificationManager.identify(file, monitor);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, e.getMessage(), e);
		}
		if (artifactKey != null) {
			dependency = new Dependency();
			dependency.setArtifactId(artifactKey.getArtifactId());
			dependency.setGroupId(artifactKey.getGroupId());
			dependency.setVersion(artifactKey.getVersion());
			dependency.setClassifier(artifactKey.getClassifier());
		}
		return Status.OK_STATUS;
	}

	public Dependency getDependency() {
		return dependency;
	}

}
