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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.wizard.xpl.MavenProjectWizardArchetypeParametersPage;
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
	private Composite warningLink;
	private Boolean isEnterpriseRepoAvailable;

	public ArchetypeExamplesWizardPage(
			ProjectImportConfiguration configuration, Project projectDescription) {
		super(configuration);
		setTitle(projectDescription.getShortDescription());
		this.projectDescription = projectDescription;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Archetype archetype = new Archetype();
		ArchetypeModel archetypeModel = projectDescription.getArchetypeModel();

		final String version = archetypeModel.getVersion();

		archetype.setGroupId(archetypeModel.getArchetypeGroupId());
		archetype.setArtifactId(archetypeModel.getArchetypeArtifactId());
		archetype.setVersion(archetypeModel.getArchetypeVersion());
		archetype.setRepository(archetypeModel.getArchetypeRepository());
		
		//Since we set archetypeChanged=false later on, we need to do the equivalent of 
		//MavenProjectWizardArchetypeParametersPage.loadArchetypeDescriptor(), 
		//as we don't have the guarantee archetypeModel holds ALL the required properties
		//This really is an extra-safe guard, as I believe we'll probably always
		//redefine all required properties in project-examples-maven-xxx.xml
		Properties defaultRequiredProperties = getRequiredProperties(archetype, archetypeModel.getArchetypeRepository());
		Properties properties = new Properties();
		
		for (Object key : defaultRequiredProperties.keySet()) {
			properties.put(key, defaultRequiredProperties.get(key));
		}

		//Override default required properties with our specific JBoss Tools values
		for (Object key : archetypeModel.getArchetypeProperties().keySet()) {
			properties.put(key, archetypeModel.getArchetypeProperties().get(key));
		}

		archetype.setProperties(properties);
		setArchetype(archetype);
		
		//JBIDE-9823 : Hack to prevent the properties table to be loaded a 2nd time 
		// when setVisible() is called in MavenProjectWizardArchetypeParametersPage.
		// It needs to be called AFTER setArchetype(archetype) !!! 
		archetypeChanged = false;
		
		resolverConfigurationComponent.setExpanded(!resolverConfigurationComponent.getResolverConfiguration().getActiveProfileList().isEmpty());
	}

	@Override
	protected void createAdvancedSettings(Composite composite, GridData gridData) {
		// TODO Auto-generated method stub
		super.createAdvancedSettings(composite, gridData);
		createMissingRepositoriesWarning(composite, gridData);
	}

	private void createMissingRepositoriesWarning(Composite parent,
			GridData gridData) {
		//TODO make that damn component align correctly
		//warningLink = new MissingRepositoryWarningComponent(parent);
		
		//TODO delete that code
		warningLink = new Composite(parent, SWT.NONE);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(3, 1)
				.applyTo(warningLink);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(warningLink);

		Label warningImg = new Label(warningLink, SWT.CENTER | SWT.TOP);
		warningImg.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));

		Link link = new Link(warningLink, SWT.NONE);
		String message = "Artifacts needed from JBoss Enterprise Maven repository do not seem to be available.\nThis might cause build problems. "
				+ "Follow this <a href=\"http://community.jboss.org/wiki/SettingUpTheJBossEnterpriseRepositories\">link</a> for more details.";
		link.setText(message);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Open default external browser
					PlatformUI.getWorkbench().getBrowserSupport()
							.getExternalBrowser().openURL(new URL(e.text));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		warningLink.setVisible(false);
	}


	
	public Archetype getArchetype() {
		return archetype;
	}
	
	private Properties getRequiredProperties(Archetype archetype, final String archetypeRepositoryUrl) {
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

	            List<ArtifactRepository> repositories = maven.getArtifactRepositories();
	            
	            ArtifactRepository archetypeRepository  = null;
	            
	            if (StringUtils.isNotEmpty(archetypeRepositoryUrl)) {
	            	archetypeRepository  = maven.createArtifactRepository("archetypeRepo", archetypeRepositoryUrl);
	            }

	            //JBIDE-10018 : remote archetypes need to be downloaded 1st or resolution will fail
	            Artifact a = downloadArchetype(groupId, artifactId, version, archetypeRepository, repositories);
	            
	            ArchetypeArtifactManager aaMgr = MavenPluginActivator.getDefault().getArchetypeArtifactManager();
	            
	            ArchetypeDescriptor descriptor = aaMgr.getFileSetArchetypeDescriptor(a.getFile());
	              
	            if(descriptor != null && descriptor.getName() != null) {
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
	        	 //TODO don't swallow exceptions
	          } catch(CoreException ex) {
	        	  //TODO don't swallow exceptions
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
	
	
  /**
   * Apparently, Archetype#generateProjectFromArchetype 2.0-alpha-4 does not attempt to resolve archetype
   * from configured remote repositories. To compensate, we populate local repo with archetype pom/jar.
   * So we use the same hack as in m2e to force the download first.
   * @see http://git.eclipse.org/c/m2e/m2e-core.git/tree/org.eclipse.m2e.core/src/org/eclipse/m2e/core/internal/project/ProjectConfigurationManager.java#n596
   */
	private Artifact downloadArchetype(String groupId, String artifactId,
			String version, ArtifactRepository archetypeRepository,
			List<ArtifactRepository> repositories) throws CoreException {
		IMaven maven = MavenPlugin.getMaven();
	    ArrayList<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
	    if (archetypeRepository != null) {
		    repos.add(archetypeRepository);
	    }
	    repos.addAll(maven.getArtifactRepositories()); // see org.apache.maven.archetype.downloader.DefaultDownloader#download    
	    IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
	    maven.resolve(groupId, artifactId, version, "pom", null, repos, nullProgressMonitor); //$NON-NLS-1$
	    Artifact a = maven.resolve(groupId, artifactId, version, "jar", null, repos, nullProgressMonitor); //$NON-NLS-1$
	    return a;
	}
	
	
	public void updateArchetypeProperty(String key, String value){
		if (propertiesTable == null) return;
		
		for(int i = 0; i < propertiesTable.getItemCount(); i++ ) {
	      TableItem item = propertiesTable.getItem(i);
	      if (item.getText(KEY_INDEX).equals(key) && !item.getText(VALUE_INDEX).equals(value)) {
		      item.setText(VALUE_INDEX, value);
	    	  //don't break, just to be sure
	      }
		}
	}

	public void setArtifactId(String projectName) {
		if (artifactIdCombo != null) {
			artifactIdCombo.setText(projectName);
		}
	}

	public void setPackageName(String packageName) {
		if (packageCombo != null) {
			if (!packageCombo.getText().equals(packageName)){ 
				packageCombo.setText(packageName);
			}
			if (!groupIdCombo.getText().equals(packageName)){
				groupIdCombo.setText(packageName);
			}
		}
	}

	@Override
	protected void validate() {
		checkEnterpriseProperty();
		super.validate();
	}

	private void checkEnterpriseProperty() {
		if (warningLink == null) {
			//Not initialized yet
			return;
		}
		// TODO Auto-generated method stub
		for(int i = 0; i < propertiesTable.getItemCount(); i++ ) {
		      TableItem item = propertiesTable.getItem(i);
		      String value = item.getText(VALUE_INDEX);
		      if (item.getText(KEY_INDEX).equals("enterprise") 
		    		  && (Boolean.TRUE.toString().equalsIgnoreCase(value)
		    		  || "y".equalsIgnoreCase(value)
		    		  || "yes".equalsIgnoreCase(value))
		    		  && !assertEnterpriseRepoAccessible()) {
		    	  warningLink.setVisible(true);
		    	  return;
		      }
		}
  	  warningLink.setVisible(false);
	}
	
	private boolean assertEnterpriseRepoAccessible() {
		if (isEnterpriseRepoAvailable != null) {
			return isEnterpriseRepoAvailable.booleanValue();
		}
		
		isEnterpriseRepoAvailable = MavenArtifactHelper.isEnterpriseRepositoryAvailable();
		return isEnterpriseRepoAvailable.booleanValue();
	}

}
