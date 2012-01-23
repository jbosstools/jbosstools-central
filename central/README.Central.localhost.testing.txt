To build/test locally:

0. Run a web server locally, so you can access central/site/target/site/jbosstools-directory.xml via an http:// URL. You have many options for this. Here's one:

	su
	cd /tmp; wget -nc http://elonen.iki.fi/code/nanohttpd/NanoHTTPD.java
	javac NanoHTTPD.java; java NanoHTTPD -d /path/to/parent/folder/for/central/

1. If you need to iterate through changes to the product/plugins/com.jboss.jbds.central plugin, you can do so, rebuild it, and reinstall it into a running JBDS instance.

   	Help > Install new > file:///path/to/product/site/target/site/

For example, you might want to edit central/plugins/org.jboss.tools.central/src/org/jboss/tools/central/configurators/DefaultJBossCentralConfigurator.java to set a new path for JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML:

	private static final String JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML = "http://localhost/central/site/target/site/jbosstools-directory.xml"; // for testing on localhost

2. Rebuild:

	cd /path/to/central; mvn clean install

3. Verify the directory.xml file is generated, and http-accessible:

	firefox http://localhost/central/site/target/site/jbosstools-directory.xml 

4. Verify central/site/target/site/jbosstools-directory.xml points at the updated version of the o.j.t.central plugin in /path/to/central/site/target/site/plugins/

	Note: if built locally, there should be no Jenkins/Hudson buildID Hxxx number in the version suffix, eg., 
		1.0.0.v20120120-1459-Beta1 
	instead of 
		1.0.0.v20120120-1459-H123-Beta1

5. Launch JBDS like this:

	./eclipse  -vmargs -Djboss.discovery.directory.url=file:///path/to/central/site/target/site/jbosstools-directory.xml
		or	
	./eclipse -vmargs  -Djboss.discovery.directory.url=http://localhost/central/site/target/site/jbosstools-directory.xml

6. Select 'Help > Jboss Central' (if not already loaded). Check the 'Software/Updates' tab to review contents of the discovery site (as per org.jboss.tools.central.discovery/plugin.xml). Hit the refresh icon if necessary to see changes.

--

To iterate through local changes:

7. Change product/plugins/org.jboss.tools.central.discovery/plugin.xml.

8. Repeat steps 2-6. If you rebuild the central plugin, you must reinstall it; the discovery plugin can simply be rebuilt and refreshed w/o needing to install anything new.

