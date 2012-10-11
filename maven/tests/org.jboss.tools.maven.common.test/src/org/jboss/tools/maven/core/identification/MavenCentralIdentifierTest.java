/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.identification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.internal.identification.MavenCentralIdentifier;
import org.junit.Test;

public class MavenCentralIdentifierTest extends AbstractIdentificationTest {

	@Test
	public void testIdentify() throws Exception {
		MavenCentralIdentifier identifier = new MavenCentralIdentifier();
		ArtifactKey key;
	
		key = identifier.identify(groovy_jsr223, null);
		assertNotNull("groovy-jsr223 was not identifed", key);
		assertEquals("groovy-jsr223", key.getArtifactId());
		assertEquals("org.codehaus.groovy", key.getGroupId());
		assertEquals("2.0.4", key.getVersion());
	}

	
}
