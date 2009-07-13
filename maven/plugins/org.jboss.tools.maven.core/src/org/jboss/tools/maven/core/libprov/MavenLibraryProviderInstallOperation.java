package org.jboss.tools.maven.core.libprov;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperation;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.util.PomResourceImpl;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.embedder.MavenModelManager;

public class MavenLibraryProviderInstallOperation extends
		LibraryProviderOperation {

	@Override
	public void execute(LibraryProviderOperationConfig config,
			IProgressMonitor monitor) throws CoreException {
		IFacetedProjectBase facetedProject = config.getFacetedProject();
		IProject project = facetedProject.getProject();
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		MavenLibraryProviderInstallOperationConfig mavenConfig = (MavenLibraryProviderInstallOperationConfig) config;
		if (pom.exists()) {
			// JBoss Maven Integration facet has been executed		
			MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
			PomResourceImpl resource = modelManager.loadResource(pom);
			Model projectModel = resource.getModel();
			Model libraryModel = mavenConfig.getModel();
			MavenCoreActivator.mergeModel(projectModel, libraryModel);
			try {
				Map<String,String> options = new HashMap<String,String>();
				options.put(XMIResource.OPTION_ENCODING, MavenCoreActivator.ENCODING);
				resource.save(options);
			} catch (IOException e) {
				MavenCoreActivator.log(e);
			} finally {
				resource.unload();
			}
			ILibraryProvider provider = config.getLibraryProvider();
			File providerFile = MavenCoreActivator.getProviderFile(provider);
			URL url = null;
			PomResourceImpl libraryResource = null;
			URIConverter.WriteableOutputStream uws = null;
			FileWriter fw = null;
			try {
				if (providerFile.exists()) {
					url = providerFile.toURL();
				} else {
					Map<String, String> params = provider.getParams();
					String pomURLString = params.get("template"); //$NON-NLS-1$
					URL platformURL = new URL(pomURLString);
					url = FileLocator.resolve(platformURL);
				}
				libraryResource = MavenCoreActivator.loadResource(url);
				libraryResource.getContents().clear();
				libraryResource.getContents().add(libraryModel);

				Map<String, String> options = new HashMap<String, String>();
				options.put(XMIResource.OPTION_ENCODING,
						MavenCoreActivator.ENCODING);
				fw = new FileWriter(providerFile);
				uws = new URIConverter.WriteableOutputStream(
						fw, MavenCoreActivator.ENCODING);
				libraryResource.save(uws, options);
			} catch (IOException e) {
				MavenCoreActivator.log(e);
			} finally {
				if (uws != null) {
					try {
						uws.flush();
						uws.close();
					} catch (IOException ignore) {}
				}
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException ignore) {}
				}
				if (libraryResource != null) {
					libraryResource.unload();
				}
			}
			
		} else {
			MavenCoreActivator.addLibraryProviderOperationConfig(config);
		}
		
	}

	
}
