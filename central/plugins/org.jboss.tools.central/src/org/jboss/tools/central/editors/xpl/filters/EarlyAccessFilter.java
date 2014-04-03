package org.jboss.tools.central.editors.xpl.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;

public class EarlyAccessFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (! (element instanceof ConnectorDescriptor)) {
			return false;
		}
		String cert = ((ConnectorDescriptor)element).getCertificationId();
		return cert == null || !cert.toLowerCase().contains("earlyaccess");
	}

}
