package org.jboss.tools.maven.project.examples.wizard;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.Project;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;
import org.maven.ide.eclipse.wizards.MavenProjectWizardArchetypeParametersPage;

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
