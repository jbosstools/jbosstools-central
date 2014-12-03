/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.fixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.fixes.UnsupportedFixProvider;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.model.RequirementModel;

public class ProjectFixManager {

	private static final IProjectFixProvider UNSUPPORTED_FIX_PROVIDER = new UnsupportedFixProvider();
	
	public void fix(ProjectExample example, IProgressMonitor monitor) {
		List<IProjectExamplesFix> fixes = ((ProjectExampleWorkingCopy)example).getFixes();

		if (fixes == null || fixes.isEmpty()) {
			List<RequirementModel> reqs = example.getRequirements();
			if (reqs == null || reqs.isEmpty()) {
				return;
			}
			fixes = new ArrayList<>(reqs.size());
			Map<String, IProjectFixProvider> fixProviders = getFixProviders();
			for (RequirementModel requirement : reqs) {
				if (monitor.isCanceled()) {
					return;
				}
				IProjectExamplesFix fix = getFix(example, requirement, fixProviders);
				if (fix != null) {
					fixes.add(fix);
				}
			}
		}
		fix(fixes, monitor);
	}

	protected void fix(Collection<IProjectExamplesFix> fixes, IProgressMonitor monitor) {
		for (IProjectExamplesFix fix : fixes) {
			if (monitor.isCanceled()) {
				return;
			}
			fix.fix(monitor);
		}
	}

	public IProjectExamplesFix getFix(ProjectExample example, RequirementModel requirement) {
		return getFix(example, requirement, getFixProviders());
	}	

	
	protected IProjectExamplesFix getFix(ProjectExample example, RequirementModel requirement, Map<String, IProjectFixProvider> fixProviders) {
		Assert.isNotNull(requirement);
		return getFixProvider(requirement.getType(), fixProviders).create(example, requirement);
	}
	
	public UIHandler getUIHandler(IProjectExamplesFix fix) {
		Assert.isNotNull(fix);
		return getFixProvider(fix.getType()).createUIHandler();
	}
	
	public void loadFixes(ProjectExampleWorkingCopy workingCopy) {
		if (workingCopy == null || workingCopy.getRequirements() == null || workingCopy.getRequirements().isEmpty()) {
			return;
		}
		List<RequirementModel> reqs = workingCopy.getRequirements();
		if (reqs == null || reqs.isEmpty()) {
			return;
		}
		List<IProjectExamplesFix> fixes = new ArrayList<>(reqs.size());
		Map<String, IProjectFixProvider> providers = getFixProviders();
		for (RequirementModel req : reqs) {
			IProjectExamplesFix fix = getFix(workingCopy, req, providers);
			if (fix != null) {
				fixes.add(fix);
			}
		}
		workingCopy.setFixes(fixes);
	}

	private IProjectFixProvider getFixProvider(String type) {
		return getFixProvider(type, getFixProviders());
	}
	
	private IProjectFixProvider getFixProvider(String type, Map<String, IProjectFixProvider> fixProviders) {
		IProjectFixProvider provider = fixProviders.get(type);
		return provider == null?UNSUPPORTED_FIX_PROVIDER:provider;
	}
	
	
	private static Map<String, IProjectFixProvider> getFixProviders() {
		Map<String, IProjectFixProvider> providers = new HashMap<>(); 
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.jboss.tools.project.examples.projectFixProvider"); //$NON-NLS-1$
	    IExtension[] extensions = extensionPoint.getExtensions();
	    for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String type = element.getAttribute("type"); //$NON-NLS-1$
				if (type == null || type.isEmpty()) {
					continue;
				}
				try {
					IProjectFixProvider provider = (IProjectFixProvider)element.createExecutableExtension("class"); //$NON-NLS-1$
					providers.put(type, provider);
				} catch (CoreException e) {
					ProjectExamplesActivator.log("Unable to instanciate fixProvider for type "+type + " : " + e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	    return providers;
	}
	
	
}
