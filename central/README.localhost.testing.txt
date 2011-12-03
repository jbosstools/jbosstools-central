To build/test/iterate locally:

1. Run a web server locally, so you can access central/site/target/site/jbosstools-directory.xml via an http:// URL

To run a server locally, you have many options. Here's one:

	su
	cd /tmp; wget -nc http://elonen.iki.fi/code/nanohttpd/NanoHTTPD.java
	javac NanoHTTPD.java; java NanoHTTPD -d /path/to/central/site/

1. edit central/plugins/org.jboss.tools.central/src/org/jboss/tools/central/configurators/DefaultJBossCentralConfigurator.java

	set a new path for JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML, pointing at http://localhost/target/site/jbosstools-directory.xml as per the example above

2. build in central/ folder using `mvn clean install`

4. verify the directory.xml file is generated, and http-accessible:

	firefox http://localhost/target/site/jbosstools-directory.xml 

5. verify central/site/target/site/jbosstools-directory.xml points at the updated version of the o.j.t.central plugin in /path/to/central/site/target/site/plugins/

6. install updated o.j.t.central plugin into new Eclipse instance from http://localhost/target/site/


