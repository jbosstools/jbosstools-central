/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
@Grab(group='org.jboss.jdf', module='stacks-client', version='1.0.1.Final')
@Grab(group='commons-io', module='commons-io', version='2.4')
//jcommander is included in the groovy distro by accident, better make sure we express a direct dependency here
@Grab(group='com.beust', module='jcommander', version='1.30')

import org.apache.commons.io.FileUtils
import static groovyx.gpars.GParsPool.*
import static groovy.io.FileType.*
import com.beust.jcommander.*
import org.jboss.jdf.stacks.client.*
import org.jboss.jdf.stacks.model.*
import java.util.concurrent.TimeUnit

class GoOfflineScript {
  @Parameter(description = "<descriptor url list>")
  def descriptors = [];

  @Parameter(names =["-c", "--clean"], description = "Clean offline directory")
  boolean clean = false

  @Parameter(names =["-q", "--quiet"], description = "Quiet mode (reduced console output)")
  boolean quiet = false

  @Parameter(names =["-e", "--enterprise"], description = "Cache enterprise dependency")
  boolean enterprise = false

  @Parameter(names =["-od", "--offline-dir"], description = "Base offline directory")
  File offlineDir = new File("offline")

  /*@Parameter(names =["-i", "--interactive"], description = "Interactive mode")
  boolean interactive = false

  @Parameter(names =["-fcd", "--final-cache-dir"], description = "Final cache directory used by JBoss Tools/JBDS")
  File finalCacheDir = new File(System.getProperty("user.home"), ".jbosstools/cache")

  @Parameter(names =["-lmr", "--local-maven-repo"], description = "Local Maven repository")
  File localMavenRepoDir = new File(System.getProperty("user.home"), ".m2/repository")
  */
  @Parameter(names = ["-h", "--help"], description = "This help", help = true)
  boolean help;

  @Parameter(names =["-s", "--settings"], description = "Path to custom Maven settings.xml")
  File settings = null

  boolean forceMavenResourcePluginResolution = true //on some OS/maven combos, m-r-p:2.5 is not resolved. It should.

  def buildErrors = [:]

  def enterpriseArchetypes = ["org.jboss.aerogear.archetypes:jboss-html5-mobile-archetype:7.1.3.Final",
                              "org.jboss.spec.archetypes:jboss-javaee6-webapp-blank-archetype:7.1.3.Final",
                              "org.jboss.spec.archetypes:jboss-javaee6-webapp-archetype:7.1.3.Final",
                              "org.richfaces.archetypes:richfaces-archetype-kitchensink:4.2.3.Final-2"
                              ]

  public static main(args) {
    def script = new GoOfflineScript()
    def cmd = new JCommander();
    try {
      cmd.addObject(script);
      cmd.parse(args)
    } catch (ParameterException e) {
      println e.getLocalizedMessage()
      cmd.usage();
      return
    }

    if (script.help) {
      cmd.usage();
      return
    }
    println "Quiet mode : "+script.quiet
    script.goOffline()
  }

  def goOffline (args) {

    if (settings) {
      if (!settings.exists()) {
        throw new IllegalArgumentException("${settings.absolutePath} is not a valid settings.xml path")
      } else {
        println "Using settings from ${settings.absolutePath}"
      }
    }

    println "Descriptors : "+ descriptors
    long start = System.currentTimeMillis()

    if (clean && offlineDir.exists()) {
      println "deleting existing $offlineDir"
      if (!offlineDir.deleteDir()) {
        throw new IOException("Could not delete $offlineDir")
      }
    }

    def downloadDir = new File(offlineDir, ".jbosstools/cache")

    println "creating $downloadDir"
    downloadDir.mkdirs()

    //This is the directory where examples will be unzipped to be built
    def workDir = new File(offlineDir, "workDir")
    workDir.mkdirs()

    def allArchetypeProjects= []

    descriptors.each { descriptorUrl ->
      def archetypeProjects = downloadExamples(descriptorUrl, downloadDir, workDir)
      if (archetypeProjects) allArchetypeProjects.addAll archetypeProjects
    }

    def mavenRepoDir = new File(offlineDir, ".m2/repository")

    buildExamplesDir(workDir, mavenRepoDir)

    buildArchetypesFromExamples(allArchetypeProjects, workDir, mavenRepoDir)

    buildArchetypesFromStacks(workDir, mavenRepoDir)

    println 'Cleaning up installed artifacts created from archetypes'
    if (mavenRepoDir) {
      def installedArtifactfolder = new File(mavenRepoDir, "org/jbosstoolsofflineexamples")
      if (installedArtifactfolder.exists() && installedArtifactfolder.isDirectory()) {
        installedArtifactfolder.deleteDir()
      }
    }

    long elapsed = System.currentTimeMillis() -start

    def duration = String.format("%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(elapsed),
        TimeUnit.MILLISECONDS.toSeconds(elapsed) -
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
    );

    println "Script executed in $duration with ${buildErrors.size()} error(s)"

    if (buildErrors.size()) {
      def msg = ""
      buildErrors.each {
        msg += '\r\n\t'+it.key
      }
      throw new ScriptProblems(msg)
    } 
  }


////////////////////////////////////////////////////////////////////////////////////////////////////
  def downloadExamples(descriptorUrl, downloadArea, workDir) {
    if (!descriptorUrl) {
     return
    }
    //download descriptor
    println "parsing $descriptorUrl"
    def descrUrl = new URL(descriptorUrl)
    def localDescriptor = new File(downloadArea, descrUrl.getFile())
    //Descriptors are cheap to download/update
    FileUtils.copyURLToFile(descrUrl, localDescriptor)

    def root = new XmlSlurper(false,false).parse(localDescriptor)

    //foreach example in descriptor
    def projects = root.project

    if (projects.size() == 0) {
      return null
    }

    def archetypeProjects = [] as java.util.concurrent.CopyOnWriteArrayList

    withPool(4) {
      projects.eachParallel { p ->
        if ("mavenArchetype" == p.importType.text()) {
             archetypeProjects << p
             return archetypeProjects
        }
        String sUrl = p.url?.text().trim()
        if (!sUrl) {
             return archetypeProjects
        }

        URL url = new URL(sUrl)
        def zip = new File(downloadArea, url.getFile())
        //println "Starting download of $url"
        if (!zip.exists()) {
          FileUtils.copyURLToFile(url, zip)
          def totalSize = FileUtils.byteCountToDisplaySize(zip.size())

          println "Downloaded $url ($totalSize) to $zip"
        }

        if ("maven" == p.importType.text()) {
          def ant = new AntBuilder()   // create an antbuilder
          ant.unzip(  src: zip, dest: new File(workDir, zip.getName()),  overwrite:"false")
        }
      }
    }

    archetypeProjects
  }

  def buildExamplesDir(workDir, localRepo) {
    workDir.eachFileMatch DIRECTORIES, ~/.*\.zip/, { unzipped ->
         execMavenGoOffline(unzipped, localRepo)
    }
  }


  def buildArchetypesFromExamples(projects, workDir, localRepo) {
    def archetypes= projects.findAll{!(it.stacksId.text() || it.stacksType.text())}

    archetypes.each{p ->
      File folder = new File(workDir, p.mavenArchetype.archetypeArtifactId.text())
      if (folder.exists()) {
        folder.deleteDir()
      }
      folder.mkdirs()
      def pid = p.mavenArchetype.archetypeGroupId.text()
      def aid = p.mavenArchetype.archetypeArtifactId.text()
      def v = p.mavenArchetype.archetypeVersion.text()

      def appName = "myapp."+aid
      execMavenArchetypeBuild (pid, aid, v, folder, localRepo, appName)
      execMavenGoOffline(new File(folder, appName), localRepo)

      def gav = pid + ":"+ aid+ ":"+ v
      //Only build with enterprise flag when necessary
      if (enterprise && enterpriseArchetypes.contains(gav)){
        appName += "-enterprise"
        execMavenArchetypeBuild (pid, aid, v, folder, localRepo, appName)
        execMavenGoOffline(new File(folder, appName), localRepo)
      }
    }
  }

  def buildArchetypesFromStacks(workDir, localRepo) {
    def config = new DefaultStacksClientConfiguration(cacheRefreshPeriodSeconds:1)
    Stacks stacks = new StacksClient(config).getStacks();
    stacks.getAvailableArchetypeVersions().each { av ->
      def a = av.archetype

      File folder = new File(workDir, a.artifactId)
      if (folder.exists()) {
        folder.deleteDir()
      }
      folder.mkdirs()

      def appName = "my-${a.artifactId}"
      execMavenArchetypeBuild (a.groupId, a.artifactId, av.version, folder, localRepo, appName)
      execMavenGoOffline(new File(folder, appName), localRepo)

      def gav = a.groupId + ":"+ a.artifactId+ ":"+ av.version
      //Only build with enterprise flag when necessary
      if (enterprise && enterpriseArchetypes.contains(gav)){


        appName += "-enterprise"
        execMavenArchetypeBuild (a.groupId, a.artifactId, a.recommendedVersion, folder, localRepo, appName)
        execMavenGoOffline(new File(folder, appName), localRepo)
      }
    }
  }

  def execMavenGoOffline (def rootDirectory, def localRepo) {
    def directory = rootDirectory
    def pom = new File(directory, "pom.xml")

    if (!pom.exists() && rootDirectory.exists()) {
       //GateIn examples have their pom.xml one folder down
      rootDirectory.traverse(maxDepth:1) {
        if (it.isDirectory()) {
          pom = new File(it, "pom.xml")
          if (pom.exists()) {
            directory = it
            return groovy.io.FileVisitResult.TERMINATE
          }
        }
      }
    }
    if (!pom.exists()) {
       println "${directory}/pom.xml can't be found. Skipping maven build"
       return
    }

    def pomModel = new XmlSlurper(false,false).parse(pom)
    def profiles = pomModel?.profiles?.profile?.id.collect{ it.text()}.findAll{!it.startsWith("aerogearci")}.join(",")

    def name = pomModel.name.text().toLowerCase()

    //errai has borked profiles
    if (name.contains("errai")) {
      profiles = profiles.replace(",arq-jbossas-managed","").replace(",arq-jbossas-remote","")
    }

    //richfaces-archetype-kitchensink 4.5.x has a borked debug profile
    else if (name.contains("richfaces-archetype-kitchensink")) {
      profiles = profiles.replace(",debug","")
    }

    //contacts-mobile-basic has a borked minify profile
    else if(name.contains("html5-mobile-blank-archetype")) {
      profiles = profiles.replace(",minify","")
    }

    //kitchensink-backbone has a borked profiles
    else if (name.contains("kitchensink-backbone")) {
      profiles = profiles.replace(",minify","")
    }

    addMavenWrapper(directory, localRepo)

     //"arq-jbossas-remote" can't be combined with other arquillian profiles, it would bork dependency resolution
     //so we execute 2 builds. with and without arq-jbossas-remote
     if (profiles.contains("arq-jbossas-remote")) {
       execMavenGoOfflineForProfiles (directory, localRepo, pomModel, profiles.replace(",arq-jbossas-remote",""))
       execMavenGoOfflineForProfiles (directory, localRepo, pomModel, "arq-jbossas-remote")
     } else {
       execMavenGoOfflineForProfiles (directory, localRepo, pomModel, profiles)
     }


  }

  def execMavenGoOfflineForProfiles (def directory, def localRepo, def pomModel, def profiles) {
    def ant = new AntBuilder()

    println "Building ${pomModel.groupId}:${pomModel.artifactId}:${pomModel.version} " + profiles?:"with profiles $profiles"

    //remove [exec] prefixes
    def logger = ant.project.buildListeners.find { it instanceof org.apache.tools.ant.DefaultLogger }
    logger.emacsMode = true
    if (!quiet) {
      logger.setMessageOutputLevel(3)
    }
    
    def ultimateGoal = "install"

    if (pomModel.groupId.text() == "org.jboss.resteasy.examples" && pomModel.artifactId.text() == "simple") {
      //this example has non-skippable ITs, and they fail because jetty is not properly configured!!!
      //So we don't go the whole 9 yards
      ultimateGoal = "package"
    }

    ant.exec(errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "false",
             dir: directory,
             executable: getMavenExec()) {
                if (settings) {
                  arg(value:"-s")
                  arg(value:"${settings.absolutePath}")
                }
                arg(value:"-B")
                if (quiet) arg(value:"-q")
                arg(value:"clean")
                if ("pom" != pomModel.packaging.text()) arg(value:"dependency:go-offline")
                if (forceMavenResourcePluginResolution) arg(value:"org.apache.maven.plugins:maven-resources-plugin:2.5:resources")
                arg(value:ultimateGoal)
                arg(value:"-DskipTests=true")
                if (profiles)  arg(value:"-P$profiles")
                if (localRepo) arg(value:"-Dmaven.repo.local=${localRepo.absolutePath}")
                if(directory.toString().contains("jboss-html5-mobile-archetype")) {
                   arg(value:"-Dversion.jboss.as=7.1.1.Final")//For broken aerogear archetype
                }
                if(directory.toString().contains("jboss-as-kitchensink-html5-mobile.zip")) {
                   arg(value:"-Dversion.org.jboss.as=7.1.1.Final")//For broken html5 quickstart
                }
             }

      if (localRepo) {
        def installedArtifactfolder = new File(localRepo, pomModel.groupId.text().replace('.',File.separator)+File.separator+pomModel.artifactId.text()+File.separator+pomModel.version.text())
        if (installedArtifactfolder.exists() && installedArtifactfolder.isDirectory()) {
          installedArtifactfolder.deleteDir()
        }
      }


      if(ant.project.properties.cmdExit != "0"){
        buildErrors["Project $directory failed to build"] = ant.project.properties.cmdErr
      } else {
        forceMavenResourcePluginResolution = false
      }
      ant.project.properties.cmdExit
  }

  String getMavenExec()  {
    def mvnFileName="./mvnw"
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
      mvnFileName+=".bat"
    }
    mvnFileName
  }

  def execMavenArchetypeBuild (groupId, artifactId, version, directory, localRepo, appName) {

    addMavenWrapper(directory, localRepo)
    
    def ant = new AntBuilder()
    //remove [exec] prefixes
    def logger = ant.project.buildListeners.find { it instanceof org.apache.tools.ant.DefaultLogger }
    logger.emacsMode = true
    if (!quiet) {
      logger.setMessageOutputLevel(3)
    }
  
    ant.exec(errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "false",
             dir: directory,
             executable: getMavenExec()) {
                arg(value:"archetype:generate")
                if (settings) {
                  arg(value:"-s")
                  arg(value:"${settings.absolutePath}")
                }
                if (quiet) arg(value:"-q")
                arg(value:"-B")
                arg(value:"-DarchetypeGroupId=${groupId}")
                arg(value:"-DarchetypeArtifactId=${artifactId}")
                arg(value:"-DarchetypeVersion=${version}")
                arg(value:"-DgroupId=org.jbosstoolsofflineexamples")
                arg(value:"-DartifactId=${appName}")
                arg(value:"-DinteractiveMode=false")
                arg(value:"-Dversion=1.0.0-SNAPSHOT")
                if (appName.endsWith("-enterprise")) arg(value:"-Denterprise=true")
                if (localRepo) arg(value:"-Dmaven.repo.local=${localRepo.absolutePath}")

             }

      if(ant.project.properties.cmdExit != "0"){
        buildErrors["Failed to generate project ${appName} from archetype ${groupId}:${artifactId}:${version}"] = ant.project.properties.cmdErr
      }
      ant.project.properties.cmdExit
  }

  def addMavenWrapper(directory, localRepo) {
    def ant = new AntBuilder()
    //remove [exec] prefixes
    def logger = ant.project.buildListeners.find { it instanceof org.apache.tools.ant.DefaultLogger }
    logger.emacsMode = true
    if (!quiet) {
      logger.setMessageOutputLevel(3)
    }
  
    ant.exec(errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "true",
             dir: directory,
             executable: "mvn") {
                arg(value:"io.takari:maven:wrapper")
                 arg(value:"-N")
                if (settings) {
                  arg(value:"-s")
                  arg(value:"${settings.absolutePath}")
                }
                if (quiet) arg(value:"-q")
                if (localRepo) arg(value:"-Dmaven.repo.local=${localRepo.absolutePath}")
             }

    if(ant.project.properties.cmdExit != "0"){
        buildErrors["Failed to add the maven wrapper to "+directory] = ant.project.properties.cmdErr
    }
    ant.project.properties.cmdExit
  }


}



class ScriptProblems extends RuntimeException {

  boolean displayMessage;

  public ScriptProblems(String msg) {
    super(msg)
  }

  public synchronized Throwable fillInStackTrace() {
    this
  }

  public String getMessage() {
    if (!displayMessage) {
      displayMessage = true
      return "Some build errors occured"
    }
    super.getMessage()
  }
}
