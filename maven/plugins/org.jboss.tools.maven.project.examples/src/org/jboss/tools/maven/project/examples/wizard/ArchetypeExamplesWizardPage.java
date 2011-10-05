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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
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
		
		//Since we set archetypeChanged=false later on, we need to do the equivalent of 
		//MavenProjectWizardArchetypeParametersPage.loadArchetypeDescriptor(), 
		//as we don't have the guarantee archetypeModel holds ALL the required properties
		//This really is an extra-safe guard, as I believe we'll probably always
		//redefine all required properties in project-examples-maven-xxx.xml
		Properties defaultRequiredProperties = getRequiredProperties(archetype);
		Properties properties = new Properties(archetypeModel.getArchetypeProperties());
		//Add remaining requiredProperties not defined by default in the example project
		for (Object key : defaultRequiredProperties.keySet()) {
			if (!properties.containsKey(key)) {
				properties.put(key, defaultRequiredProperties.get(key));
			}
		}
		archetype.setProperties(properties);
		setArchetype(archetype);
		
		//JBIDE-9823 : Hack to prevent the properties table to be loaded a 2nd time 
		// when setVisible() is called in MavenProjectWizardArchetypeParametersPage.
		// It needs to be called AFTER setArchetype(archetype) !!! 
		archetypeChanged = false;
		
		//Use archetype/example name by default
		artifactIdCombo.setText(artifactId);
	    
		//Check if project already exists
	    IStatus nameStatus = getImportConfiguration().validateProjectName(getModel());
	    if(!nameStatus.isOK()) {
	    	//Force the user to change the name if the project exists
	    	artifactIdCombo.setText("");//$NON-NLS-1$
	    }
	    
		groupIdCombo.setText(groupId);
		versionCombo.setText(version);
		packageCombo.setText(javaPackage);
	}

	public Archetype getArchetype() {
		return archetype;
	}
	
	private Properties getRequiredProperties(Archetype archetype) {
	    final String groupId = archetype.getGroupId();
	    final String artifactId = archetype.getArtifactId();
	    final String version = archetype.getVersion();
	    final String archetypeName = groupId + ":" + artifactId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
	    final Properties requiredProperties = new Properties();
	    try {
	      getContainer().run(false, true, new IRunnableWithProgress() {
	        public void run(IProgressMonitor monitor) {
	          monitor.beginTask(NLS.bind("Downloading Archetype {0}", archetypeName), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
	          try {
	            IMaven maven = MavenPlugin.getMaven();

	            ArtifactRepository localRepository = maven.getLocalRepository();

	            List<ArtifactRepository> repositories = maven.getArtifactRepositories();

	            ArchetypeArtifactManager aaMgr = MavenPluginActivator.getDefault().getArchetypeArtifactManager();
	            if(aaMgr.isFileSetArchetype(groupId, artifactId, version, null, localRepository, repositories)) {
	              ArchetypeDescriptor descriptor = aaMgr.getFileSetArchetypeDescriptor(groupId, artifactId, version, null,
	                  localRepository, repositories);
	              List<?> properties = descriptor.getRequiredProperties();
	              if(properties != null) {
	                for(Object o : properties) {
	                  if(o instanceof RequiredProperty) {
	                    RequiredProperty rp = (RequiredProperty) o;
	                    requiredProperties.put(rp.getKey(), rp.getDefaultValue());
	                  }
	                }
	              }
	            }
	          } catch(UnknownArchetype e) {
	        	 MavenProjectExamplesActivator.log(e);
	          } catch(CoreException ex) {
	        	 MavenProjectExamplesActivator.log(ex);
	          } finally {
	            monitor.done();
	          }
	        }
	      });
	    } catch(InterruptedException ex) {
	      // ignore
	    } catch(InvocationTargetException ex) {
	      String msg = NLS.bind("Error downloading archetype {0}", archetypeName);//$NON-NLS-1$
	      MavenProjectExamplesActivator.log(ex, msg);
	      setErrorMessage(msg + "\n" + ex.toString()); //$NON-NLS-1$
	    }
	    return requiredProperties;
	  }
}
