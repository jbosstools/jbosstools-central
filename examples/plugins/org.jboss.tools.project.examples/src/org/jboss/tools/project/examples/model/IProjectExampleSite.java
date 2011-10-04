package org.jboss.tools.project.examples.model;

import java.net.URL;

public interface IProjectExampleSite {
	public String getName();

	public void setEditable(boolean editable);

	public boolean isEditable();

	public void setExperimental(boolean experimental);

	public boolean isExperimental();

	public void setName(String name);

	public void setUrl(URL url);

	public URL getUrl();
}
