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
package org.jboss.tools.maven.project.examples.wizard;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.Project;

/**
 * 
 * @author snjeza
 *
 */
public class ArchetypeExamplesWizardPage extends
		MavenProjectWizardArchetypeParametersPage {

	private Project projectDescription;

	public ArchetypeExamplesWizardPage(
			ProjectImportConfiguration configuration, Project projectDescription) {
		super(configuration);
		this.projectDescription = projectDescription;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Archetype archetype = new Archetype();
		ArchetypeModel archetypeModel = projectDescription.getArchetypeModel();

		final String groupId = archetypeModel.getGroupId();
		final String artifactId = archetypeModel.getArtifactId();
		final String version = archetypeModel.getVersion();
		final String javaPackage = archetypeModel.getJavaPackage();

		archetype.setGroupId(archetypeModel.getArchetypeGroupId());
		archetype.setArtifactId(archetypeModel.getArchetypeArtifactId());
		archetype.setVersion(archetypeModel.getArchetypeVersion());
		archetype.setRepository(archetypeModel.getArchetypeRepository());
		archetype.setProperties(archetypeModel.getArchetypeProperties());
		setArchetype(archetype);
		groupIdCombo.setText(groupId);
		artifactIdCombo.setText(artifactId);
		versionCombo.setText(version);
		packageCombo.setText(javaPackage);
	}

	public Archetype getArchetype() {
		return archetype;
	}

}
