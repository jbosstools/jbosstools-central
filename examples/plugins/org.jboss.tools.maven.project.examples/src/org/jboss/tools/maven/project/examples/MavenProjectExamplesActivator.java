package org.jboss.tools.maven.project.examples;

import static org.jboss.tools.maven.project.examples.Messages.MavenProjectExamplesActivator_Downloading_Examples_Wizards_Metadata;
import static org.jboss.tools.maven.project.examples.Messages.MavenProjectExamplesActivator_Error_Retrieving_Stacks_MetaData;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.maven.project.examples.offline.MavenOfflinePropertyChangeListener;
import org.jboss.tools.maven.project.examples.xpl.UpdateMavenProjectJob;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.stacks.core.model.StacksManager;
import org.jboss.tools.stacks.core.model.StacksManager.StacksType;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenProjectExamplesActivator extends AbstractUIPlugin {
 
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.project.examples"; //$NON-NLS-1$

	// The shared instance
	private static MavenProjectExamplesActivator plugin;

	private MavenOfflinePropertyChangeListener mavenOfflinePropertyChangeListener;
	
	private Stacks stacks; 
	
	private long lastUpdate;
	
	private static long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 1000;// 1 hour
	
	/**
	 * Returns a cached {@link Stacks} model. Cache is evicted after 60 minutes.
	 * Be aware Stacks updates occur within a {@link ProgressMonitorDialog}. 
	 * 
	 * @provisional this method can be removed without warning 
	 */
	public synchronized Stacks getCachedStacks() {
		if (System.currentTimeMillis() - lastUpdate > DEFAULT_CACHE_TIMEOUT) {
			stacks = null;
		}
		
		if (stacks == null) {
		    final Stacks[] s = new Stacks[1];
		    ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());  
		    try {  
		        progressDialog.run(true, true, new IRunnableWithProgress() {  

		        @Override  
		        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		            monitor.beginTask(MavenProjectExamplesActivator_Downloading_Examples_Wizards_Metadata, 100);  
		            try {  
		            	Stacks[] result = new StacksManager().getStacks(MavenProjectExamplesActivator_Downloading_Examples_Wizards_Metadata, monitor, StacksType.STACKS_TYPE);
		            	if(result != null && result.length > 0) {
		            		s[0] = result[0]; 
		            	}
		            } catch (Exception e) {  
		                log(e, MavenProjectExamplesActivator_Error_Retrieving_Stacks_MetaData);  
		            }  
		            monitor.done();   
		        }  
		     });
		        
	        } catch (InvocationTargetException ex) {
	            log(ex, MavenProjectExamplesActivator_Error_Retrieving_Stacks_MetaData);  
	        } catch (InterruptedException ex) {
	            //Ignore
	        }
		    if (s[0] != null) {
		    	stacks = s[0];
		    	lastUpdate = System.currentTimeMillis();		    	
		    }
		} 
		return stacks;
	}

	/**
	 * The constructor
	 */
	public MavenProjectExamplesActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		mavenOfflinePropertyChangeListener = new MavenOfflinePropertyChangeListener();
		ProjectExamplesActivator.getDefault().getPreferenceStore().addPropertyChangeListener(mavenOfflinePropertyChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		ProjectExamplesActivator.getDefault().getPreferenceStore().removePropertyChangeListener(mavenOfflinePropertyChangeListener);
		mavenOfflinePropertyChangeListener = null;
		stacks = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MavenProjectExamplesActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}
	
	public static String getProjectName(MavenProjectInfo projectInfo,
			ProjectImportConfiguration configuration) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		File pomFile = projectInfo.getPomFile();
		Model model = projectInfo.getModel();
		IMaven maven = MavenPlugin.getMaven();
		if (model == null) {
			model = maven.readModel(pomFile);
			projectInfo.setModel(model);
		}

		String projectName = configuration.getProjectName(model);

		File projectDir = pomFile.getParentFile();
		String projectParent = projectDir.getParentFile().getAbsolutePath();

		if (projectInfo.getBasedirRename() == MavenProjectInfo.RENAME_REQUIRED) {
			File newProject = new File(projectDir.getParent(), projectName);
			if (!projectDir.equals(newProject)) {
				projectDir = newProject;
			}
		} else {
			if (projectParent.equals(root.getLocation().toFile()
					.getAbsolutePath())) {
				projectName = projectDir.getName();
			}
		}
		return projectName;
	}

	public static ImageDescriptor getNewWizardImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/new_wiz.png"); //$NON-NLS-1$
	}
	
	public static void updateMavenConfiguration(String projectName, List<String> includedProjects,final IProgressMonitor monitor) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project != null && project.isAccessible()) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				// ignore
			}
		}
		IProject[] selectedProjects = new IProject[includedProjects.size()+1];
		selectedProjects[0] = project;
		if (includedProjects.size() > 0) {
			int i = 1;
			
			for (String selectedProjectName:includedProjects) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectName);
				selectedProjects[i++] = project;
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					// ignore
				}
			}
		}
		Job updateJob = new UpdateMavenProjectJob(selectedProjects);
		updateJob.schedule();
		try {
			updateJob.join();
		} catch (InterruptedException e) {
			// ignore
		}
			
	}
}
