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
package org.jboss.tools.maven.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author snjeza
 *
 */
public class JBossSourceContainerType extends AbstractSourceContainerTypeDelegate {

	private static final String RUNTIME_ID_ATTRIBUTE = "runtimeId"; //$NON-NLS-1$
	private static final String HOMEPATH_ATTRIBUTE = "homepath"; //$NON-NLS-1$
	private static final String JBOSSAS_ELEMENT = "jbossas"; //$NON-NLS-1$
	private static final String RUNTIME_TYPE_ID_ATTRIBUTE = "runtimeTypeId"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		JBossSourceContainer jbossSourceContainer = (JBossSourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement(JBOSSAS_ELEMENT);
		if (jbossSourceContainer.getHomePath() != null && !jbossSourceContainer.getHomePath().isEmpty()) {
			element.setAttribute(HOMEPATH_ATTRIBUTE, jbossSourceContainer.getHomePath()); 
		}
		if (jbossSourceContainer.getRuntime() != null) {
			element.setAttribute(RUNTIME_ID_ATTRIBUTE, jbossSourceContainer.getRuntime().getId());
			element.setAttribute(RUNTIME_TYPE_ID_ATTRIBUTE, jbossSourceContainer.getRuntime().getRuntimeType().getId());
		}
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
			if (JBOSSAS_ELEMENT.equals(element.getNodeName())) {
				String homePath = element.getAttribute(HOMEPATH_ATTRIBUTE); 
				if (homePath == null || homePath.length() == 0) {
					String runtimeId = element.getAttribute(RUNTIME_ID_ATTRIBUTE);
					String runtimeTypeId = element.getAttribute(RUNTIME_TYPE_ID_ATTRIBUTE);
					IRuntime[] runtimes = ServerCore.getRuntimes();
					for (IRuntime runtime:runtimes) {
						if (runtimeId != null && runtimeId.equals(runtime.getId()) &&
								runtimeTypeId != null && runtimeTypeId.equals(runtime.getRuntimeType().getId())	) {
							return new JBossSourceContainer(runtime);
						}
					}
				} else {
					return new JBossSourceContainer(homePath);
				}
			} 
			abort("Unable to restore source lookup path - expecting typeId attribute.", null); 
		}
		abort("Unable to restore source lookup path - unknown type source container type specified: {0}", null); 
		return null;
	}
}
