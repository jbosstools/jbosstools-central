/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
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
import static org.junit.Assert.assertNull;

import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.internal.identification.MavenPropertiesIdentifier;
import org.junit.Test;

public class MavenPropertiesIdentifierTest extends AbstractIdentificationTest {

	@Test
	public void testIdentify() throws Exception {
		MavenPropertiesIdentifier identifier = new MavenPropertiesIdentifier();
		//Has No Maven Properties 
		assertNull(identifier.identify(junit));
		
		//Has Multiple Maven Properties
		assertNull(identifier.identify(jansi));
		
		ArtifactKey key = identifier.identify(arquillian);
		assertEquals("arquillian-core-spi", key.artifactId());
		assertEquals("1.7.0.Final-SNAPSHOT", key.version());
	}

}
