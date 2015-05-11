package org.jboss.tools.project.examples.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.FavoriteItem;
import org.jboss.tools.project.examples.IFavoriteExampleManager;

public class FavoriteExampleManager implements IFavoriteExampleManager {

	private IFavoriteSerializer favoriteSerializer;

	public FavoriteExampleManager() {
		this(null);
	}
	
	public FavoriteExampleManager(IFavoriteSerializer favoriteSerializer) {
		if (favoriteSerializer == null) {
			File baseDir = ProjectExamplesActivator.getDefault().getStateLocation().toFile();
			File xmlFile = new File(baseDir, "favorites.xml");
			favoriteSerializer = new FavoriteItemXmlSerializer(xmlFile);
		}
		this.favoriteSerializer = favoriteSerializer;
	}
	
	@Override
	public void favorite(String itemId, IProgressMonitor monitor) throws CoreException {
		Map<String, FavoriteItem> favoritesMap =  getFavoriteItems(monitor);
		FavoriteItem item = getFavoriteItem(itemId, favoritesMap, monitor);
		item.setCount(item.getCount()+1);
		item.setLastTimeUsed(System.currentTimeMillis());
		favoritesMap.put(itemId, item);
		favoriteSerializer.serialize(favoritesMap.values());
	}

	@Override
	public List<FavoriteItem> getFavoriteItems(int maxFavorites, IProgressMonitor monitor) throws CoreException {
		Map<String, FavoriteItem> favoriteItemsMap = getFavoriteItems(monitor);
		List<FavoriteItem> favorites = new ArrayList<>(favoriteItemsMap.values());
		Collections.sort(favorites);
		if (maxFavorites >= favorites.size()) {
			return favorites;
		}
		return favorites.subList(0, maxFavorites);
	}

	protected FavoriteItem getFavoriteItem(String id, Map<String, FavoriteItem> favoritesMap, IProgressMonitor monitor) {
		FavoriteItem item = favoritesMap.get(id);
		if (item == null) {
			item = new FavoriteItem();
			item.setId(id);
		}
		return item;
	}
	
	Map<String, FavoriteItem> getFavoriteItems(IProgressMonitor monitor) throws CoreException {
		Collection<FavoriteItem> favorites = favoriteSerializer.deSerialize();
		Map<String, FavoriteItem> favoritesMap = new HashMap<>(favorites.size());
		for (FavoriteItem fi : favorites) {
			favoritesMap.put(fi.getId(), fi);
		}
		return favoritesMap;
	}
}
