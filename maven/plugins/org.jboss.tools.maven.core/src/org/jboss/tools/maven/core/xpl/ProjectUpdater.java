package org.jboss.tools.maven.core.xpl;

import org.eclipse.m2e.model.edit.pom.Model;

/**
 * Project updater
 *
 * @author Eugene Kuleshov
 */
public abstract class ProjectUpdater {

  public abstract void update(Model model);

}
