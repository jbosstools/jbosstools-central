package org.jboss.tools.maven.ui.internal.profiles;

import java.util.Map;

import org.apache.maven.model.Profile;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
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
		final IMavenProjectFacade facade = getSelectedMavenProject(event);
		if (facade != null ) {
			
			final IProfileManager profileManager = MavenCoreActivator.getDefault().getProfileManager();
			
			Map<Profile, Boolean> availableProfiles;
			Map<Profile, Boolean> availableSettingsProfiles;

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
	private IMavenProjectFacade getSelectedMavenProject(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IProject project = getSelectedProject(selection);
		try {
			if (project == null) {
			  IEditorInput input = HandlerUtil.getActiveEditorInput(event);
	          if(input instanceof IFileEditorInput) {
	            IFileEditorInput fileInput = (IFileEditorInput) input;
	            project = fileInput.getFile().getProject();
	          }
			}
			if (project != null && project.hasNature(IMavenConstants.NATURE_ID)) {
				return MavenPlugin.getMavenProjectRegistry().getProject(project);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}

		return null;
	}

	private IProject getSelectedProject(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement(); 
			if (firstElement instanceof IResource) {
				return ((IResource) firstElement).getProject();
			}
		}
		return null;
	}
	
}
