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
	private List<Message> receive;
	private List<Message> coHoldbackQueue;
	private Map<String, SocketPairs> nameSocketMap;
	private List<Message> outgoingMessagesDelay;
	private List<Message> incomingMessagesDelay;
	private String configurationFilename;
	private ClockService clockService;
	private MessagePasser msgPasser;
	
	// Constructor
	public CommUtil(String configurationFilename, ClockService clockService, MessagePasser msgPasser) {
		incomingMessages = new LinkedList<Message>();
		receive =  new LinkedList<Message>();
		coHoldbackQueue = new LinkedList<Message>();
		nameSocketMap = new HashMap<String, SocketPairs>();
		outgoingMessagesDelay = new LinkedList<Message>();
		incomingMessagesDelay = new LinkedList<Message>();
		this.configurationFilename = configurationFilename;
		this.clockService = clockService; 
		this.msgPasser = msgPasser;
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
	
	//update Reliable recieve queue
	public synchronized Message updateReceive(Message message, boolean isAdd){
		if(isAdd) {
			receive.add(message);
			return null;
		} else {
			if(!receive.isEmpty())
				return receive.remove(0);
			else 
				return null;
		}
	}
	
	public synchronized Message updateCoHoldbackQueue(Message message, boolean isAdd){
		if(isAdd) {
			coHoldbackQueue.add(message);
			return null;
		} else {
			if(!coHoldbackQueue.isEmpty()){
				 coHoldbackQueue.remove(message);
				 return message;
			}
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

	public MessagePasser getMsgPasser() {
		return msgPasser;
	}
	
	public List<Message> getReceive() {
		return receive;
	}
	
	public List<Message> getCoHoldbackQueue(){
		return coHoldbackQueue;
	}

	
	
}
