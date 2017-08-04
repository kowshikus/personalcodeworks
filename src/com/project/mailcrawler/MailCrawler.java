package com.project.mailcrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.DOMParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MailCrawler {
	private static int NO_OF_THREADS = 100;
	public static void main(String[] args) throws ParserConfigurationException, SAXException {
		long starttime = new Date().getTime();
		Map<String, List<MailDisplayModel>> mailTableByYear = MailCrawler.getMailTableByYear("http://mail-archives.apache.org/mod_mbox/maven-users/",2002);
		if(mailTableByYear!=null){
			try {
				File dir = new File(".");
				String outputFolder = dir.getCanonicalPath() + File.separator +"output";
				File output = new File(outputFolder);
				if(!output.exists())output.mkdirs();
				createEmailResults(mailTableByYear,outputFolder);
				long endtime = new Date().getTime();
				System.out.println("Total Execution time : "+(endtime-starttime)/1000+" secs");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			System.out.println("Failed to parse data");
	}
	
	public static Map<String,List<MailDisplayModel>> getMailTableByYear(String Url, int year) {
		Map<String,List<MailDisplayModel>> montlyMailMap = new HashMap<String,List<MailDisplayModel>>();

		Document doc = null;
		try {
			doc = Jsoup.connect(Url).get();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		Elements yearTable = doc.select("table:eq(0):has(th:contains("+year+"))");

		Elements monthlyLinks = yearTable.select("a:contains(thread)");
		for (Element monthLink : monthlyLinks) {
			String threadHref = monthLink.attr("abs:href");
			String monthName = monthLink.parent().parent().previousElementSibling().html();
			String monthlyLinkUrl = threadHref.replace("/thread", "/ajax/thread?");
			int totalPages = getTotalPages(monthName,monthlyLinkUrl);
			System.out.println("Number of pages in the month("+monthName+") - "+totalPages);
			processMonthlyMails(monthName,monthlyLinkUrl,montlyMailMap,totalPages);			
		}
		return montlyMailMap;
		
	}
	
	private static int getTotalPages(String monthName, String monthlyLinkUrl) {
		int pageCount = 1;
		String monthlyXmlResponse = MailCrawlerUtil.makeRestGetServiceCall(monthlyLinkUrl);

		DOMParser parser = new DOMParser();
		try {
		    parser.parse(new InputSource(new java.io.StringReader(monthlyXmlResponse)));
		    org.w3c.dom.Document doc = parser.getDocument();
		    Node indexNode = doc.getElementsByTagName("index").item(0);
		    Node namedItem = indexNode.getAttributes().getNamedItem("pages");
		    String pages = namedItem.getTextContent();
		    pageCount = Integer.parseInt(pages);
		} catch (SAXException|IOException e) {
		    e.printStackTrace(); 
		}

		return pageCount;
	}
	
	
	
	private static void createEmailResults(Map<String, List<MailDisplayModel>> mailTableByYear, String outputFolder) {
		String currentTestMainFolder = outputFolder + File.separator+"TestResult"+new Date().getTime();
		File currentTestMainFolderFile = new File(currentTestMainFolder);
		currentTestMainFolderFile.mkdirs();
		
		for (Map.Entry<String, List<MailDisplayModel>> entry : mailTableByYear.entrySet()) {
			String innerMonthFolderPath = currentTestMainFolder+ File.separator+entry.getKey();
			File innerMonthFolderFile = new File(innerMonthFolderPath);
			innerMonthFolderFile.mkdirs();
			List<MailDisplayModel> mailValues = entry.getValue();
			for(int i=0;i<mailValues.size();i++){
				String fileName= innerMonthFolderFile+File.separator+"MailNo#"+(i+1)+".txt";
				File newFile = new File(fileName);
				try {
					newFile.createNewFile();
		          FileWriter writer = new FileWriter(newFile);
					writer.write("FROM    - "+mailValues.get(i).getFrom()+"\r\n");
					writer.write("DATE    - "+mailValues.get(i).getDate()+"\r\n");
					writer.write("SUBJECT - "+mailValues.get(i).getSubject()+"\r\n");
					writer.write("CONTENT - "+mailValues.get(i).getContent());
		          writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	
	// SINGLE THREADED APPROACH
/*	private static void processMonthlyMails(String monthName, String monthlyLinkUrl, Map<String, List<MailDisplayModel>> montlyMailMap, int totalPages) {
		List<String> messageIds = new ArrayList<String>();
		for(int i=0;i<totalPages;i++){
			String monthlyLinkPageUrl = monthlyLinkUrl+i;
			String monthlyXmlResponse = MailCrawlerUtil.makeRestGetServiceCall(monthlyLinkPageUrl);
			if(!monthlyXmlResponse.isEmpty()){
				messageIds.addAll(extractIndividualMailMessageId(monthlyXmlResponse));				
			}
		}
		System.out.println("Total messages in month("+monthName+") - "+messageIds.size());
		if(messageIds.size()>0){
			List<MailDisplayModel> monthMailList= new ArrayList<MailDisplayModel>();
			String mailAjaxUrl = monthlyLinkUrl.replace("/thread?", "/");
			for(String msgId : messageIds){
				String individualMailXmlResponse = MailCrawlerUtil.makeRestGetServiceCall(mailAjaxUrl+MailCrawlerUtil.getEncodedText(msgId));
				if(!individualMailXmlResponse.isEmpty()){
					monthMailList.add(parseMailXmlResponseToModel(individualMailXmlResponse));
				}
			}
			montlyMailMap.put(monthName, monthMailList);
		}
	}*/
	
	// MULTITHREADED THREADED APPROACH
	private static void processMonthlyMails(String monthName, String monthlyLinkUrl, Map<String, List<MailDisplayModel>> montlyMailMap, int totalPages) {
		List<String> messageIds = new ArrayList<String>();
		for(int i=0;i<totalPages;i++){
			String monthlyLinkPageUrl = monthlyLinkUrl+i;
			String monthlyXmlResponse = MailCrawlerUtil.makeRestGetServiceCall(monthlyLinkPageUrl);
			if(!monthlyXmlResponse.isEmpty()){
				messageIds.addAll(extractIndividualMailMessageId(monthlyXmlResponse));				
			}
		}
		System.out.println("Total messages in month("+monthName+") - "+messageIds.size());
		if(messageIds.size()>0){
			List<MailDisplayModel> monthMailList= new ArrayList<MailDisplayModel>();
			montlyMailMap.put(monthName, monthMailList);
			String mailAjaxUrl = monthlyLinkUrl.replace("/thread?", "/");
			ExecutorService executor = Executors.newFixedThreadPool(NO_OF_THREADS);
			for(String msgId : messageIds){
				MailCrawlerRunnable worker = new MailCrawlerRunnable(mailAjaxUrl,msgId,montlyMailMap,monthName);
				executor.execute(worker);
			}
			executor.shutdown();
			// Wait until all threads to terminate
			while (!executor.isTerminated()) {}
			
		}

	}
	
	static MailDisplayModel parseMailXmlResponseToModel(String xml){
		MailDisplayModel response = new MailDisplayModel();
		DOMParser parser = new DOMParser();
		try {
		    parser.parse(new InputSource(new java.io.StringReader(xml)));
		    org.w3c.dom.Document doc = parser.getDocument();
		    NodeList childNodes = doc.getElementsByTagName("mail");
		    Node mailItem = childNodes.item(0);
		    org.w3c.dom.Element mailItemElement = (org.w3c.dom.Element)mailItem;  
		    response.setFrom(mailItemElement.getElementsByTagName("from").item(0).getFirstChild().getNodeValue());
		    response.setSubject( mailItemElement.getElementsByTagName("subject").item(0).getFirstChild().getNodeValue());
		    response.setDate(mailItemElement.getElementsByTagName("date").item(0).getFirstChild().getNodeValue());
		    response.setContent(mailItemElement.getElementsByTagName("contents").item(0).getFirstChild().getNodeValue());
		} catch (SAXException|IOException e) {
		    e.printStackTrace(); 
		}
		return response;
	}

	private static List<String> extractIndividualMailMessageId(String xml){
		List<String> messageIds = new ArrayList<String>();
		DOMParser parser = new DOMParser();
		try {
		    parser.parse(new InputSource(new java.io.StringReader(xml)));
		    org.w3c.dom.Document doc = parser.getDocument();
		    NodeList childNodes = doc.getElementsByTagName("message");
		    for (int a = 0; a < childNodes.getLength(); a++) 
		    {
	            Node theAttribute = childNodes.item(a);
	            Node namedItem = theAttribute.getAttributes().getNamedItem("id");
	            messageIds.add(namedItem.getNodeValue());
		    }
		} catch (SAXException|IOException e) {
		    e.printStackTrace(); 
		}
		return messageIds;
	}
}
