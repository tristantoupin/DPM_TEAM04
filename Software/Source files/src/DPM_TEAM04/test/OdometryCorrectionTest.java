package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.DISPLAY_PERIOD;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.odometry.Odometer;
import DPM_TEAM04.odometry.OdometryCorrection;
import lejos.hardware.Button;


/**
 * Odometry correction test class. Will test the odometry correction and will 
 * succeed if the wheel base is placed in the bottom left quadrant of the
 * light sensor in the top right quadrant.
 * 
 * @author tristansaumure-toupin
 *
 */
public class OdometryCorrectionTest {

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

		Driver driver = Driver.getDriver();
		
		OdometryCorrection odoCorrection = new OdometryCorrection();
		
		while(true) {
		
		driver.rotate(-115, CoordinateSystem.POLAR_DEG, false);
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);
		odoCorrection.doCorrection();
	
		
		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 0, 0));
		driver.turnTo(0.0, CoordinateSystem.POLAR_DEG);
		
		Button.waitForAnyPress();
		}
		
	}

}

