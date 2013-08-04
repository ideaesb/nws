package org.pdc.nws;

import java.net.*;
import java.io.*;
import java.util.*;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.CapException;
import com.google.publicalerts.cap.NotCapException;
import com.google.publicalerts.cap.CapException.Reason;
import com.google.publicalerts.cap.feed.*;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.synd.*;

import org.jdom.Element;




/**
 * Parses the national atom feed from alerts.weather.gov 
 */
public class National {

	private static void out(String s)
	{
		System.out.println(s);
	}

	
	public static void main( String[] args )
    {
		URL url = null;
		String inputLine;
		
		//////////////////////////////////////////////
		// Timeouts - to be trapped later 
		int connectTimeoutinMilliseconds = 1000;
		int readTimeoutinMilliseconds = 1000;
		

		try
		{
          url = new URL("http://alerts.weather.gov/cap/us.php?x=0");
          URLConnection conn = url.openConnection();
          // setting timeouts 
          conn.setConnectTimeout(connectTimeoutinMilliseconds);
          conn.setReadTimeout(readTimeoutinMilliseconds);
          BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          
          /* echo
          while ((inputLine = in.readLine()) != null) {
        	  out(inputLine);
          }
          */
          
          // construct cap feed parser with boolean validate
          CapFeedParser atomSmashher = new CapFeedParser(true); 
          
          SyndFeed feed = atomSmashher.parseFeed(in);
	      
          List<SyndEntry> entries = feed.getEntries();
          //out("Size = " + entries.size());
		  List<Alert> alerts = new ArrayList<Alert>();
          
		  for (SyndEntry entry : entries)
		  {
			  System.out.println(entry.getTitle());
				System.out.println(entry.getUri());
				System.out.println(entry.getDescription().getValue());
				List<Element> foreignMarkups = (List<Element>) entry.getForeignMarkup();
				for (Element foreignMarkup : foreignMarkups) {
					
					System.out.println("Foreign markup "
							+ foreignMarkup.getNamespaceURI() + "/"
							+ foreignMarkup.getName() + " = "
							+ foreignMarkup.getText());
                    
					// look one level below
					for (Element child : (List<Element>) foreignMarkup.getChildren())
					{
						System.out.println("Child markup "
								+ child.getNamespaceURI() + "/"
								+ child.getName() + " = "
								+ child.getText());
					}
					
					//out(foreignMarkup.getText());
				}
				System.out.println("-----------------");

		  }          
          
		  
		  for (Iterator i = alerts.iterator(); i.hasNext();) {
             Alert alert = (Alert) i.next();
             System.out.println(alert.getNote());
          }
          
          in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
    }	
	
}
