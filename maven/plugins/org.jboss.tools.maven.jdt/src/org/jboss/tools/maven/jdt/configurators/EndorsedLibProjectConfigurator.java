/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt.configurators;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.internal.markers.SourceLocation;
import org.eclipse.m2e.core.internal.markers.SourceLocationHelper;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.jboss.tools.maven.jdt.MavenJdtActivator;
import org.jboss.tools.maven.jdt.endorsedlib.IEndorsedLibrariesManager;
import org.jboss.tools.maven.jdt.utils.ClasspathHelpers;


/**
 * Endorsed Libraries project configurator
 * 
 * @author Fred Bricon
 *
 */
public class EndorsedLibProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

	private static final Pattern jAVA_ENDORSED_DIRS_PATTERN = Pattern.compile("-Djava.endorsed.dirs=([^ \\t]+)");
	
	private static final String MISSING_ENDORSED_DIRS_MARKER = "org.jbosstools.maven.configuration.jdt.endorsedlib";

	@SuppressWarnings("restriction")
	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {

		markerManager.deleteMarkers(request.getProject(), MISSING_ENDORSED_DIRS_MARKER);
		
		IMavenProjectFacade mavenProjectFacade = request.getMavenProjectFacade();
		File[] endorsedDirs = getEndorsedDirs(mavenProjectFacade, monitor);
		if (endorsedDirs == null || endorsedDirs.length == 0) {
			return;
		}
		
		Set<File> missingEndorsedDir = checkMissingDirs(endorsedDirs);
		if (!missingEndorsedDir.isEmpty()) {
			MojoExecutionKey key = new MojoExecutionKey("org.apache.maven.plugins","maven-compiler-plugin",null,null,null,null); 
			SourceLocation sourceLocation = SourceLocationHelper.findLocation(mavenProjectFacade.getMavenProject(), key);
			for (File dir : missingEndorsedDir) {
				addMissingDirWarning(request.getProject(), sourceLocation, dir);
			}
		}
    }

	@SuppressWarnings("restriction")
	private void addMissingDirWarning(IProject project, SourceLocation sourceLocation, File dir) throws CoreException {
		
		IMarker marker = markerManager.addMarker(project.getFile("pom.xml"), 
	    		MISSING_ENDORSED_DIRS_MARKER, 
	    		"Endorsed directory '" + dir.getAbsolutePath() + "' is missing. " +
	    				"You may need to a perform a Maven command line build in order to create it."
	    		,sourceLocation.getLineNumber(),  IMarker.SEVERITY_ERROR);
		marker.setAttribute("outputDirectory", dir.getAbsolutePath());
	}

	private Set<File> checkMissingDirs(File[] endorsedDirs) {
		Set<File> missingDirs = new HashSet<File>(endorsedDirs.length);
		for (File dir : endorsedDirs) {
			if (!dir.exists()) {
				missingDirs.add(dir);
			}
		}
		return missingDirs;
	}
	
	public void configureClasspath(IMavenProjectFacade facade,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {
		// Nothing to configure here 
	}

	public void configureRawClasspath(ProjectConfigurationRequest request,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {

		IJavaProject javaProject = JavaCore.create(request.getProject());
		if (javaProject == null) {
			return;
		}

		File[] endorsedDirs = getEndorsedDirs(request.getMavenProjectFacade(), monitor);
		
		if (endorsedDirs == null || endorsedDirs.length == 0){
			ClasspathHelpers.removeEndorsedLibClasspathContainer(classpath);
			return;
		}
		
		getEndorsedLibrariesManager().configureEndorsedLibs(javaProject, classpath, endorsedDirs, monitor);
	}
	
	private File[] getEndorsedDirs(IMavenProjectFacade mavenProjectFacade, IProgressMonitor monitor) throws CoreException {
		MavenSession session =  createSession(mavenProjectFacade, monitor);
		MojoExecution mojoExecution = getCompilerMojoExecution(mavenProjectFacade, session, monitor);
		
		//Parse <compilerArgument> for -Djava.endorsed.dirs 
		String compilerArgument  = maven.getMojoParameterValue(session, mojoExecution, "compilerArgument", String.class);//
		File[] javaEndorsedDirs = parseJavaEndorsedDirs(mavenProjectFacade.getProject(), compilerArgument);

		//Check <compilerArguments> for <endorseddirs>
		@SuppressWarnings("unchecked")
		Map<String, String> compilerArguments = maven.getMojoParameterValue(session, mojoExecution, "compilerArguments", Map.class); 
		String endorsedDirsArg = (compilerArguments == null)?null:compilerArguments.get("endorseddirs");
		File[] endorsedDirs = parseEndorsedDirs(mavenProjectFacade.getProject(), endorsedDirsArg);
		
		return concat(javaEndorsedDirs, endorsedDirs);
	}

	private File[] parseJavaEndorsedDirs(IProject project, String compilerArgument) {
		if (compilerArgument == null) {
			return null;
		}
		
		//We can expect patterns like -Djava.endorsed.dirs=/path/white space/dir" "-Dfoo=bar
		//as a workaround for maven-compiler-plugin  <compilerArgument> not handling multiple values correctly
		//So instead of using rexeps to parse the path, we manually look for the presence of quotes and spaces
		String key = "-Djava.endorsed.dirs=";
		int start = compilerArgument.indexOf(key);
		if (start < 0) {
			return null;
		}
		File[] dirs = null;
		int end = compilerArgument.indexOf("\"", start);
		if (end < 0) {
			end = compilerArgument.indexOf(" ", start);
			if (end < 0) {
				end = compilerArgument.length();
			}
		}
		if (end > 0) {
			String argument = compilerArgument.substring(start+key.length(), end);
			dirs = parseEndorsedDirs(project, argument);
			
		}
		return dirs;
	}

	private File[] parseEndorsedDirs(IProject project, String endorsedDirs) {
		if (endorsedDirs == null){
			return null;
		}
		//Remove quotes, use system separators
		endorsedDirs = endorsedDirs.replaceAll("\"", "");
		
		//Quote from http://docs.oracle.com/javase/6/docs/technotes/guides/standards/
		//"If more than one directory path is specified by java.endorsed.dirs, 
		//they must be separated by File.pathSeparatorChar."
		String[] paths = endorsedDirs.split(""+File.pathSeparatorChar);

		//Convert dir paths to Files
		List<File> dirs = new ArrayList<File>(paths.length);
		for (String path : paths) {
			IPath p = new Path(useSystemSeparator(path));
			if (!p.isAbsolute()) {
				p = project.getLocation().append(p);
			}
			File lib = new File(p.toOSString());
			dirs.add(lib);
		}
		
		return dirs.toArray(new File[0]);
	}
	
    
	private static String useSystemSeparator(String name) {
		if (name == null) return null;
		return name.replace('/', File.separatorChar)
	            .replace('\\', File.separatorChar);
	}
	
	private MojoExecution getCompilerMojoExecution(IMavenProjectFacade mavenProjectFacade, 
												   MavenSession session, 
												   IProgressMonitor monitor) throws CoreException {
		MavenExecutionPlan executionPlan = maven.calculateExecutionPlan(session, 
																		mavenProjectFacade.getMavenProject(), 
																		Collections.singletonList("compile"), 
																		true, 
																		monitor);
	    MojoExecution mojoExecution = getExecution(executionPlan, "maven-compiler-plugin", "compile");
		return mojoExecution;
	}

	private MavenSession createSession(IMavenProjectFacade mavenProjectFacade, IProgressMonitor monitor) throws CoreException {
		IFile pomResource = mavenProjectFacade.getPom();
	    MavenExecutionRequest request = projectManager.createExecutionRequest(pomResource, 
	    																	  mavenProjectFacade.getResolverConfiguration(), 
	    																	  monitor);
	    MavenSession session = maven.createSession(request, mavenProjectFacade.getMavenProject());
		return session;
	}
	
    private MojoExecution getExecution(MavenExecutionPlan executionPlan, String artifactId, String goal) throws CoreException {
      for(MojoExecution execution : executionPlan.getMojoExecutions()) {
        if(artifactId.equals(execution.getArtifactId()) && goal.equals(execution.getGoal())) {
          return execution;
        }
      }
      return null;
    }	
	
	private static <T> T[] concat(T[] first, T[] second) {
	  if (second == null) {
		  return first;
	  }
	  if (first == null) {
		  return second;
	  }
	  T[] result = Arrays.copyOf(first, first.length + second.length);
	  System.arraycopy(second, 0, result, first.length, second.length);
	  return result;
	}
	
	private IEndorsedLibrariesManager getEndorsedLibrariesManager() {
		return MavenJdtActivator.getDefault().getEndorsedLibrariesManager();
	}
	
	public void unconfigure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		super.unconfigure(request, monitor);
		ClasspathHelpers.removeEndorsedLibClasspathContainer(request.getProject());		 
    }
	
}
