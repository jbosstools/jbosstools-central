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
package org.jboss.tools.central.test;

import org.jboss.tools.central.test.xpl.ExpressionResolverTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@Suite.SuiteClasses({
	CentralTest.class,
	ExpressionResolverTest.class,
})

@RunWith(Suite.class)
public class JBossCentralAllTests {

}