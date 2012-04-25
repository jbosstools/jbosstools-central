package org.jboss.tools.central.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.model.FeedsEntry;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public abstract class AbstractRefreshJob extends Job {

	protected List<FeedsEntry> entries = new ArrayList<FeedsEntry>();
	protected Throwable exception;
	protected long cacheModified;
	private String urlString;
	private File cacheFile;
	private boolean forcedDownload = false;
	private boolean needsRefresh = true;

	public AbstractRefreshJob(String name, String urlString) {
		super(name);
		this.urlString = urlString;
		setPriority(LONG);
		cacheFile = getCacheFile();
		if (cacheFile.exists()) {
			cacheModified = cacheFile.lastModified();
			try {
				getEntries(cacheFile);
			} catch (FileNotFoundException e) {
				JBossCentralActivator.log(e);
			} catch (IllegalArgumentException e) {
				JBossCentralActivator.log(e);
			} catch (FeedException e) {
				JBossCentralActivator.log(e);
			} catch (IOException e) {
				JBossCentralActivator.log(e);
			}
		}
	}

	public abstract File getCacheFile();
	
	public abstract File getValidCacheFile();

	protected void getEntries(File file) throws IOException,
			IllegalArgumentException, FeedException {
		entries.clear();
		needsRefresh = true;
		InputStream in = new FileInputStream(file);
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed syndFeed = input.build(new XmlReader(in));
		List<SyndEntry> feeds = syndFeed.getEntries();
		if (feeds == null || feeds.size() == 0) {
			return;
		}
		int i = 0;

		for (SyndEntry feed : feeds) {
			FeedsEntry entry = adaptEntry(feed);
			if (entry == null) {
				continue;
			}
			if (i++ > JBossCentralActivator.MAX_FEEDS) {
				break;
			}
			entries.add(entry);
		}
	}

	private FeedsEntry adaptEntry(SyndEntry entry) {
		if (entry == null) {
			return null;
		}
		String title = null;
		if (entry.getTitle() != null) {
			title = entry.getTitle();
		} else {
			SyndContent titleEx = entry.getTitleEx();
			if (titleEx != null && !titleEx.getValue().isEmpty()) {
				title = titleEx.getValue();
			}
		}
		if (title == null) {
			return null;
		}
		title = StringEscapeUtils.escapeHtml(title);
		String link;
		if (entry.getLink() != null) {
			link = entry.getLink();
		} else {
			link = entry.getUri();
		}
		String description = null;
		if (entry.getDescription() != null) {
			SyndContent desc = entry.getDescription();
			if (desc != null && !desc.getValue().isEmpty()) {
				description = desc.getValue();
			}
		}
		if (description == null) {
			List<SyndContent> contents = entry.getContents();
			if (contents != null && contents.size() > 0) {
				SyndContent desc = contents.get(0);
				if (desc != null && !desc.getValue().isEmpty()) {
					description = desc.getValue();
				}
			}
		}
		
		Date date;
		if (entry.getUpdatedDate() != null) {
			date = entry.getUpdatedDate();
		} else {
			date = entry.getPublishedDate();
		}
		String author = entry.getAuthor();
		if (author != null) {
			author = StringEscapeUtils.escapeHtml(author);
		}

		// description = "&nbsp; " + description;
		return new FeedsEntry(title, link, description, entry.getAuthor(), date);
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public List<FeedsEntry> getEntries() {
		return entries;
	}

	public String getUrlString() {
		return urlString;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		exception = null;
		long urlModified = -1;
		if (!forcedDownload) {
			urlModified = getUrlModified();
			if (exception != null) {
				return Status.CANCEL_STATUS;
			}
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		if (forcedDownload || cacheModified == 0 || urlModified != cacheModified) {
			try {
				File tempFile = File.createTempFile("news", ".xml");
				tempFile.deleteOnExit();
				OutputStream destination = new FileOutputStream(tempFile);
				IStatus status = ECFExamplesTransport.getInstance().download(
						cacheFile.getName(), urlString, destination, monitor);
				URL url = getURL();
				if (monitor.isCanceled()) {
					return getValidEntries(monitor);
				}
				if (status.isOK() && url != null) {
					cacheModified = ECFExamplesTransport.getInstance().getLastModified(url);
					ProjectExamplesActivator.copyFile(tempFile, cacheFile);
					tempFile.delete();
					if (monitor.isCanceled()) {
						return getValidEntries(monitor);
					}
					cacheFile.setLastModified(cacheModified);
					getEntries(cacheFile);
				} else {
					setException(status.getException());
				}
			} catch (Exception e) {
				return getValidEntries(monitor);
			}
		}
		if (monitor.isCanceled()) {
			return getValidEntries(monitor);
		}
		if (getEntries().size() > 0) {
			try {
				File validCacheFile = getValidCacheFile();
				if (!validCacheFile.isFile() || cacheFile.lastModified() != validCacheFile.lastModified()) {
					ProjectExamplesActivator.copyFile(cacheFile, getValidCacheFile());
					validCacheFile.setLastModified(cacheFile.lastModified());
				}
			} catch (Exception e) {
				exception = e;
				return getValidEntries(monitor);
			}
		} else {
			return getValidEntries(monitor);
		}
		return Status.OK_STATUS;
	}

	protected IStatus getValidEntries(IProgressMonitor monitor) {
		File file = getValidCacheFile();
		if (file.isFile()) {
			try {
				getEntries(file);
			} catch (Exception e) {
				exception = e;
				return Status.CANCEL_STATUS;
			}
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	

	private long getUrlModified() {
		URL url = getURL();
		if (exception != null) {
			return -1;
		}
		long urlModified;
		try {
			urlModified = ECFExamplesTransport.getInstance().getLastModified(
					url);
		} catch (CoreException e) {
			exception = e;
			urlModified = -1;
		}
		return urlModified;
	}

	private URL getURL() {
		URL url;
		try {
			url = new URL(urlString);
			return url;
		} catch (MalformedURLException e) {
			exception = e;
			return null;
		}
	}

	public boolean isForcedDownload() {
		return forcedDownload;
	}

	public void setForcedDownload(boolean forced) {
		this.forcedDownload = forced;
	}

	public boolean needsRefresh() {
		return needsRefresh;
	}

	public void setNeedsRefresh(boolean needRefresh) {
		this.needsRefresh = needRefresh;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
	}

}
