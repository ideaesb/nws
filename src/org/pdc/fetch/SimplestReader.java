package org.pdc.fetch;

import java.io.*;
import java.util.*;

import com.sun.syndication.io.SyndFeedInput;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplestReader {
	
    
	
	public static void main(String[] args) 
	{
	
		Logger logger = LoggerFactory.getLogger(SimplestReader.class);
		
		String feedURL = "http://alerts.weather.gov/cap/us.php?x=0";
		
		String candidateHazardsDirectory = "candidateHazards";
		File candidateHazards = new File(System.getProperty("user.dir"), candidateHazardsDirectory);
        if (!candidateHazards.exists()) candidateHazards.mkdirs();
        if (candidateHazards.exists() && candidateHazards.isDirectory())
        {
        	logger.info("candidate hazards directory exists " + candidateHazards.getAbsolutePath());
        }
        
		// Now try parsing the feed URL 
		SyndFeed feed = null;
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
        
        // to be used to hold the new/updated feed entries for output into candidate hazards directory  
        List<PdcEntry> candidates = new ArrayList<PdcEntry>();

        // Fire up the cache 
        DiskCache cache = new DiskCache();
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
		logger.info("cache should have increased by added " + candidates.size());
	          
		
		for (PdcEntry candidate : candidates)
		{
		  
	      // 		
		  File candidateFile = new File(candidateHazards, candidate.getFilename()); 
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
  		      FileWriter fwr = new FileWriter(candidateFile, false);  // append = false 
	          candidate.write(fwr);
	          fwr.close();
		    }
		    catch (Exception e)
		    {
			  logger.error("Unable to write (into) file " + candidateFile );
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
		
		logger.info(candidates.size() + " new candidate hazards files were printed");

  }

}
