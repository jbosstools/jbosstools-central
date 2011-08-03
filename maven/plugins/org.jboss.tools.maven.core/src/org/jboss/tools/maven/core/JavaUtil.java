package org.jboss.tools.maven.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.tools.common.util.EclipseJavaUtil;

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
       		 result = EclipseJavaUtil.findType(javaProject, className) != null;
       	   } catch (JavaModelException e) {
       		 e.printStackTrace();
       	   }
       }
       return result;
    }

}
