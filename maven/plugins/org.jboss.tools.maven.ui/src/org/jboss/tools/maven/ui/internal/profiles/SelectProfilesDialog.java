/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.profiles;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jboss.tools.maven.core.profiles.ProfileState;
import org.jboss.tools.maven.ui.Messages;

public class SelectProfilesDialog extends TitleAreaDialog implements
		IMenuListener {

	private static int PROFILE_ID_COLUMN = 0;
	private static int SOURCE_COLUMN = 1;

	private CheckboxTableViewer profileTableViewer;
	private Button offlineModeBtn;
	private Button forceUpdateBtn;

	private boolean offlineMode ;
	private boolean forceUpdate;

	List<ProfileSelection> sharedProfiles;
	Set<IMavenProjectFacade> facades;
	IMavenProjectFacade facade;
	
	public SelectProfilesDialog(Shell parentShell, Set<IMavenProjectFacade> facades,
			List<ProfileSelection> sharedProfiles) {
		super(parentShell);
		offlineMode = MavenPlugin.getMavenConfiguration().isOffline();
		this.facades = facades;
		if(facades.size() == 1){
			facade = facades.iterator().next();
		}
		this.sharedProfiles = sharedProfiles; 
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.SelectProfilesDialog_Select_Maven_profiles);
	}
	
	/**
	 * Make the dialog resizeable
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 12;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setTitle(Messages.SelectProfilesDialog_Maven_profile_selection);
		String text;
		if (facade == null) {
			text = "Select the active profiles for selected projects";
		} else {
			text = NLS.bind(
					Messages.SelectProfilesDialog_Select_the_active_Maven_profiles,
					facade.getProject().getName());
		}
		setMessage(text);

		boolean hasProfiles = !sharedProfiles.isEmpty();
		Label lblAvailable = new Label(container, SWT.NONE);
		String textLabel;
		if (hasProfiles) {
			if (facade == null) {
				textLabel = "Common profiles for selected projects";
			} else {
				textLabel = Messages.SelectProfilesDialog_Available_profiles;
			}
		} else {
			if (facade == null) {
				textLabel = "There are no common profiles for the selected projects";
			} else {
				textLabel = 
				NLS.bind(Messages.SelectProfilesDialog_Project_has_no_available_profiles, facade.getProject().getName());
			}
		}
		lblAvailable.setText(textLabel);
		lblAvailable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 2, 1));

		if (hasProfiles) {

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
			gd.heightHint = 150;
			gd.widthHint = 500;

			profileTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
			Table table = profileTableViewer.getTable();
			table.setLayoutData(gd);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);

			TableColumn profileColumn = new TableColumn(table, SWT.NONE);
			profileColumn.setText(Messages.SelectProfilesDialog_Profile_id_header);
			profileColumn.setWidth(350);

			TableColumn sourceColumn = new TableColumn(table, SWT.NONE);
			sourceColumn.setText(Messages.SelectProfilesDialog_Profile_source_header);
			sourceColumn.setWidth(120);

			profileTableViewer
					.setContentProvider(new IStructuredContentProvider() {

						public void inputChanged(Viewer viewer,
								Object oldInput, Object newInput) {
							// nothing to do
						}

						public void dispose() {
							// nothing to do
						}

						@SuppressWarnings("rawtypes")
						public Object[] getElements(Object input) {
							if (input instanceof Collection) {
								return ((Collection) input).toArray();
							}
							return null;
						}
					});

			profileTableViewer.setLabelProvider(new ProfileLabelProvider(parent
					.getFont()));

			
			profileTableViewer.addCheckStateListener(new ICheckStateListener() {
				
				public void checkStateChanged(CheckStateChangedEvent event) {
					ProfileSelection profile = (ProfileSelection) event.getElement();
					if (profileTableViewer.getGrayed(profile)) {
						profileTableViewer.setGrayed(profile, false);
					}
					profile.setSelected(profileTableViewer.getChecked(profile));
					if (profile.getActivationState() == null) {
						profile.setActivationState(ProfileState.Active);
					}
				}
			});
			
			profileTableViewer.setInput(sharedProfiles);

			addSelectionButton(container, Messages.SelectProfilesDialog_SelectAll, true);

			addSelectionButton(container, Messages.SelectProfilesDialog_DeselectAll, false);

			offlineModeBtn = addCheckButton(container, Messages.SelectProfilesDialog_Offline, offlineMode);

			forceUpdateBtn = addCheckButton(container, Messages.SelectProfilesDialog_Force_update, forceUpdate);

			createMenu();

		}

		return area;
	}

	private Button addCheckButton(Composite container, String label, boolean selected) {
		Button checkBtn = new Button(container, SWT.CHECK);
		checkBtn.setText(label);
		checkBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				true, false, 2, 1));
		checkBtn.setSelection(selected);
		return checkBtn;
	}

	private Button addSelectionButton(Composite container, String label, final boolean ischecked) {
		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		button.setText(label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				profileTableViewer.setAllGrayed(false);
				for (ProfileSelection profile : sharedProfiles) {
					profileTableViewer.setChecked(profile, ischecked);
					
					profile.setSelected(profileTableViewer.getChecked(profile));
					if (profile.getActivationState() == null) {
						profile.setActivationState(ProfileState.Active);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		return button;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (profileTableViewer != null) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		if (profileTableViewer != null) {
			//Object[] obj = profileTableViewer.getCheckedElements();
			//for (int i = 0; i < obj.length; i++) {}
			offlineMode = offlineModeBtn.getSelection();
			forceUpdate = forceUpdateBtn.getSelection();
		}
		super.okPressed();
	}

	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		Menu contextMenu = menuMgr.createContextMenu(profileTableViewer
				.getControl());
		menuMgr.addMenuListener(this);
		profileTableViewer.getControl().setMenu(contextMenu);
		menuMgr.setRemoveAllWhenShown(true);

		for (ProfileSelection p : sharedProfiles) {
			Boolean selected = p.getSelected();
			if (selected ==null || p.getActivationState() == null) {
				profileTableViewer.setGrayed(p, true);
				profileTableViewer.setChecked(p, true);
			} else if(selected != null) {
				profileTableViewer.setChecked(p, selected );
			}
		}
	}

	final Action activationAction = new Action("") { //$NON-NLS-1$
		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) profileTableViewer
					.getSelection();
			if (!selection.isEmpty()) {
				final ProfileSelection entry = (ProfileSelection) selection.getFirstElement();
				entry.setActivationState(ProfileState.Active);
				profileTableViewer.setChecked(entry, true);
				profileTableViewer.setGrayed(entry, false);
				profileTableViewer.refresh();
			}
			super.run();
		}
	};
	final Action deActivationAction = new Action("") { //$NON-NLS-1$
		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) profileTableViewer
					.getSelection();
			if (!selection.isEmpty()) {
				final ProfileSelection entry = (ProfileSelection) selection.getFirstElement();
				entry.setActivationState(ProfileState.Disabled);
				profileTableViewer.setChecked(entry, true);
				profileTableViewer.setGrayed(entry, false);
				profileTableViewer.refresh();
			}
			super.run();
		}
	};

	
	public void menuAboutToShow(IMenuManager manager) {

		IStructuredSelection selection = (IStructuredSelection) profileTableViewer
				.getSelection();
		if (!selection.isEmpty()) {
			final ProfileSelection entry = (ProfileSelection) selection.getFirstElement();
			String text = "";
			ProfileState state = entry.getActivationState();
			if ( state == null || state.equals(ProfileState.Disabled)) {
				text = Messages.SelectProfilesDialog_Activate_menu;
				activationAction.setText(NLS.bind(text, entry.getId()));
				manager.add(activationAction);
			} 
			if( !ProfileState.Disabled.equals(state)) {
				text = Messages.SelectProfilesDialog_Deactivate_menu;
				deActivationAction.setText(NLS.bind(text, entry.getId()));
				manager.add(deActivationAction);
			}
		}
	}

	public boolean isOffline() {
		return offlineMode;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}
	
	private class ProfileLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		
		private Font implicitActivationFont;
		
		private Color inactiveColor;
		
		public ProfileLabelProvider(Font defaultFont) {
			FontData[] fds = defaultFont.getFontData();
			for (FontData fd : fds) {
				fd.setStyle(SWT.ITALIC);
			}
			implicitActivationFont = new Font(defaultFont.getDevice(), fds);
			inactiveColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		}
		
		public Font getFont(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			Font font = null;
			if (Boolean.TRUE.equals(entry.getAutoActive())
					&& PROFILE_ID_COLUMN == columnIndex) {
				font = implicitActivationFont;
			}
			return font;
		}
		
		public Color getForeground(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			if (ProfileState.Disabled.equals(entry.getActivationState())) {
				return inactiveColor;
			}
			return null;
		}
		
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			StringBuilder text = new StringBuilder();
			if (entry != null) {
				if (columnIndex == PROFILE_ID_COLUMN) {
					text.append(entry.getId());

					ProfileState state = entry.getActivationState();
					if (state == ProfileState.Disabled) {
						text.append(Messages.SelectProfilesDialog_deactivated);
					} else if (Boolean.TRUE.equals(entry.getAutoActive())) {
						text.append(Messages.SelectProfilesDialog_autoactivated);
					}
				} else if (columnIndex == SOURCE_COLUMN) {
					text.append(entry.getSource());
				}
			}
			return text.toString();
		}
	}
}
