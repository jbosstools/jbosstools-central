/*************************************************************************************
 * Copyright (c) 2014-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.model;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;

@SuppressWarnings("nls")
public class ProjectExampleSiteParser extends JaxbParser {

	private static final JAXBContext jaxbContext; 
	
	static {
		try {
			jaxbContext = JAXBContext.newInstance(SitesWrapper.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<IProjectExampleSite> parse(String xml) throws CoreException {
		if (xml == null || xml.trim().isEmpty()) {
			return null;
		}
		try {
			SitesWrapper siteList = (SitesWrapper) unmarshall(jaxbContext, xml);
			return siteList.sites == null? Collections.<IProjectExampleSite>emptySet():siteList.sites;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to parse user example sites", e));
		}
	}
	
	public String serialize(Set<IProjectExampleSite> sites) throws CoreException {
		try {
			SitesWrapper siteList = new SitesWrapper();
			siteList.sites = sites;
			
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			StringWriter writer = new StringWriter();
			marshaller.marshal(siteList, writer);
			return writer.toString();
		} catch (Exception shouldntHappen) {
			throw new CoreException(new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to serialize example sites ", shouldntHappen));
		}
	}
	
	@XmlRootElement(name = "sites")
	@XmlAccessorType (XmlAccessType.FIELD)
	static class SitesWrapper {
		@XmlElement(name = "site", type=ProjectExampleSite.class)
		Set<IProjectExampleSite> sites;
	}
}
