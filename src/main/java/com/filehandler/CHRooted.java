package com.filehandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Make CHRooted or isolated environment to execute API calls with user defined
 * rules.
 * 
 * Custom rules can be defined in abstract functions:
 * {@code validateFile}: Check if following file can be accessed in the CHRooted
 * environment.
 */
public abstract class CHRooted {

	/**
	 * CHRoot or parent of the restricted directory where API calls must be limited
	 * to.
	 * 
	 * @see setCHRoot
	 */
	private String CHRoot = new File(System.getProperty("user.home"), "filehandler_chroot").getAbsolutePath();

	/**
	 * File to store logs of the critical requests made.
	 */
	private File criticalLogs;

	/**
	 * File to store logs of the requests made.
	 */
	private File logs;

	/**
	 * Writes log to the subclass specific file, with timestamp.
	 * 
	 * @param log_data Data to be logged, after timestamp.
	 */
	public void log(String log_data) {
		try {
			FileWriter logsW = new FileWriter(logs, true);
			logsW.write(LocalDateTime.now().toString() + ": " + log_data + "\n");
			logsW.close();
		} catch (IOException e) {
			System.out.println("Error writing log file.");
			System.out.println(log_data);
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Writes critical log to the subclass specific file, with timestamp.
	 * 
	 * @param log_data Request information to be logged, after timestamp.
	 */
	protected void criticalLog(String log_data) {
		try {
			FileWriter criticalLogsWriter = new FileWriter(criticalLogs, true);
			criticalLogsWriter.write(LocalDateTime.now().toString() + ": " + log_data + "\n");
			criticalLogsWriter.close();
			System.out.println("FileStat: Critical request made.");
			System.out.println(log_data);
		} catch (IOException e) {
			System.out.println("Error writing log file.");
			System.out.println(log_data);
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Custom CHRoot variables to be set by the extended class.
	 */
	public abstract void changeCHRoot();

	/**
	 * Get 'CHRoot'.
	 * 
	 * @return 'CHRoot'
	 * @see CHRoot
	 */
	public String getCHRoot() {
		return CHRoot;
	}

	/**
	 * Set CHRoot as 'path' after checking if the following directory exist and API
	 * caller can access it.
	 * 
	 * @param path Absolute path for the directory to be set as CHRoot
	 * @return 'true', if CHRoot is set along with the files and deletes, 'false',
	 *         otherwise.
	 * @see CHRoot
	 */
	public boolean setCHRoot(String path) {
		File chroot = new File(path);
		try {
			if (chroot.isFile()) {
				System.out.println("CHRoot API Error: (setCHRoot) File found at given path (directory required).");
			} else {
				if (!chroot.isDirectory() && !chroot.mkdirs()) {
					System.out.println("CHRoot API Error: No directory '" + chroot.getCanonicalPath()
							+ "' found, or can't create or access new directory.");
					return false;
				}
				CHRoot = chroot.getCanonicalPath();
				logs = new File(CHRoot, "logs.txt");
				criticalLogs = new File(CHRoot, "critical-logs.txt");
				logs.createNewFile();
				criticalLogs.createNewFile();
				log("");
				log("Chroot set to '" + CHRoot + "'.");
				log("Writing logs to '" + logs.getAbsolutePath() + "'.");
				log("Writing critical logs to '" + criticalLogs.getAbsolutePath() + "'.");
				changeCHRoot();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("FileHandler API Error: Can't change chroot due to internal error.");
		}
		return false;
	}

	/**
	 * The custom function to validate the file in the request, to be written
	 * according to the requirement of the API.
	 * 
	 * @param filePath The path of the file relative to 'files' to be checked for
	 *                 the validity of request.
	 * @throws IOException The 'canonical file' checked.
	 * @return 'true', if the request if valid. 'false', otherwise.
	 */
	public abstract boolean isValid(String filePath) throws IOException;

	/**
	 * The custom function to validate the file in the request, to be written
	 * according to the requirement of the API.
	 * 
	 * @param file The file to be checked for the validity of request.
	 * @throws IOException The 'canonical file' checked.
	 * @return 'true', if the request if valid. 'false', otherwise.
	 */
	public abstract boolean isValid(File file) throws IOException;

	/**
	 * Validates request containing file and handle invalid request accordingly.
	 * 
	 * @param file File to be checked for validity.
	 * @return 'true', for valid request. 'false', otherwise.
	 */
	public boolean validateFile(File file) {
		try {
			if (isValid(file)) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		criticalLog("CHRoot: Path '" + file.getAbsolutePath() + "' illegal.");
		return false;
	}
}
