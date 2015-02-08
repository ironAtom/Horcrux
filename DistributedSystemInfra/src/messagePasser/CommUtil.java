/**
 * Communication Utility Class for maintaining shared input queue
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */

package messagePasser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import message.Message;
import clock.ClockService;

// Class that contains a incoming message queue and a name socket map
public class CommUtil {
	private List<Message> incomingMessages;
	private Map<String, SocketPairs> nameSocketMap;
	private List<Message> outgoingMessagesDelay;
	private List<Message> incomingMessagesDelay;
	private String configurationFilename;
	private ClockService clockService;
	
	// Constructor
	public CommUtil(String configurationFilename, ClockService clockService) {
		incomingMessages = new LinkedList<Message>();
		nameSocketMap = new HashMap<String, SocketPairs>();
		outgoingMessagesDelay = new LinkedList<Message>();
		incomingMessagesDelay = new LinkedList<Message>();
		this.configurationFilename = configurationFilename;
		this.clockService = clockService; 
	}
	
	// Update incoming message delay queue
	public synchronized Message updateInDelayQueue(Message message, boolean isAdd) {
		if(isAdd) {
			incomingMessagesDelay.add(message);
			return null;
		} else {
			if(!incomingMessagesDelay.isEmpty())
				return incomingMessagesDelay.remove(0);
			else 
				return null;
		}
	}
	
	// Update outgoing message delay queue
	public synchronized Message updateOutDelayQueue(Message message, boolean isAdd) {
		if(isAdd) {
			outgoingMessagesDelay.add(message);
			return null;
		} else {
			if(!outgoingMessagesDelay.isEmpty())
				return outgoingMessagesDelay.remove(0);
			else 
				return null;
		}
	}
	
	// Update name socket map
	public synchronized SocketPairs updateNameSocketMap(String name, SocketPairs socketPairs, boolean isAdd) {

		boolean containsKey = nameSocketMap.containsKey(name);
		if(isAdd) {
			// try to add the name, socket map
			if(!containsKey)
				nameSocketMap.put(name, socketPairs);
			return null;
		} else {
			if(containsKey)
				return nameSocketMap.get(name);
			else
				return null;
		}
	}
	
	// Update incoming message delay queue
	public synchronized Message updateIncomingMessages(Message message, boolean isAdd) {
		if(isAdd) {
			incomingMessages.add(message);
			return null;
		} else {
			if(!incomingMessages.isEmpty()) {
				return incomingMessages.remove(0);
			}
			else
				return null;
		}
	}

	// Get configuration filename
	public synchronized String getConfigurationFilename() {
		return configurationFilename;
	}

	public synchronized ClockService getClockService() {
		return clockService;
	}
	
}