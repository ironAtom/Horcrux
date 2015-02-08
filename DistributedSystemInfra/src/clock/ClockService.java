package clock;

import message.TimeStampedMessage;

public abstract class ClockService {
	
	// For message use
	public abstract TimeStamp getSendTimeStamp();
	public abstract TimeStamp getRecvTimeStamp(TimeStampedMessage tsMsg);
	// For general purpose
	public abstract TimeStamp issueTimestamp();
	
}
