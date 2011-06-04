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
package org.jboss.tools.maven.ui.internal.libprov;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.m2e.model.edit.pom.Exclusion;

/**
 * 
 * @author snjeza
 *
 */
public class ExclusionEditingSupport extends EditingSupport {

	private CellEditor editor;
	private int column;

	public ExclusionEditingSupport(ColumnViewer viewer, int column) {
		super(viewer);
		editor = new TextCellEditor(((TableViewer) viewer).getTable());
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
		Exclusion exclusion = (Exclusion) element;
		String value = null;
		switch (this.column) {
		case 0:
			value = exclusion.getGroupId();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			return value;
		case 1:
			value = exclusion.getArtifactId();
			if (value == null) {
				value = ""; //$NON-NLS-1$
			}
			return value;
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		Exclusion exclusion = (Exclusion) element;

		String valueString = null;
		if (value == null) {
			valueString = null;
		} else {
			valueString = value.toString();
		}
		switch (this.column) {
		case 0:
			exclusion.setGroupId(valueString);
			break;
		case 1:
			exclusion.setArtifactId(valueString);
			break;
		default:
			break;
		}
		getViewer().update(element, null);

	}

}
