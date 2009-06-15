package org.jboss.tools.maven.seam;

import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.Parent;
import org.maven.ide.components.pom.PomFactory;
import org.maven.ide.eclipse.embedder.ProjectUpdater;

public class ParentAdder extends ProjectUpdater {

	private static final PomFactory POM_FACTORY = PomFactory.eINSTANCE;
    private final String groupId;
    private final String artifactId;
    private final String version;

    public ParentAdder(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
    }

    public void update(Model model) {
      Parent parent = model.getParent();
      if(parent==null) {
        parent = POM_FACTORY.createParent();
        parent.setArtifactId(artifactId);
        parent.setGroupId(groupId);
        parent.setVersion(version);
        model.setParent(parent);
      }
    }
  }