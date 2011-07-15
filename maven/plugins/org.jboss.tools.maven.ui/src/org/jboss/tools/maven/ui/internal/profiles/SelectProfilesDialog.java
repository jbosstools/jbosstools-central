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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
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
import org.jboss.tools.maven.ui.Messages;

public class SelectProfilesDialog extends TitleAreaDialog implements
		IMenuListener {

	private static int PROFILE_ID_COLUMN = 0;
	private static int SOURCE_COLUMN = 1;

	private CheckboxTableViewer profileTableViewer;
	private Button offlineModeBtn;
	private Button forceUpdateBtn;

	private final IMavenProjectFacade facade;

	private List<Map.Entry<Profile, Boolean>> availableProfiles;
	private List<String> initialInactiveProfileIds = new ArrayList<String>();
	private List<String> initialActiveProfileIds = new ArrayList<String>();
	private List<String> inactiveProfileIds = new ArrayList<String>();
	private List<String> selectedProfiles;

	private boolean offlineMode ;
	private boolean forceUpdate;

	public SelectProfilesDialog(Shell parentShell, IMavenProjectFacade facade,
			Map<Profile, Boolean> availableProjectProfiles,
			Map<Profile, Boolean> availableSettingsProfiles) {
		super(parentShell);
		this.facade = facade;

		availableProfiles = new ArrayList<Map.Entry<Profile, Boolean>>(availableProjectProfiles.entrySet());
		availableProfiles.addAll(availableSettingsProfiles.entrySet());

		offlineMode = MavenPlugin.getMavenConfiguration().isOffline();

		final IProjectConfigurationManager configurationManager = MavenPlugin
				.getProjectConfigurationManager();
		final ResolverConfiguration configuration = configurationManager
				.getResolverConfiguration(facade.getProject());
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

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 12;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setTitle(Messages.SelectProfilesDialog_Maven_profile_selection);
		setMessage(NLS.bind(
				Messages.SelectProfilesDialog_Select_the_active_Maven_profiles,
				facade.getProject().getName()));

		boolean hasProfiles = !availableProfiles.isEmpty();
		Label lblAvailable = new Label(container, SWT.NONE);
		String textLabel;
		if (hasProfiles) {
			textLabel = Messages.SelectProfilesDialog_Available_profiles;
		} else {
			textLabel = Messages.SelectProfilesDialog_Project_has_no_available_profiles;
		}
		lblAvailable.setText(NLS.bind(textLabel, facade.getProject().getName()));
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

			profileTableViewer.setInput(availableProfiles);

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
				for (Map.Entry<Profile, Boolean> entry : availableProfiles) {
					profileTableViewer.setChecked(entry, ischecked);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		return button;
	}

	@SuppressWarnings("unchecked")
	private Map.Entry<Profile, Boolean> getEntry(Object o) {
		if (o instanceof Map.Entry<?, ?>) {
			return (Map.Entry<Profile, Boolean>) o;
		}
		return null;
	}

	private boolean isDeactivated(Entry<Profile, Boolean> entry) {
		return entry != null
				&& inactiveProfileIds.contains(entry.getKey().getId());
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
			Object[] obj = profileTableViewer.getCheckedElements();
			List<String> selectedProfiles = new ArrayList<String>(obj.length);
			for (int i = 0; i < obj.length; i++) {
				@SuppressWarnings("unchecked")
				Map.Entry<Profile, Boolean> entry = (Map.Entry<Profile, Boolean>) obj[i];
				String id = entry.getKey().getId();
				if (isDeactivated(entry)) {
					selectedProfiles.add("!" + id); //$NON-NLS-1$
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

	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		Menu contextMenu = menuMgr.createContextMenu(profileTableViewer
				.getControl());
		menuMgr.addMenuListener(this);
		profileTableViewer.getControl().setMenu(contextMenu);
		menuMgr.setRemoveAllWhenShown(true);

		for (Map.Entry<Profile, Boolean> entry : availableProfiles) {
			String id = entry.getKey().getId();
			boolean isSelected = initialActiveProfileIds.contains(id)
								 || inactiveProfileIds.contains(id);
			profileTableViewer.setChecked(entry, isSelected);
		}
	}

	final Action activationAction = new Action("") { //$NON-NLS-1$
		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) profileTableViewer
					.getSelection();
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

	public void menuAboutToShow(IMenuManager manager) {

		IStructuredSelection selection = (IStructuredSelection) profileTableViewer
				.getSelection();
		if (!selection.isEmpty()) {
			final Map.Entry<Profile, Boolean> entry = getEntry(selection
					.getFirstElement());
			final boolean isDeactivated = isDeactivated(entry);
			String text;
			if (isDeactivated) {
				text = Messages.SelectProfilesDialog_Activate_menu;
			} else {
				text = Messages.SelectProfilesDialog_Deactivate_menu;
			}
			activationAction.setText(NLS.bind(text, entry.getKey().getId()));
			manager.add(activationAction);
		}
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
			Entry<Profile, Boolean> entry = getEntry(element);
			Font font = null;
			if (entry != null && Boolean.TRUE.equals(entry.getValue())
					&& PROFILE_ID_COLUMN == columnIndex) {
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
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			Entry<Profile, Boolean> entry = getEntry(element);
			StringBuilder text = new StringBuilder();
			if (entry != null) {
				boolean isDeactivated = isDeactivated(entry);
				Profile profile = entry.getKey();
				if (columnIndex == PROFILE_ID_COLUMN) {
					text.append(profile.getId());
					if (isDeactivated) {
						text.append(Messages.SelectProfilesDialog_deactivated);
					} else if (Boolean.TRUE.equals(entry.getValue())) {
						text.append(Messages.SelectProfilesDialog_autoactivated);
					}
				} else if (columnIndex == SOURCE_COLUMN) {
					text.append(profile.getSource());
				}
			}
			return text.toString();
		}
	}
}
