/**
 * The Logger class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package log;

import java.util.*;

import clock.TimeStampCompareException;
import message.TimeStampedMessage;
import messagePasser.MessagePasser;


// needs to be initialized using the config file
public class Logger {
	List<TimeStampedMessage> list;
	
	
	// Constructor
	public Logger() {
		this.list = new LinkedList<TimeStampedMessage>();
	}

	public void makeLog(MessagePasser mp) throws TimeStampCompareException {
		TimeStampedMessage tMsg = null;
		while ((tMsg = (TimeStampedMessage)mp.receive())!=null){
			list.add(tMsg);
		}
		
		if (!list.isEmpty()){
			for (int i = 0; i < list.size(); i++){
				List<Integer> before = new ArrayList<Integer>();
				List<Integer> after = new ArrayList<Integer>();
				List<Integer> concurrent = new ArrayList<Integer>();
				TimeStampedMessage msg1 = list.get(i);
				for (int j = 0; j < list.size(); j++){
					if (i != j){
						TimeStampedMessage msg2 = list.get(j);
						if (msg1.compareTo(msg2) == -1){ // msg2 is after msg1
							after.add(j);
						}
						else if(msg1.compareTo(msg2) == 1){ //msg2 is before msg1
							before.add(j);
						}
						else{
							concurrent.add(j);
						}
					}
				}
				System.out.println("#"+i+": "+msg1.toString());
				System.out.println("\thappened before: "+before.toString()+"\thappened after: "+after.toString()
						+"\tconcurrent: "+concurrent.toString());
			}
		}
		
	}

	public void dumpLog() {
		
	}
	
	
	/*The log output should clearly show as many
messages in the proper order as is possible. Concurrent messages should be noted, as
well as showing the limits of this concurrency (i.e. just exactly which messages it is
concurrent with).*/
	@Override
	public String toString() {

		return null;
	
	}
	
	
	
}
