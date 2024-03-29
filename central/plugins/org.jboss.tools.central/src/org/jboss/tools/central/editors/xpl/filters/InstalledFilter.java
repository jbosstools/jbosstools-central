/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl.filters;

import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Excludes {@link ConnectorDescriptor} that are already installed
 * @author mistria
 *
 */
@SuppressWarnings("restriction")
public class InstalledFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return element instanceof CatalogItem && ! ((CatalogItem)element).isInstalled();
	}

}
