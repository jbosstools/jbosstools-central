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
package org.jboss.tools.maven.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author snjeza
 *
 */
public class JBossSourceContainerType extends AbstractSourceContainerTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		JBossSourceContainer jbossSourceContainer = (JBossSourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement("jbossas"); //$NON-NLS-1$
		element.setAttribute("homepath", jbossSourceContainer.getHomePath()); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("jbossas".equals(element.getNodeName())) { //$NON-NLS-1$
				String homePath = element.getAttribute("homepath"); //$NON-NLS-1$
				if (homePath == null || homePath.length() == 0) {
					abort("Exception occurred during source lookup", null); 
				}
				return new JBossSourceContainer(homePath);
			} 
			abort("Unable to restore source lookup path - expecting typeId attribute.", null); 
		}
		abort("Unable to restore source lookup path - unknown type source container type specified: {0}", null); 
		return null;
	}
}
