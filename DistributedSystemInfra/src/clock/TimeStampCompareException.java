/**
 * The timestamp compare exception class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package clock;

public class TimeStampCompareException extends Exception {
	private int type;
	
	
	public TimeStampCompareException(int type) {
		this.type = type;
	}


	@Override
	public String toString() {		
		if(type == 0) {
			return "TimeStampCompareException: Cannot compare timestamp using different clock type\n";
		} else {
			return "TimeStampCompareException: hard to determine event sequence for logical clock\n";
		}
	}
	
	
}
