package org.jboss.tools.maven.gwt;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.jboss.tools.maven.ui.Activator;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.eclipse.core.modules.IModule;
import com.google.gwt.eclipse.core.modules.ModuleUtils;
import com.google.gwt.eclipse.core.properties.GWTProjectProperties;

public class GWTProjectConfigurator extends AbstractProjectConfigurator {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractProjectConfigurator.class);

	public static final String CODEHAUS_GROUP_ID = "org.codehaus.mojo";
	public static final String GWT_MAVEN_PLUGIN_ARTIFACT_ID = "gwt-maven-plugin";
	
	@Override
	public void configure(ProjectConfigurationRequest arg0, IProgressMonitor arg1) throws CoreException {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureGWT = store.getBoolean(Activator.CONFIGURE_GWT);
		log.debug("GWT Entry Point Modules configuration is {}",configureGWT ? "enabled" : "disabled");
		if(configureGWT) {
			List<Plugin> plugins = arg0.getMavenProjectFacade().getMavenProject().getBuildPlugins();
			for (Plugin plugin : plugins) {
				if(CODEHAUS_GROUP_ID.equals(plugin.getGroupId()) && GWT_MAVEN_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
					log.debug("Configure Entry Point Modules for GWT Project {}", arg0.getProject().getName());
					IModule[] modules = ModuleUtils.findAllModules(JavaCore.create(arg0.getProject()),false);
					List<String> modNames = new ArrayList<String>();
					for (IModule iModule : modules) {
						modNames.add(iModule.getQualifiedName());
						log.debug("\t {}",iModule.getQualifiedName());
					}
					try {
						GWTProjectProperties.setEntryPointModules(arg0.getProject(), modNames);
					} catch (BackingStoreException e) {
						log.error("Ecseption in Maven GWT Configurator", e);
					}
					break;
				}
			}
		}
	}
}
