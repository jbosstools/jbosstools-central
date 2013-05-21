/*************************************************************************************
 * Copyright (c) 2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author snjeza
 *
 */
public class CheatSheetTest {

	private static final String WEB_APPLICATIONS = "Web Applications";
	private static final String KITCHENSINK = "jboss-as-kitchensink";

	@BeforeClass
	public static void init() throws Exception {
		importKitchensink();
	}

	@AfterClass
	public static void removeProjects() throws Exception {
		ProjectExamplesUtil.removeProjects();
	}
	
	private static void importKitchensink() throws Exception {
		WorkspaceJob workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					importProject(monitor);
				} catch (Exception e) {
					// ignore
				} 
				return Status.OK_STATUS;
			}
			
		};
		workspaceJob.setUser(true);
		workspaceJob.schedule();
		workspaceJob.join();
	}

	private static void importProject(IProgressMonitor monitor) throws MalformedURLException, Exception {
		List<ProjectExampleCategory> projects = ProjectExampleUtil.getProjects(monitor);
		ProjectExampleCategory quickstartsCategory = null;
		for (ProjectExampleCategory category: projects) {
			if (WEB_APPLICATIONS.equals(category.getName())) {
				quickstartsCategory = category;
				break;
			}
		}
		assertNotNull(quickstartsCategory);
		ProjectExample projectExample = null;
		for (ProjectExample project: quickstartsCategory.getProjects()) {
			if (KITCHENSINK.equals(project.getName())) {
				projectExample = project;
				break;
			}
		}
		assertNotNull(projectExample);
		ProjectExamplesActivator.downloadProject(projectExample, new NullProgressMonitor());
		assertNotNull(projectExample.getFile());
		IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(projectExample.getImportType());
		if (importProjectExample.importProject(projectExample, projectExample.getFile(), null, monitor)) {
			importProjectExample.fix(projectExample, monitor);
		}
	}
	
	@Test
	public void testAction() throws Exception {
		JobUtils.waitForIdle();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();    
		IProject project = root.getProject(KITCHENSINK);
		assertTrue("The jboss-as-kitchensink project is not imported.", project.exists());
		final IFile file = project.getFile("cheatsheet.xml");
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				testActionInNonUIThread(file);
			}
		});
		
	}

	public void testActionInNonUIThread(IFile file) {
		boolean delete = false;
		CheatSheetView view = null;
		try {
			if (!file.exists()) {
				createCheatSheet(file);
				delete = true;
			}
			view = openCheatSheet(file);
			testCommand();
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
			fail(e.getMessage());
		}
		finally {
			getActivePage().hideView(view);
			if (delete) {
				try {
					file.delete(true, null);
				} catch (CoreException e) {
					// ignore
				}
			}
		}
	}

	private void testCommand() throws Exception {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		
 		Command openFile = commandService.getCommand("org.jboss.tools.project.examples.cheatsheet.openFileInEditor");
 		IParameter path = openFile.getParameter("path");
 
 		Parameterization parm = new Parameterization(path, "/jboss-as-kitchensink/src/main/java/org/jboss/as/quickstarts/kitchensink/model/Member.java");
 		ParameterizedCommand parmCommand = new ParameterizedCommand(
 				openFile, new Parameterization[] { parm });
 
 		handlerService.executeCommand(parmCommand, null);
 		JobUtils.waitForIdle();
 		IWorkbenchPage page = getActivePage();
		
		IEditorPart editor = page.getActiveEditor();
		assertTrue("Java Editor is not opened.", editor instanceof CompilationUnitEditor);
		IEditorInput input = editor.getEditorInput();
		assertTrue("Incorrect editor input.", input instanceof IFileEditorInput);
		IFileEditorInput fileEditorInput = (IFileEditorInput) input;
		IFile file = fileEditorInput.getFile();
		assertEquals("Incorrect file opened.",file.getFullPath().toString(), ("/jboss-as-kitchensink/src/main/java/org/jboss/as/quickstarts/kitchensink/model/Member.java"));
		page.closeAllEditors(false);
	}

	private IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public CheatSheetView openCheatSheet(IFile file)
			throws MalformedURLException {
		CheatSheetView view = ViewUtilities.showCheatSheetView();
		URL url = file.getLocation().toFile().toURI().toURL();
		String id = "testId";
		view.getCheatSheetViewer().setInput(id, id, url, new DefaultStateManager(), false);
		return view;
	}
	
	private void createCheatSheet(IFile file) throws CoreException {
		String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +  
				"<cheatsheet title=\"Test\"> \n" +
				"<intro><description/></intro><item skip=\"true\" title=\"Test\"><description/></item>\n" +
				"</cheatsheet>";
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		file.create(source, true, null);
	}

}
