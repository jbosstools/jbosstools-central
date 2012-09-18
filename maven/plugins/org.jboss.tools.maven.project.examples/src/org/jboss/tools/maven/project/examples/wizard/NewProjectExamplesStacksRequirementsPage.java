/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.wizard;

import static org.jboss.tools.maven.project.examples.stacks.StacksUtil.createArchetypeModel;
import static org.jboss.tools.maven.project.examples.stacks.StacksUtil.getArchetype;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.maven.project.examples.stacks.StacksManager;
import org.jboss.tools.maven.project.examples.stacks.StacksUtil;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesRequirementsPage;

public class NewProjectExamplesStacksRequirementsPage extends NewProjectExamplesRequirementsPage {

	private static final String PAGE_NAME = "org.jboss.tools.project.examples.stacksrequirements"; //$NON-NLS-1$

	org.jboss.jdf.stacks.model.Archetype stacksArchetype;   

	private ArchetypeVersion version;
	
	private Button useBlankArchetype;

	private Stacks stacks;
	
	public NewProjectExamplesStacksRequirementsPage() {
		this(null);
		try {
			stacks = new StacksManager().getStacks(new NullProgressMonitor());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public NewProjectExamplesStacksRequirementsPage(ProjectExample projectExample) {
		super(PAGE_NAME, projectExample);
	}

	@Override
	public String getProjectExampleType() {
		return "mavenArchetype";
	}
	
	@Override
	protected void setDescriptionArea(Composite composite) {
		super.setDescriptionArea(composite);
	}
	
	@Override
	public void setProjectExample(ProjectExample projectExample) {
		super.setProjectExample(projectExample);
		if (projectExample != null) {
			String stacksId = projectExample.getStacksId();
			stacksArchetype = getArchetype(stacksId, stacks);
			setArchetypeVersion();
		}
	}

	private void setArchetypeVersion() {
		if (stacksArchetype == null) {
			return;
		}
		
		org.jboss.jdf.stacks.model.Archetype a;
		
		if (useBlankArchetype != null && useBlankArchetype.getSelection()) {
			a = stacksArchetype.getBlank();
		} else {
			a = stacksArchetype;
		}
		version = StacksUtil.getDefaultArchetypeVersion(a, stacks);

		
		StringBuilder description = new StringBuilder(version.getArchetype().getDescription());
		description.append("\r\n").append("\r\n")
		           .append("Project based on the ")
		           .append(version.getArchetype().getGroupId())
		           .append(":")
		           .append(version.getArchetype().getArtifactId())
		           .append(":")
		           .append(version.getVersion())
		           .append(" Maven archetype");
		
		setDescriptionText(description.toString());
		
		ArchetypeModel mavenArchetype;
		try {
			mavenArchetype = createArchetypeModel(projectExample.getArchetypeModel(), version);
			
			wizardContext.setProperty(MavenProjectConstants.ARCHETYPE_MODEL, mavenArchetype);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void setSelectionArea(Composite composite) {
		useBlankArchetype = new Button(composite, SWT.CHECK);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalAlignment = SWT.BOTTOM;
		useBlankArchetype.setLayoutData(gd);
		useBlankArchetype.setText("Create an empty project");
		useBlankArchetype.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setArchetypeVersion();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			useBlankArchetype.setVisible(stacksArchetype != null 
					&& stacksArchetype.getBlank() != null );	
		}
		super.setVisible(visible);
	}
	
}
