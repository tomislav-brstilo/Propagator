package org.geotools.Propagator;
public class CoordinateTransforms {
	
	public static final double EarthRadius = 6378165;
	public static final double Earth_a = 6378137;
	public static final double Earth_b = 6356752.3142;
	public static final double gravitationalConstant = 3.986013502E14;
	
	public static final double groundStationLatitude = 45.80150012179227;
	public static final double groundStationLongitude = 15.97093521535156;
	public static final double groundStationHeight = 0;

	public static double[] positionToECI(double[] satellitePosition, double rightAscensionOfTheAscendingNode, double argumentOfPerigee, double inclination) {
		double[] eciCoordinates = {
				satellitePosition[0]*(Math.cos(rightAscensionOfTheAscendingNode)*Math.cos(argumentOfPerigee) - Math.sin(rightAscensionOfTheAscendingNode)*Math.cos(inclination)*Math.sin(argumentOfPerigee))
			  - satellitePosition[1]*(Math.cos(rightAscensionOfTheAscendingNode)*Math.sin(argumentOfPerigee) + Math.sin(rightAscensionOfTheAscendingNode)*Math.cos(inclination)*Math.cos(argumentOfPerigee)),
				satellitePosition[0]*(Math.sin(rightAscensionOfTheAscendingNode)*Math.cos(argumentOfPerigee) + Math.cos(rightAscensionOfTheAscendingNode)*Math.cos(inclination)*Math.sin(argumentOfPerigee))
			  + satellitePosition[1]*(-Math.sin(rightAscensionOfTheAscendingNode)*Math.sin(argumentOfPerigee) + Math.cos(rightAscensionOfTheAscendingNode)*Math.cos(inclination)*Math.cos(argumentOfPerigee)),
				satellitePosition[0]*(Math.sin(argumentOfPerigee)*Math.sin(inclination))
			  + satellitePosition[1]*(Math.cos(argumentOfPerigee)*Math.sin(inclination))
		};
		return eciCoordinates;
	}
	
	public static double[] ECItoECEF(double[] eciCoordinates, double GMST) {
		double[] ecefCoordinates = {
				eciCoordinates[0]*Math.cos(GMST)+eciCoordinates[1]*Math.sin(GMST),
			   -eciCoordinates[0]*Math.sin(GMST)+eciCoordinates[1]*Math.cos(GMST),
				eciCoordinates[2]
		};
		return ecefCoordinates;
	}
	
	public static double[] radcur(double latitude) {
		double  dsq = 1.0 - (1 - Math.pow(Earth_b, 2)/Math.pow(Earth_a, 2)) * Math.pow(Math.sin(Math.toRadians(latitude)), 2),
	    		rn = Earth_a/Math.sqrt(dsq),
	    		rm = rn * (1.0 - (1 - Math.pow(Earth_b, 2)/Math.pow(Earth_a, 2))) / Math.sqrt(dsq),
	    		rho = rn * Math.cos(Math.toRadians(latitude)),
	    		z = (1.0 - (1 - Math.pow(Earth_b, 2)/Math.pow(Earth_a, 2))) * rn * Math.sin(Math.toRadians(latitude)),
	    		rsq = Math.pow(rho, 2) + Math.pow(z, 2);

	    double[] rrnrm = {Math.sqrt(rsq), rn, rm};
	    
	    return rrnrm;
	}
			
	public static double[] ECEFtoLLA(double[] ECEFcoordinates) {
		double  X = ECEFcoordinates[0]/1000,
				Y = ECEFcoordinates[1]/1000,
				Z = ECEFcoordinates[2]/1000,
				p = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
				
		double  e1 = Math.sqrt((Math.pow(Earth_a/1000, 2) - Math.pow(Earth_b/1000, 2))/(Math.pow(Earth_a/1000, 2))),
				e2 = Math.sqrt((Math.pow(Earth_a/1000, 2) - Math.pow(Earth_b/1000, 2))/(Math.pow(Earth_b/1000, 2))),
				theta = Math.atan2(Earth_a/1000 * Z, Earth_b/1000 * p),
				num = Z + Math.pow(e2, 2) * Earth_b/1000 * Math.pow(Math.sin(theta), 3),
				denum = p - Math.pow(e1, 2) * Earth_a/1000 * Math.pow(Math.cos(theta), 3);

		double 	latitude = Math.atan2(num, denum),
				longitude = Math.atan2(Y, X),
				altitude = p / Math.cos(latitude) - (Earth_a/1000 / (Math.sqrt(1 - Math.pow(e1, 2) * Math.pow(Math.sin(latitude), 2))));
		
		if(Math.abs(longitude) >= Math.PI) {
			if(longitude < 0)
				longitude += 2*Math.PI;
			else
				longitude -= 2*Math.PI;
		}
		
		double[] LLA = {latitude, longitude, altitude};
		
		return LLA;
	}

}