package message;

import clock.ClockType;
import clock.TimeStamp;
import clock.TimeStampCompareException;


public class TimeStampedMessage extends Message{
	TimeStamp timeStamp; // new field
	
	
	// Constructor
	public TimeStampedMessage(String dest, String kind, Object data) {
		super(dest, kind, data);
	}
	
	public void setTimeStamp(TimeStamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public TimeStamp getTimeStamp() {
		return timeStamp;
	}
	
	
	// comparison methods of the Timestamps
	public int compareTimeStampedMessage() {
		
		return 0;
	}

	@Override
	public String toString() {
		
		return super.toString() + " " + timeStamp;
	}

	public int compareTo(TimeStampedMessage o) throws TimeStampCompareException {
		if(timeStamp.getClockType() != o.getTimeStamp().getClockType()) {
			throw new TimeStampCompareException(0);
		}
		
		// 0 for "happened before", 1 for not "happened before"
		// Compare logical clock
		if(timeStamp.getClockType() == ClockType.LOGICAL_CLOCK) {
			if (!this.getSource().equals(o.getSource()) )
				throw new TimeStampCompareException(1);
		}
		return timeStamp.compareTo(o.timeStamp);
	}
}
