/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.fixes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.server.core.internal.JavaServerPlugin;
import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
import org.eclipse.jst.server.core.internal.RuntimeClasspathProviderWrapper;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.tools.portlet.core.internal.PortletRuntimeComponentProvider;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;

/**
 * 
 * @author snjeza
 *
 */
public class WTPRuntimeFix implements ProjectExamplesFix {

	private static final String TEIID_DEPLOYER = "teiid.deployer"; //$NON-NLS-1$
	private static final String RIFTSAW_SAR = "riftsaw.sar"; //$NON-NLS-1$
	private static final String BPEL = "bpel"; //$NON-NLS-1$
	private static final String TEIID = "teiid"; //$NON-NLS-1$
	private static final String JBOSSESB_SAR = "jbossesb.sar"; //$NON-NLS-1$
	private static final String JBOSSESB_ESB = "jbossesb.esb"; //$NON-NLS-1$
	private static final String ESB = "esb"; //$NON-NLS-1$
	private static final String PORTLET = "portlet"; //$NON-NLS-1$
	private static final String REQUIRED_COMPONENTS = "required-components"; //$NON-NLS-1$
	private static final IPath ESB_SERVER_SUPPLIED_CONTAINER_PATH = new Path("org.jboss.esb.runtime.classpath/server.supplied"); //$NON-NLS-1$

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
				if (facetedProject == null) {
					continue;
				}
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
							fixEsb(eclipseProject, fix, wtpRuntime);
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

	private void fixEsb(IProject eclipseProject,
			ProjectFix fix, org.eclipse.wst.common.project.facet.core.runtime.IRuntime wtpRuntime) throws JavaModelException {
		String required_components = fix.getProperties().get(REQUIRED_COMPONENTS);
		if (required_components == null) {
			return;
		} 
		List<String> components = tokenize(required_components);
		if (components == null) {
			return;
		} 
		boolean esbRequired = false;
		for (String component:components) {
			if (ESB.equals(component)) {
				esbRequired = true;
				break;
			}
		}
		if (esbRequired) {
			IJavaProject javaProject = JavaCore.create(eclipseProject);
			if (javaProject != null) { 
				if (!javaProject.isOpen()) {
					javaProject.open(null);
				}
				IClasspathEntry[] entries = javaProject.getRawClasspath();
				IClasspathEntry[] newEntries = new IClasspathEntry[entries.length];
				boolean changed = false;
				IRuntime runtime = getRuntime(wtpRuntime);
				for (int i = 0; i < entries.length; i++) {
					IClasspathEntry entry = entries[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path = entry.getPath();
						if (new Path(RuntimeClasspathContainer.SERVER_CONTAINER).isPrefixOf(path)) {
							RuntimeClasspathProviderWrapper rcpw = JavaServerPlugin.findRuntimeClasspathProvider(runtime.getRuntimeType());
							IPath serverContainerPath = new Path(RuntimeClasspathContainer.SERVER_CONTAINER)
								.append(rcpw.getId()).append(runtime.getId());
							newEntries[i] = JavaCore.newContainerEntry(serverContainerPath);
							changed = true;
						} else if (ESB_SERVER_SUPPLIED_CONTAINER_PATH.isPrefixOf(path)) {
							IPath esbContainerPath = ESB_SERVER_SUPPLIED_CONTAINER_PATH.append(runtime.getId());
							newEntries[i] = JavaCore.newContainerEntry(esbContainerPath);
							changed = true;
						} else {
							newEntries[i] = entry;
						}
					} else {
						newEntries[i] = entry;
					}
				}
				if (changed) {
					javaProject.setRawClasspath(newEntries, new NullProgressMonitor());
				}
			}
		}
	}

	private IRuntime getBestRuntime(Project project, ProjectFix fix) {
		String allowedTypes = fix.getProperties().get(
				ProjectFix.ALLOWED_TYPES);
		if (allowedTypes == null) {
			ProjectExamplesActivator.log(NLS.bind(Messages.WTPRuntimeFix_Invalid_WTP_runtime_fix, project.getName()));
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
				for (IRuntime runtime:runtimes) {
					IRuntime componentPresent = isComponentPresent(fix, runtime);
					if (componentPresent != null) {
						return isComponentPresent(fix, runtime);
					}
				}
				return null;
			}
			for (int i = 0; i < runtimes.length; i++) {
				IRuntime runtime = runtimes[i];
				IRuntimeType runtimeType = runtime.getRuntimeType();
				if (runtimeType.getId().equals(allowedType)) {
					IRuntime componentPresent = isComponentPresent(fix, runtime);
					if (componentPresent != null) {
						return isComponentPresent(fix, runtime);
					}
				}
			}
		}
		return null;
	}

	private IRuntime isComponentPresent(ProjectFix fix, IRuntime runtime) {
		String required_components = fix.getProperties().get(REQUIRED_COMPONENTS);
		if (required_components == null) {
			return runtime;
		} 
		List<String> components = tokenize(required_components);
		if (components == null) {
			return runtime;
		} 
		File location = null;
		if (runtime != null && runtime.getLocation() != null) {
			location = runtime.getLocation().toFile();
		} else {
			return null;
		}
		for (String component:components) {
			if (PORTLET.equals(component)) {	
				if (!PortletRuntimeComponentProvider.isPortalPresent(location, runtime, PortletRuntimeComponentProvider.IS_PORTLET_RUNTIME)) {
					return null;
				}
			}
			if (ESB.equals(component)) {
				if (!isEsbPresent(location, runtime)) {
					return null;
				}
			}
			if (BPEL.equals(component)) {
				if (!isBpelPresent(location, runtime)) {
					return null;
				}
			}
			if (TEIID.equals(component)) {
				if (!isTeiidPresent(location, runtime)) {
					return null;
				}
			}
			return runtime;
		}
		return null;
	}

	private boolean isBpelPresent(File location, IRuntime runtime) {
		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if (jbossRuntime != null) {
			IPath jbossLocation = runtime.getLocation();
			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
			File configFile = configPath.toFile();
			return exists(configFile, RIFTSAW_SAR);
		}
		return false;
	}

	private List<String> tokenize(String requiredComponents) {
		StringTokenizer tokenizer = new StringTokenizer(requiredComponents, ","); //$NON-NLS-1$
		List<String> components = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			components.add(tokenizer.nextToken().trim());
		}
		return components;
	}

	public static boolean isEsbPresent(final File location,
			IRuntime runtime) {
		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if (jbossRuntime != null) {
			IPath jbossLocation = runtime.getLocation();
			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
			File configFile = configPath.toFile();
			return exists(configFile, JBOSSESB_ESB) && exists(configFile, JBOSSESB_SAR);
		}
		return false;
	}
	
	public static boolean isTeiidPresent(final File location,
			IRuntime runtime) {
		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if (jbossRuntime != null) {
			IPath jbossLocation = runtime.getLocation();
			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
			IPath deployersPath = configPath.append(IJBossRuntimeResourceConstants.DEPLOYERS);
			File deployersFile = deployersPath.toFile();
			File teiidFile = new File(deployersFile, TEIID_DEPLOYER);
			return teiidFile.exists();
		}
		return false;
	}

	private static boolean exists(final File location,String esbDir) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			esbDir = esbDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File deployFile = new File(location,IJBossServerConstants.DEPLOY);
		if (!deployFile.exists() && !deployFile.isDirectory()) {
			return false;
		}
		File file = new File(deployFile,esbDir);
		return file.exists();
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
