/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.dialog;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
* @author snjeza
* 
*/
public class MarkerLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof IMarker)) {
			return null;
		}
		IMarker marker = (IMarker) element;
		switch (columnIndex) {
		case 0:
			return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		case 1:
			return marker.getResource().getName();
		case 2:
			try {
				IExtensionPoint extensionPoint = Platform
						.getExtensionRegistry().getExtensionPoint(
								"org.eclipse.core.resources.markers"); //$NON-NLS-1$
				IExtension[] extensions = extensionPoint.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IExtension extension = extensions[i];
					String id = extension.getUniqueIdentifier();
					if (id != null && id.equals(marker.getType())) {
						String name = extension.getLabel();
						if (name != null) {
							return name;
						}
					}
				}
				return marker.getType();
			} catch (CoreException e) {
				return ""; //$NON-NLS-1$
			}
		}
		return null;
	}

}
