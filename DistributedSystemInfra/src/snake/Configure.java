/**
 * The Configure Class, parse YAML format configuration file
 * 
 * Author:	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 		  	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */

package snake;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.Yaml;

public class Configure {
	
	/**
	 * get a list of MessagePaser Nodes' configuration from YAML format configuration file
	 * @param configuration_filename
	 * @return a list of MPnode
	 * */
	public List<MPnode> getMPnodes(String configuration_filename){
		List<MPnode> configs = new ArrayList<MPnode>();
	    InputStream input;
		try {
			input = new FileInputStream(new File(configuration_filename));
			Yaml yaml = new Yaml();
			Map<String, Object> configObject = (Map<String, Object>) yaml.load(input);
		    List<Map<String, Object>> configList = (List<Map<String, Object>>)configObject.get("configuration");
		    for(Map<String, Object> node :configList){
		    	String name = (String)node.get("name");
		    	String ip = (String)node.get("ip");
		    	int port = (Integer)node.get("port");
		    	MPnode newNode = new MPnode(name,ip,port);
		    	configs.add(newNode);
		    }
		} catch (FileNotFoundException e) {
			System.out.println("Configuration file not found! Exit.");
			System.exit(1);
		}
		return configs;
	}
	
	/**
	 * get a list of send rules from YAML format configuration file
	 * @param configuration_filename
	 * @return a list of Rules
	 * */
	public List<Rule> getSendRules(String configuration_filename){
		List<Rule> rules = new ArrayList<Rule>();
	    InputStream input;
		try {
			input = new FileInputStream(new File(configuration_filename));
			Yaml yaml = new Yaml();
			Map<String, Object> ruleObject = (Map<String, Object>) yaml.load(input);
		    List<Map<String, Object>> ruleList = (List<Map<String, Object>>)ruleObject.get("sendRules");
		    if (ruleList != null){
		    	for(Map<String, Object> rule :ruleList){
			    	Rule newRule = new Rule();
			    	newRule.action = (String)rule.get("action");
			    	newRule.src = (String)rule.get("src");
			    	newRule.dest = (String)rule.get("dest");
			    	newRule.kind = (String)rule.get("kind");
			    	newRule.seqNum = (Integer)rule.get("seqNum");
			    	newRule.src = (String)rule.get("src");
			    	
			    	rules.add(newRule);
			    }
		    }
		} catch (FileNotFoundException e) {
			System.out.println("Configuration file not found! Exit.");
			System.exit(1);
		} 
		
		return rules;
	}
	
	/**
	 * get a list of receive rules from YAML format configuration file
	 * @param configuration_filename
	 * @return a list of Rules
	 * */
	public List<Rule> getReceiveRules(String configuration_filename){
		List<Rule> rules = new ArrayList<Rule>();
	    InputStream input;
		try {
			input = new FileInputStream(new File(configuration_filename));
			Yaml yaml = new Yaml();
			Map<String, Object> ruleObject = (Map<String, Object>) yaml.load(input);
		    List<Map<String, Object>> ruleList = (List<Map<String, Object>>)ruleObject.get("receiveRules");
		    if (ruleList != null){
		    	for(Map<String, Object> rule :ruleList){
			    	Rule newRule = new Rule();
			    	newRule.action = (String)rule.get("action");
			    	newRule.src = (String)rule.get("src");
			    	newRule.dest = (String)rule.get("dest");
			    	newRule.kind = (String)rule.get("kind");
			    	newRule.seqNum = (Integer)rule.get("seqNum");
			    	newRule.src = (String)rule.get("src");
			    	Object tmp = rule.get("duplicate");
			    	if (tmp != null){
			    		if ((boolean)tmp){
			    			newRule.duplicate = "true";
			    		}
			    		else{
			    			newRule.duplicate = "false";
			    		}
			    	}
			    	
			    	rules.add(newRule);
			    }
		    }
		    
		} catch (FileNotFoundException e) {
			System.out.println("Configuration file not found! Exit.");
			System.exit(1);
		}
		return rules;
	}
}
