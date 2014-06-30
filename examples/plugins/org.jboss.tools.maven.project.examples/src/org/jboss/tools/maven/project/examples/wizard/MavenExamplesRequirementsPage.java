/*************************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.settings.MavenSettingsChangeListener;
import org.jboss.tools.maven.project.examples.utils.MavenArtifactHelper;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesRequirementsPage;

public class MavenExamplesRequirementsPage extends
		NewProjectExamplesRequirementsPage implements
		MavenSettingsChangeListener {

	private static final String PAGE_NAME = "org.jboss.tools.project.examples.mavenrequirements"; //$NON-NLS-1$

	protected MissingRepositoryWarningComponent warningComponent;

	private IStatus enterpriseRepoStatus;

	public MavenExamplesRequirementsPage() {
		this(PAGE_NAME, null);
	}

	public MavenExamplesRequirementsPage(String pageName, ProjectExampleWorkingCopy example) {
		super(pageName, example);
	}

	@Override
	public String getProjectExampleType() {
		return "maven"; //$NON-NLS-1$
	}

	@Override
	public void setProjectExample(final ProjectExampleWorkingCopy projectExample) {
		super.setProjectExample(projectExample);
		validateEnterpriseRepo();
	}

	@Override
	protected void setAdditionalControls(Composite composite) {
		warningComponent = new MissingRepositoryWarningComponent(composite);

		MavenCoreActivator.getDefault().registerMavenSettingsChangeListener(
				this);
	}

	@Override
	public void dispose() {
		if (warningComponent != null) {
			warningComponent.dispose();
		}

		super.dispose();
	}

	protected void validateEnterpriseRepo() {
		if (warningComponent != null) {
			warningComponent.setLinkText(""); //$NON-NLS-1$
			if (enterpriseRepoStatus == null) {
				enterpriseRepoStatus = MavenArtifactHelper.checkRequirementsAvailable(projectExample);
			}
			if (enterpriseRepoStatus.isOK()) {
				warningComponent.setRepositoryUrls(null);
			} else {
				warningComponent.setLinkText(enterpriseRepoStatus.getMessage());
			}
		}
	}

	@Override
	public void onSettingsChanged() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				enterpriseRepoStatus = null;
				validateEnterpriseRepo();
			}
		});
	}

}
