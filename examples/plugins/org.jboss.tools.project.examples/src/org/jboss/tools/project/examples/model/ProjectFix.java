package org.jboss.tools.project.examples.model;

import java.util.HashMap;
import java.util.Map;

public class ProjectFix {

	public final static String WTP_RUNTIME = "wtpruntime"; //$NON-NLS-1$
	public final static String SEAM_RUNTIME = "seam"; //$NON-NLS-1$
	public final static String DROOLS_RUNTIME = "drools"; //$NON-NLS-1$
	public final static String PLUGIN_TYPE = "plugin"; //$NON-NLS-1$
	public final static String ALLOWED_VERSIONS = "allowed-versions"; //$NON-NLS-1$
	public final static String ECLIPSE_PROJECTS = "eclipse-projects"; //$NON-NLS-1$
	public final static String ALLOWED_TYPES = "allowed-types"; //$NON-NLS-1$
	public final static String ID = "id"; //$NON-NLS-1$
	public final static String VERSION = "VERSION"; //$NON-NLS-1$
	public final static String DESCRIPTION = "description"; //$NON-NLS-1$
	public final static String SHORT_DESCRIPTION = "short-description"; //$NON-NLS-1$
	public final static String ANY = "any"; //$NON-NLS-1$
	
	private String type;
	private Map<String,String> properties = new HashMap<String,String>();
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
}
