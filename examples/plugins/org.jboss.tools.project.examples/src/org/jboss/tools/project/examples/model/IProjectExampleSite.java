/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.net.URI;

public interface IProjectExampleSite {
	public String getName();

	public void setEditable(boolean editable);

	public boolean isEditable();

	public void setExperimental(boolean experimental);

	public boolean isExperimental();

	public void setName(String name);

	public void setUrl(URI url);

	public URI getUrl();
}
