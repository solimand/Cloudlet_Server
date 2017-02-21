package com.example.cloudlet.ingunibo.cloudlettestapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Avahi_Thread extends Thread {
  
	public void run() {
		File serviceFile = new File (OS_Configuration.DEF_AVAHI_DIR
				+OS_Configuration.DEF_CLOUDLET_SERVICE_FILE_NAME);
		if(serviceFile.exists() && !serviceFile.isDirectory()) {
			System.out.println("\nThe cloudlet service is already advertised.\n");
		} else{
			try {
				setUpAvahiService();
			} catch (URISyntaxException e) {
				System.out.println("\nError adding cloudlet avahi service, EXIT\n");
				e.printStackTrace();
			}
			System.out.println("\nThe cloudlet service is just set up!!.\n");
		}
		
	}
	
	public void setUpAvahiService() throws URISyntaxException{
		URL urlAvahiService = getClass().getResource("/cloudlet_service.service");
		
		File destFile = new File (OS_Configuration.DEF_AVAHI_DIR
				+OS_Configuration.DEF_CLOUDLET_SERVICE_FILE_NAME);
		Path dest = Paths.get(destFile.getAbsolutePath());
		
		List<String> lines = new ArrayList<String>();
		try {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlAvahiService.openStream()));
            String cloudletServiceLine;
            while ((cloudletServiceLine = in.readLine()) != null) 
            	lines.add(cloudletServiceLine);
            
            java.nio.file.Files.write(dest, lines, Charset.forName("UTF-8"));
			System.out.println("\nService Added.\n");

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("\nSomething wrong in writing avahi cloudlet service file.\n");
		}
	}
}
