package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.jclouds.openstack.nova.v2_0.domain.Server;

public class Handoff_OLD_Thread extends Thread{
	private ServerSocket cloudletServRecHandoffSock; 
    private Socket cloudletClientSock;
    private BufferedReader in;
    private PrintWriter out;
    
//    private static String newIPhandoff;
//    public Handoff_OLD_Thread (String prevIP){
//		super();
//		setNewIPhandoff(prevIP);
//	}
//
//	private static String getNewIPhandoff() {
//		return newIPhandoff;
//	}
//	private static void setNewIPhandoff(String newIPhandoff) {
//		Handoff_OLD_Thread.newIPhandoff = newIPhandoff;
//	}
	
	public void run() {
		try {
			cloudletServRecHandoffSock = new ServerSocket(OS_Configuration.DEF_HANDOFF_REQ_PORT);
			System.out.println("\nWaiting for handoff req on port "+OS_Configuration.DEF_HANDOFF_REQ_PORT);
			cloudletClientSock = cloudletServRecHandoffSock.accept();
			in = new BufferedReader(new InputStreamReader(cloudletClientSock.getInputStream()));
			out = new PrintWriter(cloudletClientSock.getOutputStream());
			
			String communication;
    		int commInt =0 ;
    		while (commInt==0){
    			loopcomm: while((communication = in.readLine())!=null){
    				switch(communication){
    				case OS_Configuration.KNOCK:
    					out.println(OS_Configuration.HANDOFF_OK_MSG);out.flush();
    					String ipDestHandoff = cloudletClientSock.getRemoteSocketAddress().toString();
    					CLI_Utils myUtils = new CLI_Utils();
    					Server VMServer=myUtils.searchReadyServer(OS_Configuration.DEF_SYNTHESIZED_VM_NAME);
    					if(VMServer!=null){
    						//getting the UUID
    						String VMid = VMServer.getId();
    						System.out.println("\n Starting handoff of VM with UUID = "+VMid+", to client "+ipDestHandoff);

    						//execution of cmd = ~/elijah-openstack/client/cloudlet_client.py 
        		        	//								-c ~/elijah-openstack/client/credential handoff 
        		        	//								UUID
        					//								dest_credential
    						    					
    						//TODO Auto update dest credential with IP
    						String cmdHandoffVM = OS_Configuration.DEF_PYTHON_CLIENT_PATH
    			        			+ OS_Configuration.DEF_PYTHON_CLIENT_NAME
    			        			+ "-c "+ OS_Configuration.DEF_PYTHON_CLIENT_PATH + OS_Configuration.DEF_OS_CRED_LOC_ADMIN_FILE_NAME
    			        			+ OS_Configuration.DEF_HANDOFF_CMD + VMid +" "
    			        			+ OS_Configuration.DEF_PYTHON_CLIENT_PATH + OS_Configuration.DEF_OS_CRED_REM_PC_ADMIN_FILE_NAME;
    						
    						BufferedReader [] std_err = myUtils.execProcess(cmdHandoffVM);
    			    		if (std_err!=null){
    			    			myUtils.getBufferedReadersText(std_err[0], std_err[1]);
    			    		}
    			    		else System.out.println("\nThere is an error in command exec, but we continue...\n");
    					}else 
    						System.out.println("\nNo Ready VM for handoff, EXIT!");
    					break loopcomm;
    				}
    			}
    			closeConnections();
    			commInt++;
    		}
		}catch (IOException e) {
			e.printStackTrace();
			System.out.println("The client brutally went out!!Closing...");
			try {
				closeConnections();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Problem in closing connection...EXIT");
			}
		}
		
	}
	
	private void closeConnections() throws IOException{
		if (cloudletServRecHandoffSock!=null)
			cloudletServRecHandoffSock.close();
		if (cloudletClientSock!=null)
			cloudletClientSock.close();
		if(in!=null) in.close();
		if(out!=null) out.close();
		System.out.println("\nAll connections closed\n");
	}
	
}
