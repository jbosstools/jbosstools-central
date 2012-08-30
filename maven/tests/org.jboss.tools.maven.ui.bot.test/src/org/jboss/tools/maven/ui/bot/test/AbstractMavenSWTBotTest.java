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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.m2e.tests.common.JobHelpers;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.tools.maven.ui.bot.test.utils.ProjectHasNature;
import org.jboss.tools.test.util.ResourcesUtils;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.jboss.tools.ui.bot.ext.parts.SWTBotRadioExt;
import org.jboss.tools.ui.bot.ext.view.ErrorLogView;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractMavenSWTBotTest extends AbstractMavenProjectTestCase {

	public static final String PACKAGE_EXPLORER = "Package Explorer"; //$NON-NLS-1$
	public static final int TIMEOUT = 30*1000;
	public static final int HAS_NATURE_TIMEOUT=10000;
	private ErrorLogView errorLog;

	protected SWTBotExt bot = new SWTBotExt();
	
	@BeforeClass 
	public static void beforeClass() throws Exception {
		setUserSettingsAndPerspective();
		WorkbenchPlugin.getDefault().getPreferenceStore()
		.setValue(IPreferenceConstants.RUN_IN_BACKGROUND, true);

		PrefUtil.getAPIPreferenceStore().setValue(
		IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);
	}

	//@AfterClass
	public final static void cleanUp() throws Exception {
		boolean buildAutomatically = ResourcesUtils.setBuildAutomatically(false);
		ValidationFramework.getDefault().suspendAllValidation(true);
		try {
			WorkspaceHelpers.cleanWorkspace();
		} finally {
			ResourcesUtils.setBuildAutomatically(buildAutomatically);
			ValidationFramework.getDefault().suspendAllValidation(false);
		}
		JobHelpers.waitForLaunchesToComplete(30*1000);
		JobHelpers.waitForJobsToComplete();
	}

	public void waitForIdle() {
		JobHelpers.waitForLaunchesToComplete(TIMEOUT);
		JobHelpers.waitForJobsToComplete();
	}
	
	public boolean isMavenProject(String projectName) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project.hasNature(IMavenConstants.NATURE_ID);
	}
	
	public boolean hasNature(String projectName, String natureID){
		try{
			bot.waitUntil(new ProjectHasNature(projectName, natureID),HAS_NATURE_TIMEOUT);
		} catch (TimeoutException ex){
			return false;
		}
		return true;
	}

	public void waitForShell(SWTUtilExt botUtil, String shellName) throws InterruptedException {
		while(!botUtil.isShellActive(shellName)){
			Thread.sleep(500);
		}
	}
	
	protected void addDependencies(String projectName, String groupId, String artifactId, String version, String type) throws Exception{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(project.getProject().getFile("pom.xml").getContents());
	    Element dependenciesElement = null;
	    if(docPom.getElementsByTagName("dependencies").item(0)==null){
	    	dependenciesElement = docPom.createElement("dependencies");
	    } else {
	    	dependenciesElement = (Element) docPom.getElementsByTagName("dependencies").item(0);
	    }
	    Element dependencyElement = docPom.createElement("dependency");
	    Element groupIdElement = docPom.createElement("groupId");  
	    Element artifactIdElement = docPom.createElement("artifactId");	    
	    Element versionElement = docPom.createElement("version");
	    Element typeElement = docPom.createElement("type");
	    
	    groupIdElement.setTextContent(groupId);
	    artifactIdElement.setTextContent(artifactId);
	    versionElement.setTextContent(version);
	    
	    Element root = docPom.getDocumentElement();
	    dependencyElement.appendChild(groupIdElement);
	    dependencyElement.appendChild(artifactIdElement);
	    dependencyElement.appendChild(versionElement);
	    if(type!=null){
	    	typeElement.setTextContent(type);
	    	dependencyElement.appendChild(typeElement);
	    }
	    dependenciesElement.appendChild(dependencyElement);
	    root.appendChild(dependenciesElement);
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		project.getProject().getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
	}
	
	protected void updateConf(SWTUtilExt botUtil, String projectName){
		SWTBotTree innerBot = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot, "Maven","Update Project Configuration...");
		bot.button("OK").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		botUtil.waitForNonIgnoredJobs();
	}
	
	private static void setUserSettingsAndPerspective() throws InterruptedException, IOException, CoreException{
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("Window").menu("Preferences").click();
		botExt.tree().expandNode("Maven").select("User Settings").click();
		File f = new File("usersettings/settings.xml");
		botExt.text(1).setText(f.getAbsolutePath());
		botExt.button("Update Settings").click();
		botExt.tree().expandNode("General").select("Perspectives").click();
		SWTBotRadioExt radio = new SWTBotRadioExt(botExt.radio("Never open").widget);
		radio.clickWithoutDeselectionEvent();
		botExt.button("OK").click();
	}
	
	protected void clearErrorLog(){
		errorLog = new ErrorLogView();
		errorLog.clear();
	}
	
	protected void checkErrorLog() {
		int count = errorLog.getRecordCount();
		if (count > 0) {
			errorLog.logMessages();
			fail("Unexpected messages in Error log, see test log");
		}
	}
	
	public void buildProject(String projectName, String mavenBuild, String packaging, String version) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		bot.viewByTitle("Package Explorer").setFocus();
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
		bot.sleep(500);
		SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
		SWTBotExt swtBot = new SWTBotExt();
		item.pressShortcut(Keystrokes.SHIFT,Keystrokes.ALT,KeyStroke.getInstance("X"));
		bot.sleep(1000);
		item.pressShortcut(KeyStroke.getInstance("M"));
		swtBot.waitForShell("Edit Configuration");
		swtBot.textWithLabel("Goals:").setText("clean package");
		swtBot.button("Run").click();
		waitForIdle();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		IFile jarFile = project.getFile("target/" + projectName + version+"."+packaging);
		assertTrue(jarFile + " is missing ", jarFile.exists());
	}
	
}
