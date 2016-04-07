/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.hibernate.libprov;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jpt.common.core.JptWorkspace;
import org.eclipse.jpt.common.core.libval.LibraryValidator;
import org.eclipse.jpt.common.core.libval.LibraryValidatorManager;
import org.eclipse.jpt.jpa.core.JpaPlatform;
import org.eclipse.jpt.jpa.core.libprov.JpaLibraryProviderInstallOperationConfig;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperationConfig;

/**
 * @author Fred Bricon
 * 
 */
public class MavenHibernateLibraryProviderInstallOperationConfig extends MavenLibraryProviderInstallOperationConfig implements JpaLibraryProviderInstallOperationConfig {

	private JpaPlatform.Config jpaPlatformDescription;
	
	@Override
	public JpaPlatform.Config getJpaPlatformConfig() {
		return jpaPlatformDescription;
	}
	
	@Override
	public void setJpaPlatformConfig(JpaPlatform.Config jpaPlatform) {
		this.jpaPlatformDescription = jpaPlatform;
	}	
	
	@Override
	public synchronized IStatus validate() {
		IStatus status = super.validate();
		if (! status.isOK()) {
			return status;
		}
		if (getJpaPlatformConfig() != null) {
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			JptWorkspace jptWorkspace = (JptWorkspace) workspace.getAdapter(JptWorkspace.class);
			if (jptWorkspace != null) {
				LibraryValidatorManager lvm = jptWorkspace.getLibraryValidatorManager();
				if (lvm != null) {
					for (LibraryValidator libraryValidator : lvm.getLibraryValidators(this)) {
						status = libraryValidator.validate(this);
						if (! status.isOK()) {
							return status;
						}
					}
				}
			}
		}
		
		return Status.OK_STATUS;
	}

}
