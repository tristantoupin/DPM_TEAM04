package DPM_TEAM04.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for saving various data to a CSV file
 * 
 * @author KareemHalabi
 *
 */
public class FileLogger extends Thread {

	// Member variables
	private String fileName;
	private int refreshPeriod;

	private DataEntryProvider[] dataProviders;

	/**
	 * Creates a new FileLogger
	 * 
	 * @param fileName
	 *            name of log file, should include .csv extension
	 * @param refreshPeriod
	 *            minimum refresh period in ms
	 * @param dataEntryProviders
	 *            the entry providers to poll
	 */
	public FileLogger(String fileName, int refreshPeriod,
			DataEntryProvider... dataEntryProviders) {
		this.fileName = fileName;
		this.refreshPeriod = refreshPeriod;
		this.dataProviders = dataEntryProviders;
	}

	@Override
	public void run() {

		File file = new File(fileName);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			writeHeadings(writer);

			// Repeat until closed
			while (!this.isInterrupted()) {
				long start = System.currentTimeMillis();
				String dataLine = "";

				for (DataEntryProvider data : dataProviders)
					dataLine += data.getEntry() + ", ";

				writer.write(dataLine + "\n");

				long end = System.currentTimeMillis();

				if (refreshPeriod - (end - start) > 0) // ensure minimum delay
														// is met, otherwise
														// sleeps for remainder
					Thread.sleep(refreshPeriod - (end - start));
			}

			writer.close();
		} catch (InterruptedException e) { // close file if interrupted
			try {
				writer.close();
			} catch (IOException e1) {
			}
		} catch (IOException e) {
			System.out.println("Error Writing file");
		}
	}

	/**
	 * Writes headings at the top of the CSV file
	 * 
	 * @param writer
	 *            the BufferedWriter that is writing to the file
	 * @throws IOException
	 */
	public void writeHeadings(BufferedWriter writer) throws IOException {
		String heading = "";
		for (DataEntryProvider data : dataProviders)
			heading += data.HEADING + ", ";

		writer.write(heading + "\n");
	}
}
