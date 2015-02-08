/**
 * The MessagePasser Node Configuration Class, contains Node'name, ip address, listen port number
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */
package snake;

public class MPnode { 
	public String name;
	public String ip;
	public int port;
	
	/**
	 * Constructor
	 * @param name string
	 * @param ip string
	 * @param port number   
	 * */
	public MPnode(String name,String ip, int port){
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public String toString(){
		return "name:"+name+" ip:"+ip+" port:"+ port;
	}
}
