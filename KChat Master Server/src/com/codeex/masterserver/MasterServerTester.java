package com.codeex.masterserver;

import java.net.*;
import java.util.Scanner;
import java.io.PrintWriter;

public class MasterServerTester {

	public static void main(String[] args) {
		Scanner inFromConsole = new Scanner(System.in);
		InetAddress serverIP;
		
		try {
		serverIP = InetAddress.getLocalHost();
		}
		catch (Exception e) {
			serverIP = null;
			System.out.println("Error! Server IP is null.");
			System.exit(0);
		}
		
		System.out.println("Enter port: ");
		int port = Integer.parseInt(inFromConsole.nextLine());
		System.out.println("Enter password: ");
		String password = inFromConsole.nextLine();
		
		Socket socket;
		Scanner in;
		PrintWriter out;
		try {
		socket = new Socket(serverIP,port);
		in = new Scanner(socket.getInputStream());
		out = new PrintWriter(socket.getOutputStream(),true);
		}
		catch(Exception e) {
			socket = null;
			in = null;
			out = null;
			System.out.println("Error with socket!");
			System.exit(0);
		}
		
		out.println("/register 192.168.1.150:5555");
		
		String input = in.nextLine();
		
		System.out.println(input);
	}

}
