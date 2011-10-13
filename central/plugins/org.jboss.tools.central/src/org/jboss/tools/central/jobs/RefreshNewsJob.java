package org.jboss.tools.central.jobs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.model.NewsEntry;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RefreshNewsJob extends Job {

	private List<NewsEntry> entries = new ArrayList<NewsEntry>();
	private Exception exception;
	public static RefreshNewsJob INSTANCE = new RefreshNewsJob(JBossCentralActivator.NEWS_ATOM_URL);
	
	private String newsurl;
	
	private RefreshNewsJob(String newsurl) {
		super("Refreshing JBoss News...");
		setPriority(LONG);
		this.newsurl=newsurl;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		entries.clear();
		SyndFeedInput input = new SyndFeedInput();
		URL url;
		try {
			url = new URL(newsurl);
		} catch (MalformedURLException e) {
			exception = e;
			return Status.CANCEL_STATUS;
		}
		try {
			SyndFeed syndFeed = input.build(new XmlReader(url));
			List<SyndEntry> feeds = syndFeed.getEntries();
			if (feeds == null || feeds.size() == 0) {
				return Status.OK_STATUS;
			}
			int i = 0;
			
			for (SyndEntry feed:feeds) {
				NewsEntry entry = adaptEntry(feed);
				if (entry == null) {
					continue;
				}
				if (i++ > JBossCentralActivator.MAX_FEEDS) {
					break;
				}
				entries.add(entry);
			}
		} catch (Exception e) {
			exception = e;
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	
	private NewsEntry adaptEntry(SyndEntry entry) {
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
		
		//description = "&nbsp; " + description;
		return new NewsEntry(title, link, description, entry.getAuthor(), date);
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public List<NewsEntry> getEntries() {
		return entries;
	}
}
