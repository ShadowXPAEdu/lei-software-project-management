/*
 * Constants
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.File;

public class Constants {
	
	public static final String SUCCESS = "SUCCESS";
	public static final String ERROR = "ERROR";
	public static final String REGISTER = "REGISTER";
	public static final String LOGIN = "LOGIN";
	public static final String LOGOUT = "LOGOUT";
	public static final String GET_CHANNELS = "GET_CHANNELS";
	public static final String GET_MESSAGES = "GET_MESSAGES";
	public static final String NEW_MESSAGE = "NEW_MESSAGE";
	public static final String DOWNLOAD_FILE = "DOWNLOAD_FILE";
	public static final String GET_USERS = "GET_USERS";
	public static final String NEW_CHANNEL = "NEW_CHANNEL";
	public static final String EDIT_CHANNEL = "EDIT_CHANNEL";
	public static final String EDIT_CHANNEL_GET_USERS = "EDIT_CHANNEL_GET_USERS";
	public static final String DELETE_CHANNEL = "DELETE_CHANNEL";
	public static final String FILE_BLOCK = "FILE_BLOCK";
	
	public static final String SERVER_SHUTDOWN = "SERVER_SHUTDOWN";
	
	public static final int BUFFER_SIZE = 100 * 1024;

    public static File getFile(String fileName) {
		File file = new File("files" + File.separator + fileName);
		Utils.createFileDirectories(file);
		return file;
	}
	
	public static final int SERVER_PORT = 5432;
	
	public static final String DOWNLOAD_IDENTIFIER = "DOWNLOAD_";
	public static final String UPLOAD_IDENTIFIER = "UPLOAD_";
	
	public static final String DATABASE_USERNAME = "userman";
	public static final String DATABASE_PASSWORD = "random secure password";
	
	public static String getDatabaseConnectionString(String databaseAddress) {
		return "jdbc:mysql://" + databaseAddress + ":3306/gps?allowPublicKeyRetrieval=true&autoReconnect=true&useSSL=false&"
				+ "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	}
}
