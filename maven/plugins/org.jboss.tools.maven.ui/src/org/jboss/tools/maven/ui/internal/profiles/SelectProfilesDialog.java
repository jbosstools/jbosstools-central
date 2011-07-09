/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.maven.ui.internal.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.model.Profile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.maven.ui.Messages;

public class SelectProfilesDialog extends TitleAreaDialog implements IMenuListener {

  private CheckboxTableViewer profileTableViewer;

  private Button offlineModeBtn;

  private Button forceUpdateBtn;

  private final IMavenProjectFacade facade;

  private List<Map.Entry<Profile, Boolean>> availableProfiles;
  
  private List<String> initialInactiveProfileIds = new ArrayList<String>();
  private List<String> initialActiveProfileIds = new ArrayList<String>();
  private List<String> inactiveProfileIds = new ArrayList<String>();
  
  private List<String> selectedProfiles;
  
  private boolean offlineMode;

  private boolean forceUpdate;

  public SelectProfilesDialog(Shell parentShell, IMavenProjectFacade facade, 
							  Map<Profile, Boolean> availableProjectProfiles, 
							  Map<Profile, Boolean> availableSettingsProfiles) {
    super(parentShell);
    this.facade = facade;

    availableProfiles = new ArrayList<Map.Entry<Profile,Boolean>>(availableProjectProfiles.entrySet());
    availableProfiles.addAll(availableSettingsProfiles.entrySet());
    
    offlineMode = MavenPlugin.getMavenConfiguration().isOffline();
    forceUpdate = false;
    
	final IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
	final ResolverConfiguration configuration =configurationManager.getResolverConfiguration(facade.getProject());
	for (String p : configuration.getActiveProfileList()) {
		if (p.startsWith("!")) { //$NON-NLS-1$
			initialInactiveProfileIds.add(p.substring(1));
		} else {
			initialActiveProfileIds.add(p);
		}
	}
	inactiveProfileIds.addAll(initialInactiveProfileIds);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.SelectProfilesDialog_Select_Maven_profiles);
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  @SuppressWarnings("rawtypes")
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    
    GridLayout layout = new GridLayout(2, false);
    layout.marginLeft = 12;
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));

    setTitle(Messages.SelectProfilesDialog_Maven_profile_selection);
    setMessage(Messages.SelectProfilesDialog_Select_the_active_Maven_profiles+facade.getProject().getName());

    boolean hasProfiles = !availableProfiles.isEmpty();
    Label lblAvailable = new Label(container, SWT.NONE);
    if (hasProfiles) {
    	lblAvailable.setText(Messages.SelectProfilesDialog_Available_profiles);
    } else {
    	lblAvailable.setText(Messages.SelectProfilesDialog_Project_has_no_available_profiles+facade.getProject().getName() +"' has no available profiles"); //$NON-NLS-2$
    }
    lblAvailable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

    if (hasProfiles) {
        profileTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        profileTableViewer.setContentProvider(new IStructuredContentProvider() {
    		
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing to do
    		}
    		
    		public void dispose() {
    			// nothing to do
    		}
    		
    		public Object[] getElements(Object input) {
    	        if(input instanceof Collection) {
    	            return ((Collection) input).toArray();
    	          }
    	          return null;
    		}
        });
        
        profileTableViewer.setLabelProvider(new ProfileLabelProvider(this));

        profileTableViewer.setInput(availableProfiles);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
        gd.heightHint = 200;
        gd.widthHint = 200;
        profileTableViewer.getTable().setLayoutData(gd);

        Button selectAllBtn = new Button(container, SWT.NONE);
        selectAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        selectAllBtn.setText(Messages.SelectProfilesDialog_SelectAll);
        selectAllBtn.addSelectionListener(new SelectionListener() {
          public void widgetSelected(SelectionEvent e) {
            for(Map.Entry<Profile, Boolean> entry: availableProfiles) {
              profileTableViewer.setChecked(entry, true);
            }
          }

          public void widgetDefaultSelected(SelectionEvent e) {

          }
        });

        Button deselectAllBtn = new Button(container, SWT.NONE);
        deselectAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        deselectAllBtn.setText(Messages.SelectProfilesDialog_DeselectAll);
        deselectAllBtn.addSelectionListener(new SelectionListener() {
          public void widgetSelected(SelectionEvent e) {
           for(Map.Entry<Profile, Boolean> entry: availableProfiles) {
              profileTableViewer.setChecked(entry, false);
           }
          }

          public void widgetDefaultSelected(SelectionEvent e) {

          }
        });

        offlineModeBtn = new Button(container, SWT.CHECK);
        offlineModeBtn.setText(Messages.SelectProfilesDialog_Offline);
        offlineModeBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        offlineModeBtn.setSelection(offlineMode);

        forceUpdateBtn = new Button(container, SWT.CHECK);
        forceUpdateBtn.setText(Messages.SelectProfilesDialog_Force_update);
        forceUpdateBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        forceUpdateBtn.setSelection(forceUpdate);

        createMenu();

    }

    return area;
  }

  
  private class ProfileLabelProvider extends LabelProvider implements ITableFontProvider, ITableColorProvider{
	  
    private final SelectProfilesDialog selectProfilesDialog;

    private Font implicitActivationFont;
    
    private Color inactiveColor;
    
	public ProfileLabelProvider(SelectProfilesDialog selectProfilesDialog) {
		this.selectProfilesDialog = selectProfilesDialog;

		Font defaultFont = selectProfilesDialog.profileTableViewer.getTable().getFont();
		FontData[] fds = defaultFont.getFontData();
		for(FontData fd : fds) {
			fd.setStyle(SWT.ITALIC);
		}
		implicitActivationFont = new Font(defaultFont.getDevice(), fds);
		inactiveColor =  Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	}

	public String getText(Object element) {
	  Entry<Profile, Boolean> entry = getEntry(element);
	  StringBuilder text = new StringBuilder();
	  if (entry!= null) {
		  boolean isDeactivated = isDeactivated(entry);
		  Profile profile = entry.getKey();
		  text.append(profile.getId());
		  if (isDeactivated) {
			  text.append(Messages.SelectProfilesDialog_deactivated); 
		  } else if (Boolean.TRUE.equals(entry.getValue())) {
			  text.append(Messages.SelectProfilesDialog_autoactivated); 
		  }
	  }
	  return text.toString();
    }
	
	
      
	public Font getFont(Object element, int columnIndex) {
	  Entry<Profile, Boolean> entry = getEntry(element);
      Font font = null;
	  if (entry!=null && Boolean.TRUE.equals(entry.getValue())) {
    	 font = implicitActivationFont;
      }
	  return font;
	}
	
	public Color getForeground(Object element, int columnIndex) {
		Entry<Profile, Boolean> entry = getEntry(element);
	    if (isDeactivated(entry)) {
	    	return inactiveColor;
	    }
		return null;
	}

	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
	  
  }
  
	private Map.Entry<Profile, Boolean>  getEntry(Object o) {
		if (o instanceof Map.Entry<?,?>) {
			return (Map.Entry<Profile, Boolean> )o;
		}
		return null;
	}
	
	private boolean isDeactivated(Entry<Profile, Boolean> entry) {
		return entry != null && inactiveProfileIds.contains(entry.getKey().getId());
	}
	
  /**
   * Create contents of the button bar.
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
	if (profileTableViewer != null) {
	    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  protected void okPressed() {
	if (profileTableViewer != null) {
	    Object[] obj = profileTableViewer.getCheckedElements();
	    List<String> selectedProfiles = new ArrayList<String>(obj.length);
	    for(int i = 0; i < obj.length; i++ ) {
	    	Map.Entry<Profile, Boolean> entry = (Map.Entry<Profile, Boolean>) obj[i];
	    	String id = entry.getKey().getId();
	    	if (isDeactivated(entry)) {
	    	   selectedProfiles.add("!"+id); //$NON-NLS-1$
	    	} else {
		       selectedProfiles.add(id);
	    	}
	    }
	    this.selectedProfiles = selectedProfiles;
	
	    offlineMode = offlineModeBtn.getSelection();
	    forceUpdate = forceUpdateBtn.getSelection();
	}
    super.okPressed();
  }

  public List<String> getSelectedProfiles() {
    return selectedProfiles;
  }

  public boolean isOffline() {
    return offlineMode;
  }

  public boolean isForceUpdate() {
    return forceUpdate;
  }

  private void createMenu() {
    MenuManager menuMgr = new MenuManager();
    Menu contextMenu = menuMgr.createContextMenu(profileTableViewer.getControl());
    menuMgr.addMenuListener(this);
    profileTableViewer.getControl().setMenu(contextMenu);
    menuMgr.setRemoveAllWhenShown(true);

    for(Map.Entry<Profile, Boolean> entry: availableProfiles) {
    	String id = entry.getKey().getId();
    	boolean isSelected = initialActiveProfileIds.contains(id) || inactiveProfileIds.contains(id);
        profileTableViewer.setChecked(entry, isSelected);
    }
  }
  
  final Action activationAction = new Action("") { //$NON-NLS-1$
		@Override
		public void run() {
	      IStructuredSelection selection = (IStructuredSelection) profileTableViewer.getSelection();
		  if (!selection.isEmpty()) {
			final Map.Entry<Profile, Boolean> entry = getEntry(selection.getFirstElement());
			final boolean isDeactivated = isDeactivated(entry);

			if (isDeactivated) {
				inactiveProfileIds.remove(entry.getKey().getId());
			} else {
				inactiveProfileIds.add(entry.getKey().getId());
				profileTableViewer.setChecked(entry, true);
			}
			  profileTableViewer.refresh();
			}
			super.run();
		}
  };
  
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
   */
  public void menuAboutToShow(IMenuManager manager) {
	  
	  IStructuredSelection selection = (IStructuredSelection) profileTableViewer.getSelection();
		if (!selection.isEmpty()) {
			final Map.Entry<Profile, Boolean> entry = getEntry(selection.getFirstElement());
			final boolean isDeactivated = isDeactivated(entry);
			if (isDeactivated) {
				activationAction.setText(Messages.SelectProfilesDialog_Activate_menu+ entry.getKey().getId());
			} else {
				activationAction.setText(Messages.SelectProfilesDialog_Deactivate_menu+ entry.getKey().getId());
			}
			manager.add(activationAction);
		}
	  
  }
  
  

}
