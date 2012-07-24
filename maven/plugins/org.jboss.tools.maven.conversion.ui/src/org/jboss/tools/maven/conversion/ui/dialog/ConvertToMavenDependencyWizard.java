package org.jboss.tools.maven.conversion.ui.dialog;

import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.wizard.Wizard;

public class ConvertToMavenDependencyWizard extends Wizard {

	private IProject project;
	private Set<IClasspathEntry> entries;


	IdentifyMavenDependencyPage identificationPage;
	private List<Dependency> dependencies;
	
	public ConvertToMavenDependencyWizard(IProject project, Set<IClasspathEntry> entries) {
		this.project = project;
		this.entries = entries;
		String title = "Convert to Maven ";
		if (entries.size() > 1) {
			title += "Dependencies";
		} else {
			title += "Dependency";
		}
		setWindowTitle(title);
	}


	@Override
	public void addPages() {
		identificationPage = new IdentifyMavenDependencyPage(project, entries);
		addPage(identificationPage);
		//DependencyConversionPreviewPage page2 = new DependencyConversionPreviewPage("Foo");
		//addPage(page2);
	}
	
	
	@Override
	public boolean performFinish() {
		if (identificationPage != null) {
			dependencies = identificationPage.getDependencies(); 
		}
		return true;
	}


	public List<Dependency> getDependencies() {
		return dependencies;
	}

	
}
