package org.jboss.tools.maven.ui.internal.profiles;

import java.util.List;

import org.apache.maven.model.Profile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class ActiveProfilesContentProvider implements
ITreeContentProvider  {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IProject) {
			IProject project = (IProject) inputElement;
			try {
				if (project.isAccessible() && project.hasNature(IMavenConstants.NATURE_ID)) {
					IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
					if (facade != null && facade.getMavenProject() != null) {
						List<Profile> profiles = facade.getMavenProject().getActiveProfiles();
						return new Object[] { new ActiveMavenProfilesNode(profiles)};
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return true;
	}

}
