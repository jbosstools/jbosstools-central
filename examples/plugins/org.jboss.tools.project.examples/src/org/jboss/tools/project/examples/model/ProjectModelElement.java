/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

/**
 * @author snjeza
 * 
 */
public interface ProjectModelElement {

	public String getName();
	public String getDescription();
	public String getShortDescription();
	public String getSite();
}
