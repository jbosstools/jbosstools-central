package org.jboss.tools.maven.core;


import org.apache.maven.model.Plugin;
import org.maven.ide.components.pom.Build;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.PomFactory;
import org.maven.ide.eclipse.embedder.ProjectUpdater;

public class PluginUpdater extends ProjectUpdater {

	private static final PomFactory POM_FACTORY = PomFactory.eINSTANCE;
	private Plugin plugin;

    public PluginUpdater(Plugin plugin) {
      this.plugin = plugin;
    }

    public void update(Model model) {
    	Build build = model.getBuild();
        if(build==null) {
          build = POM_FACTORY.createBuild();
          model.setBuild(build);
        }
        org.maven.ide.components.pom.Plugin newPlugin = POM_FACTORY.createPlugin();
        newPlugin.setArtifactId(plugin.getArtifactId());
        newPlugin.setGroupId(plugin.getGroupId());
        newPlugin.setVersion(plugin.getVersion());
        newPlugin.setExtensions(plugin.getExtensions());
        // FIXME 
    }
  }
