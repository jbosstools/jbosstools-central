package org.jboss.tools.maven.ui.bot.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


@Require(perspective="Java EE")
public class EARProjectTest extends AbstractMavenSWTBotTest{
	
	public static final String WAR_PROJECT_NAME="earWeb";
	public static final String EJB_PROJECT_NAME="earEJB";
	public static final String EAR_PROJECT_NAME="ear";

	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}

	
	@Test
	public void createEARProject() throws Exception{
		createWarProject(WAR_PROJECT_NAME);
		Thread.sleep(500);
		createEJBProject(EJB_PROJECT_NAME);
		Thread.sleep(500);
		bot.menu("File").menu("Enterprise Application Project").click();
		bot.textWithLabel("Project name:").setText(EAR_PROJECT_NAME);
		bot.button("Modify...").click();
		bot.tree().getTreeItem("JBoss Maven Integration").check();
		bot.button("OK").click();
		bot.button("Next >").click();
		bot.button("Select All").click();
		bot.button("Next >").click();
		bot.comboBoxWithLabel("Packaging:").setSelection("ear");
		bot.button("Finish").click();
		waitForIdle();
		assertTrue("EAR project isn't maven project", isMavenProject(EAR_PROJECT_NAME));
		installProject(WAR_PROJECT_NAME);
		installProject(EJB_PROJECT_NAME);
		addDependencies(EAR_PROJECT_NAME, "org.jboss.tools", WAR_PROJECT_NAME, "0.0.1-SNAPSHOT", "war");
		addDependencies(EAR_PROJECT_NAME, "org.jboss.tools", EJB_PROJECT_NAME, "0.0.1-SNAPSHOT", "ejb");
		confEarMavenPlugn(EAR_PROJECT_NAME);
		bot.viewByTitle("Package Explorer").setFocus();
		SWTBotTree innerBot = bot.viewByTitle("Package Explorer").bot().tree().select(EAR_PROJECT_NAME);
		ContextMenuHelper.clickContextMenu(innerBot,"Run As","3 Maven build...");
		waitForShell(botUtil,"Edit Configuration");
		bot.textWithLabel("Goals:").setText("clean package");
		bot.button("Run").click();
		waitForIdle();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(EAR_PROJECT_NAME);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IFolder earFolder = project.getFolder("target/" + EAR_PROJECT_NAME + "-0.0.1-SNAPSHOT");
		assertTrue(earFolder +" is missing ", earFolder.exists());
		assertTrue(WAR_PROJECT_NAME+ ".war is missing in ear",project.getFile("target/" +EAR_PROJECT_NAME+ "-0.0.1-SNAPSHOT/" +WAR_PROJECT_NAME+ "-0.0.1-SNAPSHOT.war").exists());
		assertTrue(EJB_PROJECT_NAME+ ".jar is missing in ear",project.getFile("target/" +EAR_PROJECT_NAME+ "-0.0.1-SNAPSHOT/" +EJB_PROJECT_NAME+ "-0.0.1-SNAPSHOT.jar").exists());
	}
	
	private void createWarProject(String projectName) throws CoreException, InterruptedException{
		bot.menu("File").menu("Dynamic Web Project").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.checkBox("Generate web.xml deployment descriptor").select();
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		SWTBotTreeItem item = bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		waitForShell(botUtil, "Properties for "+projectName);
		SWTBot shellProperties = bot.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    waitForIdle();
	    bot.sleep(500);
	    bot.hyperlink("Further configuration required...").click();
	    bot.button("OK").click();
	    bot.button("OK").click();
	    botUtil.waitForAll(Long.MAX_VALUE);
		assertTrue("Web project doesn't have maven nature",isMavenProject(projectName));
		
	}
	
	private void createEJBProject(String projectName) throws CoreException, InterruptedException{
		bot.menu("File").menu("EJB Project").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Modify...").click();
		bot.tree().getTreeItem("JBoss Maven Integration").check();
		bot.button("OK").click();
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.comboBoxWithLabel("Packaging:").setSelection("ejb");
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		assertTrue("EJB project doesn't have maven nature", isMavenProject(projectName));
	}
	
	private void installProject(String projectName) throws InterruptedException{
		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.tree().expandNode("Java").select("Package Explorer").click();
		bot.button("OK").click();
		SWTBotTree innerBot = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot,"Run As","5 Maven build...");
		waitForShell(botUtil,"Edit Configuration");
		bot.textWithLabel("Goals:").setText("clean install");
		bot.button("Run").click();
		waitForIdle();
		botUtil.waitForAll();
		bot.sleep(5000);
	}
	
	private void confEarMavenPlugn(String projectName) throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(project.getProject().getFile("pom.xml").getContents());
	    Element configurationElement = (Element) docPom.getElementsByTagName("configuration").item(0);
	    Element modulesElement = docPom.createElement("modules");
	    Element warModuleElement = docPom.createElement("webModule");  
	    Element ejbModuleElement = docPom.createElement("ejbModule");	    
	    Element groupIdWarElement = docPom.createElement("groupId");  
	    Element artifactIdWarElement = docPom.createElement("artifactId");	
	    Element groupIdEJBElement = docPom.createElement("groupId");  
	    Element artifactIdEJBElement = docPom.createElement("artifactId");	

	    groupIdWarElement.setTextContent("org.jboss.tools");
	    groupIdEJBElement.setTextContent("org.jboss.tools");
	    artifactIdWarElement.setTextContent(WAR_PROJECT_NAME);
	    artifactIdEJBElement.setTextContent(EJB_PROJECT_NAME);
	    
	    warModuleElement.appendChild(groupIdWarElement);
	    warModuleElement.appendChild(artifactIdWarElement);
	    ejbModuleElement.appendChild(groupIdEJBElement);
	    ejbModuleElement.appendChild(artifactIdEJBElement);
	    modulesElement.appendChild(warModuleElement);
	    modulesElement.appendChild(ejbModuleElement);
	    configurationElement.appendChild(modulesElement);
	    
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		project.getProject().getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
	}
	
	
}
