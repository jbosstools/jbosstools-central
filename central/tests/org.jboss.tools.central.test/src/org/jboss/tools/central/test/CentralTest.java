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
package org.jboss.tools.central.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.ShowJBossCentral;
import org.jboss.tools.central.editors.JBossCentralEditor;
import org.jboss.tools.central.jobs.RefreshBlogsJob;
import org.jboss.tools.central.jobs.RefreshNewsJob;
import org.jboss.tools.central.jobs.RefreshTutorialsJob;
import org.jboss.tools.central.model.FeedsEntry;
import org.jboss.tools.central.model.TutorialCategory;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.xpl.EditorTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author snjeza
 * 
 */
public class CentralTest {

	private static JBossCentralEditor editor;

	@BeforeClass
	public static void init() throws Exception {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				editor = JBossCentralActivator.getJBossCentralEditor();
			}
		});
		
	}

	@AfterClass
	public static void close() throws Exception {
		EditorTestHelper.closeAllEditors();
	}

	@Test
	public void testEditorOpen() throws Exception {
		assertTrue("The JBoss Central editor isn't open",editor != null);
	}
	
	@Test
	public void testTutorials() throws Exception {
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		Map<String, TutorialCategory> categories = RefreshTutorialsJob.INSTANCE.getTutorialCategories();
		assertTrue("No one tutorial is found", categories.size() > 0);
	}
	
	@Test
	public void testNews() throws Exception {
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		List<FeedsEntry> news = RefreshNewsJob.INSTANCE.getEntries();
		assertTrue("No one news is found", news.size() > 0);
	}
	
	@Test
	public void testBlogs() throws Exception {
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		List<FeedsEntry> blogs = RefreshBlogsJob.INSTANCE.getEntries();
		assertTrue("No one blog is found", blogs.size() > 0);
	}
	
	@Test
	public void testCachingBlogs() throws Exception {
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		assertTrue("Blog entries aren't cached", RefreshBlogsJob.INSTANCE.getCacheFile().exists());
	}
	
	@Test
	public void testCachingNews() throws Exception {
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		assertTrue("News entries aren't cached", RefreshNewsJob.INSTANCE.getCacheFile().exists());
	}
	@Test
	public void testShowOnStartup() throws Exception {
		EditorTestHelper.closeAllEditors();
		assertTrue("The Show On Startup property isn't set by default", JBossCentralActivator.getDefault().showJBossCentralOnStartup());
		new ShowJBossCentral().earlyStartup();
		JobUtils.delay(1000);
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		
		assertTrue("The JBoss Central editor isn't open by default", hasOpenEditor());
		IEclipsePreferences prefs = JBossCentralActivator.getDefault()
				.getPreferences();
		prefs.putBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP,
				false);
		assertFalse("The Show On Startup property isn't changed", JBossCentralActivator.getDefault().showJBossCentralOnStartup());
		EditorTestHelper.closeAllEditors();
		new ShowJBossCentral().earlyStartup();
		Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		JobUtils.delay(1000);
		assertFalse("The JBoss Central editor is open when the Show On Startup property is unchecked", hasOpenEditor());
		prefs.putBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP,
				JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
	}

	private boolean hasOpenEditor() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				IEditorReference[] editorReferences= pages[j].getEditorReferences();
				if (editorReferences.length > 0) {
					return true;
				}
			}
		}
		return false;
	}

}
