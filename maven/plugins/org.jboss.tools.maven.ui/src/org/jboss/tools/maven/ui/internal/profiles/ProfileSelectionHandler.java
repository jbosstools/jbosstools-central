package org.jboss.tools.maven.ui.internal.profiles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Profile;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.profiles.IProfileManager;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.maven.ui.Messages;

/**
 * Handles profile selection commands. 
 */
public class ProfileSelectionHandler extends AbstractHandler {

	/**
	 * Opens the Maven profile selection Dialog window. 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Set<IMavenProjectFacade> facades = getSelectedMavenProjects(event);
		if (!facades.isEmpty() ) {
			
			System.out.print("Select projects "+facades);
			
			final IProfileManager profileManager = MavenCoreActivator.getDefault().getProfileManager();
			
			Map<Profile, Boolean> availableProfiles;
			Map<Profile, Boolean> availableSettingsProfiles;
			final IMavenProjectFacade facade = facades.iterator().next();
			try {
				availableProfiles = profileManager.getAvailableProfiles(facade);
			
				availableSettingsProfiles = profileManager.getAvailableSettingProfiles();
				
			} catch (CoreException e) {
				throw new ExecutionException("Unable to open the Maven Profile selection dialog", e);
			}
			final SelectProfilesDialog dialog = new SelectProfilesDialog(window.getShell(), 
																	facade, 
																	availableProfiles,
																	availableSettingsProfiles);
		    if(dialog.open() == Window.OK) {
				WorkspaceJob job = new WorkspaceJob(Messages.ProfileManager_Updating_maven_profiles) {

					public IStatus runInWorkspace(IProgressMonitor monitor) {
						try {

							profileManager.updateActiveProfiles(facade, dialog.getSelectedProfiles(), 
									dialog.isOffline(), dialog.isForceUpdate(), monitor); 
						} catch (CoreException ex) {
							Activator.log(ex);
							return ex.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				job.setRule( MavenPlugin.getProjectConfigurationManager().getRule());
				job.schedule();
		    }		
		}
	    return null;
	}

	/**
	 * Returns an IMavenProjectFacade from the selected IResource, or from the active editor 
	 * @param event
	 * @return the selected IMavenProjectFacade
	 */
	private Set<IMavenProjectFacade> getSelectedMavenProjects(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IProject[] projects = getSelectedProjects(selection);
		try {
			if (projects.length == 0) {
			  IEditorInput input = HandlerUtil.getActiveEditorInput(event);
	          if(input instanceof IFileEditorInput) {
	            IFileEditorInput fileInput = (IFileEditorInput) input;
	            projects = new IProject[]{fileInput.getFile().getProject()};
	          }
			}
			Set<IMavenProjectFacade> facades = new HashSet<IMavenProjectFacade>();
			for (IProject p : projects) {
				if (p != null && p.hasNature(IMavenConstants.NATURE_ID)) {
					IMavenProjectFacade facade =MavenPlugin.getMavenProjectRegistry().getProject(p);
					if (facade.getMavenProject() == null) {
						System.err.println(facade.getProject() + " facade has no MavenProject!!!");
					} else {
						facades.add(facade);
					}
				}
			}
			return facades;
		} catch (CoreException e) {
			Activator.log(e);
		}

		return null;
	}

	private IProject[] getSelectedProjects(ISelection selection) {
		Set<IProject> projects = new HashSet<IProject>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator<?> it = structuredSelection.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof IResource) {
		        	projects.add(((IResource) o).getProject());
				} else if (o instanceof IWorkingSet) {
					IAdaptable[] elements = ((IWorkingSet)o).getElements();
					if (elements != null) {
						for (IAdaptable e : elements) {
							IProject p = (IProject) e.getAdapter(IProject.class);
							if (p != null) {
					        	projects.add(p);
							}
						}
					}
				}
			}
		}
		IProject[] array = new IProject[projects.size()];
		projects.toArray(array);
		return array;
	}
	
}
