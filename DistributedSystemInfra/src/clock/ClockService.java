/**
 * The ClockService class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package clock;

import message.TimeStampedMessage;

public abstract class ClockService {
	
	// For message use
	public abstract TimeStamp getSendTimeStamp();
	public abstract TimeStamp getRecvTimeStamp(TimeStampedMessage tsMsg);
	// For general purpose
	public abstract TimeStamp issueTimestamp();
	
}
