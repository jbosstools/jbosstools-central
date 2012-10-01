/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.hibernate.libprov;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jpt.common.core.libprov.JptLibraryProviderInstallOperationConfig;
import org.eclipse.jpt.common.core.libval.LibraryValidator;
import org.eclipse.jpt.jpa.core.libprov.JpaLibraryProviderInstallOperationConfig;
import org.jboss.tools.maven.core.MavenCoreActivator;

public class HibernateLibValidator implements LibraryValidator {

	@SuppressWarnings("nls")
	public IStatus validate(JptLibraryProviderInstallOperationConfig config) {
		JpaLibraryProviderInstallOperationConfig jpaConfig = (JpaLibraryProviderInstallOperationConfig) config;
		IStatus status;
		if (jpaConfig.getJpaPlatform().getId().contains("hibernate")) {
			status = Status.OK_STATUS;
		} else {
			status = new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID, "This JPA implementation requires an Hibernate-based Platform");
		}
		return status;
	}

}
