/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.central.reddeer.api;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.reddeer.common.condition.AbstractWaitCondition;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.swt.api.Browser;

/**
 * Singleton helper for executing javascript functions (from file resources/functions.js). These functions should work with JBoss Central.
 * 
 * Workflow is as follows:
 * 1) Set browser.
 * 2) Call other methods ;-)
 * 
 * @author rhopp
 *
 */

public class JavaScriptHelper {

	private String functionsFile;
	private Browser browser;
	
	private static JavaScriptHelper instance = new JavaScriptHelper();
	
	public static JavaScriptHelper getInstance(){
		return instance;
	}
	
	public JavaScriptHelper() {
		loadFunctions(new File("resources/functions.js").getAbsolutePath());
	}
	
	/**
	 * This method has to be called before any else. Otherwise the methods would throw exceptions.
	 */
	
	public void setBrowser(Browser browser){
		this.browser = browser;
	}

	public void clickExample(String exampleName) {
		evaluateFunction("clickExample", exampleName);
		
	}

	public String getHTML(){
		return evaluateFunction("getHTML");
	}

	public void searchFor(String query){
		new WaitUntil(new SomeExamplesAreFound(query));
	}
	
	public String[] getExamples(){
		return evaluateFunction("getExamples").split(";");
	}
	
	public void clickWizard(String name){
		evaluateFunction("clickWizard", name);
	}
	
	public boolean hasNext(){
		return evaluateBoolFunction("hasNext");
	}
	
	public boolean hasPrevious(){
		return evaluateBoolFunction("hasPrevious");
	}
	
	public void nextPage(){
		executeFunction("nextPage");
	}
	
	public void prevPage(){
		executeFunction("prevPage");
	}
	
	/**
	 * Example has to be currently visible
	 * @param exampleName
	 * @return
	 */
	
	public String getDescriptionForExample(String exampleName){
		return evaluateFunction("getDescriptionForExample", exampleName);
	}
	
	public String[] getWizards(){
		return evaluateFunction("getWizards").split(";");
	}
	
	public void clearSearch(){
		executeFunction("clearSearch");
	}
	
	private void loadFunctions(String file){
		try {
			functionsFile = readFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] getLabelsForExample(String exampleName){
		return evaluateFunction("getLabelsForExample", exampleName).split(";");
	}
	
	private Boolean evaluateBoolFunction(String functionName, String... parameters){
		Boolean returnValue = (Boolean) browser.evaluate(buildFunctionWithReturn(functionName, parameters));
		return returnValue;
	}
	
	private String evaluateFunction(String functionName, String... parameters){
		String returnValue = (String) browser.evaluate(buildFunctionWithReturn(functionName, parameters));
		if (returnValue == null){
			return null;
		}
		return returnValue;
	}
	
	private void executeFunction(String functionName, String... parameters){
		
		browser.execute(buildFunctionWithoutReturn(functionName, parameters));
	}
	
	private String buildFunctionWithReturn(String functionName, String... parameters){
		return buildFunction(functionName, true, parameters);
	}
	
	private String buildFunctionWithoutReturn(String functionName, String... parameters){
		return buildFunction(functionName, false, parameters);
	}

	private String buildFunction(String functionName, boolean ret, String... parameters){
		
		StringBuilder sb = new StringBuilder(functionsFile);
		if (ret){
			sb.append("return ");
		}
		sb.append(functionName+"(");
		String prefix = "";
		for (String parameter : parameters) {
			sb.append(prefix+"\""); //apends prefix and starting parentheses
			sb.append(parameter);
			sb.append("\"");//appends ending parentheses
			prefix = ", ";
		}
		sb.append(");");
		return sb.toString();
	}
	
	private String readFile(String path) throws IOException {
		String fileContent = FileUtils.readFileToString(new File(path));
		return fileContent;
	}
	
	private class SomeExamplesAreFound extends AbstractWaitCondition{

		private String query;
		
		public SomeExamplesAreFound(String query) {
			this.query=query;
		}
		
		@Override
		public boolean test() {
			evaluateFunction("searchFor", query);
			String[] listOfExamples = evaluateFunction("getExamples").split(";");
			return listOfExamples.length>0 && !listOfExamples[0].equals("");
		}

		@Override
		public String description() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
