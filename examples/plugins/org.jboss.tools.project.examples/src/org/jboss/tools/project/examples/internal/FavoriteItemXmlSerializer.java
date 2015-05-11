package org.jboss.tools.project.examples.internal;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;

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
import org.jboss.tools.project.examples.FavoriteItem;
import org.jboss.tools.project.examples.internal.model.JaxbParser;

public class FavoriteItemXmlSerializer extends JaxbParser implements IFavoriteSerializer {

	private File xmlFile;

	FavoriteItemXmlSerializer(File xmlFile) {
		this.xmlFile = xmlFile;
	}
	
	private static final JAXBContext jaxbContext; 
	
	static {
		try {
			jaxbContext = JAXBContext.newInstance(FavoriteItemsWrapper.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<FavoriteItem> deSerialize() throws CoreException {
		Collection<FavoriteItem> items = null;
		try {
			if (xmlFile.isFile()) {
				FavoriteItemsWrapper wrapper = (FavoriteItemsWrapper) unmarshall(jaxbContext, xmlFile);
				items = wrapper.items;
			}
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to parse user example sites", e));
		}
		return items == null?Collections.<FavoriteItem>emptyList() :items;
	}

	@Override
	public void serialize(Collection<FavoriteItem> collection) throws CoreException {
		try {
			FavoriteItemsWrapper wrapper = new FavoriteItemsWrapper();
			wrapper.items = collection;
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			xmlFile.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(xmlFile);
			marshaller.marshal(wrapper, writer);
		} catch (Exception shouldntHappen) {
			throw new CoreException(new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to serialize favorite items", shouldntHappen));
		}
	}

	
	@XmlRootElement(name = "favorites")
	@XmlAccessorType (XmlAccessType.FIELD)
	static class FavoriteItemsWrapper {
		@XmlElement(name = "favoriteItem", type=FavoriteItem.class)
		Collection<FavoriteItem> items;
	}
}
