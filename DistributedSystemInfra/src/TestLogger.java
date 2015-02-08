import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import log.Logger;
import messagePasser.MessagePasser;
import clock.ClockType;
import clock.TimeStampCompareException;


public class TestLogger {
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
		Logger log = new Logger();

		boolean isQuit = false;

		while (!isQuit) {
			System.out.println("1) log 2) quit");
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
					case "log":
						try {
							log.makeLog(mp);
						} catch (TimeStampCompareException e) {
							System.out.println(e.toString());
						}
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
