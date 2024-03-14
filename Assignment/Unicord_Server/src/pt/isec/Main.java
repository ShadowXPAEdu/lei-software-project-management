/*
 * Main
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

public class Main {
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 1) {
			System.out.println("Invalid arguments: databaseAddress");
			System.exit(-1);
		}
		
		String databaseAddress = args[0];
		
		Database database = new Database(Constants.getDatabaseConnectionString(databaseAddress),
				Constants.DATABASE_USERNAME, Constants.DATABASE_PASSWORD);
		
		
		MainServer server = new MainServer(database, Constants.SERVER_PORT);
		server.start();
		
	}
}
