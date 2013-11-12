/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.jboss.tools.central.internal.dnd.JBossCentralDropTarget;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * 
 * @author snjeza
 *
 */
public class ShowJBossCentral implements IStartup {

	private static final String EDITOR_AREA_ID = "org.eclipse.ui.editorss";
	private static final String ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW = "org.jboss.tools.central.donotshow"; //$NON-NLS-1$
	private static final String ORG_JBOSS_TOOLS_USAGE = "org.jboss.tools.usage"; //$NON-NLS-1$

	@Override
	public void earlyStartup() {
		registerDropTarget(2000);
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				registerDropTarget(0);
			}
			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {}
			
			@Override
			public void windowClosed(IWorkbenchWindow window) {}
			
			@Override
			public void windowActivated(IWorkbenchWindow window) {}
		});
		boolean doNotShow = Boolean.getBoolean(ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW);
		if (doNotShow) {
			return;
		}
		boolean showJBossCentral = JBossCentralActivator.getDefault()
				.showJBossCentralOnStartup();
		IEclipsePreferences prefs = JBossCentralActivator.getDefault()
				.getPreferences();
		Bundle usage = Platform.getBundle(ORG_JBOSS_TOOLS_USAGE);
		Bundle central = Platform.getBundle(JBossCentralActivator.PLUGIN_ID);
		if (!showJBossCentral) {
			if (usage != null) {
				Version version = usage.getVersion();
				String versionString = version.toString();
				String savedVersion = prefs.get(ORG_JBOSS_TOOLS_USAGE, "");
				if (!savedVersion.equals(versionString)) {
					showJBossCentral = true;
				} else {
					if (central != null) {
						version = central.getVersion();
						versionString = version.toString();
						savedVersion = prefs.get(
								JBossCentralActivator.PLUGIN_ID, "");
						if (!savedVersion.equals(versionString)) {
							showJBossCentral = true;
						}
					}
				}
			}
		}
		saveVersion(prefs, usage, ORG_JBOSS_TOOLS_USAGE);
		saveVersion(prefs, central, JBossCentralActivator.PLUGIN_ID);
		
		if (!showJBossCentral) {
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				JBossCentralActivator.getJBossCentralEditor(false);
			}
		});
	}

	private void registerDropTarget(int delay) {
		UIJob registerJob = new UIJob(Display.getDefault(), "JBoss Central DND initialization") {
			{
				setPriority(Job.DECORATE);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (IWorkbenchWindow window : workbenchWindows) {
					Shell shell = window.getShell();
					JBossCentralActivator.initDropTarget(shell);
					addJBossDropListener(window);
				}
				return Status.OK_STATUS;
			}

			private void addJBossDropListener(IWorkbenchWindow window) {
				if ( !(window instanceof WorkbenchWindow) ) {
					return;
				}
				WorkbenchWindow workbenchWindow = (WorkbenchWindow) window;
				EModelService modelService = (EModelService) window.getService(EModelService.class);
				if (modelService == null) {
					return;
				}
				List<MArea> areas = modelService.findElements(workbenchWindow.getModel(),EDITOR_AREA_ID, MArea.class, null);
				if (areas == null || areas.size() <= 0) {
					return;
				}
				for (MArea area : areas) {
					Object object = area.getWidget();
					if (object instanceof Composite) {
						Composite composite = (Composite) object;
						Object o = composite.getData(DND.DROP_TARGET_KEY);
						if (o instanceof DropTarget) {
							new JBossCentralDropTarget((DropTarget) o);
						}
					}
				}
			}

		};
		registerJob.schedule(delay);
	}

	protected void saveVersion(IEclipsePreferences prefs, Bundle bundle, String preference) {
		if (bundle == null || prefs == null || preference == null) {
			return;
		}
		Version version = bundle.getVersion();
		String versionString = version.toString();
		String savedVersion = prefs.get(preference, "");
		if (!savedVersion.equals(versionString)) {
			prefs.put(preference, versionString);
		}
	}

}
