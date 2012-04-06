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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperation;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.model.edit.pom.Dependency;
import org.eclipse.m2e.model.edit.pom.Exclusion;
import org.eclipse.m2e.model.edit.pom.Model;
import org.eclipse.m2e.model.edit.pom.Repository;
import org.eclipse.m2e.model.edit.pom.RepositoryPolicy;
import org.eclipse.m2e.model.edit.pom.util.PomResourceFactoryImpl;
import org.eclipse.m2e.model.edit.pom.util.PomResourceImpl;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

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
			savePomModel(providerFile, libraryModel);
		} else {
			MavenCoreActivator.addLibraryProviderOperationConfig(config);
		}
	}

	private void savePomModel(File file, Model pomModel) throws CoreException {
		org.apache.maven.model.Model model = new org.apache.maven.model.Model();
	    model.setModelVersion("4.0.0"); //$NON-NLS-1$
	    
	    model.setGroupId(pomModel.getArtifactId());
	    model.setArtifactId(pomModel.getArtifactId());
	    model.setVersion(pomModel.getVersion());
	    model.setPackaging(pomModel.getPackaging());
	    
	    if(pomModel.getName() != null && pomModel.getName().length() > 0) {
	      model.setName(pomModel.getName());
	    }
	    if(pomModel.getDescription() != null && pomModel.getDescription().length() > 0) {
	      model.setDescription(pomModel.getDescription());
	    }
	    EList<Dependency> pomDependencies = pomModel.getDependencies();
	    List<org.apache.maven.model.Dependency> dependencies = model.getDependencies();
	    for (Dependency pomDependency:pomDependencies) {
	    	org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
	    	dependency.setArtifactId(pomDependency.getArtifactId());
	    	dependency.setGroupId(pomDependency.getGroupId());
	    	dependency.setVersion(pomDependency.getVersion());
	    	dependency.setType(pomDependency.getType());
	    	dependency.setScope(pomDependency.getScope());
	    	dependency.setClassifier(pomDependency.getClassifier());
	    	dependency.setOptional(pomDependency.getOptional());
	    	dependency.setSystemPath(pomDependency.getSystemPath());
	    	EList<Exclusion> pomExclusions = pomDependency.getExclusions();
	    	List<org.apache.maven.model.Exclusion> exclusions = dependency.getExclusions();
	    	for (Exclusion pomExclusion:pomExclusions) {
	    		org.apache.maven.model.Exclusion exclusion = new org.apache.maven.model.Exclusion();
	    		exclusion.setGroupId(pomExclusion.getGroupId());
	    		exclusion.setArtifactId(pomExclusion.getArtifactId());
	    		exclusions.add(exclusion);
	    	}
	    	dependencies.add(dependency);
	    }
	    EList<Repository> pomRepositories = pomModel.getRepositories();
	    List<org.apache.maven.model.Repository> repositories = model.getRepositories();
	    for (Repository pomRepository:pomRepositories) {
	    	org.apache.maven.model.Repository repository = new org.apache.maven.model.Repository();
	    	repository.setId(pomRepository.getId());
	    	repository.setLayout(pomRepository.getLayout());
	    	repository.setName(pomRepository.getName());
	    	RepositoryPolicy pomReleases = pomRepository.getReleases();
	    	repository.setReleases(getRepositoryPolicy(pomReleases));
	    	RepositoryPolicy pomSnapshots = pomRepository.getSnapshots();
	    	repository.setSnapshots(getRepositoryPolicy(pomSnapshots));
	    	repository.setLayout(pomRepository.getLayout());
	    	repository.setUrl(pomRepository.getUrl());
	    	repositories.add(repository);
	    }
	    createMavenModel(file, model);
	}

	public void createMavenModel(File file, org.apache.maven.model.Model model) throws CoreException {

	    try {
	      ByteArrayOutputStream buf = new ByteArrayOutputStream();

	      IMaven maven = MavenPluginActivator.getDefault().getMaven();
	      maven.writeModel(model, buf);

	      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	      documentBuilderFactory.setNamespaceAware(false);
	      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	      
	      Document document = documentBuilder.parse(new ByteArrayInputStream(buf.toByteArray()));
	      Element documentElement = document.getDocumentElement();

	      NamedNodeMap attributes = documentElement.getAttributes();

	      if(attributes == null || attributes.getNamedItem("xmlns") == null) { //$NON-NLS-1$
	        Attr attr = document.createAttribute("xmlns"); //$NON-NLS-1$
	        attr.setTextContent("http://maven.apache.org/POM/4.0.0"); //$NON-NLS-1$
	        documentElement.setAttributeNode(attr);
	      }

	      if(attributes == null || attributes.getNamedItem("xmlns:xsi") == null) { //$NON-NLS-1$
	        Attr attr = document.createAttribute("xmlns:xsi"); //$NON-NLS-1$
	        attr.setTextContent("http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$
	        documentElement.setAttributeNode(attr);
	      }

	      if(attributes == null || attributes.getNamedItem("xsi:schemaLocation") == null) { //$NON-NLS-1$
	        Attr attr = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation"); //$NON-NLS-1$ //$NON-NLS-2$
	        attr.setTextContent("http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"); //$NON-NLS-1$
	        documentElement.setAttributeNode(attr);
	      }
	      
	      TransformerFactory transfac = TransformerFactory.newInstance();
	      Transformer trans = transfac.newTransformer();
	      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$

	      buf.reset();
	      trans.transform(new DOMSource(document), new StreamResult(buf));

	      OutputStream os = null;
	      InputStream is = null;
	      try {
	    	  os = new FileOutputStream(file);
	    	  is = new ByteArrayInputStream(buf.toByteArray());
	    	  MavenCoreActivator.copy(is, os);
	      } catch (Exception e) {
				MavenCoreActivator.log(e);
				throw new CoreException(new Status(IStatus.ERROR,
						MavenCoreActivator.PLUGIN_ID, -1, e.getMessage(), e));
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception ignore) {}
				}
				if (os != null) {
					try {
						os.close();
					} catch (Exception ignore) {}
				}
			}

		} catch (RuntimeException ex) {
			String msg = "Can't create model " + file.getAbsolutePath();
			throw new CoreException(new Status(IStatus.ERROR,
					MavenCoreActivator.PLUGIN_ID, -1, msg, ex));
		} catch (Exception ex) {
			String msg = "Can't create model " + file.getAbsolutePath();
			throw new CoreException(new Status(IStatus.ERROR,
					MavenCoreActivator.PLUGIN_ID, -1, msg, ex));
		}
	  }

	private org.apache.maven.model.RepositoryPolicy getRepositoryPolicy(
			RepositoryPolicy pomRepositoryPolicy) {
		if (pomRepositoryPolicy == null) {
			return null;
		}
		org.apache.maven.model.RepositoryPolicy repositoryPolicy = new org.apache.maven.model.RepositoryPolicy();
    	repositoryPolicy.setChecksumPolicy(pomRepositoryPolicy.getChecksumPolicy());
    	repositoryPolicy.setEnabled(pomRepositoryPolicy.getEnabled());
    	repositoryPolicy.setUpdatePolicy(pomRepositoryPolicy.getUpdatePolicy());
		return repositoryPolicy;
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
