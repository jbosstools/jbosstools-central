package org.jboss.tools.maven.seam.internal.project.facet;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.eclipse.wst.common.project.facet.core.events.IProjectFacetActionEvent;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.seam.MavenSeamActivator;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;

public class MavenPostInstallListener implements IFacetedProjectListener {

	private static final String M2_FACET_MODEL_PROVIDER = "org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider"; //$NON-NLS-1$
	
	private static final String SEAM_FACET_MODEL_PROVIDER = "org.jboss.tools.seam.internal.core.project.facet.SeamFacetInstallDataModelProvider"; //$NON-NLS-1$
	
	private static final IProjectFacet MAVEN_FACET = ProjectFacetsManager.getProjectFacet(IJBossMavenConstants.M2_FACET_ID);
			
	private static final IProjectFacet SEAM_FACET = ProjectFacetsManager.getProjectFacet("jst.seam"); //$NON-NLS-1$
	
	private Map<IFacetedProject, IDataModel> seamModels = new WeakHashMap<IFacetedProject, IDataModel>(1);
	
	private Map<IFacetedProject, IDataModel> mavenModels = new WeakHashMap<IFacetedProject, IDataModel>(1);
	
	public void handleEvent(IFacetedProjectEvent event) {
		if (!(event instanceof IProjectFacetActionEvent)) {
			return;
		}
		
		IFacetedProject facetedProject = event.getProject();
		
		boolean isSeamProject = facetedProject.hasProjectFacet(SEAM_FACET);
		boolean isM2Project = facetedProject.hasProjectFacet(MAVEN_FACET);
		
		Object object = ((IProjectFacetActionEvent) event).getActionConfig();

		IDataModel m2FacetModel = null;
		IDataModel seamFacetModel = null;
		if (object instanceof IDataModel) {
			
			IDataModel dataModel = (IDataModel) object;
			
			if ( SEAM_FACET_MODEL_PROVIDER.equals(dataModel.getID())) {
				seamModels.put(facetedProject, dataModel);
				seamFacetModel  = dataModel;
			} 
			else if ( M2_FACET_MODEL_PROVIDER.equals(dataModel.getID()) ) {
				mavenModels.put(facetedProject, dataModel);
				m2FacetModel  = dataModel;
			}
		}
		
		if (m2FacetModel == null) {
			m2FacetModel = mavenModels.get(facetedProject);
		}
		if (seamFacetModel == null) {
			seamFacetModel = seamModels.get(facetedProject);
		}
		
		if (isSeamProject && isM2Project && seamFacetModel != null && m2FacetModel != null) {
			boolean mavenProjectExists = seamFacetModel.getBooleanProperty(ISeamFacetDataModelProperties.PROJECT_ALREADY_EXISTS);
			mavenProjectExists = mavenProjectExists || m2FacetModel.getBooleanProperty(IJBossMavenConstants.MAVEN_PROJECT_EXISTS);
			//System.err.println("Configuration using m2eFacetModel of "+  m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID));
			if (!mavenProjectExists) {
				MavenSeamActivator.getDefault().configureSeamProject(seamFacetModel,m2FacetModel);
			} 
			mavenModels.remove(facetedProject);
			seamModels.remove(facetedProject);
		}
	}


}
