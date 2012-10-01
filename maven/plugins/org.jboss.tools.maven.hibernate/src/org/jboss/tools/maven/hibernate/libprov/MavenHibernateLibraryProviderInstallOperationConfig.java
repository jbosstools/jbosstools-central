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
import org.eclipse.jpt.common.core.JptCommonCorePlugin;
import org.eclipse.jpt.common.core.libval.LibraryValidator;
import org.eclipse.jpt.jpa.core.libprov.JpaLibraryProviderInstallOperationConfig;
import org.eclipse.jpt.jpa.core.platform.JpaPlatformDescription;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperationConfig;

/**
 * @author Fred Bricon
 * 
 */
public class MavenHibernateLibraryProviderInstallOperationConfig extends MavenLibraryProviderInstallOperationConfig implements JpaLibraryProviderInstallOperationConfig {

	private JpaPlatformDescription jpaPlatformDescription;
	
	public JpaPlatformDescription getJpaPlatform() {
		return jpaPlatformDescription;
	}
	
	public void setJpaPlatform(JpaPlatformDescription jpaPlatform) {
		this.jpaPlatformDescription = jpaPlatform;
	}	
	
	@Override
	public synchronized IStatus validate() {
		IStatus status = super.validate();
		if (! status.isOK()) {
			return status;
		}
		if (getJpaPlatform() != null) {
			for (LibraryValidator libraryValidator : JptCommonCorePlugin.getLibraryValidators(this)) {
				status = libraryValidator.validate(this);
				if (! status.isOK()) {
					return status;
				}
			}
		}
		
		return Status.OK_STATUS;
	}

}
