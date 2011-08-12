package org.jboss.tools.maven.jbosspackaging.configurators;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class JBossPackagingPluginConfiguration {

	private static final String DEFAULT_LIB_DIRECTORY = "/lib";

	protected final MavenProject mavenProject;

	protected Set<String> excludes = null;
	
	protected Boolean excludeAll;
	
	protected Boolean removeDependencyVersions;

	protected Xpp3Dom configuration;
	
	protected String libDirectory;
	
	public JBossPackagingPluginConfiguration(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
		Plugin plugin = mavenProject.getPlugin("org.codehaus.mojo:jboss-packaging-maven-plugin");
		if (plugin != null) {
			configuration =(Xpp3Dom) plugin.getConfiguration(); 
		}
	}

	
	public String getLibDirectory() {
		if (libDirectory == null) {
			if (configuration != null) {
				Xpp3Dom libDom = configuration.getChild("libDirectory");
				if(libDom != null) {
					libDirectory = libDom.getValue();
				}
			}
			if (StringUtils.isEmpty(libDirectory)) {
				libDirectory = DEFAULT_LIB_DIRECTORY;
			}
		}
		return libDirectory;
	}
	
	public boolean isExcludeAll() {
		if (excludeAll == null) {
			if (configuration != null) {
				Xpp3Dom excludeAllDom = configuration.getChild("excludeAll");
				if(excludeAllDom != null) {
					excludeAll = Boolean.valueOf(excludeAllDom.getValue());
				} 
			}
			if (excludeAll == null) {
				excludeAll = Boolean.FALSE;
			}
		}
		return excludeAll.booleanValue();
	}
	
	public boolean isRemoveDependencyVersions() {
		if (removeDependencyVersions == null) {
			if (configuration != null) {
				Xpp3Dom removeVersionDom = configuration.getChild("removeDependencyVersions");
				if(removeVersionDom != null) {
					removeDependencyVersions = Boolean.valueOf(removeVersionDom.getValue());
				} 
			}
			if (removeDependencyVersions == null) {
				removeDependencyVersions = Boolean.FALSE;
			}
		}
		return removeDependencyVersions.booleanValue();
	}
	
	public boolean isExcluded(Artifact artifact) {
		if (isExcludeAll() || !artifact.getArtifactHandler().isAddedToClasspath()) {
			return true;
		}
		if (getExcludes() != null) {
			for (String groupIdArtifactId : excludes) {
				if (groupIdArtifactId.equals(artifact.getGroupId()+":"+artifact.getArtifactId())) {
					return true;
				}
			}
		}
		return false;
	}

	private Set<String> getExcludes() {
		if (excludes == null) {
			excludes = new HashSet<String>();
			if (configuration != null) {
				Xpp3Dom excludesDom = configuration.getChild("excludes");
				if(excludesDom != null) {
					for (Xpp3Dom excludeDom : excludesDom.getChildren("exclude")) {
						if (excludeDom != null) {
							String exclude = excludeDom.getValue();
							if (StringUtils.isNotEmpty(exclude)) {
								excludes.add(exclude);
							}
						}
					}
				} 
			}
		}
		return excludes;
	}


	public String mapFileName(Artifact artifact) {
			StringBuilder fileName = new StringBuilder(artifact.getArtifactId());
			String classifier = artifact.getClassifier();
			if (classifier != null) {
				fileName.append("-").append(classifier);
			}
			if (!isRemoveDependencyVersions()) {
				fileName.append("-").append(artifact.getVersion());
			}
			fileName.append(".").append(artifact.getArtifactHandler().getExtension());
			return fileName.toString();
	}

}
