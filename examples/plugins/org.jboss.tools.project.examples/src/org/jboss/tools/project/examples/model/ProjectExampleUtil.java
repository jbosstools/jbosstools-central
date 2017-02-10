/*************************************************************************************
 * Copyright (c) 2008-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;
import org.jboss.tools.project.examples.internal.Messages;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.model.ProjectExampleCategoryParser;
import org.jboss.tools.project.examples.internal.model.ProjectExampleJaxbParser;
import org.jboss.tools.project.examples.internal.model.ProjectExampleSiteParser;
import org.jboss.tools.project.examples.offline.OfflineUtil;

/**
 * @author snjeza
 * 
 */
public class ProjectExampleUtil {

	private static final String SERVER_PROJECT_EXAMPLE_XML = ".project_example.xml"; //$NON-NLS-1$

	private static final String URL = "url"; //$NON-NLS-1$

	public static final String EDITOR = "editor"; //$NON-NLS-1$

	public static final String CHEATSHEETS = "cheatsheets"; //$NON-NLS-1$

	public static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

	public static final String PROTOCOL_PLATFORM = "platform"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_XML_EXTENSION_ID = "org.jboss.tools.project.examples.projectExamplesXml"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_CATEGORIES_EXTENSION_ID = "org.jboss.tools.project.examples.categories"; //$NON-NLS-1$

	private static final int TIME_OUT = 2*1000;

	private static String URL_EXT = URL;

	private static String URL_KEY = "urlKey"; //$NON-NLS-1$
	
	private static String EXPERIMENTAL_EXT = "experimental"; //$NON-NLS-1$

	private static Set<IProjectExampleSite> pluginSites;

	private static HashSet<IProjectExampleSite> invalidSites = new HashSet<>();

	private static Set<URI> categoryUris;

	private ProjectExampleUtil() {
	}

	public static Set<IProjectExampleSite> getPluginSites() {
		if (pluginSites == null) {
			pluginSites = new HashSet<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(PROJECT_EXAMPLES_XML_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] configurationElements = extension.getConfigurationElements();
				IProjectExampleSite site = new ProjectExampleSite();
				site.setName(extension.getLabel());
		        String urlKey = null;
		        String urlValue = null;
		        for (IConfigurationElement configurationElement : configurationElements) {
		          if (URL_KEY.equals(configurationElement.getName())) {
		            urlKey = configurationElement.getValue();
		          } if (URL_EXT.equals(configurationElement.getName())) {
		            urlValue = configurationElement.getValue();
		          } else if (EXPERIMENTAL_EXT.equals(configurationElement
		              .getName())) {
		            String experimental = configurationElement.getValue();
		            site.setExperimental(Boolean.parseBoolean(experimental));
		          }
		        }
		        String urlString = urlValue;
		        if (urlKey != null) {
		          urlString = PropertiesHelper.getPropertiesProvider().getValue(urlKey, urlValue);
		        }
	        	URI url = getURI(urlString);
	        	site.setUrl(url);
				if (site.getUrl() != null) {
					pluginSites.add(site);
				}
			}
		}
		return pluginSites;
	}

	public static Set<IProjectExampleSite> getRuntimeSites() {
		return getRuntimeSites(false);
	}
	
	public static Set<IProjectExampleSite> getRuntimeSites(boolean force) {
		Set<IProjectExampleSite> sites = new HashSet<>();
		if (!force) {
			IPreferenceStore store = ProjectExamplesActivator.getDefault()
					.getPreferenceStore();
			if (!store.getBoolean(ProjectExamplesActivator.SHOW_RUNTIME_SITES)) {
				return sites;
			}
		}
		IServer[] servers = ServerCore.getServers();
		for (IServer server:servers) {
			IRuntime runtime = server.getRuntime();
			if (runtime == null) {
				continue;
			}
			IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			if (jbossRuntime == null) {
				continue;
			}
			IPath jbossLocation = runtime.getLocation();
			if (jbossRuntime.getRuntime() == null) {
				continue;
			}
			String name = jbossRuntime.getRuntime().getName() + " Project Examples"; //$NON-NLS-1$
			File serverHome = jbossLocation.toFile();
			File file = getFile(serverHome, true);
			if (file != null) {
				ProjectExampleSite site = new ProjectExampleSite();
				site.setExperimental(false);
				site.setName(name);
				site.setUrl(file.toURI());
				sites.add(site);
			}
		}
		return sites;
	}
	private static File getFile(File serverHome, boolean b) {
		if (!serverHome.isDirectory()) {
			return null;
		}
		File[] directories = serverHome.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (directories != null && directories.length > 0) {
			for (File directory:directories) {
				File projectExampleFile = new File(directory, SERVER_PROJECT_EXAMPLE_XML);
				if (projectExampleFile.isFile()) {
					return projectExampleFile;
				}
			}
		}
		return null;
	}

	public static Set<IProjectExampleSite> getUserSites() {
		Set<IProjectExampleSite> sites = new LinkedHashSet<>();
		ProjectExampleSite site = getSite(getProjectExamplesXml());
		if (site != null) {
			sites.add(site);
		}
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String sitesAsXml = store.getString(ProjectExamplesActivator.USER_SITES);
		if (sitesAsXml != null && !sitesAsXml.trim().isEmpty()) {
			try {
				Set<IProjectExampleSite> userSites = new ProjectExampleSiteParser().parse(sitesAsXml);
				if (userSites != null) {
					sites.addAll(userSites);
				}
			} catch (CoreException e) {
				ProjectExamplesActivator.log("Unable to parse user site preferences ( " + sitesAsXml+ ").\r\nException :"+e.getLocalizedMessage());
			}
		}
		return sites;
	}

	private static Set<IProjectExampleSite> getSites() {
		Set<IProjectExampleSite> sites = new HashSet<>();
		sites.addAll(getPluginSites());
		sites.addAll(getUserSites());
		sites.addAll(getRuntimeSites());
		return sites;
	}

	public static ProjectExampleSite getSite(String url) {
		if (url != null) {
			ProjectExampleSite site = new ProjectExampleSite();
			try {
				site.setUrl(new URL(url).toURI());
			} catch (MalformedURLException | URISyntaxException e) {
				ProjectExamplesActivator.log(url + " is not a valid URL "+ e.getMessage());
				return null;
			}
			site.setExperimental(true);
			site.setName(Messages.ProjectUtil_Test);
			return site;
		}
		return null;
	}

	private static URI getURI(String urlString) {
		if (urlString != null && urlString.trim().length() > 0) {
			urlString = urlString.trim();
			try {
				URI url = new URL(urlString).toURI();
				return url;
			} catch (MalformedURLException | URISyntaxException e) {
				ProjectExamplesActivator.log(e);
			}
		}
		return null;
	}

	@Deprecated
	public static List<ProjectExampleCategory> getCategories(
			IProgressMonitor monitor) {
		return getCategories(getSites(), monitor);
	}
	
	@Deprecated
	public static List<ProjectExampleCategory> getCategories(
			Set<IProjectExampleSite> sites, IProgressMonitor monitor) {
		
		monitor.setTaskName(Messages.ProjectUtil_Parsing_project_description_files);
		invalidSites.clear();
		
		Map<String, ProjectExampleCategory> categories = fetchCategories(monitor);
		
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		CompletionService<Tuple<IProjectExampleSite, List<ProjectExample>>> pool = new ExecutorCompletionService<>(service);
		try {
			boolean showExperimentalSites = ProjectExamplesActivator
					.getDefault()
					.getPreferenceStore()
					.getBoolean(
							ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES);
			
			
		    int count = 0;
			for (IProjectExampleSite site : sites) {
				if (!showExperimentalSites && site.isExperimental()) {
					continue;
				}
				if (monitor.isCanceled()) {
					invalidSites.add(site);
					continue;
				}
				
				pool.submit(new FetchProjectExampleDocumentTask(site));
				count++;
			}

			for (int k=0; k <count; k++) {
				//Handle the next finished task first
				Tuple<IProjectExampleSite, List<ProjectExample>> tuple = pool.take().get();
				IProjectExampleSite site = tuple.key;
				List<ProjectExample> examples = tuple.value;
				if (examples == null) {
					invalidSites.add(site);
					continue;
				}
				for (ProjectExample example : examples) {
					if (canBeImported(example)) {
						addToCategory(example, categories);
					}
				}
				
			}
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		} finally {
			service.shutdown();
		}

		ArrayList<ProjectExampleCategory> result = new ArrayList<>(categories.values());
		return result;
	}

  public static void addToCategory(ProjectExample example, Map<String, ProjectExampleCategory> categories) {
	  String categoryName = example.getCategory();
	  if (categoryName == null || categoryName.trim().isEmpty()) {
		  categoryName = ProjectExampleCategory.OTHER;
		  example.setCategory(categoryName);
	  }
	  ProjectExampleCategory category = categories.get(categoryName);
	  if (category == null) {
		  category = new ProjectExampleCategory(categoryName);
		  categories.put(categoryName, category);
	  }
	  category.getProjects().add(example);
	}

  public static Set<URI> getCategoryURIs() {
    if (categoryUris == null) {
      categoryUris = new HashSet<>();
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IExtensionPoint extensionPoint = registry
          .getExtensionPoint(PROJECT_EXAMPLES_CATEGORIES_EXTENSION_ID);
      IExtension[] extensions = extensionPoint.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IExtension extension = extensions[i];
        IConfigurationElement[] configurationElements = extension
            .getConfigurationElements();

        String urlKey = null;
        String urlValue = null;
        for (int j = 0; j < configurationElements.length; j++) {
          IConfigurationElement configurationElement = configurationElements[j];
          if (URL_EXT.equals(configurationElement.getName())) {
            urlValue = configurationElement.getValue();
          } else if (URL_KEY.equals(configurationElement.getName())) {
            urlKey = configurationElement.getValue();
          }
        }
        String urlString = PropertiesHelper.getPropertiesProvider().getValue(urlKey, urlValue);
        URI url = getURI(urlString);
        if (url != null) {
          categoryUris.add(url);
        }
      }

    }
    return categoryUris;
  }
	
	public static Map<String, ProjectExampleCategory> fetchCategories(IProgressMonitor monitor) {
		
		List<ProjectExampleCategory> list = new ArrayList<>();
		Set<URI> urls = getCategoryURIs();
		for (URI url:urls) {
			if (monitor.isCanceled()) {
				break;
			}
			File file = null;
			try {
				file = getProjectExamplesFile(url.toURL(),
						"categories", ".xml", monitor);//$NON-NLS-1$ //$NON-NLS-2$
			} catch (MalformedURLException e) {
				ProjectExamplesActivator.log(e);
			} 
			if (file == null || !file.exists() || !file.isFile()) {
				ProjectExamplesActivator.log(NLS.bind(
						Messages.ProjectUtil_Invalid_URL, url
								.toString()));
				continue;
			}
			List<ProjectExampleCategory> cats;
			try {
				cats = new ProjectExampleCategoryParser().parse(file);
				list.addAll(cats);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(list);
		Map<String, ProjectExampleCategory> categories = new LinkedHashMap<>();
		for (ProjectExampleCategory c : list) {
			categories.put(c.getName(), c);
		}
		return categories;
	}

	private static String getProjectExamplesXml() {
		String projectXML = System
				.getProperty("org.jboss.tools.project.examples.xml"); //$NON-NLS-1$
		if (projectXML != null && projectXML.length() > 0) {
			return projectXML;
		}
		return null;
	}

	public static File getProjectExamplesFile(URL url, String prefix,
			String suffix, IProgressMonitor monitor) {
		
		File file = null;
		if (PROTOCOL_FILE.equals(url.getProtocol())
				|| PROTOCOL_PLATFORM.equalsIgnoreCase(url.getProtocol())) {
			try {
				// assume all illegal characters have been properly encoded, so
				// use URI class to unencode
				file = new File(new URI(url.toExternalForm()));
			} catch (Exception e) {
				// URL contains unencoded characters
				file = new File(url.getFile());
			}
			if (!file.exists())
				return null;
		} else {
			
			if (OfflineUtil.isOfflineEnabled()) {
				return OfflineUtil.getOfflineFile(url);
			}
			if (monitor.isCanceled()) {
				return null;
			}						
			file = getCacheFile(url);
			long urlModified = getRemoteTimestamp(url);
			if( canReuseCache(file, url, urlModified)) {
				return file;
			}

			file.getParentFile().mkdirs();
			if (monitor.isCanceled()) {
				return null;
			}
			
			file = downloadExamplesFile(file, prefix, url, urlModified, monitor);
		}
		return file;
	}
	
	private static File downloadExamplesFile(File finalDestination, String prefix, URL url, long urlModified, IProgressMonitor monitor){ 
		File tmpFile = getTemporaryFile(url);
		if( tmpFile.exists()) {
			tmpFile.delete();
		}
		try {
			BufferedOutputStream destination = new BufferedOutputStream(
					new FileOutputStream(tmpFile));
			IStatus result = new URLTransportUtility().download(prefix,
					url.toExternalForm(), destination, TIME_OUT, monitor);
			if (!result.isOK()) {
				// The download failed. Cleanup the tmp file and try to return the last download
				ProjectExamplesActivator.getDefault().getLog().log(result);
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
				
				if( finalDestination.exists()) {
					return finalDestination;
				}
				return null;
			} else {
				// Download was a success
				// renameTo will fail on some OS if a file is hogging the new destination spot
				File cacheBackup = getFile(url.getFile() + ".tmp2");
				if( cacheBackup.exists() )
					cacheBackup.delete();
				
				if( finalDestination.exists()) {
					boolean bak = finalDestination.renameTo(cacheBackup);
					if( !bak ) {
						// backup failed... odds are similar renameTo will fail and we don't want to lose our existing cache
						// So let's just return the tmpFile we downloaded
						cacheBackup.delete();
						tmpFile.setLastModified(urlModified);
						return tmpFile;
					}
					// cache backup succeeded, so we can delete our final destination here 
					// and rename our newly dl'd file to this final destination
					finalDestination.delete();
				}
				boolean renameSuccess = tmpFile.renameTo(finalDestination);
				File retval = null;
				if( renameSuccess ) {
					// the new DL'd file is in final destination, so cache backup is useless now
					cacheBackup.delete();
					retval = finalDestination;
				} else { 
					// the rename failed... but we still have a new successful DL, so return that
					retval = tmpFile;
				}
				retval.setLastModified(urlModified);
				return retval;
			}
		} catch(FileNotFoundException fnfe) {
			ProjectExamplesActivator.log(fnfe);
			return null;
		}
	}
	
	private static long getRemoteTimestamp(URL url) {
		long urlModified = -1;
		try {
			urlModified = new URLTransportUtility().getLastModified(url);
		} catch (CoreException e) {
			// Ignore this and just return the -1 
		}
		return urlModified;
	}
	private static boolean canReuseCache(File file, URL url, long urlModified) {
		if( urlModified == -1 && file.exists()) {
			return true;
		}
		//!!! urlModified == 0 when querying files from github 
		//It means that files from github can not be cached! 
		if (file.exists()) {
			long modified = file.lastModified();
			if (modified > 0 && //file already exists and doesn't come from github (or other server sending lastmodified = 0) 
					(urlModified == 0 //and now there is a problem downloading the file
					|| 
					urlModified == modified)) {//or the file hasn't changed
				return true;
			}
			//Attention fugly hack following this, please close your eyes :-/
			//if .GA.zip or .Final.zip from github, assume cache can be safely reused
			if (modified == 0 && urlModified==modified && (file.getName().endsWith("GA.zip") || file.getName().endsWith("Final.zip"))) {
				return true;
			}
		}
		return false;
	}

	private static File getCacheFile(URL url) {
		return getFile(url.getFile());
	}

	private static File getTemporaryFile(URL url) {
		return getFile(url.getFile() + ".tmp");
	}

	private static File getFile(String name) {
		IPath location = ProjectExamplesActivator.getDefault().getStateLocation();
		File root = location.toFile();
		File file = new File(root, name);
		return file;

	}
	
	public static String getAsXML(Set<IProjectExampleSite> sites) throws CoreException {
		return new ProjectExampleSiteParser().serialize(sites);			
	}

	public static HashSet<IProjectExampleSite> getInvalidSites() {
		return invalidSites;
	}

	public static List<ProjectExample> getProjectsByTags(
			Collection<ProjectExampleCategory> categories, String... tags) {
		if (categories == null) {
			return null;
		}
		List<ProjectExample> selection = new ArrayList<>();
		for (ProjectExampleCategory c : categories) {
			for (ProjectExample p : c.getProjects()) {
				if (p.hasTags(tags) && !selection.contains(p)) {
					selection.add(p);
				}
			}
		}
		return selection;
	}
	
	private static boolean canBeImported(ProjectExample project) {
		return ProjectExamplesActivator.getDefault()
				.getImportProjectExample(project.getImportType()) != null;
	}
	
	private static class Tuple<X, Y> {
		
		X key;
		Y value;

		public Tuple(X key) {
			this.key = key;
		}
		
	}
	
	private static class FetchProjectExampleDocumentTask implements Callable<Tuple<IProjectExampleSite,  List<ProjectExample>>> {

		Tuple<IProjectExampleSite, List<ProjectExample>> tuple; 
		
		public FetchProjectExampleDocumentTask(IProjectExampleSite site) {
			 tuple = new Tuple<>(site);
		}

		@Override
		public Tuple<IProjectExampleSite,  List<ProjectExample>> call() throws Exception {
			URI uri = tuple.key.getUrl();
			File file = getProjectExamplesFile(uri.toURL(), "projectExamples", ".xml", new NullProgressMonitor());  //$NON-NLS-1$ //$NON-NLS-2$
			if(file == null || !file.exists() || !file.isFile()) {
				ProjectExamplesActivator.log(NLS.bind(Messages.ProjectUtil_Invalid_URL, uri.toString()));
				return tuple;
			}
			try {
				ProjectExampleJaxbParser parser = new ProjectExampleJaxbParser();
				List<ProjectExample> examples = parser.parse(file);
				if (examples != null) {
					for (ProjectExample project : examples) {
						project.setSite(tuple.key);
					}
					tuple.value = examples; 
				}
				
			} catch (Exception e) {
				tuple.value = null;
				ProjectExamplesActivator.log(e);
			}
			return tuple;
		}

	}

	public static void setProjectExamplesFile(ProjectExample project, File file) {
		if (project != null) {
			project.setFile(file);		
		}
	}
	
	@SuppressWarnings("nls")
	public static IPath getDefaultExamplesDirectory() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String baseName = "examples";
		IPath dir = Platform.getLocation().append(baseName);
		for (int i = 2; root.getContainerForLocation(dir) != null; i++) {
		 dir = Platform.getLocation().append(baseName + "_"+ i);
		}
		return dir;
	}
}
