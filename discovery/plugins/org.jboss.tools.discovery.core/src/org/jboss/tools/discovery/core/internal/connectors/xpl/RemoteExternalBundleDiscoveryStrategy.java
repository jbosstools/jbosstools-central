/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat - file based remote connectors
 *******************************************************************************/
package org.jboss.tools.discovery.core.internal.connectors.xpl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.Directory;
import org.eclipse.equinox.internal.p2.discovery.compatibility.Directory.Entry;
import org.eclipse.equinox.internal.p2.discovery.compatibility.DirectoryParser;
import org.eclipse.equinox.internal.p2.discovery.compatibility.util.TransportUtil;
import org.eclipse.equinox.internal.p2.discovery.compatibility.util.TransportUtil.TextContentProcessor;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;

/**
 * 
 * This class was forked from <a href=
 * "http://git.eclipse.org/c/mylyn/org.eclipse.mylyn.commons.git/commit/org.eclipse.mylyn.discovery.core/src/org/eclipse/mylyn/internal/discovery/core/model/RemoteBundleDiscoveryStrategy.java?id=7991a279dcbf705b49cbee255c32167a21507204"
 * >org.eclipse.mylyn.internal.discovery.core.model.
 * RemoteBundleDiscoveryStrategy</a> Changes include :
 * <ul>
 * <li>Allow non Connector Extension</li>
 * <li>Allow file based remote bundles</li>
 * </ul>
 * 
 * A discovery strategy that downloads a simple directory of remote jars. The
 * directory is first downloaded, then each remote jar is downloaded.
 * 
 * @author David Green
 */
@SuppressWarnings("restriction")
public class RemoteExternalBundleDiscoveryStrategy extends ExternalBundleDiscoveryStrategy {

	private int maxDiscoveryJarDownloadAttempts = 1;
	
	private String directoryUrl;
	
	public RemoteExternalBundleDiscoveryStrategy() {
		setDeleteStorageFolderOnFailure(true);
	}

	protected Map<File, Entry> loadRegistry(File storageDirectory, IProgressMonitor monitor) throws CoreException {

		// new SubProgressMonitor(monitor, ticksTenPercent * 3);

		final int totalTicks = 100000;
		final int ticksTenPercent = totalTicks / 10;

		monitor.beginTask("Remote discovery", totalTicks);

		Directory directory;

		try {
			final Directory[] temp = new Directory[1];
			final URI uri = new URI(directoryUrl);
			TransportUtil.readResource(uri, new TextContentProcessor() {
				public void process(Reader reader) throws IOException {
					DirectoryParser parser = new DirectoryParser();
					//parser.setBaseUri(uri);
					temp[0] = parser.parse(reader);
				}
			}, SubMonitor.convert(monitor, ticksTenPercent));
			directory = temp[0];
			if (directory == null) {
				throw new IllegalStateException();
			}
		} catch (UnknownHostException e) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							DiscoveryCore.ID_PLUGIN,
							NLS.bind(
									"Cannot access {0}: unknown host: please check your Internet connection and try again.",
									e.getMessage()), e));
		} catch (IOException e) {
		    if (monitor.isCanceled()) {
		        return null;
		    } else {
	            throw new CoreException(new Status(IStatus.ERROR,
	                    DiscoveryCore.ID_PLUGIN,
	                    "IO failure: cannot load discovery directory", e));
		    }
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					DiscoveryCore.ID_PLUGIN,
					"IO failure: cannot load discovery directory", e));
		}
		if (monitor.isCanceled()) {
			return null;
		}
		if (directory.getEntries().isEmpty()) {
			throw new CoreException(new Status(IStatus.ERROR,
					DiscoveryCore.ID_PLUGIN, "Discovery directory is empty"));
		}

		Map<File, Directory.Entry> bundleFileToDirectoryEntry = new HashMap<File, Directory.Entry>();

		ExecutorService executorService = createExecutorService(directory
				.getEntries().size());
		try {
			List<Future<DownloadBundleJob>> futures = new ArrayList<Future<DownloadBundleJob>>();
			// submit jobs
			for (Directory.Entry entry : directory.getEntries()) {
				futures.add(executorService.submit(new DownloadBundleJob(entry, storageDirectory, monitor)));
			}
			int futureSize = ticksTenPercent * 4
					/ directory.getEntries().size();
			// collect job results
			for (Future<DownloadBundleJob> job : futures) {
				try {
					DownloadBundleJob bundleJob;
					for (;;) {
						try {
							bundleJob = job.get(1L, TimeUnit.SECONDS);
							break;
						} catch (TimeoutException e) {
							if (monitor.isCanceled()) {
								return null;
							}
						}
					}
					if (bundleJob.file != null) {
						bundleFileToDirectoryEntry.put(bundleJob.file,
								bundleJob.entry);
					}
					monitor.worked(futureSize);
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if ((cause instanceof RuntimeException) && (cause.getCause() instanceof InvocationTargetException)) {
					    cause = ((InvocationTargetException)cause.getCause()).getTargetException();
					}
					if (cause instanceof OperationCanceledException) {
						monitor.setCanceled(true);
						return null;
					}
					IStatus status;
					if (cause instanceof CoreException) {
						status = ((CoreException) cause).getStatus();
					} else {
						status = new Status(IStatus.ERROR,
								DiscoveryCore.ID_PLUGIN, "Unexpected error",
								cause);
					}
					// log errors but continue on
					DiscoveryActivator.getDefault().getLog().log(status);
				} catch (InterruptedException e) {
					monitor.setCanceled(true);
					return null;
				}
			}
		} finally {
			executorService.shutdownNow();
		}
		return bundleFileToDirectoryEntry;
	}

	private class DownloadBundleJob implements Callable<DownloadBundleJob> {
		private final IProgressMonitor monitor;

		private final Entry entry;

		private File file;

		private File downloadStorage;

		public DownloadBundleJob(Entry entry, File downloadFolder, IProgressMonitor monitor) {
			this.entry = entry;
			this.monitor = monitor;
			this.downloadStorage = downloadFolder;
		}

		public DownloadBundleJob call() {

			String bundleUrl = entry.getLocation();
			for (int attemptCount = 0; attemptCount < maxDiscoveryJarDownloadAttempts; ++attemptCount) {
				try {
					if (!bundleUrl.startsWith("http://") && !bundleUrl.startsWith("https://") && !bundleUrl.startsWith("file:")) { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						DiscoveryActivator.getDefault().getLog()
								.log(new Status(
										IStatus.WARNING,
										DiscoveryCore.ID_PLUGIN,
										NLS.bind(
												"Unrecognized discovery bundle URL: {0}",
												bundleUrl)));
						continue;
					}
					File target = File
							.createTempFile(getFileNameFor(bundleUrl) + "_", ".downloading", this.downloadStorage); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$

					if (monitor.isCanceled()) {
						break;
					}

					try {
						TransportUtil.downloadResource(new URI(bundleUrl), target,
								new NullProgressMonitor() {
									@Override
									public boolean isCanceled() {
										return super.isCanceled()
												|| monitor.isCanceled();
									}
								}/* don't use sub progress monitor here */);
					} catch (URISyntaxException | CoreException e) {
					}
					file = new File(target.getParentFile(), target.getName().replace("downloading", "jar"));
					FileUtils.moveFile(target, file);
				} catch (IOException e) {
				    if (!monitor.isCanceled()) {
				    	DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
	                            DiscoveryCore.ID_PLUGIN, NLS.bind(
	                                    "Cannot download bundle at {0}: {1}",
	                                    bundleUrl, e.getMessage()), e));
	                    if (isUnknownHostException(e)) {
	                        break;
	                    }
				    }
				}
			}
			return this;
		}

		//Copied from http://git.eclipse.org/c/mylyn/org.eclipse.mylyn.commons.git/commit/?id=9d3ecc1be8ab17136e7c81581dccca1b26fb24cc
		private String getFileNameFor(String bundleUrl) throws IOException {
			//TODO delegate to WebUtil.getFileNameFor() once a fixed Mylyn version is available 
			if (bundleUrl.charAt(bundleUrl.length() - 1) == '/') {
			   bundleUrl = bundleUrl.substring(0, bundleUrl.length() - 1);
			}
			if (bundleUrl.lastIndexOf('/') != -1) {
			   bundleUrl = bundleUrl.substring(bundleUrl.lastIndexOf('/') + 1);
			}
			return bundleUrl.replaceAll("[^a-zA-Z0-9_\\.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private ExecutorService createExecutorService(int size) {
		final int maxThreads = 4;
		return Executors.newFixedThreadPool(Math.min(size, maxThreads));
	}

	/**
	 * walk the exception chain to determine if the given exception or any of
	 * its underlying causes are an {@link UnknownHostException}.
	 * 
	 * @return true if the exception or one of its causes are
	 *         {@link UnknownHostException}.
	 */
	private boolean isUnknownHostException(Throwable t) {
		while (t != null) {
			if (t instanceof UnknownHostException) {
				return true;
			}
			Throwable t2 = t.getCause();
			if (t2 == t) {
				break;
			}
			t = t2;
		}
		return false;
	}

	/**
	 * indicate how many times discovyer jar downloads should be attempted
	 */
	public int getMaxDiscoveryJarDownloadAttempts() {
		return maxDiscoveryJarDownloadAttempts;
	}

	/**
	 * indicate how many times discovyer jar downloads should be attempted
	 * 
	 * @param maxDiscoveryJarDownloadAttempts
	 *            a number >= 1
	 */
	public void setMaxDiscoveryJarDownloadAttempts(
			int maxDiscoveryJarDownloadAttempts) {
		if (maxDiscoveryJarDownloadAttempts < 1
				|| maxDiscoveryJarDownloadAttempts > 2) {
			throw new IllegalArgumentException();
		}
		this.maxDiscoveryJarDownloadAttempts = maxDiscoveryJarDownloadAttempts;
	}
	
	public String getDirectoryUrl() {
		return directoryUrl;
	}

	public void setDirectoryUrl(String directoryUrl) {
		this.directoryUrl = directoryUrl;
	}
	
	@Override
	public File getStorageFolder() throws CoreException {
		File storageFolder = super.getStorageFolder();
		if (storageFolder == null) {
			try {
				storageFolder = createTempFolder();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						DiscoveryActivator.PLUGIN_ID,
						"IO failure: cannot create temporary storage folder", e)); //$NON-NLS-1$
			}				
			setStorageFolder(storageFolder);
		}
		return storageFolder;
	}

	private File createTempFolder() throws IOException {
		File temporaryStorage = File.createTempFile(RemoteExternalBundleDiscoveryStrategy.class.getSimpleName(), ".tmp");//$NON-NLS-1$
		if (!temporaryStorage.delete() ||
				!temporaryStorage.mkdirs()) {
			throw new IOException("Can't create temporary directory"); //$NON-NLS-1$
		}
		return temporaryStorage;
	}

}