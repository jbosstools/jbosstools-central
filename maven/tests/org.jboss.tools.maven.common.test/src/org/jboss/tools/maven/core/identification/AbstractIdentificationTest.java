package org.jboss.tools.maven.core.identification;

import java.io.File;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractIdentificationTest {

	protected File arquillian;//Has maven properties
	protected File junit;//Has no maven properties
	protected File jansi;//Has multiple maven properties
	
	@Before
	public void initFiles() {
		junit = new File("resources/junit_4_10.jar");
		arquillian = new File("resources/arquillian-core-spi.jar");
		jansi = new File("resources/jansi-1.6.jar");
	}
	
	@After
	public void disposeFiles() {
		junit = null;
		arquillian = null;
		jansi = null;
	}
	
	
}
