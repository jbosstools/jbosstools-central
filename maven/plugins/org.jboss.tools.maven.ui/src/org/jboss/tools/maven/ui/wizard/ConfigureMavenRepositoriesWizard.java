/*************************************************************************************
 * Copyright (c) 2010-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.wizard;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * @author snjeza
 *
 */
public class ConfigureMavenRepositoriesWizard extends Wizard implements
		INewWizard {

	private ConfigureMavenRepositoriesWizardPage page;
	private ArtifactKey artifactKey;
	private String preSelectedProfileId;

	public ConfigureMavenRepositoriesWizard() {
		super();
		setWindowTitle("Maven Repositories");
	}
	
	public ConfigureMavenRepositoriesWizard(ArtifactKey artifactKey) {
		this();
		this.artifactKey = artifactKey;
	}

	public ConfigureMavenRepositoriesWizard(ArtifactKey artifactKey, String profileId) {
		this(artifactKey);
		this.preSelectedProfileId = profileId;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initializeDefaultPageImageDescriptor();
	}

	private void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						"icons/repo_wiz.gif"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}
	
	@Override
	public boolean performFinish() {
		return page.finishPage();
	}

	@Override
	public void addPages() {
		page = new ConfigureMavenRepositoriesWizardPage(artifactKey, preSelectedProfileId);
		addPage(page);
	}
	
	@Override
	public void addPage(IWizardPage page) {
		IWizardContainer container = getContainer(); 
		if (container instanceof IPageChangeProvider && page instanceof IPageChangedListener) { 
			((IPageChangeProvider) container).addPageChangedListener((IPageChangedListener)page); 
		} 
		super.addPage(page);
	}
	
	@Override
	public void dispose() {
		IWizardContainer container = getContainer(); 
		if (container instanceof IPageChangeProvider) { 
			for (IWizardPage page : getPages()) {
				if (page instanceof IPageChangedListener) {
					((IPageChangeProvider) container).removePageChangedListener((IPageChangedListener)page); 
				}
			}
		} 
		super.dispose();
	}
}
