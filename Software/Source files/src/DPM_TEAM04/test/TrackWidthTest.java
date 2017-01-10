package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.DISPLAY_PERIOD;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;


/**
 * Test class to find the best Track width. Must be performed after the Wheel radius test class. 
 * The robot will execute 10 laps on itself. If it does not complete the 10 laps, the @Resources.TRACK
 * must be grater. If the robot complete more than 10 laps, the @Resources.TRACK must be decreased.
 * 
 * @author Tristan Toupin
 *
 */
public class TrackWidthTest {

	public static final int VERSION_NB = 1;

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

		LCDLogger lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider,
				xProvider, yProvider, tProvider);

		odometer.start();
		lcdLog.start();

		Coordinate coordinate = new Coordinate(CoordinateSystem.POLAR_DEG, 0, 0.0);// coordinate(system.PolarDeg, length, angle)
		Driver driver = Driver.getDriver();

		// To test the Track
		while (true) {
			coordinate.setCoordinate(CoordinateSystem.POLAR_DEG, 0.0, 0.0);
			driver.rotate(-360.0*10, CoordinateSystem.HEADING_DEG);

			// if not escape, break while loop and repeat
			while (true) {
				if (Button.waitForAnyPress() != Button.ID_ESCAPE) {
					break;
				} else {
					System.exit(0);
				}
			}
		}
	}
}