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
package org.jboss.tools.project.examples.model;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.model.XmlUnMarshallers.StringToListUnMarshaller;
import org.jboss.tools.project.examples.internal.model.XmlUnMarshallers.StringToSetUnMarshaller;

/**
 * @author snjeza
 * 
 */
@XmlRootElement(name = "project")
@XmlAccessorType (XmlAccessType.FIELD)
public class ProjectExample implements ProjectModelElement,
		Comparable<ProjectExample> {
	private static final String SEP = "/"; //$NON-NLS-1$
	private static String[] PREFIXES = { "file:", "http:", "https:", "ftp:" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	public static final String IMPORT_TYPE_ZIP = "zip"; //$NON-NLS-1$
	//name acts as example id
	private String id;
	
	private String name;
	//headline used in project wizards
	private String headLine;
	//short description used in generic project-example wizard
	private String shortDescription;
	private String description;
	private String url;
	private long size;
	private String category = ProjectExampleCategory.OTHER;
	@XmlElement(name="included-projects")
	@XmlJavaTypeAdapter(StringToListUnMarshaller.class)
	private List<String> includedProjects;
	private boolean welcome;
	private String type;
	private String welcomeURL;
	private boolean welcomeFixRequired = true;
	@XmlElementWrapper(name = "fixes")
	@XmlElement(name = "fix")
	private List<RequirementModel> requirements;
	private String perspectiveId;
	private String importType;
	private String importTypeDescription;
	@XmlElement(name="mavenArchetype")
	private ArchetypeModel archetypeModel;
	private File file;
	
	@XmlTransient
	private IProjectExampleSite site;
	
	@XmlElement(name="defaultMavenProfiles")
	private String defaultProfiles = ""; //$NON-NLS-1$
	private int priority;

	@XmlJavaTypeAdapter(StringToSetUnMarshaller.class)
	private Set<String> tags;

	@XmlJavaTypeAdapter(StringToSetUnMarshaller.class)
	private Set<String> importFilter;
	
	@XmlJavaTypeAdapter(StringToSetUnMarshaller.class)
	private Set<String> essentialEnterpriseDependencies;
	private String iconPath;
	private String sourceLocation;
	private String stacksId;
	private String stacksType;
	public ProjectExample() {
		name = ""; //$NON-NLS-1$
		shortDescription = ""; //$NON-NLS-1$
		description = ""; //$NON-NLS-1$
		url = ""; //$NON-NLS-1$
		welcome = false;
		perspectiveId = null;
		importType = IMPORT_TYPE_ZIP;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		if (shortDescription == null) {
			shortDescription = name;
		}
		return shortDescription;
	}

	void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		if (url == null) {
			return url;
		}
		url = url.trim();
		for (String prefix : PREFIXES) {
			if (url.startsWith(prefix)) {
				return url;
			}
		}
		if (site == null) {
			return url;
		}
		URI siteURL = site.getUrl();
		if (siteURL == null) {
			return url;
		}
		String urlString = siteURL.toString();
		if (urlString.endsWith(SEP)) {
			urlString = urlString.substring(0, urlString.length() - 1);
		} else {
			int index = urlString.lastIndexOf(SEP);
			if (index > 0) {
				urlString = urlString.substring(0, index);
			}
		}
		if (url.startsWith(SEP)) {
			return urlString + url;
		}
		return urlString + SEP + url;
	}

	void setUrl(String url) {
		this.url = url;
	}

	public long getSize() {
		return size;
	}

	void setSize(long size) {
		this.size = size;
	}

	public String getCategory() {
		return category;
	}

	void setCategory(String category) {
		this.category = category;
	}

	public String getSizeAsText() {
		String sizeString = ""; //$NON-NLS-1$
		BigDecimal sizeDecimal = new BigDecimal(size);
		BigDecimal MB = new BigDecimal(1024 * 1024);
		BigDecimal KB = new BigDecimal(1024);
		if (sizeDecimal.compareTo(MB) > 0) {
			sizeString = String.format(Locale.US,"%5.2fMB", sizeDecimal.divide(MB)); //$NON-NLS-1$
		} else if (sizeDecimal.compareTo(KB) > 0) {
			sizeString = String.format(Locale.US,"%5.2fKB", sizeDecimal.divide(KB)); //$NON-NLS-1$
		} else {
			sizeString = String.format("%db", size); //$NON-NLS-1$
		}
		return sizeString;
	}

	public List<String> getIncludedProjects() {
		return includedProjects;
	}

	void setIncludedProjects(List<String> includedProjects) {
		this.includedProjects = includedProjects;
	}

	public boolean isWelcome() {
		return welcome;
	}

	void setWelcome(boolean welcome) {
		this.welcome = welcome;
	}

	public String getType() {
		return type;
	}

	void setType(String type) {
		this.type = type;
	}

	public String getWelcomeURL() {
		return (welcomeURL == null) ? "" : welcomeURL;
	}

	void setWelcomeURL(String welcomeURL) {
		this.welcomeURL = welcomeURL;
	}

	public IProjectExampleSite getSite() {
		return site;
	}

	void setSite(IProjectExampleSite site) {
		this.site = site;
	}

	public List<RequirementModel> getRequirements() {
		if (requirements == null) {
			requirements = new ArrayList<>();
		}
		return requirements;
	}

	public void setRequirements(List<RequirementModel> requirements) {
		this.requirements = requirements;
	}

	public String getPerspectiveId() {
		return perspectiveId;
	}

	void setPerspectiveId(String perspectiveId) {
		this.perspectiveId = perspectiveId;
	}

	public String getImportType() {
		return importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}

	public String getImportTypeDescription() {
		return importTypeDescription;
	}

	void setImportTypeDescription(String importTypeDescription) {
		this.importTypeDescription = importTypeDescription;
	}

	void setArchetypeModel(ArchetypeModel archetypeModel) {
		this.archetypeModel = archetypeModel;
	}
	
	public ArchetypeModel getArchetypeModel() {
		if (archetypeModel == null) {
			archetypeModel = new ArchetypeModel();
		}
		return archetypeModel;
	}

	public boolean isURLRequired() {
		return !ProjectExamplesActivator.MAVEN_ARCHETYPE.equals(importType);
	}

	public File getFile() {
		return file;
	}

	void setFile(File file) {
		this.file = file;
	}

	public String getDefaultProfiles() {
		return defaultProfiles;
	}

	void setDefaultProfiles(String defaultProfiles) {
		this.defaultProfiles = defaultProfiles;
	}

	@Override
	public String toString() {
		return getName();
	}

	void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public Set<String> getTags() {
		if (tags == null) {
			tags = new HashSet<String>();
		}
		return tags;
	}
	
	void setImportFilter(Set<String> importFilter) {
		this.importFilter = importFilter;
	}

	public Set<String> getImportFilter() {
		if (importFilter == null) {
			importFilter = new HashSet<String>();
		}
		return importFilter;
	}

	public boolean hasTags(String... tags) {
		if (!getTags().isEmpty() && tags != null && tags.length > 0) {
			for (String tag : tags) {
				if (!getTags().contains(tag)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	void setIconPath(String path) {
		this.iconPath = path;
	}

	public String getIconPath() {
		return iconPath;
	}

	public int getPriority() {
		return priority;
	}

	void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(ProjectExample o) {
		if (o == null) {
			return -1;
		}
		int other = o.getPriority();
		if (other < this.priority)
			return 1;
		else if (other > this.priority)
			return -1;
		if (name == null) {
			return -1;
		}
		return name.compareTo(o.getName());
	}
	

	void setEssentialEnterpriseDependencyGavs(Set<String> gavs) {
	  essentialEnterpriseDependencies = gavs;
	}
	

	public Set<String> getEssentialEnterpriseDependencyGavs() {
	  return essentialEnterpriseDependencies;
	}

	public boolean isWelcomeFixRequired() {
		return welcomeFixRequired;
	}

	void setWelcomeFixRequired(boolean welcomeFixRequired) {
		this.welcomeFixRequired = welcomeFixRequired;
	}

	public String getSourceLocation() {
		return sourceLocation;
	}

	void setSourceLocation(String sourceLocation) {
		this.sourceLocation = sourceLocation;
	}
	
	public String getStacksId() {
		return stacksId;
	}

	void setStacksId(String stacksId) {
		this.stacksId = stacksId;
	}

	public String getHeadLine() {
		if (headLine == null) {
			headLine = getShortDescription(); 
		}
		return headLine;
	}

	void setHeadLine(String headLine) {
		this.headLine = headLine;
	}

	/**
	 * @since 1.5.3
	 */
	void setStacksType(String stacksType) {
		this.stacksType = stacksType;
	}

	/**
	 * @since 1.5.3
	 */
	public String getStacksType() {
		return stacksType;
	}

	public String getId() {
		//archetype model is a maven concept, shouldn't be available here
		ArchetypeModel model = getArchetypeModel();
		//need to introduce stinky coupling to maven type here
		if (model != null && "mavenArchetype".equals(getImportType()) && model.getArchetypeArtifactId() != null) {
			return model.getGAV();
		}
		if (id == null) {
			id = name;
		}
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

}
