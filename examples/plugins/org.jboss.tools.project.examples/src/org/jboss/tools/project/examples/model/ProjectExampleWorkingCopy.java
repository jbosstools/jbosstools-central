/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;

/**
 * Implementation of a ProjectExample designed to be mutable
 * 
 * @author Fred Bricon
 *
 */
public class ProjectExampleWorkingCopy extends ProjectExample {

	private List<IProjectExamplesFix> fixes;

	public ProjectExampleWorkingCopy() {
		super();
	}
	
	public ProjectExampleWorkingCopy(ProjectExample projectExample) {
		this();
		setId(projectExample.getId());
		setCategory(projectExample.getCategory());
		setDefaultProfiles(projectExample.getDefaultProfiles());
		setDescription(projectExample.getDescription());
		if (projectExample.getEssentialEnterpriseDependencyGavs() != null) {
		  setEssentialEnterpriseDependencyGavs(new LinkedHashSet<>(projectExample.getEssentialEnterpriseDependencyGavs()));
		}
		setFile(projectExample.getFile());
		if (projectExample.getRequirements() != null) {
		  setRequirements(new ArrayList<>(projectExample.getRequirements()));
		}
		setHeadLine(projectExample.getHeadLine());
		setIconPath(projectExample.getIconPath());
		setImportType(projectExample.getImportType());
		setImportTypeDescription(projectExample.getImportTypeDescription());
		if (projectExample.getIncludedProjects() != null) {
			setIncludedProjects(new ArrayList<>(projectExample.getIncludedProjects()));
		}
		setName(projectExample.getName());
		setPerspectiveId(projectExample.getPerspectiveId());
		setPriority(projectExample.getPriority());
		setShortDescription(projectExample.getShortDescription());
		setSite(projectExample.getSite());
		setSize(projectExample.getSize());
		setSourceLocation(projectExample.getSourceLocation());
		setStacksId(projectExample.getStacksId());
		setStacksType(projectExample.getStacksType());
		if (projectExample.getTags() != null) {
			setTags(new LinkedHashSet<>(projectExample.getTags()));
		}
		setType(projectExample.getType());
		setUrl(projectExample.getUrl());
		setWelcome(projectExample.isWelcome());
		setWelcomeFixRequired(projectExample.isWelcomeFixRequired());
		setWelcomeURL(projectExample.getWelcomeURL());
		setImportFilter(projectExample.getImportFilter());
		if (projectExample.getArchetypeModel() != null) {
			try {
					setArchetypeModel((ArchetypeModel)projectExample.getArchetypeModel().clone());
			} catch (CloneNotSupportedException e) {
				//there's *no* chance this can happen 
				throw new RuntimeException("Error cloning archetypeModel", e); //$NON-NLS-1$
			}
		}
		setVersion(projectExample.getVersion());
	}

	public List<IProjectExamplesFix> getFixes() {
		if (fixes == null) {
			fixes = new ArrayList<>(0);
		}
		return fixes;
	}

	public void setFixes(List<IProjectExamplesFix> fixes) {
		this.fixes = fixes;
	}
	
	@Override
	public void setWelcome(boolean welcome) {
		super.setWelcome(welcome);
	}

	@Override
	public void setWelcomeFixRequired(boolean welcomeFixRequired) {
		super.setWelcomeFixRequired(welcomeFixRequired);
	}
	
	@Override
	public void setType(String type) {
		super.setType(type);
	}
	
	@Override
	public void setIncludedProjects(List<String> includedProjects) {
		super.setIncludedProjects(includedProjects);
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
	}
	
	@Override
	public void setUrl(String url) {
		super.setUrl(url);
	}
	
	@Override
	public void setEssentialEnterpriseDependencyGavs(Set<String> gavs) {
		super.setEssentialEnterpriseDependencyGavs(gavs);
	}
	
	@Override
	public void setImportType(String importType) {
		super.setImportType(importType);
	}
	
	@Override
	public void setArchetypeModel(ArchetypeModel archetypeModel) {
		super.setArchetypeModel(archetypeModel);
	}

	public void setId(String exampleId) {
		super.setId(exampleId);
	}

	@Override
	public void setHeadLine(String headline) {
		super.setHeadLine(headline);
	}
	
	@Override
	public void setWelcomeURL(String welcomeURL) {
		super.setWelcomeURL(welcomeURL);
	}
	
	@Override
	public void setImportFilter(Set<String> importFilter) {
		super.setImportFilter(importFilter);
	}
	
	@Override
	public void setTags(Set<String> tags) {
		super.setTags(tags);
	}
}
