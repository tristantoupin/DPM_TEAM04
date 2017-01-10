package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.navigation.ObstacleAvoidance;
import DPM_TEAM04.odometry.Odometer;
import lejos.robotics.geometry.Point2D;


/**
 * Test class for the obstacle avoidance thread. Will start the odometer, driver, 
 * ExitThread and the obstacle avoidance threads.
 * 
 * @author tristansaumure-toupin
 *
 */
public class ObstacleAvoidTest {

	public static LCDLogger lcdLog;

	public static void main(String[] args) {

		Resources.initialize = true;

		// Initialize the odometer
		final Odometer odometer = Odometer.getOdometer();
		Driver driver = Driver.getDriver();

		// Initialize the display
		DataEntryProvider versionProvider = new DataEntryProvider("Version") {
			@Override
			public double getEntry() {
				return 3.1415;
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

		lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider, xProvider,
				yProvider, tProvider);

		ExitThreadForCollectingGrabbingTest exit = new ExitThreadForCollectingGrabbingTest();
		exit.start();

		odometer.start();
		lcdLog.start();

		searchPoint = new Point2D.Double((6.0*TILE_WIDTH), 0.0);
		
		@SuppressWarnings("static-access")
		Coordinate position = odometer.getOdometer().getPosition();
		
		(new ObstacleAvoidance()).start();

		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, (6.0 * TILE_WIDTH), 0.0));
		Point2D destPoint = new Point2D.Double((6.0 * TILE_WIDTH), 0.0);
		Point2D actualPoint = new Point2D.Double(position.getX(), position.getY());
		while(destPoint.distanceSq(actualPoint)  > 4.0) {
			actualPoint = new Point2D.Double(position.getX(), position.getY());
		}
		
		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 0.0, 0.0));

	}
	

}