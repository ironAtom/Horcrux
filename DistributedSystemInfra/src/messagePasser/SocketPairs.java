/**
 * The Socket OutputStream Class to Map socket to specific outputstream
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */
package messagePasser;

import java.io.ObjectOutputStream;
import java.net.Socket;


public class SocketPairs {
	private ObjectOutputStream  outStream;
	private Socket socket;
	
	// Constructor
	public SocketPairs(Socket socket, ObjectOutputStream outStream) {
		this.socket = socket;
		this.outStream = outStream;
	}

	// Getters
	public ObjectOutputStream getOutStream() {
		return outStream;
	}

	public Socket getSocket() {
		return socket;
	}

	// Setters
	public void setOutStream(ObjectOutputStream outStream) {
		this.outStream = outStream;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	

}
