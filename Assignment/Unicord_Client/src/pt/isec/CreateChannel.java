/*
 * CreateChannel
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
import javafx.stage.Stage;

import java.io.IOException;

public class CreateChannel {

    public TextField channelNameTextField;
    public Button createButton;

    public void createButton(ActionEvent actionEvent) {

        String channelName = channelNameTextField.getText();
        App app = App.getApp();
        Channel channel = new Channel(app.getUser().id,channelName);

        try {
            Command command = app.sendAndReceive(Constants.NEW_CHANNEL, channel);
            if (command.protocol.equals(Constants.SUCCESS)) {
                Stage thisStage = (Stage) createButton.getScene().getWindow();
                thisStage.close();
            } else {
                App.getApp().openMessageDialog(Alert.AlertType.ERROR, "Creating Channel", (String) command.extras);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
