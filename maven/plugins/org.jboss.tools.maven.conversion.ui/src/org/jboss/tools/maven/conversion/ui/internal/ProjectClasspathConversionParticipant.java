package org.jboss.tools.maven.conversion.ui.internal;

import org.jboss.tools.maven.conversion.core.internal.JavaDependencyCollector;

public class ProjectClasspathConversionParticipant extends AbstractReferenceConversionParticipant {

	public ProjectClasspathConversionParticipant() {
		super(new JavaDependencyCollector());
	}

}
