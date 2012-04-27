/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.index.IIndex;
import org.eclipse.m2e.core.internal.index.IndexManager;
import org.eclipse.m2e.core.internal.index.IndexedArtifactFile;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndex;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndexManager;
import org.eclipse.m2e.core.repository.IRepository;
import org.eclipse.m2e.core.repository.IRepositoryRegistry;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBean;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.sourcelookup.NexusRepository;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;

/**
 * 
 * @author snjeza
 * 
 */
public class JBossSourceContainer extends AbstractSourceContainer {

	private static final String PATH_SEPARATOR = "/";
	private static final String CLASSIFIER_SOURCES = "sources"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTS = "tests"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTSOURCES = "test-sources"; //$NON-NLS-1$
	public static final String TYPE_ID = "org.jboss.tools.maven.sourcelookup.containerType"; //$NON-NLS-1$

	public static final String EAP = "EAP"; //$NON-NLS-1$
	public static final String EAP_STD = "EAP_STD"; //$NON-NLS-1$
	public static final String SOA_P = "SOA-P"; //$NON-NLS-1$
	public static final String SOA_P_STD = "SOA-P-STD"; //$NON-NLS-1$
	public static final String EPP = "EPP"; //$NON-NLS-1$
	public static final String EWP = "EWP"; //$NON-NLS-1$

	private List<File> jars;
	private List<ISourceContainer> sourceContainers = new ArrayList<ISourceContainer>();
	protected static File resolvedFile;
	private String homePath;
	private static List<IRepository> globalRepositories;

	public JBossSourceContainer(ILaunchConfiguration configuration)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		if (server != null) {
			JBossServer jbossServer = ServerConverter
					.checkedGetJBossServer(server);
			if (jbossServer != null) {
				IJBossServerRuntime runtime = jbossServer.getRuntime();
				if (runtime != null) {
					IPath location = runtime.getRuntime().getLocation();
					this.homePath = location.toOSString();
				}
			}
		}
		if (this.homePath == null) {
			IStatus status = new Status(IStatus.ERROR,
					SourceLookupActivator.PLUGIN_ID, "Invalid configuration");
			throw new CoreException(status);
		}
		initialize();
	}

	private static void initialize() throws CoreException {
		IRepositoryRegistry repositoryRegistry = MavenPlugin
				.getRepositoryRegistry();
		globalRepositories = repositoryRegistry
				.getRepositories(IRepositoryRegistry.SCOPE_SETTINGS);
		MavenPlugin.getMaven().reloadSettings();
	}

	public JBossSourceContainer(String homePath) {
		this.homePath = homePath;
		try {
			initialize();
		} catch (CoreException e) {
			SourceLookupActivator.log(e);
		}
	}

	private List<File> getJars() throws CoreException {
		if (jars != null) {
			return jars;
		}
		jars = new ArrayList<File>();
		if (homePath == null) {
			return jars;
		}
		File location = new File(homePath);
		ServerBeanLoader loader = new ServerBeanLoader(location);
		ServerBean serverBean = loader.getServerBean();
		JBossServerType type = serverBean.getType();
		String version = serverBean.getVersion();
		if (JBossServerType.AS7.equals(type)) {
			getAS7xJars();
		}
		if (JBossServerType.AS.equals(type)) {
			if (IJBossToolingConstants.V6_0.equals(version)
					|| IJBossToolingConstants.V6_1.equals(version)) {
				getAS6xJars();
			}
			if (IJBossToolingConstants.V5_0.equals(version)
					|| IJBossToolingConstants.V5_1.equals(version)) {
				getAS5xJars();
			}
		}
		if (JBossServerType.EAP6.equals(type)) {
			getAS7xJars();
		}
		if (JBossServerType.SOAP.equals(type) || JBossServerType.EAP.equals(type) || EPP.equals(type)
				|| JBossServerType.SOAP_STD.equals(type) || JBossServerType.EWP.equals(type)
				|| JBossServerType.EAP_STD.equals(type)) {

			getAS5xJars();
		}
		if (jars.size() == 0 && homePath != null) {
			IPath jarPath = new Path(homePath);
			addJars(jarPath, jars);
		}
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

	private void getAS7xJars() {
		IPath modules = new Path(homePath)
				.append(IJBossRuntimeResourceConstants.AS7_MODULES);
		addJars(modules, jars);
		IPath bundles = new Path(homePath).append("bundles");
		addJars(bundles, jars);
		File modulesFile = new File(homePath,
				IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR);
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
			if (file.isFile()
					&& file.getName().endsWith(".jar") && !jars.contains(file)) { //$NON-NLS-1$
				jars.add(file);
			}
		}
	}

	public String getName() {
		String name;
		if (homePath != null) {
			name = "JBoss Source Container (" + homePath + ")";
		} else {
			name = "JBoss Source Container";
		}
		return name; //$NON-NLS-1$
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
		List<File> removeJars = new ArrayList<File>();
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
				String className = name.replace(".java", ".class");
				className = className.replace("\\", PATH_SEPARATOR);
				ZipEntry entry = jar.getEntry(className);//$NON-NLS-1$
				if (entry != null) {
					ArtifactKey artifact = getArtifact(file, jar);
					if (artifact != null) {
						IPath sourcePath = getSourcePath(artifact);
						if (sourcePath == null) {
							Job job = downloadArtifact(file, artifact);
							try {
								job.join();
							} catch (InterruptedException e) {
								continue;
							}
							if (resolvedFile != null) {
								ISourceContainer container = new ExternalArchiveSourceContainer(
										resolvedFile.getAbsolutePath(), true);
								objects = container.findSourceElements(name);
								if (objects != null && objects.length > 0) {
									sourceContainers.add(container);
									removeJars.add(file);
								}
							}
						} else {
							ISourceContainer container = new ExternalArchiveSourceContainer(
									sourcePath.toOSString(), true);
							objects = container.findSourceElements(name);
							if (objects != null && objects.length > 0) {
								sourceContainers.add(container);
								removeJars.add(file);
							}
						}
						break;
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
			for (File remove : removeJars) {
				jars.remove(remove);
			}
		}
		return objects;
	}

	public static ArtifactKey getArtifact(File file, ZipFile jar)
			throws CoreException, IOException {
		ArtifactKey artifact = getArtifactFromM2eIndex(file);
		if (artifact == null) {
			artifact = getArtifactFromMetaInf(jar);
		}
		if (artifact == null) {
			artifact = getArtifactFromJBossNexusRepository(file);
		}
		return artifact;
	}

	private static ArtifactKey getArtifactFromJBossNexusRepository(String sha1,
			NexusRepository nexusRepository) {
		if (sha1 == null || nexusRepository == null
				|| nexusRepository.getUrl() == null) {
			return null;
		}
		HttpURLConnection connection = null;
		try {
			String base = nexusRepository.getUrl();
			if (!base.endsWith(PATH_SEPARATOR)) {
				base = base + PATH_SEPARATOR;
			}
			// String url =
			// "https://repository.jboss.org/nexus/service/local/data_index?sha1=";
			String url = base + "service/local/data_index?sha1=";
			url = url + URLEncoder.encode(sha1, "UTF-8");
			JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
			Object object = unmarshaller.unmarshal(connection.getInputStream());
			if (object instanceof SearchResponse) {
				SearchResponse searchResponse = (SearchResponse) object;
				for (NexusArtifact nexusArtifact : searchResponse.getData()) {
					String groupId = nexusArtifact.getGroupId();
					String artifactId = nexusArtifact.getArtifactId();
					String version = nexusArtifact.getVersion();
					String classifier = nexusArtifact.getClassifier();
					ArtifactKey artifact = new ArtifactKey(groupId, artifactId,
							version, classifier);
					return artifact;
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private static ArtifactKey getArtifactFromJBossNexusRepository(File file) {
		String sha1;
		try {
			sha1 = getSHA1(file);
		} catch (Exception e) {
			return null;
		}
		Set<NexusRepository> nexusRepositories = SourceLookupActivator
				.getNexusRepositories();
		for (NexusRepository repository : nexusRepositories) {
			if (!repository.isEnabled()) {
				continue;
			}
			ArtifactKey key = getArtifactFromJBossNexusRepository(sha1,
					repository);
			if (key != null) {
				ArtifactKey sourcesArtifact = new ArtifactKey(
						key.getGroupId(), key.getArtifactId(),
						key.getVersion(),
						getSourcesClassifier(key.getClassifier()));
				ArtifactKey resolvedKey = getSourcesArtifactFromJBossNexusRepository(sourcesArtifact, repository);
				if (resolvedKey != null) {
					return key;
				}
			}
		}
		return null;
	}

	private static ArtifactKey getSourcesArtifactFromJBossNexusRepository(ArtifactKey key,
			NexusRepository nexusRepository) {
		if (key == null || nexusRepository == null
				|| nexusRepository.getUrl() == null) {
			return null;
		}
		HttpURLConnection connection = null;
		try {
			String base = nexusRepository.getUrl();
			if (!base.endsWith(PATH_SEPARATOR)) {
				base = base + PATH_SEPARATOR;
			}
			// String url =
			// "https://repository.jboss.org/nexus/service/local/data_index?g=groupId&a=artifactId&v=version&c=classifier";
			String url = base + "service/local/data_index?";
			url= url + "g=" + URLEncoder.encode(key.getGroupId(), "UTF-8") + "&";
			url= url + "a=" + URLEncoder.encode(key.getArtifactId(), "UTF-8") + "&";
			url= url + "v=" + URLEncoder.encode(key.getVersion(), "UTF-8") + "&";
			url= url + "c=" + URLEncoder.encode(key.getClassifier(), "UTF-8");
			JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
			Object object = unmarshaller.unmarshal(connection.getInputStream());
			if (object instanceof SearchResponse) {
				SearchResponse searchResponse = (SearchResponse) object;
				for (NexusArtifact nexusArtifact : searchResponse.getData()) {
					String groupId = nexusArtifact.getGroupId();
					String artifactId = nexusArtifact.getArtifactId();
					String version = nexusArtifact.getVersion();
					String classifier = nexusArtifact.getClassifier();
					ArtifactKey artifact = new ArtifactKey(groupId, artifactId,
							version, classifier);
					return artifact;
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
	private static String getSHA1(File file) throws IOException,
			NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		InputStream inputStream = new FileInputStream(file);
		byte[] bytes = new byte[16 * 1024];
		int count = 0;
		while ((count = inputStream.read(bytes)) != -1) {
			md.update(bytes, 0, count);
		}
		byte[] digestBytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < digestBytes.length; i++) {
			sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}

	protected static ArtifactKey getArtifactFromMetaInf(ZipFile jar)
			throws IOException {
		ZipEntry mavenEntry = jar.getEntry("META-INF/maven");//$NON-NLS-1$
		if (mavenEntry == null) {
			return null;
		}
		String entryName = mavenEntry.getName();
		Enumeration<? extends ZipEntry> zipEntries = jar.entries();
		ArtifactKey artifact = null;
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();
			if (zipEntry.getName().endsWith("pom.properties")
					&& zipEntry.getName().startsWith(entryName)) {
				Properties props = new Properties();
				props.load(jar.getInputStream(zipEntry));
				String groupId = props.getProperty("groupId");
				String artifactId = props.getProperty("artifactId");
				String version = props.getProperty("version");
				String classifier = props.getProperty("classifier");
				if (groupId != null && artifactId != null && version != null) {
					artifact = new ArtifactKey(groupId, artifactId, version,
							classifier);
					return artifact;
				}
			}
		}
		return artifact;
	}

	protected static ArtifactKey getArtifactFromM2eIndex(File file)
			throws CoreException {
		IndexManager indexManager = MavenPlugin.getIndexManager();
		IIndex index = indexManager.getAllIndexes();
		IndexedArtifactFile info = null;
		try {
			info = index.identify(file);
		} catch (Throwable e) {
			// ignore
		}
		ArtifactKey artifact = null;
		if (info != null) {
			artifact = info.getArtifactKey();
			if (artifact != null) {
				return artifact;
			}
		}
		if (indexManager instanceof NexusIndexManager) {
			NexusIndexManager nexusIndexManager = (NexusIndexManager) indexManager;
			if (globalRepositories == null) {
				initialize();
			}
			for (IRepository repository : globalRepositories) {
				NexusIndex nexusIndex = nexusIndexManager.getIndex(repository);
				if (nexusIndex != null) {
					try {
						info = nexusIndex.identify(file);
					} catch (Throwable t) {
						// ignore
					}
					if (info != null) {
						artifact = info.getArtifactKey();
						if (artifact != null) {
							return artifact;
						}
					}
				}
			}
		}
		return artifact;
	}

	public static Job downloadArtifact(File file, ArtifactKey artifact) {
		final ArtifactKey sourcesArtifact = new ArtifactKey(
				artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getVersion(),
				getSourcesClassifier(artifact.getClassifier()));
		resolvedFile = null;
		Job job = new Job("Downloading sources for " + file.getName()) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					resolvedFile = download(sourcesArtifact, monitor);
				} catch (CoreException e) {
					SourceLookupActivator.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
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

	static String getSourcesClassifier(String baseClassifier) {
		return CLASSIFIER_TESTS.equals(baseClassifier) ? CLASSIFIER_TESTSOURCES
				: CLASSIFIER_SOURCES;
	}

	public static IPath getSourcePath(ArtifactKey a) {
		File file = getAttachedArtifactFile(a,
				getSourcesClassifier(a.getClassifier()));

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
}
