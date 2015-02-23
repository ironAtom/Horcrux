/**
 * The Vector Clock class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package clock;

import java.util.Arrays;

import message.GroupTimeStampedMessage;
import message.TimeStampedMessage;

public class VectorClock extends ClockService {
	static private ClockType clockType = ClockType.VECTOR_CLOCK;
	private int[] counterVector;
	private int increment;
	private int position;
	

	// Constructor
	// The VectorClock size is based on the number of names in the config file
	// position is based on the process's name's sequence in the config file
	public VectorClock(int size, int position) {
		// initialize counter vector
		counterVector = new int[size];
		// specify position
		this.position = position;
		// specify increment
		increment = 1;
	}

	// apply send time stamp scheme
	public synchronized TimeStamp getSendTimeStamp() {
		counterVector[position] += increment;
		int[] value = Arrays.copyOf(counterVector, counterVector.length);
		return new TimeStamp(value, clockType);
	}

	// apply recv time stamp scheme
	public synchronized TimeStamp getRecvTimeStamp(TimeStampedMessage message) {
		GroupTimeStampedMessage tsMsg = (GroupTimeStampedMessage)message;//converto gpTsMsg
		counterVector[position] += increment;
		
		int[] value = tsMsg.getTimeStamp().getValue();
		int i;
		for(i=0; i<counterVector.length; i++) {
			counterVector[i] = Integer.max(counterVector[i], value[i]);
		}
		
		value = Arrays.copyOf(counterVector, counterVector.length);
		return new TimeStamp(value, clockType);

	}
	
	public synchronized TimeStamp getRecvGroupTimeStamp(TimeStampedMessage message) {
		GroupTimeStampedMessage tsMsg = (GroupTimeStampedMessage)message;//converto gpTsMsg
		//counterVector[position] += increment;
		
		int[] value = tsMsg.getGroupTimeStamp().getValue();
		int i;
		for(i=0; i<counterVector.length; i++) {
			counterVector[i] = Integer.max(counterVector[i], value[i]);
		}
		
		value = Arrays.copyOf(counterVector, counterVector.length);
		return new TimeStamp(value, clockType);

	}

	// apply general timestamp scheme
	public synchronized TimeStamp issueTimestamp() {
		return getSendTimeStamp();
	}
	
	
	public synchronized int[] getVector(){
		return counterVector;
	}
	public synchronized int getPosition(){
		return this.position;
	}
	
}

