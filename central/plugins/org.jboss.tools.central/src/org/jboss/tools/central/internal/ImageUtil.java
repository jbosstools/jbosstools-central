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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.central.JBossCentralActivator;


/**
 * Image utility class
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
			new UrlStreamJarProvider(iconUrl).readStream( new StreamHandler() {
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
	 * Convert an image embedded into a jar or a bundle to a locally accessible url or else it will return the original url.
	 */
	public static String getImageAsLocalUrl(String url) throws CoreException {
		UrlStreamProvider streamProvider = null;
		try {
			if (url.startsWith("jar:")) {
				streamProvider = new UrlStreamJarProvider(url);
			} else if (url.startsWith("bundleentry:")) {
				streamProvider = new UrlStreamDefaultProvider(url);	
			}
		} catch (MalformedURLException e) {
			throwCoreException("Error reading "+url, e);
		}
		if (streamProvider == null) {
			return url;
		}
		
		final Path localFile = getLocalImage(url);
		if (Files.notExists(localFile)) {
			streamProvider.readStream(new StreamHandler() {
				@Override
				public void handle(InputStream stream) throws IOException {
					Files.createDirectories(localFile.getParent());
					Files.copy(stream, localFile);
				}
			});
		}
		return localFile.toUri().toString();
	}
	
	private static Path getLocalImage(String url) {
		String baseDir = JBossCentralActivator.getDefault().getStateLocation().toOSString();
		String name = url.substring(url.lastIndexOf("/"), url.length()); 
		String hashedPath = String.valueOf(Math.abs(url.hashCode()));
		return Paths.get(baseDir, "images", hashedPath, name);
	}

	private static class UrlStreamJarProvider extends UrlStreamProvider {

		UrlStreamJarProvider(URL url) throws MalformedURLException {
			if (!url.getProtocol().equals("jar")) {
				throw new IllegalArgumentException("Only 'jar:' urls are supported");
			}
			this.url = url;
		}
		
		UrlStreamJarProvider(String url) throws MalformedURLException {
			if (!url.startsWith("jar")) {
				throw new IllegalArgumentException("Only 'jar:' urls are supported");
			}
			setUrl(url);
		}

		@Override
		void readStream(StreamHandler streamHandler)  throws CoreException {
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
				} catch (IOException | URISyntaxException e) {
					throwCoreException("Error extracting "+url, e);
				}
			}
		}
		
	}
	
	private static class UrlStreamDefaultProvider extends UrlStreamProvider {

		UrlStreamDefaultProvider(String url) throws MalformedURLException {
			setUrl(url);
		}

		@Override
		void readStream(StreamHandler streamHandler) throws CoreException  {
			try (InputStream stream = url.openStream()){
				streamHandler.handle(stream);
			} catch (IOException e) {
				throwCoreException("Error extracting "+url, e);
			}
		}
		
	}
	
	private static void throwCoreException(String msg, Exception e) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID, msg, e);
		throw new CoreException(status);
	}
	
	private interface StreamHandler {
		void handle(InputStream stream) throws IOException;
	}
	
	private static abstract class UrlStreamProvider {
		
		protected URL url;
		
		protected void setUrl(String sUrl) throws MalformedURLException {
			try {
				url = new URL(URLDecoder.decode(sUrl, "utf-8"));
			} catch (UnsupportedEncodingException cantHappen) {
				//ignore
			}
		}
		
		abstract void readStream(StreamHandler streamHandler) throws CoreException;
	}
	
}
