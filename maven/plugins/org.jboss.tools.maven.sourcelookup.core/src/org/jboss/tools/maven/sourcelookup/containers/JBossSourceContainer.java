/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.containers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.classpath.core.runtime.RuntimeJarUtility;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;
import org.jboss.tools.maven.core.identification.IdentificationUtil;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class JBossSourceContainer extends AbstractSourceContainer {

	private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$
	public static final String TYPE_ID = "org.jboss.tools.maven.sourcelookup.containerType"; //$NON-NLS-1$

	private List<File> jars;
	private List<ISourceContainer> sourceContainers = new ArrayList<ISourceContainer>();
	private String homePath;
	private IRuntime runtime;

	private static IFileIdentificationManager fileIdentificationManager = new FileIdentificationManager();
	
	public JBossSourceContainer(ILaunchConfiguration configuration)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		if (server != null) {
			IJBossServer jbossServer = ServerConverter
					.checkedGetJBossServer(server);
			if (jbossServer != null) {
				IJBossServerRuntime runtime = jbossServer.getRuntime();
				if (runtime != null) {
					this.runtime = runtime.getRuntime();
				}
			}
		}
		if (this.runtime == null) {
			IStatus status = new Status(IStatus.ERROR,
					SourceLookupActivator.PLUGIN_ID, "Invalid configuration");
			throw new CoreException(status);
		}
	}

	public JBossSourceContainer(IRuntime runtime) {
		this.runtime = runtime;
	}
	
	public JBossSourceContainer(String homePath) {
		this.homePath = homePath;
	}

	public List<File> getJars() throws CoreException {
		if (jars != null) {
			return jars;
		}
		jars = new ArrayList<File>();
		if (runtime != null) {
			IPath[] paths = new RuntimeJarUtility().getJarsForRuntime(runtime, RuntimeJarUtility.ALL_JARS);
			if (paths != null && paths.length > 0) {
				for (IPath path:paths) {
					addFile(jars, path.toFile());
				}
				return jars;
			}
		}
		if (homePath == null) {
			return jars;
		}
		IPath jarPath = new Path(homePath);
		addJars(jarPath, jars);
		
		return jars;
	}

	private void getAS6xJars() {
		getAS5xJars();
	}

	private void getAS5xJars() {
		IPath common = new Path(homePath)
				.append(IJBossRuntimeResourceConstants.COMMON);
		addJars(common, jars);
		IPath lib = new Path(homePath)
				.append(IJBossRuntimeResourceConstants.LIB);
		addJars(lib, jars);
		IPath serverPath = new Path(homePath)
				.append(IJBossRuntimeResourceConstants.SERVER);
		IPath defaultConfiguration = serverPath
				.append(IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
		IPath configurationLib = defaultConfiguration
				.append(IJBossRuntimeResourceConstants.LIB);
		addJars(configurationLib, jars);
		IPath deployPath = defaultConfiguration
				.append(IJBossRuntimeResourceConstants.DEPLOY);
		IPath deployLib = deployPath.append(IJBossRuntimeResourceConstants.LIB);
		addJars(deployLib, jars);
		IPath jbossweb = deployPath
				.append(IJBossRuntimeResourceConstants.JBOSSWEB_SAR);
		addJars(jbossweb, jars);
		IPath deployers = defaultConfiguration
				.append(IJBossRuntimeResourceConstants.DEPLOYERS);
		addJars(deployers, jars);
	}

	private void getJBossModules() {
		IPath modules = new Path(homePath)
				.append(IJBossRuntimeResourceConstants.AS7_MODULES);
		addJars(modules, jars);
		IPath bundles = new Path(homePath).append("bundles");
		addJars(bundles, jars);
		File modulesFile = new File(homePath, IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR);
		if (modulesFile.exists()) {
			jars.add(modulesFile);
		}
	}

	private void addJars(IPath path, List<File> jars) {
		File folder = path.toFile();
		if (folder == null || !folder.isDirectory()) {
			return;
		}
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				addJars(path.append(file.getName()), jars);
			}			
			
			addFile(jars, file);
		}
	}

	private void addFile(List<File> jars, File file) {
		if (file != null && file.isFile() && file.getName().endsWith(".jar") && !jars.contains(file)) { //$NON-NLS-1$
			boolean include = true;
			String includeFilter = SourceLookupActivator.getDefault().getIncludePattern();
			if (includeFilter != null && !includeFilter.isEmpty()) {
				try {
					include = Pattern.matches(includeFilter, file.getAbsolutePath());
				} catch (Exception e) {
					SourceLookupActivator.log(e);
				}
			}
			if (include) {
				boolean exclude = false;
				String excludeFilter = SourceLookupActivator.getDefault().getExcludePattern();
				if (excludeFilter != null && !excludeFilter.isEmpty()) {
					try {
						exclude = Pattern.matches(excludeFilter, file.getAbsolutePath());
					} catch (Exception e) {
						SourceLookupActivator.log(e);
					}
				}
				if (!exclude) {
					jars.add(file);
				}
			}
		}
	}

	public String getName() {
		String name;
		if (homePath != null) {
			name = "JBoss Maven Source Container (" + homePath + ")";
		} else {
			name = "JBoss Maven Source Container";
		}
		return name;
	}

	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		for (ISourceContainer container : sourceContainers) {
			Object[] objects = container.findSourceElements(name);
			if (objects != null && objects.length > 0) {
				return objects;
			}
		}
		Object[] objects = new Object[0];
		List<File> list = getJars();
		Iterator<File> iterator = list.iterator();
		ZipFile jar = null;
		try {
			while (iterator.hasNext()) {
				File file = iterator.next();
				if (file == null || !file.exists()) {
					continue;
				}
				jar = new ZipFile(file);
				String className = name.replace(".java", ".class"); //$NON-NLS-1$ //$NON-NLS-2$
				className = className.replace("\\", PATH_SEPARATOR); //$NON-NLS-1$
				ZipEntry entry = jar.getEntry(className);
				if (entry != null) {
					ArtifactKey artifact = getArtifact(file);
					if (artifact != null) {
						IPath sourcePath = getSourcePath(artifact);
						if (sourcePath == null) {
							Job job = downloadArtifact(file, artifact);
							try {
								job.join();
							} catch (InterruptedException e) {
								continue;
							}
							Artifact resolved = resolveArtifact(artifact, new NullProgressMonitor());
							File resolvedFile = null;
							if (resolved != null) {
								resolvedFile = resolved.getFile();
							}
							if (resolvedFile != null) {
								ISourceContainer container = new ExternalArchiveSourceContainer(
										resolvedFile.getAbsolutePath(), true);
								objects = container.findSourceElements(name);
								if (objects != null && objects.length > 0) {
									sourceContainers.add(container);
									iterator.remove();
								}
							}
						} else {
							ISourceContainer container = new ExternalArchiveSourceContainer(
									sourcePath.toOSString(), true);
							objects = container.findSourceElements(name);
							if (objects != null && objects.length > 0) {
								sourceContainers.add(container);
								iterator.remove();
							}
						}
						if (objects != null && objects.length > 0) {
							break;
						}
						//Keep looking if source jar doesn't contain source file
						//e.g. io.undertow.server.handlers.Predicate.class exists in wildfly-cli.jar (but there's no matching source)
						//and undertow-core.jar, which actually has the matching source jar
					} else {
						// TODO 
					}
				}
			}
		} catch (ZipException e) {
			SourceLookupActivator.log(e);
		} catch (IOException e) {
			SourceLookupActivator.log(e);
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
					// ignore
				}
				jar = null;
			}
		}
		return objects;
	}

	public ArtifactKey getArtifact(File file) throws CoreException {
		return fileIdentificationManager.identify(file, new NullProgressMonitor());
	}

	public static Job downloadArtifact(File file, ArtifactKey artifact) {
		return downloadArtifact(file, artifact, (IJobChangeListener[])null);
	}

	public static Job downloadArtifact(File file, ArtifactKey artifact, IJobChangeListener ... listeners) {
		final ArtifactKey sourcesArtifact = new ArtifactKey(
				artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getVersion(),
				IdentificationUtil.getSourcesClassifier(artifact.getClassifier()));
		Job job = new Job("Downloading sources for " + file.getName()) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					download(sourcesArtifact, monitor);
				} catch (CoreException e) {
					//We couldn't find sources, no need to make a fuss about it
					SourceLookupActivator.logInfo(e.getLocalizedMessage());
				}
				return Status.OK_STATUS;
			}
		};
		if (listeners != null && listeners.length > 0) {
			for (IJobChangeListener listener : listeners) {
				if (listener != null) {
					job.addJobChangeListener(listener);
				}
			}
		}
		job.schedule();
		return job;
	}
	
	private static File download(ArtifactKey artifact, IProgressMonitor monitor)
			throws CoreException {
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.beginTask("Downloading sources...", 2);
		monitor.worked(1);
		Artifact resolved = resolveArtifact(artifact, monitor);
		return resolved.getFile();
	}

	protected static Artifact resolveArtifact(ArtifactKey artifact,
			IProgressMonitor monitor) throws CoreException {
		IMaven maven = MavenPlugin.getMaven();
		Artifact resolved = maven.resolve(artifact.getGroupId(), //
				artifact.getArtifactId(), //
				artifact.getVersion(), //
				"jar" /* type */, // //$NON-NLS-1$
				artifact.getClassifier(), //
				maven.getArtifactRepositories(), //
				monitor);
		monitor.done();
		return resolved;
	}

	public static IPath getSourcePath(ArtifactKey a) {
		File file = getAttachedArtifactFile(a,
				IdentificationUtil.getSourcesClassifier(a.getClassifier()));

		if (file != null) {
			return Path.fromOSString(file.getAbsolutePath());
		}

		return null;
	}

	private static File getAttachedArtifactFile(ArtifactKey a, String classifier) {
		try {
			IMaven maven = MavenPlugin.getMaven();
			ArtifactRepository localRepository = maven.getLocalRepository();
			String relPath = maven.getArtifactPath(localRepository,
					a.getGroupId(), a.getArtifactId(), a.getVersion(),
					"jar", classifier); //$NON-NLS-1$
			File file = new File(localRepository.getBasedir(), relPath)
					.getCanonicalFile();
			if (file.canRead()) {
				return file;
			}
		} catch (CoreException ex) {
			// fall through
		} catch (IOException ex) {
			// fall through
		}
		return null;
	}

	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	public String getHomePath() {
		return homePath;
	}
	
	public IRuntime getRuntime() {
		return runtime;
	}
}
