/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.reddeer.requirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.reddeer.junit.requirement.Requirement;
import org.eclipse.reddeer.requirements.property.PropertyConfiguration;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.maven.reddeer.maven.ui.preferences.ConfiguratorPreferencePage;
import org.jboss.tools.maven.reddeer.requirement.ClearRepositoryRequirement.RemoveMavenRepository;
import org.jboss.tools.maven.reddeer.wizards.ConfigureMavenRepositoriesWizard;

public class ClearRepositoryRequirement extends PropertyConfiguration implements Requirement<RemoveMavenRepository> {

	protected RemoveMavenRepository repo;
	protected String url;

	public @interface ToRemoveMavenRepository {
		String url() default "https://maven.repository.redhat.com/ga/";

		String ID() default "ga";

		boolean hasToExists();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface RemoveMavenRepository {
		ToRemoveMavenRepository[] toRemoveRepositories() default {};
	}

	@Override
	public void fulfill() {
		ConfigureMavenRepositoriesWizard mr = openRepositoriesWizard();

		DefaultTable repoTable = new DefaultTable(new DefaultGroup("Repositories"));
		for (ToRemoveMavenRepository r : repo.toRemoveRepositories()) {
			String id = r.ID() + "-" + r.url();
			if (r.hasToExists() || repoTable.containsItem(id)) {
				// if not exists and should throws an exception
				mr.removeRepo(id);
			}
		}
		// by default if list is empty remove all repositories
		if (repo.toRemoveRepositories().length == 0)
			mr.removeAllRepos();
		closeRepositoriesWizard();
	}

	@Override
	public void setDeclaration(
			org.jboss.tools.maven.reddeer.requirement.ClearRepositoryRequirement.RemoveMavenRepository declaration) {
		this.repo = declaration;
	}

	@Override
	public void cleanUp() {
		openRepositoriesWizard();
		closeRepositoriesWizard();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected ConfigureMavenRepositoriesWizard openRepositoriesWizard() {
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		preferenceDialog.open();
		ConfiguratorPreferencePage jm = new ConfiguratorPreferencePage(preferenceDialog);
		preferenceDialog.select(jm);
		return jm.configureRepositories();
	}

	protected void closeRepositoriesWizard() {
		ConfigureMavenRepositoriesWizard mr = new ConfigureMavenRepositoriesWizard();
		// TODO add canFinish() check to ConfigureMavenRepositoriesWizard class
		try {
			// in case nothing was removed
			mr.confirm();
		} catch (Exception e) {
			mr.cancel();
		} finally {
			WorkbenchPreferenceDialog dialog = new WorkbenchPreferenceDialog();
			if (dialog.canFinish())
				dialog.ok();
			else
				dialog.cancel();
		}
	}

	@Override
	public RemoveMavenRepository getDeclaration() {
		return this.repo;
	}

}
