/**
 * The multiThread worker for receiving messages and put the
 *  recieved message into the queue
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import clock.TimeStamp;
import clock.VectorClock;
import message.GroupTimeStampedMessage;
import message.Message;
import message.TimeStampedMessage;
import messagePasser.CommUtil;
import messagePasser.MessagePasser;
import messagePasser.SocketPairs;
import snake.Configure;
import snake.Rule;

public class WorkerRunnable implements Runnable{

	protected Socket clientSocket;
	protected CommUtil commUtil;

	public WorkerRunnable(Socket clientSocket, CommUtil commUtil) {
		this.clientSocket = clientSocket;
		this.commUtil = commUtil;
	}

	public void run() {
		// Open a ObjectInputStream on the socket
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch(SocketException e) {
			return;
		} catch (EOFException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		GroupTimeStampedMessage tsMsg = null;
		Configure conf = new Configure();
		List<Rule> rules;
		String source = null;
		while (true) {
			try {
				//if(in.available() > 1) {
				tsMsg = (GroupTimeStampedMessage)in.readObject();
				// Try to add the socket to the map pair
				source = tsMsg.getSource();
				SocketPairs socketPairs = new SocketPairs(clientSocket, null);
				commUtil.updateNameSocketMap(source, socketPairs, true);

				// Check receive rules
				rules = conf.getReceiveRules(commUtil.getConfigurationFilename());
				String recvRule = "";
				GroupTimeStampedMessage tsDelayMessage = null;
				for (Rule r : rules ){	
					if (r.matchRule(tsMsg)){
						recvRule = r.action;
						break;
					}
				}
				
				if (tsMsg.getGroup() == -1) {
					switch(recvRule.toLowerCase()) { // ordinary message
					case "duplicate":
						// deliver one to the queue
						// should set timestamp for the msg recved
						// tsMsg.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsMsg));
						commUtil.updateIncomingMessages(tsMsg, true);
						// check all delay messages
						while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
							//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
							commUtil.updateIncomingMessages(tsDelayMessage, true);
						}
						// deliver one to the queue
						//tsMsg.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsMsg));
						commUtil.updateIncomingMessages(tsMsg, true);
						// check all delay messages
						while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
							//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
							commUtil.updateIncomingMessages(tsDelayMessage, true);
						}
						break;
					case "delay":	
						commUtil.updateInDelayQueue(tsMsg, true); // add message to delay queue
						break;
					case "drop":
						break;
					default:
						//tsMsg.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsMsg));
						commUtil.updateIncomingMessages(tsMsg, true);	
						while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
							//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
							commUtil.updateIncomingMessages(tsDelayMessage, true);
						}
						break;
					}
				}
				else { //multicast message
					switch(recvRule.toLowerCase()) {
					case "duplicate":
						// deliver one to the queue
						// should set timestamp for the msg recved
						// tsMsg.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsMsg));
						List<Message> recvQueue = commUtil.getReceive();
						int i = 0;
						for(; i < recvQueue.size(); i++){
							GroupTimeStampedMessage gMsg = (GroupTimeStampedMessage)recvQueue.get(i);
							if(gMsg.isSameMsg(tsMsg))
								break;
						}
						
						if (i >= recvQueue.size()){//tsMsg is not received before
							commUtil.updateReceive(tsMsg, true);//add tsMsg to Receive
							
							if (!tsMsg.getOriginalSender().equalsIgnoreCase(commUtil.getMsgPasser().getLocalName())){
								//if is not original sender, multicast the message
								GroupTimeStampedMessage copyMsg = new GroupTimeStampedMessage(tsMsg);
								commUtil.getMsgPasser().reMulticastMsg(copyMsg, copyMsg.getGroup());
							}
							//add to CO-holdback queue
							commUtil.updateCoHoldbackQueue(tsMsg, true);
							
							Message msg = null;
							while((msg = commUtil.getMsgPasser().getNextDeliverMsg(commUtil.getCoHoldbackQueue())) != null){//deliver m
								commUtil.updateCoHoldbackQueue(msg, false);//remove from hold back queue
								
								commUtil.updateIncomingMessages(msg, true); // add to userinput Queue
								//msg = commUtil.getMsgPasser().getNextDeliverMsg(commUtil.getCoHoldbackQueue());
								
								while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
									//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
									commUtil.updateIncomingMessages(tsDelayMessage, true);
								}
								
								commUtil.updateIncomingMessages(msg, true); // add to userinput Queue
								//msg = commUtil.getMsgPasser().getNextDeliverMsg(commUtil.getCoHoldbackQueue());
								
								while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
									//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
									commUtil.updateIncomingMessages(tsDelayMessage, true);
								}
							}
							
							
						}
						break;
					case "delay":	
						commUtil.updateInDelayQueue(tsMsg, true); // add message to delay queue
						break;
					case "drop":
						break;
					default:
						//tsMsg.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsMsg));
						
//						if ((!copyMsg.getOriginalSender().equalsIgnoreCase(commUtil.getMsgPasser().getLocalName()))  
//						&& copyMsg.getOriginalSender().equalsIgnoreCase(copyMsg.getSource())){
//							commUtil.getMsgPasser().reMulticastMsg(copyMsg, copyMsg.getGroup());
//						}
						List<Message> recvQueue1 = commUtil.getReceive();
						int i1 = 0;
						for(; i1 < recvQueue1.size(); i1++){
							GroupTimeStampedMessage gMsg = (GroupTimeStampedMessage)recvQueue1.get(i1);
							if(gMsg.isSameMsg(tsMsg))
								break;
						}
						
						if (i1 >= recvQueue1.size()){//tsMsg is not received before
							commUtil.updateReceive(tsMsg, true);//add tsMsg to Receive
							
							if (!tsMsg.getOriginalSender().equalsIgnoreCase(commUtil.getMsgPasser().getLocalName())){
								//if is not original sender, multicast the message
								GroupTimeStampedMessage copyMsg = new GroupTimeStampedMessage(tsMsg);
								commUtil.getMsgPasser().reMulticastMsg(copyMsg, copyMsg.getGroup());
							}
							//add to CO-holdback queue
							commUtil.updateCoHoldbackQueue(tsMsg, true);
							
							Message msg = null;
							while((msg = commUtil.getMsgPasser().getNextDeliverMsg(commUtil.getCoHoldbackQueue())) != null){//deliver m
								commUtil.updateCoHoldbackQueue(msg, false);//remove from hold back queue
								
								commUtil.updateIncomingMessages(msg, true); // add to userinput Queue
								//msg = commUtil.getMsgPasser().getNextDeliverMsg(commUtil.getCoHoldbackQueue());
								
								while((tsDelayMessage = (GroupTimeStampedMessage)commUtil.updateInDelayQueue(null, false)) != null) {
									//tsDelayMessage.setTimeStamp(commUtil.getClockService().getRecvTimeStamp(tsDelayMessage));
									commUtil.updateIncomingMessages(tsDelayMessage, true);
								}
							}
							
							
						}
						
						break;
					}
				}
				
			} catch(EOFException e) {
				// connection closed, clear socket pairs
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					clientSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				commUtil.updateNameSocketMap(source, null, true);
				return;
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//
		} 
	} 

	
}