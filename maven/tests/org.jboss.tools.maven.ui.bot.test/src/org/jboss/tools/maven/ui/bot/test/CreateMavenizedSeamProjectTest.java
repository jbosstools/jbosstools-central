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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.db.generic.IDBConnectionProfileConstants;
import org.eclipse.datatools.connectivity.db.generic.IDBDriverDefinitionConstants;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.models.TemplateDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.ResourcesUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.Parent;
import org.maven.ide.components.pom.util.PomResourceImpl;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.embedder.MavenModelManager;

/**
 * @author Snjeza
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateMavenizedSeamProjectTest {
	
	protected static final int IDLE_TIME = 60000;

	private static final String CONNECTION_PROFILE_NAME = "DefaultDS";

	private static final String SEAM_RUNTIME_NAME = "Seam 2.2";

	private static final String JBOSS_AS_RUNTIME_NAME = "JBoss AS 5.1 Runtime";

	public static final String PACKAGE_EXPLORER = "Package Explorer"; //$NON-NLS-1$
	
	private static final String JBOSS_AS_SERVER_NAME = "JBoss AS 5.1 Server";

	public static final String JBOSS_AS_HOST = "localhost"; //$NON-NLS-1$

	public static final String JBOSS_AS_DEFAULT_CONFIGURATION_NAME = "default"; //$NON-NLS-1$

	public static final String HSQL_DRIVER_DEFINITION_ID 
												= "DriverDefn.Hypersonic DB"; //$NON-NLS-1$

	public static final String HSQL_DRIVER_NAME = "Hypersonic DB"; //$NON-NLS-1$

	public static final String HSQL_DRIVER_TEMPLATE_ID 
						= "org.eclipse.datatools.enablement.hsqldb.1_8.driver"; //$NON-NLS-1$

	public static final String DTP_DB_URL_PROPERTY_ID 
								= "org.eclipse.datatools.connectivity.db.URL"; //$NON-NLS-1$
	
	public static final String HSQL_PROFILE_ID = "org.eclipse.datatools.enablement.hsqldb.connectionProfile";

	public static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home.5.1", "E:\\JBossRuntimes\\jboss-5.1.0.GA");

	public static final String JBOSS_AS_RUNTIME_ID = "org.jboss.ide.eclipse.as.runtime.51";
	
	public static final String JBOSS_AS_SERVER_ID = "org.jboss.ide.eclipse.as.51";
	
	public static final String SEAM_HOME_PROPERTY = System.getProperty("jbosstools.test.seam.2.0.1.GA.home", "E:\\JBossRuntimes\\jboss-seam-2.2.1.CR3");

	public static final String HSQLDB_DRIVER_JAR_NAME = "hsqldb.jar"; //$NON-NLS-1$
	
	public static final String HSQLDB_DRIVER_LOCATION = "/common/lib/" + HSQLDB_DRIVER_JAR_NAME; //$NON-NLS-1$
	
	public static final String PROJECT_NAME_WAR = "MavenizedSeamProjectWar";
	
	public static final String TEST_PROJECT_NAME_WAR = "MavenizedSeamProjectWar-test";
	
	public static final String PARENT_PROJECT_NAME_WAR = "MavenizedSeamProjectWar-parent";
	
	
	public static final String PROJECT_NAME = "MavenizedSeamProject";
	
	public static final String EAR_PROJECT_NAME = "MavenizedSeamProject-ear";
	
	public static final String EJB_PROJECT_NAME = "MavenizedSeamProject-ejb";
	
	public static final String TEST_PROJECT_NAME = "MavenizedSeamProject-test";
	
	public static final String PARENT_PROJECT_NAME = "MavenizedSeamProject-parent";
	
	public static final String DEPLOY_TYPE_EAR = "EAR";
	
	public static final String DEPLOY_TYPE_WAR = "WAR";
	
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public final static void beforeClass() throws Exception {
		initSWTBot();

		switchPerspective("org.jboss.tools.seam.ui.SeamPerspective");

		String asLocation = JBOSS_AS_HOME;
		
		String runtimeType = JBOSS_AS_RUNTIME_ID;
		String serverType = JBOSS_AS_SERVER_ID;
		
		createJBossServer(new File(asLocation), serverType, runtimeType, JBOSS_AS_SERVER_NAME, JBOSS_AS_RUNTIME_NAME);
		
		String seamPath = SEAM_HOME_PROPERTY;
		createSeamRuntime(SEAM_RUNTIME_NAME, seamPath, SeamVersion.SEAM_2_2);
		
		createDriver(asLocation, HSQLDB_DRIVER_LOCATION);
		
		activateSchell();
		
		createNewSeamWebProjectWizard(PROJECT_NAME, DEPLOY_TYPE_EAR);
		
		createNewSeamWebProjectWizard(PROJECT_NAME_WAR, DEPLOY_TYPE_WAR);
	}

	private static void initSWTBot() throws CoreException {
		bot = new SWTWorkbenchBot();
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 1000;
		SWTBotPreferences.PLAYBACK_DELAY = 5;
		JobUtils.waitForIdle(60000);
		try {
			SWTBotView view = bot.viewByTitle("Welcome");
			if (view != null) {
				view.close();
			}
		} catch (WidgetNotFoundException ignore) {
		}

		SWTBotShell[] shells = bot.shells();
		for (SWTBotShell shell : shells) {
			final Shell widget = shell.widget;
			Object parent = UIThreadRunnable.syncExec(shell.display,
					new Result<Object>() {
						public Object run() {
							return widget.isDisposed() ? null : widget.getParent();
						}
					});
			if (parent == null) {
				continue;
			}
			shell.close();
		}

		List<? extends SWTBotEditor> editors = bot.editors();
		for (SWTBotEditor e : editors) {
			e.close();
		}

		removeProjects();

		WorkbenchPlugin.getDefault().getPreferenceStore()
				.setValue(IPreferenceConstants.RUN_IN_BACKGROUND, true);

		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);
	}

	private static void removeProjects() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProject[] projects = workspace.getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					projects[i].delete(true, true, monitor);
				}
			}
		}, new NullProgressMonitor());
	}

	private static void removeServers() throws CoreException {
		IServer server = ServerCore.findServer(JBOSS_AS_SERVER_NAME);
		IServerWorkingCopy wc = server.createWorkingCopy();
		IModule[] modules = wc.getModules();
		IProgressMonitor monitor = new NullProgressMonitor();
		wc.modifyModules(new IModule[] {} , modules, monitor);
		wc.save(true, monitor);
		server.publish(IServer.PUBLISH_INCREMENTAL, monitor);
		JobUtils.waitForIdle(IDLE_TIME);
		server.getRuntime().delete();
		server.delete();
		JobUtils.waitForIdle(IDLE_TIME);
	}

	protected static void switchPerspective(final String pid) {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IPerspectiveRegistry perspectiveRegistry = workbench
						.getPerspectiveRegistry();
				IPerspectiveDescriptor perspective = perspectiveRegistry
						.findPerspectiveWithId(pid);
				workbench.getActiveWorkbenchWindow().getActivePage()
						.setPerspective(perspective);
			}
		});
	}
	
	@Before
    public void setUp() throws Exception {
        activateSchell();
    }

	private static void activateSchell() {
		UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
            	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
                        .forceActive();
            }
        });
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@AfterClass
	public final static void afterClass() throws Exception {
		boolean buildAutomatically = ResourcesUtils.setBuildAutomatically(false);
		ValidationFramework.getDefault().suspendAllValidation(true);
		try {
			removeServers();
			removeProjects();
		} finally {
			ResourcesUtils.setBuildAutomatically(buildAutomatically);
			ValidationFramework.getDefault().suspendAllValidation(false);
		}
		JobUtils.waitForIdle(IDLE_TIME);
	}
	
	protected static void createJBossServer(File asLocation, String serverType, String runtimeType, String name, String runtimeName) throws CoreException {
		if (!asLocation.isDirectory()) {
			return;
		}
		IPath jbossAsLocationPath = new Path(asLocation.getAbsolutePath());

		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			IRuntime runtime = servers[i].getRuntime();
			if(runtime != null && runtime.getLocation().equals(jbossAsLocationPath)) {
				return;
			}
		}

		IRuntime runtime = null;
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[0].getLocation().equals(jbossAsLocationPath)) {
				runtime = runtimes[0].createWorkingCopy();
				break;
			}
		}

		IProgressMonitor progressMonitor = new NullProgressMonitor();
		if (runtime == null) {
			runtime = createRuntime(runtimeName, asLocation.getAbsolutePath(), progressMonitor, runtimeType);
		}
		if (runtime != null) {
			createServer( runtime, serverType, name, progressMonitor);
		}
	}

	protected static IRuntime createRuntime(String runtimeName, String jbossASLocation, IProgressMonitor progressMonitor, String runtimeType) throws CoreException {
		IRuntimeWorkingCopy runtime = null;
		String type = null;
		String version = null;
		String runtimeId = null;
		IPath jbossAsLocationPath = new Path(jbossASLocation);
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(type, version, runtimeType);
		if (runtimeTypes.length > 0) {
			runtime = runtimeTypes[0].createRuntime(runtimeId, progressMonitor);
			runtime.setLocation(jbossAsLocationPath);
			if(runtimeName!=null) {
				runtime.setName(runtimeName);				
			}
			((RuntimeWorkingCopy) runtime).setAttribute("org.jboss.ide.eclipse.as.core.runtime.configurationName", JBOSS_AS_DEFAULT_CONFIGURATION_NAME); //$NON-NLS-1$

			return runtime.save(false, progressMonitor);
		}
		return runtime;
	}

	protected static void createDriver(String jbossASLocation, String driverLocation) throws ConnectionProfileException, IOException {
		if(ProfileManager.getInstance().getProfileByName(CONNECTION_PROFILE_NAME) != null) {
			return;
		}
		String driverPath = new File(jbossASLocation + driverLocation).getCanonicalPath(); //$NON-NLS-1$
		
		DriverInstance driver = DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
		if (driver == null) {
			TemplateDescriptor descr = TemplateDescriptor.getDriverTemplateDescriptor(HSQL_DRIVER_TEMPLATE_ID);
			IPropertySet instance = new PropertySetImpl(HSQL_DRIVER_NAME, HSQL_DRIVER_DEFINITION_ID);
			instance.setName(HSQL_DRIVER_NAME);
			instance.setID(HSQL_DRIVER_DEFINITION_ID);
			Properties props = new Properties();

			IConfigurationElement[] template = descr.getProperties();
			for (int i = 0; i < template.length; i++) {
				IConfigurationElement prop = template[i];
				String id = prop.getAttribute("id"); //$NON-NLS-1$

				String value = prop.getAttribute("value"); //$NON-NLS-1$
				props.setProperty(id, value == null ? "" : value); //$NON-NLS-1$
			}
			props.setProperty(DTP_DB_URL_PROPERTY_ID, "jdbc:hsqldb:."); //$NON-NLS-1$
			props.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, descr.getId());
			props.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, driverPath);

			instance.setBaseProperties(props);
			DriverManager.getInstance().removeDriverInstance(instance.getID());
			System.gc();
			DriverManager.getInstance().addDriverInstance(instance);
		}

		driver = DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
		if (driver != null && ProfileManager.getInstance().getProfileByName(CONNECTION_PROFILE_NAME) == null) { //$NON-NLS-1$
			// create profile
			Properties props = new Properties();
			props.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID, HSQL_DRIVER_DEFINITION_ID);
			props.setProperty(IDBConnectionProfileConstants.CONNECTION_PROPERTIES_PROP_ID, ""); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID,	driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_NAME_PROP_ID, "Default"); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.PASSWORD_PROP_ID, ""); //$NON-NLS-1$
			props.setProperty(IDBConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, "false"); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.URL_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.URL_PROP_ID));

			ProfileManager.getInstance().createProfile(CONNECTION_PROFILE_NAME,	"The JBoss AS Hypersonic embedded database", HSQL_PROFILE_ID, props, "", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
	}
	
	protected static IServerWorkingCopy createServer(IRuntime runtime, String runtimeType, String name, IProgressMonitor progressMonitor) throws CoreException {
		IServerType serverType = ServerCore.findServerType(runtimeType);
		IServerWorkingCopy server = serverType.createServer(null, null, runtime, progressMonitor);

		server.setHost(JBOSS_AS_HOST);
		server.setName(name);
		
		// JBossServer.DEPLOY_DIRECTORY
		String deployVal = runtime.getLocation().append("server").append(JBOSS_AS_DEFAULT_CONFIGURATION_NAME).append("deploy").toOSString(); //$NON-NLS-1$ //$NON-NLS-2$
		((ServerWorkingCopy) server).setAttribute("org.jboss.ide.eclipse.as.core.server.deployDirectory", deployVal); //$NON-NLS-1$

		// IDeployableServer.TEMP_DEPLOY_DIRECTORY
		String deployTmpFolderVal = runtime.getLocation().append("server").append(JBOSS_AS_DEFAULT_CONFIGURATION_NAME).append("tmp").append("jbosstoolsTemp").toOSString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		((ServerWorkingCopy) server).setAttribute("org.jboss.ide.eclipse.as.core.server.tempDeployDirectory", deployTmpFolderVal); //$NON-NLS-1$

		// If we'd need to set up a username / pw for JMX, do it here.
//		((ServerWorkingCopy)serverWC).setAttribute(JBossServer.SERVER_USERNAME, authUser);
//		((ServerWorkingCopy)serverWC).setAttribute(JBossServer.SERVER_PASSWORD, authPass);

		server.save(false, progressMonitor);
		return server;
	}

	protected static void createSeamRuntime(String name, String seamPath, SeamVersion seamVersion) {
		SeamRuntime seamRuntime = SeamRuntimeManager.getInstance().findRuntimeByName(name);
		if (seamRuntime != null) {
			return;
		}
		File seamFolder = new File(seamPath);
		if(seamFolder.exists() && seamFolder.isDirectory()) {
			SeamRuntime rt = new SeamRuntime();
			rt.setHomeDir(seamPath);
			rt.setName(name);
			rt.setDefault(true);
			rt.setVersion(seamVersion);
			SeamRuntimeManager.getInstance().addRuntime(rt);
		}
	}
	
	public static void createNewSeamWebProjectWizard(String projectName, String deployType) throws Exception {
		JobUtils.waitForIdle(60000);
		bot.menu("File").menu("New").menu("Seam Web Project").click();
		 
		SWTBotShell mainShell = bot.shell("New Seam Project");
		mainShell.activate();

		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Modify...").click();
		SWTBotShell shell = bot.shell("Project Facets");
		shell.activate();

		SWTBotTree treeWidget = bot.tree();
		SWTBotTreeItem jmi = treeWidget.getTreeItem("JBoss Maven Integration");
		jmi.check();
		
		bot.button("OK").click();
		
		mainShell.activate();
		
		bot.comboBox(0).setSelection(JBOSS_AS_RUNTIME_NAME);
		bot.comboBox(2).setSelection(JBOSS_AS_SERVER_NAME);
		
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.button("Next >").click();
		
		bot.comboBox(0).setSelection("Library Provided by Target Runtime");
		bot.button("Next >").click();
		
		bot.comboBox(0).setSelection(SEAM_RUNTIME_NAME);
		bot.radio(deployType).click();
		bot.comboBox(1).setSelection("HSQL");
		bot.comboBox(2).setSelection(CONNECTION_PROFILE_NAME);
		bot.button("Finish").click();
		
		JobUtils.waitForIdle(60000);
		
	}
	
	@Test
	public void testErrors() throws Exception {
		checkErrors(PROJECT_NAME);
		checkErrors(EAR_PROJECT_NAME);
		checkErrors(EJB_PROJECT_NAME);
		checkErrors(TEST_PROJECT_NAME);
		checkErrors(PARENT_PROJECT_NAME);
		checkErrors(PROJECT_NAME_WAR);
		checkErrors(TEST_PROJECT_NAME_WAR);
		checkErrors(PARENT_PROJECT_NAME_WAR);
	}

	private void checkErrors(String projectName) throws CoreException {
		List<IMarker> markers = new ArrayList<IMarker>();
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		IMarker[] projectMarkers = project.findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);
		for (int i = 0; i < projectMarkers.length; i++) {
			if (projectMarkers[i].getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
				markers.add(projectMarkers[i]);
			}
		}
		assertTrue("The '" + projectName + "' contains errors.", markers.size() == 0);
	}
	
	@Test
	public void testMavenProjects() throws Exception {
		isMavenProject(PROJECT_NAME);
		isMavenProject(EAR_PROJECT_NAME);
		isMavenProject(EJB_PROJECT_NAME);
		isMavenProject(TEST_PROJECT_NAME);
		isMavenProject(PARENT_PROJECT_NAME);
		isMavenProject(PROJECT_NAME_WAR);
		isMavenProject(TEST_PROJECT_NAME_WAR);
		isMavenProject(PARENT_PROJECT_NAME_WAR);
	}

	private void isMavenProject(String projectName) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertTrue("The '" + projectName + "' project isn't a Maven project.", project.hasNature(IMavenConstants.NATURE_ID));
	}

	// see https://jira.jboss.org/browse/JBIDE-6587
	@Test
	public void testMavenWarArchive() throws Exception {
		final SWTBotView packageExplorer = bot.viewByTitle(PACKAGE_EXPLORER);
		SWTBot innerBot = packageExplorer.bot();
		innerBot.activeShell().activate();
		final SWTBotTree tree = innerBot.tree();
		final SWTBotTreeItem warProjectItem = tree.getTreeItem(PROJECT_NAME_WAR);
		warProjectItem.select();
		
		SWTBotMenu runAs = tree.contextMenu("Run As");
		runAs.menu("6 Maven build...").click();

		SWTBotShell shell = bot.shell("Edit Configuration");
		shell.activate();
		
		bot.textWithLabel("Goals:").setText("clean package");
		bot.button("Run").click();
		
		JobUtils.waitForIdle(IDLE_TIME);
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME_WAR);
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IPath webInfPath = new Path("target/" + PROJECT_NAME_WAR + "-0.0.1-SNAPSHOT/WEB-INF");
		assertFalse(project.getFolder(webInfPath.append("src")).exists());
		assertFalse(project.getFolder(webInfPath.append("dev")).exists());
		assertTrue(project.getFolder(webInfPath.append("lib")).exists());
		
	}
	
	// see https://jira.jboss.org/browse/JBIDE-6767
	@Test
	public void testLibraries() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(EAR_PROJECT_NAME);
		File rootDirectory = new File(project.getLocation().toOSString(), "EarContent");
		String[] libs = rootDirectory.list(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				if (name.endsWith(".jar")) {
					return true;
				}
				return false;
			}
		});
		assertTrue(libs.length == 0);
		File libDirectory = new File (rootDirectory,"lib");
		if (libDirectory.isDirectory()) {
			libs = libDirectory.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.endsWith(".jar")) {
						return true;
					}
					return false;
				}
			});
			assertTrue(libs.length == 0);
		}
	}
	
}
