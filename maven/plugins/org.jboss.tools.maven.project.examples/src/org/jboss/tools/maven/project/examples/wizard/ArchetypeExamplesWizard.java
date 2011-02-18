package org.jboss.tools.maven.project.examples.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.IProgressConstants;
import org.jboss.tools.project.examples.job.ProjectExamplesJob;
import org.jboss.tools.project.examples.model.Project;
import org.maven.ide.eclipse.MavenImages;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.actions.OpenMavenConsoleAction;
import org.maven.ide.eclipse.core.Messages;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;

public class ArchetypeExamplesWizard extends Wizard implements INewWizard {

	private Project projectDescription;
	private File location;
	private ProjectImportConfiguration configuration;
	private ArchetypeExamplesWizardPage wizardPage;

	public ArchetypeExamplesWizard(File location, Project projectDescription) {
		super();
		setWindowTitle("Project Examples Archetype");
		setDefaultPageImageDescriptor(MavenImages.WIZ_NEW_PROJECT);
		setNeedsProgressMonitor(true);
		this.location = location;
		this.projectDescription = projectDescription;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		final Model model = wizardPage.getModel();
		final String groupId = model.getGroupId();
		final String artifactId = model.getArtifactId();
		final String version = model.getVersion();
		final String javaPackage = wizardPage.getJavaPackage();
		final Properties properties = wizardPage.getProperties();
		final MavenPlugin plugin = MavenPlugin.getDefault();
		final Archetype archetype = wizardPage.getArchetype();
		final String projectName = configuration.getProjectName(model);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(final IProgressMonitor monitor)
					throws CoreException {
				plugin.getProjectConfigurationManager().createArchetypeProject(
						project, new Path(location.getAbsolutePath()), archetype,
						groupId, artifactId, version, javaPackage, properties,
						configuration, monitor);
			}
		};
		
		final IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					final IWorkspace ws = ResourcesPlugin.getWorkspace();
					ws.run(wr, ws.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		}

		return true;
	}

	public void addPages() {
	    configuration = new ProjectImportConfiguration();
	    wizardPage = new ArchetypeExamplesWizardPage(configuration, projectDescription);
	    addPage(wizardPage);
	}
}
