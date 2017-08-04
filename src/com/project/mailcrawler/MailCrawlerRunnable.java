package com.project.mailcrawler;

import java.util.List;
import java.util.Map;

public class MailCrawlerRunnable implements Runnable {
	private String mailAjaxUrl;
	private String messageId;
	private Map<String, List<MailDisplayModel>> montlyMailMap;
	private String monthName;
	public MailCrawlerRunnable(String mailAjaxUrl, String messageId,
			Map<String, List<MailDisplayModel>> montlyMailMap,String monthName) {
		super();
		this.mailAjaxUrl = mailAjaxUrl;
		this.messageId = messageId;
		this.montlyMailMap = montlyMailMap;
		this.monthName = monthName;
	}


	@Override
	public void run() {

		String individualMailXmlResponse = MailCrawlerUtil.makeRestGetServiceCall(mailAjaxUrl+MailCrawlerUtil.getEncodedText(messageId));
		if(!individualMailXmlResponse.isEmpty()){
			MailDisplayModel parseMailXmlResponseToModel = MailCrawler.parseMailXmlResponseToModel(individualMailXmlResponse);
			montlyMailMap.get(monthName).add(parseMailXmlResponseToModel);
		}
	}
}