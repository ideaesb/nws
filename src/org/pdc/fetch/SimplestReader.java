package org.pdc.fetch;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.syndication.io.SyndFeedInput;

import com.sun.syndication.feed.atom.Entry;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import org.xml.sax.InputSource;

import org.apache.commons.lang3.StringUtils;

public class SimplestReader {
	public static void main(String[] args) {
		
		////////////////////////////////////////////////////////////////
		// Timeouts - to be trapped later - just set to maximum for now 
		int connectTimeoutinMilliseconds = 2147483647;
		int readTimeoutinMilliseconds = 2147483647;
		
		
		///////////////////////////////////////////////////////////////////////
		//  Can be externalized to command line, properties or resource bundle 
		
		String feedURL = "http://alerts.weather.gov/cap/us.php?x=0";
		
		String logsDirectory = "./logs";
		String cacheDirectory = "./cache";
		String candidateHazardsDirectory = "./candidateHazards";
		
		String cacheFileName = "last.xml";
		

		File logs = new File(logsDirectory);
        if (!logs.exists()) logs.mkdirs();

		
		File cache = new File(cacheDirectory);
        if (!cache.exists()) cache.mkdirs();
		
		File candidateHazards = new File(candidateHazardsDirectory);
        if (!candidateHazards.exists()) candidateHazards.mkdirs();
		
        
        
        //////////////////////////////////////////////
        // determines if last snap-shot (cache) exists 
		boolean cacheExists = false;
		File lastXml = new File(cache, cacheFileName);
		if (lastXml.exists() && lastXml.length() > 0 && lastXml.canRead()) cacheExists = true;
		
		boolean dbCacheExists = false; 
        // TODO see if there is anything in the database - 		
		
		
		
		// Now try parsing the feed URL 
		SyndFeed feed = null;
		try
		{
			feed = new SyndFeedInput().build(new InputSource(feedURL));
		}
		catch (Exception e)
		{
			System.err.println("Unable to reach/parse feed URL " + feedURL );
			e.printStackTrace();
			System.exit(1);
		}

        @SuppressWarnings("unchecked")
  	    List<SyndEntry> feedEntries = feed.getEntries();

		// load the file cache - last snaphot
		SyndFeed cachedFeed = null;
  	    List<SyndEntry> cachedEntries = null;  
        try
		{
			if (cacheExists) cachedFeed = new SyndFeedInput().build(lastXml);
			cachedEntries = cachedFeed.getEntries();
		}
		catch (Exception e)
		{
			cacheExists = false;
			// Perfectly normal - just log info
			//System.err.println("Unable to reach/parse cached data file " + lastXml);
			//e.printStackTrace();
			
		}
		
		
		// Sanity check cachedEntries, set cacheExists flag accordingly for rest of program 
        if (cachedEntries == null || cachedEntries.size() == 0) cacheExists = false; 
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		// build rudimentary comparisons of the two feeds - can make this as sophisticated as necessary 
		if (cacheExists && (feed.getEntries().size() == cachedFeed.getEntries().size()))
		{
           // test two - published date, and so on can build more hoops to jump through as inner ifs  
		   if (feed.getPublishedDate().equals(cachedFeed.getPublishedDate()))
           {
			  System.out.println("No Change.  Current Local Time  " +  new Date (System.currentTimeMillis()) + ", Last NWS Release " + feed.getPublishedDate());
			  System.exit(0);
           }
		}
		
		
		/////////////////////////////////////////
		//  Just get the entire text of the feed 
		
		 String inputLine;
		 String lastRawFeed = "";
		 try
		 {
		   URL url = new URL(feedURL);
		   URLConnection conn = url.openConnection();
		   
		   // setting timeouts 
		   conn.setConnectTimeout(connectTimeoutinMilliseconds);
		   conn.setReadTimeout(readTimeoutinMilliseconds);


		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    StringBuilder  sb = new StringBuilder ();          
		    while ((inputLine = in.readLine()) != null) 
		    {
		      sb.append(inputLine);
		    }

		    in.close();
		    
		    lastRawFeed = sb.toString();

		 }
		 catch (Exception e)
		 {
		   e.printStackTrace();
		 }
		
		if (StringUtils.isBlank(lastRawFeed))
		{
			System.err.println("Unable to reach/parse all the text from feed URL " + feedURL );
			System.exit(1);
		}
		
		if (cacheExists && dbCacheExists)
		{   
			///////////////////////////////////////////////////////////////////////////////////////////////////
			// just new entries to candidateHazardsDirectory, compared with BOTH file cache AND database cache  
		}
		else if (cacheExists)
		{
			// just new entries to be printed - file cache only 
		}
		else if (dbCacheExists)
		{
			// just new entries to be printed - data base cache only
		}
		else
		{
		    //////////////////////////////////////////////
			// no file cache or database cache 
			// fresh file and database caches are created - attempted
			// just to be sure only new entry files are written
			
			
			
		}
	
        MyConverterForAtom10 converter = new MyConverterForAtom10();
        
		for (SyndEntry syndentry : feedEntries)
		{
			Entry entry = converter.entryFromSyndEntry(syndentry);
			//System.out.println(StringUtils.substringAfterLast(syndentry.getLink(), "?x=") + "." + syndentry.getPublishedDate().getTime());
			System.out.println(syndentry.toString());
		}
		
		
		// Lastly, refresh file cache, append database cache
		
		if (lastXml.exists())
		{
			if (lastXml.delete())
			{
				
			}
			else
			{
				System.err.println("Warning - Could not DELETE " + lastXml + ", Probably locked by another editor or app -- No Exceptions to Report");
			}
		}
		
		try
		{
		  if (lastXml.createNewFile())
		  {
		    try
			{
			  FileWriter fwr = new FileWriter(lastXml, false);
			  fwr.write(StringUtils.replace(lastRawFeed, "xmlns", " xmlns"));
			  fwr.close();
			}
			catch (Exception e)
			{
				System.err.println("Warning - Could not WRITE " + lastXml);	
			}
		}
		else
		{
			System.err.println("Warning - Did not CREATE " + lastXml + ", Probably Already Exists/Could not delete - No Exceptions to Report");
		}
	  }
	  catch (Exception e)
	  {
		  System.err.println("Warning - Could not CREATE " + lastXml + ", Exception --->");
		  System.err.println(e);
	  }

		
		
		
		
		
		
  }

}
