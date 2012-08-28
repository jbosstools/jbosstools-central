/*************************************************************************************
 * Copyright (c) 2010-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.repositories;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.maven.core.repositories.NexusRepository;

/**
 * @author snjeza
 * 
 */
public class EditNexusRepositoryDialog extends Dialog {

	private NexusRepository nexusRepository;
	private String title;

	public EditNexusRepositoryDialog(Shell parentShell,
			NexusRepository repository) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.RESIZE
				| getDefaultOrientation());
		this.nexusRepository = repository;
		if (this.nexusRepository == null) {
			this.nexusRepository = new NexusRepository("", "", true);
			this.title = "New Repository";
		} else {
			this.title = "Edit Repository";
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(title);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		gd.widthHint = 400;
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout(2, false));
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Label nameLabel = new Label(contents, SWT.NONE);
		nameLabel.setText("Name:");

		final Text nameText = new Text(contents, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		nameText.setLayoutData(gd);
		nameText.setText(nexusRepository.getName());

		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				nexusRepository.setName(nameText.getText());
			}
		});

		Label urlLabel = new Label(contents, SWT.NONE);
		urlLabel.setText("URL:");

		final Text urlText = new Text(contents, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		urlText.setLayoutData(gd);
		urlText.setText(nexusRepository.getUrl());

		urlText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				nexusRepository.setUrl(urlText.getText());
			}
		});

		final Button enabledButton = new Button(contents, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		enabledButton.setLayoutData(gd);
		enabledButton.setText("Enabled");
		enabledButton.setSelection(nexusRepository.isEnabled());
		enabledButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				nexusRepository.setEnabled(enabledButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return area;
	}

	public NexusRepository getNexusRepository() {
		return nexusRepository;
	}

}
