package org.jboss.tools.maven.ui.bot.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.Server;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerState;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
@Require(perspective = "Web Development")
public class CreateMavenizedJSFProjectTest{
	public static final String JBOSS6_AS_HOME=System.getProperty("jbosstools.test.jboss.home.6.1", "/home/eiden/Java/RedHat/JBossASs/jboss-6.1.0.Final");
	public static final String JBOSS7_AS_HOME=System.getProperty("jbosstools.test.jboss.home.7.0", "/home/eiden/Java/RedHat/JBossASs/jboss-as-7.0.1.Final1");
	public static final String POM_FILE = "pom.xml";
	public static final String PROJECT_NAME6="JSFProject6";
	public static final String PROJECT_NAME7="JSFProject7";
	public static final String PROJECT_NAME6_v1="JSFProject6_1.2";
	public static final String PROJECT_NAME7_v1="JSFProject7_1.2";
	public static final String SERVER_RUNTIME6="JBoss 6.x Runtime";
	public static final String SERVER_RUNTIME7="JBoss 7.x Runtime";
	public static final String SERVER6="JBoss AS 6.x";
	public static final String SERVER7="JBoss AS 7.x";
	public static final String GROUPID ="javax.faces";
	public static final String ARTIFACTID ="jsf-api";
	public static final String JSF_VERSION_1_1_02 ="1.1.02";
	public static final String JSF_VERSION_1_2 ="2.0";
	public static final String JSF_VERSION_2 ="2.0";
	
	protected static SWTWorkbenchBot bot;
	
	@BeforeClass
	public final static void beforeClass() throws Exception {
		bot = AbstractMavenSWTBotTest.initSWTBot();
	}
	
	@Test
	public void createJSFProjectTest_AS6_v2() throws InterruptedException, CoreException{
		createJSFProject(SERVER_RUNTIME6, SERVER6, JBOSS6_AS_HOME,"JSF 2.0", PROJECT_NAME6);
	}
	
	@Test
	public void activateMavenFacet_AS6_v2() throws InterruptedException, CoreException{
		activateMavenFacet(PROJECT_NAME6);
	}
	
	@Test
	public void buildProject_AS6_v2() throws CoreException, ParserConfigurationException, SAXException, IOException, TransformerException, InterruptedException{
		addDependencies(PROJECT_NAME6, JSF_VERSION_2);
		buildProject(PROJECT_NAME6);
	}
	/*
	@Test
	public void deployProject_AS6_v2() throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException{
		addServerToPom(PROJECT_NAME6, JBOSS6_AS_HOME);
		deployModule(PROJECT_NAME6);
	}
	*/
	
	@Test
	public void createJSFProjectTest_AS7_v2() throws InterruptedException, CoreException{
		createJSFProject(SERVER_RUNTIME7, SERVER7, JBOSS7_AS_HOME,"JSF 2.0", PROJECT_NAME7);
	}
	
	@Test
	public void activateMavenFacet_AS7_v2() throws InterruptedException, CoreException{
		activateMavenFacet(PROJECT_NAME7);
	}
	
	@Test
	public void buildProject_AS7_v2() throws CoreException, ParserConfigurationException, SAXException, IOException, TransformerException, InterruptedException{
		addDependencies(PROJECT_NAME7, JSF_VERSION_2);
		buildProject(PROJECT_NAME7);
	}
	
	@Test
	public void createJSFProjectTest_AS6_v1() throws InterruptedException, CoreException{
		createJSFProject(SERVER_RUNTIME6, SERVER6, JBOSS6_AS_HOME,"JSF 1.2", PROJECT_NAME6_v1);
	}
	
	@Test
	public void activateMavenFacet_AS6_v1() throws InterruptedException, CoreException{
		activateMavenFacet(PROJECT_NAME6_v1);
	}
	
	@Test
	public void buildProject_AS6_v1() throws CoreException, ParserConfigurationException, SAXException, IOException, TransformerException, InterruptedException{
		addDependencies(PROJECT_NAME6_v1, JSF_VERSION_1_2);
		buildProject(PROJECT_NAME6_v1);
	}
	
	@Test
	public void createJSFProjectTest_AS7_v1() throws InterruptedException, CoreException{
		createJSFProject(SERVER_RUNTIME7, SERVER7, JBOSS7_AS_HOME,"JSF 1.2", PROJECT_NAME7_v1);
	}
	
	@Test
	public void activateMavenFacet_AS7_v1() throws InterruptedException, CoreException{
		activateMavenFacet(PROJECT_NAME7_v1);
	}
	
	@Test
	public void buildProject_AS7_v1() throws CoreException, ParserConfigurationException, SAXException, IOException, TransformerException, InterruptedException{
		addDependencies(PROJECT_NAME7_v1, JSF_VERSION_1_2);
		buildProject(PROJECT_NAME7_v1);
	}
	
	
	private void createJSFProject(String serverRuntime, String server, String serverHome, String jsfVersion, String projectName) throws InterruptedException, CoreException{
		bot.menu("File").menu("New").menu("JSF Project").click();
		SWTBot shell = bot.shell("New JSF Project").activate().bot();
		shell.textWithLabel("Project Name*").setText(projectName);
		shell.comboBox(0).setSelection(jsfVersion);
		shell.comboBox(1).setSelection("JSFKickStartWithoutLibs");
		shell.button("Next >").click();
		Thread.sleep(1000);
		shell.button("New...").click();
		SWTBot shellRuntime = bot.shell("New Server Runtime").activate().bot();
		Thread.sleep(500);
		shellRuntime.tree().expandNode("JBoss Community").select(serverRuntime);
		shellRuntime.button("Next >").click();
		shellRuntime.textWithLabel("Home Directory").setText(serverHome);
		shellRuntime.button("Finish").click();
		Thread.sleep(500);
		shell.button(1).click();
		shellRuntime = bot.shell("New Server").activate().bot();
		shellRuntime.tree().expandNode("JBoss Community").select(server);
		shellRuntime.button("Finish").click();
		waitForIdle();
		shell.button("Finish").click();;
		waitForIdle();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
	}
	
	
	private void addDependencies(String projectName, String jsfVersion) throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException, InterruptedException{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(project.getFile("pom.xml").getContents());
	    Element dependenciesElement = docPom.createElement("dependencies");
	    Element dependencyElement = docPom.createElement("dependency");    
	    Element groupIdElement = docPom.createElement("groupId");  
	    Element artifactIdElement = docPom.createElement("artifactId");	    
	    Element versionElement = docPom.createElement("version");
	    groupIdElement.setTextContent(GROUPID);
	    artifactIdElement.setTextContent(ARTIFACTID);
	    versionElement.setTextContent(jsfVersion);
	    dependencyElement.appendChild(groupIdElement);
	    dependencyElement.appendChild(versionElement);
	    dependencyElement.appendChild(artifactIdElement);
	    dependenciesElement.appendChild(dependencyElement);
	    Element root = docPom.getDocumentElement();
	    root.appendChild(dependenciesElement);    
	    //save pom
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		project.getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
	    waitForIdle();
	}
	
	private void activateMavenFacet(String projectName) throws InterruptedException, CoreException{
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
	    SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
	    Thread.sleep(500);
	    item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
	    Thread.sleep(1000);
	    SWTBot shellProperties = bot.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    Thread.sleep(500);
	    SWTBotExt swtBot = new SWTBotExt();
	    swtBot.hyperlink("Further configuration required...").click();
	    swtBot.button("OK").click();
	    swtBot.button("OK").click();
	    waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
		//Utils.isMavenProject(projectName);
	}
	
	private void buildProject(String projectName) throws CoreException{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
	    SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
	    SWTBotExt swtBot = new SWTBotExt();
	    item.contextMenu("Run As").menu("5 Maven build...").click();
	    swtBot.textWithLabel("Goals:").setText("clean package");
	    swtBot.button("Run").click();
	    waitForIdle();
	    project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IFolder warFolder = project.getFolder("target/" + projectName + "-0.0.1-SNAPSHOT");
		assertTrue(warFolder +" is missing ", warFolder.exists());
		IPath webInfPath = new Path("WEB-INF");
		assertFalse(warFolder.getFolder(webInfPath.append("src")).exists());
		assertFalse(warFolder.getFolder(webInfPath.append("dev")).exists());
		assertTrue(warFolder.getFolder(webInfPath.append("lib")).exists());
	}
	
	private void addServerToPom(String projectName, String serverLocation) throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(project.getFile("pom.xml").getContents());
	    Element pluginElement = docPom.createElement("plugin");
	    Element groupIdElement = docPom.createElement("groupId");  
	    Element artifactIdElement = docPom.createElement("artifactId");	    
	    Element versionElement = docPom.createElement("version");
	    Element configurationElement = docPom.createElement("configuration");  
	    Element jbossHomeElement = docPom.createElement("jbossHome");	    
	    Element serverNameElement = docPom.createElement("serverName");
	    Element fileNameElement = docPom.createElement("fileName");
	    
	    groupIdElement.setTextContent("org.codehaus.mojo");
	    artifactIdElement.setTextContent("jboss-maven-plugin");
	    versionElement.setTextContent("1.5.0");
	    jbossHomeElement.setTextContent(serverLocation);
	    serverNameElement.setTextContent("default");
	    fileNameElement.setTextContent("target"+projectName+".war");
	    
	    Element root = docPom.getDocumentElement();
	    Element buildElement = (Element)root.getElementsByTagName("build").item(0);
	    Node plugins = buildElement.getElementsByTagName("plugins").item(0);
	    configurationElement.appendChild(serverNameElement);
	    configurationElement.appendChild(jbossHomeElement);
	    configurationElement.appendChild(fileNameElement);
	    pluginElement.appendChild(configurationElement);
	    pluginElement.appendChild(versionElement);
	    pluginElement.appendChild(artifactIdElement);
	    pluginElement.appendChild(groupIdElement);
	    plugins.appendChild(pluginElement);
	    //save pom
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		project.getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
	    waitForIdle();
	}
	
	private void deployModule(String projectName){
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
	    SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
	    SWTBotExt swtBot = new SWTBotExt();
	    item.contextMenu("Run As").menu("5 Maven build...").click();
	    waitForIdle();
	    swtBot.textWithLabel("Goals:").setText("jboss:hard-deploy");
	    swtBot.button("Run").click();
	    waitForIdle();
	}
	
	
	private static void waitForIdle() {
		AbstractMavenSWTBotTest.waitForIdle();
	}
	
	private static void assertNoErrors(IProject project) throws CoreException {
		WorkspaceHelpers.assertNoErrors(project);
	}
	
}
