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
package org.jboss.tools.maven.reddeer.apt.ui.preferences;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;

public class AnnotationProcessingSettingsPage extends PreferencePage{
	
	public AnnotationProcessingSettingsPage(ReferencedComposite referencedComposite){
		super(referencedComposite, "Maven","Annotation Processing");
	}
	
	public enum ProcessingMode{
		NONE("Do not automatically configure/execute annotation processing from pom.xml"),
		JDT("Automatically configure JDT APT (builds faster, but outcome may differ from Maven builds)"),
		MAVEN("Experimental : Delegate annotation processing to maven plugins (for maven-processor-plugin only)");
		
		private final String checkboxText;
		
		private ProcessingMode(String checkboxText){
			this.checkboxText = checkboxText;
		}
		
		public String getText(){
			return checkboxText;
		}
	}
	
	public void setAnnotationProcessingMode(ProcessingMode mode){
		new RadioButton(new DefaultGroup("Select Annotation Processing Mode"),
				mode.getText()).toggle(true);
	}
	
	public ProcessingMode getCurrentAnnotationProcessingMode(){
		if(new CheckBox(new DefaultGroup("Select Annotation Processing Mode"),
				ProcessingMode.NONE.getText()).isChecked()){
			return ProcessingMode.NONE;
		} else if (new CheckBox(new DefaultGroup("Select Annotation Processing Mode"),
				ProcessingMode.JDT.getText()).isChecked()){
			return ProcessingMode.JDT;
		} else if (new CheckBox(new DefaultGroup("Select Annotation Processing Mode"),
				ProcessingMode.MAVEN.getText()).isChecked()){
			return ProcessingMode.MAVEN;
		}
		throw new EclipseLayerException("No annotation mode is currently used");
	}

}
