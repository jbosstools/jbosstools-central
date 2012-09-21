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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.facets.FacetUtil;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.jdf.stacks.model.Runtime;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.Messages;
import org.jboss.tools.maven.project.examples.stacks.StacksManager;
import org.jboss.tools.maven.project.examples.stacks.StacksUtil;
import org.jboss.tools.maven.project.examples.utils.MavenArtifactHelper;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesRequirementsPage;
import org.jboss.tools.runtime.core.model.DownloadRuntime;

public class NewProjectExamplesStacksRequirementsPage extends NewProjectExamplesRequirementsPage {

	private static final String PAGE_NAME = "org.jboss.tools.project.examples.stacksrequirements"; //$NON-NLS-1$

	private static final String TARGET_RUNTIME = "targetRuntime"; //$NON-NLS-1$

	private MissingRepositoryWarningComponent warningComponent;
	
	private IStatus enterpriseRepoStatus;

	private org.jboss.jdf.stacks.model.Archetype stacksArchetype;   

	private ArchetypeVersion version;
	
	private Button useBlankArchetype;

	private Stacks stacks;

	private IRuntimeLifecycleListener listener;

	private Combo serverTargetCombo;

	private Map<String, IRuntime> serverRuntimes;

	public NewProjectExamplesStacksRequirementsPage() {
		this(null);
		stacks = new StacksManager().getStacks(new NullProgressMonitor());
	}
	
	public NewProjectExamplesStacksRequirementsPage(ProjectExample projectExample) {
		super(PAGE_NAME, projectExample);
	    fieldsWithHistory = new HashMap<String, List<Combo>>();
	    initDialogSettings();
	}

	@Override
	public String getProjectExampleType() {
		return "mavenArchetype";
	}
	
	@Override
	public void setProjectExample(ProjectExample projectExample) {
		super.setProjectExample(projectExample);
		if (projectExample != null) {
			String stacksId = projectExample.getStacksId();
			stacksArchetype = getArchetype(stacksId, stacks);
			setArchetypeVersion();
			boolean hasBlank = stacksArchetype!=null && null != stacksArchetype.getBlank();
			if (useBlankArchetype != null) {
				useBlankArchetype.setVisible(hasBlank);
				((GridData) useBlankArchetype.getLayoutData()).exclude = !hasBlank;
				useBlankArchetype.getParent().layout(true, true);
			}
			
			wizardContext.setProperty(MavenProjectConstants.ENTERPRISE_TARGET, isEnterpriseTargetRuntime());

			validateEnterpriseRepo();
		}
	}

	private void setArchetypeVersion() {

		ArchetypeModel mavenArchetype = null;
		StringBuilder description = new StringBuilder();
		
		if (stacksArchetype == null) {
			description.append(projectExample.getDescription());
			mavenArchetype = projectExample.getArchetypeModel();
		} else {
			org.jboss.jdf.stacks.model.Archetype a;
	
			if (useBlankArchetype != null && useBlankArchetype.getSelection()) {
				a = stacksArchetype.getBlank();
				
			} else {
				a = stacksArchetype;
			}
	
			version = null;
			//get selected runtime from combo
			if (serverTargetCombo != null && !serverTargetCombo.isDisposed()) {
				String wtpServerId = serverTargetCombo.getText();
				IRuntime wtpRuntime = serverRuntimes.get(wtpServerId);
				if (wtpRuntime != null && wtpRuntime.getRuntimeType() != null) {
					String wtpRuntimeId = wtpRuntime.getRuntimeType().getId();
					//System.err.println(wtpRuntimeId);
					Runtime stacksRuntime = StacksUtil.getRuntimeFromWtpId(stacks, wtpRuntimeId );
					if (stacksRuntime != null) {
						List<ArchetypeVersion> compatibleVersions = StacksUtil.getCompatibleArchetypeVersions(a, stacksRuntime);
						if (compatibleVersions != null && !compatibleVersions.isEmpty()) {
							version = compatibleVersions.get(0);
						}
					} else {
						//No stacks runtime matching that server id
					}
				}
			}
			//contains wtp runtime id
			
			if (version == null) {
				version = StacksUtil.getDefaultArchetypeVersion(a, stacks);
			}
			
			description.append(version.getArchetype().getDescription());
			
			try {
				mavenArchetype = createArchetypeModel(projectExample.getArchetypeModel(), version);
				
				wizardContext.setProperty(MavenProjectConstants.ARCHETYPE_MODEL, mavenArchetype);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		
		if (mavenArchetype != null) {
			description.append("\r\n").append("\r\n")
			.append("Project based on the ")
			.append(mavenArchetype.getArchetypeGroupId())
			.append(":")
			.append(mavenArchetype.getArchetypeArtifactId())
			.append(":")
			.append(mavenArchetype.getArchetypeVersion())
			.append(" Maven archetype");
		}
		
		setDescriptionText(description.toString());
		
		
	}

	@Override
	protected void setSelectionArea(Composite composite) {

		useBlankArchetype = new Button(composite, SWT.CHECK);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false, 2, 1);
		gd.verticalAlignment = SWT.TOP;
		
		useBlankArchetype.setLayoutData(gd);
		
		useBlankArchetype.setText("Create a blank project");
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
		
		listener = new IRuntimeLifecycleListener() {
			
			@Override
			public void runtimeRemoved(IRuntime runtime) {
				runInUIThread();
			}
			
			@Override
			public void runtimeChanged(IRuntime runtime) {
				runInUIThread();
			}
			
			@Override
			public void runtimeAdded(IRuntime runtime) {
				runInUIThread();
			}
			
			private void runInUIThread() {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						configureRuntimeCombo();
					}
				});
			}
			
		};
		ServerCore.addRuntimeLifecycleListener(listener);

		createServerTargetComposite(composite);

	}
	
	@Override
	protected void setAdditionalControls(Composite composite) {
		warningComponent = new MissingRepositoryWarningComponent(composite, false);
		//Yuck!! Necessary evil in order to have the warning component fitting in 
		//the dialog page area, once it's set as visible.
		//Anybody who can find a proper solution will have my eternal gratitude
		GridDataFactory.fillDefaults().hint(625, 45).applyTo(warningComponent);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
		  if (useBlankArchetype != null) {
				useBlankArchetype.getParent().layout(true);
		  }
		
          if(!isHistoryLoaded) {
            loadInputHistory();
            isHistoryLoaded = true;
          } else {
            saveInputHistory();
          }
          
		  wizardContext.setProperty(MavenProjectConstants.ENTERPRISE_TARGET, isEnterpriseTargetRuntime());
		}
		super.setVisible(visible);
	}
	
	protected void createServerTargetComposite(Composite composite) {
		
		Composite parent = new Composite(composite, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		Label serverTargetLabel = new Label(parent, SWT.NONE);
		serverTargetLabel.setText(Messages.ArchetypeExamplesWizardFirstPage_Target_Runtime_Label);

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		serverTargetCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		serverTargetCombo.setLayoutData(gridData);
		serverTargetCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					setArchetypeVersion();
					wizardContext.setProperty(MavenProjectConstants.ENTERPRISE_TARGET, isEnterpriseTargetRuntime());
				}
				validateEnterpriseRepo();
			}
		});
		
		configureRuntimeCombo();

	}
	
	public boolean isEnterpriseTargetRuntime() {
		if (serverTargetCombo == null)
			return false;
		//server runtime names are unique so name == server id
		String serverId = serverTargetCombo.getText();
		IRuntime runtime = serverRuntimes.get(serverId);
		return (runtime != null && RuntimeUtils.isEAP(runtime));
	}


	protected void configureRuntimeCombo() {
		if (serverTargetCombo == null || serverTargetCombo.isDisposed()) {
			return;
		}
		//TODO read facet version from project example metadata
		IProjectFacetVersion facetVersion;
		try {
			facetVersion = ProjectFacetsManager.getProjectFacet(
					IJ2EEFacetConstants.DYNAMIC_WEB).getLatestVersion();
		} catch (CoreException e) {
			MavenProjectExamplesActivator.log(e);
			return;
		}
			
		int i =0, selectedRuntimeIdx = 0;
		String lastUsedRuntime = dialogSettings.get(TARGET_RUNTIME);

		serverRuntimes = getServerRuntimes(facetVersion);
		serverTargetCombo.removeAll();
		serverTargetCombo.add(Messages.ArchetypeExamplesWizardFirstPage_No_TargetRuntime);
		for (Map.Entry<String, IRuntime> entry : serverRuntimes.entrySet()) {
			serverTargetCombo.add(entry.getKey());
			++i;
			IRuntime runtime = entry.getValue();
			if (lastUsedRuntime != null && lastUsedRuntime.equals(runtime.getId())) {
				selectedRuntimeIdx = i;
			}
		}
				
		if (selectedRuntimeIdx > 0) {
			serverTargetCombo.select(selectedRuntimeIdx);
		}
	}
		
	protected List<DownloadRuntime> getDownloadRuntimes(ProjectFix fix) {
		if (ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
			List<Runtime> stacksRuntimes = StacksUtil.getCompatibleServerRuntimes(stacksArchetype, stacks);
			if (stacksRuntimes != null && !stacksRuntimes.isEmpty()) {
				List<DownloadRuntime> downloadableRuntimes = new ArrayList<DownloadRuntime>(stacksRuntimes.size());
				for (Runtime r : stacksRuntimes) {
					DownloadRuntime dr = new DownloadRuntime(r.getId(),
							r.getName(), 
							r.getVersion(), 
							r.getDownloadUrl());
					dr.setDisclaimer(!StacksUtil.isEnterprise(r));
					dr.setHumanUrl(r.getUrl());
					downloadableRuntimes.add(dr);
				}
				return downloadableRuntimes;
			}
		}
		return super.getDownloadRuntimes(fix);
	}

	
	protected Map<String, IRuntime> getServerRuntimes(
			IProjectFacetVersion facetVersion) {
		Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> runtimesSet;
		if (facetVersion == null) {
			runtimesSet =RuntimeManager.getRuntimes();
		} else {
			runtimesSet = RuntimeManager.getRuntimes(Collections.singleton(facetVersion));
		}
		
		Map<String, IRuntime> runtimesMap = new LinkedHashMap<String, IRuntime>();
		for (org.eclipse.wst.common.project.facet.core.runtime.IRuntime r : runtimesSet) {
			IRuntime serverRuntime = FacetUtil.getRuntime(r);
			if (serverRuntime != null) {
				runtimesMap.put(r.getLocalizedName(), serverRuntime);
			}
		}
		return runtimesMap;
	}

	protected void validateEnterpriseRepo() {
		if (warningComponent != null) {
			boolean isWarningLinkVisible = false;
			if (isEnterpriseTargetRuntime()) {
				if (enterpriseRepoStatus == null) {
					enterpriseRepoStatus = MavenArtifactHelper.checkEnterpriseRequirementsAvailable(projectExample); 
				}
				isWarningLinkVisible = !enterpriseRepoStatus.isOK();
				if (isWarningLinkVisible) {
					warningComponent.setLinkText(enterpriseRepoStatus.getMessage());
					//warninglink.setText(enterpriseRepoStatus.getMessage());
					warningComponent.getParent().layout(true, true);
				}
			}
			warningComponent.setVisible(isWarningLinkVisible);
		}
	}

	@Override
	public void dispose() {
		if (dialogSettings != null && serverRuntimes != null && serverTargetCombo != null) {
			IRuntime lastUsedRuntime = serverRuntimes.get(serverTargetCombo.getText());
			if (lastUsedRuntime != null) {
				dialogSettings.put(TARGET_RUNTIME, lastUsedRuntime.getId());
			}
		}
		if (listener != null) {
			ServerCore.removeRuntimeLifecycleListener(listener);
			listener = null;
		}
		
	    saveInputHistory();

		super.dispose();
	}
	
	
	/********** code below copied from {@link AbstractMavenWizardPage} **********/
	
	/** the history limit */
	protected static final int MAX_HISTORY = 15;

	/** dialog settings to store input history */
	protected IDialogSettings dialogSettings;

    /** the Map of field ids to List of comboboxes that share the same history */
    private Map<String, List<Combo>> fieldsWithHistory;

    private boolean isHistoryLoaded = false;


	  /** Loads the dialog settings using the page name as a section name. */
	  private void initDialogSettings() {
	    IDialogSettings pluginSettings;
	    
	    // This is strictly to get SWT Designer working locally without blowing up.
	    if( MavenPluginActivator.getDefault() == null ) {
	      pluginSettings = new DialogSettings("Workbench");
	    }
	    else {
	      pluginSettings = M2EUIPluginActivator.getDefault().getDialogSettings();      
	    }
	    
	    dialogSettings = pluginSettings.getSection(getName());
	    if(dialogSettings == null) {
	      dialogSettings = pluginSettings.addNewSection(getName());
	      pluginSettings.addSection(dialogSettings);
	    }
	  }

	  /** Loads the input history from the dialog settings. */
	  private void loadInputHistory() {
	    for(Map.Entry<String, List<Combo>> e : fieldsWithHistory.entrySet()) {
	      String id = e.getKey();
	      String[] items = dialogSettings.getArray(id);
	      if(items != null) {
	        for(Combo combo : e.getValue()) {
	          String text = combo.getText();
	          combo.setItems(items);
	          if(text.length() > 0) {
	            // setItems() clears the text input, so we need to restore it
	            combo.setText(text);
	          }
	        }
	      }
	    }
	  }

	  /** Saves the input history into the dialog settings. */
	  private void saveInputHistory() {
	    for(Map.Entry<String, List<Combo>> e : fieldsWithHistory.entrySet()) {
	      String id = e.getKey();

	      Set<String> history = new LinkedHashSet<String>(MAX_HISTORY);

	      for(Combo combo : e.getValue()) {
	        String lastValue = combo.getText();
	        if(lastValue != null && lastValue.trim().length() > 0) {
	          history.add(lastValue);
	        }
	      }

	      Combo combo = e.getValue().iterator().next();
	      String[] items = combo.getItems();
	      for(int j = 0; j < items.length && history.size() < MAX_HISTORY; j++ ) {
	        history.add(items[j]);
	      }

	      dialogSettings.put(id, history.toArray(new String[history.size()]));
	    }
	  }

	  /** Adds an input control to the list of fields to save. */
	  protected void addFieldWithHistory(String id, Combo combo) {
	    if(combo != null) {
	      List<Combo> combos = fieldsWithHistory.get(id);
	      if(combos == null) {
	        combos = new ArrayList<Combo>();
	        fieldsWithHistory.put(id, combos);
	      }
	      combos.add(combo);
	    }
	  }

}
