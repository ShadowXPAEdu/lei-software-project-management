/*
 * App
 * 
 * Version 1.2
 * 
 * Unicord
 */
package pt.isec;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class App extends Application {

	public static String serverAddress;
	private static App instance;

	private Socket socket;
	private ObjectOutputStream oOS;
	private MainReceiver mainReceiver;
	private User user;
	private Channel selectedChannel;
	private List<Channel> channels;
	private Stage mainStage;
	private Scene scene;
	private final SimpleDateFormat sDF = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
	private final Date date = new Date();

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Invalid arguments: server_address");
			return;
		}
		String serverAddress = args[0];
		try {
			System.out.println("Trying to connect");
			App.serverAddress = serverAddress;
			launch();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static App getApp() {
		return instance;
	}

	public void initialize() throws IOException {
		socket = new Socket(serverAddress, Constants.SERVER_PORT);
		oOS = new ObjectOutputStream(socket.getOutputStream());
		mainReceiver = new MainReceiver(socket);
		mainReceiver.start();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;
		mainStage = primaryStage;
		mainStage.setOnCloseRequest((e) -> {
			e.consume();
			Platform.exit();
			System.exit(0);
		});
		mainStage.setResizable(false);
		initialize();
		System.out.println("Connection Successful");
		
		primaryStage.setTitle("Unicord");
		Parent root = loadFxml("fxml/Login.fxml");
		scene = new Scene(root, 600, 460);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	
	public void sendCommand(String protocol, Object obj) throws IOException {
		Command command = new Command(protocol, obj);
		System.out.println("Sent: " + command);
		oOS.writeUnshared(command);
	}
	
	public Command sendAndReceive(String protocol, Object obj) throws IOException, InterruptedException {
		BlockingQueue<Command> commands = mainReceiver.addListener();
		sendCommand(protocol, obj);
		while (true) {
			Command command = commands.take();
			if (command.protocol.equals(Constants.SUCCESS) || command.protocol.equals(Constants.ERROR)) {
				mainReceiver.removeListener(commands);
				return command;
			}
		}
	}
	
	public void downloadFile(Message message, File absolutePath) throws IOException, InterruptedException {
		BlockingQueue<Command> commands = mainReceiver.addListener();
		Command command = sendAndReceive(Constants.DOWNLOAD_FILE, message.id);
		if (command.protocol.equals(Constants.ERROR)) {
			openMessageDialog(Alert.AlertType.ERROR, "Download file", "An error occurred while downloading your file.");
			return;
		}
		Thread td = new Thread(() -> {
			try {
				File file = new File(absolutePath + File.separator + message.content);
				FileOutputStream fOS = new FileOutputStream(file);
				while (true) {
					Command cmd = commands.take();
					if (cmd.protocol.equals(Constants.FILE_BLOCK) && cmd.extras instanceof FileBlock) {
						FileBlock fileBlock = (FileBlock) cmd.extras;
						if (fileBlock.identifier.equals(Constants.DOWNLOAD_IDENTIFIER + message.content)) {
							if (fileBlock.bytes.length == 0) {
								fOS.close();
								Platform.runLater(() ->openMessageDialog(Alert.AlertType.INFORMATION,
										"Info","Download Completed: " + message.content));
								break;
							}
							fOS.write(fileBlock.bytes);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		td.setDaemon(true);
		td.start();
	}
	
	public void uploadFile(File file) {
		Message message = new Message(0, user.id, selectedChannel.id, Message.TYPE_FILE, file.getName(), 0, user.username);
		message.content = Utils.addTimestampFileName(message.content);
		Thread td = new Thread(() -> {
			try {
				Command command = sendAndReceive(Constants.NEW_MESSAGE, message);
				if (command.protocol.equals(Constants.ERROR)) {
					openMessageDialog(Alert.AlertType.ERROR, "Error Dialog", command.extras.toString());
					return;
				} else {
					FileInputStream fIS = new FileInputStream(file);
					while (true) {
						FileBlock fileBlock = new FileBlock(Constants.UPLOAD_IDENTIFIER + message.content);
						byte[] bytes = fileBlock.bytes;
						int readAmount = fIS.read(bytes);
						if (readAmount <= 0) {
							fileBlock.bytes = new byte[0];
							sendCommand(Constants.FILE_BLOCK, fileBlock);
							fIS.close();
							break;
						}
						if (readAmount < fileBlock.bytes.length) {
							fileBlock.bytes = Arrays.copyOfRange(bytes, 0, readAmount);
						}
						sendCommand(Constants.FILE_BLOCK, fileBlock);
						fileBlock.bytes = bytes;
					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		td.setDaemon(true);
		td.start();
	}
	
	public void openMessageDialog(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(message);

		alert.showAndWait();
	}
	
	public boolean openMessageDialogDeleteChannel(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setContentText(message);
		Optional<ButtonType> result = alert.showAndWait();
		System.out.println(result);
		if (result.isEmpty()) return false;
		return !result.get().getButtonData().isCancelButton();
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Channel> getChannels() {
		return channels;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	
	public Stage getStage() {
		return mainStage;
	}
	
	public Channel getSelectedChannel() {
		return selectedChannel;
	}
	
	public void setSelectedChannel(Channel selectedChannel) {
		this.selectedChannel = selectedChannel;
	}
	
	public BlockingQueue<Command> getReceivedObjectQueue() {
		return mainReceiver.addListener();
	}
	
	public String getFormattedDate(long time) {
		date.setTime(time);
		return sDF.format(date);
	}
	
	public Parent loadFxml(String fxml) throws IOException {
		return FXMLLoader.load(getClass().getResource(fxml));
	}
	
	public void setWindowRoot(String fxml) throws IOException {
		scene.setRoot(loadFxml("fxml/" + fxml));
	}
	
	public void setWindowRoot(String fxml, double width, double height) throws IOException {
		setWindowRoot(fxml);
		scene.getWindow().setWidth(width);
		scene.getWindow().setHeight(height);
	}
	
}
