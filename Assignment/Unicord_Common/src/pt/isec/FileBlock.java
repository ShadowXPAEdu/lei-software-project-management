/*
 * FileBlock
 *
 * Version 1
 *
 * Unicord
 */
package pt.isec;

import java.io.Serial;
import java.io.Serializable;

public class FileBlock implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 50348927L;
	
	public final String identifier;
	public int offset;
	public byte[] bytes;
	
	public FileBlock(String identifier) {
		this.identifier = identifier;
		this.bytes = new byte[Constants.BUFFER_SIZE];
	}
	
	@Override
	public String toString() {
		return "FileBlock{" +
				"identifier='" + identifier + '\'' +
				", offset=" + offset +
				", bytes.length=" + bytes.length +
				'}';
	}
}
