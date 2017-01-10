package DPM_TEAM04.test;

import lejos.hardware.Button;

/**
 * Thread waiting for any press. Use to stop the motors in case of unexpected event.
 * 
 * @author Team04
 *
 */
public class ExitThreadForCollectingGrabbingTest extends Thread {

	public ExitThreadForCollectingGrabbingTest() {

	}

	public void run() {
		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
