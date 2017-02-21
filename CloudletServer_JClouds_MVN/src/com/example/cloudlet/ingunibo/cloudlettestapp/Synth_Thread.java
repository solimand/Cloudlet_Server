package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.openstack.nova.v2_0.domain.Server;

/**
 * the thread run always in case of NO handoff.
 * first it checks for available VM.
 * if not, executes Synthesis.
 * **/
public class Synth_Thread extends Thread {

    private List <String> serviceAddresses;
    private ServerSocket cloudletServSock; 
    private Socket mobileClientSock;
    private BufferedReader in;
    private PrintWriter out;
	private static CLI_Utils myUtils;

    public void run() {
        try{
        	cloudletServSock = new ServerSocket(OS_Configuration.DEF_CLOUDLET_SERVER_PORT);
			System.out.println("\nWaiting for mobile ack on port "+ OS_Configuration.DEF_CLOUDLET_SERVER_PORT+" ...");
			mobileClientSock = cloudletServSock.accept();
			in = new BufferedReader(
					new InputStreamReader(mobileClientSock.getInputStream()));
			out = new PrintWriter(mobileClientSock.getOutputStream());
			System.out.println("Mobile client RE connected to mobile socket.");
			
			String communications;
			int commInterrupt=0;
			while (commInterrupt==0){
				loopCommunicationVMReady: while((communications=in.readLine())!=null){
					System.out.println("Client says: " + communications);
					switch (communications){
					case OS_Configuration.KNOCK: 
			    		out.println(OS_Configuration.OK_MSG);
			    		out.flush();
			    		break;
			    	case OS_Configuration.REQ_VM_READY_MSG:
			    		System.out.println("\nSearching for ready virtual machine...");
			    		myUtils = new CLI_Utils();
			    		Server serviceVM=null;
			    		serviceVM = myUtils.searchReadyServer(OS_Configuration.DEF_SYNTHESIZED_VM_NAME);
			    		
				        if (serviceVM!=null){
				        	if(serviceVM.getStatus().equals(Server.Status.ACTIVE)){
				        		System.out.println("\nVM ready for the Service. Sending Addresses...\n");
				        		serviceAddresses=new ArrayList<String>();
				        		serviceAddresses=myUtils.getServerVmAddress(serviceVM);
					        	out.println(OS_Configuration.SERVICE_MAN_ADDR_MSG);
					    		out.println(serviceAddresses.get(0));
					    		if (serviceAddresses.size()>1){
					    			out.println(OS_Configuration.SERVICE_PUB_ADDR_MSG);
						    		out.println(serviceAddresses.get(1));
					    		}
								out.flush();
								JCloud_Thread.setVmReady(true);
				        	}
				        	else{
				        		out.println(OS_Configuration.WAIT_SYN_MSG);
					        	out.flush();
//								break loopCommunicationVMReady;
				        	}
				        }
				        else {
				        	System.out.println("\nVM needs to be synthesized. Waiting...\n");
				        	//execution of cmd = ~/elijah-openstack/client/cloudlet_client.py 
				        	//								-c ~/elijah-openstack/client/cred_ospp synthesis 
				        	//									http://localhost:54321/overlays/overlay-os.zip 
				        	//    									synVMname
				        	out.println(OS_Configuration.REQ_OVERLAY_URL);
				        	out.flush();
				        	
							String overlayUrl = in.readLine()+" ";
							Boolean waitMsgSent=false;
							
							if(overlayUrl.startsWith("http://localhost")){
					        	String cmdSynVM = /*OS_Configuration.DEF_PYTHON_CMD +*/ OS_Configuration.DEF_PYTHON_CLIENT_PATH
					        			+ OS_Configuration.DEF_PYTHON_CLIENT_NAME
					        			+ "-c "+ OS_Configuration.DEF_PYTHON_CLIENT_PATH + OS_Configuration.DEF_OS_CRED_LOC_ADMIN_FILE_NAME
					        			+ OS_Configuration.DEF_SYNTHESIS_CMD + overlayUrl 
					        			+ OS_Configuration.DEF_SYNTHESIZED_VM_NAME;
					        	BufferedReader [] std_err;
					    		std_err = myUtils.execProcess(cmdSynVM);
					    		if (std_err!=null){
					    			myUtils.getBufferedReadersText(std_err[0], std_err[1]);
					    		}
					    		else System.out.println("\nThere is an error in command exec, but we continue...\n");
							}
							else {
								//the device should not wait for the download time
								out.println(OS_Configuration.WAIT_SYN_MSG);
					        	out.flush();
					        	waitMsgSent=true;
								//download of zip in local folder
								System.out.println("\nDownload Overlay, please wait...");
								URL overlaySiteUrl = new URL(overlayUrl);
								HttpURLConnection connection = (HttpURLConnection) overlaySiteUrl.openConnection();
								connection.setRequestMethod("GET");
								InputStream inStrOverlay = connection.getInputStream();
								FileOutputStream outStrOverlay = new FileOutputStream(OS_Configuration.DEF_OVERLAY_LOCAL_PATH);
								byte[] buf = new byte[1024];
								int n = inStrOverlay.read(buf);
								int m = 0;
								while (n >= 0) {
									m++;
									outStrOverlay.write(buf, 0, n);
									n = inStrOverlay.read(buf);
									if(m%1000==0) //one MB and multiple
											System.out.print(".");
								}
								outStrOverlay.flush();
								outStrOverlay.close();								
								System.out.println("\nDownload Overlay Finished!!!");
								
								//execute syn command with local link
								String cmdSynVM = OS_Configuration.DEF_PYTHON_CLIENT_PATH
					        			+ OS_Configuration.DEF_PYTHON_CLIENT_NAME
					        			+ "-c "+ OS_Configuration.DEF_PYTHON_CLIENT_PATH + OS_Configuration.DEF_OS_CRED_LOC_ADMIN_FILE_NAME
					        			+ OS_Configuration.DEF_SYNTHESIS_CMD + OS_Configuration.DEF_OVERLAY_LOCAL_URL 
					        			+ OS_Configuration.DEF_SYNTHESIZED_VM_NAME;
					        	BufferedReader [] std_err;
					    		std_err = myUtils.execProcess(cmdSynVM);
					    		if (std_err!=null){
					    			myUtils.getBufferedReadersText(std_err[0], std_err[1]);
					    		}
					    		else System.out.println("\nThere is an error in command exec, but we continue...\n");
							}

							if(!waitMsgSent){
								out.println(OS_Configuration.WAIT_SYN_MSG);
								out.flush();
							}
				        }
				        commInterrupt++;
						break loopCommunicationVMReady;
					}
				}
			closeConnections();
			}
        }catch(IOException ioe){
        	System.out.println("\nError in communication of the addr and port of the service vm\n");
        }
       
        try {
			closeConnections();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("\nThere is an error during closing connection...\n");
		}
		
	}

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
