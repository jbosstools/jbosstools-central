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
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@Require(perspective="Java EE")
public abstract class AbstractConfiguratorsTest extends AbstractMavenSWTBotTest{
	
	public static final String PROJECT_NAME_JSF="testWEB_JSF";
	public static final String PROJECT_NAME_JAXRS="testWEB_JAXRS";
	public static final String PROJECT_NAME_CDI="testWEB_CDI";
	public static final String PROJECT_NAME_CDI_EJB="testEJB_CDI";
	public static final String PROJECT_NAME_SEAM="testWEB_SEAM";
	public static final String PROJECT_NAME_JPA="testWEB_JPA";
	public static final String JSF_NATURE="org.jboss.tools.jsf.jsfnature";
	public static final String JAXRS_NATURE="org.jboss.tools.ws.jaxrs.nature";
	public static final String CDI_NATURE="org.jboss.tools.cdi.core.cdinature";
	public static final String SEAM_NATURE="org.jboss.tools.seam.core.seamnature";
	public static final String JPA_NATURE="org.hibernate.eclipse.console.hibernateNature";
	
	public static final String WEB_XML_LOCATION="/WebContent/WEB-INF/web.xml";
	public static final String JBOSS7_AS_HOME=System.getProperty("jbosstools.test.jboss.home.7.1");
//jpa config, gwt, hibernate
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);

	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}
	
	@Before
	public void clean() throws InterruptedException, CoreException{
		WorkspaceHelpers.cleanWorkspace();
		bot.sleep(1000);
	}
	
	public void addPersistence(String projectName) throws ParserConfigurationException, SAXException, IOException, CoreException, TransformerException, InterruptedException {
		SWTBotTree innerBot = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot, "New","Other...");
		bot.tree().expandNode("XML").select("XML File");
		bot.button("Next >").click();
		bot.textWithLabel("Enter or select the parent folder:").setText(projectName+"/src/META-INF");
		bot.textWithLabel("File name:").setText("persistence.xml");
		bot.button("Finish").click();
		waitForIdle();
		bot.sleep(1000);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPer = docBuilder.newDocument();
	    
	    Element persistenceElement = docPer.createElement("persistence");
	    persistenceElement.setAttribute("version","2.0");
	    persistenceElement.setAttribute("xmlns", "http://java.sun.com/xml/ns/persistence");
	    persistenceElement.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
	    persistenceElement.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd");
	    
	    
	    Element persistenceUnitElement = docPer.createElement("persistence-unit");
	    persistenceUnitElement.setAttribute("name","primary");
	    persistenceUnitElement.setAttribute("transaction-type","JTA");
	    
	    persistenceElement.appendChild(persistenceUnitElement);
	    docPer.appendChild(persistenceElement);
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPer);
		trans.transform(source, result);
		project.getProject().getFolder("src").getFolder("META-INF").getFile("persistence.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
		
		waitForIdle();
	}

	public void createMavenizedDynamicWebProject(String projectName, boolean runtime) throws Exception{
		bot.menu("File").menu("New").menu("Other...").click();
		bot.waitForShell("New");
		bot.tree().expandNode("Web").select("Dynamic Web Project");
		bot.button("Next >").click();
		bot.waitForShell("New Dynamic Web Project");
		bot.textWithLabel("Project name:").setText(projectName);
		if(runtime){
			bot.button("New Runtime...").click();
			waitForShell(botUtil, "New Server Runtime Environment");
			bot.tree().expandNode("JBoss Community").select("JBoss 7.1 Runtime");
			bot.button("Next >").click();
			bot.textWithLabel("Home Directory").setText(JBOSS7_AS_HOME);
			bot.button("Finish").click();
		} else {
			bot.comboBoxInGroup("Target runtime").setSelection("<None>");
		}
		bot.sleep(1000);
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.button("Next >").click();
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.button("Next >").click();
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.checkBox("Generate web.xml deployment descriptor").select();
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		waitForIdle();
		SWTBotTree item = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(item, "Properties");
		SWTBot shellProperties = bot.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    botUtil.waitForAll();
	    Thread.sleep(500);
	    bot.hyperlink("Further configuration required...").click();
	    bot.button("OK").click();
	    bot.button("OK").click();
	    botUtil.waitForAll();
		assertTrue(projectName+ " doesn't have maven nature",isMavenProject(projectName));
		updateConf(botUtil,projectName);
		assertFalse("Project "+projectName+" has "+CDI_NATURE+" nature.",hasNature(projectName, CDI_NATURE)); //false always
		if(runtime){
			assertTrue("Project "+projectName+" doesn't have "+JSF_NATURE+" nature.",hasNature(projectName, JSF_NATURE));
			assertTrue("Project "+projectName+" doesn't have "+JAXRS_NATURE+" nature.",hasNature(projectName, JAXRS_NATURE));
		} else {
			assertFalse("Project "+projectName+" has "+JSF_NATURE+" nature.",hasNature(projectName, JSF_NATURE));
			assertFalse("Project "+projectName+" has "+JAXRS_NATURE+" nature.",hasNature(projectName, JAXRS_NATURE));
		}
	}
	
	public void createMavenizedEJBProject(String projectName, boolean runtime)throws Exception{
		bot.sleep(10000);
		bot.menu("File").menu("New").menu("Other...").click();
		bot.waitForShell("New");
		bot.tree().expandNode("EJB").select("EJB Project");
		bot.button("Next >").click();
		bot.waitForShell("New EJB Project");
		bot.textWithLabel("Project name:").setText(projectName);
		if(runtime){
			bot.button("New Runtime...").click();
			waitForShell(botUtil,"New Server Runtime Environment");
			bot.tree().expandNode("JBoss Community").select("JBoss 7.1 Runtime");
			bot.button("Next >").click();
			bot.textWithLabel("Home Directory").setText(JBOSS7_AS_HOME);
			bot.button("Finish").click();
		} else {
			bot.comboBoxInGroup("Target runtime").setSelection("<None>");
		}
		waitForShell(botUtil,"New EJB Project");
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		waitForIdle();
		SWTBotTree item = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(item, "Properties");
		waitForShell(botUtil,"Properties for "+projectName);
	    bot.tree().select("Project Facets");
	    bot.tree(1).getTreeItem("JBoss Maven Integration").check();
	    botUtil.waitForAll();
	    Thread.sleep(500);
	    bot.hyperlink("Further configuration required...").click();
	    bot.comboBoxWithLabel("Packaging:").setSelection("ejb");
	    bot.button("OK").click();
	    bot.button("OK").click();
	    botUtil.waitForAll();
		assertTrue(projectName+ " doesn't have maven nature",isMavenProject(projectName));
		updateConf(botUtil,projectName);
		assertFalse("Project "+projectName+" has "+JSF_NATURE+" nature.",hasNature(projectName, JSF_NATURE));
		assertFalse("Project "+projectName+" has "+JAXRS_NATURE+" nature.",hasNature(projectName, JAXRS_NATURE));
		assertFalse("Project "+projectName+" has "+CDI_NATURE+" nature.",hasNature(projectName, CDI_NATURE));
		
	}
	
	public void addFacesConf(String projectName) throws InterruptedException{
		SWTBotTree innerBot = bot.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot, "New","Other...");
		bot.tree().expandNode("JBoss Tools Web").expandNode("JSF").select("Faces Config");
		bot.button("Next >").click();
		bot.button("Browse...").click();
		bot.tree().expandNode(projectName).expandNode("WebContent").select("WEB-INF");
		bot.button("OK").click();
		bot.button("Finish").click();
		updateConf(botUtil,projectName);
	}
	
	public void addServlet(String projectName, String servletName, String servletClass, String load) throws Exception{
		IProject facade = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(facade.getProject().getFile(WEB_XML_LOCATION).getContents());
	    Element servletElement = docPom.createElement("servlet");
	    Element servletNameElement = docPom.createElement("servlet-name");  
	    Element servletClassElement = docPom.createElement("servlet-class");	    
	    Element loadElement = docPom.createElement("load-on-startup");
	    
	    servletNameElement.setTextContent(servletName);
	    servletClassElement.setTextContent(servletClass);
	    loadElement.setTextContent(load);
	    
	    Element root = docPom.getDocumentElement();
	    servletElement.appendChild(servletNameElement);
	    servletElement.appendChild(servletClassElement);
	    servletElement.appendChild(loadElement);
	    root.appendChild(servletElement);
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		facade.getProject().getFile(WEB_XML_LOCATION).setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
		botUtil.waitForAll();
		updateConf(botUtil,projectName);	
	}
}
