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

/**
 * Implementation of a ProjectExample designed to be mutable
 * 
 * @author Fred Bricon
 *
 */
public class ProjectExampleWorkingCopy extends ProjectExample {

	private List<ProjectFix> unsatisfiedFixes;

	public ProjectExampleWorkingCopy() {
		super();
	}
	
	public ProjectExampleWorkingCopy(ProjectExample projectExample) {
		this();
		setCategory(projectExample.getCategory());
		setDefaultProfiles(projectExample.getDefaultProfiles());
		setDescription(projectExample.getDescription());
		if (projectExample.getEssentialEnterpriseDependencyGavs() != null) {
		  setEssentialEnterpriseDependencyGavs(new LinkedHashSet<String>(projectExample.getEssentialEnterpriseDependencyGavs()));
		}
		setFile(projectExample.getFile());
		if (projectExample.getFixes() != null) {
		  setFixes(new ArrayList<ProjectFix>(projectExample.getFixes()));
		}
		setHeadLine(projectExample.getHeadLine());
		setIconPath(projectExample.getIconPath());
		setImportType(projectExample.getImportType());
		setImportTypeDescription(projectExample.getImportTypeDescription());
		if (projectExample.getIncludedProjects() != null) {
			setIncludedProjects(new ArrayList<String>(projectExample.getIncludedProjects()));
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
			setTags(new LinkedHashSet<String>(projectExample.getTags()));
		}
		setType(projectExample.getType());
		if (projectExample.getUnsatisfiedFixes() != null) {
			setUnsatisfiedFixes(projectExample.getUnsatisfiedFixes());
		}
		setUrl(projectExample.getUrl());
		setWelcome(projectExample.isWelcome());
		setWelcomeFixRequired(projectExample.isWelcomeFixRequired());
		setWelcomeURL(projectExample.getWelcomeURL());
		
		if (projectExample.getArchetypeModel() != null) {
			try {
					setArchetypeModel((ArchetypeModel)projectExample.getArchetypeModel().clone());
			} catch (CloneNotSupportedException e) {
				//there's *no* chance this can happen 
				throw new RuntimeException("Error cloning archetypeModel", e); //$NON-NLS-1$
			}
		}
	}

	public List<ProjectFix> getUnsatisfiedFixes() {
		return unsatisfiedFixes;
	}

	public void setUnsatisfiedFixes(List<ProjectFix> unsatisfiedFixes) {
		this.unsatisfiedFixes = unsatisfiedFixes;
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
}
