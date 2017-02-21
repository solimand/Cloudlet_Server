package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class JCloud_Thread extends Thread {
	
	private static CLI_Utils myUtils;
    
    private ServerSocket cloudletServSock; 
    private Socket mobileClientSock;
    private BufferedReader in;
    private PrintWriter out;
    private int syn_ho = 0;
    
    private static String serverPublicIpAddr;
    private static String serverPrivateIpAddr;
    private static String previousServerPublicIpAddr;
    private static boolean vmReady = false;
    
	public static boolean isVmReady() {
		return vmReady;
	}
	public static void setVmReady(boolean vmReady) {
		JCloud_Thread.vmReady = vmReady;
	}
	public static String getServerPublicIpAddr() {
		return serverPublicIpAddr;
	}
	public static void setServerPublicIpAddr(String serverPublicIpAddr) {
		JCloud_Thread.serverPublicIpAddr = serverPublicIpAddr;
	}
	public static String getServerPrivateIpAddr() {
		return serverPrivateIpAddr;
	}
	public static void setServerPrivateIpAddr(String serverPrivateIpAddr) {
		JCloud_Thread.serverPrivateIpAddr = serverPrivateIpAddr;
	}	
	public static String getPreviousServerPublicIpAddr() {
		return previousServerPublicIpAddr;
	}
	public static void setPreviousServerPublicIpAddr(String previousServerPublicIpAddr) {
		JCloud_Thread.previousServerPublicIpAddr = previousServerPublicIpAddr;
	}
	
	public JCloud_Thread (String pubAddr, String privAddr){
		super();
		setServerPublicIpAddr(pubAddr);
		setServerPrivateIpAddr(privAddr);
	}

	public void run() {
        System.out.println("\nHello, I'm the thread responsible to interact with OS Server: "
        					+ "\n\t Public Addr= "+getServerPublicIpAddr()
        					+ "\n\t Private Addr= "+getServerPrivateIpAddr());

		myUtils = new CLI_Utils();
		
		
/**--- --- ---@@@---Glance Print & Client Socket--@@@---**/
		myUtils.listImages();

        try {
			cloudletServSock = new ServerSocket(OS_Configuration.DEF_CLOUDLET_SERVER_PORT);
			System.out.println("\nWaiting for connections on port "+OS_Configuration.DEF_CLOUDLET_SERVER_PORT);
			mobileClientSock = cloudletServSock.accept();
			in = new BufferedReader(
					new InputStreamReader(mobileClientSock.getInputStream()));
			out = new PrintWriter(mobileClientSock.getOutputStream());
				
			String communications;
			int commInterrupt=0;
			while (commInterrupt==0){
				loopCommunication: while((communications=in.readLine())!=null){
					System.out.println("Client says: " + communications);
				    switch (communications){
				    	case OS_Configuration.KNOCK: 
				    		out.println(getServerPrivateIpAddr());
				    		out.println(getServerPublicIpAddr());
				    		
				    		out.println(OS_Configuration.WHAT_SERVICE);
				    		out.flush();
				    		break;
				    	case OS_Configuration.SERVICE_MSG:
				    		String reqService = in.readLine();
				    		if(reqService.equals(OS_Configuration.DEF_SERVICE_NAME)){
				    			//cloudlet Server can satisfy the request
				    			out.println(OS_Configuration.IMG_CHK_OK_MSG);
				    			out.flush();
				    		}
				    		else{
				    			//unknown service 
				    			out.println(OS_Configuration.SERVER_UNKNOWN_SERVICE_MSG);
				    			out.flush();
				    			System.out.println("\nThe service requested is unknown."); 
				    			Thread.currentThread().interrupt();
				    		}
				    		break loopCommunication;		
				    		
				    	case OS_Configuration.HANDOFF_NEED_MSG:
				    		out.println(OS_Configuration.IP_FOR_HO_REQ_MSG);
				    		out.flush();
				    		break;
				    	case OS_Configuration.IP_FOR_HO_RES_MSG:
				    		setPreviousServerPublicIpAddr(in.readLine());
				    		syn_ho++;
				    		out.println(OS_Configuration.HANDOFF_OK_MSG);
				    		out.flush();
				    		System.out.println("The source IP to contact for handoff is "+getPreviousServerPublicIpAddr());
				    		break loopCommunication;
				    }
				}
				closeConnections();
				commInterrupt++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("The client brutally went out!!Closing...");
			try {
				closeConnections();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Problem in closing connection...EXIT");
			}
		}

        if(!Thread.currentThread().isInterrupted()){
/**--- --- ---@@@---SYNTHESIS---@@@---**/
	        if(syn_ho==0){  //Synthesis
	        	System.out.println("\nSetting up Service VM...\n");
	        	while (!vmReady){
		        	Synth_Thread syn_thread=new Synth_Thread();
		        	syn_thread.start();
		        	try {
						syn_thread.join();
					} catch (InterruptedException e) {
						System.out.println("\nInterruption during joining synthesis thread");
						e.printStackTrace();
					}
	        	}
	    	}
	        
/**--- --- ---@@@---HANDOFF---@@@---**/
	        else if (syn_ho>0){ //Handoff
	    		System.out.println("\nHANDOFF_START--->Contacting previous cloudlet for handoff...\n");
	    		Handoff_NEW_Thread ho_thread= new Handoff_NEW_Thread(getPreviousServerPublicIpAddr());
	    		ho_thread.start();
	    		try {
					ho_thread.join();
				} catch (InterruptedException e) {
					System.out.println("\nInterruption during joining handoff thread");
					e.printStackTrace();
				}
	    	}
    		System.out.println("\nControl Return to Thread JCloud="+Thread.currentThread().getName()+" \n");

		}else{//else interrupted...
			System.out.println("\nCommunication with client interrupted. EXIT!!!");			
		}
    }//run

	private void closeConnections() throws IOException{
		if (cloudletServSock!=null)
			cloudletServSock.close();
		if (mobileClientSock!=null)
			mobileClientSock.close();
		if(in!=null) in.close();
		if(out!=null) out.close();
		System.out.println("\nAll connections closed\n");
	}
	
}
