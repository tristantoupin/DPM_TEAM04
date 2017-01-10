package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.navigation.Search;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;


/**
 * Test class to test the search algorithm. It will perform the search 
 * algorithm starting from (0.0,0.0).
 * 
 * @author Alexis GJ & Tristan Toupin
 *
 */

@Deprecated
public class SearchTest {

	public static void main(String[] args) {

		Resources.initialize = true;

		// Initialize the odometer
		final Odometer odometer = Odometer.getOdometer();

		odometer.start();
		(new Search()).start();

		Button.waitForAnyPress();
		System.exit(0);
	}

}
