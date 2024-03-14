/*
 * Validator
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.sql.SQLException;

public class Validator {

    public static boolean checkUsernameAvailability(String name, Database db) throws SQLException {
        return db.User.getByUsername(name) == null;
    }

    public static boolean checkChannelAvailability(String name, Database db) throws SQLException {
        return db.Channel.getByName(name) == null;
    }

    public static boolean checkPasswordMatchUsername(User user,Database db) throws SQLException {
        return db.User.doesPasswordMatchUsername(user.username,user.password);
    }

}
