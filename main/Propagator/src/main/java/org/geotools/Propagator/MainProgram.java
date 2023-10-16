package org.geotools.Propagator;
import java.awt.Color;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.api.style.Graphic;
import org.geotools.api.style.Mark;
import org.geotools.api.style.PointSymbolizer;
import org.geotools.api.style.Rule;
import org.geotools.api.style.Style;
import org.geotools.api.style.TextSymbolizer;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapFrame;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class MainProgram {
	
	public static final double EarthRadius = 6378165;
	public static final double Earth_a = 6378137;
	public static final double Earth_b = 6356752.3142;
	public static final double gravitationalConstant = 3.986013502E14;
	
	public static final String satelliteName = "SWISSCUBE";
	
	public static void main(String[] args) {
		
			DownloadTLE.TLEdownloader();
			ExtractTLE.TLEextractor(satelliteName);
			
			Boolean newInformation = ExtractTLE.getCondition();
			
			if(newInformation) {
				
				
				/* Acquire the latest TLE set */
				
				String firstLineUntrimmed = ExtractTLE.getFirstLine();
				String secondLineUntrimmed = ExtractTLE.getSecondLine();
				
				String firstLine = firstLineUntrimmed.trim().replaceAll(" +", " ");
				String secondLine = secondLineUntrimmed.trim().replaceAll(" +", " ");

				String[] firstLineArray = firstLine.split(" ");
				String[] secondLineArray = secondLine.split(" ");
				
				
				/* Extract orbital elements from the TLE set */
				
				int epochYear = Integer.parseInt(firstLineArray[3].substring(0, 2));
				double epochDay = Double.parseDouble(firstLineArray[3].substring(2, 14));
				/* double bStar = Double.parseDouble(firstLineArray[6].substring(0, 5))/1E5
								/Math.pow(10, (Double.parseDouble(firstLineArray[6].substring(6, 7)))); */
				double inclination = Double.parseDouble(secondLineArray[2]);
				double rightAscensionOfTheAscendingNode = Double.parseDouble(secondLineArray[3]);
				double eccentricity = Double.parseDouble(secondLineArray[4])/1E7;
				double argumentOfPerigee = Double.parseDouble(secondLineArray[5]);
				double initialMeanAnomaly = Double.parseDouble(secondLineArray[6]);
				double meanMotion = Double.parseDouble(secondLineArray[7].substring(0, 11));
				
				
				/* Get the epoch date, hours and minutes */
				
				if(epochYear<24)
					epochYear += Integer.parseInt("2000");
				else
					epochYear += Integer.parseInt("1900");
				int dayOfYear = (int)epochDay;
				
				int hour = (int)Math.floor((epochDay - dayOfYear)*24);
				double minute = (epochDay - dayOfYear)*24*60-(hour*60);
				int second = (int)Math.round((minute - Math.floor(minute))*60);
				
				LocalDateTime realDateTime = LocalDateTime.now();
				LocalDateTime epochDateTime = LocalDateTime.now().withYear(epochYear).withDayOfYear(dayOfYear).withHour(hour).withMinute((int)Math.floor(minute)).withSecond(second);

				String epochDateTimeString = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss").format(epochDateTime);
								
				double epochDayWithFraction = epochDateTime.getDayOfMonth() + (double)epochDateTime.getHour()/24 + (double)epochDateTime.getMinute()/1440 + (double)epochDateTime.getSecond()/86400;
				
				double jDayEpoch = JulianDayCalculator.CalculateTime(epochDayWithFraction, epochDateTime.getMonthValue(), epochDateTime.getYear());
							
				
				/* Convert degrees to radians */
				
				inclination = Math.toRadians(inclination);
				rightAscensionOfTheAscendingNode = Math.toRadians(rightAscensionOfTheAscendingNode);
				argumentOfPerigee = Math.toRadians(argumentOfPerigee);
				initialMeanAnomaly = Math.toRadians(initialMeanAnomaly);
				
				
				/* Calculate semi-major axis, semi-minor axis and satellite period */
				
				double semiMajorAxis = (Math.pow(gravitationalConstant/Math.pow((meanMotion*2*Math.PI)/86400, 2), 0.33333));
				double semiMinorAxis = semiMajorAxis*Math.sqrt(1-Math.pow(eccentricity, 2));
				
				double satellitePeriod = 24/meanMotion*60;
				
				
				/* Write satellite orbital elements to file */
				
				PrintWriter latestTLE;
				try {
					latestTLE = new PrintWriter(satelliteName + " " + epochDateTimeString + ".txt", "UTF-8");
					latestTLE.print(satelliteName + ":\n" +
									firstLineUntrimmed + "\n" +
									secondLineUntrimmed + "\n\n" +
									"Date: " + epochDateTimeString + "\n" +
									"Inclination: "+ String.format("%.5f", Math.toDegrees(inclination)) + "\n" +
									"Right ascension of the ascending node: " + String.format("%.5f", Math.toDegrees(rightAscensionOfTheAscendingNode)) + "\n" +
									"Eccentricity: " + eccentricity + "\n" +
									"Argument of perigee: " + Math.toDegrees(argumentOfPerigee) + "\n" +
									"Initial mean anomaly: " + Math.toDegrees(initialMeanAnomaly) + "\n" +
									"Mean motion: " + String.format("%.5f", meanMotion) + " rev/day\n\n" +
									"Semi-major axis: " + String.format("%.5f", semiMajorAxis) + " m\n" +
									"Semi-minor axis: " + String.format("%.5f", semiMinorAxis) + " m\n" +
									"Period of the satellite: " + String.format("%.5f", satellitePeriod) + " min");
					latestTLE.close();
				} catch (FileNotFoundException e3) {
					e3.printStackTrace();
				} catch (UnsupportedEncodingException e3) {
					e3.printStackTrace();
				}
				
				LocalDateTime currentDateTime = epochDateTime;
				
				
				/* Initialize files */
				
				PrintWriter Anomaly = null;
				PrintWriter Position = null;
				PrintWriter ECIcoordinates = null;
				PrintWriter ECEFcoordinates = null;
				PrintWriter LatitudeLongitudeAltitude = null;
				PrintWriter ArgumentsToDisplay = null;
				try {
					Anomaly = new PrintWriter("Anomaly.txt", "UTF-8");
					Position = new PrintWriter("Position.txt", "UTF-8");
					ECIcoordinates = new PrintWriter("ECI coordinates.txt", "UTF-8");
					ECEFcoordinates = new PrintWriter("ECEF coordinates.txt", "UTF-8");
					LatitudeLongitudeAltitude = new PrintWriter("Latitude, longitude and altitude.txt", "UTF-8");
					ArgumentsToDisplay = new PrintWriter("Arguments to display.txt", "UTF-8");
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				
				/* Calculate anomalies, coordinates */
				
				for(double counter=0; counter<10000; counter++) {
					
					String currentDateTimeString = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss").format(currentDateTime);
					
					double dayWithFraction = currentDateTime.getDayOfMonth() + (double)currentDateTime.getHour()/24 + (double)currentDateTime.getMinute()/1440 + (double)currentDateTime.getSecond()/86400;
					double jDayNow = JulianDayCalculator.CalculateTime(dayWithFraction, currentDateTime.getMonthValue(), currentDateTime.getYear());
					
					double timeInCenturies = (jDayNow - 2451545.0) / 36525;
					
					double GMST = -62E-7 * Math.pow(timeInCenturies, 3) + 0.093104 * Math.pow(timeInCenturies, 2) + 3164400184.812866 * timeInCenturies + 67310.54841;
					GMST = GMST * (Math.PI / 180) / 240 % (2 * Math.PI);
					if(GMST < 0) GMST += 2 * Math.PI;
										
					double timeDifference = (jDayNow - jDayEpoch)*1440;
					
					
					/* Calculate mean, eccentric and true anomaly */
					
					double meanAnomaly = initialMeanAnomaly + (meanMotion*(Math.PI/720) * timeDifference);
					while(meanAnomaly>(2*Math.PI)) meanAnomaly -= 2*Math.PI;
					
					double eccentricAnomaly = getEccentricAnomaly(eccentricity, meanAnomaly);
					
					double trueAnomaly = 2*Math.atan(Math.sqrt((1+eccentricity)/(1-eccentricity))*Math.tan(eccentricAnomaly/2));
					if(trueAnomaly<0) trueAnomaly += Math.PI*2;
					
					Anomaly.print(currentDateTimeString + "   " + Math.toDegrees(meanAnomaly) + "   " + Math.toDegrees(eccentricAnomaly) + "   " + Math.toDegrees(trueAnomaly) + "\n");
					
				
					/* Calculate satellite's distance from Earth and position in orbit */
					
					double satelliteDistance = semiMajorAxis*(1-Math.pow(eccentricity, 2))/(1+eccentricity*Math.cos(trueAnomaly));
					
					double[] satellitePosition = {
							satelliteDistance*Math.cos(trueAnomaly),
							satelliteDistance*Math.sin(trueAnomaly)
					};
					
					Position.print(currentDateTimeString + "   " + satellitePosition[0] + "   " + satellitePosition[1] + "\n");
	
					
					/* Transform from Kepler's coordinate system to ECI coordinate system */
					
					double[] satelliteECI = CoordinateTransforms.positionToECI(satellitePosition, rightAscensionOfTheAscendingNode, argumentOfPerigee, inclination);  // Vra�a koordinate u metrima
					
					ECIcoordinates.print(currentDateTimeString + "   "	+ satelliteECI[0]/1000 + "   " 	+ satelliteECI[1]/1000 + "   " + satelliteECI[2]/1000 + "\n");
					
					
					/* Transform from ECI coordinate system to ECEF coordinate system */
					
					double[] satelliteECEF = CoordinateTransforms.ECItoECEF(satelliteECI, GMST);  // Vra�a koordinate u metrima
					
					ECEFcoordinates.print(currentDateTimeString + "   " + satelliteECEF[0]/1000 + "   " + satelliteECEF[1]/1000 + "   " + satelliteECEF[2]/1000 + "\n");
					
					
					/* Get latitude, longitude and altitude from ECEF coordinates */
					
					double[] latitudeLongitudeAltitude1 = CoordinateTransforms.ECEFtoLLA(satelliteECEF);
					
					double  latitude = Math.toDegrees(latitudeLongitudeAltitude1[0]),
				    		longitude = Math.toDegrees(latitudeLongitudeAltitude1[1]),
				    		altitude = latitudeLongitudeAltitude1[2];
					
					LatitudeLongitudeAltitude.print(currentDateTimeString + "   " + latitude + "   " + longitude + "   " + altitude + "\n");
							
					if(currentDateTime.compareTo(realDateTime.minusHours(2)) == 1) {
						String correctedDateTimeString = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss").format(currentDateTime.plusHours(2));
						ArgumentsToDisplay.print(correctedDateTimeString + "," + latitude + "," + longitude + "\n");
					}
				    
					/* Calculate the same values for 1 minute in the future */
					
					currentDateTime = currentDateTime.plusSeconds(60);
		
				}
				
				Anomaly.close();
				Position.close();
				ECIcoordinates.close();
				ECEFcoordinates.close();
				LatitudeLongitudeAltitude.close();
				ArgumentsToDisplay.close();
				
				/* Map display */

		        MapContent map = new MapContent();
		        map.setTitle("Propagator");
		        		        
		        File shapefile = new File(".\\ne_50m_admin_0_sovereignty.shp");
		        FileDataStore dataStore = null;
				try {
					dataStore = FileDataStoreFinder.getDataStore(shapefile);
				} catch (IOException e) {
					e.printStackTrace();
				}
	            String typeName = null;
				try {
					typeName = dataStore.getTypeNames()[0];
				} catch (IOException e) {
					e.printStackTrace();
				}
	            SimpleFeatureSource featureSource = null;
				try {
					featureSource = dataStore.getFeatureSource(typeName);
				} catch (IOException e) {
					e.printStackTrace();
				}
		        
		        Style style = SLD.createSimpleStyle(featureSource.getSchema());
		        Layer layer = new FeatureLayer(featureSource, style);
		        map.addLayer(layer);
		        
		        String ln = "", line = "";
		        int counter = 0;
		        try (BufferedReader br = new BufferedReader(new FileReader(".\\Arguments to display.txt"))) {
					while((ln = br.readLine())!=null) {
						while(counter<98) {
							line = br.readLine();
							String[] lineArray = line.split(",");
				        	Layer pLayer = addPoint(Double.parseDouble(lineArray[2]), Double.parseDouble(lineArray[1]), lineArray[0], counter);
				            map.addLayer(pLayer);
				            counter++;
						}
					}
				}
				catch(Exception e) { 
					e.printStackTrace();  
				}

		        JMapFrame.showMap(map);
			
		}
	
	}
	
	
	/* Add a point to the map */
	
    public static Layer addPoint(double latitude, double longitude, String time, int counter) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("MyFeatureType");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);
        b.add("timestamp", String.class);

        final SimpleFeatureType TYPE = b.buildFeatureType();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        featureBuilder.add(point);
        featureBuilder.add(time);

        SimpleFeature feature = featureBuilder.buildFeature(null);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
        featureCollection.add(feature);

        Color col = new Color(0,0,0);        
        if(counter<49) {
        	col = new Color((int)(153+counter*2.125),0,0);
        } else {
        	counter -= 49;
        	col = new Color(255,(int)(counter*2.125),(int)(counter*2.125));
        }
        
        StyleBuilder styleBuilder = new StyleBuilder();
    	Mark mark = styleBuilder.createMark(StyleBuilder.MARK_CIRCLE, col, Color.WHITE, 0.5);
    	Graphic graphic = styleBuilder.createGraphic(null, mark, null);
    	PointSymbolizer symbolizer = styleBuilder.createPointSymbolizer(graphic);

    	TextSymbolizer textSymbolizer = styleBuilder.createTextSymbolizer(Color.BLACK, styleBuilder.createFont("Arial", 10), "timestamp");

    	Rule rule = styleBuilder.createRule();
    	rule.symbolizers().add(symbolizer);
    	rule.symbolizers().add(textSymbolizer);

    	FeatureTypeStyle featureTypeStyle = styleBuilder.createFeatureTypeStyle(textSymbolizer);
    	featureTypeStyle.rules().add(rule);

    	Style style = styleBuilder.createStyle();
    	style.featureTypeStyles().add(featureTypeStyle);

        Layer layer = new FeatureLayer(featureCollection, style);
        return layer;
    }	
	
	/* Function that returns eccentric anomaly */

	public static double getEccentricAnomaly(double eccentricity, double meanAnomaly) {
		double  oldEccentricAnomaly = meanAnomaly+eccentricity/2,
				newEccentricAnomaly = 0,
				difference = 1;
		while(difference>1E-6) {
			newEccentricAnomaly = oldEccentricAnomaly - (oldEccentricAnomaly-eccentricity*(Math.sin(oldEccentricAnomaly))-meanAnomaly) / (1-eccentricity*(Math.cos(oldEccentricAnomaly)));
			difference = Math.abs(oldEccentricAnomaly-newEccentricAnomaly);
			oldEccentricAnomaly = newEccentricAnomaly;
		}
		return newEccentricAnomaly;
	}
}