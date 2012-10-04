package org.jboss.tools.maven.core.identification;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileIdentificationManagerTest extends AbstractIdentificationTest {
	
	private FileIdentificationManager fileIdentificationManager;
	
	private IProgressMonitor monitor;
	
	@Before
	public void setUp() {
		ArtifactIdentifier identifier1 = new ArtifactIdentifier() {
			@Override
			public ArtifactKey identify(File file) throws CoreException {
				return null;
			}

			@Override
			public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
		};
		monitor = new NullProgressMonitor();
		fileIdentificationManager = new FileIdentificationManager(Collections.singleton(identifier1));
	}
	
	@Test
	public void testIdentify() throws Exception {
		
		assertNull(fileIdentificationManager.identify(junit, monitor));
		
		fileIdentificationManager.addArtifactIdentifier(
			new ArtifactIdentifier() {
				@Override
				public ArtifactKey identify(File file) throws CoreException {
					return new ArtifactKey("junit", "junit", "4.10", null);
				}

				@Override
				public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
					// TODO Auto-generated method stub
					return null;
				}
			}
		);
		assertNotNull(fileIdentificationManager.identify(junit, monitor));
		
	}
	
	@After
	public void tearDown() {
		fileIdentificationManager = null;
	}

}
