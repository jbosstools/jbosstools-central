package org.jboss.tools.maven.sourcelookup.internal.identification;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.sourcelookup.identification.ArtifactIdentifier;

public class MavenPropertiesIdentifier implements ArtifactIdentifier {

	@Override
	public ArtifactKey identify(File file) throws CoreException {
		ZipFile jar;
		try {
//			try {
//				Random r = new Random();
//				Thread.sleep(r.nextInt(10)*1000);
//			} catch (InterruptedException e) {
//			}
			jar = new ZipFile(file);
			return getArtifactFromMetaInf(jar);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static ArtifactKey getArtifactFromMetaInf(ZipFile jar) throws IOException {
		ZipEntry mavenEntry = jar.getEntry("META-INF/maven");//$NON-NLS-1$
		if (mavenEntry == null) {
			return null;
		}
		String entryName = mavenEntry.getName();
		Enumeration<? extends ZipEntry> zipEntries = jar.entries();
		ArtifactKey artifact = null;
		
		
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();
			if (zipEntry.getName().endsWith("pom.properties")
					&& zipEntry.getName().startsWith(entryName)) {
				if (artifact != null) {
					//org.fusesource.jansi:jansi:1.6 is an OSGi bundle containing several maven pom files.
					//The first properties being found is wrong.
					//So for the moment we bail but should try to look at the MANIFEST.MF
					return null;
				}
				Properties props = new Properties();
				props.load(jar.getInputStream(zipEntry));
				String groupId = props.getProperty("groupId");
				String artifactId = props.getProperty("artifactId");
				String version = props.getProperty("version");
				String classifier = props.getProperty("classifier");
				if (groupId != null && artifactId != null && version != null) {
					artifact = new ArtifactKey(groupId, artifactId, version,
							classifier);
				}
			}
		}
		
		return artifact;
	}
}
