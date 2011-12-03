To build/test/iterate locally:

0. Run a web server locally, so you can access central/site/target/site/jbosstools-directory.xml via an http:// URL. You have many options for this. Here's one:

	su
	cd /tmp; wget -nc http://elonen.iki.fi/code/nanohttpd/NanoHTTPD.java
	javac NanoHTTPD.java; java NanoHTTPD -d /path/to/parent/folder/for/central/

1. edit central/plugins/org.jboss.tools.central/src/org/jboss/tools/central/configurators/DefaultJBossCentralConfigurator.java to set a new path for JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML:

	private static final String JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML = "http://localhost/central/site/target/site/jbosstools-directory.xml"; // for testing on localhost

2. build central/ folder:

	cd /path/to/central; mvn clean install

3. verify the directory.xml file is generated, and http-accessible:

	firefox http://localhost/central/site/target/site/jbosstools-directory.xml 

4. verify central/site/target/site/jbosstools-directory.xml points at the updated version of the o.j.t.central plugin in /path/to/central/site/target/site/plugins/

5. install updated o.j.t.central plugin into new Eclipse instance from http://localhost/central/site/target/site/ (or local file:// path if you prefer). Restart when prompted.

6. Change central/plugins/org.jboss.tools.central/plugin.xml, then repeat steps 2-5.


