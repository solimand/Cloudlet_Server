package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Handoff_NEW_Thread extends Thread {

	private static String previousIP;
	private Socket sock;
    private BufferedReader in;
    private PrintWriter out;
    
	public Handoff_NEW_Thread (String prevIP){
		super();
		setPreviousIP(prevIP);
	}
	
	public static String getPreviousIP() {
		return previousIP;
	}
	public static void setPreviousIP(String previousIP) {
		Handoff_NEW_Thread.previousIP = previousIP;
	}

	public void run() {
		System.out.println("\nContacting Previous Cloudlet Server at "+getPreviousIP()+" . Waiting...");
		/*In this case I am the NEW Cloudlet*/
		try{
    		sock = new Socket(getPreviousIP(), OS_Configuration.DEF_HANDOFF_REQ_PORT);
    		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    		out = new PrintWriter(sock.getOutputStream());
    		System.out.println("\nConnected to previous Cloudlet");
    		
    		out.println(OS_Configuration.KNOCK); out.flush();
    		
    		String communication;
    		int commInt =0 ;
    		while (commInt==0){
    			loopcomm: while((communication = in.readLine())!=null){
    				switch(communication){
    				case OS_Configuration.HANDOFF_OK_MSG:
    					System.out.println("\nStrarting migration. Waiting...");
    					break loopcomm;
    				}
    			}
    			stopConnections();
    			commInt++;
    		}
		}catch(IOException ioe){
    		System.out.println("Problems with socket and stream to old cloudlet");
    		ioe.printStackTrace();
			try {
				stopConnections();
			} catch (IOException e) {
				System.out.println("\nProblems in stopping connection...\n");
				e.printStackTrace();
			}    		
    	}
	}
	
	private void stopConnections() throws IOException {
		if(sock!=null) sock.close();
		if (in!=null) in.close();
		if (out!=null) out.close();
	}
}
