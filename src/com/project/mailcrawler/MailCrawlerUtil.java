package com.project.mailcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MailCrawlerUtil {

	
	public static String getEncodedText(String text){
		String encodedText="";
		try {
			encodedText = URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encodedText;
	}
	
	public static String makeRestGetServiceCall(String getUrl){
		StringBuffer response = new StringBuffer();
		try{
			URL url = new URL(getUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
	
			int responseCode = con.getResponseCode();
			if(responseCode==200){
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
		}catch(IOException e){
			System.out.println("Failed to make service call. Exception - "+e.getMessage());
			e.printStackTrace();
		}
		
		return response.toString();
		
	}
}
