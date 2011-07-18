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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class ActiveProfilesLabelProvider implements ILabelProvider {
	
	//public static ImageDescriptor icon = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/maven-profiles.png");

	public Image image = null;
	
	public ActiveProfilesLabelProvider() {
		//image = icon.createImage();
	}
	
	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
		if (image != null) {
			image.dispose();
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Image getImage(Object element) {
		return image;
	}

	public String getText(Object element) {
	    if(element instanceof ActiveMavenProfilesNode) {
	        return ((ActiveMavenProfilesNode)element).getLabel(null);
	    } else if (element instanceof String) {
	    	return element.toString();
	    }
	    return null;
	}

}
