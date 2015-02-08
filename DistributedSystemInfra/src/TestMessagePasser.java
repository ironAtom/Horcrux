
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import message.TimeStampedMessage;
import messagePasser.MessagePasser;
import clock.ClockService;
import clock.ClockType;
import clock.TimeStamp;


public class TestMessagePasser {
	
	public static void main(String[] args) {
		
		// stdin input stream
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		String configuration_filename = null;
		System.out.println("Configuration filename?");
		try {
			if((userInput = stdIn.readLine()) != null) {
				configuration_filename = userInput;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String local_name = null;
		System.out.println("Who are you?");
		try {
			if((userInput = stdIn.readLine()) != null) {
				local_name = userInput;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ClockType clockType = ClockType.LOGICAL_CLOCK;
		System.out.println("Choose clock: 0 for LogicalClock, 1 for VectorClock?");
		try {
			if((userInput = stdIn.readLine()) != null) {
				int choice = Integer.parseInt(userInput);
				if(choice!=0 && choice!=1) {
					System.out.println("Invalid choice. Exit");
					System.exit(0);
				}
				clockType = choice==0?ClockType.LOGICAL_CLOCK:ClockType.VECTOR_CLOCK;
			}
		} catch(NumberFormatException e) {
			System.out.println("Invalid choice. Exit");
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// Instantiate a MessagePasser
		MessagePasser mp = new MessagePasser(configuration_filename, local_name, clockType);

		TimeStampedMessage tsMsg = null;
		boolean isQuit = false;

		while (!isQuit) {
			System.out.println("1) send <dest> <kind> <data> 2) recv 3) issueTS 4) quit");
			System.out.print("Enter your command:");

			try {
				if((userInput = stdIn.readLine()) != null) {
					String[] parts = userInput.split(" ", 5);
					System.out.println(parts[0]);
					switch(parts[0]) {
					case "quit": 
						isQuit = true;
						System.out.println("Goodbye " + local_name);
						System.exit(0);
						break;
					case "send":
						// Check arguments #
						if(parts.length != 4) {
							System.out.println("Command Syntax error!");
							System.out.println("Send syntax should be: send <dest> <kind> <data>");
							break;
						}
						String dest = parts[1];
						String kind = parts[2];
						String data = parts[3];
						
						tsMsg = new TimeStampedMessage(dest, kind, data); 
						mp.send(tsMsg);
						break;
					case "recv":
						tsMsg = (TimeStampedMessage)mp.receive();
						if(tsMsg != null) {
							System.out.println(mp.getRecvTimeStamp(tsMsg));
							System.out.println("recved message: " + tsMsg);
						} else {
							System.out.println("No message in the queue yet.");
						}
						break;
					case "issueTS":
						TimeStamp timeStamp = mp.issueTimeStamp();
						System.out.println(timeStamp);
						break;
					default:
						System.out.println("Invalid command.");
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
