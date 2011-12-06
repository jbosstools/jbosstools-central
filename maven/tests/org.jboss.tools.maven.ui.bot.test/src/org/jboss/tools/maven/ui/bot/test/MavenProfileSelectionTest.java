package org.jboss.tools.maven.ui.bot.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenProfileSelectionTest extends AbstractMavenSWTBotTest {
	
	public static final String AUTOACTIVATED_PROFILE_IN_POM = "active-profile";
	public static final String AUTOACTIVATED_PROFILE_IN_USER_SETTINGS = "jboss.repository";
	
	/*
	@BeforeClass
	public static void setUpUserSettings() throws InterruptedException, IOException, SAXException, ParserConfigurationException, TransformerException{
		SWTBotExt bot = new SWTBotExt();
		bot.menu("Window").menu("Preferences").click();
		bot.tree().expandNode("Maven").select("User Settings");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		String settingsLocation = bot.text(1).getText();
		File settings = null;
		if(settingsLocation.equals("User settings file doesn't exist")){
			settingsLocation = bot.text(2).getText();
			settings = new File(settingsLocation);
			settings.createNewFile();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("settings");
			doc.appendChild(rootElement);
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(settingsLocation); 
			transformer.transform(source, result);
		}
		settings = new File(settingsLocation);
		Document docPom = docBuilder.parse(settings);
		Element rootElement = docPom.getDocumentElement();
		Element profilesElement = docPom.createElement("profiles");
		Element profileElement = docPom.createElement("profile");
		Element idElement = docPom.createElement("id");
		Element activationElement = docPom.createElement("activation");
		Element activeByDefaultElement = docPom.createElement("activeByDefault");
		
		idElement.setTextContent(AUTOACTIVATED_PROFILE_IN_USER_SETTINGS);
		activeByDefaultElement.setTextContent("true");
		
		activationElement.appendChild(activeByDefaultElement);
		profileElement.appendChild(activationElement);
		profileElement.appendChild(idElement);
		profilesElement.appendChild(profileElement);
		rootElement.appendChild(profilesElement);
		DOMSource source = new DOMSource(docPom);
		StreamResult result = new StreamResult(settingsLocation); 
		transformer.transform(source, result);
	}
	*/
	
	@Test
	public void testOpenMavenProfiles() throws Exception {
		IProject project = importProject("projects/simple-jar/pom.xml");
		waitForJobsToComplete();
		testAutoActivatedProfiles();
		final SWTBotView packageExplorer = bot.viewByTitle("Project Explorer");
		SWTBot innerBot = packageExplorer.bot();
		innerBot.activeShell().activate();
		SWTBotTree tree = innerBot.tree();
		SWTBotTreeItem projectItem = tree.getTreeItem(project.getName());
		projectItem.select();
		openProfilesDialog(projectItem);
		Thread.sleep(2000);
	    //activate all profiles
		SWTBot shell = bot.shell("Select Maven profiles").activate().bot();
		shell.button("Select All").click();
	    String selectedProfiles = shell.textWithLabel("Active profiles for simple-jar :").getText();
	    shell.button("OK").click();
	   
	    testActivatedProfiles(project.getName(), selectedProfiles);
	    Thread.sleep(1000);
	    openProfilesDialog(projectItem);
	    
	    //disable all profiles
	    shell = bot.shell("Select Maven profiles").activate().bot();
	    shell.button("Deselect all").click();
	    selectedProfiles = bot.textWithLabel("Active profiles for simple-jar :").getText();
	    bot.button("OK").click();
	    
	    testActivatedProfiles(project.getName(), selectedProfiles);
	}
	
	private void openProfilesDialog(SWTBotTreeItem projectItem) throws ParseException, InterruptedException{
		projectItem.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		//projectItem.pressShortcut(Keystrokes.DOWN);
		//projectItem.pressShortcut(Keystrokes.LF);
		final SWTBotShell selectDialogShell = bot.shell("Select Maven profiles");
	    assertEquals("Select Maven profiles", selectDialogShell.getText());
	    Thread.sleep(1000);
	}
	
	private void testActivatedProfiles(String projectName, String profilesToCheck){
		
	    SWTBot explorer = bot.viewByTitle("Project Explorer").bot();
	    SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
	    item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
	    SWTBot shell = bot.shell("Properties for "+projectName).activate().bot();
	    shell.tree().select("Maven");
	    assertEquals("Selected profiles doesn't match", shell.textWithLabel("Active Maven Profiles (comma separated):").getText(),profilesToCheck);
	    shell.button("OK").click();
	    
	}
	
	private void testAutoActivatedProfiles(){
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", "simple-jar", "1.0.0-SNAPSHOT");
	    assertNotNull("facade is null",facade);
	    facade.getMavenProject().getActiveProfiles().get(0);
	    assertEquals("Auto Activated profiles from pom.xml doesn't match", AUTOACTIVATED_PROFILE_IN_POM, facade.getMavenProject().getActiveProfiles().get(0).getId());
	    assertEquals("Auto Activated profiles from settings.xml doesn't match", AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, facade.getMavenProject().getActiveProfiles().get(1).getId());
	}
}
