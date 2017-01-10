package DPM_TEAM04;

import static DPM_TEAM04.Resources.*;

import java.io.IOException;
import java.util.HashMap;

import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.FileLogger;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.navigation.Search;
import DPM_TEAM04.odometry.Localization;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;
import lejos.robotics.geometry.Point2D;
import lejos.robotics.geometry.Rectangle2D;
import wifi.WifiConnection;

/**
 * Main thread. Initializes and starts subsequent threads. 
 * Create the Builder/Garbage collector zone. 
 * Create StackPoint point, odometry correction point and searching point.
 * Create a log file named Log_Test.csv with diverse input
 * 
 * @author Tristan Toupin, Alexis GJ
 */
public class Main {

	public static final double VERSION_NB = 3.0;

	public static LCDLogger lcdLog;

	public static void main(String[] args) {

		// Forces initialization of Resources
		Resources.initialize = true;

		// Initialize the odometer
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
						CoordinateSystem.POLAR_RAD);
			}
		};

		lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider, xProvider,
				yProvider, tProvider);

		// Display display = new Display(odometer);

		// Initialize the localization thread
		Localization localization = new Localization();

		@SuppressWarnings("unused")
		Driver driver = Driver.getDriver();
		Search search = new Search();

		/*
		 * 
		 * 
		 * START OTHER THREADS
		 * 
		 * START THE WIFI CONNECTION AND WAIT TO RECEIVE THE PARAMETERS
		 * 
		 * PASS THE PARAMETERS TO THE RESOURCES
		 */

		lcd.clear();

		/*
		 * WiFiConnection will establish a connection to the server and wait for
		 * data If the server is not running, this will throw an IOException If
		 * the server is running but the user has yet to press start on the Java
		 * GUI with some data, this will wait forever During the competition,
		 * this means you can start your code, place it on the field, and it
		 * will wait for data from the professor's computer If you need it to
		 * stop, access the robot via the EV3Control program and click
		 * "Stop Program" Alternatively, you can reset the robot but you risk SD
		 * card corruption Note that you can set the final argument debugPrint
		 * as false to disable printing to the LCD if desired.
		 */
		WifiConnection conn = null;
		try {
			// System.out.println("Connecting...");
			conn = new WifiConnection(Resources.SERVER_IP,Resources.TEAM_NUMBER, false);
		} catch (IOException e) {
			System.out.println("Connection failed");
			Button.waitForAnyPress();
			System.exit(0);
		}

		lcd.clear();

		/*
		 * This section of the code reads and prints the data received from the
		 * server, stored as a HashMap with String keys and Integer values.
		 */
		if (conn != null) {
			HashMap<String, Integer> connData = conn.StartData;
			if (connData == null) {
				System.out.println("Failed to read transmission");
				Button.waitForAnyPress();
				System.exit(0);


			} else {

				// we just got the data from the wifi connection
				wifiData = connData;

				double builderWidth = (connData.get("UGZx") - connData
						.get("LGZx")) * TILE_WIDTH;
				double builderHeight = (connData.get("UGZy") - connData
						.get("LGZy")) * TILE_WIDTH;

				double collectorWidth = (connData.get("URZx") - connData
						.get("LRZx")) * TILE_WIDTH;
				double collectorHeight = (connData.get("URZy") - connData
						.get("LRZy")) * TILE_WIDTH;


				if (connData.get("BTN") == TEAM_NUMBER) {
					isBuilder = true;
					startingCorner = connData.get("BSC");
					
					// we create rectangles for the 2 zones
					builderZone = new Rectangle2D.Double(connData.get("LGZx")
							* TILE_WIDTH, connData.get("LGZy") * TILE_WIDTH,
							builderWidth, builderHeight);
					collectorZone = new Rectangle2D.Double(connData.get("LRZx")
							* TILE_WIDTH, connData.get("LRZy") * TILE_WIDTH,
							collectorWidth, collectorHeight);
				} else {
					isBuilder = false;
					startingCorner = connData.get("CSC");
					
					// we create rectangles for the 2 zones
					collectorZone = new Rectangle2D.Double(connData.get("LGZx")
							* TILE_WIDTH, connData.get("LGZy") * TILE_WIDTH,
							builderWidth, builderHeight);
					builderZone = new Rectangle2D.Double(connData.get("LRZx")
							* TILE_WIDTH, connData.get("LRZy") * TILE_WIDTH,
							collectorWidth, collectorHeight);
				}
				
				collectorZone = new Rectangle2D.Double(collectorZone.getMinX()-TILE_WIDTH, collectorZone.getMinY()-TILE_WIDTH,
						collectorZone.getWidth()+2.0*TILE_WIDTH, collectorZone.getHeight()+2.0*TILE_WIDTH);
				

				// get the center of the builder zone to know it is in which
				// quarter (compared to the center of the map)

				mapCenter = new Point2D.Double(
						(((MAP_DIMENSION / 2.0) - 1.0) * TILE_WIDTH),
						(((MAP_DIMENSION / 2.0) - 1.0) * TILE_WIDTH));
				mapWithoutWalls = new Rectangle2D.Double(HALF_TILE_WIDTH, HALF_TILE_WIDTH,
						((MAP_DIMENSION-3) * TILE_WIDTH), ((MAP_DIMENSION-3)
								* TILE_WIDTH));

				// Determines the stack point
				// Is is the "corner" of the builder zone that is in the
				// orientation of the red zone
				if (builderZone.getCenterX() < mapCenter.x) {
					if (builderZone.getCenterY() < mapCenter.y) {
						// Bottom left quadrant
						stackPoint = new Point2D.Double(builderZone.getMinX()
								+ HALF_TILE_WIDTH, builderZone.getMinY()
								+ HALF_TILE_WIDTH);
					} else {
						// Top left quadrant
						stackPoint = new Point2D.Double(builderZone.getMinX()
								+ HALF_TILE_WIDTH, builderZone.getMaxY()
								- HALF_TILE_WIDTH);
					}
				} else {
					if (builderZone.getCenterY() < mapCenter.y) {
						// Bottom right quadrant
						stackPoint = new Point2D.Double(builderZone.getMaxX()
								- HALF_TILE_WIDTH, builderZone.getMinY()
								+ HALF_TILE_WIDTH);
					} else {
						// Top right quadrant
						stackPoint = new Point2D.Double(builderZone.getMaxX()
								- HALF_TILE_WIDTH, builderZone.getMaxY()
								- HALF_TILE_WIDTH);
					}
				}

				// Determines the search point
				// It is the closest "corner" to the center of the map (and on
				// the border of the builder zone)
				if (builderZone.getCenterX() < mapCenter.x) {
					if (builderZone.getCenterY() < mapCenter.y) {
						// Bottom left quadrant
						searchPoint = new Point2D.Double(builderZone.getMinX()
								+ HALF_TILE_WIDTH + TILE_WIDTH, builderZone.getMinY()
								+ HALF_TILE_WIDTH + TILE_WIDTH);
						odoCorrectionPoint = new Point2D.Double(searchPoint.x - 1.5*QUARTER_TILE_WIDTH, searchPoint.y - 1.5*QUARTER_TILE_WIDTH);
						cornerX = searchPoint.x + HALF_TILE_WIDTH;
						cornerY = searchPoint.y + HALF_TILE_WIDTH;
					} else {
						// Top left quadrant
						searchPoint = new Point2D.Double(builderZone.getMinX()
								+ HALF_TILE_WIDTH  + TILE_WIDTH, builderZone.getMaxY()
								- HALF_TILE_WIDTH - TILE_WIDTH);
						odoCorrectionPoint = new Point2D.Double(searchPoint.x - 1.5*QUARTER_TILE_WIDTH, searchPoint.y - 1.5*QUARTER_TILE_WIDTH);
						cornerX = searchPoint.x + HALF_TILE_WIDTH;
						cornerY = searchPoint.y + HALF_TILE_WIDTH;
					}
				} else {
					if (builderZone.getCenterY() < mapCenter.y) {
						// Bottom right quadrant
						searchPoint = new Point2D.Double(builderZone.getMaxX()
								- HALF_TILE_WIDTH - TILE_WIDTH, builderZone.getMinY()
								+ HALF_TILE_WIDTH + TILE_WIDTH);
						odoCorrectionPoint = new Point2D.Double(searchPoint.x - 1.5*QUARTER_TILE_WIDTH, searchPoint.y - 1.5*QUARTER_TILE_WIDTH);
						cornerX = searchPoint.x + HALF_TILE_WIDTH;
						cornerY = searchPoint.y + HALF_TILE_WIDTH;
					} else {
						// Top right quadrant
						searchPoint = new Point2D.Double(builderZone.getMaxX()
								- HALF_TILE_WIDTH - TILE_WIDTH, builderZone.getMaxY()
								- HALF_TILE_WIDTH - TILE_WIDTH);
						odoCorrectionPoint = new Point2D.Double(searchPoint.x - 1.5*QUARTER_TILE_WIDTH, searchPoint.y - 1.5*QUARTER_TILE_WIDTH);
						cornerX = searchPoint.x + HALF_TILE_WIDTH;
						cornerY = searchPoint.y + HALF_TILE_WIDTH;
					}
				}
				
				/*
				 * 
				 * RESET THE STACK POINT TO THE SEARCH POINT, THE ROBOT IS GOING TO STACK FROM THIS POINT
				 * 
				 */
				
				stackPoint = new Point2D.Double(searchPoint.x, searchPoint.y);

				

			}
		}

		/*
		 * 
		 * 
		 * 
		 * END OF WIFI CONNECTION
		 */

		odometer.start();
		lcdLog.start();

		/*
		 * 
		 * LOGGER
		 */

		// Create basic data providers
		DataEntryProvider angleProvider = new DataEntryProvider(
				"Angle PolarDeg") {
			@Override
			public double getEntry() {
				return Odometer.getOdometer().getPosition()
						.getDirection(CoordinateSystem.POLAR_DEG);
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
		
		DataEntryProvider csFrontProvider = new DataEntryProvider("CS Side") {

			@Override
			public double getEntry() {
				return getDownCSData();
			}
		};
		
		DataEntryProvider leftMotorProvided = new DataEntryProvider("Left motor") {

			@Override
			public double getEntry() {
				return leftMotor.getSpeed();
			}
		};
		
		DataEntryProvider rightMotorProvided = new DataEntryProvider("Right motor") {

			@Override
			public double getEntry() {
				return rightMotor.getSpeed();
			}
		};

		FileLogger fileLog = new FileLogger("Log_Test.csv", 50, angleProvider,usFrontProvider, usSideProvider, csFrontProvider, leftMotorProvided, rightMotorProvided);

		// start logger
		fileLog.start();
		
		
		localization.start();
		
		try {
			localization.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		search.start();


		// save and close logger
		fileLog.interrupt();

		Button.waitForAnyPress();
		System.exit(0);
	}

}
