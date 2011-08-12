/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.maven.jbosspackaging.configurators;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * ResourceCleaner
 *
 * @author Fred Bricon
 */
public class ResourceCleaner {
  
  private final IProject project;

  private Map<IFolder, Boolean> folders = new LinkedHashMap<IFolder, Boolean>(); 

  private Map<IFile, Boolean> files = new LinkedHashMap<IFile, Boolean>(); 
  
  private Set<IFolder> keepers = new HashSet<IFolder>();

  /**
   * @param project
   */
  public ResourceCleaner(IProject project) {
    this.project = project;
  }

  public ResourceCleaner(IProject project, IFolder ... foldersToKeep) {
    this.project = project;
    if (foldersToKeep != null) {
      for (IFolder folder : foldersToKeep) {
        if (folder != null) {
          keepers.add(folder);
          IContainer parent = folder.getParent();
          while (parent instanceof IFolder) {
            keepers.add((IFolder)parent);
            parent = parent.getParent();
          }
        }
      }
    }
  }

  
  public void addFolder(IPath folderPath, boolean deleteEmptyParents) {
    if (folderPath == null) {
      return;
    }
    addFolder(project.getFolder(folderPath), deleteEmptyParents);
  }

  
  public void addFolders(Collection<IPath> folderPaths) {
    if (folderPaths == null) {
      return;
    }
    for (IPath path : folderPaths) {
      addFolder(path, false);
    }
  }
  
  public void addFolder(IFolder folder, boolean deleteEmptyParents) {
    if (folder != null && !folder.exists()) {
      folders.put(folder, deleteEmptyParents);
      addInexistentParentFolders(folder);
    }
  }

  public void addFiles(IPath ... filePaths) {
    if (filePaths == null) {
      return;
    }
    for (IPath fileName : filePaths) {
      IFile fileToDelete = project.getFile(fileName);
      if (!fileToDelete.exists()) {
        files.put(fileToDelete, false);
        addInexistentParentFolders(fileToDelete);
      }
    }
  }

  public void cleanUp() throws CoreException {
    IProgressMonitor monitor = new NullProgressMonitor();
    for (IFile file : files.keySet()) {
      if (file.exists()) {
        file.delete(true, monitor);
      }
    }
    for (IFolder folder : folders.keySet()) {
      if (folder.exists() && folder.members().length == 0) {
        folder.delete(true, monitor);
      }
    }
  }
  
  protected void addInexistentParentFolders(IResource resource) {
    IContainer parent = resource.getParent();
    IFolder firstInexistentParent = null; 
    while (parent instanceof IFolder) {
      if (keepers.contains(parent) 
          || parent.exists()) {
        break;
      }
      firstInexistentParent = (IFolder)parent;
      parent = parent.getParent();
    }
    if (firstInexistentParent != null) {
      folders.put(firstInexistentParent, true);
    }
  }
  
}
