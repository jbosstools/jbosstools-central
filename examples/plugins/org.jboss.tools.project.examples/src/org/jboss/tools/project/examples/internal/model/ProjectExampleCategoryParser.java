/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.model;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;

@SuppressWarnings("nls")
public class ProjectExampleCategoryParser extends JaxbParser{

	private static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(CategoriesWrapper.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<ProjectExampleCategory> parse(File file) throws CoreException {
		try {
			CategoriesWrapper categoryList = (CategoriesWrapper) unmarshall(jaxbContext, file);
			return categoryList.categories;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to parse categories in "+file.getPath(), e));
		}
	}
	
	@XmlRootElement(name = "categories")
	@XmlAccessorType (XmlAccessType.FIELD)
	static class CategoriesWrapper {

		@XmlElement(name = "category")
		List<ProjectExampleCategory> categories;
	}
}
