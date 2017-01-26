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
package org.jboss.tools.central.reddeer.wizards;

import org.jboss.reddeer.jface.wizard.WizardPage;
import org.jboss.reddeer.swt.api.Text;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.tools.central.reddeer.exception.CentralException;

/**
 * 
 * @author rhopp
 *
 */


public class NewProjectExamplesLocationPage extends WizardPage{
	
	public void toggleDefaultLocation(boolean checked){
		new CheckBox(0).toggle(checked);
	}
	
	public boolean isDefaultLocationChecked(){
		return new CheckBox(0).isChecked();
	}
	
	public String getLocation(){
		return new DefaultText(0).getText();
	}
	
	public void setLocation(String path){
		Text text = new DefaultText(0);
		if (text.isEnabled()){
			text.setText(path);
		}else{
			throw new CentralException("Location is not editable");
		}
	}

}
