package com.filehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * FileStats: Get information about files.
 * This API can get file list, read metadata and do similar
 * operations, NOT involving direct operation on file (like reading, adding,
 * removing or editing).
 * 
 * Use FileHandlerAPI for reading, creating, editing or removing files and
 * directories.
 * 
 * @see FileHandler
 */
public class FileStats extends CHRooted {

	/**
	 * The directory where methods to get information are allowed to be executed.
	 */
	protected File files;

	@Override
	public void changeCHRoot() {
		files = new File(getCHRoot(), "files");
		files.mkdirs();
		log("Getting stats of files from '" + files.getAbsolutePath() + "'.");
	}

	@Override
	public boolean isValid(String filePath) throws IOException {
		return isValid(new File(files, filePath));
	}

	@Override
	public boolean isValid(File file) throws IOException {
		return !file.getCanonicalPath().equals(files.getCanonicalPath())
				&& file.getCanonicalPath().startsWith(files.getCanonicalPath());
	}

	/**
	 * Handling CLI based FileStats calls.
	 * 
	 * @param args To be implemented
	 */
	public static void main(String[] args) {
		return;
	}

	/**
	 * Create new 'FileStats' instance with a directory named "filehandler_chroot"
	 * from user home directory as isolated directory. If you want to keep some
	 * files accessible by API, keep them in the 'files' directory under the CHRoot.
	 */
	public FileStats() {
		this.setCHRoot(getCHRoot());
		files = new File(getCHRoot(), "files");
		if (!files.exists() && !files.mkdirs()) {
			criticalLog("Error creating directory 'files' in '" + getCHRoot() + "'.");
		}
	}

	/**
	 * Create new 'FileStats' instance with 'newCHRoot' as isolated directory. If
	 * you
	 * want to keep some files accessible by API, keep them in the 'files' directory
	 * under the CHRoot.
	 * 
	 * @param newCHRoot Absolute path to isolated directory for safe API calling.
	 */
	public FileStats(String newCHRoot) {
		this.setCHRoot(newCHRoot);
		files = new File(getCHRoot(), "files");
		if (!files.exists() && !files.mkdirs()) {
			criticalLog("(FileStats) Error creating directory 'files' in '" + getCHRoot() + "'.");
		}
	}

	/**
	 * Checks if file or directory with 'fileName' exists within 'CHRoot'.
	 * 
	 * @param filePath Path of file or directory to check for existence relative to
	 *                 'CHRoot';
	 * @return 'true', if file or directory exists at given path. 'false',
	 *         otherwise.
	 */
	public boolean exists(String filePath) {
		log("(FileStats) exists '" + filePath + "'.");
		File file = new File(files, filePath);
		if (!validateFile(file)) {
			return false;
		}
		if (file.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the file size in bytes.
	 * 
	 * @param filePath Path of file to get size for.
	 * @return File size in bytes, if valid request. -1, otherwise.
	 */
	public long getFileSize(String filePath) {
		log("(FileStats) getFileSize '" + filePath + "'.");
		File file = new File(files, filePath);
		if (!validateFile(file)) {
			return -1;
		}
		if (file.exists() && file.isFile()) {
			return file.length();
		}
		return -1;
	}

	/**
	 * Stores all the basic metadata of the file / directory.
	 * If 'file' is not valid, it should be handled before calling this.
	 */
	public class FileInfo {
		/**
		 * The name of the file.
		 */
		public String name;
		/**
		 * The path of the file relative to 'files'.
		 */
		public String path;
		/**
		 * The type of file. 'File' or 'Directory'.
		 */
		public String type;
		/**
		 * The size of file in bytes.
		 */
		public long size;
		/**
		 * The date and time (ISO 8601 format) of the file / directory last modified.
		 */
		public String lastModified;

		/**
		 * Sets 'path', 'size' and 'lastModified' of this instance.
		 * 
		 * @param file The file for which metadata is to be taken.
		 */
		public FileInfo(File file) {
			try {
				if (!validateFile(file)) {
					return;
				}
				BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				name = file.getName();
				path = file.getCanonicalPath().substring(files.getCanonicalPath().length() + 1);
				type = file.isFile() ? "File" : "Directory";
				size = attr.size();
				lastModified = attr.lastModifiedTime().toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Sets 'path', 'size' and 'lastModified' of this instance.
		 * 
		 * @param filePath The path of file for which metadata is to be taken.
		 */
		public FileInfo(String filePath) {
			try {
				File file = new File(files, filePath).getCanonicalFile();
				if (!validateFile(file)) {
					return;
				}
				BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				name = file.getName();
				path = filePath;
				type = file.isFile() ? "File" : "Directory";
				size = attr.size();
				lastModified = attr.lastModifiedTime().toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the file metadata.
	 * 
	 * @param filePath Path of file or directory for getting its information.
	 * @return The 'FileInfo' object, if the request if valid. 'null', otherwise.
	 */
	public FileInfo getInfo(String filePath) {
		log("(FileStats) getInfo '" + filePath + "'.");
		File file = new File(files, filePath);
		if (!validateFile(file)) {
			return null;
		}
		if (file.exists()) {
			return new FileInfo(file);
		}
		return null;
	}

	/**
	 * Lists all the contents of a directory.
	 * 
	 * @param filePath Path of the directory for getting its contents.
	 * @return 'null', if nothing exists at filePath. Single 'FileInfo' for file at
	 *         filePath, if file is found. 'FileInfo' array, with '.' as first
	 *         element, representing directory itself, followed by other contents of
	 *         the directory.
	 */
	public FileInfo[] getList(String filePath) {
		log("(FileStats) getList '" + filePath + "'.");
		File file = new File(files, filePath);
		if (file.isFile()) {
			return new FileInfo[] { new FileInfo(file) };
		}
		if (!file.exists()) {
			return null;
		}
		File[] fileList = file.listFiles();
		FileInfo[] ret = new FileInfo[fileList.length + 1];
		ret[0] = new FileInfo(file);
		ret[0].name = ".";
		for (int i = 1; i <= fileList.length; i++) {
			File tfile = fileList[i];
			if (validateFile(tfile))
				ret[i] = new FileInfo(tfile);
		}
		return ret;
	}
}
