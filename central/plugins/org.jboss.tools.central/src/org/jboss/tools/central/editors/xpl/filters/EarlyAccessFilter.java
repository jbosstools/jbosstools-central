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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;

/**
 * Filter that excludes connector which have certifacte id containing
 * "earlyaccess" and that are not installed.
 * @author mistria
 *
 */
public class EarlyAccessFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (! (element instanceof ConnectorDescriptor)) {
			return true;
		}
		ConnectorDescriptor connector = (ConnectorDescriptor)element;
		return !JBossDiscoveryUi.isEarlyAccess(connector);
	}

}
