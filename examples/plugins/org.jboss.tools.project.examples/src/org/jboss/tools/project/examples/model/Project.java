/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

/**
 * @author snjeza
 * 
 */
public class Project implements ProjectModelElement {

	private static final String SEP = "/"; //$NON-NLS-1$
	private static String[] PREFIXES = { "file:" , "http:", "https:" , "ftp:" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private String name;
	private String shortDescription;
	private String description;
	private String url;
	private long size;
	private Category category;
	private List<String> includedProjects;
	private boolean welcome;
	private String type;
	private String welcomeURL;
	private List<ProjectFix> fixes = new ArrayList<ProjectFix>();
	private List<ProjectFix> unsatisfiedFixes;
	private String perspectiveId;
	private String importType;
	private String importTypeDescription;
	private ArchetypeModel  archetypeModel = new ArchetypeModel();
	private File file;
	private IProjectExampleSite site;
	
	public Project() {
		name=""; //$NON-NLS-1$
		shortDescription=""; //$NON-NLS-1$
		description=""; //$NON-NLS-1$
		url=""; //$NON-NLS-1$
		welcome=false;
		perspectiveId = null;
		importType = null;
		setCategory(Category.OTHER);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
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
		for (String prefix:PREFIXES) {
			if (url.startsWith(prefix)) {
				return url;
			}
		}
		if (site == null) {
			return url;
		}
		URL siteURL = site.getUrl();
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

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getSizeAsText() {
		String sizeString = ""; //$NON-NLS-1$
		BigDecimal sizeDecimal = new BigDecimal(size);
		BigDecimal MB = new BigDecimal(1024*1024);
		BigDecimal KB = new BigDecimal(1024);
		if (sizeDecimal.compareTo(MB) > 0) {
			sizeString = String.format("%5.2fM", sizeDecimal.divide(MB)); //$NON-NLS-1$
		} else if (sizeDecimal.compareTo(KB) > 0) {
			sizeString = String.format("%5.2fK", sizeDecimal.divide(KB)); //$NON-NLS-1$
		} else {
			sizeString = String.format("%d", size); //$NON-NLS-1$
		}
		return sizeString;
	}

	public List<String> getIncludedProjects() {
		return includedProjects;
	}

	public void setIncludedProjects(List<String> includedProjects) {
		this.includedProjects = includedProjects;
	}

	public boolean isWelcome() {
		return welcome;
	}

	public void setWelcome(boolean welcome) {
		this.welcome = welcome;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getWelcomeURL() {
		return welcomeURL;
	}

	public void setWelcomeURL(String welcomeURL) {
		this.welcomeURL = welcomeURL;
	}

	public IProjectExampleSite getSite() {
		/*if (site == null) {
			if (getUrl().startsWith("http://anonsvn.jboss.org")) { //$NON-NLS-1$
				site = Messages.Project_JBoss_Tools_Team_from_jboss_org;
			} else if (getUrl().startsWith("file:")) { //$NON-NLS-1$
				site = Messages.Project_Local;
			} else {
				site = Messages.Project_Unknown;
			}
		}*/
		return site;
	}

	public void setSite(IProjectExampleSite site) {
		this.site = site;
	}

	public List<ProjectFix> getFixes() {
		return fixes;
	}

	public void setFixes(List<ProjectFix> fixes) {
		this.fixes = fixes;
	}

	public List<ProjectFix> getUnsatisfiedFixes() {
		return unsatisfiedFixes;
	}

	public void setUnsatisfiedFixes(List<ProjectFix> unsatisfiedFixes) {
		this.unsatisfiedFixes = unsatisfiedFixes;
	}

	public String getPerspectiveId() {
		return perspectiveId;
	}

	public void setPerspectiveId(String perspectiveId) {
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

	public void setImportTypeDescription(String importTypeDescription) {
		this.importTypeDescription = importTypeDescription;
	}

	public ArchetypeModel getArchetypeModel() {
		return archetypeModel;
	}
	
	public boolean isURLRequired() {
		return !ProjectExamplesActivator.MAVEN_ARCHETYPE.equals(importType);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
