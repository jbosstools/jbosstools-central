package org.jboss.tools.maven.core.libprov;

import org.eclipse.emf.ecore.resource.URIConverter.WriteableOutputStream;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderInstallOperationConfig;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.util.PomResourceImpl;

public class MavenLibraryProviderInstallOperationConfig extends
		LibraryProviderInstallOperationConfig {

	private Model model;

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
	
}
