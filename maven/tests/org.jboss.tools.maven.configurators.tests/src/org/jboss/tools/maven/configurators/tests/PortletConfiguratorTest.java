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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.portlet.core.IPortletConstants;
import org.junit.Test;

@SuppressWarnings("restriction")
public class PortletConfiguratorTest extends AbstractMavenConfiguratorTest {

  private static final IProjectFacet        PORTLET_FACET    = ProjectFacetsManager
      .getProjectFacet(IPortletConstants.PORTLET_FACET_ID);
  private static final IProjectFacetVersion PORTLET_FACET_10 = PORTLET_FACET
      .getVersion(IPortletConstants.PORTLET_FACET_VERSION_10);
  private static final IProjectFacetVersion PORTLET_FACET_20 = PORTLET_FACET
      .getVersion(IPortletConstants.PORTLET_FACET_VERSION_20);

  @Test
  public void testImportPortlet10() throws Exception {
    IProject project = importProject("projects/portlets/portlet10/pom.xml");
    assertIsPortletProject(project, PORTLET_FACET_10);
  }

  @Test
  public void testImportPortlet20() throws Exception {
    IProject project = importProject("projects/portlets/portlet20/pom.xml");
    assertIsPortletProject(project, PORTLET_FACET_20);
  }
  
  @Test
  public void testDisablePortletViaProperty() throws Exception {
    String projectLocation = "projects/portlets/deactivated-portlet";
    IProject notPortletProject = importProject(projectLocation+"/pom.xml");
    waitForJobsToComplete();
    assertIsNotPortletProject(notPortletProject);
  }

	protected void assertIsNotPortletProject(IProject project) throws Exception {
		assertNoErrors(project);
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if (facetedProject != null) {
			assertNull("Portlet Facet should be missing ", facetedProject.getInstalledVersion(PORTLET_FACET));
		}
	}
  
  @Test
  public void testImportError() throws Exception {
    IProject project = importProject("projects/portlets/piglet/pom.xml");
    List<IMarker> errors = findErrorMarkers(project);
    assertFalse(errors.isEmpty());
    for (IMarker m : errors) {
      String msg = m.getAttribute(IMarker.MESSAGE).toString();
      // Should not contain Portlet error messages because configuration was
      // skipped
      // due to missing Dyn Web facet constraint.
      assertFalse(msg, msg.contains("Portlet"));
    }
  }

  private void assertIsPortletProject(IProject project,
      IProjectFacetVersion expectedPortletVersion) throws Exception {
    assertNoErrors(project);
    IFacetedProject facetedProject = ProjectFacetsManager.create(project);
    assertNotNull(project.getName() + " is not a faceted project",
        facetedProject);
    assertEquals("Unexpected Portlet Facet Version", expectedPortletVersion,
        facetedProject.getInstalledVersion(PORTLET_FACET));
    assertTrue("Java Facet is missing",
        facetedProject.hasProjectFacet(JavaFacet.FACET));
  }
}
