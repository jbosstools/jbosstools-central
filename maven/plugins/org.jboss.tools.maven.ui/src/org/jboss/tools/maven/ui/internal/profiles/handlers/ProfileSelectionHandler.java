package org.jboss.tools.maven.ui.internal.profiles.handlers;

import java.util.Map;

import org.apache.maven.model.Profile;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.jboss.tools.maven.ui.internal.profiles.IProfileManager;
import org.jboss.tools.maven.ui.internal.profiles.ProfileManager;
import org.jboss.tools.maven.ui.internal.profiles.SelectProfilesDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ProfileSelectionHandler extends AbstractHandler {

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IMavenProjectFacade facade = getSelectedMavenProject(event);
		if (facade != null) {
			
			IProfileManager profileManager = new ProfileManager();
			
			Map<Profile, Boolean> availableProfiles;
			Map<Profile, Boolean> availableSettingsProfiles;
			facade.getMavenProject().getActiveProfiles();
			try {
				availableProfiles = profileManager.getAvailableProfiles(facade);
			
				availableSettingsProfiles = profileManager.getAvailableSettingProfiles();
				
			} catch (CoreException e) {
				throw new ExecutionException("Unable to open the Maven Profile selection dialog", e);
			}
			SelectProfilesDialog dialog = new SelectProfilesDialog(window.getShell(), 
																	facade, 
																	availableProfiles,
																	availableSettingsProfiles);
		    if(dialog.open() == Window.OK) {
		    	try {
					profileManager.updateActiveProfiles(facade, dialog.getSelectedProfiles(), 
							dialog.isOffline(), dialog.isForceUpdate());
				} catch (CoreException e) {
					throw new ExecutionException("Unable to update the profiles for "+facade.getProject().getName(), e);
				}
		    }		
		}
	    return null;
	}

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
			e.printStackTrace();
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
