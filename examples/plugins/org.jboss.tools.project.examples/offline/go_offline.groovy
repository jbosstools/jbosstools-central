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
import org.jboss.jdf.stacks.client.StacksClient
import org.jboss.jdf.stacks.model.*

class GoOfflineScript {
  @Parameter(description = "<descriptor url list>")
  def descriptors = ["http://download.jboss.org/jbosstools/examples/project-examples-maven-4.1.Alpha2.xml", 
                     "http://download.jboss.org/jbosstools/examples/project-examples-community-4.1.Alpha2.xml"];

  @Parameter(names =["-c", "--clean"], description = "Clean offline directory")
  boolean clean = false

  @Parameter(names =["-q", "--quiet"], description = "Quiet mode (reduced console output)")
  boolean quiet = false

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
    script.goOffline()
  }

  def goOffline (args) {
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

    descriptors.each { descriptorUrl -> 
      downloadExamples(descriptorUrl, downloadDir, workDir)
    }

    def mavenRepoDir = new File(offlineDir, ".m2/repository")

    buildExamplesDir(workDir, mavenRepoDir)

    buildArchetypesFromStacks(workDir, mavenRepoDir)

    /*FIXME add interactive support
    boolean copyCache = false
    boolean copyMaven = false
    if (copyCache && downloadDir.exists()) {
      println "Copy $downloadDir to $finalCacheDir"
      FileUtils.copyDirectory(downloadDir, finalCacheDir)
    }
    if (copyMaven && mavenRepoDir.exists()) {
      FileUtils.copyDirectory(mavenRepoDir, localMavenRepoDir)
    } 
    */   
    long elapsed = System.currentTimeMillis() -start

    println "Script executed in $elapsed ms"
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
      return
    }
  
    withPool(4) {
      projects.eachParallel { p ->
        String sUrl = p.url?.text().trim()
        if (!sUrl) {
          return
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
  }

  def buildExamplesDir(workDir, localRepo) {
    workDir.eachFileMatch DIRECTORIES, ~/.*\.zip/, { unzipped ->
         execMavenGoOffline(unzipped, localRepo)    
    }
  }

  def buildArchetypesFromStacks(workDir, localRepo) {
    Stacks stacks = new StacksClient().getStacks();
    stacks.getAvailableArchetypes().each { a ->
      File folder = new File(workDir, a.artifactId)
      if (folder.exists()) {
        folder.deleteDir()
      }
      folder.mkdirs()
      execMavenArchetypeBuild (a.groupId, a.artifactId, a.recommendedVersion, folder, localRepo)
      execMavenGoOffline(new File(folder, "myapp"), localRepo) 
    }
  }

  def execMavenGoOffline (def directory, def localRepo) {
    def ant = new AntBuilder()
    def pom = new File(directory, "pom.xml")
    if (!pom.exists()) {
       if (!quiet) println "$pom can't be found. Skipping maven build"
       return
    }

    def pomModel = new XmlSlurper(false,false).parse(pom)    
    def profiles = pomModel?.profiles?.profile?.id.collect{ it.text()}.join(",")   
    if (!quiet) println "Building ${pomModel.groupId}:${pomModel.artifactId}:${pomModel.version} " + profiles?:"with profiles $profiles"

    //Spit everything to the current console
    if (!quiet) {
      ant.project.getBuildListeners().each{ 
        it.setOutputPrintStream(System.out) 
      } 
    }
    //remove [exec] prefixes
    def logger = ant.project.buildListeners.find { it instanceof org.apache.tools.ant.DefaultLogger }
    logger.emacsMode = true
    
    ant.exec(errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "false",
             dir: directory,
             executable: getMavenExec()) {
                //arg(value:"--quiet")
                arg(value:"dependency:go-offline") 
                if (profiles)  arg(value:"-P$profiles")
                if (localRepo) arg(value:"-Dmaven.repo.local=${localRepo.absolutePath}")
             }
  }

  String getMavenExec()  {
    def mvnFileName="mvn"
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
      mvnFileName+=".bat"
    }
    mvnFileName
  }

  def execMavenArchetypeBuild (groupId, artifactId, version, directory, localRepo) {
    def ant = new AntBuilder()
    //Spit everything to the current console
    if (!quiet)  {
      ant.project.getBuildListeners().each{ 
        it.setOutputPrintStream(System.out) 
      } 
    }
    //remove [exec] prefixes
    def logger = ant.project.buildListeners.find { it instanceof org.apache.tools.ant.DefaultLogger }
    logger.emacsMode = true
    
    ant.exec(errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "false",
             dir: directory,
             executable: getMavenExec()) {
                arg(value:"archetype:generate") 
                //arg(value:"--quiet") 
                arg(value:"-DarchetypeGroupId=${groupId}") 
                arg(value:"-DarchetypeArtifactId=${artifactId}") 
                arg(value:"-DarchetypeVersion=${version}") 
                arg(value:"-DgroupId=foo.bar") 
                arg(value:"-DartifactId=myapp") 
                arg(value:"-DinteractiveMode=false")
                arg(value:"-Dversion=1.0.0-SNAPSHOT") 
                if (localRepo) arg(value:"-Dmaven.repo.local=${localRepo.absolutePath}")
             }        
  }
}