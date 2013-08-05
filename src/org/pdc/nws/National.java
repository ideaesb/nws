package org.pdc.nws;

import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.feed.CapFeedParser;


import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;


import org.apache.commons.lang3.StringUtils;




/**
 * Parses the national atom feed from alerts.weather.gov 
 */
public class National 
{


	
	public static void main( String[] args )
    {
		
		//////////////////////////////////////////////////////////////
		// defaults
		String sourceURL = "http://alerts.weather.gov/cap/us.php?x=0";
		String destinationDirectory = System.getProperty("user.dir");  
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//  Override with arguments (TODO - get these from properties, other configurations, resource bundles as preferred)
		//
		
        if (args.length == 1) sourceURL = args[0];
        if (args.length == 2) 
        {
        	sourceURL = args[0];
        	destinationDirectory = args[1];
        	File theDir = new File(destinationDirectory);

        	  // if the directory does not exist, create it
        	  if (!theDir.exists()) 
        	  {
        	    // try to create it
        	     if(!theDir.mkdir()) 
        	     {  
        	    	// Fail, advise to omit the second parameter - user should create directory manually with
        	    	// proper privileges on the system to write and run this program from there ("user.dir")
        	        System.err.println("Could not find or create the Alerts directory " + destinationDirectory); 
        	        System.out.println("Omit the the directory parameter to create alerts in current directory");
        	        System.exit(1);
        	     }
        	  }        
        }
		
        
        
		URL url = null;
		String inputLine;
		
		////////////////////////////////////////////////////////////////
		// Timeouts - to be trapped later - just set to maximum for now 
		int connectTimeoutinMilliseconds = 2147483647;
		int readTimeoutinMilliseconds = 2147483647;
		

		try
		{

		  // open the NWS ATOM FEED as URL 
		  url = new URL(sourceURL);
          URLConnection conn = url.openConnection();
          // setting timeouts 
          conn.setConnectTimeout(connectTimeoutinMilliseconds);
          conn.setReadTimeout(readTimeoutinMilliseconds);
          BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          

          ///////////////////////////////////////////////////////////////////////////
          // construct cap feed parser with boolean validate
          // TODO: Research if atomSmasher parser can be done better( perhaps ROME)
          // All this is doing is getting SyndEntry 
          CapFeedParser atomSmasher = new CapFeedParser(true); 
          
          SyndFeed feed = atomSmasher.parseFeed(in);
          in.close();
	      
          @SuppressWarnings("unchecked")
		  List<SyndEntry> entries = feed.getEntries();
		  
          // to be used to parse alerts, persist into database  
          List<Alert> alerts = new ArrayList<Alert>();
          
          // Iterate over ATOM entries 
		  for (SyndEntry entry : entries)
		  {
			  // generate unique filename from the embedded CAP URL
			  // TODO: Better idiot-proof way to generate unique filenames, perhaps date stamp 
			  String fname = StringUtils.substringAfterLast(entry.getLink(), "?x=") + ".xml";
			  File xmlfile = new File(destinationDirectory, fname);

			  // Bypass previously captured alerts
			  // TODO: Much more robust comparator (than filename!) to eliminate duplicates 
	          if (!xmlfile.exists())
	          {
	        	  ///////////////////////////////////////////////////////////////////////////////////////////////
	        	  // Just capture the CAP Alert(s) and put it in alerts or current dir - Yes...possibly plural(?)
	        	  // TODO: Verify - Ensure/Guard against multiple CAP alerts embedded (necessary? 
	        	  
				  URL capUrl = new URL(entry.getLink());
				  URLConnection con = capUrl.openConnection();
		          con.setConnectTimeout(connectTimeoutinMilliseconds);
		          con.setReadTimeout(readTimeoutinMilliseconds);
		          in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		          StringBuffer sb = new StringBuffer();

		          while ((inputLine = in.readLine()) != null) sb.append(inputLine);
		          in.close();
		          
		          if (xmlfile.createNewFile())
		          {
	        	    FileWriter fwr = new FileWriter(xmlfile, false);
	        	    fwr.write(sb.toString());
	        	    fwr.close();
		          }
	          }
	          
	          ///////////////////////////////////////////////////////////////////////////////////
	          //  This is where the process may be extended to immediately persist into database 
	          //   Due to the dynamic nature of feed, possible locking issues, out-of-sync
	          //   Perhaps a parellel process?  In any case, an excellent test of CAP library 
	          //   And also a user-guide (hint) of how the alerts directory may be post-processed 
	          /*
	          Alert alert = atomSmasher.parseAlert(sb.toString());
	          alerts.add(alert);
	          */
		  }          
          
		  // echo alert note for debugging....
		  for (Iterator<Alert> i = alerts.iterator(); i.hasNext();) {
             Alert alert = (Alert) i.next();
             System.out.println(alert.getNote());
          }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
    }	
	
}
