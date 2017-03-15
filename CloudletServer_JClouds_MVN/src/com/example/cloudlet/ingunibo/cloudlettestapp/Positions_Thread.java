package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Positions_Thread extends Thread {
	private ServerSocket cloudletServRecPositionsSock; 
    private Socket cloudletClientSock;
    //private BufferedReader in;
    private PrintWriter out;
    private ObjectInputStream in;
    private double [][] positionsReceived;
    
    //TODO move configuration in right file. 
    private static final int DEF_CLOUDLET_SERVER_POSITIONS_PORT = 11111;
    private static final int MAX_POSITIONS_NUM = 20;
    private static final String LAT_FILE_NAME = "latitude.txt";
    private static final String LON_FILE_NAME = "longitude.txt";
	
	public Positions_Thread() {
		super();
	}
	
	public void run() {
		try{
			cloudletServRecPositionsSock = new ServerSocket(DEF_CLOUDLET_SERVER_POSITIONS_PORT);
			System.out.println("\nWaiting for positions update on port ...");
			positionsReceived=null;
			cloudletClientSock = cloudletServRecPositionsSock.accept();
			System.out.println("Connected: "+cloudletClientSock.getRemoteSocketAddress());
			//in = new BufferedReader(new InputStreamReader(cloudletClientSock.getInputStream()));
			in = new ObjectInputStream(cloudletClientSock.getInputStream());
			out = new PrintWriter(cloudletClientSock.getOutputStream());
			try {
				positionsReceived= (double[][])in.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException in readObject positions");
				e.printStackTrace();
			}
			
			if(positionsReceived!=null){
				//DEBUG
				System.out.println("Received positions: "+printPositions());
				
				//writing positions to files
				File latFile = new File(LAT_FILE_NAME);
				File lonFile = new File(LON_FILE_NAME);			
				
				// TODO cycle all positions
				for (int row=0;row<positionsReceived.length;row++){
					if(latFile.exists() && !latFile.isDirectory()) { 
					    Files.write(Paths.get(LAT_FILE_NAME), System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
					    Files.write(Paths.get(LAT_FILE_NAME), (String.valueOf(positionsReceived[row][0])+"\n").getBytes(), StandardOpenOption.APPEND);
					}
					else if (!latFile.exists()){
					    Files.write(Paths.get(LAT_FILE_NAME), (String.valueOf(positionsReceived[row][0])+"\n").getBytes(), StandardOpenOption.CREATE_NEW);
					}
					if(lonFile.exists() && !lonFile.isDirectory()) { 
					    Files.write(Paths.get(LON_FILE_NAME), System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
					    Files.write(Paths.get(LON_FILE_NAME), (String.valueOf(positionsReceived[row][1])+"\n").getBytes(), StandardOpenOption.APPEND);
					}
					else if (!lonFile.exists()){
					    Files.write(Paths.get(LON_FILE_NAME), (String.valueOf(positionsReceived[row][1])+"\n").getBytes(), StandardOpenOption.CREATE_NEW);
					}
				}
				
				
				//TODO: check if there are enough positions to send to cloud.
				
				
				/**
			     * curl -F userID_lat@="localpath" http://SERVER_IP:SERVER_PORT
			     * */
			}
			
				
			closeConnections();

		}
		catch (IOException e) {
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
	
	/**
	 * Close streams and connections. 
	 * */
	private void closeConnections() throws IOException{
		if (cloudletServRecPositionsSock!=null)
			cloudletServRecPositionsSock.close();
		if (cloudletClientSock!=null)
			cloudletClientSock.close();
		if(in!=null) in.close();
		if(out!=null) out.close();
		System.out.println("\nAll connections closed\n");
	}
	
	/*TEST PRINT POSITIONS*/
    private String printPositions(){
        String result="";
        result+="Rows --> " + positionsReceived.length+"\n";
        for (int row=0;row<positionsReceived.length;row++){
            for (int col=0;col<2;col++){
            	result=result+positionsReceived[row][col]+"\n";
            }
        }
        return result;
    }
}