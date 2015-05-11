package org.jboss.tools.project.examples;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IFavoriteExampleManager {

	void favorite(String itemId, IProgressMonitor monitor) throws CoreException;
	
	List<FavoriteItem> getFavoriteItems(int max, IProgressMonitor monitor) throws CoreException;
	
}
