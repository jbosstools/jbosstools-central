package org.jboss.tools.maven.ui.bot.test;

import static org.hamcrest.MatcherAssert.assertThat;

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
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTBotFactory;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.condition.TaskDuration;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

//TODO deployment
@SuppressWarnings("restriction")
@Require(perspective = "Web Development")
public class CreateMavenizedJSFProjectTest extends AbstractMavenSWTBotTest{
	public static final String JBOSS7_AS_HOME=System.getProperty("jbosstools.test.jboss.home.7.1");
	public static final String POM_FILE = "pom.xml";
	public static final String PROJECT_NAME7="JSFProject7";
	public static final String PROJECT_NAME7_v1="JSFProject7_1.2";
	public static final String SERVER_RUNTIME7="JBoss 7.1 Runtime";
	public static final String SERVER7="JBoss AS 7.1";
	public static final String GROUPID ="javax.faces";
	public static final String ARTIFACTID ="jsf-api";
	public static final String JSF_VERSION_1_1_02 ="1.1.02";
	public static final String JSF_VERSION_1_2 ="2.0";
	public static final String JSF_VERSION_2 ="2.0";

	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@BeforeClass
	public final static void beforeClass() throws Exception {
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}
	
	//@Test
	public void createJSFProjectTest_AS7_JSFv2() throws InterruptedException, CoreException, ParserConfigurationException, SAXException, IOException, TransformerException{
		createJSFProject(SERVER_RUNTIME7, SERVER7, JBOSS7_AS_HOME,"JSF 2.0", PROJECT_NAME7);
		activateMavenFacet(PROJECT_NAME7);
		addDependencies(PROJECT_NAME7, JSF_VERSION_2);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME7);
		assertNoErrors(project);
		buildProject(PROJECT_NAME7);
	}
	
	@Test
	public void createJSFProjectTest_AS7_JSFv1() throws InterruptedException, CoreException, ParserConfigurationException, SAXException, IOException, TransformerException{
		createJSFProject(SERVER_RUNTIME7, SERVER7, JBOSS7_AS_HOME,"JSF 1.2", PROJECT_NAME7_v1);
		activateMavenFacet(PROJECT_NAME7_v1);
		addDependencies(PROJECT_NAME7_v1, JSF_VERSION_1_2);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME7_v1);
		assertNoErrors(project);
		buildProject(PROJECT_NAME7_v1);
	}
	
	
	private void createJSFProject(String serverRuntime, String server, String serverHome, String jsfVersion, String projectName) throws InterruptedException, CoreException{
		bot.menu("File").menu("New").menu("JSF Project").click();
		bot.textWithLabel("Project Name*").setText(projectName);
		bot.comboBox(0).setSelection(jsfVersion);
		bot.comboBox(1).setSelection("JSFKickStartWithoutLibs");
		bot.button("Next >").click();
		bot.button("New...").click();
		waitForShell(botUtil,"New Server Runtime");
		bot.tree().expandNode("JBoss Community").select(serverRuntime);
		bot.button("Next >").click();
		bot.textWithLabel("Home Directory").setText(serverHome);
		bot.button("Finish").click();
		waitForShell(botUtil,"New JSF Project");
		bot.button(1).click();
		waitForShell(botUtil,"New Server");
		bot.tree().expandNode("JBoss Community").select(server);
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		bot.button("Finish").click();;
		botUtil.waitForAll(Long.MAX_VALUE);
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
		SWTBotTreeItem item = bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
	    item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
	    waitForShell(botUtil,"Properties for "+projectName);
	    bot.tree().select("Project Facets");
	    bot.tree(1).getTreeItem("JBoss Maven Integration").check();
	    botUtil.waitForAll();
	    Thread.sleep(500);
	    bot.hyperlink("Further configuration required...").click();
	    bot.button("OK").click();
	    bot.button("OK").click();
	    waitForIdle();
	    assertTrue(projectName+ " doesn't have maven nature", isMavenProject(projectName));
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
}