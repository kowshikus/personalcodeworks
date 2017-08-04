package com.project.mailcrawler;

import org.apache.commons.lang.StringEscapeUtils;

public class MailDisplayModel {
	private String from;
	private String subject;
	private String date;
	private String content;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = StringEscapeUtils.unescapeHtml(from);
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = StringEscapeUtils.unescapeHtml(content);
	}
	@Override
	public String toString() {
		return "MailDisplayModel [from=" + from + ", subject=" + subject + ", date=" + date + ", content=" + content
				+ "]";
	}
	
	

}
