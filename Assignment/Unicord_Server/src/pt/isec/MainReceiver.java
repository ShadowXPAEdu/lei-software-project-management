/*
 * MainReceiver
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainReceiver extends Thread {
	
	private final Socket socket;
	private final ObjectInputStream ois;
	private final List<BlockingQueue<Command>> list;
	private final ClientThread creator;
	
	public MainReceiver(ClientThread creator,Socket socket) throws IOException {
		this.creator = creator;
		this.socket = socket;
		this.ois = new ObjectInputStream(socket.getInputStream());
		this.list = Collections.synchronizedList(new ArrayList<>());
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				
				Command command = (Command) ois.readUnshared();
				if (!command.protocol.equals(Constants.FILE_BLOCK)) {
					System.out.println("Received: " + command);
				}
				for (var queue : list) {
					queue.offer(command);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			MainServer.getInstance().removeClient(creator);
			System.out.println("[MainReceiver] Exception: " + e.getMessage());
		}
	}
	
	public BlockingQueue<Command> addListener() {
		BlockingQueue<Command> queue = new LinkedBlockingQueue<>();
		list.add(queue);
		return queue;
	}
	
	public boolean removeListener(BlockingQueue<Command> queue) {
		return list.remove(queue);
	}
}
