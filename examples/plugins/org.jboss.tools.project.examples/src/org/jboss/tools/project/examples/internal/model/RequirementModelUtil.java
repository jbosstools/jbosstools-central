/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.tools.project.examples.model.RequirementModel;

@SuppressWarnings("nls")
public class RequirementModelUtil {
	
	private RequirementModelUtil(){}
	
	public static Collection<RequirementModel> getAsRequirements(Collection<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return Collections.emptyList();
		}
		Map<String, RequirementModel> reqMap = new LinkedHashMap<>(tags.size());
		for (String tag : tags) {
			inferRequirement(tag.toLowerCase(), reqMap);
		}
		return reqMap.values();
	}

	//TODO Find a more dynamic way to infer all this stuff
	private static void inferRequirement(String tag, Map<String, RequirementModel> reqMap) {
		if (tag ==null) { 
			return;
		}
		String key = null;
		RequirementModel req = null;
		if (tag.contains("hibernate") || tag.contains("jpa")) {
			key = "hibernate";
			if (!reqMap.containsKey(key)) {
				req = createHibernateRequirement();
			}
		} else if ("cdi".equals(tag)) {
			key = "cdi";
			if (!reqMap.containsKey(key)) {
				req = createMavenCDIRequirement();
			}
		} else if (tag.contains("cordova")) {
			key = "cordova";
			if (!reqMap.containsKey(key)) {
				req = createThymRequirement();
			}
		} else if (tag.contains("angular")) {
			key = "angular";
			if (!reqMap.containsKey(key)) {
				req = createAngularJsRequirement();
			}
		} else if (tag.contains("drools") || tag.contains("brms")) {
			key = "drools";
			if (!reqMap.containsKey(key)) {		
				req = createBrmsRequirement();
			}
		} else if (tag.startsWith("product:eap")
				|| tag.startsWith("product:wfk")
				|| tag.equals("jsf") || tag.contains("picketlink") 
				|| tag.contains("jaxrs") || tag.contains("jax-rs")) {
			key = "server";
			if (!reqMap.containsKey(key)) {
				String serverId = null;
				if (tag.startsWith("product:eap")) {
					if (tag.contains("-6.4")) {
						serverId = "jbosseap640runtime";
					} else if  (tag.contains("-6.3")) {
						serverId = "jbosseap630runtime";
			        } else if  (tag.contains("-7")) {
			          serverId = "jbosseap700runtime";
			        } 
				} else if (tag.startsWith("wfk")) {
					if (tag.contains("-2.7")) {
						serverId = "jbosseap640runtime";
					} else if  (tag.contains("-2.6")) {
						serverId = "jbosseap630runtime";
					} 
				}
				
				req = createServerRuntimeRequirement(serverId);
			}
		} else if (tag.contains("fuse")) {
			key = "fuse";
			if (!reqMap.containsKey(key)) {
				req = createFuseRequirement();	
			}
		} else if (tag.startsWith("spring")) {
			key = "spring";
			if (!reqMap.containsKey(key)) {	
				req = createSpringRequirement();
			}
		}
		if (key != null && req != null) {
			reqMap.put(key, req);
		}
	}

	public static RequirementModel createMavenCDIRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.jboss.tools.maven.cdi");
		properties.put("versions", "1.0.0");
		properties.put("description", "This example works best with the Maven CDI Configurator");
		properties.put("connectorIds", "org.jboss.tools.maven.cdi.feature");
		req.setProperties(properties);
		return req;
	}
	
	public static RequirementModel createSpringRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.springframework.ide.eclipse");
		properties.put("versions", "3.5.0");
		properties.put("description", "This example works best with Spring IDE");
		properties.put("connectorIds", "org.springframework.ide.eclipse.feature");
		req.setProperties(properties);
		return req;
	}

	public static RequirementModel createServerRuntimeRequirement(String serverId) {
	    RequirementModel req = createServerRequirement();
		Map<String, String> properties = new HashMap<>();
		List<String> supportedServerIds = new ArrayList<>();
		if (serverId == null || !serverId.contains("eap7")) {
		  properties.put("description", "Requires JBoss EAP 6.2+ or WildFly");
		  supportedServerIds.add("org.jboss.ide.eclipse.as.runtime.eap.61");
		} else {
		  properties.put("description", "Requires JBoss EAP 7+ or WildFly");
		}
		supportedServerIds.add("org.jboss.ide.eclipse.as.runtime.eap.70");
		supportedServerIds.add("org.jboss.ide.eclipse.as.runtime.wildfly.80");
		supportedServerIds.add("org.jboss.ide.eclipse.as.runtime.wildfly.90");
		supportedServerIds.add("org.jboss.ide.eclipse.as.runtime.wildfly.100");
		String allowedTypes = StringUtils.join(supportedServerIds, ",");
		
		properties.put("allowed-types", allowedTypes);
		properties.put("downloadId", serverId == null?"wildfly-1000finalruntime":serverId);
		req.setProperties(properties);
		return req;
	}


	public static RequirementModel createAngularJsRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.eclipse.angularjs.core");
		properties.put("versions", "0.8.0");
		properties.put("description", "This example works best with AngularJS Eclipse Plugin");
		properties.put("connectorIds", "angularjs-eclipse-feature");
		req.setProperties(properties);
		return req;
	}
	
	public static RequirementModel createThymRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.eclipse.thym.core");
		properties.put("versions", "0.1.0");
		properties.put("description", "This example works best with Eclipse Hybrid Mobile Tools");
		properties.put("connectorIds", "org.jboss.tools.aerogear.hybrid");
		req.setProperties(properties);
		return req;
	}
	
	private static RequirementModel createPluginRequirement() {
		return new RequirementModel("plugin");
	}
	
	private static RequirementModel createServerRequirement() {
		return new RequirementModel("wtpruntime");
	}
	
    //integration stack connectors
	public static RequirementModel createFuseRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.fusesource.ide.tooling");
		properties.put("versions", "1.0.0");
		properties.put("description", "This example works best with JBoss Fuse Development Tools");
		properties.put("connectorIds", "org.fusesource.ide");
		req.setProperties(properties);
		return req;
	}

	public static RequirementModel createBrmsRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.jbpm.eclipse");
		properties.put("versions", "6.0.0");
		properties.put("description", "This example works best with JBoss Business Process and Rules Development Tools");
		properties.put("connectorIds", "jboss.integration-stack.bundle.bpr");
		req.setProperties(properties);
		return req;
	}
	
	public static RequirementModel createHibernateRequirement() {
		RequirementModel req = createPluginRequirement();
		Map<String, String> properties = new HashMap<>();
		properties.put("id", "org.jboss.tools.maven.hibernate");
		properties.put("versions", "1.0.0");
		properties.put("description", "This example works best with the Maven Hibernate Configurator");
		properties.put("connectorIds", "org.jboss.tools.maven.hibernate.feature");
		req.setProperties(properties);
		return req;
	}
}
