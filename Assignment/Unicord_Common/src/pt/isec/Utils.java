/*
 * Utils
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Utils {
	
	public static String hashString(String str) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(str.getBytes());
		return new BigInteger(1, hash).toString(36);
	}

	public static void createFileDirectories(File file) {
		createDirectories(file, true);
	}

	private static void createDirectories(File file, boolean bottom) {
		if (file.exists()) return;
		File parent = file.getParentFile();
		if (parent != null)
			createDirectories(parent, false);
		if (!bottom)
			file.mkdir();
	}

	public static String addTimestampFileName(String fileName) {
		String utcTimeString = "" + new Date().getTime();
		int timeLength = utcTimeString.length() - 8;
		
		utcTimeString = utcTimeString.substring(Math.max(timeLength, 0));
		
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex == -1)
			return fileName + "_" + utcTimeString;
		else
			return fileName.substring(0, dotIndex) + "_" + utcTimeString + fileName.substring(dotIndex);
	}
}
