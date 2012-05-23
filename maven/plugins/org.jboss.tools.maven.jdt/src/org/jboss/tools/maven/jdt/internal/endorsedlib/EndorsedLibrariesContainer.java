/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt.internal.endorsedlib;

import java.io.Serializable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Endorsed libraries classpath container
 */
public class EndorsedLibrariesContainer implements IClasspathContainer, Serializable {
  private static final long serialVersionUID = 1L;
  
  private final IClasspathEntry[] entries;
  private final IPath path;

  public EndorsedLibrariesContainer(IPath path, IClasspathEntry[] entries) {
    this.path = path;
    this.entries = entries;
  }
  
  public String getDescription() {
    return "Endorsed Libraries"; 
  }
  
  public int getKind() {
    return IClasspathContainer.K_APPLICATION;
  }
  
  public synchronized IClasspathEntry[] getClasspathEntries() {
    return entries;
  }

  public IPath getPath() {
    return path; 
  }
  
  @Override
	public String toString() {
	  StringBuilder sb = new StringBuilder();
	  if (path != null){
		  sb.append(path.toPortableString());
	  }
	  if (entries != null) {
		  sb.append(" [");
		  for (IClasspathEntry cpe : entries) {
			  sb.append(cpe.getPath()).append(", ");
		  }
		  sb.append("]");
	  }
	  return sb.toString();
	}
  
}
