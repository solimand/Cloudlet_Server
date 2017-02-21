package com.example.cloudlet.ingunibo.cloudlettestapp.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import com.example.cloudlet.ingunibo.cloudlettestapp.OS_Configuration;

public class WaitForVMTest extends Thread {

	private static int port=22223;
	private static Socket serverSock;
	private static BufferedReader in;
	private static PrintWriter out;
	private static String addr = "192.168.1.11";
	
	
	public static void main(String[] args) {
		System.out.println("\nI'm thread waiting for VM\n");
		try {
			serverSock = new Socket(InetAddress.getByName(addr), port);
			in= new BufferedReader(new InputStreamReader(serverSock.getInputStream()));
			out = new PrintWriter(serverSock.getOutputStream(), true);
			System.out.println("Connected, sending ONE msg...");

			out.println(OS_Configuration.REQ_VM_READY_MSG);
			out.flush();
			
			String communications;
			int commInterrupt=0;
			while (commInterrupt==0){
				loopCommunication: while((communications=in.readLine())!=null){
					System.out.println("Cloudlet server says: " + communications);

					switch (communications) {
					case "ADDR":
						System.out.println("\nService management addr ---> "+in.readLine());
						System.out.println("\nService public addr ---> "+in.readLine());
						break;
					case "PORT":
						System.out.println("\nOn port ---> "+in.readLine());
						break loopCommunication;
					default:
						System.out.println("\nSwitch Def Case....\n");
						break;
					}
				}
				closeConnections();
				commInterrupt++;
			}

		}catch(IOException ioe){
			ioe.printStackTrace();
			System.out.println("\nServer not ready yet\n");
		}

	}

	private static void closeConnections() throws IOException{
		if (serverSock!=null) serverSock.close();
		if(in!=null) in.close();
		if(out!=null) out.close();
		System.out.println("\nAll connection are now closed\n");
	}
	
}
