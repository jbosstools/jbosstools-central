/*************************************************************************************
 * Copyright (c) 2008-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class UnArchiver {

	private File destinationFolder;
	private File zip;
	private Set<IPath> filters;
	
	public static UnArchiver create(File zip) {
		return create(zip, null);
	}
	
	public static UnArchiver create(File zip, File destination) {
		Assert.isNotNull(zip);
		UnArchiver unarchiver = new UnArchiver(zip);
		if (destination == null) {
			File parent = zip.getParentFile();
			if (parent == null) {
				throw new IllegalArgumentException("Can not infer a destination folder to extract "+zip); //$NON-NLS-1$
			}
			String zipName = FilenameUtils.getBaseName(zip.getName());
			destination = new File(parent, zipName);
		}
		unarchiver.setDestination(destination);
		return unarchiver;
	}

	private void setDestination(File destination) {
		destinationFolder = destination;
	}

	private UnArchiver(File zip) {
		this.zip = zip;
	}
	
	public boolean extract(IProgressMonitor monitor) throws IOException {
		destinationFolder.mkdirs();
		int filterCount = (filters == null || filters.isEmpty())?0:filters.size();
		try (ZipFile zipFile = new ZipFile(zip)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			IPath filePath;
			String fileName;
			while (entries.hasMoreElements()) {
				if (monitor.isCanceled()) {
					return false;
				}
				ZipEntry entry = (ZipEntry) entries.nextElement();
				fileName = entry.getName();
				boolean skip;
				if (filterCount == 0) {
					skip = false;
				} else {
					skip = true;
					filePath = new Path(fileName);
					for (IPath folder : filters) {
						IPath matchingPath = findSegment2(filePath, folder);
						if (matchingPath != null) {
							if (filterCount == 1) {
								fileName = matchingPath.removeFirstSegments(folder.segmentCount()).toOSString();
							} else {
								fileName = matchingPath.toOSString();
							}
							skip = false;
							break;
						}
					}
				}
				if (skip) {
					continue;
				}
				
				monitor.setTaskName("Extracting "+fileName);//$NON-NLS-1$
				if (entry.isDirectory()) {
					File dir = new File(destinationFolder, fileName);
					dir.mkdirs();
					continue;
				}
				File entryFile = new File(destinationFolder, fileName);
				entryFile.getParentFile().mkdirs();
				try (InputStream in = zipFile.getInputStream(entry); 
					 OutputStream out = new FileOutputStream(entryFile)) {
					 IOUtils.copy(in, out);
				}
			}
		}
		return true;
	}

	private IPath findSegment2(IPath source, IPath target) {

        String first = target.segments()[0];
        int max = source.segmentCount() - target.segmentCount();

        for (int i = 0; i <= max; i++) {
            if (!source.segment(i).equals(first)) {
                while (++i <= max && !source.segment(i).equals(first));
            }

            if (i <= max) {
                int j = i + 1;
                int end = j + target.segmentCount() - 1;
                for (int k = 1; j < end && source.segment(j).equals(target.segment(k)); j++, k++);

                if (j == end) {
                    return source.removeFirstSegments(i);
                }
            }
        }
        return null;
    }

	public void setFilters(Set<String> filters) {
		if (filters == null) {
			this.filters = null;
			return;
		}
		Set<IPath> paths = new HashSet<>(filters.size());
		for (String folder : filters) {
			paths.add(new Path(folder));
		}
		this.filters = paths;
	}
}
