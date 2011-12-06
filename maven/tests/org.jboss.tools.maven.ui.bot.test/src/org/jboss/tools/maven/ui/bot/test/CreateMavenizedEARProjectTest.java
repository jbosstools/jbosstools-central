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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


@Require(perspective="Java EE")
public class CreateMavenizedEARProjectTest {
	
	public static final String WAR_PROJECT_NAME="earWeb";
	public static final String EJB_PROJECT_NAME="earEJB";
	public static final String EAR_PROJECT_NAME="ear";
	
	private SWTBotExt botext = new SWTBotExt();

	
	@Test
	public void createEARProject() throws Exception{
		createWarProject(WAR_PROJECT_NAME);
		createEJBProject(EJB_PROJECT_NAME);
		botext.menu("File").menu("Enterprise Application Project").click();
		botext.textWithLabel("Project name:").setText(EAR_PROJECT_NAME);
		botext.button("Modify...").click();
		botext.tree().getTreeItem("JBoss Maven Integration").check();
		botext.button("OK").click();
		botext.button("Next >").click();
		botext.button("Select All").click();
		botext.button("Next >").click();
		botext.comboBoxWithLabel("Packaging:").setSelection("ear");
		botext.button("Finish").click();
		waitForIdle();
		installProject(WAR_PROJECT_NAME);
		installProject(EJB_PROJECT_NAME);
		editEARPomDependencies();
		botext.viewByTitle("Project Explorer").bot().tree().getTreeItem(EAR_PROJECT_NAME).contextMenu("Run As").menu("5 Maven build...").click();
		waitForIdle();
		botext.textWithLabel("Goals:").setText("clean package");
		botext.button("Run").click();
		waitForIdle();
	}
	
	private void createWarProject(String projectName){
		botext.menu("File").menu("Dynamic Web Project").click();
		botext.textWithLabel("Project name:").setText(projectName);
		botext.button("Modify...").click();
		botext.tree().getTreeItem("JBoss Maven Integration").check();
		botext.button("OK").click();
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.checkBox("Generate web.xml deployment descriptor").select();
		botext.button("Next >").click();
		botext.button("Finish").click();
		waitForIdle();
	}
	
	private void createEJBProject(String projectName){
		botext.menu("File").menu("EJB Project").click();
		botext.textWithLabel("Project name:").setText(projectName);
		botext.button("Modify...").click();
		botext.tree().getTreeItem("JBoss Maven Integration").check();
		botext.button("OK").click();
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.comboBoxWithLabel("Packaging:").setSelection("ejb");
		botext.button("Finish").click();
		waitForIdle();
	}
	
	private void installProject(String projectName) throws InterruptedException{
		botext.viewByTitle("Project Explorer").bot().tree().getTreeItem(projectName).contextMenu("Run As").menu("5 Maven build...").click();
		botext.textWithLabel("Goals:").setText("clean package install");
		botext.button("Run").click();
		waitForIdle();
	}
	
	private void editEARPomDependencies() throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException{
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(EAR_PROJECT_NAME);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(project.getFile("pom.xml").getContents());
	    Element modulesElement = docPom.createElement("modules");
	    Element webModuleElement = docPom.createElement("webModule");
	    Element groupIdWebElement = docPom.createElement("groupId");  
	    Element artifactIdWebElement = docPom.createElement("artifactId");	    
	    Element versionWebElement = docPom.createElement("version");
	    Element bundleFileNameWebElement = docPom.createElement("bundleFileName");
	    Element ejbModuleElement = docPom.createElement("ejbModule");
	    Element groupIdEjbElement = docPom.createElement("groupId");  
	    Element artifactIdEjbElement = docPom.createElement("artifactId");
	    Element groupIdEjbModuleElement = docPom.createElement("groupId");  
	    Element artifactIdEjbModuleElement = docPom.createElement("artifactId");
	    Element groupIdWebModuleElement = docPom.createElement("groupId");  
	    Element artifactIdWebModuleElement = docPom.createElement("artifactId");	
	    Element versionEjbElement = docPom.createElement("version");
	    Element bundleFileNameEjbElement = docPom.createElement("bundleFileName");
	    Element dependenciesElement = docPom.createElement("dependencies");
	    Element dependencyEjbElement = docPom.createElement("dependency");
	    Element dependencyWarElement = docPom.createElement("dependency");
	    Element typeEjbElement = docPom.createElement("type");
	    Element typeWarElement = docPom.createElement("type");
	    
	    groupIdWebElement.setTextContent("org.jboss.tools");
	    groupIdEjbElement.setTextContent("org.jboss.tools");
	    artifactIdWebElement.setTextContent(WAR_PROJECT_NAME);
	    artifactIdEjbElement.setTextContent(EJB_PROJECT_NAME);
	    groupIdWebModuleElement.setTextContent("org.jboss.tools");
	    groupIdEjbModuleElement.setTextContent("org.jboss.tools");
	    artifactIdWebModuleElement.setTextContent(WAR_PROJECT_NAME);
	    artifactIdEjbModuleElement.setTextContent(EJB_PROJECT_NAME);
	    versionWebElement.setTextContent("0.0.1-SNAPSHOT");
	    versionEjbElement.setTextContent("0.0.1-SNAPSHOT");
	    typeWarElement.setTextContent("war");
	    typeEjbElement.setTextContent("ejb");
	    bundleFileNameWebElement.setTextContent(WAR_PROJECT_NAME+"-0.0.1-SNAPSHOT.war");
	    bundleFileNameEjbElement.setTextContent(EJB_PROJECT_NAME+"-0.0.1-SNAPSHOT.jar");
	    
	    webModuleElement.appendChild(groupIdWebModuleElement);
	    webModuleElement.appendChild(artifactIdEjbModuleElement);
	    webModuleElement.appendChild(bundleFileNameWebElement);
	    ejbModuleElement.appendChild(groupIdEjbModuleElement);
	    ejbModuleElement.appendChild(artifactIdEjbModuleElement);
	    ejbModuleElement.appendChild(bundleFileNameEjbElement);
	    modulesElement.appendChild(webModuleElement);
	    modulesElement.appendChild(ejbModuleElement);
	    
	    dependencyWarElement.appendChild(groupIdWebElement);
	    dependencyWarElement.appendChild(artifactIdWebElement);
	    dependencyWarElement.appendChild(versionWebElement);
	    dependencyWarElement.appendChild(typeWarElement);
	    dependencyEjbElement.appendChild(groupIdEjbElement);
	    dependencyEjbElement.appendChild(artifactIdEjbElement);
	    dependencyEjbElement.appendChild(versionEjbElement);
	    dependencyEjbElement.appendChild(typeEjbElement);
	    dependenciesElement.appendChild(dependencyWarElement);
	    dependenciesElement.appendChild(dependencyEjbElement);
	    
	    Element root = docPom.getDocumentElement();
	    Element buildElement = (Element)root.getElementsByTagName("build").item(0);
	    Node plugin = buildElement.getElementsByTagName("configuration").item(0);
	    
	    root.appendChild(dependenciesElement);
	    plugin.appendChild(modulesElement);
	    
	    //save xml
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		project.getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
	    waitForIdle();
	    
	}
	
	private static void waitForIdle() {
		AbstractMavenSWTBotTest.waitForIdle();
	}
	
	
}
