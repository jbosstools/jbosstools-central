/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 * @author snjeza
 *
 */
public class JBossCentralEditorInput implements IEditorInput {

	public static final JBossCentralEditorInput INSTANCE = new JBossCentralEditorInput();
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return JBossCentralEditor.RED_HAT_CENTRAL;
	}


	@Override
	public String getToolTipText() {
		return JBossCentralEditor.RED_HAT_CENTRAL;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

}
