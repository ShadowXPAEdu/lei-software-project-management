/*
 * ValidatorTest
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec.tests;

import org.junit.jupiter.api.Test;
import pt.isec.*;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
	
	Database database;
	
	public ValidatorTest() throws SQLException {
		this.database = new Database(Constants.getDatabaseConnectionString("localhost"),
				Constants.DATABASE_USERNAME, Constants.DATABASE_PASSWORD);
	}
	
	@Test
	void checkUsernameAvailability() throws SQLException {
		assertSame(false, Validator.checkUsernameAvailability("Admin", database));
		assertSame(true, Validator.checkUsernameAvailability("ngoriengrewio", database));
		assertSame(true, Validator.checkUsernameAvailability("migroewmo", database));
		
	}
	
	@Test
	void checkChannelAvailability() throws SQLException {
		assertSame(false, Validator.checkChannelAvailability("General", database));
		assertSame(true, Validator.checkChannelAvailability("ngoriengrewio", database));
		assertSame(true, Validator.checkChannelAvailability("migroewmo", database));
	}
	
	@Test
	void checkPasswordMatchUsername() throws SQLException, NoSuchAlgorithmException {
		assertSame(false, Validator.checkPasswordMatchUsername(new User("Admin", Utils.hashString("gwerjiosns")), database));
		assertSame(false, Validator.checkPasswordMatchUsername(new User("ndfaseio", Utils.hashString("random secure password")), database));
		assertSame(true, Validator.checkPasswordMatchUsername(new User("Admin", Utils.hashString("random secure password")) , database));
	}
	
}