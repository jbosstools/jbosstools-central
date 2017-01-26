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
package org.jboss.tools.maven.reddeer.wizards;

import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsActive;

import java.util.List;

import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.combo.DefaultCombo;
import org.jboss.reddeer.swt.impl.group.DefaultGroup;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.table.DefaultTable;
import org.jboss.reddeer.swt.impl.text.LabeledText;
import org.jboss.reddeer.uiforms.impl.expandablecomposite.DefaultExpandableComposite;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;

public class ConfigureMavenRepositoriesWizard {

	public void open() {
		new PushButton("Configure Maven Repositories...").click();
		new DefaultShell("Maven Repositories");
	}

	public String chooseRepositoryFromList(String repo, boolean activeByDefault, boolean snapshots) {
		new PushButton(" Add Repository...").click();
		new DefaultShell("Add Maven Repository");
		new DefaultCombo(0).setSelection(repo);
		new CheckBox("Active by default").toggle(activeByDefault);
		String a = new LabeledText("Name:").getText();
		String b = new LabeledText("URL:").getText();
		String nameWithUrl = a + "-" + b;
		if (snapshots) {
			new DefaultExpandableComposite("Advanced").setExpanded(true);
			new CheckBox("Enable snapshots").toggle(true);
		}
		new PushButton("OK").click();
		new DefaultShell("Maven Repositories");
		return nameWithUrl;
	}

	public List<String> getRepositoriesList() {
		new PushButton(" Add Repository...").click();
		new DefaultShell("Add Maven Repository");
		return new DefaultCombo(0).getItems();
	}

	public String addRepository(String repoID, String repoURL, boolean activeByDefault, boolean snapshots) {
		new PushButton(" Add Repository...").click();
		new DefaultShell("Add Maven Repository");
		new DefaultCombo(0).setText(repoID);
		new CheckBox("Active by default").toggle(activeByDefault);
		new LabeledText("ID:").setText(repoID);
		new LabeledText("Name:").setText(repoID);
		String a = new LabeledText("Name:").getText();
		new LabeledText("URL:").setText(repoURL);
		String b = new LabeledText("URL:").getText();
		String nameWithUrl = a + "-" + b;
		if (snapshots) {
			new DefaultExpandableComposite("Advanced").setExpanded(true);
			new CheckBox("Enable snapshots").toggle(true);
		}
		new PushButton("OK").click();
		new DefaultShell("Maven Repositories");
		return nameWithUrl;
	}

	public void removeRepo(String repo) {
		new DefaultTable(new DefaultGroup("Repositories")).select(repo);
		if (!new PushButton(" Remove ").isEnabled()) {
			//This can help when repo is not selected on windows
			new DefaultShell("Maven Repositories");
			new DefaultTable(new DefaultGroup("Repositories")).select(repo);
		}
		new PushButton(" Remove ").click();
		new DefaultShell("Question?");
		new PushButton("Yes").click();
		new DefaultShell("Maven Repositories");
	}

	public boolean removeAllRepos() {
		if (new DefaultTable(new DefaultGroup("Repositories")).rowCount() > 0) {
			new PushButton(" Remove All ").click();
			new DefaultShell("Question?");
			new PushButton("Yes").click();
			new DefaultShell("Maven Repositories");
			return true;
		}
		return false;

	}

	public void editRepo(String repo, boolean activeByDefault, String id, String name, String url) {
		new DefaultTable(new DefaultGroup("Repositories")).select(repo);
		new PushButton(" Edit Repository...").click();
		new DefaultShell("Edit Maven Repository");
		new CheckBox("Active by default").toggle(activeByDefault);
		if (id != null) {
			new LabeledText("ID:").setText(id);
		}
		if (name != null) {
			new LabeledText("Name:").setText(name);
		}
		if (url != null) {
			new LabeledText("URL:").setText(url);
		}
		new PushButton("OK").click();
		new DefaultShell("Maven Repositories");
	}

	public void confirm() {
		new DefaultShell("Maven Repositories");
		new PushButton("Finish").click();
		try {
			new WaitUntil(new ShellWithTextIsActive("Confirm File Update"), TimePeriod.NORMAL);
			new DefaultShell("Confirm File Update");
			new PushButton("Yes").click();
		} catch (WaitTimeoutExpiredException ex) {

		}
	}

	public void cancel() {
		new DefaultShell("Maven Repositories");
		new PushButton("Cancel").click();
	}

}
