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
import java.util.ArrayList;
import java.util.List;

import message.GroupTimeStampedMessage;
import message.Message;
import message.TimeStampedMessage;
import server.MultiThreadedServer;
import server.WorkerRunnable;
import snake.Configure;
import snake.Group;
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
	private List<Group> groups;
	private ClockService[] groupClockService;

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
		commUtil = new CommUtil(configuration_filename, clockService, this);
		
		
		// inital group service
		List<Group> allgroups = conf.getGroups(configuration_filename);
		
		groups = new ArrayList<Group>();
		 
		//count how many groups contain this node
		for(int i=0; i<allgroups.size(); i++) {
			Group tmpGrp = allgroups.get(i);
			List<String> mbs = tmpGrp.getMembers();
			for(String m : mbs){
				if(m.equalsIgnoreCase(localName)){ // is in the group
					groups.add(tmpGrp); //add group that contains this node
				}
			}
		}
		
		groupClockService = new ClockService[groups.size()];
		
		for (int i = 0; i < groups.size(); i++ ){
			Group grp = groups.get(i);
			List<String> members = grp.getMembers();
			int inGroupPosition = 0;
			for (int j = 0; j < members.size(); j++){
				String member = members.get(j);
				if(member.equalsIgnoreCase(localName)){
					inGroupPosition = j;
					break;
				}
			}
			groupClockService[i] = new VectorClock(members.size(), inGroupPosition);
		}
		
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
	
	public TimeStamp getRecvGroupTimeStamp(GroupTimeStampedMessage tsMsg) {
		return tsMsg.getReceiveTimeStamp();
	}
	
	public String getLocalName(){
		return localName;
	}
	
	// Check send rules and apply specific sending
	public void send(Message message) {
		
		// should set group timestamp for the msg
		GroupTimeStampedMessage tsMsg = (GroupTimeStampedMessage)message;
		
		
		// check if multicast
		boolean isGroupMsg = false;
		for(int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			if(tsMsg.getDest().equalsIgnoreCase(group.getName())) {
				isGroupMsg = true;
				tsMsg.setOriginalSender(localName);
				multicastMsg(tsMsg,i);
				sequenceNumber++;//seq number only increase once each multicast
//				// set group field
//				tsMsg.setGroup(group.getName());
//				List<String> members = group.getMembers();
//				for(String member:members) {
//					tsMsg.setDest(member);
//					tsMsg.setGroupTimeStamp();
//					setSendMsg(tsMsg);
//				}
				break;
			}
		}
		// regular message
		if(!isGroupMsg) {
			setSendMsg(tsMsg);
			sequenceNumber++;
		}
		
	}

	public void multicastMsg(GroupTimeStampedMessage message, int groupId) {
		TimeStamp groupTimeStamp = groupClockService[groupId].getSendTimeStamp();
		Group group = groups.get(groupId);
		List<String> members = group.getMembers();
		// Check send rules
		Configure conf = new Configure();
		List<Rule> rules = conf.getSendRules(configurationFilename);
					
	
		for (String member : members){
			message.setDest(member);
			message.setGroup(groupId);
			message.setGroupTimeStamp(groupTimeStamp);
			message.set_source(localName);
			message.set_duplicate(false);
			message.set_seqNum(sequenceNumber);
			
			System.out.println("send message:" + message);
			
			String sendRule = "";
			TimeStampedMessage tsDelayMessage = null;
			for (Rule r : rules ) {	
				if (r.matchRule(message)){
					sendRule = r.action;
					break;
				}
			}

			switch(sendRule.toLowerCase()) {
			case "duplicate":

				sendToDest(message);

				// check delay message
				while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
					sendToDest(tsDelayMessage);

				message.set_duplicate(true);
				sendToDest(message);

				// check delay message
				while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
					sendToDest(tsDelayMessage);
				break;
			case "delay":	
				commUtil.updateOutDelayQueue(message, true); // add message to delay queue
				break;
			case "drop":
				break;
			default:
				sendToDest(message);

				// check delay message
				while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
					sendToDest(tsDelayMessage);
				break;
			}
		}
	}
	
	//relay multicast msg
	public void reMulticastMsg(GroupTimeStampedMessage message, int groupId) {
		//TimeStamp groupTimeStamp = groupClockService[groupId].getSendTimeStamp(); // no timestamp change
		Group group = groups.get(groupId);
		List<String> members = group.getMembers();
		// Check send rules
		Configure conf = new Configure();
		List<Rule> rules = conf.getSendRules(configurationFilename);
					
	
		for (String member : members){
			if(!member.equalsIgnoreCase(localName)){
				message.setDest(member);
				message.set_source(localName);
				message.set_duplicate(false);
				
				//System.out.println("re Multicast message:" + message);
				
				String sendRule = "";
				TimeStampedMessage tsDelayMessage = null;
				for (Rule r : rules ) {	
					if (r.matchRule(message)){
						sendRule = r.action;
						break;
					}
				}

				switch(sendRule.toLowerCase()) {
				case "duplicate":

					sendToDest(message);

					// check delay message
					while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
						sendToDest(tsDelayMessage);

					message.set_duplicate(true);
					sendToDest(message);

					// check delay message
					while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
						sendToDest(tsDelayMessage);
					break;
				case "delay":	
					commUtil.updateOutDelayQueue(message, true); // add message to delay queue
					break;
				case "drop":
					break;
				default:
					sendToDest(message);

					// check delay message
					while((tsDelayMessage = (TimeStampedMessage)commUtil.updateOutDelayQueue(null, false)) != null)
						sendToDest(tsDelayMessage);
					break;
				}
			}
		}
	}

	// set message
	private void setSendMsg(Message message) {
		GroupTimeStampedMessage tsMsg = (GroupTimeStampedMessage)message;
		
		tsMsg.setTimeStamp(clockService.getSendTimeStamp());
		tsMsg.set_source(localName);
		tsMsg.set_seqNum(sequenceNumber);
		tsMsg.set_duplicate(false);
		
		System.out.println("sent message: "+tsMsg);

		// Check send rules
		Configure conf = new Configure();
		List<Rule> rules = conf.getSendRules(configurationFilename);
		String sendRule = "";
		TimeStampedMessage tsDelayMessage = null;
		for (Rule r : rules ) {	
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
				outStream.writeObject((GroupTimeStampedMessage)message); 
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

	public Message getNextDeliverMsg(List<Message> holdbackQueue){
		
		for (Message msg : holdbackQueue){
			GroupTimeStampedMessage gMsg = (GroupTimeStampedMessage)msg;
			TimeStamp msgTs = gMsg.getGroupTimeStamp();
			int[] msgTsVector = msgTs.getValue();
			int group = gMsg.getGroup();
			
			VectorClock vec = (VectorClock)(groupClockService[group]);
			int[] recvVector = vec.getVector();
//			int pos = vec.getPosition();
			
			String orgSender = gMsg.getOriginalSender();
			Group grp = groups.get(group);
			int pos = grp.getInGroupPosition(orgSender);
			if(msgTsVector[pos] == recvVector[pos]+1){
				boolean isBefore = true;
				for (int i = 0; i < msgTsVector.length; i++){
					if (i != pos ){
						if (msgTsVector[i] > recvVector[i]){
							isBefore = false;
							break;
						}
					}
				}
				if (isBefore){
					//recvVector[pos] += 1; // increment vector
					TimeStamp ts = vec.getRecvGroupTimeStamp(gMsg);
					System.out.println("TS:"+ ts);
					gMsg.setReceiveTimeStamp(ts);;//increment group vector clock
					return gMsg;
				}
			}
			
		}
		return null;
	}
}
