package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.FileLogger;
import DPM_TEAM04.logging.LCDLogger;
import lejos.hardware.Button;
import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.Main;

/**
 * Testing the functionality of the FileLogger class Tests polling at 50 ms
 * 
 * @author Kareem Halabi
 */
@SuppressWarnings("unused")
public class FileLoggerTest {

	public static void main(String[] args) {

		Resources.initialize = true;
		Button.waitForAnyPress();

		// Create basic data providers
		DataEntryProvider systemTimeProvider = new DataEntryProvider(
				"System Time") {
			@Override
			public double getEntry() {
				return System.currentTimeMillis();
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

		FileLogger fileLog = new FileLogger("Log_Test.csv", 50,
				systemTimeProvider, csFrontProvider, usFrontProvider,
				usSideProvider, csDownProvider);

		// start logger
		fileLog.start();
		Button.waitForAnyPress();

		// save and close logger
		fileLog.interrupt();
	}
}
