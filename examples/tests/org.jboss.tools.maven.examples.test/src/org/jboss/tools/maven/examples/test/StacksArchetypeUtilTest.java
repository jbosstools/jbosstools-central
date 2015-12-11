/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.examples.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.jdf.stacks.model.Runtime;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.maven.project.examples.internal.stacks.StacksArchetypeUtil;
import org.jboss.tools.stacks.core.model.StacksManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class StacksArchetypeUtilTest {

	private static final String EAP70 = "org.jboss.ide.eclipse.as.runtime.eap.70";
	private static final String EAP60 = "org.jboss.ide.eclipse.as.runtime.eap.60";
	private static final String EAP61 = "org.jboss.ide.eclipse.as.runtime.eap.61";
	private static final String EAP62 = "org.jboss.ide.eclipse.as.runtime.eap.62";

	private static final class FacetVersionSupportAnswer implements
			Answer<Boolean> {
		private Collection<IProjectFacetVersion> supportedFacetVersions;

		public FacetVersionSupportAnswer(Collection<IProjectFacetVersion> supportedFacetVersions) {
			this.supportedFacetVersions	= supportedFacetVersions;
		}
		
		@Override
		public Boolean answer(InvocationOnMock invocation) throws Throwable {
			IProjectFacetVersion pfv = (IProjectFacetVersion)invocation.getArguments()[0];
			return supportedFacetVersions.contains(pfv);
		}
	}

	private static final List<IProjectFacetVersion> FULL_EE7_FACET_VERSIONS = Arrays.asList(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_70, IJ2EEFacetConstants.ENTERPRISE_APPLICATION_60, IJ2EEFacetConstants.DYNAMIC_WEB_31, IJ2EEFacetConstants.DYNAMIC_WEB_30);
	private static final List<IProjectFacetVersion> FULL_EE6_FACET_VERSIONS = Arrays.asList(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_60, IJ2EEFacetConstants.DYNAMIC_WEB_30);
	private static final List<IProjectFacetVersion> WEB_EE7_FACET_VERSIONS = Arrays.asList(IJ2EEFacetConstants.DYNAMIC_WEB_31, IJ2EEFacetConstants.DYNAMIC_WEB_30);
	private static final List<IProjectFacetVersion> WEB_EE6_FACET_VERSIONS = Arrays.asList(IJ2EEFacetConstants.DYNAMIC_WEB_30);

	private Stacks stacks;
	
	private StacksArchetypeUtil stacksArchetypeUtil;
	
	
	@Before
	public void setUp() throws Exception {
		String url = new File("resources/stacks.yaml").toURI().toURL().toString();
		stacks = new StacksManager().getStacks(url , new NullProgressMonitor());
		assertNotNull(stacks);
		assertEquals(14 + 1, stacks.getAvailableArchetypes().size());
		
		stacksArchetypeUtil = new StacksArchetypeUtil(createMockFacetedRuntimes());
	}
	
	private Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> createMockFacetedRuntimes() {

		Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> frs = new HashSet<IRuntime>();
		frs.add(createMockFacetedRuntime(EAP60, FULL_EE6_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime(EAP61, FULL_EE6_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime(EAP62, FULL_EE6_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime(EAP70, FULL_EE7_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime("jbossas7", FULL_EE6_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime("tomcat7", WEB_EE6_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime("tomcat6", Arrays.asList(IJ2EEFacetConstants.DYNAMIC_WEB_25)));
		frs.add(createMockFacetedRuntime("wildfly", FULL_EE7_FACET_VERSIONS));
		frs.add(createMockFacetedRuntime("tomee", WEB_EE7_FACET_VERSIONS));
		return frs;
	}

	private IRuntime createMockFacetedRuntime(String id, Collection<IProjectFacetVersion> supportedFacetVersions) {
		IRuntime fr = mock(IRuntime.class, Mockito.RETURNS_DEEP_STUBS);
		when(fr.getProperty("id")).thenReturn(id);
		when(fr.supports(any(IProjectFacetVersion.class))).then(new FacetVersionSupportAnswer(supportedFacetVersions));
		return fr;
	}

	public void tearDown() {
		stacks = null;
		stacksArchetypeUtil = null;
	}
	
	@Test
	public void testGetArchetype() {
		org.eclipse.wst.server.core.IRuntime eap6 = createRuntime(EAP60);
		
		//No matching archetype
		assertNull(stacksArchetypeUtil.getArchetype("unknown", false, null, "web-ee7", stacks));
		
		//Get matching product archetype
		ArchetypeVersion archetype = stacksArchetypeUtil.getArchetype("javaee-web", false, eap6, stacks);
		assertNotNull(archetype);
		assertEquals("jboss-javaee6-webapp-archetype",archetype.getArchetype().getId());

		//Default to latest non-blank for product 
		ArchetypeVersion archetype2 = stacksArchetypeUtil.getArchetype("javaee-web", false, "product", "full-ee999", stacks);
		assertEquals("jboss-javaee6-webapp-archetype-eap",archetype2.getArchetype().getId());
		
		//Default to latest blank for product
		ArchetypeVersion archetypeBlank = stacksArchetypeUtil.getArchetype("javaee-web", true, "product", "web-ee7", stacks);
		assertEquals("jboss-javaee6-webapp-blank-archetype-eap",archetypeBlank.getArchetype().getId());
		
		//Default to community
		ArchetypeVersion archetype3 = stacksArchetypeUtil.getArchetype("javaee-web", false, null, "web-ee7", stacks);
		assertEquals("jboss-javaee6-webapp-archetype",archetype3.getArchetype().getId());
		
		//Only one richfaces archetype for everybody
		org.eclipse.wst.server.core.IRuntime wildfly = createRuntime("wildfly");
		ArchetypeVersion richfaces = stacksArchetypeUtil.getArchetype("richfaces-kitchensink", true, wildfly, stacks);
		assertNotNull(richfaces);
		assertEquals("richfaces-archetype-kitchensink",richfaces.getArchetype().getId());

		org.eclipse.wst.server.core.IRuntime tomee = createRuntime("tomee");
		ArchetypeVersion otherRichfaces = stacksArchetypeUtil.getArchetype("richfaces-kitchensink", false, tomee, stacks);
		assertNotNull(otherRichfaces);
		assertSame(richfaces, otherRichfaces);

		otherRichfaces = stacksArchetypeUtil.getArchetype("richfaces-kitchensink", false, eap6, stacks);
		assertNotNull(otherRichfaces);
		assertSame(richfaces, otherRichfaces);
		
		//Dummy EE7 archetype
		ArchetypeVersion archetypeEE7 = stacksArchetypeUtil.getArchetype("javaee-ear", false, wildfly, stacks);
		assertEquals("foo-javaee7-webapp-ear-archetype",archetypeEE7.getArchetype().getId());

		org.eclipse.wst.server.core.IRuntime eap7 = createRuntime(EAP70);
		//Dummy EE7 archetype
		archetypeEE7 = stacksArchetypeUtil.getArchetype("javaee-ear", false, eap7, stacks);
		assertEquals("foo-javaee7-webapp-ear-archetype",archetypeEE7.getArchetype().getId());

	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingParameter() {
		stacksArchetypeUtil.getArchetype(null, false, null, null, stacks);
	}
	
	@Test
	public void getAdditionalRepositories() {
		ArchetypeVersion javaeeEar = stacksArchetypeUtil.getArchetype("javaee-ear", false, "community", "full-ee7", stacks);
		Map<String, String> repos = StacksArchetypeUtil.getAdditionalRepositories(javaeeEar);
		assertNotNull(repos);
		assertEquals(3, repos.size());
		assertEquals("http://maven.repository.redhat.com/techpreview/all/", repos.get("maven-repository-redhat-com-techpreview-all"));
		assertEquals("http://jboss-developer.github.io/temp-maven-repo/", repos.get("jboss-developer-github-io-temp-maven-repo"));
		assertEquals("scp://test123/maven%20repo/", repos.get("test123-maven-repo"));

		ArchetypeVersion javaeeWebArchetype = stacksArchetypeUtil.getArchetype("javaee-web", true, "product", "web-ee6", stacks);
		repos = StacksArchetypeUtil.getAdditionalRepositories(javaeeWebArchetype);
		assertNotNull(repos);
		assertEquals(1, repos.size());
		assertEquals("http://maven.repository.redhat.com/techpreview/all/", repos.get("redhat-techpreview-all-repository"));
	}
	

	@Test
	public void getRequiredDependencies() {
		//Get matching product archetype
		ArchetypeVersion archetype = stacksArchetypeUtil.getArchetype("javaee-web", false, "product", "web-ee6", stacks);
		assertNotNull(archetype);
		assertEquals("jboss-javaee6-webapp-archetype-eap",archetype.getArchetype().getId());
		Set<String> deps = StacksArchetypeUtil.getRequiredDependencies(archetype);
		assertTrue(deps.contains("foo:bar:pom:1.0.0"));
		assertTrue(deps.contains("foo:baz:jar:2.0.0"));
	}
	
	
	//@Test
	public void testGetArchetypeUnsupportedRuntimes() {

		//Always return the latest archetype, even for unsupported servers
		org.eclipse.wst.server.core.IRuntime tomcat = createRuntime("tomcat7");
		ArchetypeVersion archetypeEar = stacksArchetypeUtil.getArchetype("javaee-ear", false, tomcat, stacks);
		assertNotNull(archetypeEar);
		assertEquals("foo-javaee7-webapp-ear-archetype",archetypeEar.getArchetype().getId());
		assertEquals("1.0.1", archetypeEar.getVersion());
		//undefined server
		org.eclipse.wst.server.core.IRuntime glassfish = createRuntime("unsupported");
		archetypeEar = stacksArchetypeUtil.getArchetype("javaee-ear", false, glassfish, stacks);
		assertNotNull(archetypeEar);
		assertEquals("foo-javaee7-webapp-ear-archetype",archetypeEar.getArchetype().getId());
		assertEquals("1.0.1", archetypeEar.getVersion());
		//null server
		archetypeEar = stacksArchetypeUtil.getArchetype("javaee-ear", false, null, stacks);
		assertNotNull(archetypeEar);
		assertEquals("foo-javaee7-webapp-ear-archetype",archetypeEar.getArchetype().getId());
		assertEquals("1.0.1", archetypeEar.getVersion());
	}

	
	@Test
	public void testBlankArchetype() {

		ArchetypeVersion archetypeWeb = stacksArchetypeUtil.getArchetype("javaee-web", false, null, stacks);
		assertTrue(stacksArchetypeUtil.hasBlankArchetype(archetypeWeb, null, stacks));
		
		ArchetypeVersion archetypeEar = stacksArchetypeUtil.getArchetype("javaee-ear", false, null, stacks);
		assertTrue(stacksArchetypeUtil.hasBlankArchetype(archetypeEar, null, stacks));

		ArchetypeVersion archetypeRichfaces = stacksArchetypeUtil.getArchetype("richfaces-kitchensink", false, null, stacks);
		assertFalse(stacksArchetypeUtil.hasBlankArchetype(archetypeRichfaces, null, stacks));

		ArchetypeVersion archetypeErrai = stacksArchetypeUtil.getArchetype("errai-kitchensink", false, null, stacks);
		assertFalse(stacksArchetypeUtil.hasBlankArchetype(archetypeErrai, null, stacks));

	}	

	@Test
	public void testJBIDE16292_getArchetypeMatchingRuntime() {
		org.eclipse.wst.server.core.IRuntime eap6 = createRuntime(EAP60);
		ArchetypeVersion oldMobile = stacksArchetypeUtil.getArchetype("html5-mobile", false, eap6, stacks);
		assertNotNull(oldMobile);
		assertEquals("jboss-html5-mobile-archetype-wfk",oldMobile.getArchetype().getId());
		assertEquals("1.2.3", oldMobile.getVersion());
		
		org.eclipse.wst.server.core.IRuntime eap61 = createRuntime(EAP61);
		ArchetypeVersion communityMobile = stacksArchetypeUtil.getArchetype("html5-mobile", false, eap61, stacks);
		assertNotNull(communityMobile);
		assertEquals("jboss-html5-mobile-archetype",communityMobile.getArchetype().getId());
		assertEquals("7.1.3.Final", communityMobile.getVersion());
		
		org.eclipse.wst.server.core.IRuntime eap62 = createRuntime(EAP62);
		ArchetypeVersion newMobile = stacksArchetypeUtil.getArchetype("html5-mobile", false, eap62, stacks);
		assertNotNull(newMobile);
		assertEquals("jboss-html5-mobile-archetype-wfk",newMobile.getArchetype().getId());
		assertEquals("2.4.0-build-1", newMobile.getVersion());		
	}
	

	@Test
	public void testJBIDE18436_getMatchingStacksRuntimeFromWTP() {

		IPath root = new Path("resources");
		
		//check actual EAP 6.1 is ... 6.1
		org.eclipse.wst.server.core.IRuntime eap61 = createRuntime(EAP61);
		when(eap61.getLocation()).thenReturn(root.append("eap610"));
		Runtime stacksRuntime = StacksArchetypeUtil.getRuntimeFromWtp(stacks, eap61);
		assertNotNull(stacksRuntime);
		assertEquals("jbosseap610runtime",stacksRuntime.getId());

		//check EAP 6.3 which looks like 6.1 is ... 6.3
		org.eclipse.wst.server.core.IRuntime eap63 = createRuntime(EAP61);
		when(eap63.getLocation()).thenReturn(root.append("eap630"));
		stacksRuntime = StacksArchetypeUtil.getRuntimeFromWtp(stacks, eap63);
		assertNotNull(stacksRuntime);
		assertEquals("jbosseap630runtime",stacksRuntime.getId());

		//check micro release matches minor stacks release
		org.eclipse.wst.server.core.IRuntime eap69 = createRuntime(EAP61);
		when(eap69.getLocation()).thenReturn(root.append("eap699"));
		stacksRuntime = StacksArchetypeUtil.getRuntimeFromWtp(stacks, eap69);
		assertNotNull(stacksRuntime);
		assertEquals("jbosseap69xruntime",stacksRuntime.getId());

		//check remote server matches first known matching stacks runtime
		org.eclipse.wst.server.core.IRuntime remoteEap = createRuntime(EAP61);
		when(eap69.getLocation()).thenReturn(null);
		stacksRuntime = StacksArchetypeUtil.getRuntimeFromWtp(stacks, remoteEap);
		assertNotNull(stacksRuntime);
		//returned 1st one in the list of org.jboss.ide.eclipse.as.runtime.eap.61 runtimes
		assertEquals("jbosseap610runtime",stacksRuntime.getId());	
	}
	
	private org.eclipse.wst.server.core.IRuntime createRuntime(String id) {
		org.eclipse.wst.server.core.IRuntime runtime = mock(org.eclipse.wst.server.core.IRuntime.class);
		when(runtime.getId()).thenReturn(id);
		IRuntimeType runtimeType = mock(IRuntimeType.class);
		when(runtimeType.getId()).thenReturn(id);
		when(runtime.getRuntimeType()).thenReturn(runtimeType);
		return runtime;
	}
	

}
