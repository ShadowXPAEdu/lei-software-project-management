/*
 * MainWindow
 * 
 * Version 1.2
 * 
 * Unicord
 */
package pt.isec;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;

public class MainWindow implements Initializable {

    private static App app;

    public ScrollPane channelsScrollPane;
    public ScrollPane messageFileScrollPane;
    public VBox channelsVBox;
    public VBox messagesFilesVBox;
    public TextField messageTextField;

    private List<Message> messages = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        app = App.getApp();
        try {
            Command command = app.sendAndReceive(Constants.GET_CHANNELS, null);
            var channels = (List<Channel>) command.extras;
            app.setChannels(channels);
            var channel = channels.get(0);
            app.setSelectedChannel(channel);
            updateChannelList();
            channelListOnClick();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        getUpdates();
        messagesFilesVBox.setSpacing(5);
        messagesFilesVBox.setPadding(new Insets(10, 20, 10, 20));
        messageFileScrollPane.vvalueProperty().bind(messagesFilesVBox.heightProperty());

        app.getStage().widthProperty().addListener((observable, oldValue, newValue) -> {
            messagesFilesVBox.setPrefWidth(messageFileScrollPane.getWidth() - 20);
        });
        app.getStage().heightProperty().addListener((observable, oldValue, newValue) -> {
            messagesFilesVBox.setPrefHeight(messageFileScrollPane.getHeight());
        });
    }

    public void getUpdates() {
        new Thread(() -> {
            try {
                BlockingQueue<Command> objectQueue = app.getReceivedObjectQueue();

                while (true) {
                    Command command = objectQueue.take();
                    System.out.println("GetUpdates : " + command);
                    switch (command.protocol) {
                        case Constants.NEW_CHANNEL -> {
                            Channel channel = (Channel) command.extras;
                            app.getChannels().add(channel);
                            Platform.runLater(() -> updateChannelList());
                        }
                        case Constants.EDIT_CHANNEL -> {
                            Channel channel = (Channel) command.extras;
                            app.getChannels().remove(channel);
                            app.getChannels().add(channel);
                            Platform.runLater(() -> updateChannelList());
                        }
                        case Constants.DELETE_CHANNEL -> {
                            Channel channel = (Channel) command.extras;
                            app.getChannels().remove(channel);
                            Platform.runLater(() -> {
                                if (app.getSelectedChannel().equals(channel)){
                                    app.setSelectedChannel(app.getChannels().get(0));
                                    try {
                                        channelListOnClick();
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                updateChannelList();
                            });
                        }
                        case Constants.NEW_MESSAGE -> {
                            Message message = (Message) command.extras;
                            messages.add(message);
                            Platform.runLater(() -> messagesFilesVBox.getChildren().add(insertLine(message)));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createMenuItem(ActionEvent actionEvent) {
        //dialog Create Channel
        try {
            Stage createChannel = new Stage();
            createChannel.initModality(Modality.APPLICATION_MODAL);
            createChannel.setTitle("Unicord - Create channel");
            Scene cC = new Scene(app.loadFxml("fxml/CreateChannel.fxml"));
            createChannel.setScene(cC);
            createChannel.setResizable(false);
            createChannel.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void aboutMenuItem(ActionEvent actionEvent) {
        App app = App.getApp();
        app.openMessageDialog(Alert.AlertType.INFORMATION, "About", "Work done by:\n- Dorin Bosii\n- Leandro Fidalgo\n- Pedro Alves\n- Rodrigo Mendes\n- Davide Coelho");
    }

    public void SendButton(ActionEvent actionEvent) {
        String messageText = messageTextField.getText();
        if (messageText.isBlank()) return;
        if (messageText.length() > 511) {
            app.openMessageDialog(Alert.AlertType.ERROR, "Max lenght", "Give us a break, try to write less(Max:500)!");
            return;
        }
        messageTextField.setText("");
        Message message = new Message(0, app.getUser().id, app.getSelectedChannel().id, Message.TYPE_TEXT, messageText, 0, app.getUser().username);
        try {
            Thread td = new Thread(() -> {
                try {
                    app.sendCommand(Constants.NEW_MESSAGE, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            td.setDaemon(true);
            td.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendFileButton(ActionEvent actionEvent) {
        App app = App.getApp();
        if (app.getSelectedChannel() == null) {
            app.openMessageDialog(Alert.AlertType.ERROR, "Error Dialog", "Select a channel to send a file!");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the file");
        File file = fileChooser.showOpenDialog(app.getStage());
        if (file == null) {
            return;
        }
        app.uploadFile(file);
    }

    public void onEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            SendButton(null);
        }
    }

    private void updateChannelList() {
        channelsVBox.getChildren().clear();
        for (Channel channel : app.getChannels()) {

            HBox box = new HBox();
            box.setFillHeight(true);

            Label label = new Label(channel.name);
            if (channel.id == app.getSelectedChannel().id) {
                box.setStyle("-fx-background-color: cyan;");
            }

            label.setOnMouseClicked(event -> {
                event.consume();
                try {
                    app.setSelectedChannel(channel);
                    updateChannelList();
                    channelListOnClick();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            box.getChildren().add(label);
            if (channel.creatorId == app.getUser().id) {
                ImageView image = new ImageView(getClass().getResource("Images/gear.png").toExternalForm());
                image.setFitWidth(15);
                image.setFitHeight(15);
                image.setOnMouseClicked(event -> {
                    event.consume();
                    app.setSelectedChannel(channel);
                    updateChannelList();
                    openEditChannel();
                });
                box.getChildren().add(image);
            }
            channelsVBox.getChildren().add(box);
        }
    }

    private void channelListOnClick() throws IOException, InterruptedException {
        Command command = app.sendAndReceive(Constants.GET_MESSAGES, app.getSelectedChannel().id);
        if (!command.protocol.equals(Constants.SUCCESS)) {
            return;
        }
        messages = (ArrayList<Message>) command.extras;
        updateMessageList(messages);
    }

    private void updateMessageList(List<Message> messages) {
        messagesFilesVBox.getChildren().clear();
        for (var message : messages) {
            messagesFilesVBox.getChildren().add(insertLine(message));
        }
    }

    private Node insertLine(Message message) {
        HBox box = new HBox(10);
        box.setFillHeight(true);

        Label dateLabel = new Label(app.getFormattedDate(message.date));
        Label usernameLabel = new Label(message.senderUsername + ":");
        VBox vBox = new VBox();
        Label label;
        int yau = (int) Math.ceil(message.content.length() / 100.0);
        for (int i = 0, j = 0; i < yau; i++, j += 101) {
                if(message.content.length() < j + 100 ){
                    label = new Label(message.content.substring(j));
                }else{
                    label = new Label(message.content.substring(j, j + 100));
                }
                vBox.getChildren().add(label);
        }
        usernameLabel.setTextFill(app.getUser().id != message.senderId ? Color.web("#7D82B8") : Color.web("#B8B37D"));
        box.getChildren().addAll(dateLabel, usernameLabel, vBox);
        box.setAlignment(Pos.BASELINE_LEFT);

        Button downloadBtn = null;
        if (message.type.equals(Message.TYPE_FILE)) {
            downloadBtn = new Button();
            downloadBtn.setMaxWidth(10);
            downloadBtn.setMaxHeight(10);
            ImageView image = new ImageView(getClass().getResource("Images/download_icon.png").toExternalForm());
            image.setFitWidth(10);
            image.setFitHeight(10);
            downloadBtn.setGraphic(image);
            downloadBtn.setOnAction(event -> {
                event.consume();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File fileDirectory = directoryChooser.showDialog(app.getStage());
                if (fileDirectory == null) {
                    return;
                }
                try {
                    app.downloadFile(message, fileDirectory);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        if (downloadBtn != null) {
            box.getChildren().add(downloadBtn);
        }
        return box;
    }

    private void openEditChannel() {
        try {
            Stage editChannelStage = new Stage();
            editChannelStage.initModality(Modality.APPLICATION_MODAL);
            editChannelStage.setTitle("Unicord - Edit channel");
            Scene cC = new Scene(app.loadFxml("fxml/EditChannel.fxml"));
            editChannelStage.setScene(cC);
            editChannelStage.setResizable(false);
            editChannelStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
