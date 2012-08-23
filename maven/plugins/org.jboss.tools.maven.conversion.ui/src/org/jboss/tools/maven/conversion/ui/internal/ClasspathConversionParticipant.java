package org.jboss.tools.maven.conversion.ui.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.core.project.conversion.AbstractProjectConversionParticipant;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.maven.conversion.ui.dialog.ConvertToMavenDependencyWizard;

public class ClasspathConversionParticipant extends
		AbstractProjectConversionParticipant {

	@Override
	public boolean accept(IProject project) throws CoreException {
		return project.hasNature(JavaCore.NATURE_ID);
	}

	@Override
	public void convert(final IProject project, final Model model, final IProgressMonitor monitor)
			throws CoreException {

		if (accept(project)) {
			
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] classpath = javaProject.getRawClasspath();
			Set<IClasspathEntry> entries = new LinkedHashSet<IClasspathEntry>(classpath.length);
			for (IClasspathEntry cpe : classpath) {
				if (isValid(cpe)) {
					entries.add(cpe);
				}
			}
			
			if (!hasDependencies(javaProject, entries)) {
				return;
			}
			
			final ConvertToMavenDependencyWizard conversionWizard = new ConvertToMavenDependencyWizard(project, entries);
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					WizardDialog dialog = new WizardDialog(shell, conversionWizard);
					if (dialog.open() == Window.OK) {
						List<Dependency> dependencies = conversionWizard.getDependencies();
						if (dependencies != null && !dependencies.isEmpty()) {
							model.setDependencies(dependencies);
						}
					}
				}
			});
		}
	}

	private boolean hasDependencies(IJavaProject javaProject, Collection<IClasspathEntry> initialEntries) throws JavaModelException {
		for (IClasspathEntry entry : initialEntries) {
			if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY && entry.getPath() != null)
					|| (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
				return true;
			} else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject );
				if (container != null) {
					IClasspathEntry[] cpes = container.getClasspathEntries();
					if (cpes != null && cpes.length > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isValid(IClasspathEntry cpe) {
	   
       if(IClasspathEntry.CPE_CONTAINER == cpe.getEntryKind()
            && ("org.eclipse.jdt.launching.JRE_CONTAINER".equals(cpe.getPath().segment(0))
            || MavenClasspathHelpers.isMaven2ClasspathContainer(cpe.getPath()))) {
            	return false;
       }
       if (IClasspathEntry.CPE_SOURCE == cpe.getEntryKind()) {
    	   return false;
       }
       return true;
	}

}
