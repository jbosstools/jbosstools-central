package org.jboss.tools.central.editors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class AbstractJBossCentralPage extends FormPage {

	public static final String ID_PREFIX = "org.jboss.tools.central.editors.";

	public AbstractJBossCentralPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected Section createSection(FormToolkit toolkit, Composite composite, String title, int style) {
		Section section = toolkit.createSection(composite, style);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
	    section.setText(title);
	    GridLayout layout = new GridLayout();
	    layout.marginWidth = 0;
	    layout.marginHeight = 0;
	    section.setLayout(layout);
	    return section;
	}

	protected Composite createComposite(FormToolkit toolkit, Composite body) {
		Composite composite = toolkit.createComposite(body, SWT.NONE);
	    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
}
