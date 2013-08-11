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
  
  private boolean haveGeoCodes = false;
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

			  // per jdom, an empty list is returned if no kids
			  // capGeoCodeNames, capGeoCodeValue will remain empty 
			  if (gcodes.isEmpty()) continue;
			  else haveGeoCodes = true;
			  
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
			  
			  
		  }
		  else if (StringUtils.containsIgnoreCase(jdomElem.getName(), "parameter" ))
		  {
			  List children = jdomElem.getChildren();
			  // gotta have exactly two kids 
			  if (children.isEmpty() || children.size() != 2)
			  {
				  logger.info("The paramter elments was found empty or NOT have exactly two children - ignoring");
				  continue;
			  }
			  
			  // get the second element within <value> tags 
			  Element vtecValueElement = (Element) children.get(1);
			  
			  
			  if (sanityCheckVtec(vtecValueElement.getValue()))
			  {
				  logger.info("Parsing VTEC " + vtecValueElement.getValue() + ", number of tokens when split by forward slash = " + StringUtils.split(vtecValueElement.getValue(), "/").length);

				  String [] vtecArr = StringUtils.split(vtecValueElement.getValue(), "/");
				  
				  
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
			  else
			  {
				  logger.info("VTEC codes are empty or possibly corrupted " + vtecValueElement.getValue() + ".... skipped parse ");
			  }

		  }
		  
	  }
  }
		 

  public void write(java.io.Writer w) throws java.io.IOException
  {
	  // why here and not constructor ??? because no need for operation except if PdCEntry is new. 
	  copySyndEntry();
	  // this is where the PdcEntry itself writes to file
	  w.write("title="+this.syndEntry.getTitle());
	  
	  writeln(w,"#  Last Updated - milliseconds since Epoch January 1, 1970 00.00.00 UNIVERSAL Time (UTC/Zulu)");
	  writeln(w,"updatedMillis="+this.updated);
	  writeln(w,"updatedDate="+this.syndEntry.getUpdatedDate());

	  writeln(w,"#  Last Published - milliseconds since Epoch January 1, 1970 00.00.00 UNIVERSAL Time (UTC/Zulu)");
	  writeln(w,"publishedMillis="+this.published);
	  writeln(w,"publishedDate="+this.syndEntry.getPublishedDate());
	  
	  writeln(w,"#  This is the Embdedded CAP Feed Identifier extracted from the URL after the equal sign"); 
	  writeln(w,"id="+this.id);
	  writeln(w,"summary="+this.summary);
	  
	  writeln(w,"URL="+this.link);
	  writeln(w,"author="+this.author);
	  
	  writeln(w,"#  Foreign Markup (CAP info embedded in this Atom \"Index\") ------------------ ");
	  writeln(w,"cap.event="+this.capEvent);
	  writeln(w,"cap.effectiveDate="+this.capEffective);
	  writeln(w,"cap.expireDate="+this.capExpires);
	  writeln(w,"cap.status="+this.capStatus);
	  writeln(w,"cap.messageType="+this.capMsgType);
	  writeln(w,"cap.category="+this.capCategory);
	  writeln(w,"cap.urgency="+this.capUrgency);
	  writeln(w,"cap.severity="+this.capSeverity);
	  writeln(w,"cap.certainty="+this.capCertainty);
	  writeln(w,"cap.areaDescripton="+this.capAreaDesc);
	  writeln(w,"cap.polygon="+this.capPolygon);

	  writeln(w,"#  Geocodes, if available, omitted otherwise  ------------------ ");
	  
	  if (haveGeoCodes) 
	  {
		  writeln(w,"#  COMMA separated in key-value(s) format;  each key may have multiple values, which are separated by whitespace as-is from NWS feed------- ");

		  for (int i=0; i < capGeoCodeNames.length; i++)
     	  {
		    writeln(w,"cap.geocode="+ capGeoCodeNames[i] + "," + capGeoCodeValue[i]);
	      }
	  }

	  writeln(w,"# VTEC if available, omitted otherwise  ------------------  ");

	  if (havePvtec)
	  {
		  writeln(w,"vtec.ProductClass= "+this.pVtecProductClass);
		  writeln(w,"vtec.Actions="+this.pVtecActions);
		  writeln(w,"vtec.OfficeId="+this.pVtecOfficeId);
		  writeln(w,"vtec.Phenomena="+this.pVtecPhenomena);
		  writeln(w,"vtec.Significance="+this.pVtecSignificance);
		  writeln(w,"vtec.EventTrackingNumber="+this.pVtecEventTrackingNumber);
		  writeln(w,"vtec.BeginDate="+this.pVtecBeginDate);
		  writeln(w,"vtec.EndDate="+this.pVtecEndDate);

		  writeln(w,"#  Hydrological (H) VTEC if available ------------------ ");
	
		  if (haveHvtec)
		  {
			  writeln(w,"vtec.hydro.NwsLocationId="+this.hVtecNwsLocationId);
			  writeln(w,"vtec.hydro.FloodSeverity="+this.hVtecFloodSeverity);
			  writeln(w,"vtec.hydro.ImmediateCause="+this.hVtecImmediateCause);
			  writeln(w,"vtec.hydro.BeginDate="+this.hVtecBeginDate);
			  writeln(w,"vtec.hydro.FloodCrestDate="+this.hVtecFloodCrestDate);
			  writeln(w,"vtec.hydro.EndDate="+this.hVtecEndDate);
			  writeln(w,"vtec.hydro.FloodRecordStatus="+this.hVtecFloodRecordStatus);
		  }
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
	  
	  private void writeln(java.io.Writer writer, String str) throws java.io.IOException
	  {
		  writer.write(System.getProperty("line.separator"));
		  writer.write(StringUtils.trimToEmpty(str));   
	  }

	  
	  
	  private boolean sanityCheckVtec(String str)
	  {
		  boolean pass = true;
		  
		  // empty 
		  if (StringUtils.isBlank(str))
		  {
			  logger.info("VTEC value Empty - nothing to parse");
			  return false;
		  }
		  
		  // must have slash or dash delimiters 
		  if (StringUtils.contains(str, "/") && StringUtils.contains(str, "-"))
		  {
			  //ok
		  }
		  else
		  {
			  logger.info("VTEC value may be corrupt - no expected delimiters - forward slashes or dashes.");
			  return false;
		  }
		  
		  // split by slash should result in exactly two tokens
		  
		  // count non-empty
		  String [] vtecArr = StringUtils.split(str, "/");
		  List<String> nonEmpty= new ArrayList <String>();
		  
		  for (int i = 0; i < vtecArr.length; i++)
		  {
			  if (StringUtils.isNotBlank(vtecArr[i])) nonEmpty.add(vtecArr[i]);
		  }
		  
		  if (nonEmpty.size() == 1 || nonEmpty.size() == 2)
		  {
			  // ok
		  }
		  else
		  {
			  logger.info("VTEC does not split by forward slash delimiter into one or two non-empty fragments - will not parse");
			  return false; 
		  }
		  
		  String [] pelements = StringUtils.split(nonEmpty.get(0), ".");
		  if (pelements.length != 7)
		  {
              logger.info("P-VTEC does not have exactly 7 tokens when split by dot");			  
			  return false;
		  }
		  
		  String [] dateElements = StringUtils.split(pelements[6], "-");
		  if (dateElements.length != 2)
		  {
              logger.info("P-VTEC date elements does not have exactly 2 tokens when split by dash");			  
			  return false;
		  }
		  
		  if (nonEmpty.size() == 2)
		  {
		    String [] helements = StringUtils.split(nonEmpty.get(1), ".");
		    if (helements.length != 7)
		    {
              logger.info("Hydro H-VTEC does not have exactly 7 tokens when split by dot");			  
			  return false;
		    }
		  }
		  
		  // more sophsticated hurdles can be put here to ensure dates
		  
		  
		  return pass;
	  }
	  
}
  

