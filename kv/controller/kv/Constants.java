/***********************************************************************
 * This file contains all the constants used in the controller's code  *
 ***********************************************************************/
package net.floodlightcontroller.kv;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.projectfloodlight.openflow.types.DatapathId;

public class Constants {
	
	// Boolean flag to do the logging of events
	static boolean DEBUG = false;
	
		
	/***************************Configurable parameters**********************************/

	final static int NUM_MSG = 4; // Total Number of messages
	static int NUM_NONOFF_MSG = 0; // Number of offloadable messages
	
	final static String SERVER_PORT = "5858";
/*	final static String SERVER_PORT_2 = "5858";
	final static String SERVER_PORT_3 = "5858";
	final static String SERVER_PORT_4 = "5858";*/
	
	/*********************************************
	 * Load Balancer Message Codes  *
	 *********************************************/
	final static String GET = "1";
	final static String GETG = "2"; 	
	final static String PUT = "3";
	final static String PUTG = "4";
	
	//Reply from KV to Client(ACK)
	final static String SERVERFOUND = "6";
	final static String FIN = "7";
	
	final static String SEPARATOR = "@:##:@";
	final static String MAPKEYSEPARATOR = "@#";
	final static String STOREKEYSEPARATOR = "@@";
	
	/*****************************************/
	final static int DEFAULT_SWITCH_UE_PORT = 1;
	final static String RAN_IP_1 = "192.168.1.1";			// RAN							*
	final static String RAN_IP_2 = "192.168.2.1";
	final static String RAN_IP_3 = "192.168.11.1";
	final static String RAN_IP_4 = "192.168.12.1";
	final static String RAN_IP_5 = "192.168.14.1";
	final static String RAN_IP_6 = "192.168.15.1";
	
	final static int DEFAULT_SWITCH_ID_1 = 1;
	final static int DEFAULT_SWITCH_ID_2 = 2;
	final static int DEFAULT_SWITCH_ID_3 = 3;
	final static int DEFAULT_SWITCH_ID_4 = 4;
	final static int DEFAULT_SWITCH_ID_5 = 5;
	final static int DEFAULT_SWITCH_ID_6 = 6;
	
	static boolean CENTRALIZED = false;
	static boolean MIGRATING = false;
	static Double SYNC_THRESHOLD = 4.0;
	static Double ROOT_CPU_THRESHOLD_HIGH = 55.0;
	static Double ROOT_CPU_THRESHOLD_LOW = 40.0;
	static Double LOCAL_CPU_THRESHOLD_LOW = 44.0;
	static Double LOCAL_CPU_THRESHOLD_HIGH = 50.0;
	//static Double FLOW_RATE_UPBOUND = 0.0; //(1.0/NUM_MSG)*2.2; //(1.0/(NUM_MSG-2));
	static float ThrREMOTEtoCENT = (float) 2300;
	static float ThrCENTtoREMOTE = (float) 2200;
	
	
	final static String ROOT_PORT ="4000"; //external socket.. port number at which root listens to
	final static String LOCAL_1_PORT ="4001"; //external socket.. port number at which root listens to
	final static String LOCAL_2_PORT ="4001"; //external socket.. port number at which root listens to
	final static String LOCAL_3_PORT ="4001"; //external socket.. port number at which root listens to
	final static String LOCAL_4_PORT ="4001"; //external socket.. port number at which root listens to
	final static String LOCAL_5_PORT ="4001"; //external socket.. port number at which root listens to
	final static String LOCAL_6_PORT ="4001"; //external socket.. port number at which root listens to
	
	final static String ROOT_IP ="192.168.100.100"; //Inter-controller commn... Root's IP for external commn
	/*//UE_MAC
	final static String UE_MAC_1 = "00:16:3e:1c:87:ee"; // MAC Address of UE/eNodeB Node  *
	final static String UE_MAC_2 = "00:16:3e:e7:56:4b"; // MAC Address of UE/eNodeB Node  *
*/	
	//Controller IDs
	final static int CONTROLLER_ID_ROOT = 1;
	final static int CONTROLLER_ID_LOCAL_1 = 2;
	final static int CONTROLLER_ID_LOCAL_2 = 3;
	final static int CONTROLLER_ID_LOCAL_3 = 4;
	final static int CONTROLLER_ID_LOCAL_4 = 5;
	final static int CONTROLLER_ID_LOCAL_5 = 6;
	final static int CONTROLLER_ID_LOCAL_6 = 7;
	
	
	/// DGW IPs connected to Root Controller
	final static String DGW_IP_1 = "192.168.100.11";	
	final static String DGW_IP_2 = "192.168.100.12";	
	final static String DGW_IP_3 = "192.168.100.21";	
	final static String DGW_IP_4 = "192.168.100.22";	
	final static String DGW_IP_5 = "192.168.100.31";	
	final static String DGW_IP_6 = "192.168.100.32";	
	
	public static boolean getCentralizedDesign(){
		return CENTRALIZED;
		
	}
	
	public static boolean getMigrating(){
		return MIGRATING;
		
	}
	
	
	public static DatapathId getDgwDpidFromIp(String ip){
		DatapathId dgw = DatapathId.of(0);
		if (ip.equals(Constants.RAN_IP_1))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_1);
		else if (ip.equals(Constants.RAN_IP_2))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_2);
		else if (ip.equals(Constants.RAN_IP_3))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_3);
		else if (ip.equals(Constants.RAN_IP_4))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_4);
		else if (ip.equals(Constants.RAN_IP_5))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_5);
		else if (ip.equals(Constants.RAN_IP_6))
			dgw = DatapathId.of(Constants.DEFAULT_SWITCH_ID_6);
		
		return dgw;
	}
	
	public static String getRanIp(DatapathId dgw){
		String ip = "";
		if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_1)))
			ip = Constants.RAN_IP_1;
		else if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_2)))
			ip = Constants.RAN_IP_2;
		else if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_3)))
			ip = Constants.RAN_IP_3;
		else if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_4)))
			ip = Constants.RAN_IP_4;
		else if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_5)))
			ip = Constants.RAN_IP_5;
		else if (dgw.equals(DatapathId.of(DEFAULT_SWITCH_ID_6)))
			ip = Constants.RAN_IP_6;
	
		return ip;
	}
	
	public static int getChainIDFromDGW(DatapathId dgw){
		if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			return 0;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			return 1;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
			return 2;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
			return 3;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
			return 4;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
			return 5;
		else 
			if(DEBUG)
				System.out.println("GOT INVALID DGW DPID!!!");
			return 0;
	}
	
	public static String getStoreFromDGW(DatapathId dgw){
		if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			return "storeEPC1";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			return "storeEPC2";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
			return "storeEPC3";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
			return "storeEPC4";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
			return "storeEPC5";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
			return "storeEPC6";
		else 
			if(DEBUG)
				System.out.println("GOT INVALID DGW DPID!!!");
			return "";
	}
			
}

