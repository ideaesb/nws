package org.pdc.fetch;

import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.io.*;
import java.util.*;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main executable 
 * @author Uday
 *
 */
public class SimplestReader {
	
    
	
	public static void main(String[] args) 
	{
		long startMillis = System.currentTimeMillis();
		Logger logger = LoggerFactory.getLogger(SimplestReader.class);
		logger.info("NWS Feed Reader was STARTED at " + new Date(startMillis));
		
		// default URL
		String feedURL = "http://alerts.weather.gov/cap/us.php?x=0";
		String candidateHazardsDirectory = "candidateHazards";
		
		//time to IDLE in cache (seconds) - a week by default
		int tti = 60*60*24*7;  
	    //time to LIVE in seconds - a month by default
		int ttl = 60*60*24*30;
		
		// by default the output is the more readable java properties file type 
		boolean xmlOutput=false; 
		
		//////////////////////////////////////////////////
		// first over-ride of default - properties file 

		PropertiesConfiguration config = null;
		try
		{
		   config = new PropertiesConfiguration("reader.properties"); 
		   feedURL                   = config.getString("feedURL", feedURL);
		   candidateHazardsDirectory = config.getString("candidateHazardsDirectory", candidateHazardsDirectory);
		   tti                       = config.getInt("tti", 604800);
		   ttl                       = config.getInt("ttl", 2592000);
		   xmlOutput                 = config.getBoolean("xmlOutput", false);
	    }
		catch (org.apache.commons.configuration.ConfigurationException e)
		{
		  // do nothing just log and use defaults 
		  logger.warn("Unable to load the configuration (reader.properties) file from classpath " + e.toString());
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// command line over-rides default and config: FOR TESTING ONLY
		if (args.length == 5)
		{
           feedURL                   = args[0];
		   candidateHazardsDirectory = args[1];
		   tti                       = NumberUtils.toInt(args[2], 604800);
		   ttl                       = NumberUtils.toInt(args[3], 2592000);
		   xmlOutput                 = BooleanUtils.toBoolean(args[4]);
		}
		else if (args.length == 4)
		{
           feedURL                   = args[0];
		   candidateHazardsDirectory = args[1];
		   tti                       = NumberUtils.toInt(args[2], 604800);
		   ttl                       = NumberUtils.toInt(args[3], 2592000);
		}
		else if (args.length == 3)
		{
           feedURL                   = args[0];
		   candidateHazardsDirectory = args[1];
		   tti                       = NumberUtils.toInt(args[2], 604800);
		}
		else if (args.length == 2)
		{
           feedURL                   = args[0];
		   candidateHazardsDirectory = args[1];
		}
		else if (args.length == 1)
		{
           feedURL                   = args[0];
		}

		
		// echo inputs/defaults
		logger.info("feed URL = " + feedURL);
		logger.info("candidateHazardsDirectory = " + candidateHazardsDirectory);
		logger.info("Max Time to Idle (TTI) in cache, seconds " + tti); 
		logger.info("Max Time to Live (TTI) in cache, seconds " + ttl);
		if (xmlOutput) logger.info("Entries will be printed in XML format");
		else logger.info("Entries will be printed in .properties format");
		
		File candidateHazards = new File(System.getProperty("user.dir"), candidateHazardsDirectory);
        if (!candidateHazards.exists()) candidateHazards.mkdirs();
        if (candidateHazards.exists() && candidateHazards.isDirectory())
        {
        	logger.info("candidate hazards directory exists " + candidateHazards.getAbsolutePath());
        }
        
		// Now try parsing the feed URL 
		SyndFeed feed = null;
		logger.info("ENTER parse step, building ROME 1.0 Syndicated Feed Object (SyndFeed) from Input Source " + feedURL);
		try
		{
			feed = new SyndFeedInput().build(new InputSource(feedURL));
		}
		catch (Exception e)
		{
			logger.error("Unable to reach/parse feed URL " + feedURL );
			logger.error(e.toString());
			System.exit(1);
		}

        @SuppressWarnings("unchecked")
        List<SyndEntry> entries = feed.getEntries();
        logger.info("Parsed " + entries.size() + " feed entries");
        
        // to be used to hold the new/updated feed entries for output into candidate hazards directory  
        List<PdcEntry> candidates = new ArrayList<PdcEntry>();

        // Fire up the cache 
        DiskCache cache = new DiskCache(tti, ttl);
        logger.info("Size of cache BEGIN " + cache.getSize());
        for (SyndEntry entry : entries)
		{
			// put will only work (return true) if entry is new or updated
			if (cache.put(new PdcEntry(entry)))
			{
				candidates.add(new PdcEntry(entry));
			}
			else
			{
				//
			}
        }
		
		logger.info("Size of cache END " + cache.getSize());
		logger.info("Cache should have increased by " + candidates.size() + " candidated hazards just added to cache (OK? see cache size begin, end).  Will write to disk now...");
	          
		
		for (PdcEntry candidate : candidates)
		{
		  
	      //////////
		  // for xml files use the xml extension
		  File candidateFile = new File(candidateHazards, candidate.getFilename()); 
		  if(xmlOutput) candidateFile = new File(candidateHazards, candidate.getFilename("xml"));
		  
		  try
		  {
			 candidateFile.createNewFile();
		  }
		  catch (Exception e)
		  {
			  logger.error("Was unable to create candidate Hazard file " + candidateFile );
			  logger.error("Title " + candidate.getSyndEntry().getTitle());
			  System.exit(1);
		  }
		  
		  if (candidateFile.exists() && candidateFile.canWrite())
		  {
			try
			{
  		      FileWriter fwr = new FileWriter(candidateFile, false);  // append = false, meaning it will over-write (update, as intended)
  		      
  		      // choice of XML or "properties" type of output
  		      if(xmlOutput) fwr.write(candidate.getXML());
  		      else candidate.write(fwr);
  		      
	          fwr.close();
		    }
		    catch (Exception e)
		    {
			  logger.error("FATAL ERROR writing (the feed entry into) file " + candidateFile );
			  logger.error("Title " + candidate.getSyndEntry().getTitle());
			  logger.error(e.toString());
			  System.exit(1);
		    }
		  }
		  else
		  {
			  logger.error("Was unable to create or, if created, was unable able to write candidate Hazard file " + candidateFile );
			  logger.error("Title " + candidate.getSyndEntry().getTitle());
			  System.exit(1);
		  }
		}
		
		long endMillis = System.currentTimeMillis(); long duration = endMillis - startMillis;
		logger.info("END of Run (SUCCESS) " + new Date(endMillis) + ".  " + candidates.size() + " new candidate hazards files were printed.  (" + duration + " milliseconds)");
		
		

  } // end main

} // end class definition
