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

import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.condition.TableHasRows;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.tools.maven.reddeer.wizards.matchers.ArtifactIdAndGroupIdMatcher;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;

import java.util.List;

public class MavenProjectWizardSecondPage extends WizardPage{
	
	public MavenProjectWizardSecondPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}
	
	public void selectArchetype(String catalog, final String archetype){
		selectArchetype(catalog, archetype, null);
	}

	public void selectArchetype(String catalog, final String archetype, final String groupId){
		new DefaultCombo(0).setSelection(catalog);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		new WaitUntil(new TableHasRows(new DefaultTable()),TimePeriod.LONG);
		DefaultTable table = new DefaultTable();
				 	 	    
	    @SuppressWarnings("unchecked")
		List<TableItem> items = table.getItems(new ArtifactIdAndGroupIdMatcher(groupId, archetype));
	    items.get(0).select();
	}

}
