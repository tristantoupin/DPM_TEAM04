package DPM_TEAM04.test;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Test class to use lifter motor and grabber motor.
 * Used to find angle needed to rotate for achieving the block stack.
 * 
 * @author Quentin Norris 
 *
 */
public class CollectingGrabbingTest {

	private static final EV3LargeRegulatedMotor centerMotor = new EV3LargeRegulatedMotor(
			LocalEV3.get().getPort("A"));

	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated

		final TextLCD t = LocalEV3.get().getTextLCD();

		ExitThreadForCollectingGrabbingTest exit = new ExitThreadForCollectingGrabbingTest();
		exit.start();

		do {
			// clear the display
			t.clear();

			// ask the user whether the motor should turn forward or backward
			t.drawString("Rotate | Rotate ", 0, 1);
			t.drawString("forward|backward", 0, 0);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		centerMotor.setSpeed(30);
		while (buttonChoice == Button.ID_LEFT
				|| buttonChoice == Button.ID_RIGHT) {
			if (buttonChoice == Button.ID_LEFT) {
				centerMotor.rotate(190); // turn the motor forward 190 degrees
			} else {
				centerMotor.rotate(-190); // turn the motor backward 190 degrees
			}
			buttonChoice = Button.waitForAnyPress();
		}

	}

}
