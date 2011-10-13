package org.jboss.tools.central.model;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.tools.central.JBossCentralActivator;

import com.ocpsoft.pretty.time.PrettyTime;

public class NewsEntry {
	private String title;
	private String link;
	private String description;
	private String author;
	private Date date;

	public NewsEntry() {
	}

	public NewsEntry(String title, String link, String description,
			String author, Date date) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.author = author;
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFormString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		buffer.append("<img href=\"image\"/> ");
		if (link != null && !link.isEmpty()) {
			buffer.append("<a href=\"");
			buffer.append(link);
			buffer.append("\">");
			buffer.append(title);
			buffer.append("</a>");
		} else {
			buffer.append(title);
		}
		buffer.append("<br/>");
		boolean cr = false;
		if (date != null) {
			buffer.append("<span font=\"default\">");
			buffer.append("posted ");
			buffer.append("</span>");
			buffer.append("<b>");
			PrettyTime prettyTime = new PrettyTime(new Date());
			buffer.append("&#160;" + prettyTime.format(date));
			buffer.append("</b>");
			cr = true;
		}
		if (author != null && !author.isEmpty() && !"(author unknown)".equalsIgnoreCase(author)) {
			buffer.append(" ");
			buffer.append("<span font=\"default\">");
			buffer.append(" by");
			buffer.append("</span>");
			buffer.append(" ");
			buffer.append("<span color=\"author\" font=\"author\">");
			buffer.append(author);
			buffer.append("</span>");
			cr = true;
		}
		
		if (cr) {
			buffer.append("<br/>");
		}
		String shortDescription = getShortDescription();
		cr = false;
		if (shortDescription != null && !shortDescription.isEmpty()) {
			buffer.append("<span font=\"description\">");
			buffer.append(shortDescription);
			buffer.append("</span>");
			cr = true;
		}
		if (cr) {
			buffer.append("<br/><br/>");
		}
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		return buffer.toString();
	}
	
	public String getShortDescription() {
		if (description == null) {
			return null;
		}
		boolean tagStarted = false;
		StringBuffer buffer = new StringBuffer();
		for (char c:description.toCharArray()) {
			if (c == '<') {
				tagStarted = true;
			}
			if (c == '>') {
				tagStarted = false;
			} else {
				if (!tagStarted) {
					buffer.append(c);
				}
			}
		}
		char[] chars = StringEscapeUtils.unescapeHtml(buffer.toString().trim()).toCharArray();
		buffer = new StringBuffer();
		int i = 0;
		for (char c:chars) {
			if (i++ < 140) {
				buffer.append(c);
			} else {
				if ( (c == '_') ||
					 (c >= 'a' && c <= 'z') ||
					 (c >= 'a' && c <= 'Z') ||
					 (c >= '0' && c <= '9') ) {
					buffer.append(c);
				} else {
					break;
				}
			}
		}
		if (buffer.length() > 0) {
			buffer.append("...");
		}
		return buffer.toString();
	}

	@Override
	public String toString() {
		return "NewsEntry [title=" + title + ", link=" + link
				+ ", description=" + description + ", author=" + author
				+ ", date=" + date + "]";
	}

}
