/*******************************************************************************
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.maven.conversion.ui.dialog.xpl;

import static org.eclipse.m2e.core.ui.internal.util.Util.nvl;

import org.apache.maven.model.Dependency;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.util.M2EUIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;


public class EditDependencyDialog extends SelectionStatusDialog  {
  private static final String[] TYPES = new String[] {"jar", "war", "rar", "ear", "par", "ejb", "ejb-client", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
      "test-jar", "java-source", "javadoc", "maven-plugin", "pom"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

  private String[] scopes = new String[] {"compile", "provided", "runtime", "test", "system"};

  protected Text groupIdText;

  protected Text artifactIdText;

  protected Text versionText;

  protected Text classifierText;

  protected Combo typeCombo;

  protected Combo scopeCombo;

  protected Text systemPathText;

  protected Button optionalButton;

  private Dependency dependency;

/**
   * 
   * @param parent
   * @param dependencyManagement
   * @param project can be null, only used for indexer search as scope  
   * @param mavenProject
   */
  public EditDependencyDialog(Shell parent) {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setTitle("Edit dependency");
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite superComposite = (Composite) super.createDialogArea(parent);

    Composite composite = new Composite(superComposite, SWT.NONE);
    composite.setLayout(new GridLayout(3, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Label groupIdLabel = new Label(composite, SWT.NONE);
    groupIdLabel.setText(Messages.EditDependencyDialog_groupId_label);

    groupIdText = new Text(composite, SWT.BORDER);
    GridData gd_groupIdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    gd_groupIdText.horizontalIndent = 4;
    groupIdText.setLayoutData(gd_groupIdText);
    //ProposalUtil.addGroupIdProposal(project, groupIdText, Packaging.ALL);
    M2EUIUtils.addRequiredDecoration(groupIdText);

    Label artifactIdLabel = new Label(composite, SWT.NONE);
    artifactIdLabel.setText("Artifact Id");

    artifactIdText = new Text(composite, SWT.BORDER);
    GridData gd_artifactIdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    gd_artifactIdText.horizontalIndent = 4;
    artifactIdText.setLayoutData(gd_artifactIdText);
    //ProposalUtil.addArtifactIdProposal(project, groupIdText, artifactIdText, Packaging.ALL);
    M2EUIUtils.addRequiredDecoration(artifactIdText);

    Label versionLabel = new Label(composite, SWT.NONE);
    versionLabel.setText(Messages.EditDependencyDialog_version_label);

    versionText = new Text(composite, SWT.BORDER);
    GridData versionTextData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
    versionTextData.horizontalIndent = 4;
    versionTextData.widthHint = 200;
    versionText.setLayoutData(versionTextData);
    //ProposalUtil.addVersionProposal(project, mavenproject, groupIdText, artifactIdText, versionText, Packaging.ALL);

    Label classifierLabel = new Label(composite, SWT.NONE);
    classifierLabel.setText(Messages.EditDependencyDialog_classifier_label);

    classifierText = new Text(composite, SWT.BORDER);
    GridData gd_classifierText = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
    gd_classifierText.horizontalIndent = 4;
    gd_classifierText.widthHint = 200;
    classifierText.setLayoutData(gd_classifierText);
    //ProposalUtil
    //    .addClassifierProposal(project, groupIdText, artifactIdText, versionText, classifierText, Packaging.ALL);

    Label typeLabel = new Label(composite, SWT.NONE);
    typeLabel.setText(Messages.EditDependencyDialog_type_label);

    typeCombo = new Combo(composite, SWT.NONE);
    typeCombo.setItems(TYPES);
    GridData gd_typeText = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
    gd_typeText.horizontalIndent = 4;
    gd_typeText.widthHint = 120;
    typeCombo.setLayoutData(gd_typeText);

    Label scopeLabel = new Label(composite, SWT.NONE);
    scopeLabel.setText(Messages.EditDependencyDialog_scope_label);

    scopeCombo = new Combo(composite, SWT.NONE);
    scopeCombo.setItems(scopes);
    GridData gd_scopeText = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
    gd_scopeText.horizontalIndent = 4;
    gd_scopeText.widthHint = 120;
    scopeCombo.setLayoutData(gd_scopeText);

    Label systemPathLabel = new Label(composite, SWT.NONE);
    systemPathLabel.setText(Messages.EditDependencyDialog_systemPath_label);

    systemPathText = new Text(composite, SWT.BORDER);
    GridData gd_systemPathText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    gd_systemPathText.horizontalIndent = 4;
    gd_systemPathText.widthHint = 200;
    systemPathText.setLayoutData(gd_systemPathText);

//    selectSystemPathButton = new Button(composite, SWT.NONE);
//    selectSystemPathButton.setText("Select...");

    new Label(composite, SWT.NONE);

    optionalButton = new Button(composite, SWT.CHECK);
    optionalButton.setText(Messages.EditDependencyDialog_optional_checkbox);
    GridData gd_optionalButton = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
    gd_optionalButton.horizontalIndent = 4;
    optionalButton.setLayoutData(gd_optionalButton);

    composite.setTabList(new Control[] {groupIdText, artifactIdText, versionText, classifierText, typeCombo,
        scopeCombo, systemPathText, /*selectSystemPathButton,*/optionalButton});

    setDependency(dependency);

    return superComposite;
  }
  
  @Override
  protected void computeResult() {
	if (dependency == null) {
		dependency = new Dependency();
	}
    final String groupId = valueOrNull(groupIdText.getText());
    final String artifactId = valueOrNull(artifactIdText.getText());
    final String version =  valueOrNull(versionText.getText()); 
    String type = valueOrNull(typeCombo.getText());
    type = (type == null)?"jar":type;
    final String scope = valueOrNull(scopeCombo.getText());
    final String classifier = valueOrNull(classifierText.getText());
    final String system = valueOrNull(systemPathText.getText());
    
    dependency.setArtifactId(artifactId);
    dependency.setGroupId(groupId);
    dependency.setType(type);
    dependency.setVersion(version);
    dependency.setScope(scope);
    dependency.setClassifier(classifier);
    dependency.setSystemPath(system);
    boolean optional = optionalButton.getSelection();
    if (optional != dependency.isOptional()) {
    	dependency.setOptional(optional);
    }
  }
  
  private String valueOrNull(String value) {
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) {
        value = null;
      }
    }
    return value;
  }
  

  public void setDependency(Dependency dependency) {

	this.dependency = dependency == null? null : dependency.clone();

    if(dependency != null 
       && groupIdText != null 
       && !groupIdText.isDisposed()) {
      groupIdText.setText(nvl(dependency.getGroupId()));
      artifactIdText.setText(nvl(dependency.getArtifactId()));
      versionText.setText(nvl(dependency.getVersion()));
      classifierText.setText(nvl(dependency.getClassifier()));
      typeCombo.setText("".equals(nvl(dependency.getType())) ? "jar" : dependency.getType()); //$NON-NLS-1$ //$NON-NLS-2$
      scopeCombo.setText("".equals(nvl(dependency.getScope())) ? "compile" : dependency.getScope()); //$NON-NLS-1$ //$NON-NLS-2$
      systemPathText.setText(nvl(dependency.getSystemPath()));

      boolean optional = Boolean.parseBoolean(dependency.getOptional());
      if(optionalButton.getSelection() != optional) {
        optionalButton.setSelection(optional);
      }
    }
  }

  /**
  * @return the dependency
  */
  public Dependency getDependency() {
	return dependency;
  }

}
