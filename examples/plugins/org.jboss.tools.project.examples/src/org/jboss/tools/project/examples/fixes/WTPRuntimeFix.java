package org.jboss.tools.project.examples.fixes;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;

public class WTPRuntimeFix implements ProjectExamplesFix {

	public boolean canFix(Project project, ProjectFix fix) {
		if (!ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
			return false;
		}
		return getBestRuntime(project, fix) != null;
	}

	public boolean fix(Project project, ProjectFix fix,
			IProgressMonitor monitor) {
		if (!canFix(project, fix)) {
			return false;
		}
		IProject[] eclipseProjects = ProjectExamplesActivator.getEclipseProject(project, fix);
		if (eclipseProjects.length == 0) {
			return false;
		}
		boolean ret = true;
		for (int i = 0; i < eclipseProjects.length; i++) {
			IProject eclipseProject = eclipseProjects[i];
			try {
				IFacetedProject facetedProject = ProjectFacetsManager.create(eclipseProject);
				org.eclipse.wst.common.project.facet.core.runtime.IRuntime wtpRuntime = facetedProject.getPrimaryRuntime();
				if (wtpRuntime != null) {
					IRuntime runtime = getRuntime(wtpRuntime);
					if (runtime == null) {
						runtime = getBestRuntime(project, fix);
						if (runtime != null) {
							facetedProject.removeTargetedRuntime(wtpRuntime, monitor);
							wtpRuntime = RuntimeManager.getRuntime(runtime.getId());
							facetedProject.addTargetedRuntime(wtpRuntime, monitor);
							facetedProject.setPrimaryRuntime(wtpRuntime, monitor);
						}
					}
				}
			} catch (CoreException e) {
				ProjectExamplesActivator.log(e);
				ret = false;
			}
		}
		return ret;
	}

	private IRuntime getBestRuntime(Project project, ProjectFix fix) {
		String allowedTypes = fix.getProperties().get(
				ProjectFix.ALLOWED_TYPES);
		if (allowedTypes == null) {
			ProjectExamplesActivator.log("Invalid WTP runtime fix in "
					+ project.getName() + ".");
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(allowedTypes, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String allowedType = tokenizer.nextToken().trim();
			if (allowedType.length() <= 0) {
				continue;
			}
			IRuntime[] runtimes = ServerCore.getRuntimes();
			if (runtimes.length > 0
					&& ProjectFix.ANY.equals(allowedType)) {
				return runtimes[0];
			}
			for (int i = 0; i < runtimes.length; i++) {
				IRuntime runtime = runtimes[i];
				IRuntimeType runtimeType = runtime.getRuntimeType();
				if (runtimeType.getId().equals(allowedType)) {
					return runtime;
				}
			}
		}
		return null;
	}

	private static IRuntime getRuntime(
			org.eclipse.wst.common.project.facet.core.runtime.IRuntime runtime) {
		if (runtime == null)
			throw new IllegalArgumentException();

		String id = runtime.getProperty("id"); //$NON-NLS-1$
		if (id == null)
			return null;

		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (IRuntime r : runtimes) {
			if (id.equals(r.getId()))
				return r;
		}

		return null;
	}
}
