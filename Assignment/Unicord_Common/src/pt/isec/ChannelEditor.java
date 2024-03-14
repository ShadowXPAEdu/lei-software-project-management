/*
 * ChannelEditor
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.Serializable;
import java.util.ArrayList;

public class ChannelEditor implements Serializable {
	
	public final int channelId;
	public String name;
	public ArrayList<String> usersIn;
	public ArrayList<String> usersOut;
	
	public ChannelEditor(int channelId) {
		this.channelId = channelId;
	}
	
	@Override
	public String toString() {
		return "ChannelEditor{" +
				"channelId=" + channelId +
				", name='" + name + '\'' +
				", usersToAdd=" + usersIn +
				", usersToRemove=" + usersOut +
				'}';
	}
}
