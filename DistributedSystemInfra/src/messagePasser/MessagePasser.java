/**
 * The MessagePasser parse the configuration file and set up sockets for 
 * communicating with all processes listed in the configuration section of 
 * the file
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */

package messagePasser;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import message.Message;
import message.TimeStampedMessage;
import server.MultiThreadedServer;
import server.WorkerRunnable;
import snake.Configure;
import snake.MPnode;
import snake.Rule;
import clock.ClockService;
import clock.ClockType;
import clock.LogicalClock;
import clock.TimeStamp;
import clock.VectorClock;

public class MessagePasser {
	private String configurationFilename;
	private String localName;
	private int sequenceNumber;
	private CommUtil commUtil;
	private List<MPnode> configurations;
	private ClockService clockService;

	// Constructor
	public MessagePasser(String configuration_filename, String local_name, ClockType clockType) {
		// initialize 
		configurationFilename = configuration_filename;
		localName = local_name;
		sequenceNumber = 0;

		// Read configuration file, find local_name and setup listen socket
		int portNumber = 0;
		Configure conf = new Configure();
		boolean isFound = false;
		configurations = conf.getMPnodes(configuration_filename);
		int position = 0;
		for(MPnode node: configurations) {
			if(node.name.equals(localName)) {
				portNumber = node.port;
				isFound = true;
				break;
			}
			position++;
		}
		if(!isFound) {
			System.out.println("Name not found! Exit.");
			System.exit(1);
		}

		// instantiate a ClockService
		int size = configurations.size();
		if(clockType == ClockType.LOGICAL_CLOCK) {
			clockService = new LogicalClock();
		} else {
			clockService = new VectorClock(size, position);			
		}
		commUtil = new CommUtil(configuration_filename, clockService);
		
		
		// Open the server thread doing accept job
		MultiThreadedServer server = new MultiThreadedServer(portNumber, commUtil);
		new Thread(server).start();
	}

	// Check receive message queue and return message if not empty
	public Message receive() { 
	
		// Get message from incoming message queue
		Message message = commUtil.updateIncomingMessages(null , false);
		return message;
	}

	// Set client socket
	private Socket setClientSocket(String hostName, int portNumber) {
		Socket clientSocket = null;	
		try {
			// Create a socket between the client and the server
			clientSocket = new Socket(hostName, portNumber);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(ConnectException e) {
			System.out.println("Target can not be reached.");
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return clientSocket;
	}

	public TimeStamp issueTimeStamp() {
		return clockService.issueTimestamp();
	}
	
	public TimeStamp getRecvTimeStamp(TimeStampedMessage tsMsg) {
		return clockService.getRecvTimeStamp(tsMsg);
	}
	
	// Check send rules and apply specific sending
	public void send(Message message) {
		
		// should set timestamp for the msg
		TimeStampedMessage tsMsg = (TimeStampedMessage)message;
		tsMsg.setTimeStamp(clockService.getSendTimeStamp());
		
		tsMsg.set_source(localName);
		tsMsg.set_seqNum(sequenceNumber);
		sequenceNumber++;
		tsMsg.set_duplicate(false);
		
		System.out.println("sent message: "+tsMsg);

		// Check send rules
		Configure conf = new Configure();
		List<Rule> rules = conf.getSendRules(configurationFilename);
		String sendRule = "";
		TimeStampedMessage tsDelayMessage = null;
		for (Rule r : rules ){	
			if (r.matchRule(tsMsg)){
				sendRule = r.action;
				break;
			}
		}

		switch(sendRule.toLowerCase()) {
		case "duplicate":

			sendToDest(tsMsg);

			// check delay message
			while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
				sendToDest(tsDelayMessage);

			tsMsg.set_duplicate(true);
			sendToDest(tsMsg);

			// check delay message
			while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
				sendToDest(tsDelayMessage);
			break;
		case "delay":	
			commUtil.updateOutDelayQueue(tsMsg, true); // add message to delay queue
			break;
		case "drop":
			break;
		default:
			sendToDest(tsMsg);

			// check delay message
			while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
				sendToDest(tsDelayMessage);
			break;
		}
	}

	// Actually send to dest
	private void sendToDest(Message message) {
		String dest = message.getDest();

		ObjectOutputStream outStream = null;
		SocketPairs socketPair = commUtil.updateNameSocketMap(dest, null, false);
		// Set up a new client socket if necessary
		String hostName = null;
		int portNumber = 0;
		if(socketPair == null) {
			
			for(MPnode node: configurations) {
				if(node.name.equals(dest)) {
					hostName = node.ip;
					portNumber = node.port;
					break;
				}
			}
			Socket clientSocket = setClientSocket(hostName, portNumber);
			if(clientSocket == null)
				return;

			try {
				outStream = new ObjectOutputStream(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			SocketPairs socketPairs = new SocketPairs(clientSocket, outStream);
			commUtil.updateNameSocketMap(dest, socketPairs, true);

			// add a worker thread to listen
			new Thread(
					new WorkerRunnable(
							clientSocket, commUtil)
					).start();			

		} else {
			if(socketPair.getOutStream() == null) {
				try {
					outStream = new ObjectOutputStream(socketPair.getSocket().getOutputStream());
					socketPair.setOutStream(outStream);
				} catch(SocketException e) {
					// The other side failed silently
					for(MPnode node: configurations) {
						if(node.name.equals(dest)) {
							hostName = node.ip;
							portNumber = node.port;
							break;
						}
					}
					
					Socket clientSocket = setClientSocket(hostName, portNumber);
					if(clientSocket == null)
						return;
		
					try {
						outStream = new ObjectOutputStream(clientSocket.getOutputStream());
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					SocketPairs socketPairs = new SocketPairs(clientSocket, outStream);
					commUtil.updateNameSocketMap(dest, socketPairs, true);
		
					// add a worker thread to listen
					new Thread(
							new WorkerRunnable(
									clientSocket, commUtil)
							).start();	
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				outStream = socketPair.getOutStream();
			}
		}

		boolean isFail = true;
		while(isFail) {
			// write the message
			try {
				outStream.writeObject((TimeStampedMessage)message); 
				outStream.flush();
				outStream.reset(); // in case stream caches serialized object
				isFail = false;
			} catch(SocketException e) {
				// The other side failed silently
				for(MPnode node: configurations) {
					if(node.name.equals(dest)) {
						hostName = node.ip;
						portNumber = node.port;
						break;
					}
				}
				
				Socket clientSocket = setClientSocket(hostName, portNumber);
				if(clientSocket == null)
					return;
	
				try {
					outStream = new ObjectOutputStream(clientSocket.getOutputStream());
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				SocketPairs socketPairs = new SocketPairs(clientSocket, outStream);
				commUtil.updateNameSocketMap(dest, socketPairs, true);
	
				// add a worker thread to listen
				new Thread(
						new WorkerRunnable(
								clientSocket, commUtil)
						).start();			
			}
			catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}
