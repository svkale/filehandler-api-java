package com.filehandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.io.IOException;
import java.util.Random;

/**
 * FileHandler: Add/Remove/Edit files from storage.
 * This API can get file list, read metadata, read, create, edit and remove
 * files and directories.
 * 
 * Use FileStats if no direct operation of file is to be done.
 * 
 * @see FileStats
 */
public class FileHandler extends FileStats {

	/**
	 * To create random string of given length.
	 * 
	 * @param n Length of the standard string to be generated.
	 * @return Random string of given length.
	 */
	private static String randomStr(int n) {
		String ret = "";
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < n; i++) {
			ret += (char) (rand.nextInt(26) + 'a');
		}
		return ret;
	}

	@Override
	public void changeCHRoot() {
		files = new File(getCHRoot(), "files");
		deletes = new File(getCHRoot(), "deletes");
		files.mkdirs();
		deletes.mkdirs();
		log("Getting stats of files from '" + files.getAbsolutePath() + "'.");
		log("Keeping API deleted files to '" + deletes.getAbsolutePath() + "'.");
	}

	/**
	 * The directory where deleted files are kept.
	 */
	protected File deletes;

	/**
	 * Handling CLI based FileHandler calls.
	 * 
	 * @param args To be implemented
	 */
	public static void main(String[] args) {
		return;
	}

	/**
	 * Create new 'FileHandler' instance with user home directory as restricted
	 * directory
	 */
	public FileHandler() {
		super();
		deletes = new File(getCHRoot(), "deletes");
		if (!deletes.exists() && !deletes.mkdirs()) {
			criticalLog("(FileHandler) Error creating directory 'deletes' in '" + getCHRoot() + "'.");
		}
	}

	/**
	 * Create new 'FileHandler' instance with 'CHRoot' as restricted directory.
	 * 
	 * @param CHRoot Restricted directory to call API. No API call for file outside
	 *               this directory can be made.
	 */
	public FileHandler(String CHRoot) {
		super(CHRoot);
		deletes = new File(CHRoot, "deletes");
		if (!deletes.exists() && !deletes.mkdirs()) {
			criticalLog("(FileHandler) Error creating directory 'deletes' in '" + CHRoot + "'.");
		}
	}

	/**
	 * Creates new file at filePath.
	 * 
	 * @param filePath Path of file to be created related to 'files'.
	 * @return 'true', if file is created successfully. 'false', otherwise.
	 */
	public boolean createFile(String filePath) {
		try {
			log("(FileHandler) createFile '" + filePath + "'.");
			File file = new File(files, filePath);
			if (!validateFile(file)) {
				return false;
			}
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log("FileHandler API Error: Can't create new file due to internal error.");
		}
		return false;
	}

	/**
	 * Writes to the file at filePath.
	 * 
	 * @param filePath Path of file, where data is to be written related to 'files'.
	 * @param buffer   The data to be written to file.
	 * @param append   To append to file, or overwrite the file.
	 * @return 'true', if data is written successfully. 'false', otherwise.
	 */
	public boolean write(String filePath, byte[] buffer, boolean append) {
		try {
			log("(FileHandler) writeFile '" + filePath + "' " + new String(buffer) + " '" + append
					+ "'-append.");
			File file = new File(files, filePath);
			if (!validateFile(file)) {
				return false;
			}
			file.getParentFile().mkdirs();
			if (file.exists() || file.createNewFile()) {
				FileOutputStream fis = new FileOutputStream(file, append);
				fis.write(buffer);
				fis.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log("FileHandler API Error: Can't create new file due to internal error.");
		}
		return false;
	}

	/**
	 * Deletes the file/directory at filePath.
	 * 
	 * @param filePath Path of file/directory to be deleted relative to 'files'.
	 * @return 'true', if file/directory is deleted successfully. 'false',
	 *         otherwise.
	 */
	public boolean delete(String filePath) {
		log("(FileHandler) delete '" + filePath + "'.");
		File file = new File(files, filePath);
		if (!validateFile(file)) {
			return false;
		}
		File deleted = new File(deletes, filePath + "." + randomStr(4));
		if ((deleted.getParentFile().isDirectory() || deleted.getParentFile().mkdirs()) && file.renameTo(deleted)) {
			return true;
		}
		return false;
	}

	/**
	 * Reads the file at filePath.
	 * 
	 * @param filePath Path of file to be read, relative to 'files'.
	 * @return The array of bytes, if the file is read totally. 'null', otherwise.
	 */
	public byte[] readFile(String filePath) {
		try {
			log("(FileHandler) readFile '" + filePath + "'.");
			File file = new File(files, filePath);
			if (!validateFile(file)) {
				return null;
			}
			if (file.exists() && file.isFile()) {
				byte[] ret;
				FileInputStream fis = new FileInputStream(file);
				ret = fis.readAllBytes();
				fis.close();
				return ret;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log("FileHandler API Error: Can't read the file due to internal error.");
		}
		return null;
	}
}
