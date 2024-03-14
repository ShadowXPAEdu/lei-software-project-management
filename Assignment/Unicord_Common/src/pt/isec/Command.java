/*
 * Command
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.Serializable;

public class Command implements Serializable {
	
	final String protocol;
	final Object extras;
	
	public Command(String protocol, Object extras) {
		this.protocol = protocol;
		this.extras = extras;
	}
	
	public Command(String protocol) {
		this.protocol = protocol;
		this.extras = null;
	}
	
	@Override
	public String toString() {
		return "Command{" +
				"protocol='" + protocol + '\'' +
				", extras=" + extras +
				'}';
	}
}
