package org.jboss.tools.central.model;

import java.util.Date;

import org.jboss.tools.central.JBossCentralActivator;

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
		buffer.append("<span font=\"default\">");
		if (date != null) {
			buffer.append("posted on ");
			buffer.append(date);
			cr = true;
		}
		buffer.append("</span>");
		if (author != null && !author.isEmpty()) {
			buffer.append(" ");
			buffer.append("<span font=\"default\">");
			buffer.append(" by");
			buffer.append("</span>");
			buffer.append(" ");
			buffer.append("<span font=\"author\">");
			buffer.append(author);
			buffer.append("</span>");
			cr = true;
		}
		
		if (cr) {
			buffer.append("<br />");
		}
		//buffer.append("<br />");
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		return buffer.toString();
	}

	@Override
	public String toString() {
		return "NewsEntry [title=" + title + ", link=" + link
				+ ", description=" + description + ", author=" + author
				+ ", date=" + date + "]";
	}

}
