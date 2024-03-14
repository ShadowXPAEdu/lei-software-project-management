/*
 * ClientThread
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ClientThread extends Thread {
	
	private final Socket socket;
	private ObjectOutputStream oos;
	private MainReceiver receiver;
	private final MainServer app;
	private User user = null;
	private int currentChannel = 1;
	
	public ClientThread(Socket socket, MainServer mainServer) {
		this.socket = socket;
		this.app = mainServer;
	}
	
	public void sendCommand(String protocol, Object extras) throws IOException {
		Command obj = new Command(protocol, extras);
		oos.writeUnshared(obj);
		if (!(extras instanceof FileBlock)) {
			if (user != null)
				System.out.println(user.username + ": " + obj);
		}
	}
	
	@Override
	public void run() {
		BlockingQueue<Command> queue = null;
		try {
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.receiver = new MainReceiver(this, socket);
			this.receiver.start();
			queue = receiver.addListener();
			
			while (true) {
				Command command = queue.take();
				handleCommand(command);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (queue != null) receiver.removeListener(queue);
			app.clients.remove(this);
		}
	}
	
	private void handleCommand(Command command) throws Exception {
		switch (command.protocol) {
			case Constants.REGISTER -> protocolRegister((User) command.extras);
			case Constants.LOGIN -> protocolLogin((User) command.extras);
			default -> {
				if (!isLoggedIn()) {
					sendCommand(Constants.ERROR, "User is not logged in");
					return;
				}
				switch (command.protocol) {
					case Constants.LOGOUT -> protocolLogout();
					case Constants.GET_CHANNELS -> protocolGetChannels();
					case Constants.NEW_CHANNEL -> protocolNewChannel((Channel) command.extras);
					case Constants.GET_MESSAGES -> protocolGetMessages((int) command.extras);
					case Constants.NEW_MESSAGE -> protocolNewMessage((Message) command.extras);
					case Constants.DOWNLOAD_FILE -> protocolDownloadMessage((int) command.extras);
					case Constants.EDIT_CHANNEL -> protocolEditChannel((ChannelEditor) command.extras);
					case Constants.DELETE_CHANNEL -> protocolDeleteChannel((int) command.extras);
					case Constants.EDIT_CHANNEL_GET_USERS -> protocolEditChannelGetUsers((int) command.extras);
				}
			}
		}
	}
	
	private void protocolEditChannelGetUsers(int channelId) throws IOException, SQLException {
		ChannelEditor editor = new ChannelEditor(channelId);
		Channel channel = app.database.Channel.getByID(channelId);
		if (channel == null) {
			sendCommand(Constants.ERROR, "Client Side Error");
			return;
		}
		
		editor.name = channel.name;
		
		List<User> usersIn = app.database.Channel.getChannelUsers(editor.channelId);
		
		//Users already on the channel
		editor.usersIn = new ArrayList<>(usersIn.size());
		usersIn.forEach(user -> editor.usersIn.add(user.username));
		//Users that are not part of the channel
		List<User> allUsers = app.database.User.getAll();
		editor.usersOut = new ArrayList<>(allUsers.size() - usersIn.size());
		
		allUsers.forEach(user -> {
			if (!editor.usersIn.contains(user.username) && user.id != this.user.id)
				editor.usersOut.add(user.username);
		});
		
		sendCommand(Constants.SUCCESS, editor);
	}
	
	private void protocolRegister(User user) throws IOException, SQLException {
		// Colocar na base de dados o user
		// Enviar success ou error
		if (!Validator.checkUsernameAvailability(user.username, app.database)) {
			sendCommand(Constants.ERROR, "Username already in use");
		} else if (app.database.User.createUser(user)) {
			sendCommand(Constants.SUCCESS, null);
		} else {
			sendCommand(Constants.ERROR, "Register failed!");
		}
	}
	
	private void protocolLogin(User user) throws SQLException, IOException {
		// verificar se a palavra passe está correta com o username
		if (!Validator.checkPasswordMatchUsername(user, app.database)) {
			sendCommand(Constants.ERROR, "Password does not match username");
			return;
		}
		// se estiver guardar o utilizador neste objeto
		this.user = app.database.User.getByUsername(user.username);
		sendCommand(Constants.SUCCESS, this.user);
	}
	
	private void protocolLogout() throws IOException {
		this.user = null;
		sendCommand(Constants.SUCCESS, null);
	}
	
	private void protocolGetChannels() throws SQLException, IOException {
		ArrayList<Channel> userChannels = app.database.Channel.getUserChannels(user.id);
		sendCommand(Constants.SUCCESS, userChannels);
	}
	
	private void protocolGetMessages(int channelId) throws IOException, SQLException {
		ArrayList<Message> messages = app.database.Message.getAll(channelId);
		sendCommand(Constants.SUCCESS, messages);
		currentChannel = channelId;
	}
	
	private void protocolNewMessage(Message message) throws SQLException, IOException {
		message.senderId = user.id;
		message.channelId = currentChannel;
		
		if (!app.database.Channel.isUserPartOfChannel(user.id, message.channelId)) {
			sendCommand(Constants.ERROR, "This shouldn't happen, user doesnt belong to channel");
			return;
		}
		
		boolean success = app.database.Message.createMessage(message);
		if (!success) {
			sendCommand(Constants.ERROR, "Server Error");
			return;
		}
		message = app.database.Message.getByID(message.id);
		if (message.type.equals(Message.TYPE_TEXT)) {
			app.sendToChannelUsers(message.channelId, Constants.NEW_MESSAGE, message);
		} else {
			BlockingQueue<Command> commandQueue = receiver.addListener();
			sendCommand(Constants.SUCCESS, null);
			Message finalMessage = message;
			new Thread(() -> {
				try {
					FileOutputStream fos = new FileOutputStream(Constants.getFile(finalMessage.content));
					while (true) {
						Command command = commandQueue.take();
						
						if (command.protocol.equals(Constants.FILE_BLOCK) && command.extras instanceof FileBlock) {
							FileBlock fileBlock = (FileBlock) command.extras;
							// This is upload from the client to the server
							if (fileBlock.identifier.equals(Constants.UPLOAD_IDENTIFIER + finalMessage.content)) {
								if (fileBlock.bytes.length == 0) {
									fos.close();
									break;
								}
								fos.write(fileBlock.bytes);
							}
						}
					}
					app.sendToChannelUsers(finalMessage.channelId, Constants.NEW_MESSAGE, finalMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
				receiver.removeListener(commandQueue);
			}).start();
		}
	}
	
	private void protocolDeleteChannel(int channelId) throws SQLException, IOException {
		Channel channel = app.database.Channel.getByID(channelId);
		if (channel.creatorId == user.id) {
			if (app.database.Channel.deleteChannel(channelId)) {
				currentChannel = -1;
				sendCommand(Constants.SUCCESS, null);
				app.sendToAll(Constants.DELETE_CHANNEL, channel);
			} else {
				sendCommand(Constants.ERROR, "Could not delete channel!");
			}
		} else {
			sendCommand(Constants.ERROR, "You are not the channel owner!");
		}
	}
	
	private void protocolDownloadMessage(int messageId) {
		// Actually uploads
		new Thread(() -> {
			try {
				Message message = app.database.Message.getByID(messageId);
				if (message == null) {
					sendCommand(Constants.ERROR, "Message does not exist");
					return;
				}
				sendCommand(Constants.SUCCESS, null);
				
				FileInputStream fis = new FileInputStream(Constants.getFile(message.content));
				// This is download from the client to the server
				while (true) {
					FileBlock fileBlock = new FileBlock(Constants.DOWNLOAD_IDENTIFIER + message.content);
					byte[] bytes = fileBlock.bytes;
					int readAmount = fis.read(bytes);
					if (readAmount <= 0) {
						fileBlock.bytes = new byte[0];
						sendCommand(Constants.FILE_BLOCK, fileBlock);
						fis.close();
						break;
					}
					if (readAmount < fileBlock.bytes.length) {
						fileBlock.bytes = Arrays.copyOfRange(bytes, 0, readAmount);
					}
					sendCommand(Constants.FILE_BLOCK, fileBlock);
					fileBlock.bytes = bytes;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void protocolNewChannel(Channel channel) throws SQLException, IOException {
		channel.creatorId = user.id;
		if (!Validator.checkChannelAvailability(channel.name, app.database)) {
			sendCommand(Constants.ERROR, "Channel name already in use");
			return;
		}
		
		boolean success = app.database.Channel.createChannel(channel);
		if (!success) {
			sendCommand(Constants.ERROR, "Server Error");
			return;
		}
		sendCommand(Constants.SUCCESS, null);
		sendCommand(Constants.NEW_CHANNEL, channel);
	}
	
	private void protocolEditChannel(ChannelEditor channelChanges) throws IOException, SQLException {
		var channel = app.database.Channel.getByID(channelChanges.channelId);
		if (channel.creatorId != user.id) {
			sendCommand(Constants.ERROR, "User is  not channel owner");
			return;
		}
		
		if (channelChanges.name != null) {
			channel.name = channelChanges.name;
			if (Validator.checkChannelAvailability(channel.name, app.database)) {
				if (!app.database.Channel.editChannel(channel)) {
					sendCommand(Constants.ERROR, "Something went wrong - channel name");
					return;
				}
			} else {
				sendCommand(Constants.ERROR, "Name already in use");
				return;
			}
		}
		if (channelChanges.usersIn != null) {
			for (var username : channelChanges.usersIn) {
				User user = app.database.User.getByUsername(username);
				if (app.database.Channel.addUser(user.id, channel.id)) {
					app.sendToUser(user.id, Constants.NEW_CHANNEL, channel);
				}
			}
		}
		if (channelChanges.usersOut != null) {
			for (var username : channelChanges.usersOut) {
				User user = app.database.User.getByUsername(username);
				
				if (app.database.Channel.removeUser(user.id, channel.id)) {
					app.sendToUser(user.id, Constants.DELETE_CHANNEL, channel);
				}
			}
		}
		sendCommand(Constants.SUCCESS, null);
		List<User> users = app.database.Channel.getChannelUsers(channelChanges.channelId);
		Channel channelByID = app.database.Channel.getByID(channelChanges.channelId);
		for (User u : users) {
			app.sendToUser(u.id, Constants.EDIT_CHANNEL, channelByID);
		}
		sendCommand(Constants.EDIT_CHANNEL, channelByID);
	}
	
	public User getUser() {
		return this.user;
	}
	
	public int getCurrentChannel() {
		return this.currentChannel;
	}
	
	public boolean isLoggedIn() {
		return user != null;
	}
}
