/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.ui.internal.util.SourceLookupUtil;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class OpenBinaryFileTest {
	
	private static final String ORG_APACHE_COMMONS_LANG_STRING_UTILS = "org.apache.commons.lang.StringUtils";

	private static final String PROJECT_NAME = "test13848";

	public static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

	public static final String PROTOCOL_PLATFORM = "platform"; //$NON-NLS-1$

	private static final IOverwriteQuery OVERWRITE_ALL_QUERY = new IOverwriteQuery() {
		public String queryOverwrite(String pathString) {
			return IOverwriteQuery.ALL;
		}
	};

	private static IProject project;

	@BeforeClass
	public static void init() throws Exception {
		File file = getProjectFile();
		project = importTestProject(PROJECT_NAME, file);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		JobUtils.waitForIdle();
		IEclipsePreferences preferences = SourceLookupActivator
				.getPreferences();
		preferences.put(SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_ALWAYS);
	}
	
	@AfterClass 
	public static void dispose() throws Exception {
		closeAllEditors(false);
		JobUtils.waitForIdle();
		project.delete(true, true, null);
		JobUtils.waitForIdle();		
	}
	
	@Test
	public void testOpenFile() throws Exception {
		IDocument document = getDocument();
		String text = document.get();
		assertEquals(268703, text.length());
		closeAllEditors(false);
	}

	public IDocument getDocument() throws JavaModelException, PartInitException {
		IJavaProject javaProject = JavaCore.create(project);
		assertTrue(javaProject != null);
		IType type = javaProject.findType(ORG_APACHE_COMMONS_LANG_STRING_UTILS);
		assertNotNull(type);
		IClassFile classFile = type.getClassFile();
		assertNotNull(classFile);
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				closeAllEditors(false);
			}
		});
		IEditorPart activeEditor = EditorUtility.openInEditor(classFile, true);
		JobUtils.waitForIdle();
		assertTrue(activeEditor instanceof ClassFileEditor);
		ClassFileEditor editor = (ClassFileEditor) activeEditor;
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		return document;
	}
	
	@Test
	public void testInvalidAttachment() throws Exception {
		invalidateAttachment("/NON-EXISTING");
		IDocument document = getDocument();
		String text = document.get();
		assertEquals(268703, text.length());
		closeAllEditors(false);
	}

	@Test
	public void testPreference() throws Exception {
		invalidateAttachment("/NON-EXISTING");
		String oldValue = SourceLookupActivator.getDefault().getAutoAddSourceAttachment();
		IEclipsePreferences preferences = SourceLookupActivator
				.getPreferences();
		try {
			preferences.put(
					SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT,
					SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_NEVER);
			IDocument document = getDocument();
			String text = document.get();
			assertNotEquals(268703l, text.length());
		} finally {
			preferences.put(
					SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT,
					oldValue);
			closeAllEditors(false);
		}
	}

	@Test
	public void testJBIDE14990_InvalidExistingAttachment() throws Exception {
		invalidateAttachment("/" + PROJECT_NAME +"/lib/commons-lang-2.6.jar");
		IDocument document = getDocument();
		String text = document.get();
		assertEquals(268703, text.length());
		closeAllEditors(false);
	}


	public void invalidateAttachment(String path) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		assertTrue(javaProject != null);
		IType type = javaProject.findType(ORG_APACHE_COMMONS_LANG_STRING_UTILS);
		assertNotNull(type);
		IClassFile classFile = type.getClassFile();
		IJavaElement element = classFile;
		while (element.getParent() != null) {
			element = element.getParent();
			if (element instanceof IPackageFragmentRoot) {
				final IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
				SourceLookupUtil.attachSource(fragment, new Path(path));
				break;
			}
		}
	}

	private static void closeAllEditors(boolean save) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window:windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page:pages) {
				page.closeAllEditors(save);
			}
		}
	}

	public static IProject importTestProject(String projectName, File file) throws CoreException,
			ZipException, IOException, InvocationTargetException,
			InterruptedException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		createProject(project, new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		ZipFile sourceFile = new ZipFile(file);
		ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
				sourceFile);
		
		Enumeration<? extends ZipEntry> entries = sourceFile.entries();
		ZipEntry entry = null;
		List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
		List<ZipEntry> directories = new ArrayList<ZipEntry>();
		String prefix = projectName + "/"; //$NON-NLS-1$
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (entry.getName().startsWith(prefix)) {
				if (!entry.isDirectory()) {
					filesToImport.add(entry);
				} else {
					directories.add(entry);
				}
			}
		}
		
		structureProvider.setStrip(1);
		ImportOperation operation = new ImportOperation(project.getFullPath(), structureProvider.getRoot(),
				structureProvider, OVERWRITE_ALL_QUERY, filesToImport);
		operation.setContext(getActiveShell());
		operation.run(new NullProgressMonitor());
		for (ZipEntry directory : directories) {
			IPath resourcePath = new Path(directory.getName());
			if (resourcePath.segmentCount() > 1
					&& !workspace.getRoot().getFolder(resourcePath).exists()) {
				workspace.getRoot().getFolder(resourcePath)
						.create(false, true, null);
			}
		}
		return project;
	}

	public static File getProjectFile() throws IOException,
			FileNotFoundException {
		return getProjectFile("projects/test13848.zip");
	}

	public static File getProjectFile(String projectPath) throws IOException,
			FileNotFoundException {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		URL url = bundle.getEntry(projectPath);
		InputStream in = null;
		OutputStream out = null;
		File file = null;
		try {
			in = url.openStream();
			file = File.createTempFile("test", "zip");
			out = new FileOutputStream(file);
			copy(in, out);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// ignore
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return file;
	}
	
	private static void createProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IPath location = Platform.getLocation();
		if (!Platform.getLocation().equals(location)) {
			IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
			desc.setLocation(location.append(project.getName()));
			project.create(desc, monitor);
		} else
			project.create(monitor);
	}
	
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[16 * 1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
	}
	
	private static Shell getActiveShell() {
		Display display = Display.getDefault();
		final Shell[] ret = new Shell[1];
		display.syncExec(new Runnable() {

			public void run() {
				ret[0] = Display.getCurrent().getActiveShell();
			}
			
		});
		return ret[0];
	}
}
