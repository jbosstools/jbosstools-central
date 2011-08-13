/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.jbosspackaging.configurators;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.ide.eclipse.as.ui.mbeans.project.IJBossSARFacetDataModelProperties;
import org.jboss.ide.eclipse.as.ui.mbeans.project.JBossSARFacetDataModelProvider;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.maven.ide.eclipse.wtp.ArtifactHelper;
import org.maven.ide.eclipse.wtp.WTPProjectsUtil;

/**
 * 
 * @author Fred Bricon
 * 
 */
public class SarProjectConfigurator extends AbstractProjectConfigurator {

	public static final IProjectFacet JBOSS_SAR_FACET;
	public static final IProjectFacetVersion JBOSS_SAR_FACET_VERSION_1_0;
	public static final ArtifactFilter SCOPE_FILTER_RUNTIME = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	private static final IClasspathAttribute NONDEPENDENCY_ATTRIBUTE = JavaCore.newClasspathAttribute(
			IClasspathDependencyConstants.CLASSPATH_COMPONENT_NON_DEPENDENCY, "");
	static {
		JBOSS_SAR_FACET = ProjectFacetsManager.getProjectFacet("jst.jboss.sar");
		JBOSS_SAR_FACET_VERSION_1_0 = JBOSS_SAR_FACET.getVersion("1.0");//$NON-NLS-1$
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
	}

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		
		markerManager.deleteMarkers(project, MavenSarConstants.SAR_CONFIGURATION_ERROR_MARKER_ID);
		
		if (!getExpectedPackage().equals(mavenProject.getPackaging())) {
			return;
		}
		
		IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);

		Set<Action> actions = new LinkedHashSet<Action>();
		
		IMavenProjectFacade facade = request.getMavenProjectFacade();
		
		ResourceCleaner fileCleaner = new ResourceCleaner(project);
		addFilesToClean(fileCleaner, facade.getResourceLocations());
		addFilesToClean(fileCleaner, facade.getTestResourceLocations());
		addFilesToClean(fileCleaner, facade.getCompileSourceLocations());
		addFilesToClean(fileCleaner, facade.getTestCompileSourceLocations());
		
		IPath source = facade.getResourceLocations()[0];
		
		WTPProjectsUtil.installJavaFacet(actions, project, facetedProject);
		if (!actions.isEmpty()) {
			facetedProject.modify(actions, monitor);
		}
		installSarFacet(facetedProject, source, monitor);

		installM2Facet(facetedProject, monitor);

		ModuleCoreNature.addModuleCoreNatureIfNecessary(project, monitor);

		WTPProjectsUtil.removeTestFolderLinks(project, mavenProject, monitor, "/");

		setNonDependencyAttributeToContainer(project, monitor);

		WTPProjectsUtil.removeWTPClasspathContainer(project);
		
		fileCleaner.cleanUp();
	}

	private void installM2Facet(IFacetedProject fproj, IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(m2Facet)) {
			IDataModel config = (IDataModel) new MavenFacetInstallDataModelProvider().create();
			config.setBooleanProperty(IJBossMavenConstants.MAVEN_PROJECT_EXISTS, true);
			fproj.installProjectFacet(m2Version, config, monitor);
		}
	}
	
	private void installSarFacet(IFacetedProject fproj, IPath source, IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(JBOSS_SAR_FACET)) {
			IProjectFacetVersion facetVersion = JBOSS_SAR_FACET_VERSION_1_0;
			IStatus status = facetVersion.getConstraint().check(fproj.getProjectFacets());
			if (status.isOK()) {
				IDataModel config = (IDataModel) new JBossSARFacetDataModelProvider().create();
				config.setProperty(IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER, source.toPortableString());
				fproj.installProjectFacet(facetVersion, config, monitor);
			} else {
		        addErrorMarker(fproj.getProject(), facetVersion + " can not be installed : "+ status.getMessage());
				for (IStatus st : status.getChildren()) {
			        addErrorMarker(fproj.getProject(), st.getMessage());
				}
			}
		}
	}

	private void addErrorMarker(IProject project, String message) {
	    markerManager.addMarker(project, 
	    		MavenSarConstants.SAR_CONFIGURATION_ERROR_MARKER_ID, 
	    		message
	    		,-1,  IMarker.SEVERITY_ERROR);
	}
	
	protected String getExpectedPackage() {
		return "jboss-sar";
	}


	public void setModuleDependencies(IProject project, MavenProject mavenProject, IProgressMonitor monitor)
			throws CoreException {

		IVirtualComponent sarComponent = ComponentCore.createComponent(project);
		if (sarComponent == null) {
			return;
		}

		Set<IVirtualReference> newRefs = new LinkedHashSet<IVirtualReference>();

		JBossPackagingPluginConfiguration config = new JBossPackagingPluginConfiguration(mavenProject);

		if (!config.isExcludeAll()) {
			Set<Artifact> artifacts = mavenProject.getArtifacts();
			
			for (Artifact artifact : artifacts) {
				// Don't deploy pom, non runtime or optional dependencies
				if ("pom".equals(artifact.getType()) || !SCOPE_FILTER_RUNTIME.include(artifact) 
					|| artifact.isOptional() || config.isExcluded(artifact)) {
					continue;
				}
	
				IMavenProjectFacade workspaceDependency = projectManager.getMavenProject(artifact.getGroupId(),
						artifact.getArtifactId(), artifact.getVersion());
	
				IVirtualComponent depComponent;
				if (workspaceDependency != null && !workspaceDependency.getProject().equals(project)
						&& workspaceDependency.getFullPath(artifact.getFile()) != null) {
					// artifact dependency is a workspace project
					depComponent = ComponentCore.createComponent(workspaceDependency.getProject());
				} else {
					// artifact dependency should be added as a JEE module,
					// referenced with M2_REPO variable
					String artifactPath = ArtifactHelper.getM2REPOVarPath(artifact);
					depComponent = ComponentCore.createArchiveComponent(sarComponent.getProject(), artifactPath);
				}
				IVirtualReference reference = ComponentCore.createReference(sarComponent, depComponent);
				reference.setArchiveName(config.mapFileName(artifact));
				reference.setRuntimePath(new Path(config.getLibDirectory()));
				newRefs.add(reference);
			}
		}
		IVirtualReference[] newRefsArray = new IVirtualReference[newRefs.size()];
		newRefs.toArray(newRefsArray);

		// Only change the project references if they've changed
		if (WTPProjectsUtil.hasChanged(sarComponent.getReferences(), newRefsArray)) {
			sarComponent.setReferences(newRefsArray);
		}
	}

	protected void setNonDependencyAttributeToContainer(IProject project, IProgressMonitor monitor)
			throws JavaModelException {
		updateContainerAttributes(project, NONDEPENDENCY_ATTRIBUTE,
				IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY, monitor);
	}

	protected void updateContainerAttributes(IProject project, IClasspathAttribute attributeToAdd,
			String attributeToDelete, IProgressMonitor monitor) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return;
		IClasspathEntry[] cp = javaProject.getRawClasspath();
		for (int i = 0; i < cp.length; i++) {
			if (IClasspathEntry.CPE_CONTAINER == cp[i].getEntryKind()
					&& MavenClasspathHelpers.isMaven2ClasspathContainer(cp[i].getPath())) {
				LinkedHashMap<String, IClasspathAttribute> attrs = new LinkedHashMap<String, IClasspathAttribute>();
				for (IClasspathAttribute attr : cp[i].getExtraAttributes()) {
					if (!attr.getName().equals(attributeToDelete)) {
						attrs.put(attr.getName(), attr);
					}
				}
				attrs.put(attributeToAdd.getName(), attributeToAdd);
				IClasspathAttribute[] newAttrs = attrs.values().toArray(new IClasspathAttribute[attrs.size()]);
				cp[i] = JavaCore.newContainerEntry(cp[i].getPath(), cp[i].getAccessRules(), newAttrs,
						cp[i].isExported());
				break;
			}
		}
		javaProject.setRawClasspath(cp, monitor);
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) throws CoreException {
		setModuleDependencies(event.getMavenProject().getProject(), event.getMavenProject().getMavenProject(), monitor);
	}
	
	private void addFilesToClean(ResourceCleaner cleaner, IPath[] paths) {
	   for (IPath resourceFolderPath : paths) {
	     cleaner.addFiles(resourceFolderPath.append("META-INF/MANIFEST.MF"));
	   }
	}

}
