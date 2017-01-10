package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import lejos.hardware.Button;

/**
 * Test class to log the distance values fetched from the ultrasonic sensor. Data entries provided to the LCD logger and the 
 * file logger (to output a .csv file).
 * @author KareemHalabi
 */
public class UltrasonicDistanceTest {

	public static void main(String[] args) {

		Resources.initialize = true;

		DataEntryProvider usRawProvider = new DataEntryProvider("F Raw") {

			@Override
			public double getEntry() {
				return Resources.getFrontUSRawData();
			}
		};

		DataEntryProvider usFilteredProvider = new DataEntryProvider("F Filter") {

			@Override
			public double getEntry() {
				return Resources.getFrontUSData();
			}
		};

		DataEntryProvider usRawSide = new DataEntryProvider("S Raw") {

			@Override
			public double getEntry() {
				return Resources.getSideUSRawData();
			}
		};

		DataEntryProvider usFilteredSide = new DataEntryProvider("S Filtered") {

			@Override
			public double getEntry() {
				return Resources.getSideUSData();
			}
		};

		LCDLogger lcdLog = new LCDLogger(10, 2, usRawProvider,
				usFilteredProvider, usRawSide, usFilteredSide);

		lcdLog.start();
		Button.waitForAnyPress();
		System.exit(0);
	}

}
