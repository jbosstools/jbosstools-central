package org.jboss.tools.maven.ui.internal.libprov;

import org.jboss.tools.maven.ui.Messages;
import org.maven.ide.components.pom.Dependency;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class DependencyEditingSupport extends EditingSupport {

	private static String[] scopes = {"","provided", "compile", "test", "runtime", "system","import"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	private static String[] types = {"","jar","war","rar","ear","par","ejb","ejb3","ejb-client","test-jar","java-source","javadoc","maven-plugin","pom"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$
	
	private CellEditor editor;
	private int column;

	public DependencyEditingSupport(ColumnViewer viewer, int column) {
		super(viewer);
		switch (column) {
		case 3:
			editor = new ComboBoxCellEditor(((TableViewer) viewer).getTable(),
					scopes);
			break;
		case 4:
			editor = new ComboBoxCellEditor(((TableViewer) viewer).getTable(),
					types);
			break;
		default:
			editor = new TextCellEditor(((TableViewer) viewer).getTable());
		}

		
		this.column = column;
	}


	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected Object getValue(Object element) {
		Dependency dependency = (Dependency) element;
		String value = null;
		switch (this.column) {
		case 0:
			value = dependency.getGroupId();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			return value;
		case 1:
			value = dependency.getArtifactId();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			return value;
		case 2:
			value = dependency.getVersion();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			return value;
		case 3:
			for (int i = 0; i < scopes.length; i++) {
				if (scopes[i].equals(dependency.getScope())) {
					return i;
				}
			}
			return 0;
		case 4:
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(dependency.getType())) {
					return i;
				}
			}
			return 0;
		default:
			break;
		}
		return null;

	}

	@Override
	protected void setValue(Object element, Object value) {
		Dependency dependency = (Dependency) element;

		String valueString = null;
		int intValue = 0;
		if (column < 3) {
			if (value == null) {
				valueString = null;
			} else {
				valueString = value.toString();
			}
		} else {
			intValue = (Integer)value;
		}
		switch (this.column) {
		case 0:
			dependency.setGroupId(valueString);
			break;
		case 1:
			dependency.setArtifactId(valueString);
			break;
		case 2:
			dependency.setVersion(valueString);
			break;
		case 3:
			if (intValue > scopes.length) {
				intValue = 0;
			}
			dependency.setScope(scopes[intValue]);
			break;
		case 4:
			if (intValue > types.length) {
				intValue = 0;
			}
			dependency.setType(types[intValue]);
			break;
		
		default:
			break;
		}

		getViewer().update(element, null);

	}

}
