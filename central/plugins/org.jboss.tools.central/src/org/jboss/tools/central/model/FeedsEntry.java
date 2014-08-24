/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.model;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.tools.central.JBossCentralActivator;

import com.ocpsoft.pretty.time.PrettyTime;

/**
 * 
 * @author snjeza
 *
 */
public class FeedsEntry {
	
	public enum Type {
		BLOG, TWITTER
	}
	
	private String title;
	private String link;
	private String description;
	private String author;
	private Date date;
	private Type type;
	
	public FeedsEntry() {
	}

	public FeedsEntry(String title, String link, String description,
			String author, Date date) {
		this(title, link, description, author, date, Type.BLOG);
	}

	public FeedsEntry(String title, String link, String description,
			String author, Date date, Type type) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.author = author;
		this.date = date;
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public FeedsEntry.Type getType() {
		return type;
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

	public String getFormString(boolean escapeXml) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		buffer.append("<img href=\"image\"/> ");
		if (link != null && !link.isEmpty()) {
			buffer.append("<a href=\"");
			buffer.append(link);
			buffer.append("\">");
			buffer.append(escapeXml(title, escapeXml));
			buffer.append("</a>");
		} else {
			buffer.append(escapeXml(title, escapeXml));
		}
		//buffer.append("<br/>");
		boolean cr = false;
		if (date != null) {
			/*buffer.append("<span font=\"default\">");
			buffer.append("posted ");
			buffer.append("</span>");*/
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
			buffer.append(escapeXml(author, escapeXml));
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
			buffer.append(escapeXml(shortDescription, escapeXml));
			buffer.append("</span>");
			cr = true;
		}
		if (cr) {
			buffer.append("<br/><br/>");
		}
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		return buffer.toString();
	}

	public String getShortString(boolean escapeXml) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		buffer.append("<img href=\"image\"/> ");
		if (link != null && !link.isEmpty()) {
			buffer.append("<a href=\"");
			buffer.append(link);
			buffer.append("\">");
			buffer.append(escapeXml(title, escapeXml));
			buffer.append("</a>");
		} else {
			buffer.append(escapeXml(title, escapeXml));
		}
		//buffer.append("<br/>");
		boolean cr = false;
		if (date != null) {
			/*buffer.append("<span font=\"default\">");
			buffer.append("posted ");
			buffer.append("</span>");*/
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
			buffer.append(escapeXml(author, escapeXml));
			buffer.append("</span>");
			cr = true;
		}
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		return buffer.toString();
	}

	protected String escapeXml(String text, boolean escape) {
		text = StringEscapeUtils.unescapeHtml(text);
		if (escape) {
			text = StringEscapeUtils.escapeXml(text);
		}
		text = text.replaceAll("&nbsp;", "&#160;"); 
		text = text.replaceAll("& ", "&#38; ");
		return text;
	}
	
	public String getShortDescription() {
		if (description == null) {
			return null;
		}
		boolean tagStarted = false;
		StringBuilder buffer = new StringBuilder();
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
		buffer = new StringBuilder();
		int i = 0;
		for (char c:chars) {
			if (i++ < 180) {
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
		return "FeedsEntry [title=" + title + ", link=" + link
				+ ", description=" + description + ", author=" + author
				+ ", date=" + date + "]";
	}

}
