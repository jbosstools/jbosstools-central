/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import java.io.File;

import junit.framework.Assert;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectValidator;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.jst.web.kb.IKbProject;
import org.jboss.tools.maven.core.MavenUtil;
import org.jboss.tools.seam.core.ISeamProject;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SeamConfiguratorTest extends AbstractMavenConfiguratorTest {

	public static final String SEAM_HOME_PROPERTY = System.getProperty("jbosstools.test.seam.2.3.home", "target/requirements/jboss-seam-2.3.1.Final");
	
	private static final IProjectFacetVersion JSF_FACET_VERSION_2_1;
	private static final IProjectFacetVersion JSF_FACET_VERSION_2_0;
	private static final IProjectFacetVersion JSF_FACET_VERSION_1_2;
	private static final IProjectFacetVersion JSF_FACET_VERSION_1_1;
	//protected static final IProjectFacet m2Facet;
	//protected static final IProjectFacetVersion m2Version;
	public static final String JSF_VERSION_2_1 = "2.1";

	public static final String JSF_VERSION_2_0 = "2.0";
	
	public static final String JSF_VERSION_1_2 = "1.2";
	
	public static final String JSF_VERSION_1_1 = "1.1";
	
	static {
		JSF_FACET_VERSION_2_1 = JSF_FACET.getVersion(JSF_VERSION_2_1); 
		JSF_FACET_VERSION_2_0 = JSF_FACET.getVersion(JSF_VERSION_2_0); 
		JSF_FACET_VERSION_1_2 = JSF_FACET.getVersion(JSF_VERSION_1_2); //$NON-NLS-1$
		JSF_FACET_VERSION_1_1 = JSF_FACET.getVersion(JSF_VERSION_1_1); //$NON-NLS-1$
	}
	
	private static final IProjectFacet SEAM_FACET = ProjectFacetsManager.getProjectFacet("jst.seam");
	
	@Test
	public void testJBIDE9454_webXml_overwrite() throws Exception {
		IProject project = importAndCheckSeamProject("seam-webxml");
		assertIsJSFProject(project, JSF_FACET_VERSION_1_2);
	}	
	
	@Test
	public void testJBIDE6228_webXml_changed_richfaces() throws Exception {
		IProject project = importAndCheckSeamProject("seamIntegration");
		assertIsJSFProject(project, JSF_FACET_VERSION_1_2);
	}
	
	protected IProject importAndCheckSeamProject(String projectName) throws Exception {
		String projectLocation = "projects/seam/"+projectName;
		String webxmlRelPath = "src/main/webapp/WEB-INF/web.xml";
		
		IProject seamProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(seamProject);
		
		IFile webXml = seamProject.getFile(webxmlRelPath);
		assertTrue(webXml.exists());
		File originalWebXml = new File(projectLocation, webxmlRelPath);
		assertEquals("web.xml content changed ", toString(originalWebXml), toString(webXml));
		return seamProject;
	}	

	@Test
	public void testJBIDE10764_builderOrder() throws Exception {
		IProject ejb = importProject("projects/seam/JBIDE-10764/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(ejb);
		assertTrue("Seam nature is missing", ejb.hasNature(ISeamProject.NATURE_ID));
		assertTrue("KB nature is missing", ejb.hasNature(IKbProject.NATURE_ID));
	}
	
	@Test
	public void testJBIDE10790_earConfiguration()  throws Exception {
		IProject[] projects = importProjects("projects/seam/multi", 
				new String[]{ "pom.xml",
							  "multi-ear/pom.xml",
							  "multi-ejb/pom.xml",
							  "multi-web/pom.xml"}, 
				new ResolverConfiguration());
		waitForJobsToComplete();
		
		IProject pom = projects[0];
		IProject ear = projects[1];
		IProject ejb = projects[2];
		IProject web = projects[3];

		assertNoErrors(pom);
		assertNoErrors(ejb);
		assertNoErrors(ear);
		assertNoErrors(web);

		assertTrue("Seam nature is missing", ejb.hasNature(ISeamProject.NATURE_ID));
		assertTrue("KB nature is missing", ejb.hasNature(IKbProject.NATURE_ID));

		IFacetedProject fpWeb = ProjectFacetsManager.create(web);
		assertEquals(SEAM_FACET.getVersion("2.2"), fpWeb.getProjectFacetVersion(SEAM_FACET));
		IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(web);
		assertEquals(ISeamFacetDataModelProperties.DEPLOY_AS_EAR, prefs.get(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS, null));
	}
	
	
	@Test
	public void testJBIDE11176_addSeamDependencyToExistingMavenizedProject()  throws Exception {
		IProject project = createExisting("dynamic-web", "projects/conversion/dynamic-web");
		project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		waitForJobsToComplete();
		updateProject(project, "seam-dependency.pom");
		assertNoErrors(project);
		
		Model model = MavenUtil.getModel(project);
		assertNotNull("Model is null", model);
		
		assertEquals("Unexpected dependencies found :"+ model.getDependencies(), 1, model.getDependencies().size());
		assertNull("Found unxepected parent "+ model.getParent(),  model.getParent());
		
	}
	
	@Test
	public void testJBIDE11570_constraintViolations() throws Exception {
		IProject project = importProject("projects/seam/violation/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(project);
		assertIsJSFProject(project, JSF_FACET_VERSION_1_2);
		
		updateProject(project, "add-seam.pom");
		IFacetedProject fp = ProjectFacetsManager.create(project);
		assertFalse("Seam facet was installed", fp.hasProjectFacet(SEAM_FACET));
		assertIsJSFProject(project, JSF_FACET_VERSION_1_2);
		assertHasError(project, "Seam 2.3 can not be installed as it conflicts with JavaServer Faces 1.2");
	}
	
	
	@Test
	public void testJBIDE11570_unresolvedSeamDependency() throws Exception {
		IProject project = importProject("projects/seam/missing-seam/pom.xml");
		waitForJobsToComplete();
		IFacetedProject fp = ProjectFacetsManager.create(project);
		assertFalse("Seam facet was installed", fp.hasProjectFacet(SEAM_FACET));
		assertFalse("JSF facet was installed", fp.hasProjectFacet(JSF_FACET));
		assertHasError(project, "Missing artifact org.jboss.seam:jboss-seam:jar:missing");
	}
	
	@Test
	public void testJBIDE17846_checkMatchingSeamRuntime() throws Exception {
		SeamRuntime seamRuntime = createSeamRuntime("jboss-seam-2.3", SEAM_HOME_PROPERTY, SeamVersion.SEAM_2_3);
		
		IProject project = importProject("projects/seam/JBIDE-17846/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(project);
		IFacetedProject fp = ProjectFacetsManager.create(project);
		IProjectFacetVersion seamFacet = fp.getProjectFacetVersion(SEAM_FACET);
		assertNotNull("Seam facet was not installed", seamFacet);
		assertEquals("Unexpected Seam facet version", "2.3", seamFacet.getVersionString());
		assertHasSeamRuntime(project, seamRuntime);
	}
	
	@Test
	public void testJBIDE17929_DisableSeam()  throws Exception {
		IProject[] projects = importProjects("projects/seam/multi-disabled", 
				new String[]{ "pom.xml",
							  "multi-ear/pom.xml",
							  "multi-ejb/pom.xml",
							  "multi-web/pom.xml"}, 
				new ResolverConfiguration());
		waitForJobsToComplete();
		
		IProject pom = projects[0];
		IProject ear = projects[1];
		IProject ejb = projects[2];
		IProject web = projects[3];

		assertNoErrors(pom);
		assertNoErrors(ejb);
		assertNoErrors(ear);
		assertNoErrors(web);

		assertFalse("Seam nature shouldn't be added to "+web, web.hasNature(ISeamProject.NATURE_ID));
		assertFalse("Seam nature shouldn't be added to "+ejb, ejb.hasNature(ISeamProject.NATURE_ID));
		assertFalse("Seam nature shouldn't be added to "+ear, ear.hasNature(ISeamProject.NATURE_ID));
	}
	
	
	private void assertHasSeamRuntime(IProject project, SeamRuntime seamRuntime) {
		assertEquals(seamRuntime, SeamRuntimeManager.getInstance().getRuntimeForProject(project));
	}

	protected static SeamRuntime createSeamRuntime(String name, String seamPath, SeamVersion seamVersion) {
		SeamRuntime seamRuntime = SeamRuntimeManager.getInstance().findRuntimeByName(name);
		if (seamRuntime != null) {
			return seamRuntime;
		}
		File seamFolder = new File(seamPath);
		assertTrue(seamFolder + " is missing. Either run a maven build first or run this test with -Djbosstools.test.seam.2.3.home", seamFolder.exists());
		assertTrue(seamFolder + " is not a directory", seamFolder.isDirectory());
		
		SeamRuntime rt = new SeamRuntime();
		rt.setHomeDir(seamPath);
		rt.setName(name);
		rt.setDefault(true);
		rt.setVersion(seamVersion);
		SeamRuntimeManager.getInstance().addRuntime(rt);
		return rt;
	}
	
}
