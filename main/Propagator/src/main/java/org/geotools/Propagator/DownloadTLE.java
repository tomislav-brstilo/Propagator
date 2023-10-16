package org.geotools.Propagator;
import java.io.*;
import java.net.*;

public class DownloadTLE {

	public static void TLEdownloader() {
		
		try {
			URL url = new URL("https://celestrak.com/NORAD/elements/gp.php?GROUP=active&FORMAT=tle");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
	
			PrintWriter TLEdata = null;
			try {
				TLEdata = new PrintWriter("Latest TLE data.txt", "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				TLEdata.print(inputLine.trim());
				TLEdata.print('\n');
			}
			br.close();
			TLEdata.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}