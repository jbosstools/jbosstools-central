package org.jboss.tools.maven.jdt.configurators;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.internal.ClasspathDescriptor;
import org.eclipse.m2e.jdt.internal.ClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.jboss.tools.maven.jdt.MavenJdtActivator;

public class ClasspathHelpers {

	public static final String CONTAINER_ID = MavenJdtActivator.PLUGIN_ID
			+ ".ENDORSED_LIB_CLASSPATH_CONTAINER";

	public static boolean isEndorsedDirsClasspathContainer(IPath containerPath) {
		return containerPath != null && containerPath.segmentCount() > 0
				&& CONTAINER_ID.equals(containerPath.segment(0));
	}

	public static IClasspathEntry getDefaultContainerEntry() {
		return JavaCore.newContainerEntry(new Path(CONTAINER_ID));
	}

	public static IClasspathContainer getEndorsedDirsClasspathContainer(
			IJavaProject project) throws JavaModelException {
		IClasspathEntry[] entries = project.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER
					&& isEndorsedDirsClasspathContainer(entry.getPath())) {
				return JavaCore.getClasspathContainer(entry.getPath(), project);
			}
		}
		return null;
	}

	public static IClasspathEntry addEndorsedLibClasspathContainer(IClasspathDescriptor classpath) {
		IClasspathEntry cpe = getDefaultContainerEntry();
		ClasspathEntryDescriptor entry = new ClasspathEntryDescriptor(cpe);
		entry.setClasspathAttribute("org.eclipse.jst.component.nondependency", "");
		classpath.getEntryDescriptors().add(0,entry);
		return cpe;
	}

	public static void removeEndorsedLibClasspathContainer(	IClasspathDescriptor classpath ) {
		// remove any old endorsed dirs container entries
		classpath.removeEntry(new ClasspathDescriptor.EntryFilter() {
			public boolean accept(IClasspathEntryDescriptor entry) {
				return isEndorsedDirsClasspathContainer(entry.getPath());
			}
		});
	}

	public static IClasspathEntry getEndorsedDirsContainerEntry( IJavaProject javaProject ) {
		if (javaProject != null) {
			try {
				for (IClasspathEntry entry : javaProject.getRawClasspath()) {
					if (isEndorsedDirsClasspathContainer(entry.getPath())) {
						return entry;
					}
				}
			} catch (JavaModelException ex) {
				return null;
			}
		}
		return null;
	}

	public static void removeEndorsedLibClasspathContainer(IProject project) throws JavaModelException {
	    IJavaProject javaProject = JavaCore.create(project);
	    if(javaProject != null) {
	      // remove classpatch container from JavaProject
	      ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
	      for(IClasspathEntry entry : javaProject.getRawClasspath()) {
	        if(!isEndorsedDirsClasspathContainer(entry.getPath())) {
	          newEntries.add(entry);
	        }
	      }
	      javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), null);
	    }
	  }		
}
