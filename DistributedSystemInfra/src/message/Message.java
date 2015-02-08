/**
 * The Message class, represent the message packet to be sent
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */

package message;

import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = -4455309556213654127L;
	// Set by Message constructor
	private String dest;
	private String kind;
	private Object data; // need to serialize it
	
	// Set by Message setters
	private String source;
	private int sequenceNumber;
	private boolean dupe;
	
	// Constructor
	public Message(String dest, String kind, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.data = data;
	}
	// These setters are used by MessagePasser.send, not your app
	public void set_source(String source) {
		this.source = source;
	}

	public void set_seqNum(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public void set_duplicate(boolean dupe) {
		this.dupe = dupe;
	}
	// other accessors, toString, etc as needed
	public String getDest() {
		return dest;
	}
	public String getSource() {
		return source;
	}
	public String getKind() {
		return kind;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public Object getData() {
		return data;
	}
	
	public String getDupe(){
		if (this.dupe)
			return "true";
		else
			return "false";
	}
	
	@Override
	public String toString() {
		return "Message [dest=" + dest + ", kind=" + kind + ", data=" + data
				+ ", source=" + source + ", sequenceNumber=" + sequenceNumber
				+ ", dupe=" + dupe + "]";
	}
	

	
}