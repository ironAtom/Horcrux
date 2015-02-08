/**
 * The Send/Receive Rule Class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */

package snake;

import message.Message;

public class Rule { //Send/Receive Rule Class
	public String action = null;
    public String src = null;
    public String dest = null;
    public String kind = null;
    public Integer seqNum = null;
    public String duplicate = null;
    //message will have field duplicate
    
    /**
	 * determine if the message match the specific rule
	 * @param Message
	 * @return true if match, otherwise false 
	 * */
    public boolean matchRule(Message message){
    	if (action == null)
    		return false;
    	if (src != null && !src.equalsIgnoreCase(message.getSource()))
    		return false;
    	if (dest != null && !dest.equalsIgnoreCase(message.getDest()))
    		return false;
    	if (kind != null && !kind.equalsIgnoreCase(message.getKind()))
    		return false;
    	if (seqNum != null && !seqNum.equals(message.getSequenceNumber()))
    		return false;
    	if (duplicate != null && !duplicate.equalsIgnoreCase(message.getDupe()))
    		return false;
    	return true;
    }
    
    public String toString(){
    	return"action:"+ action +" src:"+ src +" dest:"+ dest +" kind:"+ kind +" seqNum:" + seqNum;
    }
}
