/*
 * ValidatorTest
 * 
 * Version 1.2
 * 
 * Unicord
 */
package pt.isec.tests;

import org.junit.jupiter.api.Test;
import pt.isec.Validator;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void checkUserPasswordRules() {
        assertSame(false, Validator.checkUserPasswordRules("badpwd"));
        assertSame(true, Validator.checkUserPasswordRules("Securep@ssword123!"));
        assertSame(false, Validator.checkUserPasswordRules("Teste123"));
        assertSame(true, Validator.checkUserPasswordRules("Teste123."));
        assertSame(false, Validator.checkUserPasswordRules("SecureTeste"));
        assertSame(true, Validator.checkUserPasswordRules("TruePassword123#"));
    }

    @Test
    void checkUsernameRules() {
        assertSame(false, Validator.checkUsernameRules("dorin"));
        assertSame(true, Validator.checkUsernameRules("leandro"));
        assertSame(false, Validator.checkUsernameRules("pedrodassilvasnevescarvalho"));
        assertSame(true, Validator.checkUsernameRules("rodrigo"));
    }
}
