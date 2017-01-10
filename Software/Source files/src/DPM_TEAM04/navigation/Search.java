package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import java.util.ArrayList;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;
import lejos.robotics.geometry.Point2D;
import lejos.utility.Delay;

/**
 * Once the localization is done, the search thread is started. It starts
 * searching optimally and drives to the first object it sees. It then analyses
 * if it is a styrofoam block or an obstacle. It picks the styrofoam blocks and
 * brings it back to its zone. Otherwise it goes back to its searching point and
 * keep searching for styrofoam blocks.
 * 
 * @author Alexis Giguere-Joannette, Tristan Toupin
 *
 */
public class Search extends Thread {

	public static DirectedCoordinate position;
	public static Driver driver = Driver.getDriver();
	public static boolean blockSeen = false;
	public static int blockDistanceCap = 5, builderZoneCorner = 0;
	public static int searchCap = 70;
	public static int nextAngleDelta = 25;
	public static double lastAngle;
	public static boolean clockwise = true; // if true will rotate clockwise, else
										// counter Clockwise
	public static double angleDifference, actualAngle;
	public static double firstAngle, startSearchAngle, endSearchAngle;
	private static ArrayList<Point2D> listOfWaypoints = new ArrayList<Point2D>();
	
	public Search() {
		
	}

	public void run() {
		
		
		ObstacleAvoidance obsAvoid = new ObstacleAvoidance();
		obsAvoid.start();
		
		
		long startTime = System.currentTimeMillis();

		//driver = Driver.getDriver();
		position = Odometer.getOdometer().getPosition();

		grabMotor.setAcceleration(ACCELERATION_SMOOTH);
		liftMotor.setAcceleration(ACCELERATION_SMOOTH);
		grabMotor.setSpeed(SPEED_GRAB);
		liftMotor.setSpeed(SPEED_LIFT);

		startSearchAngle = 0;
		endSearchAngle = 360;

		// Go to the search point
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN,
				searchPoint.x, searchPoint.y)));
		
		Point2D actualPoint = new Point2D.Double(position.getX(), position.getY());
		while(searchPoint.distanceSq(actualPoint)  > 4.0) {
			actualPoint = new Point2D.Double(position.getX(), position.getY());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Not expected to be interrupted
		}
		
		
		/*
		 * 
		 * STOP OBSTACLE AVOIDANCE
		 * 
		 */
		isSearching = true;
		
		// Time it took to get to the green zone
		long endTime = System.currentTimeMillis();
		TIME_LEFT = (int)(endTime-startTime);
		System.out.println(TIME_LEFT);

		leftMotor.stop(true);
		rightMotor.stop(false);
		

		if (searchPoint.x < mapCenter.x) {
			if (searchPoint.y < mapCenter.y) {
				// Bottom left quadrant
				startSearchAngle = 5;
				endSearchAngle = 90;
				builderZoneCorner = 1;
			} else {
				// Top left quadrant
				startSearchAngle = 270;
				endSearchAngle = 355;
				builderZoneCorner = 4;
			}
		} else {
			if (searchPoint.y < mapCenter.y) {
				// Bottom right quadrant
				startSearchAngle = 90;
				endSearchAngle = 180;
				builderZoneCorner = 2;
			} else {
				// Top right quadrant
				startSearchAngle = 180;
				endSearchAngle = 270;
				builderZoneCorner = 3;
			}
		}
		
		
		/* ODO CORRECTION START 
		 * 
		 * 
		 * THE CODE IS COMMENTED BECAUSE ODOMETRY CORRECTION CONTAINS BUGS THAT MAKES THE ODOMETRY WORST.
		 * Would need to work on odometry correction for further improvements.
		 * 
		 * 
		
		odoCorrection.isFacingStart = false;
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, odoCorrectionPoint.x, odoCorrectionPoint.y)));
		driver.turnTo(endSearchAngle, CoordinateSystem.POLAR_DEG, false);
		odoCorrection.prepareCorrection();
		
		odoCorrection.isFacingStart = true;
		driver.turnTo(startSearchAngle - 10, CoordinateSystem.POLAR_DEG, false);
		odoCorrection.prepareCorrection();
		
		driver.turnTo(-115, CoordinateSystem.POLAR_DEG, false);
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);
		odoCorrection.doCorrection();
		
		ODO CORRECTION END */

		driver.turnTo(startSearchAngle, CoordinateSystem.POLAR_DEG, false);
		search();
	}

	/**
	 * Performs the search. Scans with the front ultrasonic sensor until it gets
	 * close enough to a block. Once it is close to a block it calls scanBlock()
	 * to analyse the block.
	 */
	public static void search() {
		blockSeen = false;
		lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
		clockwise = true;
		boolean firstTime = true;
		double actualAngle = lastAngle;
		
		while (true) {
			double USDistance = Resources.getFrontUSData();
			
			if (!blockSeen || USDistance > searchCap) {
				actualAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
				System.out.println("\n\n\n\n\n" + actualAngle);
				if (endSearchAngle < startSearchAngle) {
					endSearchAngle += 360;
				}
				if (actualAngle < (startSearchAngle - 5.0)) {
					actualAngle += 360;
				}
				if (actualAngle > (endSearchAngle - 5.0) && clockwise) {
					clockwise = !clockwise;
					searchStep++;
				}
				if (endSearchAngle > 360) {
					endSearchAngle -= 360;
				}
				
				/*
				 * 
				 * IF WE REACH THE END ANGLE, MOVE THE SEARCH POINT
				 *  
				 */
				
				if (searchStep == 1) {
					
					searchStep = 2;
					
					leftMotor.stop(true);
					rightMotor.stop(false);
					
					
					driver.turnTo((startSearchAngle + 45.0), CoordinateSystem.POLAR_DEG);
					if (getFrontUSData() < 50.0) {
						
						// If there's an obstacle in the middle, move to another point.
						
						driver.turnTo((startSearchAngle), CoordinateSystem.POLAR_DEG);
						if (getFrontUSData() < 70.0) {

							if (builderZoneCorner == 1) {
								searchPoint = new Point2D.Double(searchPoint.getX(), searchPoint.getY()+TILE_WIDTH);
							} else if (builderZoneCorner == 2) {
								searchPoint = new Point2D.Double(searchPoint.getX()-TILE_WIDTH, searchPoint.getY());
							} else if (builderZoneCorner == 3) {
								searchPoint = new Point2D.Double(searchPoint.getX(), searchPoint.getY()-TILE_WIDTH);
							} else if (builderZoneCorner == 4) {
								searchPoint = new Point2D.Double(searchPoint.getX()+TILE_WIDTH, searchPoint.getY());
							}
							
						} else {
							
							if (builderZoneCorner == 1) {
								searchPoint = new Point2D.Double(searchPoint.getX()+2.0*TILE_WIDTH, searchPoint.getY());
							} else if (builderZoneCorner == 2) {
								searchPoint = new Point2D.Double(searchPoint.getX(), searchPoint.getY()+2.0*TILE_WIDTH);
							} else if (builderZoneCorner == 3) {
								searchPoint = new Point2D.Double(searchPoint.getX()-2.0*TILE_WIDTH, searchPoint.getY());
							} else if (builderZoneCorner == 4) {
								searchPoint = new Point2D.Double(searchPoint.getX(), searchPoint.getY()-2.0*TILE_WIDTH);
							}
							
						}
					} else {
						
						if (builderZoneCorner == 1) {
							searchPoint = new Point2D.Double(searchPoint.getX()+TILE_WIDTH, searchPoint.getY()+TILE_WIDTH);
						} else if (builderZoneCorner == 2) {
							searchPoint = new Point2D.Double(searchPoint.getX()-TILE_WIDTH, searchPoint.getY()+TILE_WIDTH);
						} else if (builderZoneCorner == 3) {
							searchPoint = new Point2D.Double(searchPoint.getX()-TILE_WIDTH, searchPoint.getY()-TILE_WIDTH);
						} else if (builderZoneCorner == 4) {
							searchPoint = new Point2D.Double(searchPoint.getX()+TILE_WIDTH, searchPoint.getY()-TILE_WIDTH);
						}
						
					}
					
					listOfWaypoints.add(searchPoint);
					
					
					
					
					
					driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN,
							searchPoint.x, searchPoint.y)));
					driver.turnTo(startSearchAngle+5.0, CoordinateSystem.POLAR_DEG);
					clockwise = true;
					blockSeen = false;
					
				}
				
				
				/*
				 * Right now it only deals with one case
				 */
				scanning();
			}

			if (USDistance < blockDistanceCap) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				break;
			} else if (isObjectSeen(USDistance)) {
				blockSeen = true;
				// move forward
				
				leftMotor.setSpeed(SPEED_FORWARD);
				rightMotor.setSpeed(SPEED_FORWARD);
				leftMotor.forward();
				rightMotor.forward();
				lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);

				if (firstTime) {
					firstAngle = lastAngle;
					firstTime = false;
				}

				// Delay before next poll
				Delay.msDelay(50);

			} else if (blockSeen) {
				// we went too far, re-initialize values so its at initial state
				// (no block seen) and turn the other way around.
				actualAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
				angleDifference = Math.abs(actualAngle - lastAngle);
				if (angleDifference >= 180) {
					// Doesn't make sense to have such a big difference, so one
					// angle must have been cap
					// add 360 deg to "uncap" and compute the real difference
					// between the angles
					if (actualAngle > lastAngle) {
						lastAngle += 360;
					} else {
						actualAngle += 360;
					}
					angleDifference = Math.abs(actualAngle - lastAngle);
				}
				if (angleDifference > 25) {

					blockSeen = false;
					clockwise = !clockwise;

				}
			}

		}

		scanBlock();

	}

	/**
	 * Interprets the data from the front ultrasonic sensor to determine if
	 * there is an object seen or if it is a wall.
	 * 
	 * @param USDistance
	 *            Data from the ultrasonic sensor.
	 * @return Returns true if there is an object seen. Returns false if no object
	 *         is seen.
	 */
	public static boolean isObjectSeen(double USDistance) {

		if (USDistance <= searchCap) {

			double theta = position.getDirection(CoordinateSystem.POLAR_RAD);
			Point2D.Double ObjectPoint = new Point2D.Double((USDistance
					* Math.cos(theta)) + position.getX(), (USDistance
					* Math.sin(theta)) + position.getY());
			
			if (collectorZone.contains(ObjectPoint) || builderZone.contains(ObjectPoint)) {
				// If object seen is inside the green zone or the red zone
				wallSeen();
				return false;
			}
			
			if (mapWithoutWalls.contains(ObjectPoint)) {
				// good (it is an object)
				return true;
			} else {
				// is too close to a wall
				if (blockSeen) {
					// If an object was seen, search the other way than the wall
					clockwise = !clockwise;
					search();
				} else {
					wallSeen();
				}
				return false;
			}
		} else {
			return false;
		}

	}

	/**
	 * Turn the robot either clockwise or counter-clockwise to search the area.
	 */
	public static void scanning() {
		leftMotor.setSpeed(SPEED_SCANNING);
		rightMotor.setSpeed(SPEED_SCANNING);
		if (clockwise) {
			leftMotor.backward();
			rightMotor.forward();
		} else {
			leftMotor.forward();
			rightMotor.backward();
		}
		return;
	}

	/**
	 * Scan an object to know if it is an obstacle or a styrofoam block.
	 */
	public static void scanBlock() {

		// purposely collide into block
		if (clockwise) {
			driver.rotate(7, CoordinateSystem.POLAR_DEG);
		} else {
			driver.rotate(-7, CoordinateSystem.POLAR_DEG);
		}
		// driver.travelDistance(blockDistanceCap);
		float[] colorRGB = getColorRGB();
		if (colorRGB[1] > colorRGB[0] && colorRGB[1] > colorRGB[2]) {
			captureBlock();
		} else {
			System.out.println("Not Block");
			notBlock();
		}
	}

	/**
	 * If it is not a block, go back to the search point and continue searching.
	 */
	public static void notBlock() {
		driver.travelDistance(-BUMPER_TO_CENTER);
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN,
				searchPoint.x, searchPoint.y)));
		double nextAngle = (lastAngle + nextAngleDelta) % 360.0;
		driver.turnTo(nextAngle, CoordinateSystem.POLAR_DEG);
		clockwise = true;
		search();
	}

	/**
	 * If the object is too close to a wall (or inside the green or red zone), turn instantly in the direction it was scanning.
	 */
	public static void wallSeen() {
		leftMotor.stop(true);
		rightMotor.stop(false);
		if (clockwise) {
			driver.rotate(25, CoordinateSystem.POLAR_DEG);
		} else {
			driver.rotate(-25, CoordinateSystem.POLAR_DEG);
		}
		search();
	}

	/**
	 * If the object scanned is a styrofoam block, grab the block, travel back to the waypoints and to the stack point, drop the 
	 * block according to the @towerHeight and return back to the search points (passing by the waypoints to avoid obstacles).
	 */
	public static void captureBlock() {
		
		isHoldingBlock = true;

		System.out.println("Block!");

		// Reset the lift position to the bumper
		if (liftPosition != 0) {
			liftMotor.rotate(-liftPosition, false);
			liftPosition = 0;
		}

		// Set the angles according to the tower height
		int liftAngle = 0, unliftAngle = 0;
		int turnToStack = 45;
		if (towerHeight == 0) {
			liftAngle = -450;
			unliftAngle = 450;
			turnToStack = 43;
		} else if (towerHeight == 1) {
			liftAngle = -900;
			unliftAngle = 100;
			turnToStack = 41;
		} else if (towerHeight == 2) {
			liftAngle = -1800;
			unliftAngle = 100;
			turnToStack = 39;
		} else if (towerHeight == 3) {
			liftAngle = -2600;
			unliftAngle = 100;
			turnToStack = 37;
		}
		liftPosition = liftAngle + unliftAngle;

		// Place the block in a good direction
		driver.travelDistance(-1.0);
		driver.rotate(Math.PI, CoordinateSystem.POLAR_RAD);
		driver.travelDistance(-Math.abs(BUMPER_TO_CENTER - US_TO_CENTER) - 2.0);

		// orient the block to make it easier to grab
		if (clockwise) {
			rightMotor.rotate(-90, false);
			leftMotor.rotate(-90, true);
		} else {
			leftMotor.rotate(-90, false);
			rightMotor.rotate(-90, true);
		}
	
		// grab the block
		grabMotor.rotate(240, false);
		liftMotor.rotate(liftAngle, true);

		// return to the center of the builder zone by going to the original search point (which is now called stack point)
		for (int i=(listOfWaypoints.size()-1); i>=0; i--) {
			driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, listOfWaypoints.get(i).getX(), listOfWaypoints.get(i).getY())));
		}
		
		/*
		 * 
		 * ODOMETRY CORRECTION
		 * 
		 * 
		odoCorrection.isFacingStart = false;
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, odoCorrectionPoint.x, odoCorrectionPoint.y)));
		
		//here
		driver.turnTo(45, CoordinateSystem.POLAR_DEG, false);
		odoCorrection.prepareCorrection();
		
		
		//odoCorrection.isFacingStart = true;
		//driver.turnTo(0 - 10, CoordinateSystem.POLAR_DEG, false);
		
		//odoCorrection.prepareCorrection();
		
		//do odometryCorrection
		driver.turnTo(-115, CoordinateSystem.POLAR_DEG, false);
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);
		odoCorrection.doCorrection();
		*/
		
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, stackPoint.x, stackPoint.y)));
		
		driver.turnTo(startSearchAngle, CoordinateSystem.POLAR_DEG, false);
		driver.rotate(turnToStack, CoordinateSystem.POLAR_DEG, false);
		driver.travelDistance(-(Math.hypot(HALF_TILE_WIDTH, HALF_TILE_WIDTH)));
		
		// drop the block
		liftMotor.rotate(unliftAngle, false);
		grabMotor.rotate(-190, false);
		driver.travelDistance(3.0);
		// Reposition the bumper
		if (liftPosition != 0) {
			liftMotor.rotate(-liftPosition, false);
			liftPosition = 0;
		}
		grabMotor.rotate(-50, false);

		// Go back to the search point
		for (int i=0; i<listOfWaypoints.size(); i++) {
			driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, listOfWaypoints.get(i).getX(), listOfWaypoints.get(i).getY())));
		}
		
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));

		// turn to the next angle to search
		double nextAngle = (lastAngle + nextAngleDelta) % 360.0;
		driver.turnTo(nextAngle, CoordinateSystem.POLAR_DEG);
		
		isHoldingBlock = false;

		towerHeight++;
		
		// If tower is 4 blocks high, exit the program
		if (towerHeight >= 4) {
			System.exit(0);
		}
		
		clockwise = true;
		lastAngle = startSearchAngle;
		search();

	}
}