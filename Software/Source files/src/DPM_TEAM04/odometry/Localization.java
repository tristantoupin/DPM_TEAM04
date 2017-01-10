package DPM_TEAM04.odometry;

import static DPM_TEAM04.Resources.BUMPER_TO_CENTER;
import static DPM_TEAM04.Resources.MAP_DIMENSION;
import static DPM_TEAM04.Resources.TILE_WIDTH;
import static DPM_TEAM04.Resources.getSideUSData;
import static DPM_TEAM04.Resources.isLocalizing;
import static DPM_TEAM04.Resources.leftMotor;
import static DPM_TEAM04.Resources.rightMotor;
import static DPM_TEAM04.Resources.startingCorner;

import java.util.ArrayList;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.navigation.Driver;
import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Delay;

/**
 * This class localizes the robot using the ultrasonic sensor.
 * 
 * It is started by the Main class and extends Thread. It scans its environment
 * and bumps into the closest wall (the minimal distance seen). It after bumps
 * into the other wall and sets the x, y and theta values of the odometer.
 * 
 * @author Alexis Giguere-Joannette & Tristan Saumure-Toupin
 * @version 1.0
 */
public class Localization extends Thread {

	private double minDistance, minDistAngle;
	private ArrayList<Distance> listOfDistances;
	private boolean isLeftWall;

	private DirectedCoordinate position;

	public Localization() {
		listOfDistances = new ArrayList<Distance>();
	}

	/**
	 * Code executed when the Thread is started.
	 */
	public void run() {

		position = Odometer.getOdometer().getPosition();
		Driver driver = Driver.getDriver();

		this.minDistance = Resources.getFrontUSData();
		this.minDistAngle = Odometer.getOdometer().getPosition()
				.getDirection(CoordinateSystem.POLAR_RAD);

		// Rotate 360 degrees to "scan"
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);

		// wait a little to get motors started
		Delay.msDelay(100);

		// while the motors are moving (the robot is turning), save the distance
		// seen and at which angle
		while (leftMotor.isMoving() && rightMotor.isMoving()) {
			saveDistance();
		}

		// set the minimal distance
		getMinimalDistance();

		// get the minimal distance and add 180 degrees as the bumper is on the
		// back of the robot
		// turn to the minimal distance (+180 deg) and bump into the wall
		this.minDistAngle = (this.minDistAngle + Math.PI) % (2.0 * Math.PI);
		driver.turnTo(this.minDistAngle, CoordinateSystem.POLAR_RAD);
		driver.travelDistance(-(this.minDistance - Resources.BUMPER_TO_CENTER
				+ Resources.US_TO_CENTER + 12.0));

		// Once bumped into the wall, check the side sensor to see if there is a
		// wall on its left.
		if (getSideUSData() < TILE_WIDTH) {
			isLeftWall = false;
		} else {
			isLeftWall = true;
		}

		// Correct the odometry from the first bump
		setNewCoordinates();

		// Once the odometry is corrected, move from the wall and rotate to bump
		// into the other wall (according to its position)
		if (isLeftWall) {
			driver.travelDistance(6.0);
			driver.rotate(Math.PI / 2.0, CoordinateSystem.POLAR_RAD);
		} else {
			driver.travelDistance(6.0);
			driver.rotate(-Math.PI / 2.0, CoordinateSystem.POLAR_RAD);
		}

		// Go to the next wall and bump into it.
		driver.travelDistance(-(this.minDistance - Resources.BUMPER_TO_CENTER
				+ Resources.US_TO_CENTER + 12.0));

		// Reset the boolean to set the other coordinates
		if (isLeftWall) {
			isLeftWall = false;
		} else {
			isLeftWall = true;
		}

		// Correct the odometry
		setNewCoordinates();

		/*
		 * 
		 * END OF LOCALIZATION
		 */

		// Make EV3 beep when it localized
		Audio audio = LocalEV3.get().getAudio();
		audio.systemSound(0);

		// Move away from the wall
		driver.travelDistance(10.0);

		isLocalizing = false;

	}

	/*
	 * |---------------| 
	 * | 4 + + + + + 3 | 
	 * | + + + + + + + | 
	 * | + + + + + + + | ^y 
	 * | + + + + + + + | | 
	 * | + + + + + + + | | 
	 * | 1 + + + + + 2 | |______>x
	 * |---------------|
	 */
	
	/**
	 * This method corrects the coordinates (x,y and heading) of the robot
	 * depending on its stating corner and on the wall it has bumped into.
	 */
	private void setNewCoordinates() {

		// Correct the odometry according to its starting corner and the wall it
		// has bumped into.
		if (isLeftWall) {
			if (startingCorner == 1) {
				position.setX(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(0.0, CoordinateSystem.POLAR_RAD);
			} else if (startingCorner == 2) {
				position.setY(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection((Math.PI / 2), CoordinateSystem.POLAR_RAD);

			} else if (startingCorner == 3) {
				position.setX((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((Math.PI), CoordinateSystem.POLAR_RAD);

			} else {
				position.setY((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((3 * Math.PI / 2),
						CoordinateSystem.POLAR_RAD);

			}
		} else { // right wall
			if (startingCorner == 1) {
				position.setY(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(Math.PI / 2, CoordinateSystem.POLAR_RAD);
			} else if (startingCorner == 2) {
				position.setX((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((Math.PI), CoordinateSystem.POLAR_RAD);

			} else if (startingCorner == 3) {
				position.setY((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((3 * Math.PI / 2),
						CoordinateSystem.POLAR_RAD);

			} else {
				position.setX(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(0.0, CoordinateSystem.POLAR_RAD);

			}
		}

	}

	/**
	 * Saves the distance seen by the ultrasonic sensor and the angle at which
	 * the distance has been seen. It saves a Distance object into an ArrayList.
	 */
	private void saveDistance() {

		float actualDist = Resources.getFrontUSData();
		if (actualDist > 1) {
			Distance d = new Distance(actualDist,
					position.getDirection(CoordinateSystem.POLAR_RAD));
			this.listOfDistances.add(d);
			System.out.println("\n\n\n\n\n" + actualDist);
		}

	}

	/**
	 * This method is called to set the minimal distance and its angle.
	 * 
	 * @return Returns the index of the minimal distance in the ArrayList of
	 *         distances.
	 */
	private void getMinimalDistance() {

		// Initialize the first value
		this.minDistance = this.listOfDistances.get(0).getDistance();
		this.minDistAngle = this.listOfDistances.get(0).getAngle();

		// Search all the arraylist
		for (int i = 1; i < this.listOfDistances.size(); i++) {
			if (this.listOfDistances.get(i).getDistance() < this.minDistance) {

				// If the distance searched in the arraylist is smaller than the
				// previous minimum
				// save it as the minimal distance

				this.minDistance = this.listOfDistances.get(i).getDistance();
				this.minDistAngle = this.listOfDistances.get(i).getAngle();

			}
		}

	}
	
	public class Distance {

		private double distance, angle;

		public Distance(double distance, double angle) {
			this.distance = distance;
			this.angle = angle;
		}

		public double getDistance() {
			return this.distance;
		}

		public double getAngle() {
			return this.angle;
		}

	}

}