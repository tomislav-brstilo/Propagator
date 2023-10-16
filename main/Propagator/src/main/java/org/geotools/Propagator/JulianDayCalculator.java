//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
package org.geotools.Propagator;

public class JulianDayCalculator {

	public static double CalculateTime(double Day, int Month, int Year) {
	
		Double A = 0.0;
		Double B = 0.0;
		
		double DayFrac = Day - Math.floor(Day);
		Day = Math.floor(Day);
				
		if (Month < 3) {
			if (Month==1)
				Month = 13;
			else
				Month = 14;
			Year  = Year - 1;
		}
				
		if ((Year>1582) || ((Year==1582) && (Month>10)) || ((Year==1582) && (Month==10) && (Day>=15))) {
			A = Math.floor(Year/100);
			B = 2 - A + Math.floor(A/4);
		}
				
		double JD = Math.floor(365.25*Year + 365.25*4716.0) + Math.floor(30.6001*Month + 30.6001) + Day + B - 1524.5 + DayFrac;
						
		double DayOfWeek = (JD-0.5) % 7 + 3;
		if (DayOfWeek > 7)
			DayOfWeek = DayOfWeek - 7;
		
		return JD;
	}
}