package com.example.cloudlet.ingunibo.cloudlettestapp;

public class OS_Configuration {
	
	private static CLI_Utils myUtils = new CLI_Utils();
	
	/**OS**/
	public static final String KEYSTONE_AUTH_URL = "http://localhost:35357/v2.0";  
    public static final String KEYSTONE_USERNAME = "admin";  
    public static final String KEYSTONE_PASSWORD = "nomoresecrete";
    public static final String KEYSTONE_ENDPOINT = "http://localhost:35357/v2.0";  
    public static final String TENANT_NAME = "admin";  
    public static final String NOVA_ENDPOINT = "http://localhost:5000/v2.0";  
    public static final String CEILOMETER_ENDPOINT = "";
    public static final String DEF_REGION = "RegionOne";  
    
    /**System**/
	public static final String DEF_BASH = "/bin/bash";
	public static final String DEF_SERVICE_NAME = "ubuntu-server";
	public static final String REAL_HOME_PATH = myUtils.getUserHome(System.getProperty("user.dir"), System.getProperty("file.separator"));
	public static final String DEF_PYTHON_CLIENT_PATH = REAL_HOME_PATH  + "elijah-openstack/client/";
	public static final String DEF_PYTHON_CLIENT_NAME = "cloudlet_client.py ";
	public static final String DEF_OS_CRED_LOC_ADMIN_FILE_NAME ="openstack_admin_cred_cloudlet ";
	public static final String DEF_OS_CRED_REM_PC_ADMIN_FILE_NAME = "openstack_PCdest_admin_cred_cloudlet ";
	public static final String DEF_OS_CRED_REM_VM_ADMIN_FILE_NAME = "openstack_VMdest_admin_cred_cloudlet ";
	public static final String DEF_SYNTHESIS_CMD= "synthesis ";
	public static final String DEF_HANDOFF_CMD= "handoff ";
	public static final String DEF_SYNTHESIZED_VM_NAME= "synthesizedVMtest";
	public static final String DEF_OVERLAY_LOCAL_PATH= "/var/www/MyWebServer/html/overlays/overlay-os.zip";
	public static final String DEF_OVERLAY_LOCAL_URL= "http://localhost:54321/overlays/overlay-os.zip ";
	public static final int DEF_CLOUDLET_SERVER_PORT = 22222;
	public static final int DEF_MOBILE_CLOUDLET_CLIENT_PORT = 22222;
	public static final int DEF_HANDOFF_REQ_PORT = 22221;
//	public static final int DEF_MOBILE_CLOUDLET_CLIENT_PORT_TEST = 22223;
	public static final String DEF_AVAHI_DIR = "/etc/avahi/services/";
	public static final String DEF_CLOUDLET_SERVICE_FILE_DIR = "/Resources/cloudlet_service.service";
	public static final String  DEF_CLOUDLET_SERVICE_FILE_NAME = "cloudlet_service.service";
	
	/**Mobile-Cloudlet Protocol**/
	public static final String KNOCK = "knock";
	public static final String WHAT_SERVICE = "GET_SERVICE";
	public static final String OK_MSG = "OK";
	public static final String WAIT_SYN_MSG = "WAIT";
	public static final String SERVICE_MSG = "PUT_SERVICE";
	public static final String SERVER_ERROR_MSG = "ERROR";
	public static final String SERVER_UNKNOWN_SERVICE_MSG = "UNKNOW";
	public static final String REQ_OVERLAY_URL = "REQ_OVERLAY_URL";
	public static final String CLIENT_ERROR_MSG = SERVER_ERROR_MSG;
	public static final String IMG_CHK_OK_MSG = "IMAGE_OK";
	public static final String REQ_VM_READY_MSG = "REQ_VM_READY";
	public static final String SERVICE_PUB_ADDR_MSG = "PUB_ADDR";
	public static final String SERVICE_MAN_ADDR_MSG = "MAN_ADDR";
	public static final String SERVICE_PORT_MSG = "PORT";
	public static final String HANDOFF_NEED_MSG = "HANDOFF";
	public static final String HANDOFF_OK_MSG = "HANDOFF_OK";
	public static final String IP_FOR_HO_REQ_MSG = "IP_REQ";
	public static final String IP_FOR_HO_RES_MSG = "IP_RES";
	
}
