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
		assertEquals("arquillian-core-spi", key.getArtifactId());
		assertEquals("1.0.1.Final", key.getVersion());
	}

}
