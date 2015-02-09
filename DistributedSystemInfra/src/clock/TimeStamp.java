/**
 * The Timestamp class
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Sun Feb  8 20:55:29 EST 2015
 * */
package clock;

import java.io.Serializable;
import java.util.Arrays;

public class TimeStamp implements Serializable{
	private int[] value;
	private ClockType clockType; // LogicalClock or VectorClock
	
	public TimeStamp(int[] value, ClockType clockType) {
		this.value = value;
		this.clockType = clockType;
	}
	
	public ClockType getClockType() {
		return clockType;
	}
	
	public int[] getValue() {
		return value;
	}
	
	// According to clockType, return result 
	
	public boolean isHappenedBefore(TimeStamp timeStamp) throws TimeStampCompareException {
		if(clockType != timeStamp.getClockType()) {
			throw new TimeStampCompareException(0);
		}
		
		// 0 for "happened before", 1 for not "happened before"
		// Compare logical clock
		if(clockType == ClockType.LOGICAL_CLOCK) {
			throw new TimeStampCompareException(1);
		} else {
			// Compare Vector clock
			int[] anotherValue = timeStamp.getValue();
			boolean isHappenedBefore = true;
			int i;
			for(i=0; i<value.length; i++) {
				isHappenedBefore &= value[i]<=anotherValue[i];
			}
			return isHappenedBefore;
		}
	}

	@Override
	public String toString(){
		return "TimeStamp [value=" + Arrays.toString(value) + ", clockType="
				+ clockType + "]";
	}

	public int compareTo(TimeStamp o) throws TimeStampCompareException {
		if(clockType != o.getClockType()) {
			throw new TimeStampCompareException(0);
		}
		
		// 0 for "happened before", 1 for not "happened before"
		// Compare logical clock
		if(clockType == ClockType.LOGICAL_CLOCK) {
			int[] anotherValue = o.getValue();
			return Integer.compare(value[0], anotherValue[0]);
		} else {
			// Compare Vector clock
			int[] anotherValue = o.getValue();
			int before = 1;
			int i;
			for(i=0; i<value.length; i++) {
				if (value[i] > anotherValue[i]){
					before = 0;
					break;
				}
			}
			
			if (before == 1){
				return -1;
			}
			
			int after = 1;
			for(i=0; i<value.length; i++) {
				if (value[i] < anotherValue[i]){
					after = 0;
					break;
				}
			}
			
			if (after == 1)
				return 1;
			else
				return 0; //concurrent
		}
	}
}
