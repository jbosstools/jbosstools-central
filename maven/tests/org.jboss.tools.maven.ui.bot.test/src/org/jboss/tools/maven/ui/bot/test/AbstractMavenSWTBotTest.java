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
package org.jboss.tools.maven.ui.bot.test;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.m2e.tests.common.JobHelpers;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.tools.test.util.ResourcesUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractMavenSWTBotTest extends AbstractMavenProjectTestCase {

	public static final String PACKAGE_EXPLORER = "Package Explorer"; //$NON-NLS-1$

	protected static SWTWorkbenchBot bot;
	
	@BeforeClass 
	public static void beforeClass() throws Exception {
		bot = initSWTBot();
		WorkbenchPlugin.getDefault().getPreferenceStore()
		.setValue(IPreferenceConstants.RUN_IN_BACKGROUND, true);

		PrefUtil.getAPIPreferenceStore().setValue(
		IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);

	}
	
	public static SWTWorkbenchBot initSWTBot() throws CoreException {
		SWTWorkbenchBot swtbot = new SWTWorkbenchBot();
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 1000;
		SWTBotPreferences.PLAYBACK_DELAY = 5;
		waitForIdle();
		try {
			SWTBotView view = swtbot.viewByTitle("Welcome");
			if (view != null) {
				view.close();
			}
		} catch (WidgetNotFoundException ignore) {
		}

		SWTBotShell[] shells = swtbot.shells();
		for (SWTBotShell shell : shells) {
			final Shell widget = shell.widget;
			Object parent = UIThreadRunnable.syncExec(shell.display,
					new Result<Object>() {
						public Object run() {
							return widget.isDisposed() ? null : widget.getParent();
						}
					});
			if (parent == null) {
				continue;
			}
			shell.close();
		}

		List<? extends SWTBotEditor> editors = swtbot.editors();
		for (SWTBotEditor e : editors) {
			e.close();
		}
		
		return swtbot;
	}
	
	@Before
    public void setUp() throws Exception {
        activateSchell();
        super.setUp();
    }

	public static void activateSchell() {
		UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
            	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
                        .forceActive();
            }
        });
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@AfterClass
	public final static void cleanUp() throws Exception {
		boolean buildAutomatically = ResourcesUtils.setBuildAutomatically(false);
		ValidationFramework.getDefault().suspendAllValidation(true);
		try {
			executeAfterClass();
			WorkspaceHelpers.cleanWorkspace();
		} finally {
			ResourcesUtils.setBuildAutomatically(buildAutomatically);
			ValidationFramework.getDefault().suspendAllValidation(false);
		}
		waitForIdle();
	}
	
	protected static void executeAfterClass()  throws Exception {
	}


	public static void waitForIdle() {
		JobHelpers.waitForLaunchesToComplete(30*1000);
		JobHelpers.waitForJobsToComplete();
	}
	
}
