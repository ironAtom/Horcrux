package snake;

import java.util.*;

public class Group {
	private String name;
	private List<String> members;
	public Group(String name, List<String> members){
		this.name = name;
		this.members = members;
	}
	public String getName() {
		return name;
	}
	public List<String> getMembers() {
		return members;
	}
	
	public int getInGroupPosition(String name){
		int i = 0;
		for (String member: members){
			if(member.equalsIgnoreCase(name))
				return i;
			i++;
		}
		return -1;
	}
}
