package org.jboss.tools.project.examples.model;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.Assert;
import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;
import org.jboss.tools.project.examples.internal.TokenizerUtil;

public abstract class AbstractProjectFix implements IProjectExamplesFix {

	protected ProjectExample project;

	protected RequirementModel requirement;
	
	public AbstractProjectFix(ProjectExample project, RequirementModel requirement) {
		Assert.isNotNull(requirement);
		this.project = project;
		this.requirement = requirement;
	}

	@Override
	public String getDescription() {
		return requirement.getProperties().get(RequirementModel.DESCRIPTION);
	}

	@Override
	public String getLabel() {
		return requirement.getType();
	}

	@Override
	public String getType() {
		return requirement.getType();
	}
	
	@Override
	public boolean isRequired() {
		return requirement.isRequired();
	}
	
	protected Collection<String> splitProperty(String propertyName) {
		String valueString = requirement.getProperties().get(propertyName);
		if (valueString == null) {
			return Collections.emptySet();
		}
		return TokenizerUtil.splitToSet(valueString);
	}
	
	public Collection<String> getConnectorIDs() {
		return splitProperty(RequirementModel.CONNECTOR_ID);
	}

	
}
