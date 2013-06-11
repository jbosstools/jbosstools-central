/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.jboss.tools.project.examples.model.ProjectExample;

public interface IProjectExamplesWizardPage extends IWizardPage, IWizardContextChangeListener {

	boolean finishPage();
	
	String getProjectExampleType();
	
	void setProjectExample(ProjectExample projectExample);

	Map<String, Object> getPropertiesMap();
	
	void setWizardContext(WizardContext context);

	String getPageType();
}
