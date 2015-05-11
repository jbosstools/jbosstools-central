package org.jboss.tools.project.examples.internal;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.project.examples.FavoriteItem;

public interface IFavoriteSerializer {

	Collection<FavoriteItem> deSerialize() throws CoreException;
	
	void serialize(Collection<FavoriteItem> collection) throws CoreException;
}
