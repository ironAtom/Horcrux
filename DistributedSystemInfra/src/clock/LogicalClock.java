/**
 * The Logical Clock class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package clock;

import message.TimeStampedMessage;

public class LogicalClock extends ClockService {
	static private ClockType clockType = ClockType.LOGICAL_CLOCK;
	private int counter;
	private int increment;
	
	public LogicalClock() {
		// initialize counter
		counter = 0;
		// specify increment
		increment = 1;
	}

	// apply send timestamp scheme
	public synchronized TimeStamp getSendTimeStamp() {
		counter += increment;
		int[] value = {counter};
		return new TimeStamp(value, clockType);
	}

	// apply recv timestamp scheme
	public synchronized TimeStamp getRecvTimeStamp(TimeStampedMessage tsMsg) {
		
		counter = Integer.max(counter, tsMsg.getTimeStamp().getValue()[0]) + increment;
		int[] value = {counter};
		return new TimeStamp(value, clockType);
	}

	// apply general timestamp scheme
	public synchronized TimeStamp issueTimestamp() {
		return getSendTimeStamp();
	}

	@Override
	public TimeStamp getRecvGroupTimeStamp(TimeStampedMessage tsMsg) {
		// TODO Auto-generated method stub
		return null;
	}

}
