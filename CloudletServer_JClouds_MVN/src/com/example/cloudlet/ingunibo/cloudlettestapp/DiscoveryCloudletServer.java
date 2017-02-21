package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.util.Map;

public class DiscoveryCloudletServer { 
	private static final String SHOW_PUB_IP_CMD = "wget http://ipinfo.io/ip -qO -";
	private static final String SHOW_LOC_IP_CMD = "ip route get 8.8.8.8";
	private static CLI_Utils myUtils;

	protected static Map <String, String>  myEnvVar;
	private static String myPublicAddr;
	private static String myPrivateAddr;

	public static String getMyPrivateAddr() {
		return myPrivateAddr;
	}
	public static void setMyPrivateAddr(String myPrivateAddr) {
		DiscoveryCloudletServer.myPrivateAddr = myPrivateAddr;
	}
	public static String getMyPublicAddr() {
		return myPublicAddr;
	}
	public static void setMyPublicAddr(String myPublicAddr) {
		DiscoveryCloudletServer.myPublicAddr = myPublicAddr;
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Hi Everyone, \n\tI am a cloudlet and expose my services...\n");

/**--- --- ---@@@---Advertise cloudlet service---@@@---**/
		Avahi_Thread avThread = new Avahi_Thread();
		avThread.start();
		avThread.join();
		System.out.println("\nControl Return to Thread ="+Thread.currentThread().getName()+" \n");

/**--- --- ---@@@---Store IP Addresses---@@@---**/
		BufferedReader [] std_out;
		//set public IP of Cloudlet Server
		myUtils = new CLI_Utils();
		std_out = myUtils.execProcess(SHOW_PUB_IP_CMD);
		if (std_out!=null){
			myUtils.getBufferedReadersText(std_out[0], std_out[1]);
			setMyPublicAddr(myUtils.getCurrentStdOut());
		}
		else System.out.println("\nThere is an error in command exec, but we continue...\n");
		
		//set local IP of Cloudlet Server
		/*
		 * ORIGINAL (ip route get 8.8.8.8 | awk '{print $1; exit}')
		 * EDITED (ip route get 8.8.8.8)
		 * 	OUTPUT 8.8.8.8 via 192.168.1.1 dev eth1  src 192.168.1.9
		 * 			cache
	    */
		myUtils = new CLI_Utils();
		std_out = myUtils.execProcess(SHOW_LOC_IP_CMD);
		if (std_out!=null){
			myUtils.getBufferedReadersText(std_out[0], std_out[1]);
			String myline = myUtils.getFirstLineStdOut().substring(0,myUtils.getFirstLineStdOut().length()-1);
			String extractedPrivateIp = myline.substring(myline.lastIndexOf(" ")+1);
			setMyPrivateAddr(extractedPrivateIp);
		}
		else System.out.println("\nThere is an error in command exec, but we continue...\n");
		
		System.out.println("CLOUDLET: Public IP = " + getMyPublicAddr());
		System.out.println("CLOUDLET: Private IP = " + getMyPrivateAddr());
		
/**--- --- ---@@@---Multi-Thread: Wait Handoff Req from more convenient cloudlet---@@@---**/	
/*		
		Handoff_OLD_Thread ho_old_thread = new Handoff_OLD_Thread();
		ho_old_thread.start();
*/
		
/**--- --- ---@@@---Create VM Thread---@@@---**/	
		JCloud_Thread jcThread=new JCloud_Thread(getMyPublicAddr(), getMyPrivateAddr());
		jcThread.start();
		jcThread.join();
		System.out.println("\nControl Return to Thread Main="+Thread.currentThread().getName()+" \n");
		
		
/**--- --- ---@@@---SIGINT Interceptor---@@@---**/
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run(){
				System.out.println("\n ShutDown Hook --> CLOSE!!!");
			}
		});		

	}//main
}
