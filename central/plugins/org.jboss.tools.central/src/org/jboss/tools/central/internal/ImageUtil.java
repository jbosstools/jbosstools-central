/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;


/**
 * IMage utility class
 * 
 * @author Fred Bricon
 */
public class ImageUtil {

	/**
	 * Creates an image from an {@link URL}.
	 * If the iconUrl points at a jar file, the created image doesn't not leak file handle. 
	 */
	public static Image createImageFromUrl(final Device device, URL iconUrl) {

		if (!iconUrl.getProtocol().equals("jar")) {
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(iconUrl);
			return descriptor.createImage();
		}
		
		//Load from jar:
		final Image[] image = new Image[1];
		try {
			getStreamFromJar(iconUrl, new StreamHandler() {
				@Override
				public void handle(InputStream inputStream) {
					image[0] = new Image(device, inputStream);
				}
			});
		} catch (Exception e) {
			JBossCentralActivator.log(e);
		}
		return image[0];
	}
	
	/**
	 * Convert an image embedded into a jar to a locally accessible url 
	 */
	public static String getImageAsLocalUrl(String url) throws CoreException {
		if (!url.startsWith("jar:")) {
			return url;
		}
		try {
			url = URLDecoder.decode(url, "utf-8");
		} catch (UnsupportedEncodingException cantHappen) {
			//ignore
		}
		final Path localFile = getLocalImage(url);
		if (Files.notExists(localFile)) {
			try {
				getStreamFromJar(new URL(url),new StreamHandler() {
					@Override
					public void handle(InputStream stream) throws IOException {
						Files.createDirectories(localFile.getParent());
						Files.copy(stream, localFile);
					}
				});
			} catch (URISyntaxException | IOException e) {
				IStatus status = new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID, "An error occured while saving "+url, e);
				throw new CoreException(status);
			}
		}
		return "file://"+ localFile.toString();
	}
	
	private static Path getLocalImage(String url) {
		String baseDir = JBossCentralActivator.getDefault().getStateLocation().toOSString();
		String name = url.substring(url.lastIndexOf("/"), url.length()); 
		String hashedPath = String.valueOf(Math.abs(url.hashCode()));
		return Paths.get(baseDir, "images", hashedPath, name);
	}

	public static void getStreamFromJar(URL url, StreamHandler streamHandler) throws URISyntaxException, IOException {
		if (!url.getProtocol().equals("jar")) {
			throw new IllegalArgumentException("Only 'jar:' urls are supported");
		}
		String fileName = url.getFile();
		if (fileName.contains("!")) {
			String[] location = fileName.split("!");
			fileName = location[0];
			String imageName = null;
			try {
				imageName = URLDecoder.decode(location[1].substring(1), "utf-8");
				File file = new File(new URI(fileName));
				try (JarFile jarFile = new JarFile(file)){
					ZipEntry imageEntry = jarFile.getEntry(imageName);
					if (imageEntry != null) {
						streamHandler.handle(jarFile.getInputStream(imageEntry));
					} 
				}
			} catch (UnsupportedEncodingException e) {
				//can't happen here
			}
		}
	}
	
	private interface StreamHandler {
		void handle(InputStream stream) throws IOException;
	}
	
}
