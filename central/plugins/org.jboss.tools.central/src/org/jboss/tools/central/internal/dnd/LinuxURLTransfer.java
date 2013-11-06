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
package org.jboss.tools.central.internal.dnd;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * The <code>LinuxURLTransfer</code> class fixes dropping a URL from Chrome into Eclipse on Linux
 *  
 *  @author snjeza
 *  
 * @see https://issues.jboss.org/browse/JBIDE-15499
 * @see Transfer
 */
public class LinuxURLTransfer extends ByteArrayTransfer {

	static LinuxURLTransfer _instance = new LinuxURLTransfer();
	private static final String URI_LIST = "text/uri-list"; //$NON-NLS-1$
	private static final int URI_LIST_ID = registerType(URI_LIST);
	
	private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
	private static final int TEXT_HTML_ID = registerType(TEXT_HTML);
	
	private LinuxURLTransfer() {
	}
	
	/**
	 * Returns the singleton instance of the LinuxURLTransfer class.
	 *
	 * @return the singleton instance of the LinuxURLTransfer class
	 */
	public static LinuxURLTransfer getInstance () {
		return _instance;
	}
	
	/**
	 * This implementation of <code>javaToNative</code> converts a URL
	 * represented by a java <code>String</code> to a platform specific representation.
	 * 
	 * @param object a java <code>String</code> containing a URL
	 * @param transferData an empty <code>TransferData</code> object that will
	 *  	be filled in on return with the platform specific format of the data
	 * 
	 * @see Transfer#nativeToJava
	 */
	public void javaToNative(Object object, TransferData transferData) {
		if (isLinuxGTK()) {
			try {
				transferData.type = TEXT_HTML_ID;
				HTMLTransfer.getInstance().javaToNative(object, transferData);
			} finally {
				transferData.type = URI_LIST_ID;
			}
		}
	}
	
	/**
	 * This implementation of <code>nativeToJava</code> converts a platform 
	 * specific representation of a URL to a java <code>String</code>.
	 * 
	 * @param transferData the platform specific representation of the data to be converted
	 * @return a java <code>String</code> containing a URL if the conversion was successful;
	 * 		otherwise null
	 * 
	 * @see Transfer#javaToNative
	 */
	public Object nativeToJava(TransferData transferData) {
		Object object = null;
		if (isLinuxGTK()) {
			try {
				transferData.type = TEXT_HTML_ID;
				object = HTMLTransfer.getInstance().nativeToJava(transferData);
			} finally {
				transferData.type = URI_LIST_ID;
			}
		}
		return object;
	}
	
	protected int[] getTypeIds(){
		return new int[] {URI_LIST_ID};
	}

	@Override
	public TransferData[] getSupportedTypes() {
		if (!isLinuxGTK()) {
			return new TransferData[0];
		}
		return super.getSupportedTypes();
	}

	@Override
	public boolean isSupportedType(TransferData transferData) {
		if (!isLinuxGTK()) {
			return false;
		}
		return super.isSupportedType(transferData);
	}

	protected String[] getTypeNames(){
		if (!isLinuxGTK()) {
			return new String[0];
		}
		return new String[] {URI_LIST}; 
	}
	
	boolean checkURL(Object object) {
		return (object instanceof String) && ((String)object).length() > 0;
	}

	protected boolean validate(Object object) {
		return checkURL(object);
	}
	
	public static boolean isLinuxGTK() {
		return Platform.OS_LINUX.equals(Platform.getOS()) && Platform.WS_GTK.equals(Platform.getWS());
	}
}
