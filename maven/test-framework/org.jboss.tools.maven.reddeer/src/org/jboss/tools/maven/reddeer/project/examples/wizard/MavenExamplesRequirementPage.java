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
package org.jboss.tools.maven.reddeer.project.examples.wizard;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.link.DefaultLink;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.core.matcher.WithLabelMatcher;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.common.wait.WaitWhile;

public class MavenExamplesRequirementPage extends WizardPage {


	public MavenExamplesRequirementPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	public String getDescription() {
		return new DefaultText(new WithLabelMatcher("Description:")).getText();
	}

	public List<ExampleRequirement> getRequirements() {
		List<ExampleRequirement> list = new ArrayList<ExampleRequirement>();
		DefaultTable table = new DefaultTable();
		List<TableItem> items = table.getItems();
		for (TableItem item : items) {
			list.add(new ExampleRequirement(table, table.indexOf(item)));
		}
		return list;
	}

	public void setRuntime(int index) {
		new DefaultCombo(new WithLabelMatcher("Target Runtime"))
				.setSelection(index);
	}

	public void setRuntime(String text) {
		new DefaultCombo(new WithLabelMatcher("Target Runtime"))
				.setSelection(text);
	}

	public void testInstallButton() {
		new DefaultTable().getItem(0).select();
		new PushButton("Install...").click();
		Shell s = new DefaultShell("Preferences");
		if (!new DefaultTreeItem("JBoss Tools", "JBoss Runtime Detection")
				.isSelected()) {
			s.close();
			fail("Preferences should have opened on \"JBoss Runtime Detection\"");
		}
		s.close();
	}
	
	public String getWarningMessage() {
		DefaultLink link = new DefaultLink();
		new WaitWhile(new MavenIsUpdatingLinkCheck(link));
		return link.getText();
	}

	class MavenIsUpdatingLinkCheck extends AbstractWaitCondition {

		private DefaultLink link;

		public MavenIsUpdatingLinkCheck(DefaultLink link) {
			this.link = link;
		}

		public boolean test() {
			if (link.getText().equals(""))
				return false;
			return link.getText().equalsIgnoreCase(
					"Checking enterprise maven repository availability...");
		}

		public String description() {
			return "Maven repository check wait";
		}

	}
	
}
