package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import lejos.hardware.Button;

import static DPM_TEAM04.Resources.*;

import DPM_TEAM04.Main;

/**
 * Testing the functionality of the LCDLogger class Tests polling at 50 ms and
 * adding entry providers after starting thread
 * 
 * @author Kareem Halabi
 *
 */
public class LCDLoggerTest {

	public static void main(String[] args) {

		Resources.initialize = true;
		Button.waitForAnyPress();

		// Create basic data providers
		DataEntryProvider version = new DataEntryProvider("Version") {
			@Override
			public double getEntry() {
				return Main.VERSION_NB;
			}
		};

		DataEntryProvider csFrontProvider = new DataEntryProvider("ColorID") {

			@Override
			public double getEntry() {
				return getColorRGB()[0];
			}
		};

		DataEntryProvider usFrontProvider = new DataEntryProvider("US Front") {

			@Override
			public double getEntry() {
				return getFrontUSData();
			}
		};

		DataEntryProvider usSideProvider = new DataEntryProvider("US Side") {

			@Override
			public double getEntry() {
				return getSideUSData();
			}
		};

		DataEntryProvider csDownProvider = new DataEntryProvider("CS Down") {

			@Override
			public double getEntry() {
				return getDownCSData();
			}
		};

		LCDLogger lcdLog = new LCDLogger(50, 2);

		// start logger with one provider
		lcdLog.addToEntryProviders(version);
		lcdLog.start();

		Button.waitForAnyPress();

		// add the rest of the providers
		lcdLog.addToEntryProviders(csFrontProvider, usFrontProvider,
				usSideProvider, csDownProvider);
		Button.waitForAnyPress();

		// stop the logger
		lcdLog.interrupt();
	}
}
