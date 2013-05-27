package org.jboss.tools.central.internal;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.jboss.tools.central.JBossCentralActivator;

public class ImageUtil {

	/**
	 * Creates an image from an {@link URL}.
	 * If the iconUrl points at a jar file, the created image doesn't not leak file handle. 
	 */
	public static Image createImageFromUrl(Device device, URL iconUrl) {

		if (!iconUrl.getProtocol().equals("jar")) {
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(iconUrl);
			return descriptor.createImage();
		}
		
		//Load from jar:
		Image image = null;
		try {
			String fileName = iconUrl.getFile();
			if (fileName.contains("!")) {
				String[] location = fileName.split("!");
				fileName = location[0];
				String imageName = URLDecoder.decode(location[1].substring(1), "utf-8");
				File file = new File(new URI(fileName));
				JarFile jarFile = null;
				InputStream inputStream = null; 
				try {
					jarFile = new JarFile(file);
					ZipEntry imageEntry = jarFile.getEntry(imageName);
					if (imageEntry != null) {
						inputStream = jarFile.getInputStream(imageEntry);
						image = new Image(device, inputStream);
					} 
				}finally {
					IOUtils.closeQuietly(inputStream);
					try {
						if (jarFile != null) {
							jarFile.close();
						}
					} catch (Exception e) {
						//ignore
				    }
				}
			}
		} catch (Exception e) {
			JBossCentralActivator.log(e);
		}
		
		return image;
	}
}
