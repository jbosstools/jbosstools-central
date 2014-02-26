/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.maven.project.examples.wizard.xpl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.archetype.ArchetypeCatalogFactory.RemoteCatalogFactory;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.components.TextComboBoxCellEditor;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;


/**
 * Wizard page responsible for gathering information about the Maven2 artifact when an archetype is being used to create
 * a project (thus the class name pun).
 * 
 * This is a copy of org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage
 * as we need to access the Properties list. Changed :
 * <ul>
 * <li>Replaced slf4j loggers with MavenProjectExamplesActivator.log</li>
 * <li>Changed visibility of propertiesTable and propertiesViewer to protected</li>
 * <li>Changed visibility of validate() to protected</li>
 * </ul>
 */
public class MavenProjectWizardArchetypeParametersPage extends AbstractMavenWizardPage {

  public static final String DEFAULT_VERSION = "0.0.1-SNAPSHOT"; //$NON-NLS-1$

  public static final String DEFAULT_PACKAGE = "foo"; //$NON-NLS-1$

  protected Table propertiesTable;

  protected TableViewer propertiesViewer;
  
  protected TableViewerColumn valueColumn;

  final public static String KEY_PROPERTY = "key"; //$NON-NLS-1$

  final public static int KEY_INDEX = 0;

  final public static String VALUE_PROPERTY = "value"; //$NON-NLS-1$

  final public static int VALUE_INDEX = 1;

  /** group id text field */
  protected Combo groupIdCombo;

  /** artifact id text field */
  protected Combo artifactIdCombo;

  /** version text field */
  protected Combo versionCombo;

  /** package text field */
  protected Combo packageCombo;

  protected Button removeButton;

  private boolean isUsed = true;

  protected Set<String> requiredProperties;

  protected Set<String> optionalProperties;

  protected Archetype archetype;

  protected boolean archetypeChanged = false;

  /** shows if the package has been customized by the user */
  protected boolean packageCustomized = false;

  /** Creates a new page. */
  public MavenProjectWizardArchetypeParametersPage(ProjectImportConfiguration projectImportConfiguration) {
    super("Maven2ProjectWizardArchifactPage", projectImportConfiguration); //$NON-NLS-1$

    setTitle(Messages.wizardProjectPageMaven2Title); 
    setDescription(Messages.wizardProjectPageMaven2ArchetypeParametersDescription);
    setPageComplete(false);

    requiredProperties = new HashSet<String>();
    optionalProperties = new HashSet<String>();
  }

  /** Creates page controls. */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    composite.setLayout(new GridLayout(3, false));

    createArtifactGroup(composite);
    createPropertiesGroup(composite);

    validate();

    createAdvancedSettings(composite, new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));
    resolverConfigurationComponent.setModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });

    setControl(composite);

  }

  private void createArtifactGroup(Composite parent) {
//    Composite artifactGroup = new Composite(parent, SWT.NONE);
//    GridData gd_artifactGroup = new GridData( SWT.FILL, SWT.FILL, true, false );
//    artifactGroup.setLayoutData(gd_artifactGroup);
//    artifactGroup.setLayout(new GridLayout(2, false));

    Label groupIdlabel = new Label(parent, SWT.NONE);
    groupIdlabel.setText(Messages.artifactComponentGroupId); 

    groupIdCombo = new Combo(parent, SWT.BORDER);
    groupIdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    addFieldWithHistory("groupId", groupIdCombo); //$NON-NLS-1$
    groupIdCombo.setData("name", "groupId"); //$NON-NLS-1$ //$NON-NLS-2$
    groupIdCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateJavaPackage();
        validate();
      }
    });

    Label artifactIdLabel = new Label(parent, SWT.NONE);
    artifactIdLabel.setText(Messages.artifactComponentArtifactId); 

    artifactIdCombo = new Combo(parent, SWT.BORDER);
    artifactIdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    addFieldWithHistory("artifactId", artifactIdCombo); //$NON-NLS-1$
    artifactIdCombo.setData("name", "artifactId"); //$NON-NLS-1$ //$NON-NLS-2$
    artifactIdCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateJavaPackage();
        validate();
      }
    });

    Label versionLabel = new Label(parent, SWT.NONE);
    versionLabel.setText(Messages.artifactComponentVersion); 

    versionCombo = new Combo(parent, SWT.BORDER);
    GridData gd_versionCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_versionCombo.widthHint = 150;
    versionCombo.setLayoutData(gd_versionCombo);
    versionCombo.setText(DEFAULT_VERSION);
    addFieldWithHistory("version", versionCombo); //$NON-NLS-1$
    versionCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });

    Label packageLabel = new Label(parent, SWT.NONE);
    packageLabel.setText(Messages.artifactComponentPackage); 

    packageCombo = new Combo(parent, SWT.BORDER);
    packageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    packageCombo.setData("name", "package"); //$NON-NLS-1$ //$NON-NLS-2$
    addFieldWithHistory("package", packageCombo); //$NON-NLS-1$
    packageCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if(!packageCustomized && !packageCombo.getText().equals(getDefaultJavaPackage())) {
          packageCustomized = true;
        }
        validate();
      }
    });
  }

  private void createPropertiesGroup(Composite composite) {
    Label propertiesLabel = new Label(composite, SWT.NONE);
    propertiesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
    propertiesLabel.setText(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_lblProps);

    propertiesViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
    propertiesTable = propertiesViewer.getTable();
    propertiesTable.setLinesVisible(true);
    propertiesTable.setHeaderVisible(true);
    propertiesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));

    TableColumn propertiesTableNameColumn = new TableColumn(propertiesTable, SWT.NONE);
    propertiesTableNameColumn.setWidth(130);
    propertiesTableNameColumn.setText(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_columnName);

    TableColumn propertiesTableValueColumn = new TableColumn(propertiesTable, SWT.NONE);
    propertiesTableValueColumn.setWidth(230);
    propertiesTableValueColumn.setText(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_columnValue);

    propertiesViewer.setColumnProperties(new String[] {KEY_PROPERTY, VALUE_PROPERTY});

    propertiesViewer.setCellEditors(new CellEditor[] {new TextCellEditor(propertiesTable, SWT.NONE),
        new TextCellEditor(propertiesTable, SWT.NONE)});
    propertiesViewer.setCellModifier(new ICellModifier() {
      public boolean canModify(Object element, String property) {
        return true;
      }

      public void modify(Object element, String property, Object value) {
        if(element instanceof TableItem) {
          ((TableItem) element).setText(getTextIndex(property), String.valueOf(value));
          validate();
        }
      }

      public Object getValue(Object element, String property) {
        if(element instanceof TableItem) {
          return ((TableItem) element).getText(getTextIndex(property));
        }
        return null;
      }
    });

    Button addButton = new Button(composite, SWT.NONE);
    addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    addButton.setText(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_btnAdd);
    addButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        propertiesViewer.editElement(addTableItem("?", "?"), KEY_INDEX); //$NON-NLS-1$ //$NON-NLS-2$
      }
    });

    removeButton = new Button(composite, SWT.NONE);
    removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    removeButton.setText(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_btnRemove);
    removeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if(propertiesTable.getSelectionCount() > 0) {
          propertiesTable.remove(propertiesTable.getSelectionIndices());
          removeButton.setEnabled(propertiesTable.getItemCount() > 0);
          validate();
        }
      }
    });
  }

  /**
   * Validates the contents of this wizard page.
   * <p>
   * Feedback about the validation is given to the user by displaying error messages or informative messages on the
   * wizard page. Depending on the provided user input, the wizard page is marked as being complete or not.
   * <p>
   * If some error or missing input is detected in the user input, an error message or informative message,
   * respectively, is displayed to the user. If the user input is complete and correct, the wizard page is marked as
   * begin complete to allow the wizard to proceed. To that end, the following conditions must be met:
   * <ul>
   * <li>The user must have provided a valid group ID.</li>
   * <li>The user must have provided a valid artifact ID.</li>
   * <li>The user must have provided a version for the artifact.</li>
   * </ul>
   * </p>
   * 
   * @see org.eclipse.jface.dialogs.DialogPage#setMessage(java.lang.String)
   * @see org.eclipse.jface.wizard.WizardPage#setErrorMessage(java.lang.String)
   * @see org.eclipse.jface.wizard.WizardPage#setPageComplete(boolean)
   */
  protected void validate() {
    String error = validateInput();
    setErrorMessage(error);
    setPageComplete(error == null);
  }

  private String validateInput() {
    String error = validateGroupIdInput(groupIdCombo.getText().trim()); 
    if(error != null) {
      return error;
    }

    error = validateArtifactIdInput(artifactIdCombo.getText().trim());
    if(error != null) {
      return error;
    }

    String versionValue = versionCombo.getText().trim();
    if(versionValue.length() == 0) {
      return Messages.wizardProjectPageMaven2ValidatorVersion;
    }
    //TODO: check validity of version?

    String packageName = packageCombo.getText();
    if(packageName.trim().length() != 0) {
      if(!Pattern.matches("[A-Za-z_$][A-Za-z_$\\d]*(?:\\.[A-Za-z_$][A-Za-z_$\\d]*)*", packageName)) { //$NON-NLS-1$
        return org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_error_package;
      }
    }

    // validate project name
    IStatus nameStatus = getImportConfiguration().validateProjectName(getModel());
    if(!nameStatus.isOK()) {
      return NLS.bind(Messages.wizardProjectPageMaven2ValidatorProjectNameInvalid, nameStatus.getMessage());
    }

    if(requiredProperties.size() > 0) {
      Properties properties = getProperties();
      for(String key : requiredProperties) {
        String value = properties.getProperty(key);
        if(value == null || value.length() == 0) {
          return NLS.bind(Messages.wizardProjectPageMaven2ValidatorRequiredProperty, key); 
        }
      }
    }

    return null;
  }

  /** Ends the wizard flow chain. */
  public IWizardPage getNextPage() {
    return null;
  }

  public void setArchetype(Archetype archetype) {
    if(archetype == null) {
      if (propertiesTable != null)
    	  propertiesTable.removeAll();
      archetypeChanged = false;
    } else if(!areEqual(archetype, this.archetype)) {
      this.archetype = archetype;
      requiredProperties.clear();
      optionalProperties.clear();
      archetypeChanged = true;

      if (propertiesTable != null) {
    	  propertiesTable.removeAll();
    	  Properties properties = archetype.getProperties();
    	  if(properties != null) {
    		  for(Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
    			  Map.Entry<?, ?> e = it.next();
    			  String key = (String) e.getKey();
    			  addTableItem(key, (String) e.getValue());
    			  optionalProperties.add(key);
    		  }
    	  }
      }
    }
  }

  void loadArchetypeDescriptor() {
	    
	    try {
	      RequiredPropertiesLoader propertiesLoader = new RequiredPropertiesLoader(archetype);
	      getContainer().run(true, true, propertiesLoader);
	      
	      List<?> properties = propertiesLoader.getProperties();
	      if(properties != null) {
	        for(Object o : properties) {
	          if(o instanceof RequiredProperty) {
	            RequiredProperty rp = (RequiredProperty) o;
	            requiredProperties.add(rp.getKey());
	            addTableItem(rp.getKey(), rp.getDefaultValue());
	          }
	        }
	      }

	    } catch(InterruptedException ex) {
	      // ignore
	    } catch(InvocationTargetException ex) {
	      String msg = NLS.bind(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_error_download, getName(archetype));
	      MavenProjectExamplesActivator.log(ex, msg);
	      setErrorMessage(msg + "\n" + ex.toString()); //$NON-NLS-1$
	    }
	  }

	  static String getName(Archetype archetype) {
	    final String groupId = archetype.getGroupId();
	    final String artifactId = archetype.getArtifactId();
	    final String version = archetype.getVersion();
	    return groupId + ":" + artifactId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
	  }
	  
	  private static class RequiredPropertiesLoader implements IRunnableWithProgress {
	    
	    private Archetype archetype;
	    
	    private List<?> properties;
	    
	    RequiredPropertiesLoader(Archetype archetype) {
	      this.archetype = archetype;
	    }
	    
	    List<?> getProperties() {
	      return properties;
	    }
	    
	    public void run(IProgressMonitor monitor) {
	      String archetypeName = getName(archetype);
	      monitor.beginTask(NLS.bind(org.eclipse.m2e.core.ui.internal.Messages.MavenProjectWizardArchetypeParametersPage_task, archetypeName ), IProgressMonitor.UNKNOWN);
	      
	      try {
	        
	        ArtifactRepository remoteArchetypeRepository = getArchetypeRepository(archetype);
	        
	        properties = getRequiredProperties(archetype, remoteArchetypeRepository, monitor);
	        
	      } catch(UnknownArchetype e) {
	    	  String msg = NLS.bind("Error downloading archetype {0}",archetypeName);
	    	  MavenProjectExamplesActivator.log(e, msg); //$NON-NLS-1$
	      } catch(CoreException ex) {
	    	  MavenProjectExamplesActivator.log(ex, ex.getMessage()); //$NON-NLS-1$
	      } finally {
	        monitor.done();
	      }
	    }    
	  }
  /**
   * @param key
   * @param value
   */
  TableItem addTableItem(String key, String value) {
    TableItem item = new TableItem(propertiesTable, SWT.NONE);
    item.setData(item);
    item.setText(KEY_INDEX, key);
    item.setText(VALUE_INDEX, value == null ? "" : value); //$NON-NLS-1$
    return item;
  }

  /**
   * Updates the properties when a project name is set on the first page of the wizard.
   */
  public void setProjectName(String projectName) {
    if(artifactIdCombo.getText().equals(groupIdCombo.getText())) {
      groupIdCombo.setText(projectName);
    }
    artifactIdCombo.setText(projectName);
    packageCombo.setText("org." + projectName.replace('-', '.')); //$NON-NLS-1$
    validate();
  }

  /**
   * Updates the properties when a project name is set on the first page of the wizard.
   */
  public void setParentProject(String groupId, String artifactId, String version) {
    groupIdCombo.setText(groupId);
    versionCombo.setText(version);
    validate();
  }

  /** Enables or disables the artifact id text field. */
  public void setArtifactIdEnabled(boolean b) {
    artifactIdCombo.setEnabled(b);
  }

  /** Returns the package name. */
  public String getJavaPackage() {
    if(packageCombo.getText().length() > 0) {
      return packageCombo.getText();
    }
    return getDefaultJavaPackage();
  }

  /** Updates the package name if the related fields changed. */
  protected void updateJavaPackage() {
    if(packageCustomized) {
      return;
    }

    String defaultPackageName = getDefaultJavaPackage();
    packageCombo.setText(defaultPackageName);
  }

  /** Returns the default package name. */
  protected String getDefaultJavaPackage() {
    return MavenProjectWizardArchetypeParametersPage.getDefaultJavaPackage(groupIdCombo.getText().trim(),
        artifactIdCombo.getText().trim());
  }

  /** Creates the Model object. */
  public Model getModel() {
    Model model = new Model();

    model.setModelVersion("4.0.0"); //$NON-NLS-1$
    model.setGroupId(groupIdCombo.getText());
    model.setArtifactId(artifactIdCombo.getText());
    model.setVersion(versionCombo.getText());

    return model;
  }

  public void setUsed(boolean isUsed) {
    this.isUsed = isUsed;
  }

  public boolean isPageComplete() {
    return !isUsed || super.isPageComplete();
  }

  /** Loads the group value when the page is displayed. */
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if(visible) {
      if(groupIdCombo.getText().length() == 0 && groupIdCombo.getItemCount() > 0) {
        groupIdCombo.setText(groupIdCombo.getItem(0));
        packageCombo.setText(getDefaultJavaPackage());
        packageCustomized = false;
      }

      if(archetypeChanged && archetype != null) {
        archetypeChanged = false;
        loadArchetypeDescriptor();
        validate();
      }

      updatePropertyEditors();
    }
  }

  public Properties getProperties() {
    if(propertiesViewer.isCellEditorActive()) {
      propertiesTable.setFocus();
    }
    Properties properties = new Properties();
    for(int i = 0; i < propertiesTable.getItemCount(); i++ ) {
      TableItem item = propertiesTable.getItem(i);
      properties.put(item.getText(KEY_INDEX), item.getText(VALUE_INDEX));
    }
    return properties;
  }

  public int getTextIndex(String property) {
    return KEY_PROPERTY.equals(property) ? KEY_INDEX : VALUE_INDEX;
  }

  public void updatePropertyEditors() {
    CellEditor[] ce = propertiesViewer.getCellEditors();

    int n = requiredProperties.size() + optionalProperties.size();
    if(n == 0) {
      if(ce[KEY_INDEX] instanceof TextComboBoxCellEditor) {
        // if there was a combo editor previously defined, and the current
        // archetype has no properties, replace it with a plain text editor
        ce[KEY_INDEX].dispose();
        ce[KEY_INDEX] = new TextCellEditor(propertiesTable, SWT.FLAT);
      }
    } else {
      TextComboBoxCellEditor comboEditor = null;
      // if there was a plain text editor previously defined, and the current
      // archetype has properties, replace it with a combo editor
      if(ce[KEY_INDEX] instanceof TextComboBoxCellEditor) {
        comboEditor = (TextComboBoxCellEditor) ce[KEY_INDEX];
      } else {
        ce[KEY_INDEX].dispose();
        comboEditor = new TextComboBoxCellEditor(propertiesTable, SWT.FLAT);
        ce[KEY_INDEX] = comboEditor;
      }

      // populate the property name selection
      List<String> propertyKeys = new ArrayList<String>(n);
      propertyKeys.addAll(requiredProperties);
      propertyKeys.addAll(optionalProperties);
      comboEditor.setItems(propertyKeys.toArray(new String[n]));
    }
  }

  public static String getDefaultJavaPackage(String groupId, String artifactId) {
    return org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage.getDefaultJavaPackage(groupId, artifactId);
  }
  
  /**
   * Gets the remote {@link ArtifactRepository} of the given {@link Archetype}, or null if none is found.
   * The repository url is extracted from {@link Archetype#getRepository()}, or, if it has none, the remote catalog the archetype is found in. 
   * The {@link ArtifactRepository} id is set to <strong>archetypeId+"-repo"</strong>, to enable authentication on that repository.
   *
   * @see <a href="http://maven.apache.org/archetype/maven-archetype-plugin/faq.html">http://maven.apache.org/archetype/maven-archetype-plugin/faq.html</a>
   * @param archetype
   * @return the remote {@link ArtifactRepository} of the given {@link Archetype}, or null if none is found.
   * @throws CoreException
   */
  public static ArtifactRepository getArchetypeRepository(Archetype archetype) throws CoreException {
    String repoUrl = archetype.getRepository();
    if (repoUrl == null || repoUrl.trim().isEmpty()) {
    	return null;
    }
    return MavenPlugin.getMaven().createArtifactRepository(archetype.getArtifactId()+"-repo", repoUrl); //$NON-NLS-1$
  }


  /**
   * Gets the required properties of an {@link Archetype}.
   * 
   * @param archetype the archetype possibly declaring required properties
   * @param remoteArchetypeRepository the remote archetype repository, can be null.
   * @param monitor the progress monitor, can be null.
   * @return the required properties of the archetypes, null if none is found.
   * @throws UnknownArchetype thrown if no archetype is can be resolved
   * @throws CoreException
   */
  public static List<?> getRequiredProperties(Archetype archetype, ArtifactRepository remoteArchetypeRepository, IProgressMonitor monitor) throws UnknownArchetype, CoreException {
    Assert.isNotNull(archetype, "Archetype can not be null");
    
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    
    final String groupId = archetype.getGroupId();
    final String artifactId = archetype.getArtifactId();
    final String version = archetype.getVersion();
    
    IMaven maven = MavenPlugin.getMaven();

    ArtifactRepository localRepository = maven.getLocalRepository();

    List<ArtifactRepository> repositories;
    
    if (remoteArchetypeRepository == null) {
      repositories = maven.getArtifactRepositories();
    } else {
      repositories = Collections.singletonList(remoteArchetypeRepository);
    }

    MavenSession session = maven.createSession(maven.createExecutionRequest(monitor), null);
    
    MavenSession oldSession = MavenPluginActivator.getDefault().setSession(session);

    ArchetypeArtifactManager aaMgr = MavenPluginActivator.getDefault().getArchetypeArtifactManager();

    List<?> properties = null;

    try {
      if(aaMgr.isFileSetArchetype(groupId,
                                  artifactId,
                                  version,
                                  null,
                                  localRepository,
                                  repositories)) {
        ArchetypeDescriptor descriptor = aaMgr.getFileSetArchetypeDescriptor(groupId,
                                                                             artifactId,
                                                                             version,
                                                                             null,
                                                                             localRepository,
                                                                             repositories);
        
        properties = descriptor.getRequiredProperties();
      }
    } finally {
      MavenPluginActivator.getDefault().setSession(oldSession);
    }

    return properties;
  }

  
  /**
   * Checks {@link Archetype} equality by testing <code>groupId</code>, <code>artifactId</code> and <code>version</code>
   * 
   * code copied from ArchetypeUtil in m2e 1.3
   */
  public static boolean areEqual(Archetype one, Archetype another) {
    if(one == another) {
      return true;
    }

    if(another == null) {
      return false;
    }

    return StringUtils.equals(one.getGroupId(), another.getGroupId())
        && StringUtils.equals(one.getArtifactId(), another.getArtifactId())
        && StringUtils.equals(one.getVersion(), another.getVersion());
  }
}
