/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.preferences;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;

/**
 * 
 * @author snjeza
 *
 */
public class SiteDialog extends TitleAreaDialog {

	private static final String ADD_PROJECT_EXAMPLE_SITE = Messages.SiteDialog_Add_Project_Example_Site;
	private static final String EDIT_PROJECT_EXAMPLE_SITE = Messages.SiteDialog_Edit_Project_Example_Site;
	private Image dlgTitleImage;
	private ProjectExampleSite selectedSite;
	private String name;
	private URL url;
	private Text nameText;
	private Text urlText;
	private Button okButton;
	private Sites sites;

	protected SiteDialog(Shell parentShell, ProjectExampleSite site, Sites sites) {
		super(parentShell);
		this.selectedSite = site;
		this.sites = sites;
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Control contents = super.createContents(parent);
		if (selectedSite == null) {
			setTitle(ADD_PROJECT_EXAMPLE_SITE);
			setMessage(ADD_PROJECT_EXAMPLE_SITE);
		} else {
			setTitle(EDIT_PROJECT_EXAMPLE_SITE);
			setMessage(EDIT_PROJECT_EXAMPLE_SITE);
		}
		ImageDescriptor descriptor = ProjectExamplesActivator
		.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID,
				"icons/SiteWizBan.png"); //$NON-NLS-1$
        if(descriptor != null) {
        	dlgTitleImage = descriptor.createImage();
        	setTitleImage(dlgTitleImage);
        }
        
		return contents;
	}

	@Override
	public boolean close() {
        if (dlgTitleImage != null) {
			dlgTitleImage.dispose();
		}
        return super.close();
    }
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
        
		Composite container = new Composite(parentComposite, SWT.FILL);
		GridLayout layout = new GridLayout(3,false);
		layout.marginWidth = layout.marginHeight = 10;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(Messages.SiteDialog_Name);
		nameText = new Text(container, SWT.SINGLE|SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener(){
		
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		
		Label urlLabel = new Label(container, SWT.NONE);
		urlLabel.setText(Messages.SiteDialog_URL);
		urlText = new Text(container, SWT.SINGLE|SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		urlText.setLayoutData(gd);
		urlText.addModifyListener(new ModifyListener(){
			
			public void modifyText(ModifyEvent e) {
				validatePage();
			}

		});
		if (selectedSite != null) {
			urlText.setText(selectedSite.getUrl().toString());
			nameText.setText(selectedSite.getName());
		}
		Button browse = new Button(container,SWT.PUSH);
		browse.setText(Messages.Browse);
		browse.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
				dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$;
			
				String result = dialog.open();
				if (result == null || result.trim().length() == 0) {
					return;
				}
				try {
					String urlString = new File(result).toURL().toString();
					urlText.setText(urlString);
				} catch (MalformedURLException e1) {
					urlText.setText("file:///" + result); //$NON-NLS-1$
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		return parentComposite;
	}

	private boolean validatePage() {
		name = null;
		url = null;
		if (nameText.getText().trim().length() <= 0) {
			setErrorMessage(Messages.SiteDialog_The_name_field_is_required);
			return updateButton(false);
		}
		Set<IProjectExampleSite> siteList = sites.getSites();
		for(IProjectExampleSite site:siteList) {
			if (site != selectedSite && nameText.getText().equals(site.getName())) {
				setErrorMessage(Messages.SiteDialog_The_site_already_exists);
				return updateButton(false);
			}
		}
		if (urlText.getText().trim().length() <= 0) {
			setErrorMessage(Messages.SiteDialog_The_url_field_is_required);
			return updateButton(false);
		}
		try {
			@SuppressWarnings("unused")
			URL url = new URL(urlText.getText());
		} catch (MalformedURLException e) {
			setErrorMessage(Messages.SiteDialog_Invalid_URL);
			return updateButton(false);
		}
		setErrorMessage(null);
		name = nameText.getText();
		try {
			url = new URL(urlText.getText());
		} catch (MalformedURLException ignore) {}
		return updateButton(true);
	}

	private boolean updateButton(boolean enabled) {
		if (okButton != null) {
			okButton.setEnabled(enabled);
		}
		return false;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(selectedSite != null);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }
	
	public String getName() {
		return name;
	}
	
	public URL getURL() {
		return url;
	}
	
}
