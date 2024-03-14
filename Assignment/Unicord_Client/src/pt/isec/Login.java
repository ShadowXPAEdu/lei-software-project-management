/*
 * Login
 * 
 * Version 1.2
 * 
 * Unicord
 */
package pt.isec;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Login {

    public TextField usernameTextField;
    public TextField passwordTextField;
    public Button registerButton;
    public Button logInButton;

    public void registerButton(ActionEvent actionEvent) {
        try {
            App.getApp().setWindowRoot("Register.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logInButton(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        App app = App.getApp();


        if (username.isBlank() || password.isBlank()) {
            app.openMessageDialog(Alert.AlertType.ERROR,Constants.ERROR, "You must enter your credentials");
        } else {
            try {
                User user = new User(username, Utils.hashString(password));
                Command command = app.sendAndReceive(Constants.LOGIN, user);
                if (command.protocol.equals(Constants.ERROR)){
                    app.openMessageDialog(Alert.AlertType.ERROR,Constants.ERROR, (String) command.extras);
                }else{
                    app.setUser((User) command.extras);
                    app.setWindowRoot("MainWindow.fxml", 1300, 760);
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public void onEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            logInButton(null);
        }
    }
}
