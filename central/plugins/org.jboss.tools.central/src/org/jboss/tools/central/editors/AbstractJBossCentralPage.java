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

package org.jboss.tools.central.editors;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class AbstractJBossCentralPage extends FormPage {

	public static final String ID_PREFIX = "org.jboss.tools.central.editors.";

	public AbstractJBossCentralPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected Composite createComposite(FormToolkit toolkit, Composite body) {
		Composite composite = toolkit.createComposite(body, SWT.NONE);
	    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	    GridLayout layout = new GridLayout();
	    layout.marginWidth = 0;
	    layout.marginHeight = 0;
	    composite.setLayout(layout);
		return composite;
	}

	protected Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	protected Section createSection(FormToolkit toolkit, Composite parent, String name, int style) {
		final Section section = toolkit.createSection(parent, style);
		section.setText(name);
	    section.setLayout(new GridLayout());
		return section;
	}

	protected Composite createLoadingComposite(FormToolkit toolkit, Composite parent) {
		Composite composite = toolkit.createComposite(parent, SWT.WRAP);
		composite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
		composite.setLayoutData(gd);
		try {
			final RefreshIndicator indicator = new RefreshIndicator(composite, "/icons/loader.gif", SWT.NONE);
			gd = new GridData(GridData.FILL, GridData.FILL, false, false);
			gd.widthHint = 30;
			gd.heightHint = 10;
			indicator.setLayoutData(gd);
			indicator.setBusy(true);
			toolkit.adapt(indicator, true, false);
			composite.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					indicator.dispose();
				}
			});
			
		} catch (IOException e) {
			JBossCentralActivator.log(e);
		}
		FormText formText = toolkit.createFormText(composite, true);
		gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    formText.setLayoutData(gd);
		String text = JBossCentralActivator.FORM_START_TAG +
				"Refreshing..." +
				JBossCentralActivator.FORM_END_TAG;
		formText.setText(text, true, false);
		return composite;
	}
}
