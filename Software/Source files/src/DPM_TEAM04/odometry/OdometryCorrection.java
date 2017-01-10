package DPM_TEAM04.odometry;

import java.util.LinkedList;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.navigation.Driver;
import static DPM_TEAM04.Resources.*;
import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;


/**
 * 
 * @author Tristan Toupin & Ben Willms
 *
 */
public class OdometryCorrection {
	
	private static final long CORRECTION_PERIOD = 50;
	//private static final int ROTATION_SPEED = 100;
	private static final double CS_DISTANCE = 15.8; //in cm
	private static final double CS_ANGLE = 34.0*Math.PI/180.0; //in rads
	
	private double currentSampleDifference;
	private double lastSample, currentSample, sampleAngle;
	private boolean firstTime;
	private int lineCounter;
	private LinkedList<AngleCSDataPair> samples;
	private LinkedList<Double> angleList = new LinkedList<Double>();		// 0 and 2 are x angles and 1 and 3 are y angles
	private AngleCSDataPair currentPair;
	private Driver driver = Driver.getDriver();
	
	public boolean isFacingStart;
	
	//for plotting
	private double errorY = 0.0, errorX = 0.0, currentAngle = 0.0, correctedAngle = 0.0;
	
	private DirectedCoordinate position;
	
	public OdometryCorrection() {
		
		// Initialize variables and the list of sample pairs
		this.lineCounter = 0;
		this.firstTime = true;
		this.samples = new LinkedList<AngleCSDataPair>();
		this.position = Odometer.getOdometer().getPosition();
	}
	
	
	/**
	 * Will perform the odometry correction properly if the light sensor is 
	 * place in the bottom left quadrant of a known black cross.
	 */
	public void doCorrection() {
		
		// drive to location the top right corner of a free square,
		// rotate to 270 degrees, and start doing a 360 degree turn (counter-clockwise),
		// clock all the gridlines, and
		// do trig to compute position and heading
		
		currentSampleDifference = 0.0;
		lastSample = 0.0;
		currentSample = 0.0; 
		sampleAngle = 0.0;
		firstTime= true;
		lineCounter = 0;
		samples.clear();
		angleList.clear();
		currentPair = new AngleCSDataPair();
		
		this.firstTime = true;
		long correctionStart, correctionEnd;
		AngleCSDataPair maxPair, minPair;
		double bottomYLineOrientation = 0.0, firstXLineOrientation = 0.0;
		
		while (true) {
			correctionStart = System.currentTimeMillis();
			
			currentSample = getDownCSData()*1000;
			sampleAngle = position.getDirection(CoordinateSystem.POLAR_RAD);
			//sampleAngle = (sampleAngle-(30.0*2.0*Math.PI/360.0))%(2.0*Math.PI);
			
			// Fetch the data from the color sensor
			if (firstTime) {
				// If it's the first data fetched, set the last sample as the same as the first
				lastSample = currentSample;
				firstTime = false;
			}
			
			// Calculate the difference between the last color value and the current one
			currentSampleDifference = lastSample - currentSample;
			lastSample = currentSample;
			currentPair = new AngleCSDataPair(currentSampleDifference, sampleAngle);
			// We use an list to store the last 20 sample pairs to be able to
			// determine if a line has been detected.
			
			// Add the current sample difference to the list (at the end)
			samples.addLast(currentPair);
			if (samples.size() > 20) {
				// If the list contains more than 20 sample pairs, remove the oldest one (at the beginning)
				samples.removeFirst();
			} else if(samples.size() <= 20) {
				continue;
			}
			
			// WHEN A LINE IS DETECTED
			// Numbers were found by experimentation to detect the line
			maxPair = getMaxSamplePair(samples);
			minPair = getMinSamplePair(samples);

			if (maxPair.getcsDifference() >= 70 && minPair.getcsDifference() <= -30) {
				
				// Make EV3 beep each time a line is seen
				Audio audio = LocalEV3.get().getAudio();
			    audio.systemSound(0);
				
				// Once a line is detected, clear the list as we don't want to detect the same line twice.
				samples.clear();
				
				// When a line is passed, store the angle calculated by the odometer in an array
				this.lineCounter++;
				this.angleList.add((maxPair.getAngle() + angleBetween(maxPair.getAngle(), minPair.getAngle()))%(2.0*Math.PI));
				if(lineCounter == 1) {
					bottomYLineOrientation = (maxPair.getAngle() + angleBetween(maxPair.getAngle(), minPair.getAngle()))%(2.0*Math.PI); //Keep track of the robot's orientation when detecting the first y line
				} else if(lineCounter == 2) {
					firstXLineOrientation = (maxPair.getAngle() + angleBetween(maxPair.getAngle(), minPair.getAngle()))%(2.0*Math.PI); //Keep track of the robot's orientation when detecting the first x line
				}
			}
			
			if(!leftMotor.isMoving() && !rightMotor.isMoving()) {
				break;	//The 360 degree turn is finished
			}

			// this ensures the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
		
		// After the while loop stops, compute trigonometry to correct it's position and heading
		if(this.lineCounter > 4 || this.lineCounter < 4) {
			//DO NOTHINGGGGG!!!!
			Audio audio = LocalEV3.get().getAudio();
		    audio.systemSound(2);
		} else {
			// Initialize variables
			double halfAngleBetweenX, halfAngleBetweenY, xPosition, yPosition, deltaAngleY, deltaAngleX, averageDeltaAngle, thetaCorrected;
			
			// Determine angle between x and y angles that have been stored
			halfAngleBetweenY = (1.0/2.0)*(angleBetween(angleList.get(0), angleList.get(2)));
			halfAngleBetweenX = (1.0/2.0)*(angleBetween(angleList.get(1), angleList.get(3)));
			
			// Compute the corrected x and y positions with the half angle between and color sensor distance
			xPosition = (-1.0)*CS_DISTANCE*Math.cos(halfAngleBetweenY);
			yPosition = (-1.0)*CS_DISTANCE*Math.cos(halfAngleBetweenX);
			
			bottomYLineOrientation -= CS_ANGLE;
			firstXLineOrientation -= CS_ANGLE;
			if(firstXLineOrientation < 0.0) {
				firstXLineOrientation += 2.0*Math.PI;
			}
			if(bottomYLineOrientation < 0.0) {
				bottomYLineOrientation += 2.0*Math.PI;
			}
			// Compute the error between the true orientation, and the odometer's orientation
			deltaAngleY = (2.0)*Math.PI - ((bottomYLineOrientation)%(2.0*Math.PI))- halfAngleBetweenY;
			deltaAngleX = (1.0/2.0)*Math.PI - ((firstXLineOrientation)%(2.0*Math.PI))- halfAngleBetweenX;
		
			if(deltaAngleX < -1) {
				deltaAngleX += 2.0*Math.PI;
			}
			
			errorY = deltaAngleY;
			errorX = deltaAngleX;
			averageDeltaAngle = (deltaAngleY + deltaAngleX)/(2.0);
			
			//get the theta that the robot actually stopped at.
			double currentAngle = position.getDirection(CoordinateSystem.POLAR_RAD);
			
			this.currentAngle = currentAngle;
			
			//compute the current true orientation
			thetaCorrected = (currentAngle + averageDeltaAngle)%(2.0*Math.PI);
			
			correctedAngle = thetaCorrected;
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// there is nothing to be done here because it is not
				// expected that the odometry correction will be
				// interrupted by another thread
			}
			
			//update the odometer
			position.setDirection(thetaCorrected, CoordinateSystem.POLAR_RAD);
			position.setX(xPosition + cornerX); 
			position.setY(yPosition + cornerY);	

			return;
		}
	}
	/**
	 * Will place the robot in a decent position if it is place +/-10 cm inside the bottom left tile of odometry correction corner.
	 * The angle must have a maximum error of 15˚.
	 */
	
public void prepareCorrection() {
		
		long correctionStart, correctionEnd;
		AngleCSDataPair maxPair, minPair;
		
		
		
		leftMotor.stop(true);
		rightMotor.stop(false);
		
		
		Object lock2 = new Object();
		synchronized (lock2) {
			leftMotor.setSpeed(SPEED_FORWARD);
			rightMotor.setSpeed(SPEED_FORWARD);
			
			leftMotor.forward();
			rightMotor.forward();
		}
		
		
		
		while (true) {
			correctionStart = System.currentTimeMillis();
			
			currentSample = getDownCSData()*1000;
			sampleAngle = position.getDirection(CoordinateSystem.POLAR_RAD);
			//sampleAngle = (sampleAngle-(30.0*2.0*Math.PI/360.0))%(2.0*Math.PI);
			
			// Fetch the data from the color sensor
			if (firstTime) {
				// If it's the first data fetched, set the last sample as the same as the first
				lastSample = currentSample;
				firstTime = false;
			}
			
			// Calculate the difference between the last color value and the current one
			currentSampleDifference = lastSample - currentSample;
			lastSample = currentSample;
			currentPair = new AngleCSDataPair(currentSampleDifference, sampleAngle);
			// We use an list to store the last 20 sample pairs to be able to
			// determine if a line has been detected.
			
			// Add the current sample difference to the list (at the end)
			samples.addLast(currentPair);
			if (samples.size() > 20) {
				// If the list contains more than 20 sample pairs, remove the oldest one (at the beginning)
				samples.removeFirst();
			} else if(samples.size() <= 20) {
				continue;
			}
			
			
			
			// WHEN A LINE IS DETECTED
			// Numbers were found by experimentation to detect the line
			maxPair = getMaxSamplePair(samples);
			minPair = getMinSamplePair(samples);

			
			if (maxPair.getcsDifference() >= 70 && minPair.getcsDifference() <= -30) {
				
				// Make EV3 beep each time a line is seen
				Audio audio = LocalEV3.get().getAudio();
			    audio.systemSound(0);
				
				// Once a line is detected, clear the list as we don't want to detect the same line twice.
				samples.clear();
				driver.travelDistance(7);
				return;				
			}
		
			// this ensures the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	
	
	/**
	 * Will return the angle equal to the half of the difference between @angleOne and @angelTwo.
	 * @param angleOne
	 * @param angleTwo
	 * @return
	 */
	private Double angleBetween(double angleOne, double angleTwo) {
		double angleBetween;
		
		if (angleOne > angleTwo) {	//This happens in the case where angleTwo overlaps to around 360.
			angleOne = (2.0*Math.PI) - angleOne;	//The true positive angle from the original 0 point is 360-angleOne.
			angleBetween = angleOne + angleTwo;	//The total angle between the orientations in this case is the sum of the corrected angleOne, and angleTwo.
		} else if (angleTwo > angleOne) {	// angleOne and angleTwo are positive, with Two > One.
			angleBetween = angleTwo - angleOne;	//The total angle between the walls is the difference between angleOne and angleTwo.
		} else {
			angleBetween = 0;	//It should never enter this else, as the two angles can't be the same without a bug.
		}
		
		return angleBetween;		// return the angle between the two inputs
		
	}
	
	public double getErrorY() {
		return this.errorY;
	}
	
	public double getErrorX() {
		return this.errorX;
	}
	
	public double getCurrentAngle() {
		return this.currentAngle;
	}
	
	public double getCorrectedAngle() {
		return this.correctedAngle;
	}
	
	public double getCurrentSampleDifference() {
		return this.currentSampleDifference;
	}
	
	
	/**
	 * 
	 * Loops through the @samples to find the pair with the maximum CS value
	 * It sets the max if the value is greater than the actual max
	 * 
	 * @param samples
	 * @return
	 */
	public AngleCSDataPair getMaxSamplePair(LinkedList<AngleCSDataPair> samples) {
		
		double max = 0.0;
		AngleCSDataPair maxPair = new AngleCSDataPair();
		AngleCSDataPair curr = new AngleCSDataPair();
		if (!samples.isEmpty()) {
			curr = samples.get(0);
			max = curr.getcsDifference();
			maxPair = curr;
			for (int i=1; i<samples.size(); i++) {
				curr = samples.get(i);
				if(curr.getcsDifference() > max){
		            max = curr.getcsDifference();
		            maxPair = curr;
		        }
			}
		}
		return maxPair;
		
	}
	
	
	/**
	 * 
	 * Loops through the @samples to find the pair with the minimum CS value
	 * It sets the max if the value is greater than the actual max
	 * 
	 * @param samples
	 * @return
	 */
	// Loops through the list to find the pair with the minimum CS value
	public AngleCSDataPair getMinSamplePair(LinkedList<AngleCSDataPair> samples) {
		
		double min = 0.0;
		AngleCSDataPair minPair = new AngleCSDataPair();
		AngleCSDataPair curr = new AngleCSDataPair();
		if (!samples.isEmpty()) {
			curr = samples.get(0);
			min = curr.getcsDifference();
			minPair = curr;
			for (int i=1; i<samples.size(); i++) {
				curr = samples.get(i);
				if(curr.getcsDifference() < min){
		            min = curr.getcsDifference();
		            minPair = curr;
		        }
			}
		}
		return minPair;
	}
	
	
	/**
	 * Object class of a light sensor data paired with an angle.
	 * 
	 * @author Ben Willms
	 *
	 */
	public class AngleCSDataPair {
		
		double csDifference, angle;
		
		AngleCSDataPair() {
			this.csDifference = 0;
			this.angle = 0;
		}
		
		AngleCSDataPair(double csDifference, double angle) {
			this.csDifference = csDifference;
			this.angle = angle;
		}
		
		public double getcsDifference() {
			return this.csDifference;
		}
		
		public double getAngle() {
			return this.angle;
		}
	}
}