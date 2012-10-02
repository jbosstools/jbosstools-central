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
package org.jboss.tools.maven.conversion.ui.handlers;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.maven.conversion.ui.dialog.ConvertToMavenDependencyWizard;

/**
 * Materialize Library Handler.
 * 
 * @author Fred Bricon
 */

@SuppressWarnings("restriction")
public class ConvertToMavenDependencyHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {

	final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

	ISelection selection = HandlerUtil.getCurrentSelection(event);
    
    Set<IClasspathEntry> entries = getSelectedClasspathEntries(selection);
    
    if (entries == null || entries.isEmpty()) {
    	MessageDialogWithToggle.openInformation(window.getShell(), "Convert to Maven Dependency", "Nothing to convert");
    	return null;
    }
    
    ConvertToMavenDependencyWizard wizard = new ConvertToMavenDependencyWizard( 
                                                                   null, //project,
                                                                   null 
                                                                   ); 

    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
	dialog.open();
    return null;
  }


  private Set<IClasspathEntry> getSelectedClasspathEntries(ISelection selection) {
    Set<IClasspathEntry> files = null;
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      files = new LinkedHashSet<IClasspathEntry>(structuredSelection.size());
      Iterator<?> ite = structuredSelection.iterator();
      while(ite.hasNext()) {
    	  Object o = ite.next();
    	  if (o instanceof IPackageFragmentRoot) {
    		  ((IPackageFragmentRoot) o).getPath();
    		  addClasspathEntry((IPackageFragmentRoot)o, files);
    	  }
    	  else if (o instanceof ClassPathContainer) {
    		  ClassPathContainer container = (ClassPathContainer) o;
    		  if (isValid(container)) {
    			  for (IPackageFragmentRoot pfr : container.getPackageFragmentRoots()) {
    				  addClasspathEntry(pfr, files);
    			  }
    		  }
    	  }
      }
    }
    return files;
  }
  
  
  private boolean isValid(ClassPathContainer container) {
	return !MavenClasspathHelpers.isMaven2ClasspathContainer(container.getClasspathEntry().getPath());
  }

  private void addClasspathEntry(IPackageFragmentRoot pfr, Collection<IClasspathEntry> entries) {
	  if (pfr.isArchive()) {
		  pfr.getResource();
		try {
			IClasspathEntry cpe = pfr.getResolvedClasspathEntry();
			if (cpe != null && cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				entries.add(cpe);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	  }
  }
  
}