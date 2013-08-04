package org.pdc.nws;

import java.net.*;
import java.io.*;



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
		int connectTimeoutinMilliseconds = 1000;
		int readTimeoutinMilliseconds = 1000;
		
		
		try
		{
          url = new URL("http://alerts.weather.gov/cap/us.php?x=0");
          URLConnection conn = url.openConnection();
          // setting timeouts 
          conn.setConnectTimeout(connectTimeoutinMilliseconds);
          conn.setReadTimeout(readTimeoutinMilliseconds);
          BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          
          while ((inputLine = in.readLine()) != null) {
        	  out(inputLine);
          }
          in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
    }	
	
}
