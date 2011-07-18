/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.maven.ui.internal.profiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Profile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.jboss.tools.maven.ui.Activator;


/**
 * Maven profile resource node
 * 
 * @author Fred Bricon
 */
public class ActiveMavenProfilesNode implements IWorkbenchAdapter {

  private Object[] ids = null;
  
  public static ImageDescriptor icon = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/maven-profiles.png");

  
  public ActiveMavenProfilesNode(List<Profile> profiles) {
    ids = getProfileIds(profiles);
  }

  public Object[] getResources() {
	  return ids;
  }
  
  private Object[] getProfileIds(List<Profile> profiles) {
	    if(profiles != null && !profiles.isEmpty()) {
	    	List<String> idList = new ArrayList<String>(profiles.size()); 
			for (Profile p : profiles) {
				idList.add(p.getId());
			}
			return idList.toArray();
	    }
	    return null;
  }

  public String getLabel(Object o) {
	return "Active Maven profiles";
  }

  public ImageDescriptor getImageDescriptor(Object object) {
    return icon;
  }

  public Object getParent(Object o) {
    return null;
  }

  public Object[] getChildren(Object o) {
    return ids;
  }

}
