JBoss Central has a "software/update" page which gets its content from a hardcoded URL, set in the org.jboss.tools.central plugin.

The published (current) milestone release URL is 

	http://download.jboss.org/jbosstools/updates/development/indigo/jbosstools-directory.xml

The stable branch (upcoming milestone) URL is

	http://download.jboss.org/jbosstools/updates/nightly/core/3.3.indigo/jbosstools-directory.xml

The unstable trunk URL is

	http://download.jboss.org/jbosstools/updates/nightly/core/trunk/jbosstools-directory.xml

The same as used for all milestone releases of JBoss Tools; should you want to pull the list of available updates from a NEWER directory file (eg., during a QE cycle when there's a milestone candidate available, but the published milestone site still contains the PREVIOUS milestone), you can do so using a commmandline flag.

Thus, when running Eclipse, simply pass in an alternate URL like this:

	./eclipse -vmargs -Djboss.discovery.directory.url=http://download.jboss.org/jbosstools/updates/nightly/core/3.3.indigo/jbosstools-directory.xml

Or, you can add the -Djboss.discovery.directory.url flag to your eclipse.ini file after the -vmargs line.

