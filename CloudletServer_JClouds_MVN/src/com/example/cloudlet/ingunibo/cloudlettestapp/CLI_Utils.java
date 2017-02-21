package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.Image;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * 
 * My Methods to execute command in CLI and JCloud
 * 
 * **/
public class CLI_Utils {

	private String currentStdOut;
	private String currentStdErr;
	private String firstLineStdOut;
	
	private String provider = "openstack-nova";
    private String identity = OS_Configuration.KEYSTONE_USERNAME+":"+OS_Configuration.TENANT_NAME;
    private String credential = OS_Configuration.KEYSTONE_PASSWORD;
    private ComputeServiceContext novaCtx;
	private Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
    private Properties overrides = new Properties();
    private NovaApi novaApi;
    private Set<String> regions;
	
	public CLI_Utils(){super();}

	public String getFirstLineStdOut() {
		return firstLineStdOut;
	}
	public void setFirstLineStdOut(String firstLineStdOut) {
		this.firstLineStdOut = firstLineStdOut;
	}
	public String getCurrentStdErr() {
		return currentStdErr;
	}
	private void setCurrentStdErr(String currentStdErr) {
		this.currentStdErr = currentStdErr;
	}
	public String getCurrentStdOut() {
		return currentStdOut;
	}
	private void setCurrentStdOut(String currentStdOut) {
		this.currentStdOut = currentStdOut;
	}

	/**
	 * I need this because the process is root but my files is in /home/[username]/. Usually it uses these param:
	 * 
	 * @param System.getProperty("user.dir");
	 * @param System.getProperty("file.separator")
	 * @return
	 */
	public String getUserHome(String in, String separator){
		String result = null;
		int indexOfSeparatorAfterHome =in.indexOf(separator, 2); 
		String temp = in.substring(0, indexOfSeparatorAfterHome);
		int indexOfSeparatorAfterName =in.indexOf(separator, indexOfSeparatorAfterHome+1);
		temp+=separator+in.substring(indexOfSeparatorAfterHome+1, indexOfSeparatorAfterName)+separator;
		result = temp;
		return result;
	}
	
	/**
	 * 
	 * @param cmd = command to execute
	 * @return = Array{standard output, error output}.
	 */
	public BufferedReader [] execProcess(String cmd){
		System.out.println("\nExecution of command --> \t"+cmd);
		Process proc;
		try {
			proc = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.out.println("\nERR in Execution of command --> \t"+cmd);
			e.printStackTrace();
			return null;
		}
		BufferedReader []result = new BufferedReader[2];
		result[0] = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		result[1] = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		return result;
	}
	
	/**
	 * 
	 * @param cmd = command to execute.
	 * @param envVariables = environment variables map.
	 * @return = Array{standard output, error output}.
	 */
	public BufferedReader [] execProcess(String cmd, Map<String, String> envVariables){
		System.out.println("\nExecution of command "+cmd);
		ProcessBuilder procBuilder;
		Process proc;
		try {
			procBuilder = new ProcessBuilder(OS_Configuration.DEF_BASH,"-c", cmd);
			Map<String, String> env = procBuilder.environment();
			for (Map.Entry<String, String> entry : envVariables.entrySet())
			{
				env.put(entry.getKey(), entry.getValue());
			}
			proc = procBuilder.start();
		} catch (IOException e) {
			System.out.println("\nERR in Execution of command " +cmd);
			e.printStackTrace();
			return null;
		}
		BufferedReader []result = new BufferedReader[2];
		result[0] = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		result[1] = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		return result;
	}
	
	/**
	 * 
	 * Print two BufferedReader to stdout. Used to read STD and ERR output of command line.
	 */
	public void getBufferedReadersText(BufferedReader std, BufferedReader err){
		String currentLine="";
		if (std!=null && err!=null)
			try{
				String tmp_out="";
				System.out.println("\n Std Out = ");
				boolean firstLine=true;
				while((currentLine=std.readLine())!=null){
					System.out.println(currentLine);
					tmp_out = tmp_out+currentLine;
					if (firstLine) setFirstLineStdOut(tmp_out);
					firstLine=false;
				}
				setCurrentStdOut(tmp_out);

				String tmp_err="";
				System.out.println("\n Std Err = ");
				while((currentLine=err.readLine())!=null){
					System.out.println(currentLine);
					tmp_err=tmp_out+currentLine;
				}
				setCurrentStdErr(tmp_err);
			}
			catch (IOException e) {
				System.out.println("\nERR in read of buffered Reader ");
				e.printStackTrace();
			}
		else System.out.println("\nERR during read of buffered reader, seems null...\n");
	}
	
	
	/**
	 * List Images in Glance.
	 */
	protected void listImages(){
		novaCtx = ContextBuilder.newBuilder(provider)
        		.endpoint(OS_Configuration.NOVA_ENDPOINT)
                .credentials(identity, credential)
                .modules(modules)
                .overrides(overrides)
                .buildView(ComputeServiceContext.class);

    	ComputeService computeService = novaCtx.getComputeService(); 
    	
    	Set<? extends Image> imgs = computeService.listImages();
    	System.out.println("\nNow I'm going to print all the " +imgs.size()+" images...\n");
    	for(org.jclouds.compute.domain.Image i:imgs){
	    	System.out.println(i.getId());
	    }
	}
	
	/**
	 * 
	 * @param VMname = name of the VM of interest.
	 * @return = Server object with running service 
	 * 				or null if server with name VMname is in error state.
	 */
	protected Server searchReadyServer(String VMname) {
		Server result = null;
		novaApi = ContextBuilder.newBuilder(provider)
                .endpoint(OS_Configuration.NOVA_ENDPOINT)
                .credentials(identity, credential)
                .modules(modules)
                .buildApi(NovaApi.class);
		
		if (novaApi != null)
			regions = novaApi.getConfiguredRegions();
		
		System.out.println("\nAvailable Regions are: \n");
        for (String region : regions) {
        	System.out.println("\t" + region+"\n");
        }
        
        ServerApi serverApi = novaApi.getServerApi("RegionOne");
        System.out.println("Servers in Default Region (" + OS_Configuration.DEF_REGION + "):\n");
        for (Server server : serverApi.listInDetail().concat()){
            System.out.println("\t" + server.getName()+", in status = " + server.getStatus()
            		+",\n\tcon IP = "+server.getAddresses()
            		+"\n");
            if (server.getName().equals(VMname)){
            	if (server.getStatus() == Server.Status.ACTIVE){
            		result = server;
            	}else if (server.getStatus() == Server.Status.ERROR)
            		System.out.println("\nServer in error state, something wrong, return...\n");
            	else if (server.getStatus() == Server.Status.BUILD){
            		System.out.println("\nServer in building state...\n");
            		result = server;
            	}
            }
        }
        return result;
    }
	
	/**
	 * 
	 * @param service = service server.
	 * @return = list of addresses.  
	 * 
	 * */
	protected ArrayList<String> getServerVmAddress(Server service){
		ArrayList<String> result = new ArrayList<String>();
		for (String key : service.getAddresses().keys()){
        	if (key.equals("private")){
        		System.out.println("\t"+key);
	        	for (Address addr: service.getAddresses().get(key)){
	        		System.out.println("\t"+addr+"\n");
	        		result.add(addr.getAddr());
	        	}
	        	break;
        	}
        }
		return result;
	}
}
