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
package org.jboss.tools.maven.ui.internal.problems;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.performOnDOMDocument;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jface.text.IDocument;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.Operation;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.OperationTuple;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.w3c.dom.Document;

public class JBossRepositoriesMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	public String getDescription() {
		return getLabel();
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return "Add missing JBoss repositories to settings.xml";
	}

	public void run(IMarker arg0) {
		Operation addRepos = new AddRepositoriesOperation();
		File settings = new File(MavenPlugin.getMavenConfiguration().getUserSettingsFile());
		try {
			String content = new String(Util.getFileCharContent(settings, null));
			IDocument settingsDoc = new org.eclipse.jface.text.Document(content);
			performOnDOMDocument(new OperationTuple(settingsDoc, addRepos));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	class AddRepositoriesOperation implements Operation {

		public void process(Document document) {
			document.createComment("Changing settings at "+new Date());
			System.err.println("Resolving repository stuff");	
		}
		
	}
}
