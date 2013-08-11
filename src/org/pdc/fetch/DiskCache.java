package org.pdc.fetch;


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class DiskCache 
{

	  private static final CacheManager  cacheManager  = new CacheManager();
	  
	  int m_tti = 60*60*24*7;
	  int m_ttl = 60*60*24*30;
	  
	  public DiskCache(int tti, int ttl) 
	  {
	    m_tti = tti; m_ttl = ttl;  
	  }

	  public PdcEntry get(String key) 
	  {
	    Element elem = getCache().get(key);
	    return (PdcEntry) elem.getObjectValue();
	  }
	  
	  public boolean put(PdcEntry entry)
	  {
		  if (getCache().isKeyInCache(entry.getKey()))
		  {
			  return false;
		  }
		  else
		  {
			  Element elem = new Element(entry.getKey(), entry, m_tti, m_ttl);
			  getCache().put(elem);
			  return true;
		  }
	  }


	  public long getTTL() {
	    return getCache().getCacheConfiguration().getTimeToLiveSeconds();
	  }

	  public long getTTI() {
	    return getCache().getCacheConfiguration().getTimeToIdleSeconds();
	  }

	  public int getSize() {
	    return getCache().getSize();
	  }

	  private Ehcache getCache() {
	    return cacheManager.getEhcache("nwscapatom");
	  }

}
