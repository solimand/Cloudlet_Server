package com.example.cloudlet.ingunibo.cloudlettestapp.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MobileClientTest {

	private static Socket serverSock;
	private static BufferedReader in;
	private static PrintWriter out;
	private static int port=22222;
	private static String addr = "192.168.1.11";
//	private static String communication;

	public static void main(String[] args) {
		try {
			serverSock = new Socket(InetAddress.getByName(addr), port);
			in= new BufferedReader(new InputStreamReader(serverSock.getInputStream()));
			out = new PrintWriter(serverSock.getOutputStream(), true);
			System.out.println("Connected, sending ONE msg...");
			
			MCTest mctestThread=new MCTest(in, out);
			mctestThread.start();
			mctestThread.join();
			closeConnections();
			
		}catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected static void closeConnections() throws IOException{
		if (serverSock!=null)
			serverSock.close();
		if(in!=null) in.close();
		if(out!=null) out.close();
	}
}
class MCTest extends Thread{
	
	private BufferedReader in;
	private PrintWriter out;
	private String communication;
	
	public MCTest(BufferedReader in, PrintWriter out){
		super();
		this.in = in;
		this.out=out;
	}
	
	public void run() {
		out.println("ONE");
		out.flush();
		
		try {
			Thread.sleep (100);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		
		while (true){
			try {
				while((communication=in.readLine())!=null){
				System.out.println("Server says: "+ communication);
					switch (communication) {
					case "Error":
						System.out.println("ERROR! Closing...");
						break;
					case "Give Service...":
						out.println("ubuntu-server");
						out.flush();
						break;
					case "Starting Service...ubuntu-server":
						System.out.println("Waiting...");
						out.println("WAIT");
						out.flush();
						break;
					default:
						System.out.println("DEF case...");
						break;
					}
				}
				MobileClientTest.closeConnections();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
