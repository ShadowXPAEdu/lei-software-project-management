/*
 * Channel
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.Serial;
import java.io.Serializable;

public class Channel implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 4278929534L;
	
	public int id;
	public int creatorId;
	public String name;
	
	public Channel(int creatorId, String name) {
		this.creatorId = creatorId;
		this.name = name;
	}
	
	public Channel(int id, int creatorId, String name) {
		this.id = id;
		this.creatorId = creatorId;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Channel{" +
				"id=" + id +
				", creatorId=" + creatorId +
				", name='" + name + '\'' +
				'}';
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Channel user = (Channel) o;
		return id == user.id;
	}
}
