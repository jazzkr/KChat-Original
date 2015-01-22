package com.codeex.masterserver;

import javax.swing.*;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.BorderLayout;
import java.lang.Math;

public class MasterServer extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "v1.00";
	private static final String newline = System.getProperty("line.separator");
	public int port;
	private String password;

	JTextArea console;
	ServerSocket ss;
	Socket socket;
	Scanner in;
	PrintWriter out;
	ArrayList<String> ipAddresses;

	public MasterServer(String titleBar) {
		super(titleBar);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 300);
		console = new JTextArea();
		console.setEditable(false);
		console.setLineWrap(true);
		console.setWrapStyleWord(true);
		JScrollPane consoleScroller = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(consoleScroller, BorderLayout.CENTER);
		setVisible(true);
	}

	private void print(String text) {
		console.append(newline + text);
		console.setCaretPosition(console.getDocument().getLength());
	}

	private void out(String text) {
		out.println(text);
	}

	private int generatePort() {
		Random random = new Random();
		int portGen;
		portGen = ((random.nextInt(1000) * 4) + 1000);
		return portGen;
	}

	private String generatePassword() {
		String passwordGen = "";
		char[] lettersAndNumber = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
		Random random = new Random();
		//Create a new password of 5 characters from array
		for (int i = 0; i < 6; i++) {
			passwordGen = passwordGen + lettersAndNumber[random.nextInt(36)];
		}
		return passwordGen;
	}

	private String arrayListToString(ArrayList<String> al) {
		String stringGen = "";
		if (al.isEmpty()) {
			stringGen = "NoServersFound";
			return stringGen;
		} else {
			for (int i = 0; i < al.size(); i++) {
				stringGen = stringGen + al.get(i) + " ";
			}
			return stringGen;
		}
	}

	//Lightweight integer parser that cannot do negatives and only parses numbers with < 4 digits
	//Used to check validity of port number entered
	private int parseInt(String text) throws Exception {
		if (text.length() > 4) {
			throw new Exception();
		} else {
			int number = 0;
			int length = (text.length() - 1);
			char[] numberChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
			for (int i = 0; i < text.length(); i++) {
				for (int j = 0; j < numberChars.length; j++) {
					if (text.charAt(i) == numberChars[j]) {
						int base = (int) Math.pow(10, length);
						number += j * base;
						length--;
					}
				}
			}
			return number;
		}
	}
	
	//Method that returns the current time of the system in 12 hour notation
	private String currentTime() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		Calendar calendar = new GregorianCalendar();
		String am_pm;
		int hour = calendar.get(Calendar.HOUR);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		if (calendar.get(Calendar.AM_PM) == 0)
			am_pm = "AM";
		else
			am_pm = "PM";
		return (nf.format(hour) + ":" + nf.format(minute) + ":" + nf.format(second) + " " + am_pm);
	}

	//Method that evaluates communication received from KChat Servers...
	private void serverCommand(String input) {
		if (input.charAt(0) == '/') {
			String command;
			String parameter;
			try {
				String[] inputArray = input.split(" ", 2);
				command = inputArray[0];
				parameter = inputArray[1];
			} catch (Exception e) {
				print("Command has no parameter.");
				command = input;
				parameter = null;
			}

			if (command.equals("/register")) {
				//Try separating into port and IP address first
				String stringIP;
				String stringPort;
				try {
					String[] inputArray = parameter.split(":", 2);
					stringIP = inputArray[0];
					stringPort = inputArray[1];
				} catch (Exception e) {
					print("No port specified.");
					stringIP = null;
					stringPort = null;
				}
				try {
					@SuppressWarnings("unused")
					InetAddress ipaddress = InetAddress.getByName(stringIP); //Check if IP Address is valid...
					@SuppressWarnings("unused")
					int port = parseInt(stringPort); // Convert port to integer, checking if it is valid...
					ipAddresses.add(parameter); // Store IP+Port in arraylist of Ip Addresses
					print("Ip Address & port successfully received and stored.");
					out("/print MS: Successfully registered!");
					print("IP Addresses: " + ipAddresses.toString());
				} catch (Exception e) {
					print("IP Address invalid!");
					print("IP Addresses: " + ipAddresses.toString());
					out("/print MS: Invalid Ip Address.");
				}
			} else if (command.equals("/changepw")) {
				String currentPassword = parameter;
				if (!currentPassword.equalsIgnoreCase(password)) {
					out("/print MS: Incorrect password.");
					print("Password \"" + currentPassword + "\" rejected. Request is denied.");
				} else if (currentPassword.equalsIgnoreCase(password)) {
					password = generatePassword();
					out("/print MS: Password changed to " + password + ".");
					print("Password successfully changed! New password is " + password + ".");
				}
			} else if (command.equals("/printclients")) {
				print("Sending \"" + arrayListToString(ipAddresses) + "\" to connection.");
				out("/clients " + arrayListToString(ipAddresses));
			} else if (command.equals("/remove")) {
				int index;
				try {
					index = parseInt(parameter); //Custom made integer parser, will generally return something, or else if parameter too long throws generic exception
				} catch (Exception e) {
					index = -1;
					print("Parameter not an index...");
				}
				if (index != -1) {
					if (index < ipAddresses.size()) {
						ipAddresses.remove(index);
						print(parameter + " successfully removed from list.");
						out("/print MS: IP Address successfully removed.");
					} else {
						print("Given index is not present in the list.");
						out("/print MS: Error, Index not present in list.");
					}
				} else if (ipAddresses.contains(parameter)) {
					index = ipAddresses.indexOf(parameter);
					ipAddresses.remove(index);
					print("IP Address @ index " + parameter + " successfully removed from list.");
					out("/print MS: IP Address successfully removed.");

				} else {
					print("IP address \"" + parameter + "\" was not found.");
					out("/print MS: Error, IP Address not found in list.");
				}
			} else {
				print("Command is not valid.");
				out("MS: Command is not valid.");
			}
		} else {
			print("Command is not valid.");
			out("MS: Command is not valid.");
		}
	}

	public static void main(String[] args) {

		MasterServer ms = new MasterServer("KChat Master Server " + VERSION);
		ms.console.append("KChat Master Server " + VERSION + " starting...");
		ms.print("Current time is: "+ms.currentTime()+"...");
		ms.print("Generating new port number...");
		ms.port = ms.generatePort();
		ms.print("Creating storage for IP Addresses & ports...");
		ms.ipAddresses = new ArrayList<String>();
		ms.print("Generating new password...");
		ms.password = ms.generatePassword();
		ms.print(".....................................");

		// Create a new server socket.....
		try {
			ms.ss = new ServerSocket(ms.port);
			ms.print("Server Socket successfully initialized on port " + ms.port + ".");
			ms.print("Server password is " + ms.password + ".");
			ms.print("Server is now listening for connections.");
			ms.print(".....................................");
		} catch (Exception e) {
			ms.ss = null; // Could not create a server socket so make it null
			e.printStackTrace();
			ms.print("Server Socked failed to initialize.");
			ms.print(".....................................");
		}

		while (true) {

			if (ms.ss == null) { // if ss was not set, break immediately
				ms.print("Master Server breaking...");
				break;
			}

			while (true) {
				try {
					ms.socket = ms.ss.accept(); // Hangs here until connection is made
					ms.in = new Scanner(ms.socket.getInputStream());
					ms.out = new PrintWriter(ms.socket.getOutputStream(), true);
					ms.print(" ");
					ms.print("Connection accepted from " + ms.socket.getInetAddress().toString() + " at "+ms.currentTime());
				} catch (IOException e) {
					ms.socket = null;
					ms.in = null;
					ms.out = null;
					e.printStackTrace();
					ms.print("Connection failed to initialize.");
				}

				if (ms.socket == null || ms.in == null || ms.out == null) { // if socket, in, or out is not set, break connection
					ms.print("Aborting connection...");
					break;
				}

				String input = ms.in.nextLine(); // Hangs here waiting for input from connection
				ms.print("Command \"" + input + "\" received from " + ms.socket.getInetAddress().toString());
				ms.serverCommand(input); // Process any input for meaningful commands
				ms.print("Connection dropped.");
			}

		}

		ms.print("Master Server has finished execution.");

	}

}
