/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperation;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.model.edit.pom.Model;
import org.eclipse.m2e.model.edit.pom.util.PomResourceFactoryImpl;
import org.eclipse.m2e.model.edit.pom.util.PomResourceImpl;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
import org.jboss.tools.maven.core.MavenCoreActivator;

/**
 * @author snjeza
 * 
 */
public class MavenLibraryProviderInstallOperation extends
		LibraryProviderOperation {

	@Override
	public void execute(LibraryProviderOperationConfig config,
			IProgressMonitor monitor) throws CoreException {
		IFacetedProjectBase facetedProject = config.getFacetedProject();
		IProject project = facetedProject.getProject();
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		MavenLibraryProviderInstallOperationConfig mavenConfig = (MavenLibraryProviderInstallOperationConfig) config;
		if (mavenConfig.getModel() == null) {
			return;
		}
		if (pom.exists()) {
			// JBoss Maven Integration facet has been executed		
			//MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
			PomResourceImpl resource = loadResource(pom);
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
				Map<String, String> params = provider.getParams();
				String pomURLString = params.get("template"); //$NON-NLS-1$
				URL platformURL = new URL(pomURLString);
				url = FileLocator.resolve(platformURL);
				
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
					} catch (IOException e) {
						MavenCoreActivator.log(e);
					}
				}
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						MavenCoreActivator.log(e);
					}
				}
				if (libraryResource != null) {
					libraryResource.unload();
				}
			}
			
		} else {
			MavenCoreActivator.addLibraryProviderOperationConfig(config);
		}
	}

	public static PomResourceImpl loadResource(IFile pomFile)
			throws CoreException {
		String path = pomFile.getFullPath().toOSString();
		URI uri = URI.createPlatformResourceURI(path, true);
		try {
			Resource resource = new PomResourceFactoryImpl()
					.createResource(uri);
			resource.load(new HashMap());
			return (PomResourceImpl) resource;

		} catch (Exception ex) {
			String msg = "Can't load model " + pomFile;
			MavenCoreActivator.log(ex);
			throw new CoreException(new Status(IStatus.ERROR,
					MavenCoreActivator.PLUGIN_ID, -1, msg, ex));
		}
	}  
}
