/*
 * Validator
 * 
 * Version 1.2
 * 
 * Unicord
 */
package pt.isec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    
    private Validator(){}
    
    public static boolean checkUserPasswordRules(String password){
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@.$#!?/|&\\\\%()]).{8,25}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean checkUsernameRules(String username){
        return username.length() >= 6 && username.length() <= 25;
    }
}
