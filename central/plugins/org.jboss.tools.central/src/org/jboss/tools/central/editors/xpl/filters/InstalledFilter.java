package org.jboss.tools.central.editors.xpl.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;

public class InstalledFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return element instanceof ConnectorDescriptor && ! ((ConnectorDescriptor)element).isInstalled();
	}

}
