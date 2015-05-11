package org.jboss.tools.project.examples;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "favoriteItem")
@XmlAccessorType (XmlAccessType.FIELD)
public class FavoriteItem implements Comparable<FavoriteItem>{
	
	private static long COUNT_VS_TIME_WEIGHT = 3600*24*30*1000;
	
	private String id;
	private int count;
	private long lastTimeUsed;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public long getLastTimeUsed() {
		return lastTimeUsed;
	}
	public void setLastTimeUsed(long lastTimeUsed) {
		this.lastTimeUsed = lastTimeUsed;
	}
	
	@Override
	public int compareTo(FavoriteItem o) {
		return Long.compare(getWeight(), o.getWeight());
	}
	
	private long getWeight() {
		return lastTimeUsed + count*COUNT_VS_TIME_WEIGHT;
	}
	
}
