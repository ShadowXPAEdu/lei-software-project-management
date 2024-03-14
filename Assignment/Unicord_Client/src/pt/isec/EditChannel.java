/*
 * EditChannel
 *
 * Version 1.2
 *
 * Unicord
 */
package pt.isec;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EditChannel implements Initializable {
	
	public Button applyBtn;
	public Button deleteBtn;
	public Button closeBtn;
	public VBox membersVbox;
	public VBox inviteVbox;
	public TextField channelNameTextField;
	
	private ChannelEditor oldChannelEditor;
	private ChannelEditor newChannelEditor;
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		App app = App.getApp();
		try {
			Command command = app.sendAndReceive(Constants.EDIT_CHANNEL_GET_USERS, app.getSelectedChannel().id);
			if (command.protocol.equals(Constants.ERROR)) {
				app.openMessageDialog(Alert.AlertType.ERROR, "Channel Editing", (String) command.extras);
			} else {
				oldChannelEditor = (ChannelEditor) command.extras;
				channelNameTextField.setText(oldChannelEditor.name);
				
				newChannelEditor = new ChannelEditor(oldChannelEditor.channelId);
				newChannelEditor.usersIn = new ArrayList<>();
				newChannelEditor.usersOut = new ArrayList<>();
				
				scrollPanesEditChannel(oldChannelEditor.usersIn, true);
				scrollPanesEditChannel(oldChannelEditor.usersOut, false);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void scrollPanesEditChannel(ArrayList<String> users, boolean isIn) {
		for (var user : users) {
			HBox hBox = new HBox();
			Label label = new Label(user);
			ImageView imageView = clickMaister(isIn);
			
			hBox.getChildren().addAll(label, imageView);
			if (isIn) {
				membersVbox.getChildren().add(hBox);
			} else {
				inviteVbox.getChildren().add(hBox);
			}
		}
	}
	
	public ImageView clickMaister(boolean isIn) {
		ImageView imageView = new ImageView(isIn ? getClass().getResource("Images/delete_user.png").toExternalForm() : getClass().getResource("Images/add_user.png").toExternalForm());
		imageView.setFitWidth(15);
		imageView.setFitHeight(15);
		if (isIn) {
			imageView.setOnMouseClicked(event -> {
				ImageView thisOne = (ImageView) event.getSource();
				HBox box = (HBox) thisOne.getParent();
				Label thisLabel = (Label) box.getChildren().get(0);
				String thisUsername = thisLabel.getText();
				oldChannelEditor.usersIn.removeIf(username -> username.equals(thisUsername));
				newChannelEditor.usersIn.removeIf(username -> username.equals(thisUsername));
				
				membersVbox.getChildren().remove(box);
				inviteVbox.getChildren().add(box);
				
				newChannelEditor.usersOut.add(thisUsername);
				
				box.getChildren().remove(1);
				box.getChildren().add(clickMaister(false));
			});
		} else {
			imageView.setOnMouseClicked(event -> {
				ImageView thisOne = (ImageView) event.getSource();
				HBox box = (HBox) thisOne.getParent();
				Label thisLabel = (Label) box.getChildren().get(0);
				String thisUsername = thisLabel.getText();
				oldChannelEditor.usersIn.removeIf(username -> username.equals(thisUsername));
				newChannelEditor.usersIn.removeIf(username -> username.equals(thisUsername));
				
				inviteVbox.getChildren().remove(box);
				membersVbox.getChildren().add(box);
				
				newChannelEditor.usersIn.add(thisUsername);
				
				box.getChildren().remove(1);
				box.getChildren().add(clickMaister(true));
			});
		}
		return imageView;
	}
	
	public void applyButton(ActionEvent actionEvent) {
		App app = App.getApp();
		String channelName = channelNameTextField.getText();
		
		ChannelEditor temp = new ChannelEditor(oldChannelEditor.channelId);
		temp.usersIn = newChannelEditor.usersIn;
		temp.usersOut = newChannelEditor.usersOut;
		temp.name = channelName;
		
		if (channelName.isBlank() || channelName.equals(oldChannelEditor.name)) {
			newChannelEditor.name = null;
		} else {
			newChannelEditor.name = channelName;
		}
		
		if (newChannelEditor.usersIn != null && newChannelEditor.usersIn.size() == 0)
			newChannelEditor.usersIn = null;
		if (newChannelEditor.usersOut != null && newChannelEditor.usersOut.size() == 0)
			newChannelEditor.usersOut = null;
		
		try {
			Command command = app.sendAndReceive(Constants.EDIT_CHANNEL, newChannelEditor);
			if (command.protocol.equals(Constants.ERROR)) {
				app.openMessageDialog(Alert.AlertType.ERROR, Constants.ERROR, (String) command.extras);
				newChannelEditor = temp;
			} else {
				closeButton(null);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void deleteButton(ActionEvent actionEvent) {
		App app = App.getApp();
		try {
			boolean bool = app.openMessageDialogDeleteChannel(Alert.AlertType.CONFIRMATION,
					"Delete channel", "Do you want to delete this channel?");
			System.out.println(bool);
			if (bool) {
				Command command = app.sendAndReceive(Constants.DELETE_CHANNEL, app.getSelectedChannel().id);
				if (command.protocol.equals(Constants.ERROR)) {
					app.openMessageDialog(Alert.AlertType.ERROR, Constants.ERROR, (String) command.extras);
				} else {
					closeButton(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeButton(ActionEvent actionEvent) {
		Stage thisStage = (Stage) channelNameTextField.getScene().getWindow();
		thisStage.close();
	}
}
