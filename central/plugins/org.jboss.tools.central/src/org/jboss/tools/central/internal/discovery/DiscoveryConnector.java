/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.discovery.core.model.ValidationException;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.internal.xpl.ExpressionResolver;

/**
 * 
 * (non-Javadoc)
 * @see org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor
 * 
 * @author snjeza
 * 
 */
public class DiscoveryConnector extends org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector {

	@Override
	public void validate() throws ValidationException {
		if (siteUrl != null) {
			try {
				siteUrl = ExpressionResolver.DEFAULT_RESOLVER.resolve(siteUrl);
				super.validate();
			} catch (Exception e) {
				String message = "Invalid connectorDescriptor/@siteUrl: " + siteUrl;
				IStatus status = new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID,
						message, e);
				JBossCentralActivator.getDefault().getLog().log(status);
			}
		}
	}

}
