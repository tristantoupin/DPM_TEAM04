package DPM_TEAM04;

import java.util.Arrays;
import java.util.HashMap;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Point2D;
import lejos.robotics.geometry.Rectangle2D;
import lejos.utility.Delay;

/**
 * This class contains all resources (Motors, Sensors, Filters, Display) and all
 * constants required for operation. Resources are initialized once this class
 * is loaded by the JVM
 * 
 * Sensor data is also acquired through this class
 * 
 * @author Kareem Halabi, Alexis Giguere-Joannette, Tristan Toupin
 * @version 3.0
 */
public class Resources {

	// Wifi

	public static final String SERVER_IP = "192.168.2.3";
	public static final int TEAM_NUMBER = 4;
	public static boolean isBuilder;
	public static int startingCorner;
	public static HashMap<String, Integer> wifiData;

	// MAP
	public static final int MAP_DIMENSION = 12; // 12 X 12 tiles

	// Distance Constants
	public static final double TILE_WIDTH = 30.48,
			HALF_TILE_WIDTH = TILE_WIDTH / 2.0, QUARTER_TILE_WIDTH = TILE_WIDTH / 4.0;
	public static final double CS_TO_CENTER = 14.7, US_TO_CENTER = 20.1,
			BUMPER_TO_CENTER = 9.1;
	public static final double TRACK = 11.05, WHEEL_RADIUS = 2.03,
			LEFT_WHEEL_RADIUS = WHEEL_RADIUS,
			RIGHT_WHEEL_RADIUS = WHEEL_RADIUS;

	public static final int ODOMETER_PERIOD = 25; // odometer update period, in
													// ms
	public static final int DISPLAY_PERIOD = 250;
	public static final double BAND_CENTER = 20.0;
	public static final double NAVIGATION_POSITION_BANDWIDTH = 2.5,
			NAVIGATION_HEADING_BANDWIDTH = 0.14;

	// Geometry
	public static Rectangle2D builderZone;
	public static Rectangle2D collectorZone;
	public static Rectangle2D mapWithoutWalls;
	public static Point2D.Double mapCenter;
	public static Point2D.Double searchPoint;
	public static Point2D.Double stackPoint;
	public static Point2D.Double odoCorrectionPoint;

	// Motors
	private static final String LEFT_MOTOR_PORT = "C";
	public static final EV3LargeRegulatedMotor leftMotor;
	private static final String RIGHT_MOTOR_PORT = "D";
	public static final EV3LargeRegulatedMotor rightMotor;
	private static final String GRAB_MOTOR_PORT = "B";
	public static final EV3LargeRegulatedMotor grabMotor;
	private static final String LIFT_MOTOR_PORT = "A";
	public static final EV3LargeRegulatedMotor liftMotor;

	// Motor Constants
	public static final int SPEED_FORWARD = 200, SPEED_TURNING_FAST = 140,
			SPEED_TURNING_SLOW = 60;
	public static final int SPEED_TURNING_MEDIUM = 100, SPEED_SCANNING = 30;

	public static final int SPEED_GRAB = 100, SPEED_LIFT = 300;
	public static final int SPEED_AVOIDING_MIN = 50,
			SPEED_AVOIDING_MAX = 200,
			SPEED_AVOIDING_INBETWEEN = (SPEED_AVOIDING_MIN + SPEED_AVOIDING_MAX) / 2;
	public static final int ACCELERATION_FAST = 4000,
			ACCELERATION_MEDIUM = 700, ACCELERATION_SMOOTH = 400;

	// Ultrasonic sensors
	private static final String US_FRONT_PORT = "S1";
	private static final SampleProvider usFront;
	private static final float[] usDataFront;
	private static final int US_FRONT_NUM_SAMPLES = 15;
	private static final int US_FRONT_SAMPLE_DELAY = 10;
	public static float US_FRONT_CLIP = 100;

	private static final String US_SIDE_PORT = "S3";
	private static final SampleProvider usSide;
	private static final float[] usDataSide;
	private static final int US_SIDE_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;
	private static final int US_SIDE_SAMPLE_DELAY = US_FRONT_SAMPLE_DELAY;
	public static float US_SIDE_CLIP = US_FRONT_CLIP;

	// Color sensors
	private static final String CS_FRONT_PORT = "S2";
	private static final SampleProvider csFront;
	private static final float[] csDataFront;
	private static final int CS_FRONT_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;

	private static final String CS_DOWN_PORT = "S4";
	private static final SampleProvider csDown;
	private static final float[] csDataDown;
	private static final int CS_DOWN_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;

	// Threads
	public static boolean isLocalizing = true;
	public static int towerHeight = 0;
	public static int liftPosition = 0;
	public static boolean isHoldingBlock = false;
	public static boolean isSearching = false;
	public static int searchStep = 0;
	public static int TIME_LEFT = (270 * 1000);
	public static double cornerX, cornerY;

	// LCD
	public static final TextLCD lcd;

	// Dummy variable to force initialization
	public static boolean initialize;

	// Initializes resources
	// ENSURE SENSORS AND MOTORS ARE CORRECTED PROPERLY
	// There is no reasonably easy way to tell if correct
	// sensors are attached to the correct ports
	/**
	 * Initializes resources. Ensure sensors and motors are corrected properly.
	 */
	static {
		// --------------------------Motors-------------------------

		System.out.println("L Motor-" + LEFT_MOTOR_PORT);
		leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(
				LEFT_MOTOR_PORT));

		System.out.println("R Motor-" + RIGHT_MOTOR_PORT);
		rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(
				RIGHT_MOTOR_PORT));

		System.out.println("Grab Motor-" + GRAB_MOTOR_PORT);
		grabMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(
				GRAB_MOTOR_PORT));

		System.out.println("Lift Motor-" + LIFT_MOTOR_PORT);
		liftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(
				LIFT_MOTOR_PORT));

		// ------------------------US Sensors------------------------

		System.out.println("US Front-" + US_FRONT_PORT);
		usFront = (new EV3UltrasonicSensor(LocalEV3.get()
				.getPort(US_FRONT_PORT))).getMode("Distance");
		usDataFront = new float[usFront.sampleSize() * US_FRONT_NUM_SAMPLES];

		System.out.println("US Side-" + US_SIDE_PORT);
		usSide = (new EV3UltrasonicSensor(LocalEV3.get().getPort(US_SIDE_PORT)))
				.getMode("Distance");
		usDataSide = new float[usSide.sampleSize() * US_SIDE_NUM_SAMPLES];

		// -----------------------Color Sensors-----------------------

		System.out.println("CS Front-" + CS_FRONT_PORT);
		csFront = (new EV3ColorSensor(LocalEV3.get().getPort(CS_FRONT_PORT)))
				.getMode("RGB");
		csDataFront = new float[csFront.sampleSize() * CS_FRONT_NUM_SAMPLES];

		System.out.println("CS Down-" + CS_DOWN_PORT);
		csDown = (new EV3ColorSensor(LocalEV3.get().getPort(CS_DOWN_PORT)))
				.getMode("Red");
		csDataDown = new float[csDown.sampleSize() * CS_DOWN_NUM_SAMPLES];

		// ---------------------------LCD---------------------------
		lcd = LocalEV3.get().getTextLCD();

		// lcd.clear() does not work
		// print 8 blank lines
		for (int i = 0; i < 8; i++)
			System.out.println();

		lcd.drawString("Ready", 0, 0);
		Sound.beep();

	}

	/**
	 * Fetch data from the front ultrasonic sensor.
	 * @return Returns the distance in cm.
	 */
	public static float getFrontUSRawData() {
		float[] singleSample = new float[usFront.sampleSize()];
		usFront.fetchSample(singleSample, 0);
		return singleSample[0];
	}

	/**
	 * Fetch data from the front ultrasonic sensor applying a median filter. It fetches a number of samples, sort the array of 
	 * samples and returns the median value of the array.
	 * @return Returns the median value of the array of samples.
	 */
	public static float getFrontUSData() {

		// Populate the array with samples (with a delay between each sample)
		for (int i = 0; i < US_FRONT_NUM_SAMPLES; i++) {
			usFront.fetchSample(usDataFront, i);
			Delay.msDelay(US_FRONT_SAMPLE_DELAY);
		}

		// Sort samples
		Arrays.sort(usDataFront);

		// Acquire median from middle of array and scale up by 100
		// to get value in centimeters
		float median = usDataFront[usDataFront.length / 2] * 100;

		if (median > US_FRONT_CLIP)
			median = US_FRONT_CLIP;

		return median;
	}

	/**
	 * Fetch data from the side ultrasonic sensor.
	 * @return Returns the distance in cm.
	 */
	public static float getSideUSRawData() {
		float[] singleSample = new float[usSide.sampleSize()];
		usSide.fetchSample(singleSample, 0);
		return singleSample[0];
	}

	/**
	 * Fetch data from the side ultrasonic sensor applying a median filter. It fetches a number of samples, sort the array of 
	 * samples and returns the median value of the array.
	 * @return Returns the median value of the array of samples.
	 */
	public static float getSideUSData() {
		// Populate the array with samples (with a delay between each sample)
		for (int i = 0; i < US_SIDE_NUM_SAMPLES; i++) {
			usSide.fetchSample(usDataSide, i);
			Delay.msDelay(US_SIDE_SAMPLE_DELAY);
		}

		// Sort samples
		Arrays.sort(usDataSide);

		// Acquire median from middle of array and scale up by 100
		// to get value in centimeters
		float median = usDataSide[usDataSide.length / 2] * 100;

		if (median > US_SIDE_CLIP)
			median = US_SIDE_CLIP;

		return median;
	}

	/**
	 * Fetch the RGB values of the front color sensor.
	 * @return Return the array of RGB values.
	 */
	public static float[] getColorRGB() {
		// for(int i = 0; i < CS_FRONT_NUM_SAMPLES; i++)
		// csFrontFilter.fetchSample(csDataFront, 0);
		csFront.fetchSample(csDataFront, 0);
		return csDataFront;
	}

	/**
	 * Fetch the red value from the down facing color sensor.
	 * @return Return the red value.
	 */
	public static float getDownCSData() {
		csDown.fetchSample(csDataDown, 0);

		return csDataDown[0];
	}
	
}