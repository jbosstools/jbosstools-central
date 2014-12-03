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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

abstract class JaxbParser {

	Object unmarshall(JAXBContext jaxbContext, String xml) throws JAXBException, IOException, XMLStreamException {
		return unmarshall(jaxbContext, new StringReader(xml));
	}
	
	Object unmarshall(JAXBContext jaxbContext, File file) throws JAXBException, IOException, XMLStreamException {
		return unmarshall(jaxbContext, new FileReader(file));
	}

	Object unmarshall(JAXBContext jaxbContext, Reader reader) throws JAXBException, IOException, XMLStreamException {
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		try (Reader r = reader){
			XMLStreamReader xmler = xmlif.createXMLStreamReader(r);
			return jaxbUnmarshaller.unmarshal(xmler);
		}
	}
}
