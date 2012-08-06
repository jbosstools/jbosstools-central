package org.jboss.tools.maven.ui.bot.test;

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

@SuppressWarnings("restriction")
@Require(perspective="Java EE")
public class ConfiguratorsTest extends AbstractMavenSWTBotTest{
	
	public static final String PROJECT_NAME_JSF="testWEB_JSF";
	public static final String PROJECT_NAME_JAXRS="testWEB_JAXRS";
	public static final String PROJECT_NAME_CDI="testWEB_CDI";
	public static final String PROJECT_NAME_CDI_EJB="testEJB_CDI";
	public static final String JSF_NATURE="org.jboss.tools.jsf.jsfnature";
	public static final String JAXRS_NATURE="org.jboss.tools.ws.jaxrs.nature";
	public static final String CDI_NATURE="org.jboss.tools.cdi.core.cdinature";
	
	public static final String WEB_XML_LOCATION="/WebContent/WEB-INF/web.xml";
	public static final String JBOSS7_AS_HOME=System.getProperty("jbosstools.test.jboss.home.7.1");
//seam config, jpa config
	
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
	}

	//https://issues.jboss.org/browse/JBIDE-10468
	//https://issues.jboss.org/browse/JBIDE-10831
	@Test
	public void testJSFConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JSF+"_noRuntime", "com.sun.faces", "mojarra-jsf-api", "2.0.0-b04",null);
		updateConf(botUtil,PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" with mojarra dependency doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addFacesConf(PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" with faces config doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-10831
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addServlet(PROJECT_NAME_JSF+"_noRuntime","Faces Servlet","javax.faces.webapp.FacesServlet","1");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+"with servlet in web.xml doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		IProject facade = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" doesn't have faces-config.xml file",facade.getProject().getFile("faces-config.xml") != null);
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, true);
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-8755
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_seam", false);
		addDependencies(PROJECT_NAME_JSF+"_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_JSF+"_seam");
		assertTrue("Project "+PROJECT_NAME_JSF+"_seam"+" with seam-faces3 dependency doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_seam", JSF_NATURE));
		
		
		
	}
	
	
	@Test
	public void testCDIConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime", "javax.enterprise", "cdi-api","1.1.EDR1.2",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime"+" with cdi dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime", "javax.enterprise", "cdi-api","1.1.EDR1.2",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime"+" with cdi dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI, true);
		assertFalse("Project "+PROJECT_NAME_CDI+" has "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI, CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB, true);
		assertFalse("Project "+PROJECT_NAME_CDI_EJB+" has "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB, CDI_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-8755
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_seam"+" with seam-faces3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_seam"+" with seam-faces3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_seam", "org.jboss.seam.international", "seam-international", "3.0.0.Alpha1",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_seam"+" with seam3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_seam", CDI_NATURE));
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", "org.jboss.seam.international", "seam-international", "3.0.0.Alpha1",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_seam"+" with seam3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", "org.apache.deltaspike.core", "deltaspike-core-api", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api"+" with deltaspike-api dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", "org.apache.deltaspike.core", "deltaspike-core-api", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_deltaspike-api");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_deltaspike-api"+" with deltaspike-api dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", "org.apache.deltaspike.core", "deltaspike-core-impl", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl"+" with deltaspike-impl dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", "org.apache.deltaspike.core", "deltaspike-core-impl", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl"+" with deltaspike-impl dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", CDI_NATURE));
		
	}

	@Test
	public void testJAXRSConfigurator() throws Exception {
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JAXRS+"_noRuntime", "com.cedarsoft.rest", "jersey", "1.0.0",null);
		updateConf(botUtil,PROJECT_NAME_JAXRS+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JAXRS+"_noRuntime"+" with jersey dependency doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS+"_noRuntime", JAXRS_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JAXRS+"_noRuntime", "org.jboss.jbossas", "jboss-as-resteasy", "6.1.0.Final",null);
		updateConf(botUtil,PROJECT_NAME_JAXRS+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JAXRS+"_noRuntime"+" with resteasy dependency doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS+"_noRuntime", JAXRS_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS, true);
		assertTrue("Project "+PROJECT_NAME_JAXRS+" doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS, JAXRS_NATURE));
	}
	
	
	private void createMavenizedDynamicWebProject(String projectName, boolean runtime) throws Exception{
		bot.menu("File").menu("New").menu("Dynamic Web Project").click();
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
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.button("Next >").click();
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.button("Next >").click();
		waitForShell(botUtil, "New Dynamic Web Project");
		bot.checkBox("Generate web.xml deployment descriptor").select();
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		waitForIdle();
		SWTBotTreeItem item = bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
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
	
	private void createMavenizedEJBProject(String projectName, boolean runtime)throws Exception{
		bot.menu("File").menu("New").menu("EJB Project").click();
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
		SWTBotTreeItem item = bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
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
	
	private void addFacesConf(String projectName) throws InterruptedException{
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
	
	private void addServlet(String projectName, String servletName, String servletClass, String load) throws Exception{
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
