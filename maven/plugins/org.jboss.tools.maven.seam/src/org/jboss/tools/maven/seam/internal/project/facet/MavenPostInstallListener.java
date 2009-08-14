package org.jboss.tools.maven.seam.internal.project.facet;

import java.util.Set;

import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.eclipse.wst.common.project.facet.core.events.IProjectFacetActionEvent;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.seam.MavenSeamActivator;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamFacetInstallDataModelProvider;

public class MavenPostInstallListener implements IFacetedProjectListener {

	private IDataModel m2FacetModel, seamFacetModel;
	private boolean configured = false;
	
	private static final String M2_FACET_MODEL_PROVIDER = "org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider"; //$NON-NLS-1$
	private static final String SEAM_FACET_MODEL_PROVIDER = "org.jboss.tools.seam.internal.core.project.facet.SeamFacetInstallDataModelProvider"; //$NON-NLS-1$
	public void handleEvent(IFacetedProjectEvent event) {
		IFacetedProject facetedProject = event.getProject();
		Set<IProjectFacetVersion> projectFacets = facetedProject
				.getProjectFacets();

		boolean isSeamProject = false;
		boolean isM2Project = false;
		for (IProjectFacetVersion projectFacetVersion : projectFacets) {
			IProjectFacet projectFacet = projectFacetVersion.getProjectFacet();
			if (ISeamFacetDataModelProperties.SEAM_FACET_ID.equals(projectFacet
					.getId())) {
				isSeamProject = true;
			}
			if (IJBossMavenConstants.M2_FACET_ID.equals(projectFacet.getId())) {
				isM2Project = true;
			}
		}

		IProjectFacetActionEvent actionEvent = (IProjectFacetActionEvent) event;
		Object object = actionEvent.getActionConfig();
		if (object instanceof IDataModel) {
			IDataModel dataModel = (IDataModel) object;
			if ( SEAM_FACET_MODEL_PROVIDER.equals(dataModel.getID())) {
				seamFacetModel = dataModel;
			}

			if ( M2_FACET_MODEL_PROVIDER.equals(dataModel.getID()) ) {
				m2FacetModel = dataModel;
			}
		}
		if (!isSeamProject) {
			configured = false;
		}
		
		if (isSeamProject && isM2Project && !configured) {
			boolean mavenProjectExists = m2FacetModel.getBooleanProperty(IJBossMavenConstants.MAVEN_PROJECT_EXISTS);
			if (!mavenProjectExists) {
				MavenSeamActivator.getDefault().configureSeamProject(seamFacetModel,m2FacetModel);
			}
			configured=true;
		}
	}

}
