/*
 * Message
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 8574382340L;
	
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_FILE = "file";
	
	public int id;
	public int senderId;
	public int channelId;
	public long date;
	public String type;
	public String content;
	public String senderUsername;
	
	public Message(int senderId, int channelId, String type, String content) {
		this.senderId = senderId;
		this.channelId = channelId;
		this.type = type;
		this.content = content;
	}
	
	public Message(int id, int senderId, int channelId, String type, String content, long date, String senderUsername) {
		this.id = id;
		this.senderId = senderId;
		this.channelId = channelId;
		this.type = type;
		this.content = content;
		this.date = date;
		this.senderUsername = senderUsername;
	}
	
	@Override
	public String toString() {
		return "Message{" +
				"id=" + id +
				", senderId=" + senderId +
				", channelId=" + channelId +
				", date=" + date +
				", type='" + type + '\'' +
				", content='" + content + '\'' +
				", senderUsername='" + senderUsername + '\'' +
				'}';
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message user = (Message) o;
		return id == user.id;
	}
}
