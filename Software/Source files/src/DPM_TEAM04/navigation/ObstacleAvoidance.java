package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Sound;

/**
 * Extends Thread. Constantly check if there is an obstacle to avoid when travelling to a destination and the robot is not 
 * in a searching state. If an object is seen, it checks whether it is an obstacle or a styrofoam block. If the object is 
 * an obstacle, the robot is in obstacle avoidance state until it reaches the original angle and the path is clear.
 * 
 * @author Tristan Toupin, Alexis Giguere-Joannette
 *
 */
public class ObstacleAvoidance extends Thread {

	private DirectedCoordinate position;
	private Driver driver = Driver.getDriver();
	public static Object lock;
	private static boolean isAvoiding = false;

	public ObstacleAvoidance() {
		lock = new Object();
	}
	
	public void run() {

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		// While loop that checks forever if there is an obstacle.
		while (true) {

			while (!isSearching && !getIsAvoiding()) {
				if (driver.getIsTravelling()) {
					// Checks if obstalce when travelling and not searching
					isThereObstacle();
				}
			}

			if (getIsAvoiding()) {
				// When the robot is in avoidance state
				position = Odometer.getOdometer().getPosition();
				
				synchronized (driver) {
					// Interrupt the driver thread (to stop the motors)
					driver.interrupt();
					driver.setIsTravelling(false);
				}
				Sound.beep();
				double firstAng = position.getDirection(CoordinateSystem.POLAR_DEG);

				leftMotor.stop(true);
				rightMotor.stop(false);

				// Rotate -90 degrees when obstacle was seen
				driver.rotate(-90, CoordinateSystem.POLAR_DEG);
				
				while (Math.abs((firstAng + 360) - position.getDirection(CoordinateSystem.POLAR_DEG)) % 360 > 25) {
					// Start avoiding block
					avoidBlock();
					if (getFrontUSData() < 10) {
						// Check if obstacle in front while avoiding
						leftMotor.stop(true);
						rightMotor.stop(false);
						driver.rotate(-90, CoordinateSystem.POLAR_DEG);
					} 
				}
				
				Sound.beep();
				
				leftMotor.stop(true);
				rightMotor.stop(false);
				leftMotor.setAcceleration(ACCELERATION_SMOOTH);
				rightMotor.setAcceleration(ACCELERATION_SMOOTH);
				
				// After avoidance, travel back to the last destination saved.
				
				driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
				
				setIsAvoiding(false);
			}
		}
	}

	/**
	 * Check if there is an obstacle seen by the front ultrasonic sensor.
	 */
	private void isThereObstacle() {
		double USDistance = getFrontUSData();
		
		if (USDistance > 10) {
			// Not avoiding
			setIsAvoiding(false);
			return;
		} else {
			// Avoiding
			leftMotor.stop(true);
			rightMotor.stop(false);
			
			if (USDistance > 10) {
				USDistance = 10;
			}
			
			// Travel close to the obstacle
			driver.travelDistance(USDistance - 4);
			float[] colorRGB = getColorRGB();
			
			// Check if it is an obstacle or a styrofoam block
			if (colorRGB[1] > colorRGB[0] && colorRGB[1] > colorRGB[2]) {
				driver.rotate(-30, CoordinateSystem.POLAR_DEG);
				driver.travelDistance(8);
				driver.rotate(60, CoordinateSystem.POLAR_DEG);
				
				
				leftMotor.stop(true);
				rightMotor.stop(false);
				leftMotor.setAcceleration(ACCELERATION_SMOOTH);
				rightMotor.setAcceleration(ACCELERATION_SMOOTH);
				
				driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
				
				setIsAvoiding(false);
				return;
			} else {
				// Set avoiding to true
				setIsAvoiding(true);
				return;
			}

		}
	}

	/**
	 * Avoidance of an object as a P controller. It adjusts the speed of the motors according to a linear function of the distance 
	 * of the side ultrasonic sensor.
	 */
	private void avoidBlock() {
		double distance;
		double speed;
		leftMotor.setSpeed(SPEED_AVOIDING_INBETWEEN);
		distance = getSideUSData();
		
		
		//change distance max and min
		if (distance > 30){				//max distance
			speed = SPEED_AVOIDING_MAX;
		} else if (distance < 10){		//min distance
			speed = SPEED_AVOIDING_MIN;
		}else {							//in between
			speed = SPEED_AVOIDING_INBETWEEN;
		}

		rightMotor.setSpeed((float) speed);

		leftMotor.forward();
		rightMotor.forward();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Sets the isAvoiding value.
	 * @param boolValue Boolean value to be set at.
	 */
	public static void setIsAvoiding(boolean boolValue) {
		synchronized (lock) {
			isAvoiding = boolValue;
		}
	}
	/**
	 * Get the isAvoiding boolean.
	 * @return Returns the isAvoiding variable.
	 */
	public static boolean getIsAvoiding() {
		synchronized (lock) {
			return isAvoiding;
		}
	}

	/**
	 * Make the Thread sleep for more than 5 minutes.
	 * @throws InterruptedException
	 */
	public static void stopObsAvoid() throws InterruptedException {
		Thread.sleep(50000000);
	}
	
	
}
