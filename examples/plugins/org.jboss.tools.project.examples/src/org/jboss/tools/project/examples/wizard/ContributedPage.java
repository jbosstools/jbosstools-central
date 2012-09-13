package org.jboss.tools.project.examples.wizard;

import org.eclipse.core.runtime.IConfigurationElement;

public class ContributedPage implements Comparable<ContributedPage> {

	private String exampleType;
	private int priority;
	private IConfigurationElement configurationElement;
	private String clazz;
	private String pageType;

	public ContributedPage(IConfigurationElement configurationElement,
			String type, String pageType, int priority, String clazz) {
		super();
		this.configurationElement = configurationElement;
		this.exampleType = type;
		this.setPageType(pageType);
		this.priority = priority;
		this.clazz = clazz;
	}

	public String getType() {
		return exampleType;
	}

	public void setType(String type) {
		this.exampleType = type;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	public void setConfigurationElement(
			IConfigurationElement configurationElement) {
		this.configurationElement = configurationElement;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	@Override
	public int compareTo(ContributedPage o) {
		if (o == null)
			return 1;
		int other = o.getPriority();
		if (other < this.priority)
			return 1;
		else if (other > this.priority)
			return -1;
		return 0;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

}
