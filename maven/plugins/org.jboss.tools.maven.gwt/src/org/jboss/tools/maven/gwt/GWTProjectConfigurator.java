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

	public static final String GWT_WAR_MAVEN_PLUGIN_KEY = "org.apache.maven.plugins:maven-war-plugin";
	
	@Override
	public void configure(ProjectConfigurationRequest projectConfig, IProgressMonitor monitor) throws CoreException {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureGWT = store.getBoolean(Activator.CONFIGURE_GWT);
		log.debug("GWT Entry Point Modules configuration is {}",configureGWT ? "enabled" : "disabled");
		if(configureGWT && projectConfig.getMavenProject().getPlugin(GWT_WAR_MAVEN_PLUGIN_KEY)!=null) {
			log.debug("Configure Entry Point Modules for GWT Project {}", projectConfig.getProject().getName());
			IModule[] modules = ModuleUtils.findAllModules(JavaCore.create(projectConfig.getProject()),false);
			List<String> modNames = new ArrayList<String>();
			for (IModule iModule : modules) {
				modNames.add(iModule.getQualifiedName());
				log.debug("\t {}",iModule.getQualifiedName());
			}
			try {
				GWTProjectProperties.setEntryPointModules(projectConfig.getProject(), modNames);
			} catch (BackingStoreException e) {
				log.error("Ecseption in Maven GWT Configurator", e);
			}
		}
	}
}
