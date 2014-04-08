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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

import org.eclipse.equinox.p2.metadata.Version;
import org.jboss.tools.central.editors.xpl.ConnectorDescriptorItemUi;

public class BiggestVersionComparator implements Comparator<ConnectorDescriptorItemUi>, Serializable {
	
	private static final long serialVersionUID = -8050934311301624177L;

	@Override
	public int compare(ConnectorDescriptorItemUi item1, ConnectorDescriptorItemUi item2) {
		if (item1 == item2) {
			return 0;
		}
		for (Entry<String, Version> entry : item1.getConnectorUnits().entrySet()) {
			Version otherVersion = item2.getConnectorUnits().get(entry.getKey());
			if (otherVersion == null) {
				continue;
			}
			int diffVersion = otherVersion.compareTo(entry.getValue());
			if (diffVersion != 0) {
				return diffVersion;
			}
		}
		return 0;
	}

}
