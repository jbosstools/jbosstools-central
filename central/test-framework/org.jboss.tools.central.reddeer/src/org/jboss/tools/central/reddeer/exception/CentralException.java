/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.central.reddeer.exception;

/**
 * 
 * @author rhopp
 *
 */

public class CentralException extends RuntimeException {
	
	public CentralException(String message) {
		super(message);
	}
	
	public CentralException(String message, Throwable cause){
		super(message, cause);
	}
	
	public CentralException(Throwable cause){
		super(cause);
	}

}
