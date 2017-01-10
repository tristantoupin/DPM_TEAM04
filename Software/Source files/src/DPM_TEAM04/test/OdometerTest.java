package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;

import static DPM_TEAM04.Resources.*;

/**
 * Test class to test the odometer using float motors.
 * 
 * @author team04
 *
 */
public class OdometerTest {

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
		leftMotor.flt();
		rightMotor.flt();

		Button.waitForAnyPress();
		System.exit(0);
	}

}
