package DPM_TEAM04.test;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Audio;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;


/**
 * Test class to see that the two brick connection is working well using several motors and sensors.
 * 
 * @author KareemHalabi
 */
public class BrickComs {

	public static final String MASTER_NAME = "T4_M1";
	public static final String SLAVE_NAME = "T4_S1";

	public static void main(String[] args) {

		try {
			/*
			 * RemoteEV3 allows access to the hardware of either local or remote
			 * EV3s
			 */
			RemoteEV3 master = new RemoteEV3(BrickFinder.find(MASTER_NAME)[0].getIPAddress());

			final TextLCD masterLCD = LocalEV3.get().getTextLCD(); // master.getTextLCD()
																	// Does not
																	// work
			masterLCD.clear();
			masterLCD.drawString("M_Brick", 0, 0);

			RemoteEV3 slave = new RemoteEV3(BrickFinder.find(SLAVE_NAME)[0].getIPAddress());
			final TextLCD slaveLCD = slave.getTextLCD();
			slaveLCD.clear();

			final int LCD_OFFSET = 3; // the first three lines on the slave
										// brick are reserved for battery,
										// IP address and a blank separator
			slaveLCD.drawString("S_Brick", 0, 0 + LCD_OFFSET);

			/*
			 * Quick info on Remote Method Invocation (RMI): Basically it's a
			 * system provided by Java that allows an application to call
			 * methods on object that may be located on a remote JVM. To the
			 * programmer, the details of communications are handled internally
			 * and the remote objects behave as if they were running locally.
			 * 
			 * RMIRegulatedMotors technically run on the brick they are
			 * connected to, but all are accessible from the master brick
			 */

			Queue<Remote> closeables = new LinkedList<>();

			final RMIRegulatedMotor masterMA = master.createRegulatedMotor("A",
					'L'); // EV3 motor
			closeables.add(masterMA);
			final RMIRegulatedMotor masterMB = master.createRegulatedMotor("B",
					'N'); // NXT motor
			closeables.add(masterMB);
			final RMIRegulatedMotor masterMC = master.createRegulatedMotor("C",
					'M'); // EV3 medium motor
			closeables.add(masterMC);

			masterLCD.drawString("M_Motors", 0, 1);

			final RMIRegulatedMotor slaveMA = slave.createRegulatedMotor("A",
					'L'); // EV3 motor
			closeables.add(slaveMA);
			final RMIRegulatedMotor slaveMB = slave.createRegulatedMotor("B",
					'N'); // NXT motor
			closeables.add(slaveMB);
			final RMIRegulatedMotor slaveMC = slave.createRegulatedMotor("C",
					'N'); // NXT motor
			closeables.add(slaveMC);

			slaveLCD.drawString("S_Motors", 0, 1 + LCD_OFFSET);

			Audio masterAudio = master.getAudio();
			Audio slaveAudio = slave.getAudio();

			masterMA.rotate(360, true);
			slaveMA.rotate(360);

			masterAudio.systemSound(Audio.BEEP);
			masterLCD.drawString("M_Audio", 0, 2);

			Delay.msDelay(1000);

			slaveAudio.systemSound(Audio.DOUBLE_BEEP);
			slaveLCD.drawString("S_Audio", 0, 2 + LCD_OFFSET);

			/*
			 * RMISampleProviders also run on the brick they are connected to
			 * but are all accessible from the master brick
			 */

			final RMISampleProvider masterColor = master.createSampleProvider(
					"S1", "lejos.hardware.sensor.EV3ColorSensor", "Red");
			closeables.add(masterColor);
			masterLCD.drawString("M_Color", 0, 3);

			final RMISampleProvider masterUS = master.createSampleProvider(
					"S4", "lejos.hardware.sensor.EV3UltrasonicSensor",
					"Distance");
			closeables.add(masterUS);
			masterLCD.drawString("M_US", 0, 4);

			final RMISampleProvider masterTouch = master.createSampleProvider(
					"S2", "lejos.hardware.sensor.EV3TouchSensor", "Touch");
			closeables.add(masterTouch);

			final RMISampleProvider slaveColor = slave.createSampleProvider(
					"S1", "lejos.hardware.sensor.EV3ColorSensor", "Red");
			closeables.add(slaveColor);
			slaveLCD.drawString("S_Color", 0, 3 + LCD_OFFSET);

			final RMISampleProvider slaveUS = slave.createSampleProvider("S4",
					"lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
			closeables.add(slaveUS);
			slaveLCD.drawString("S_US", 0, 4 + LCD_OFFSET);

			final RMISampleProvider slaveTouch = slave.createSampleProvider(
					"S2", "lejos.hardware.sensor.EV3TouchSensor", "Touch");
			closeables.add(slaveTouch);

			/*
			 * Changes speed of master & slave motor A using color sensor data
			 */
			final Thread colorThread = new Thread() {

				@Override
				public void run() {
					try {
						masterMA.forward();
						slaveMA.forward();
						while (!this.isInterrupted()) {
							masterLCD.clear(0);
							masterLCD.drawInt(
									(int) masterColor.fetchSample()[0], 0, 0);
							masterMA.setSpeed((int) (200 * masterColor
									.fetchSample()[0] + 10));
							slaveLCD.clear(0 + LCD_OFFSET);
							slaveLCD.drawInt((int) slaveColor.fetchSample()[0],
									0, 0 + LCD_OFFSET);
							slaveMA.setSpeed((int) (200 * slaveColor
									.fetchSample()[0]) + 10);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			};

			/*
			 * Changes speed of master & slave motor B using US sensor data
			 */
			final Thread USThread = new Thread() {
				@Override
				public void run() {
					try {
						masterMB.forward();
						slaveMB.forward();
						while (!this.isInterrupted()) {
							masterLCD.clear(1);
							float masterSample = masterUS.fetchSample()[0];
							masterLCD.drawInt((int) (masterSample * 100), 0, 1);
							if (masterSample > 1)
								masterSample = 1;
							masterMB.setSpeed((int) (masterSample * 100));

							slaveLCD.clear(1 + LCD_OFFSET);
							float slaveSample = slaveUS.fetchSample()[0];
							slaveLCD.drawInt((int) (slaveSample * 100), 0,
									1 + LCD_OFFSET);
							if (slaveSample > 1)
								slaveSample = 1;
							slaveMB.setSpeed((int) (slaveSample * 100));
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			};

			/*
			 * Reverses master & slave motor C based on touch
			 */
			final Thread touchThread = new Thread() {
				@Override
				public void run() {
					try {
						while (!this.isInterrupted()) {
							if (masterTouch.fetchSample()[0] == 1)
								masterMC.backward();
							else
								masterMC.forward();

							if (slaveTouch.fetchSample()[0] == 1)
								slaveMC.backward();
							else
								slaveMC.forward();
						}

					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			};

			masterLCD.clear();
			slaveLCD.clear();

			colorThread.start();
			USThread.start();
			touchThread.start();

			Keys masterBtns = master.getKeys();

			// Wait to terminate
			masterBtns.waitForAnyPress();

			// Stop all threads
			colorThread.interrupt();
			USThread.interrupt();
			touchThread.interrupt();
			try { // wait for threads to close safely
				colorThread.join();
				USThread.join();
				touchThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*
			 * It is now SUPER IMPORTANT that RMI resources are closed before
			 * exiting the application. As I mentioned before, RMI objects
			 * technically run on the device they are connected to if the master
			 * program exits, the slave RMI objects are still active, blocking
			 * the ports from being opened the next time the program runs
			 * 
			 * If the ports do get blocked, on the Lejos menu go to System then
			 * click Reset
			 */

			for (Remote closeable : closeables) { // remote is a common
													// superclass of
													// RMIRegulatedMotor and
													// RMISampleProvider
				if (closeable instanceof RMIRegulatedMotor)
					((RMIRegulatedMotor) closeable).close();

				else if (closeable instanceof RMISampleProvider)
					((RMISampleProvider) closeable).close();
			}

		} catch (IOException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
