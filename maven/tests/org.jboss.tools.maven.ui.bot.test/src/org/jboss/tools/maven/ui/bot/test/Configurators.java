package org.jboss.tools.maven.ui.bot.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
@Require(perspective="Java EE")
public class Configurators {
	
	public static final String PROJECT_NAME_JSF="testWEB_JSF";
	public static final String PROJECT_NAME_CDI="testWEB_CDI";
	public static final String PROJECT_NAME_CDI_EJB="test_CDI_EJB";
	public static final String JSF_NATURE="org.jboss.tools.jsf.jsfnature";
	public static final String JAXRS_NATURE="org.jboss.tools.ws.jaxrs.nature";
	public static final String CDI_NATURE="org.jboss.tools.cdi.core.cdinature";
	public static final String JBOSS6_AS_HOME=System.getProperty("jbosstools.test.jboss.home.6.0");
	
	private SWTBotExt botExt = new SWTBotExt();
	private SWTUtilExt botUtil = new SWTUtilExt(botExt);
	
	
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
	}

	//TODO https://issues.jboss.org/browse/JBIDE-10468
	//https://issues.jboss.org/browse/JBIDE-10831
	@Test
	public void testJSFConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, false);
		addDependencies(PROJECT_NAME_JSF, "com.sun.faces", "mojarra-jsf-api", "2.0.0-b04");
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",Utils.hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, false);
		addFacesConf(PROJECT_NAME_JSF);
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",Utils.hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, false);
		addServlet(PROJECT_NAME_JSF,"Faces Servlet","javax.faces.webapp.FacesServlet","1");
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",Utils.hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools", PROJECT_NAME_JSF,"0.0.1-SNAPSHOT");
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have faces-config.xml file",facade.getProject().getFile("faces-config.xml") != null);
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, true);
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",Utils.hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		
		
		
	}
	
	
	@Test
	public void testCDIConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI, false);
		addDependencies(PROJECT_NAME_CDI, "javax.enterprise", "cdi-api","1.1.EDR1.2");
		assertTrue("Project "+PROJECT_NAME_CDI+" doesn't have "+CDI_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_CDI, CDI_NATURE));
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB, false);
		addDependencies(PROJECT_NAME_CDI_EJB, "javax.enterprise", "cdi-api","1.1.EDR1.2");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+" doesn't have "+CDI_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_CDI_EJB, CDI_NATURE));
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI, true);
		assertTrue("Project "+PROJECT_NAME_CDI+" doesn't have "+CDI_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_CDI, CDI_NATURE));
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB, true);
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+" doesn't have "+CDI_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_CDI_EJB, CDI_NATURE));
	}

	@Test
	public void testJAXRSConfigurator() throws Exception {
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, false);
		addDependencies(PROJECT_NAME_JSF, "com.cedarsoft.rest", "jersey", "1.0.0");
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JAXRS_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_JSF, JAXRS_NATURE));
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, false);
		addDependencies(PROJECT_NAME_JSF, "org.jboss.jbossas", "jboss-as-resteasy", "6.1.0.Final");
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JAXRS_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_JSF, JAXRS_NATURE));
		clean();
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, true);
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JAXRS_NATURE+" nature.",Utils.hasNature(PROJECT_NAME_JSF, JAXRS_NATURE));
	}
	
	
	private void createMavenizedDynamicWebProject(String projectName, boolean runtime) throws Exception{
		botExt.menu("File").menu("New").menu("Dynamic Web Project").click();
		botExt.textWithLabel("Project name:").setText(projectName);
		if(runtime){
			botExt.button("New Runtime...").click();
			Thread.sleep(500);
			botExt.tree().expandNode("JBoss Community").select("JBoss 6.x Runtime");
			botExt.button("Next >").click();
			botExt.textWithLabel("Home Directory").setText(JBOSS6_AS_HOME);
			botExt.button("Finish").click();
		} else {
			botExt.comboBoxInGroup("Target runtime").setSelection("<None>");
		}
		botExt.button("Next >").click();
		botExt.button("Next >").click();
		botExt.checkBox("Generate web.xml deployment descriptor").select();
		botExt.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		Utils.waitForIdle();
		SWTBotTreeItem item = botExt.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		SWTBot shellProperties = botExt.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    botUtil.waitForAll();
	    Thread.sleep(500);
	    botExt.hyperlink("Further configuration required...").click();
	    botExt.button("OK").click();
	    botExt.button("OK").click();
	    botUtil.waitForAll();
		assertTrue("Web project doesn't have maven nature",Utils.isMavenProject(projectName));
		updateConf(projectName);
		assertFalse("Project "+projectName+" have "+JSF_NATURE+" nature.",Utils.hasNature(projectName, JSF_NATURE));
		assertFalse("Project "+projectName+" have "+JAXRS_NATURE+" nature.",Utils.hasNature(projectName, JAXRS_NATURE));
		assertFalse("Project "+projectName+" have "+CDI_NATURE+" nature.",Utils.hasNature(projectName, CDI_NATURE));
	}
	
	private void createMavenizedEJBProject(String projectName, boolean runtime)throws Exception{
		botExt.menu("File").menu("New").menu("EJB Project").click();
		botExt.textWithLabel("Project name:").setText(projectName);
		if(runtime){
			botExt.button("New Runtime...").click();
			Thread.sleep(500);
			botExt.tree().expandNode("JBoss Community").select("JBoss 6.x Runtime");
			botExt.button("Next >").click();
			botExt.textWithLabel("Home Directory").setText(JBOSS6_AS_HOME);
			botExt.button("Finish").click();
		} else {
			botExt.comboBoxInGroup("Target runtime").setSelection("<None>");
		}
		botExt.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		Utils.waitForIdle();
		SWTBotTreeItem item = botExt.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		SWTBot shellProperties = botExt.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    botUtil.waitForAll();
	    Thread.sleep(500);
	    botExt.hyperlink("Further configuration required...").click();
	    botExt.button("OK").click();
	    botExt.button("OK").click();
	    botUtil.waitForAll();
		assertTrue("EJB project doesn't have maven nature",Utils.isMavenProject(projectName));
		updateConf(projectName);
		assertFalse("Project "+projectName+" have "+JSF_NATURE+" nature.",Utils.hasNature(projectName, JSF_NATURE));
		assertFalse("Project "+projectName+" have "+JAXRS_NATURE+" nature.",Utils.hasNature(projectName, JAXRS_NATURE));
		assertFalse("Project "+projectName+" have "+CDI_NATURE+" nature.",Utils.hasNature(projectName, CDI_NATURE));
		
	}
	
	private void addDependencies(String projectName, String groupId, String artifactId, String version) throws Exception{
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools", projectName,"0.0.1-SNAPSHOT");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(facade.getProject().getFile("pom.xml").getContents());
	    Element dependenciesElement = docPom.createElement("dependencies");
	    Element dependencyElement = docPom.createElement("dependency");
	    Element groupIdElement = docPom.createElement("groupId");  
	    Element artifactIdElement = docPom.createElement("artifactId");	    
	    Element versionElement = docPom.createElement("version");
	    
	    groupIdElement.setTextContent(groupId);
	    artifactIdElement.setTextContent(artifactId);
	    versionElement.setTextContent(version);
	    
	    Element root = docPom.getDocumentElement();
	    dependencyElement.appendChild(groupIdElement);
	    dependencyElement.appendChild(artifactIdElement);
	    dependencyElement.appendChild(versionElement);
	    dependenciesElement.appendChild(dependencyElement);
	    root.appendChild(dependenciesElement);
	    TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		StringWriter xmlAsWriter = new StringWriter(); 
		StreamResult result = new StreamResult(xmlAsWriter);
		DOMSource source = new DOMSource(docPom);
		trans.transform(source, result);
		facade.getProject().getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
		botUtil.waitForAll();
		updateConf(projectName);
	}
	
	private void removeFacets(String projectName) throws Exception{
		IProject facade = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertTrue(facade != null);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(facade.getProject().getFile(".project").getContents());
	    Element root = docPom.getDocumentElement();
	    Node natures = root.getElementsByTagName("natures").item(0);
	    NodeList natureChilds = natures.getChildNodes();
	    for(int i = 0; i<natureChilds.getLength()-1; i++){
	    	if(natureChilds.item(i).getTextContent().equals(JSF_NATURE) || 
	    	   natureChilds.item(i).getTextContent().equals(JAXRS_NATURE) || 
	    	   natureChilds.item(i).getTextContent().equals(CDI_NATURE)) {
	    		natures.removeChild(natureChilds.item(i));
	    	}
	    }
		updateConf(projectName);
	    
	}
	
	private void updateConf(String projectName){
		SWTBotTree innerBot = botExt.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot, "Maven","Update Project Configuration...");
		botExt.button("OK").click();
		botUtil.waitForAll();
	}
	
	private void addFacesConf(String projectName){
		SWTBotTree innerBot = botExt.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot, "New","Other...");
		botExt.tree().expandNode("JBoss Tools Web").expandNode("JSF").select("Faces Config");
		botExt.button("Next >").click();
		botExt.button("Finish").click();
		updateConf(projectName);
	}
	
	private void addServlet(String projectName, String servletName, String servletClass, String load) throws Exception{
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools", projectName,"0.0.1-SNAPSHOT");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document docPom = docBuilder.parse(facade.getProject().getFile("web.xml").getContents());
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
		facade.getProject().getFile("pom.xml").setContents(new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8")), 0, null);
		botUtil.waitForAll();
		updateConf(projectName);	
	}
}
