package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.DISPLAY_PERIOD;
import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.FileLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;

public class FilterTest {

	public static final float VERSION_NB = 1;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Resources.initialize = true;

		final Odometer odometer = Odometer.getOdometer();

		// Initialize the display
		
		DataEntryProvider versionProvider = new DataEntryProvider("Version") {
			@Override
			public double getEntry() {
				return VERSION_NB;
			}
		};

		DataEntryProvider xProvider = new DataEntryProvider("X") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getX();
			}
		};

		DataEntryProvider yProvider = new DataEntryProvider("Y") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getY();
			}
		};

		DataEntryProvider tProvider = new DataEntryProvider("T") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getDirection(
						CoordinateSystem.POLAR_DEG);
			}
		};

		DataEntryProvider usFrontRawProvider = new DataEntryProvider("US Raw") {

			@Override
			public double getEntry() {
				return Resources.getFrontUSRawData();
			}
		};

		DataEntryProvider usFrontProvider = new DataEntryProvider("US Filter") {

			@Override
			public double getEntry() {
				return Resources.getFrontUSData();
			}
		};

		// LCDLogger lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider,
		// xProvider, yProvider, tProvider, usFrontRawProvider,
		// usFrontProvider);
		// lcdLog.start();

		FileLogger fileLog = new FileLogger("US Filter Test.csv",
				DISPLAY_PERIOD, tProvider, usFrontRawProvider, usFrontProvider);

		Driver driver = Driver.getDriver();
		fileLog.start();
		driver.rotate(360, CoordinateSystem.POLAR_DEG);
		Button.waitForAnyPress();

		fileLog.interrupt();
		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

}
