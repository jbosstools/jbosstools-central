package org.jboss.tools.maven.conversion.ui.internal;

import org.jboss.tools.maven.conversion.core.internal.ComponentDependencyCollector;

public class ComponentReferenceConversionParticipant extends AbstractReferenceConversionParticipant {

	public ComponentReferenceConversionParticipant() {
		super(new ComponentDependencyCollector());
	}

}
