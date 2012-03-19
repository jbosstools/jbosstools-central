/*******************************************************************************
 * Copyright (c) 2005 Oracle Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ian Trimble - initial API and implementation
 *******************************************************************************/ 
package org.jboss.tools.maven.jsf.utils.xpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jst.j2ee.common.ParamValue;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.model.IModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.ContextParam;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.jboss.tools.maven.jsf.MavenJSFActivator;

/**
 * Class copied from {@link org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigUtils}. 
 * <ul>
 * <li>getConfigFilesFromContextParam() method modified to allow reading of javax.faces.CONFIG_FILES 
 * value in web.xml on projects not having the JSF facet already</li>
 * <li>Unused methods have been removed</li>
 * <li>Code changed to use Java 1.5+ features</li>
 * </ul>
 * 
 * JSFAppConfigUtils provides utility methods useful in processing of a JSF
 * application configuration.
 * 
 * 
 * @author Ian Trimble - Oracle
 */
public class JSFAppConfigUtils {

	/**
	 * Name of JSF CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES").
	 */
	public static final String CONFIG_FILES_CONTEXT_PARAM_NAME = "javax.faces.CONFIG_FILES"; //$NON-NLS-1$

	/**
	 * Location in JAR file of application configuration resource file
	 * ("META-INF/faces-config.xml"). 
	 */
	public static final String FACES_CONFIG_IN_JAR_PATH = "META-INF/faces-config.xml"; //$NON-NLS-1$

    
	/**
	 * Gets list of application configuration file names as listed in the JSF
	 * CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES"). Will return
	 * an empty list if WebArtifactEdit is null, if WebApp is null, if context
	 * parameter does not exist, or if trimmed context parameter's value is
	 * an empty String.
	 * 
	 * @param project IProject instance for which to get the context
	 * parameter's value.
	 * @return List of application configuration file names as listed in the
	 * JSF CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES"); list
	 * may be empty.
	 */
	public static List<String> getConfigFilesFromContextParam(IProject project) {
		List<String> filesList = Collections.emptyList();
		if (ModuleCoreNature.isFlexibleProject(project)) {
			try {
				IModelProvider provider = ModelProviderManager.getModelProvider(project);
				if (provider != null) {
					Object webAppObj = provider.getModelObject();
					if (webAppObj != null){
						if (webAppObj instanceof WebApp)
							filesList = getConfigFilesForJ2EEApp(project);
						else if (webAppObj instanceof org.eclipse.jst.javaee.web.WebApp)
							filesList = getConfigFilesForJEEApp((org.eclipse.jst.javaee.web.WebApp)webAppObj);
					}
				}
			} catch (Exception e) {
				//Fix for JBIDE-11078 : in extremely rare cases, 
				//a NPE can be thrown if no IModelProvider is found for project
				//At this point that error shouldn't block the user so we just log it
				MavenJSFActivator.log("Could not read web.xml", e); //$NON-NLS-1$
			}
		}
		return filesList;
	}

	private static List<String> getConfigFilesForJEEApp(org.eclipse.jst.javaee.web.WebApp webApp) {
		String filesString = null;
		for (org.eclipse.jst.javaee.core.ParamValue paramValue : webApp.getContextParams()) {
			if (paramValue.getParamName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
				filesString = paramValue.getParamValue();
				break;
			}
		}
		return parseFilesString(filesString);	
	}

	private static List<String> getConfigFilesForJ2EEApp(IProject project){
		List<String> filesList = new ArrayList<String>();
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(project);
		if (webArtifactEdit != null) {
			try {
				WebApp webApp = null;
				try {
					webApp = webArtifactEdit.getWebApp();
				} catch(ClassCastException cce) {
					//occasionally thrown from WTP code in RC3 and possibly later
					MavenJSFActivator.log(cce);
					return filesList;
				}
				if (webApp != null) {
					String filesString = null;
					//need to branch here due to model version differences (BugZilla #119442)
					if (webApp.getVersionID() == J2EEVersionConstants.WEB_2_3_ID) {
						EList contexts = webApp.getContexts();
						Iterator itContexts = contexts.iterator();
						while (itContexts.hasNext()) {
							ContextParam contextParam = (ContextParam)itContexts.next();
							if (contextParam.getParamName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
								filesString = contextParam.getParamValue();
								break;
							}
						}
					} else {
						EList contextParams = webApp.getContextParams();
						Iterator itContextParams = contextParams.iterator();
						while (itContextParams.hasNext()) {
							ParamValue paramValue = (ParamValue)itContextParams.next();
							if (paramValue.getName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
								filesString = paramValue.getValue();
								break;
							}
						}
					}					
					filesList = parseFilesString(filesString);				
				}
			} finally {
				webArtifactEdit.dispose();
			}
		}

		return filesList;
	}
	
	private static List<String> parseFilesString(String filesString) {
		List<String> filesList = new ArrayList<String>();
		if (filesString != null && filesString.trim().length() > 0) {			
			StringTokenizer stFilesString = new StringTokenizer(filesString, ","); //$NON-NLS-1$
			while (stFilesString.hasMoreTokens()) {
				String configFile = stFilesString.nextToken().trim();
				filesList.add(configFile);
			}
		}
		return filesList;
	}

}
