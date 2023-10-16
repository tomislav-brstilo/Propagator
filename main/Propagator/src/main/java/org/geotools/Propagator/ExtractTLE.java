package org.geotools.Propagator;
import java.io.*;

public class ExtractTLE {
	
	private static boolean newInformation = false;
	
	private static PrintWriter newTLE = null;
	
	private static String firstLineUntrimmed = "";
	private static String secondLineUntrimmed = "";
	
	private static int epochYear = 0;
	private static double epochDay = 0;

	public static void TLEextractor(String satelliteName) {
		
		String line = "";
		
		try (BufferedReader br = new BufferedReader(new FileReader(".\\Latest TLE data.txt"))) {
			while((line = br.readLine())!=null) {
				if(line.equals(satelliteName)) {
					firstLineUntrimmed = br.readLine();
					secondLineUntrimmed = br.readLine();
					break;
				}
			}
		}
		catch(Exception e) { 
			e.printStackTrace();  
		}
		
		String firstLine = firstLineUntrimmed.trim().replaceAll(" +", " ");
		
		String[] firstLineArray = firstLine.split(" ");
		
		if(Integer.parseInt(firstLineArray[3].substring(0, 2))==epochYear && Double.parseDouble(firstLineArray[3].substring(2, 14))==epochDay) {
			newInformation = false;
			System.out.println("Still no new TLE information!\n");
		} else {
			newInformation = true;
			System.out.println(satelliteName + ":");
			System.out.println(firstLineUntrimmed);
			System.out.println(secondLineUntrimmed + "\n");
		}
		
		epochYear = Integer.parseInt(firstLineArray[3].substring(0, 2));
		epochDay = Double.parseDouble(firstLineArray[3].substring(2, 14));
			
	}
	
	public static PrintWriter getPrintWriter() {
		return newTLE;
	}
	
	public static boolean getCondition() {
		return newInformation;
	}
	
	public static String getFirstLine() {
		return firstLineUntrimmed;
	}
	
	public static String getSecondLine() {
		return secondLineUntrimmed;
	}
}