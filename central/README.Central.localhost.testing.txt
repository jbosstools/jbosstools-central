To build/test locally:

0. Run a web server locally, so you can access central/site/target/site/jbosstools-directory.xml via an http:// URL. You have many options for this. Here's one:

	su
	cd /tmp; wget -nc http://elonen.iki.fi/code/nanohttpd/NanoHTTPD.java
	javac NanoHTTPD.java; java NanoHTTPD -d /path/to/parent/folder/for/central/

1. If you need to iterate through changes to the product/plugins/com.jboss.jbds.central plugin, you can do so, rebuild it, and reinstall it into a running Eclipse instance.

   	Help > Install new > file:///path/to/product/site/target/site/

You may also want to rebuild this plugin to set a different default URL (jboss.discovery.directory.url) for the directory.xml file:

	examples/plugins/org.jboss.tools.project.examples/pom.xml

2. Rebuild:

	cd /path/to/examples; mvn install -DskipTests -Dmaven.test.skip
	cd /path/to/central; mvn install -DskipTests -Dmaven.test.skip

3. Verify the directory.xml file is generated, and http-accessible:

	firefox http://localhost/central/site/target/site/jbosstools-directory.xml 

4. Verify central/site/target/site/jbosstools-directory.xml points at the updated version of the o.j.t.central plugin in /path/to/central/site/target/site/plugins/

	Note: if built locally, there should be no Jenkins/Hudson buildID Hxxx number in the version suffix, eg., 
		1.0.0.v20120120-1459-Beta1 
	instead of 
		1.0.0.v20120120-1459-H123-Beta1

5. Launch Eclipse like this:

	./eclipse  -vmargs -Djboss.discovery.directory.url=file:///path/to/central/site/target/site/jbosstools-directory.xml
		or	
	./eclipse -vmargs  -Djboss.discovery.directory.url=http://localhost/central/site/target/site/jbosstools-directory.xml
		or
	add the -Djboss.discovery.directory.url flag to your eclipse.ini file after the -vmargs line

6. Select 'Help > Jboss Central' (if not already loaded). Check the 'Software/Updates' tab to review contents of the discovery site (as per org.jboss.tools.central.discovery/plugin.xml). Hit the refresh icon if necessary to see changes.

--

To iterate through local changes:

7. Change product/plugins/org.jboss.tools.central.discovery/plugin.xml.

8. Repeat steps 2-6. If you rebuild the central plugin, you must reinstall it; the discovery plugin can simply be rebuilt and refreshed w/o needing to install anything new.

