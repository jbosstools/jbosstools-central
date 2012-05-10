package org.jboss.tools.maven.jdt.configurators;

import java.io.File;
import java.io.FilenameFilter;

public class JarFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		return name.endsWith(".jar") || name.endsWith(".zip");
	}

}
