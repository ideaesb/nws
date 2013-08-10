package org.pdc.fetch;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.impl.BaseWireFeedParser;
import com.sun.syndication.feed.module.content.ContentModule;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Thin wrapper on SyndEntry - convenience methods for generating unique identifiers from contents of SyndEntry,
 * output contents of the <entry> as in the NWS Feed, expanding out the VTEC codes   
 * @author Uday
 *
 */
public class PdcEntry implements Serializable
{

  private Logger logger = LoggerFactory.getLogger(PdcEntry.class);

  SyndEntry syndEntry; 
  
  // these are the inner xml tags
  private String id ="";
  private String updated = "";
  private String published ="";
  private String author = "";
  private String title = "";
  private String link ="";
  private String summary = "";
  private String capEvent= "";
  private String capEffective="";
  private String capExpires="";
  private String capStatus="";
  private String capMsgType="";
  private String capCategory="";
  private String capUrgency="";
  private String capSeverity="";
  private String capCertainty="";
  private String capAreaDesc="";
  private String capPolygon="";
  
  private String[] capGeoCodeNames = {""};
  private String[] capGeoCodeValue = {""};
  
  private String pVtecProductClass = "";
  private String pVtecActions = "";
  private String pVtecOfficeId = "";
  private String pVtecPhenomena = "";
  private String pVtecSignificance = "";
  private String pVtecEventTrackingNumber="";
  private String pVtecBeginDate="";
  private String pVtecEndDate="";
  
  private String hVtecNwsLocationId="";
  private String hVtecFloodSeverity="";
  private String hVtecBeginDate="";
  private String hVtecFloodCrestDate="";
  private String hVtecEndDate="";
  private String hVtecImmediateCause="";
  private String hVtecFloodRecordStatus="";
  
  private boolean havePvtec = false;
  private boolean haveHvtec = false;
  
  //private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
  
	
  public PdcEntry(SyndEntry entry) 
  {
    syndEntry = entry; 	    
  }

  /**
   * Provides access to syndEntry (and all this methods)
   * @return the internal SyndEntry member variable 
   */
  public SyndEntry getSyndEntry()
  {
	  return syndEntry;
  }
	 
 /**
  * This will serve as key for the entries cache or hash - (key, value), where value is PdcEntry 
  * TODO may be modified to any string like hash of various parameters  
  * @return  the CAP ID concatenated dot published date in milliseconds since java epoch  
  */
  public String getKey()
  {
	String key = syndEntry.getLink() + "." + syndEntry.getPublishedDate().getTime();
	// try hashing it
	try
	{
		key = getMD5hash(key);
	}
	catch (Exception e)
	{
		
	}
	
	// will fire only if MD5 hash failed AND link is still http
	if (StringUtils.startsWithIgnoreCase(key,"http"))
	{
		key = StringUtils.substringAfterLast(key, "=");
	}
	
	return key;
  }
	 
  public String getFilename(String ext)
  {
	  return getKey() + "." + ext;
  }
  public String getFilename()
  {
	  return getFilename("txt");
  }
  
  
  public String getForeignMarkup()
  {
	  List fms = (List) syndEntry.getForeignMarkup();
	  
	  StringBuilder sb = new StringBuilder();
	  /*
	  XMLOutputter xo = new XMLOutputter();
	  sb.append(xo.outputString(fms));
	  */
	  
	  for (Object fm: fms)  
	  {
		  Element jdomElem = (Element) fm;
		  if (sb.length() > 0) sb.append(", "); 
		  sb.append(jdomElem.getName() + "=" + jdomElem.getValue());
		  
	  }
	  
	  return sb.toString();
  }
  public String toString()
  {
	  return syndEntry.toString();
  }

  /**
   * this will be the main output to file
   * @return
   */
  public String getProperties()
  {
	  copySyndEntry();
	  return syndEntry.toString();
  }
  
  public String getXML()
  {
	  return "";
  }

  /**
   * 
   * @param string
   * @return NoSuchAlgorithm Exception - 
   * @throws Exception
   */
  public String getMD5hash (String s) throws Exception 
  {
      MessageDigest m= MessageDigest.getInstance("MD5");
      m.update(s.getBytes(),0,s.length());
      return new BigInteger(1,m.digest()).toString(16);
  }

  
  private void copySyndEntry()
  {
	  
	  logger.info("Entering copySyndEntry " + syndEntry.getTitle());
	  
	  
	  this.id = StringUtils.substringAfterLast(syndEntry.getLink(), "=");
	  this.link = syndEntry.getUri();
	  
	  //StringBuffer sb = new StringBuffer(); dateTimeFormatter.printTo(sb, syndEntry.getUpdatedDate().getTime());
	  this.updated = syndEntry.getUpdatedDate().getTime() + "";
	  
	  //sb = new StringBuffer(); dateTimeFormatter.printTo(sb, syndEntry.getPublishedDate().getTime());
	  this.published = syndEntry.getPublishedDate().getTime() + "";
	  
	  this.author = syndEntry.getAuthor();
	  
	  this.title = syndEntry.getTitle();
	  
	  this.summary = syndEntry.getDescription().getValue();

	  // just brute-force the jdom elements being apparently returned as foreign markup
	  List fms = (List) syndEntry.getForeignMarkup();
	  logger.info("Entering foreign markup for loop.  Number of markups to be processed  " + fms.size());

	  for (Object fm: fms)  
	  {
		  Element jdomElem = (Element) fm;
		  
		  if (StringUtils.containsIgnoreCase(jdomElem.getName(), "event" ))
		  {
			  capEvent=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "effective" ))
		  {
			  capEffective=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "expires" ))
		  {
			  capExpires=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "status" ))
		  {
			  capStatus=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "msgType" ))
		  {
			  capMsgType=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "category" ))
		  {
			  capCategory=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "urgency" ))
		  {
			  capUrgency=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "severity" ))
		  {
			  capSeverity=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "certainty" ))
		  {
			  capCertainty=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "areaDesc" ))
		  {
			  capAreaDesc=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "polygon" ))
		  {
			  capPolygon=jdomElem.getValue();
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "geocode" ))
		  {
	  
			  List gcodes = (List) jdomElem.getChildren();
			  logger.info("Entering Geo Code.  Number of geocode children (names and values)  " + gcodes.size());
			  
			  List<String> gnames= new ArrayList <String>();
			  List<String> gvalues= new ArrayList <String>();
			  
			  for (Object gcode: gcodes) 
			  {
				  Element glement = (Element) gcode; 
				  if (StringUtils.containsIgnoreCase(glement.getName(), "name" )) gnames.add(glement.getValue());
				  else gvalues.add(glement.getValue());
			  }
			  
			  
			  capGeoCodeNames = new String[gnames.size()];
			  capGeoCodeValue = new String [gvalues.size()];
			  
			  for (int i=0; i < gnames.size(); i++)
			  {
				  capGeoCodeNames[i] = gnames.get(i);
			  }
			  for (int i=0; i < gvalues.size(); i++)
			  {
				  capGeoCodeValue[i] = gvalues.get(i);
			  }
			  
			  for (int i=0; i < gnames.size(); i++)
			  {
				  logger.info(capGeoCodeNames[i] + ".value(s) = " + capGeoCodeValue[i] ); 	  
			  }
			  
			  
			  /*
			  capGeoCodeNames = capGeoCodeValue = new String[gcodes.size()/2];
			  
			  for (int i=0; i < gcodes.size(); i=i+2)
			  {
				  Element glement1 = (Element) gcodes.get(i);
				  capGeoCodeNames[i]= glement1.getValue();
				  
				  Element glement2 = (Element) gcodes.get(i+1);
				  capGeoCodeValue [i] = glement2.getValue();
			  }
			  */
			  
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "parameter" ))
		  {
			  List children = jdomElem.getChildren();
			  Element vtecValue = (Element) children.get(1);
			  
			  // just log if-else 
			  if (StringUtils.isNotBlank(vtecValue.getValue()))
			  {
			    logger.info("Parsing VTEC " + vtecValue.getValue() + ", number of tokens when split by forward slash = " + StringUtils.split(vtecValue.getValue(), "/").length);
			  }
			  else
			  {
				  logger.info("VTEC Omitted...Skipping");
			  }
			  
			  if (StringUtils.isNotBlank(vtecValue.getValue()))
			  {
				  String [] vtecArr = StringUtils.split(vtecValue.getValue(), "/");
				  
				  
				  // count non-empty
				  List<String> nonEmpty= new ArrayList <String>();
				  for (int i = 0; i < vtecArr.length; i++)
				  {
					  if (StringUtils.isNotBlank(vtecArr[i])) nonEmpty.add(vtecArr[i]);
				  }
				  
				  if (nonEmpty.size() > 0)
				  {
					  // P-VTEC
					  logger.info("P-VTEC...parse BEGIN");
					  
					  String [] pelements = StringUtils.split(nonEmpty.get(0), ".");
					  
					  pVtecProductClass = getVtec("vtec.product_class.properties",pelements[0]); logger.info("pVtec.Product.Class = " + pVtecProductClass);
					  pVtecActions      = getVtec("vtec.actions.properties",pelements[1]); logger.info("pVtec.Actions = " + pVtecActions);
					  pVtecOfficeId     = pelements[2]; logger.info("pVtec.Office ID = " + pVtecOfficeId);
					  pVtecPhenomena    = getVtec("vtec.phenomena.properties",pelements[3]); logger.info("pVtec.Phenomena = " + pVtecPhenomena);
					  pVtecSignificance = getVtec("vtec.significance.properties",pelements[4]); logger.info("pVtec.Significance= " + pVtecSignificance);
					  pVtecEventTrackingNumber = pelements[5]; logger.info("pVtec.Event Tracking # = " + pVtecEventTrackingNumber);
					  
					  String [] dateElements = StringUtils.split(pelements[6], "-");
					  
					  pVtecBeginDate    = dateElements[0]; logger.info("pVTEC Begin Date = " + pVtecBeginDate);
					  pVtecEndDate      = dateElements[1]; logger.info("pVTEC End  Date = " + pVtecEndDate);
					  
					  havePvtec = true;
				      if (nonEmpty.size() > 1)
				      {
						  // H-VTEC
	
				    	  logger.info("Hydro-VTEC...parse BEGIN");
						  String [] helements = StringUtils.split(nonEmpty.get(1), ".");
	
						  
						  hVtecNwsLocationId  =  helements[0]; logger.info("Location ID " + hVtecNwsLocationId);
						  hVtecFloodSeverity  =  getVtec("vtec.flood_severity.properties",helements[1]); logger.info("Flood Severity " + hVtecFloodSeverity);
						  hVtecImmediateCause =  getVtec("vtec.immediate_cause.properties",helements[2]); logger.info("Immediate Cause " + hVtecImmediateCause);
						  
						  hVtecBeginDate      = helements[3];  logger.info("Begin Date " + hVtecBeginDate);
						  hVtecFloodCrestDate = helements[4];  logger.info("Crest Date " + hVtecFloodCrestDate);
						  hVtecEndDate        = helements[5];  logger.info("End Date " + hVtecEndDate);
						  hVtecFloodRecordStatus = getVtec("vtec.flood_record_status.properties",helements[6]); logger.info("Flood Record Status " + hVtecFloodRecordStatus);
						  haveHvtec = true;
				     }
				  }
				  
			  }
		  }
		  
	  }
  }
		 

  public void write(java.io.Writer writer) throws java.io.IOException
  {
	  copySyndEntry();
	  // this is where the PdcEntry itself writes to file
	  writer.write("title="+this.title);
	  
	  writer.write("\n#  Last Updated - milliseconds since Epoch January 1, 1970 00.00.00 UNIVERSAL Time (UTC/Zulu)");
	  writer.write("\nupdatedMillis="+this.updated);
	  writer.write("\nupdatedDate="+this.syndEntry.getUpdatedDate());

	  writer.write("\n#  Last Published - milliseconds since Epoch January 1, 1970 00.00.00 UNIVERSAL Time (UTC/Zulu)");
	  writer.write("\npublishedMillis="+this.published);
	  writer.write("\npublishedDate="+this.syndEntry.getPublishedDate());
	  
	  writer.write("\n#  This is the Embdedded CAP Feed Identifier extracted from the URL after the equal sign"); 
	  writer.write("\nid="+this.id);
	  writer.write("\nsummary="+this.summary);
	  
	  writer.write("\nURL="+this.link);
	  writer.write("\nauthor="+this.author);
	  
	  writer.write("\n#  Foreign Markup (CAP info embedded in this Atom \"Index\") ------------------ ");
	  writer.write("\ncap.event="+this.capEvent);
	  writer.write("\ncap.effectiveDate="+this.capEffective);
	  writer.write("\ncap.expireDate="+this.capExpires);
	  writer.write("\ncap.status="+this.capStatus);
	  writer.write("\ncap.messageType="+this.capMsgType);
	  writer.write("\ncap.category="+this.capCategory);
	  writer.write("\ncap.urgency="+this.capUrgency);
	  writer.write("\ncap.severity="+this.capSeverity);
	  writer.write("\ncap.certainty="+this.capCertainty);
	  writer.write("\ncap.areaDescripton="+this.capAreaDesc);
	  writer.write("\ncap.polygon="+this.capPolygon);

	  writer.write("\n#  Geocodes, if available, omitted otherwise  ------------------ ");
	  
	  for (int i=0; i < capGeoCodeNames.length; i++)
	  {
		  writer.write("\ncap.geocode."+ capGeoCodeNames[i] + "=" + capGeoCodeValue[i]);
	  }

	  writer.write("\n#  P VTEC if available, omitted otherwise  ------------------  ");

	  if (havePvtec)
	  {
		  writer.write("\nP-VTEC.ProductClass= "+this.pVtecProductClass);
		  writer.write("\nP-VTEC.Actions="+this.pVtecActions);
		  writer.write("\nP-VTEC.OfficeId="+this.pVtecOfficeId);
		  writer.write("\nP-VTEC.Phenomena="+this.pVtecPhenomena);
		  writer.write("\nP-VTEC.Significance="+this.pVtecSignificance);
		  writer.write("\nP-VTEC.EventTrackingNumber="+this.pVtecEventTrackingNumber);
		  writer.write("\nP-VTEC.BeginDate="+this.pVtecBeginDate);
		  writer.write("\nP-VTEC.EndDate="+this.pVtecEndDate);
	  }
	  
	  writer.write("\n#  Hydrological (H) VTEC if available ------------------ ");

	  if (haveHvtec)
	  {
		  writer.write("\nH-VTEC.NwsLocationId="+this.hVtecNwsLocationId);
		  writer.write("\nH-VTEC.FloodSeverity="+this.hVtecFloodSeverity);
		  writer.write("\nH-VTEC.ImmediateCause="+this.hVtecImmediateCause);
		  writer.write("\nH-VTEC.BeginDate="+this.hVtecBeginDate);
		  writer.write("\nH-VTEC.FloodCrestDate="+this.hVtecFloodCrestDate);
		  writer.write("\nH-VTEC.EndDate="+this.hVtecEndDate);
		  writer.write("\nH-VTEC.FloodRecordStatus="+this.hVtecFloodRecordStatus);
	  }
	  
  }
		  
	 
	  
	  
	  private String getVtec(String propertiesFile, String key)
	  {
		  PropertiesConfiguration config = null;
		  try
		  {
			   config = new PropertiesConfiguration(propertiesFile); 
		  }
		  catch (org.apache.commons.configuration.ConfigurationException e)
		  {
			  
		  }
		  
		  
		  if (config == null)  return key;
		  
		  
		  return config.getString(key, key);
		  
		  
	  }

	  
	  
}
  

