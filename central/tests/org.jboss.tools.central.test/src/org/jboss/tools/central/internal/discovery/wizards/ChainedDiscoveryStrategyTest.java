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
package org.jboss.tools.central.internal.discovery.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoveryStrategy;
import org.jboss.tools.discovery.core.internal.connectors.ChainedDiscoveryStrategy;
import org.junit.Test;

public class ChainedDiscoveryStrategyTest {

	private static class MockDiscoveryStrategy extends AbstractDiscoveryStrategy implements ProxyWizardDiscoveryStrategy{
		
		private List<ProxyWizard> wizards;

		MockDiscoveryStrategy(List<ProxyWizard> wizards) {
			this.setProxyWizards(wizards);
		}

		@Override
		public void performDiscovery(IProgressMonitor monitor)
				throws CoreException {
		}

		public List<ProxyWizard> getProxyWizards() {
			return wizards;
		}

		public void setProxyWizards(List<ProxyWizard> wizards) {
			this.wizards = wizards;
		}
	}
	
	private static class MockFailingDiscoveryStrategy extends MockDiscoveryStrategy {

		private String message;

		MockFailingDiscoveryStrategy(String message) {
			super(null);
			this.message = message;
		}

		@Override
		public void performDiscovery(IProgressMonitor monitor)
				throws CoreException {
			throw new CoreException(new Status(IStatus.ERROR, "foo", message));
		}
	}

	@Test
	public void testChain() throws Exception {
		ProxyWizardDataCollector dataCollector = new ProxyWizardDataCollector();
		ChainedDiscoveryStrategy chain = new ChainedDiscoveryStrategy(dataCollector);
		chain.addStrategy(new MockDiscoveryStrategy(null));
		chain.addStrategy(new MockDiscoveryStrategy(Collections.<ProxyWizard>emptyList()));
		List<ProxyWizard> expectedWizards = createMockProxyWizards(2);
		chain.addStrategy(new MockDiscoveryStrategy(expectedWizards));
		chain.addStrategy(new MockDiscoveryStrategy(createMockProxyWizards(3)));
		
		chain.performDiscovery(null);
		
		List<ProxyWizard> result = dataCollector.getProxyWizards();
		assertEquals(expectedWizards, result);
	}
	
	@Test
	public void testNoResults() throws Exception {
		ProxyWizardDataCollector dataCollector = new ProxyWizardDataCollector();
		ChainedDiscoveryStrategy chain = new ChainedDiscoveryStrategy(dataCollector);
		chain.addStrategy(new MockFailingDiscoveryStrategy("message1"));
		chain.addStrategy(new MockDiscoveryStrategy(null));
		chain.performDiscovery(null);
		List<ProxyWizard> result = dataCollector.getProxyWizards();
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testExceptions() {
		ProxyWizardDataCollector dataCollector = new ProxyWizardDataCollector();
		ChainedDiscoveryStrategy chain = new ChainedDiscoveryStrategy(dataCollector);
		chain.addStrategy(new MockFailingDiscoveryStrategy("message1"));
		chain.addStrategy(new MockFailingDiscoveryStrategy("message2"));
		try {
			chain.performDiscovery(null);
			fail("An exception was expected");
		} catch (CoreException ce) {
			assertTrue(ce.getStatus() instanceof MultiStatus);
			IStatus[] statuses = ce.getStatus().getChildren();
			assertEquals(2, statuses.length);
			assertEquals("message1", statuses[0].getException().getMessage());
			assertEquals("message2", statuses[1].getException().getMessage());
		}
	}
	
	private List<ProxyWizard> createMockProxyWizards(int n) {
	   List<ProxyWizard> list = new ArrayList<ProxyWizard>(n); 
	   for (int i = 0; i < n; i++) {
		   ProxyWizard pw = new ProxyWizard();
		   String id = "wizard-"+i;
		   pw.setId(id);
		   pw.setDescription(id);
		   pw.setLabel(id);
		   pw.setPriority(i);
		   pw.setRequiredComponentIds(Collections.singletonList(id));
		   pw.setWizardId(id);
		   list.add(pw);
	   }
	   return list;
	}
}
