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

import org.apache.commons.lang3.*;
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
		int connectTimeoutinMilliseconds = 2147483647;
		int readTimeoutinMilliseconds = 2147483647;
		

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
          CapFeedParser atomSmasher = new CapFeedParser(true); 
          
          SyndFeed feed = atomSmasher.parseFeed(in);
          in.close();
	      
          List<SyndEntry> entries = feed.getEntries();
          //out("Size = " + entries.size());
		  List<Alert> alerts = new ArrayList<Alert>();
          
		  for (SyndEntry entry : entries)
		  {
			  
			  String fname = "c:/temp/alerts/" + StringUtils.substringAfterLast(entry.getLink(), "?x=") + ".xml";
			  File xmlfile = new File(fname);
	          
	          if (!xmlfile.exists())
	          {
				  URL capUrl = new URL(entry.getLink());
				  
				  URLConnection con = capUrl.openConnection();
		          // setting timeouts 
		          con.setConnectTimeout(connectTimeoutinMilliseconds);
		          con.setReadTimeout(readTimeoutinMilliseconds);
		          in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		          StringBuffer sb = new StringBuffer();
		          while ((inputLine = in.readLine()) != null) sb.append(inputLine);
		          in.close();
		          out(entry.getTitle() + "Link: " + entry.getLink());
	        	  FileWriter fwr = new FileWriter(fname, false);
	        	  fwr.write(sb.toString());
	        	  fwr.close();
	          }
	          
	          /*
	          Alert alert = atomSmasher.parseAlert(sb.toString());
	          alerts.add(alert);
	          */
		  }          
          
		  
		  for (Iterator i = alerts.iterator(); i.hasNext();) {
             Alert alert = (Alert) i.next();
             System.out.println(alert.getNote());
          }
          
          //in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
    }	
	
}
