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
package org.jboss.tools.central.internal.xpl;

/**
 * Thrown to indicate that an attempt has been made to resolve
 * expression ${sysPropertyName:defaultValue} in URL and it has
 * failed to parse expression or to resolve it to actual value
 * @author eskimo
 */
public class ExpressionResolutionException extends RuntimeException {

	public ExpressionResolutionException(String message) {
		super(message);
	}

}
