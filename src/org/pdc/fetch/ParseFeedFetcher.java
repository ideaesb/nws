package org.pdc.fetch;       //|#1
import com.sun.syndication.feed.synd.*;       //|#2;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import java.io.*;
import java.net.URL;
import java.util.*;


/**
 * Version of ParseFeed that uses a disk-based cache via the ROME Fetcher.
 */
public class ParseFeedFetcher {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("USAGE: ParseFeed <feed-url>");
            return;
        }
        new ParseFeedFetcher().parseFeed(args[0]);
    }
    public void parseFeed(String feedURL) throws Exception {
        
        File cache = new File("./cache");
        if (!cache.exists()) cache.mkdirs();
        FeedFetcherCache feedInfoCache = 
            new DiskFeedInfoCache(cache.getAbsolutePath());
        FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
        SyndFeed feed = feedFetcher.retrieveFeed(new URL(feedURL));
                
        Iterator entries = feed.getEntries().iterator();
        while (entries.hasNext()) {  
            SyndEntry entry = (SyndEntry)entries.next();
            
            //System.out.println("Uri: " + entry.getUri());     
            //System.out.println("  Link:      " + entry.getLink()); 
            System.out.println("  Title:     " + entry.getTitle());
            //System.out.println("  Published: " + entry.getPublishedDate());
            //System.out.println("  Updated:   " + entry.getUpdatedDate());
            /*
            if (entry.getDescription() != null) {
                System.out.println("  Description: "   
                        + entry.getDescription().getValue());
            }
            if (entry.getContents().size() > 0) {   
                SyndContent content = (SyndContent)entry.getContents().get(0);
                System.out.print("  Content type=" + content.getType());
                System.out.println(" value=" + content.getValue());
            }
            for (int i=0; i < entry.getLinks().size(); i++) { 
                SyndLink link = (SyndLink)entry.getLinks().get(i);
                System.out.println(
                        "  Link type=" + link.getType() +
                        " length="   + link.getLength() +
                        " hreflang=" + link.getHreflang() +
                        " href="     + link.getHref());
            }
            System.out.println("\n");
            */
        }        
    }
}
