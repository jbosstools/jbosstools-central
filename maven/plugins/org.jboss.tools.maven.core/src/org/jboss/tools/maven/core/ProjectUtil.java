package org.jboss.tools.maven.core;

import java.io.File;
import java.net.URI;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

/**
 * A utility class for Eclipse Projects.
 * @author Xavier Coulon
 *
 */
public class ProjectUtil {
	
	/**
	 * Refreshes the projects hierarchy. For example, if the project on
	 * which a facet should be installed is 'Parent1/Parent2/Child',
	 * then both Parent1, Parent2 and Child are refreshed.
	 * 
	 * @param basedir : the base directory (absolute file system path) of the (child) project to refresh.
	 * @param refreshDepth: the refresh depth
	 * @param monitor : the progress monitor
	 * @return the number of projects that were refreshed
	 * @throws CoreException
	 *             in case of problem during refresh
	 * @see IResource for depth values.
	 */
	public static int refreshHierarchy(File basedir, int refreshDepth, IProgressMonitor monitor) throws CoreException {
		try {
			int count = 0;
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IProject[] projects = root.getProjects();
			final IPath rootLocation = root.getLocation();
			IPath basedirPath = new Path(basedir.getAbsolutePath());
			while(basedirPath.matchingFirstSegments(rootLocation) > 0) {
				for(IProject project : projects) {
					final IPath projectLocation = project.getLocation();
					if(projectLocation.equals(basedirPath) && project.isOpen()) {
						project.refreshLocal(refreshDepth, monitor);
						count++;
						break;
					}
				}
				basedirPath = basedirPath.removeLastSegments(1);
			}
			return count;
		} finally {
			monitor.done();
		}
	}
	
}
