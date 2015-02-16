package message;

import clock.TimeStamp;

public class GroupTimeStampedMessage extends TimeStampedMessage {
	
	private TimeStamp groupTimeStamp; // add a field
	private int group = -1;
	private String originalSender;
	
	public GroupTimeStampedMessage(String dest, String kind, Object data) {
		super(dest, kind, data);
	}
	
	public GroupTimeStampedMessage(GroupTimeStampedMessage another) {
		super(another.getDest(), another.getKind(), another.getData());
		this.originalSender = another.originalSender;
		this.groupTimeStamp = another.getGroupTimeStamp();
		this.timeStamp = another.getTimeStamp();
		this.group = another.getGroup();
		this.set_source(another.getSource());
		this.set_seqNum(another.getSequenceNumber());
		if("True".equalsIgnoreCase(another.getDupe()))
			this.set_duplicate(true);
		else
			this.set_duplicate(false);
	}
	

	public TimeStamp getGroupTimeStamp() {
		return groupTimeStamp;
	}
	
	
	public String getOriginalSender(){
		return originalSender;
	}
	
	public void setOriginalSender(String sender){
		this.originalSender = sender;
	}

	public void setGroupTimeStamp(TimeStamp groupTimeStamp) {
		this.groupTimeStamp = groupTimeStamp;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}
	
	

	@Override
	public String toString() {
		
		return super.toString() +" Original Sender: "+ originalSender + " Group:"+ group +" Group TS: " + groupTimeStamp;
	}
	
	public boolean isSameMsg(GroupTimeStampedMessage gMsg){
		if (this.originalSender.equalsIgnoreCase(gMsg.getOriginalSender())&&
				this.getSequenceNumber() == gMsg.getSequenceNumber())//use sender and seqNum to identify the message
			return true;
		return false;
	}
	
}
