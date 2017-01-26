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
import java.util.ArrayList;
import java.util.List;

import org.jboss.reddeer.junit.requirement.PropertyConfiguration;
import org.jboss.reddeer.junit.requirement.Requirement;
import org.jboss.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.maven.reddeer.maven.ui.preferences.ConfiguratorPreferencePage;
import org.jboss.tools.maven.reddeer.requirement.NewRepositoryRequirement.DefineMavenRepository;
import org.jboss.tools.maven.reddeer.wizards.ConfigureMavenRepositoriesWizard;

public class NewRepositoryRequirement implements Requirement<DefineMavenRepository>, PropertyConfiguration{
	
	private DefineMavenRepository repo;
	private List<String> repositoriesToDelete;
	private String url;
	
	public @interface MavenRepository {
		  String url();
		  String ID();
		  boolean snapshots();
	}
	
	public @interface PredefinedMavenRepository {
		  String ID();
		  boolean snapshots();
	}
	
	public @interface PropertyDefinedMavenRepository {
		String ID();
		boolean snapshots();
	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface DefineMavenRepository{
		MavenRepository[] newRepositories() default {};
		PredefinedMavenRepository[] predefinedRepositories() default {};
		PropertyDefinedMavenRepository[] propDefMavenRepo() default {};
	}

	@Override
	public boolean canFulfill() {
		return true;
	}

	@Override
	public void fulfill() {
		repositoriesToDelete = new ArrayList<String>();
		ConfigureMavenRepositoriesWizard mr = openRepositoriesWizard();
		for(MavenRepository r: repo.newRepositories()){
			repositoriesToDelete.add(mr.addRepository(r.ID(), r.url(), true,r.snapshots()));
		}
		for(PredefinedMavenRepository pr: repo.predefinedRepositories()){
			repositoriesToDelete.add(mr.chooseRepositoryFromList(pr.ID(), true, pr.snapshots()));
		}
		if (repo.propDefMavenRepo().length>0){
			repositoriesToDelete.add(mr.addRepository(repo.propDefMavenRepo()[0].ID(), url, true, repo.propDefMavenRepo()[0].snapshots()));
		}
		closeRepositoriesWizard();
	}

	@Override
	public void setDeclaration(org.jboss.tools.maven.reddeer.requirement.NewRepositoryRequirement.DefineMavenRepository declaration) {
		this.repo = declaration;
	}

	@Override
	public void cleanUp() {
		ConfigureMavenRepositoriesWizard mr = openRepositoriesWizard();
		for(String r: repositoriesToDelete){
			mr.removeRepo(r);
		}
		closeRepositoriesWizard();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	private ConfigureMavenRepositoriesWizard openRepositoriesWizard(){
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		preferenceDialog.open();
		ConfiguratorPreferencePage jm = new ConfiguratorPreferencePage();
		preferenceDialog.select(jm);
		return jm.configureRepositories();
	}
	
	private void closeRepositoriesWizard(){
		ConfigureMavenRepositoriesWizard mr = new ConfigureMavenRepositoriesWizard();
		mr.confirm();
		new WorkbenchPreferenceDialog().ok();
	}
	
	

}
