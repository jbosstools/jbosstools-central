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
