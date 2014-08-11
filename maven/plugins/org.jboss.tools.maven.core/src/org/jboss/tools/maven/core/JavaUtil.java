/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Fred Bricon
 *
 */
public class JavaUtil {
	
	private JavaUtil() {
	}
	
    /**
     * Checks if a project has a given class in its classpath.<br/>
     * @param project : the workspace project
     * @param className : the fully qualified name of the class to search for
     * @return true if className is found in the project's classpath (provided the project is a JavaProject and its classpath has been set.)
     *    
     */
    public static boolean hasInClassPath(IProject project, String className) {
      boolean result = false;
      if (project != null){
        result = hasInClassPath(JavaCore.create(project), className);
      }
      return result;
    }
  
    /**
     * Checks if a java project has a given class in its classpath.<br/>
     * @param javaProject : the workspace project
     * @param className : the fully qualified name of the class to search for
     * @return true if className is found in the project's classpath (provided the project is a JavaProject and its classpath has been set.)
     *    
     */
    public static boolean hasInClassPath(IJavaProject javaProject, String className) {
    	boolean result = false;
        if (javaProject != null) {
           try {
       		 result = javaProject.findType(className) != null;
       	   } catch (JavaModelException e) {
       		 e.printStackTrace();
       	   }
       }
       return result;
    }

}
