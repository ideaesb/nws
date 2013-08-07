/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdc.fetch;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import java.io.File;
import java.net.URL;

public class FeedFetcherTest implements FetcherListener {      //|#1
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {                                 //|#2
            System.out.println(
                "USAGE: FeedFetcherTest [disk|memory] <feed-url>");
            return;
        }
        new FeedFetcherTest(args[0], args[1]);                 //|#3
    }
    public FeedFetcherTest(String type, String url) throws Exception {
        FeedFetcherCache feedInfoCache = null;
        if ("disk".equals(type)) {                             //|#4
            File cache = new File("./cache");
            if (!cache.exists()) cache.mkdirs();
            feedInfoCache = new DiskFeedInfoCache(cache.getAbsolutePath());
        } else { 
            feedInfoCache = new HashMapFeedInfoCache();        //|#5
        }       
        FeedFetcher feedFetcher =                              //|#6
           new HttpURLFeedFetcher(feedInfoCache);
        feedFetcher.addFetcherEventListener(this);             //|#7
        SyndFeed feed = 
           feedFetcher.retrieveFeed(new URL(url));             //|#8
    }
    public void fetcherEvent(FetcherEvent ev) {                //|#9
        System.out.println("FetcherEvent received");
        System.out.println("  eventType: " + ev.getEventType());
        System.out.println("  urlString: " + ev.getUrlString());
    }
}

