package DPM_TEAM04.logging;

import static DPM_TEAM04.Resources.lcd;

import java.util.ArrayList;

/**
 * Displays numerical information on the EV3 LCD for diagnotistics purposes
 * 
 * @author KareemHalabi
 * @version 1.0
 */
public class LCDLogger extends Thread {

	private int refreshPeriod;
	private int decimalPlaces;
	private ArrayList<DataEntryProvider> dataProviders;

	public LCDLogger(int refreshPeriod, int decimalPlaces) {
		this.refreshPeriod = refreshPeriod;
		this.decimalPlaces = decimalPlaces;
		this.dataProviders = new ArrayList<>();
	}

	public LCDLogger(int refreshPeriod, int decimalPlaces,
			DataEntryProvider... inputDataProviders) {
		this.refreshPeriod = refreshPeriod;
		this.decimalPlaces = decimalPlaces;
		this.dataProviders = new ArrayList<>();
		addToEntryProviders(inputDataProviders);
	}

	/**
	 * Adds one or many entry providers to the display. Only occurs at the end
	 * of a display cycle
	 * 
	 * @param inputDataProviders
	 *            the entry provider(s) to add
	 */
	public void addToEntryProviders(DataEntryProvider... inputDataProviders) {
		synchronized (dataProviders) {
			for (DataEntryProvider data : inputDataProviders)
				dataProviders.add(data);
		}
	}

	@Override
	public void run() {
		lcd.clear();

		while (!this.isInterrupted()) {

			long start = System.currentTimeMillis();

			// We don't want entry providers to be added while they are being
			// displayed
			synchronized (dataProviders) {

				// Clear each line then redraw heading and data
				for (int i = 0; i < dataProviders.size(); i++) {
					DataEntryProvider provider = dataProviders.get(i);
					lcd.clear(i);
					lcd.drawString(
							provider.HEADING
									+ ": "
									+ formattedDoubleToString(
											provider.getEntry(), decimalPlaces),
							0, i);
				}
			}

			// sleep for remaining display period
			long end = System.currentTimeMillis();
			if ((end - start) < refreshPeriod) {
				try {
					Thread.sleep(refreshPeriod - (end - start));
				} catch (InterruptedException e) { // if interrupted, exit
					break;
				}
			}
		}
	}

	/**
	 * Converts a double into a formatted string with desired number of decimal
	 * places. This method is more suitable than printing the raw double as this
	 * can overflow a line on the EV3 LCD.
	 * 
	 * @param x
	 *            the double to convert
	 * @param places
	 *            the number of decimal places to truncate {@code x} to
	 * @return a String representing {@code x} with {@code places} number of
	 *         decimal places
	 */
	public static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;

		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";

		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long) x;
			if (t < 0)
				t = -t;

			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}

			result += stack;
		}

		// put the decimal, if needed
		if (places > 0) {
			result += ".";

			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long) x);
			}
		}

		return result;
	}

}
