/*
 * MainServer
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainServer extends Thread {
	
	private static MainServer instance;
	
	public static MainServer getInstance() {
		return instance;
	}
	
	public final List<ClientThread> clients;
	public final Database database;
	private final ServerSocket serverSocket;
	
	
	public MainServer(Database database, int serverPort) throws Exception {
		this.database = database;
		this.serverSocket = new ServerSocket(serverPort);
		clients = new ArrayList<>();
		
		if (instance != null) throw new Exception("what you trying to do????");
		MainServer.instance = this;
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}
	
	@Override
	public void run() {
		System.out.println("Server Running");
		try {
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Accepted new Client");
				ClientThread client = new ClientThread(socket, this);
				client.start();
				clients.add(client);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendToAll(String protocol, Object extras) throws IOException {
		for (var client : clients) {
			client.sendCommand(protocol, extras);
		}
	}
	
	public void sendToUser(int userId, String protocol, Object extras) throws IOException {
		for (ClientThread u : clients) {
			if (u.isLoggedIn() && u.getUser().id == userId) {
				u.sendCommand(protocol, extras);
				return;
			}
		}
	}
	
	public void sendToChannelUsers(int channelId, String protocol, Object extras) throws IOException {
		for (ClientThread u : clients) {
			if (u.isLoggedIn() && u.getCurrentChannel() == channelId) {
				u.sendCommand(protocol, extras);
			}
		}
	}
	
	public void shutdown() {
		try {
			sendToAll(Constants.SERVER_SHUTDOWN, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeClient(ClientThread client) {
		clients.remove(client);
	}
}
