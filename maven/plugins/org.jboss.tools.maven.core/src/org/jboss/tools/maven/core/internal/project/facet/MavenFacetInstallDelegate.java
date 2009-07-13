package org.jboss.tools.maven.core.internal.project.facet;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperation;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperationConfig;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.embedder.MavenModelManager;

public class MavenFacetInstallDelegate implements IDelegate {
	
	public void execute(IProject project, IProjectFacetVersion fv, Object cfg,
			IProgressMonitor monitor) throws CoreException {
		IDataModel config = null;

		if (cfg != null) {
			config = (IDataModel) cfg;
		} else {
			throw new CoreException(
					MavenCoreActivator
							.getStatus("Internal Error creating JBoss Maven Facet.  Missing configuration"));
		}

		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		IJavaProject javaProject = JavaCore.create(project);
		IFacetedProjectWorkingCopy fpwc = (IFacetedProjectWorkingCopy) config
		.getProperty(IFacetDataModelProperties.FACETED_PROJECT_WORKING_COPY);
		if (!pom.exists()) {
			Model model = new Model();
			model.setModelVersion(IJBossMavenConstants.MAVEN_MODEL_VERSION);
			model.setGroupId(config
					.getStringProperty(IJBossMavenConstants.GROUP_ID));
			String artifactId = config.getStringProperty(IJBossMavenConstants.ARTIFACT_ID);
			model.setArtifactId(artifactId);
			model.setVersion(config
					.getStringProperty(IJBossMavenConstants.VERSION));
			model.setName(config.getStringProperty(IJBossMavenConstants.NAME));
			model.setPackaging(config
					.getStringProperty(IJBossMavenConstants.PACKAGING));
			model.setDescription(config
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			Build build = new Build();
			model.setBuild(build);
			
			// build.setFinalName(artifactId);			
			if (fpwc.hasProjectFacet(JavaFacet.FACET)) {
				String outputDirectory = MavenCoreActivator
						.getOutputDirectory(javaProject);
				build.setOutputDirectory(outputDirectory);
				String sourceDirectory = MavenCoreActivator
						.getSourceDirectory(javaProject);
				if (sourceDirectory != null) {
					build.setSourceDirectory(sourceDirectory);
				}
			}
			
			if (fpwc.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
				
				MavenCoreActivator.addMavenWarPlugin(build, project);
			}
			if (fpwc.hasProjectFacet(IJ2EEFacetConstants.EJB_FACET)) {
				
				MavenCoreActivator.addMavenEjbPlugin(build, project);
			}
			if (fpwc.hasProjectFacet(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_FACET)) {
				MavenCoreActivator.addMavenEarPlugin(build, project, config, false);
				MavenCoreActivator.createMavenProject(project.getName(), monitor, model, true);
			}
			
			if (!pom.exists()) {
				MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
				modelManager.createMavenModel(pom, model);
			}
		}
		
		boolean hasMavenNature = MavenCoreActivator.addMavenNature(project, monitor);
		
		if (fpwc.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(
							IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY,
							ClasspathDependencyUtil.getDefaultRuntimePath(true).toString());
			MavenCoreActivator.addClasspathAttribute(javaProject, attribute, monitor);
		}
		// FIXME
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(
				MavenCoreActivator.OWNER_PROJECT_FACETS_ATTR,
				IJBossMavenConstants.M2_FACET_ID);
		MavenCoreActivator.addClasspathAttribute(javaProject, attribute, monitor);
		if (!hasMavenNature) {
			MavenCoreActivator.updateMavenProjectConfiguration(project);
		}
		
		List<LibraryProviderOperationConfig> configs = MavenCoreActivator.getLibraryProviderOperationConfigs();
		if (configs.size() > 0) {
			MavenLibraryProviderInstallOperation operation = new MavenLibraryProviderInstallOperation();
			for (LibraryProviderOperationConfig libraryProviderOperationConfig:configs) {
				operation.execute(libraryProviderOperationConfig, monitor);
			}
			configs.clear();
		}
		
	}
	
}
