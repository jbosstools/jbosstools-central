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
package org.jboss.tools.maven.reddeer.project.examples.wait;

import org.eclipse.reddeer.swt.api.Link;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.swt.exception.SWTLayerException;
import org.eclipse.reddeer.swt.impl.link.DefaultLink;

/**
 * Waits for link with information about RH enterprise maven repo not being in settings.xml.
 * 
 * @author rhopp
 *
 */
public class MavenRepositoryNotFound extends AbstractWaitCondition{
	
	@Override
	public String description() {
		return "Waiting for maven repository to be found";
	}
	
	@Override
	public boolean test() {
		Link link = null;
		try{
			link = new DefaultLink();
			return link.getText().startsWith("This project has a dependency");
		}catch(SWTLayerException ex){
			return false;
		}
	}
}