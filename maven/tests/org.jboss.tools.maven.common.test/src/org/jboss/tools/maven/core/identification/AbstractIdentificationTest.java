/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.identification;

import java.io.File;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractIdentificationTest {

	protected File arquillian;//Has maven properties
	protected File junit;//Has no maven properties
	protected File jansi;//Has multiple maven properties
	protected File groovy_jsr223;//only has manifest info
	
	@Before
	public void initFiles() {
		junit = new File("resources/junit_4_10.jar");
		arquillian = new File("resources/arquillian-core-spi.jar");
		jansi = new File("resources/jansi-1.6.jar");
		groovy_jsr223 = new File("resources/groovy-jsr223-2.0.4.jar");
	}
	
	@After
	public void disposeFiles() {
		junit = null;
		arquillian = null;
		jansi = null;
		groovy_jsr223 = null;
	}
	
	
}
