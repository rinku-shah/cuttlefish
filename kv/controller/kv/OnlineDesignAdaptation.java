package net.floodlightcontroller.kv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.internal.FloodlightProvider;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.PacketParsingException;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionNicira;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraController;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraController.Builder;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraDecTtl;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.sdnplatform.sync.IStoreListener;
import org.sdnplatform.sync.internal.rpc.IRPCListener;
import org.sdnplatform.sync.internal.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Input {
	 // msgInput format: iptos_val,G,L,X,#XR
    private int msg_id; // original msg-id val, not iptos_val
    private boolean offloadable; //X
// constructor
public Input(int id, boolean offloadable) {
   this.msg_id= id; //not iptos
   this.offloadable=offloadable;
}

public int getId(){
	return this.msg_id;
}

public boolean getX(){
	return this.offloadable;
}

}

public class OnlineDesignAdaptation implements IFloodlightModule, IOFMessageListener, IOFSwitchListener {
		protected static Logger log = LoggerFactory.getLogger(OnlineDesignAdaptation.class);
		private IFloodlightProviderService floodlightProvider;
		protected IThreadPoolService threadPoolService;
	    protected IOFSwitchService switchService;
	    String controllerIdStr;
	    		
		public OnlineDesignAdaptation(IOFSwitch sw1,IOFSwitch sw2,IOFSwitch sw3,IOFSwitch sw4,IOFSwitch sw5,IOFSwitch sw6){
			OnlineThread t;
			t = new OnlineThread(sw1,sw2,sw3,sw4,sw5,sw6);
			t.start();
		}

		
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return OnlineDesignAdaptation.class.getName();
		}

		@Override
		public boolean isCallbackOrderingPrereq(OFType type, String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCallbackOrderingPostreq(OFType type, String name) {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public void switchAdded(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchRemoved(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchActivated(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchChanged(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchDeactivated(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Collection<Class<? extends IFloodlightService>> getModuleServices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
			// TODO Auto-generated method stub
		    Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		    l.add(IFloodlightProviderService.class);
			return l;
		}

		@Override
		public void init(FloodlightModuleContext context) throws FloodlightModuleException {
			// TODO Auto-generated method stub
			floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
			threadPoolService = context.getServiceImpl(IThreadPoolService.class);
			//switchService = context.getServiceImpl(IOFSwitchService.class);
/*			Map<String, String> configParams = context.getConfigParams(FloodlightProvider.class);
			controllerIdStr = configParams.get("controllerId");*/
			
		}

		@Override
		public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
			// TODO Auto-generated method stub
			floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		}

		@Override
		public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
				FloodlightContext cntx) {
			// TODO Auto-generated method stub
			return null;
		}
}

class OnlineThread implements Runnable {
	private static IOFSwitch sw1,sw2,sw3,sw4,sw5,sw6;
	public InterControllerClient clt1, clt2, clt3, clt4, clt5, clt6; //clients for SGW conn
	public InterControllerClient dclt1, dclt2, dclt3, dclt4, dclt5, dclt6; //clients for DGW conn
	public InterControllerServer server;
	public Boolean flag2 = false;
	float PTT, GTT, PTT1, GTT1, PTT2, GTT2, PTT3, GTT3, PTT4, GTT4, PTT5, GTT5, PTT6, GTT6;
	
	Input[] inputArr = new Input[20]; /// INPUT MSG ARRAY
	int[] nonOffArr = new int[20];  /// OFFLOADABLE MSG ARRAY
	Double RootRX = 0.0; // Root received data in Mbps
	Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
	Double LocalCpu = 0.0; // Local CPU utilization
	Double LocalCpu1 = 0.0, LocalCpu2 = 0.0; 
	Double LocalCpu3 = 0.0, LocalCpu4 = 0.0; 
	Double LocalCpu5 = 0.0, LocalCpu6 = 0.0;
	int NumLocalCpu = 6; // Number of Local CPUs
	Double RootCpu = 0.0; // Root CPU utilization
	Double[] MsgFlowRate = new Double[100];
	//Double[] MsgFlowRateAvg = new Double[100];
	int SyncCauseMsgId; // The message that created high sync cost
	boolean flag=false;
	Double FlowRate1 = 0.0;
	Double FlowRate2 = 0.0;
	
	float maxPutRate = 0;
	int maxPutId = 0;
	float centPutPerSec = 0;
	
	public OnlineThread(IOFSwitch sw1,IOFSwitch sw2,IOFSwitch sw3,IOFSwitch sw4,IOFSwitch sw5,IOFSwitch sw6) {
		OnlineThread.sw1 = sw1;
		OnlineThread.sw2 = sw2;
		OnlineThread.sw3 = sw3;
		OnlineThread.sw4 = sw4;
		OnlineThread.sw5 = sw5;
		OnlineThread.sw6 = sw6;
	}

	public void start() {
		// TODO Auto-generated method stub
		System.out.println("Online Thread started");
	}
	
	/*private void findXmsg(){
		
		int j=0; //Temp Count of offloadable messages
		// FIND OFFLOADABLE MESSAGES
		for (int i=0; i<Constants.NUM_MSG; i++){
			if(inputArr[i].getG()==false){ //If Global states are not accessed
				if(inputArr[i].getX() || inputArr[i].getL()){ //If X or Local state accessed
					offArr[j]= inputArr[i];
					j++;
				}
			}
			//System.out.println(inputArr[i].getId());
		}
		Constants.NUM_OFF_MSG = j;
		
		for (int i=0; i<Constants.NUM_OFF_MSG; i++){
			System.out.println(offArr[i].getId());
		}
		
		int max = 0;
		for (int i=0; i<Constants.NUM_OFF_MSG; i++){
			if(offArr[i].getXR() > max){
				max = offArr[i].getXR();
				SyncCauseMsgId = offArr[i].getId();
			}
		}
		System.out.println("Sync message id "+ SyncCauseMsgId);
		// END- FIND OFFLOADABLE MESSAGES
	}*/
	private void initServer(){
		try {
			server=new InterControllerServer(this,Integer.parseInt(Constants.ROOT_PORT));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPacketToLocal("Hello... U R Connected To ROOT");
		sendPacketToSwitch("Hello... U R Connected To ROOT");

	}
	private void initMetrics(){
		RootRX = 0.0; // Root received data in Mbps
		FlowRateTot = 0.0; // Total Flow Rate in Mbps
		LocalCpu = 0.0; // Local CPU utilization
		//NumLocalCpu = 8; // Number of Local CPUs
		RootCpu = 0.0; // Root CPU utilization
		//for (int i =0; i < Constants.NUM_CHAINS; i++){
		Arrays.fill(MsgFlowRate, 0.0);
		//}
		//Arrays.fill(MsgFlowRateAvg, 0.0);	
	}
	private void printMetricVal(){
		/*for (int i=0; i<100; i++){
			MsgFlowRateAvg[i] = MsgFlowRate[0][i] + MsgFlowRate[1][i] + MsgFlowRate[2][i] + MsgFlowRate[3][i] + MsgFlowRate[4][i] + MsgFlowRate[5][i];
		}*/
		
		System.out.println("ROOT RX Mbps= "+ RootRX); 
		//System.out.println("Flow Rate from the app"+ FlowRateTot);
		System.out.println("Root CPU= "+ RootCpu);
		System.out.println("Local CPU= "+ LocalCpu);
		System.out.println("Avg Packet Rate from flows-8, 16 = " + MsgFlowRate[8] + " " + MsgFlowRate[16]);
	}
	
	private void captureMetricsPTT(){
				// ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4), & FROM SGW 5(for 5-6, & ROOT) ///
				sendPacketToLocal("GetPTT");
				//}
				// END- ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4) ///
				
				// ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///
				/*ProcessBuilder builder1 = new ProcessBuilder("/bin/sh", "-c", "bash /home/ubuntu/cpu/remote-cpu.sh");
				builder1.redirectOutput(new File("/home/ubuntu/cpu/util.dat"));
				builder1.redirectError(new File("/home/ubuntu/cpu/util.dat"));
				try {
					Process p = builder1.start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // may throw IOException
				*/
				// END- ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///		
				
		}
	
	private void captureMetrics(){
	//// ASK DGWs to start gathering flow stats ///
			try {
				sendPacketToDgw("", "FetchFlowRateToRoot");
			} catch (NumberFormatException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
	    	//// END - ASK DGWs to start gathering flow stats ///
			
			//if(Constants.CENTRALIZED ==false){
				///// START: EXEC BASH PROCESS TO GET RX BW in Mbps //////
					ProcessBuilder builder = new ProcessBuilder("sh", "/home/ubuntu/sync-remote.sh");
					builder.redirectOutput(new File("/home/ubuntu/bandwidth1.dat"));
					builder.redirectError(new File("/home/ubuntu/bandwidth1.dat"));
					try {
						Process p = builder.start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // may throw IOException
								
				///// END: EXEC BASH PROCESS TO GET RX BW in Mbps //////
				
				// ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4), & FROM SGW 5(for 5-6, & ROOT) ///
				
				sendPacketToLocal("CpuUtil");
			//}
			// END- ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4) ///
			
			// ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///
			/*ProcessBuilder builder1 = new ProcessBuilder("/bin/sh", "-c", "bash /home/ubuntu/cpu/remote-cpu.sh");
			builder1.redirectOutput(new File("/home/ubuntu/cpu/util.dat"));
			builder1.redirectError(new File("/home/ubuntu/cpu/util.dat"));
			try {
				Process p = builder1.start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // may throw IOException
			*/
			// END- ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///		
			
	}
	
	private void printMetrics(){
	//// REPORT METRIC VALUES ///
		
/*		Double RootRX = 0.0; // Root received data in Mbps
		Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
		Double LocalCpu = 0.0; // Local CPU utilization
		int NumLocalCpu = 8; // Number of Local CPUs
		Double RootCpu = 0.0; // Root CPU utilization
*/		
				String[] cmd = {
						"/bin/sh",
						"-c",
						"cat /home/ubuntu/bandwidth1.dat | tail -n1",
						
						};
				//executeCommand(cmd);
				String result = executeCommandAndReport(cmd);
				//System.out.println("RX traffic at Root Controller in Mbps= " + result);
				RootRX = Double.parseDouble(result);
				/*String[] cmd1 = {
						"/bin/sh",
						"-c",
						"cat /home/ubuntu/cpu/util.dat | tail -n1",
						
						};
				//executeCommand(cmd);
				String result1 = executeCommandAndReport(cmd1);
				String[] tmp = result1.split(";");
				System.out.println("CPU usage at SGW 5 = " + tmp[0]);
				System.out.println("CPU usage at SGW 6 = " + tmp[1]);
				System.out.println("CPU usage at Root = " + tmp[2]);*/
				
				try {
					sendPacketToDgw("", "FlowRateToRoot");
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// ASK FOR CPU UTIL FROM SGW 1(for 1-4) , & FROM SGW 5(for 5-6, & ROOT) ///
				sendPacketToLocal("FetchCpuUtil");
				// END- ASK FOR CPU UTIL FROM SGW 1(for 1-4) ///
				
				
				
			//// END- REPORT METRIC VALUES ///
	}
	private void executeCommand(String[] cmd) {

		//StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			/*p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}

		//return output.toString();

	}
	
	private String executeCommandAndReport(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
	
	public void beginCentralizedMode(){
	////////START REMOTE TO CENTRALIZED //////////
		sendPacketToLocal("StartCentralized");
		
		Constants.MIGRATING = true;
			
		modifyCentralizedRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
		modifyCentralizedRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
		modifyCentralizedRule(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
		modifyCentralizedRule(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
		modifyCentralizedRule(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
		modifyCentralizedRule(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);
		
		Constants.CENTRALIZED = true;
		
		try {
			Thread.currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Constants.MIGRATING = false;
		
		sendPacketToLocal("StopCentralized");
				
		////////    ENDS REMOTE TO CENTRALIZED      ///////////////
	}
	public void beginRemoteMode(){
	/////// START CENTRALIZED TO REMOTE ///////////////////			
			sendPacketToLocal("StartRemote");
			
			Constants.MIGRATING = true;
			Constants.CENTRALIZED = false;
			//FT.migrateLocalStore(LB.LocalAllocatedServerMap, "AllocatedServerMap");
			//FT.migrateLocalStore(FT.ServerConnNumMap);
			//FT.migrateLocalStore(FT.ServerConnNumMap, "LocalServerConnNumMap");
			FT.migrateLocalStore(FT.kv);
			System.out.println("MIGRATED STORES");
			
			try {
				Thread.currentThread();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*FT.migrateLocalStore(FT.uekey_sgw_teid_map, "uekey_sgw_teid_map");
			FT.migrateLocalStore(FT.sgw_teid_uekey_map, "sgw_teid_uekey_map");
			FT.migrateLocalStore(FT.uekey_ueip_map, "uekey_ueip_map");
			FT.migrateLocalStore(FT.ue_state, "ue_state");
			FT.migrateLocalStore(FT.SGW_PGW_TEID_MAP,"SGW_PGW_TEID_MAP");
					
			System.out.println("MIGRATED STORES AGAIN");*/
			//Constants.CENTRALIZED = false;
			
			//delete(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
			//delete(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
			//delete(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
			//delete(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
			//delete(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
			//delete(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);
			
			modifyLocalRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
			modifyLocalRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
			modifyLocalRule(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
			modifyLocalRule(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
			modifyLocalRule(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
			modifyLocalRule(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);
			
			try {
			Thread.currentThread();
			Thread.sleep(5000);
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			
		   Constants.MIGRATING = false;	
		//sendPacketToLocal("StopRemote");
					
		/////// ENDS CENTRALIZED TO REMOTE ///////////////////
	}
		
	public void sendPacketToLocal(String msg){
		try {
			sendPacketToController("", msg);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void sendPacketToSwitch(String msg){
		try {
			sendPacketToDgw("", msg);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void modifyCentralizedRule(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
				System.out.println("SW= " + sw1);
				/*String ranIp = Constants.RAN_IP_1;
				int controllerId = Constants.CONTROLLER_ID_ROOT;*/
				
			    int controllerId = rootControllerId;
				/*modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
				modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,4);*/
			 
			    modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,1);
			    modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,2);
			    modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,3);
			    modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,4);
	}
	
	public void modifyLocalRule(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
				System.out.println("SW= " + sw1);
				/*String ranIp = Constants.RAN_IP_1;
				int controllerId = Constants.CONTROLLER_ID_ROOT;*/
				
			    int controllerId = localControllerId;
				modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
				modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,3);
				
				modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,2);
				modifyDefaultSwitchNetRuleController(sw1,ranIp,rootControllerId,4);
	}
	
	public void modifyDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		int ip_tos= msgId * 4;
		//System.out.println((byte)ip_tos);
		System.out.println("MODIFIED MESSAGE ROUTING RULE");
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowModify();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		//mb.setExact(MatchField.IN_PORT,OFPort.of(4));
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		//mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ranIp));
		mb.setExact(MatchField.IP_DSCP,IpDscp.of((byte) ip_tos));
			
		OFActionNiciraController.Builder builder = OFFactories.getFactory(OFVersion.OF_10).actions().buildNiciraController();
		builder.setControllerId(controllerId);
		builder.setMaxLen(0xffFFffFF);
		fmb.setActions(Collections.singletonList(builder.build()));	

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
	}
	public void delete(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
				System.out.println("SW= " + sw1);
				/*String ranIp = Constants.RAN_IP_1;
				int controllerId = Constants.CONTROLLER_ID_ROOT;*/
				
			    int controllerId = rootControllerId;
			    deleteDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
			    deleteDefaultSwitchNetRuleController(sw1,ranIp,controllerId,4);
	}
	public void deleteDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		int ip_tos= msgId * 4;
		//System.out.println((byte)ip_tos);
		System.out.println("DELETING MESSAGE ROUTING RULE");
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		//mb.setExact(MatchField.IN_PORT,OFPort.of(4));
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		//mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ranIp));
		mb.setExact(MatchField.IP_DSCP,IpDscp.of((byte) ip_tos));
		
		OFActionNiciraController.Builder builder = OFFactories.getFactory(OFVersion.OF_10).actions().buildNiciraController();
		builder.setControllerId(controllerId);
		builder.setMaxLen(0xffFFffFF);
		fmb.setActions(Collections.singletonList(builder.build()));	

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		
		sw.write(fmb.build());
	}

	@Override
	public void run() {
		System.out.println("Inside OnlineThread");
		//System.out.println("Constant= " + Constants.FLOW_RATE_UPBOUND);
		/// INPUT MSG INITIALIZATION ///
		//Input(int id, boolean global, boolean local, boolean offloadable, int offReads)
		//inputArr[0]= new Input(4,false,false,true,10); // Start
		//inputArr[1]= new Input(16,false,false,true,2); // Stop	
		/// END- INPUT MSG INITIALIZATION ///
		
		/// INPUT MSG INITIALIZATION ///
				//Input(int msgid, boolean offloadable)
				inputArr[0]= new Input(1,true); // Split Get(local)
				inputArr[1]= new Input(2,false); // Root get
				inputArr[2]= new Input(3,true); // Split put
				inputArr[3]= new Input(4,false); // Root put
				
		/// END- INPUT MSG INITIALIZATION ///
				
		/// Initialize Non-Offloadable msgid array---Used for puts/sec when in Cent mode
				int j = 0;
				for(int i=0; i<Constants.NUM_MSG; i++){
					if(inputArr[i].getX()==false){
						nonOffArr[j] = inputArr[i].getId();
						j++;
					}
				}
				Constants.NUM_NONOFF_MSG = j;
				System.out.println("Num Non-Offloadable Message: " + Constants.NUM_NONOFF_MSG);
				System.out.println("Non-Off msg ids are: ");
				for (int k=0; k<Constants.NUM_NONOFF_MSG; k++){
					System.out.print(nonOffArr[k] + "--");
				}
		/// END Initialize Non-Offloadable msgid array---Used for puts/sec when in Cent mode
		
		initMetrics();
		
		//findXmsg(); // Finds Offloadable messages
		
		initServer();
		
		//initMetrics();
		
		try {
			Thread.currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
			captureMetrics();
			
			try {
				Thread.currentThread();
				//Thread.sleep(40000);
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("DONE ");
			
			printMetrics();
			
			try {
				Thread.currentThread();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			///// //Always compute the Local CPU avg before printing values ///
			LocalCpu = LocalCpu/NumLocalCpu; 
			printMetricVal();
			
			if(Constants.CENTRALIZED == false){ //U R currently in Remote mode
				// If #puts/sec to partitioned state is beyond threshold shift to centralized mode
				// Also find the message with highest rate at root, and remember the rate of that msgid
				//CuttleThread.P: puts/sec to parttioned state
				// CuttleThread.Put: put/sec array for each msg id
				/*static float ThrREMOTEtoCENT = (float) 1400;
				static float ThrCENTtoREMOTE = (float) 1200;*/
				
				/*maxPutRate = 0;
				for(int k=0; k<10;k++){
					if (CuttleThread.Put[k] >= maxPutRate){
						maxPutRate = CuttleThread.Put[k];
						maxPutId = k*4;
					}
				}*/
				
				//System.out.println("maxPutRate:" + maxPutRate + " maxPutId:" + maxPutId);
				System.out.println("CuttleThread.P:"+ CuttleThread.P);
				
				if (CuttleThread.P >= Constants.ThrREMOTEtoCENT){
					beginCentralizedMode();
				}
				
			}else { //U R currently in Centralized mode
				centPutPerSec = 0;
				for(int k=0; k<Constants.NUM_NONOFF_MSG; k++){
					centPutPerSec = centPutPerSec + CuttleThread.Put[nonOffArr[k]];
				}
				System.out.println("Puts/sec(centPutPerSec)= " + centPutPerSec);
				
				//System.out.println("MsgFlowRate[maxPutId]:"+ MsgFlowRate[maxPutId]);
				/*if (MsgFlowRate[maxPutId] <= Constants.ThrCENTtoREMOTE){
					beginRemoteMode();
				}*/
				
				if (centPutPerSec <= Constants.ThrCENTtoREMOTE){
					beginRemoteMode();
				}
			}
			
			//System.out.println("puts/sec:" + CuttleThread.P);
						
			/*try {
				Thread.currentThread();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	*/
			initMetrics();
		
		}
			
			/*try {
				Thread.currentThread();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/	
			
			/*captureMetrics();
			
			try {
				Thread.currentThread();
				//Thread.sleep(70000);
				Thread.sleep(40000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("DONE ");
			
			printMetrics();
			
			try {
				Thread.currentThread();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			///// //Always compute the Local CPU avg before printing values ///
			LocalCpu = LocalCpu/NumLocalCpu; 
			printMetricVal();
			////////////////////////////////////////////////////////////////////
*/			
			/*if(Constants.CENTRALIZED == false){ //U R currently in Remote mode
				if((RootRX-FlowRateTot) >= Constants.SYNC_THRESHOLD){
					boolean a = RootCpu >= Constants.ROOT_CPU_THRESHOLD;
					boolean b = LocalCpu <= Constants.LOCAL_CPU_THRESHOLD;
					System.out.println(" RXsync " + (RootRX-FlowRateTot));
					if ((RootCpu >= Constants.ROOT_CPU_THRESHOLD) && (LocalCpu <= Constants.LOCAL_CPU_THRESHOLD)){
						beginCentralizedMode();
					}
				}
			}else {
				//System.out.println("Msgrate= "+ MsgFlowRate[SyncCauseMsgId] +"Constant= " +Constants.FLOW_RATE_UPBOUND);
				if (MsgFlowRate[SyncCauseMsgId] < Constants.FLOW_RATE_UPBOUND) { //>= (1/(Constants.NUM_OFF_MSG+1))) {
					beginRemoteMode();
				}
			}*/
			
			/*//////////////////ORIGINAL DECISION LOOP STARTS HERE /////////////////
			try {
				Thread.currentThread();
				//Thread.sleep(70000);
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(Constants.CENTRALIZED == false){ //U R currently in Remote mode
				captureMetricsPTT();
				try {
					Thread.currentThread();
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PTT = (PTT1+PTT2);///2;
				GTT = (GTT1+GTT2);///2;
				System.out.println("Remote PTT="+PTT+" "+" Remote GTT="+GTT);
				
				//if (PTT >= Constants.ThrREMOTEtoCENT){
				if (PTT <= Constants.ThrREMOTEtoCENT){
					//beginCentralizedMode();
				}
				
			}else {
				float P = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
				float G = CuttleThread.G/CuttleThread.T; //GET to TOTAL
				System.out.println("P= "+CuttleThread.P+" "+"T= "+CuttleThread.T+" "+"putCount= "+FT.putCount);
				System.out.println("PTT="+P+" "+"GTT="+G);
				if (P <= Constants.ThrCENTtoREMOTE){
					//beginRemoteMode();
				}
			}
						
			try {
				Thread.currentThread();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			//initMetrics();
		//////////////////ORIGINAL DECISION LOOP ENDS HERE /////////////////
*/		
			
	}
	
	public void processControllerPacket(String msg){
		String tmpArray[];
		//sw=switch_mapping.get(DatapathId.of(Constants.DEFAULT_SWITCH_ID));
		tmpArray = msg.split(Constants.SEPARATOR);		
		StringBuilder response;	
		//System.out.println("I have got "+tmpArray[0] + "and "+tmpArray[0]==Constants.ATTACH_COMPLETE);
		//ue_key +inport + srcip +dstip + srcmac + dstmac +srcport +dstport
		
		/*	
		Double RootRX = 0.0; // Root received data in Mbps
		Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
		Double LocalCpu = 0.0; // Local CPU utilization
		int NumLocalCpu = 8; // Number of Local CPUs
		Double RootCpu = 0.0; // Root CPU utilization
		 */	
		switch(tmpArray[0]){
		case "PTT1":
			PTT1 = Float.parseFloat(tmpArray[1]);
			GTT1 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "PTT2":
			PTT2 = Float.parseFloat(tmpArray[1]);
			GTT2 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "PTT3":
			PTT3 = Float.parseFloat(tmpArray[1]);
			GTT3 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "PTT4":
			PTT4 = Float.parseFloat(tmpArray[1]);
			GTT4 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "PTT5":
			PTT5 = Float.parseFloat(tmpArray[1]);
			GTT5 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "PTT6":
			PTT6 = Float.parseFloat(tmpArray[1]);
			GTT6 = Float.parseFloat(tmpArray[2]);
			//PTT = CuttleThread.P/CuttleThread.T; //PUT to TOTAL
			//GTT = CuttleThread.G/CuttleThread.T; //GET to TOTAL
			//System.out.println("PTT="+PTT+" "+"GTT="+GTT);
			break;
		case "LocalCpuUtilResult":
			if(tmpArray.length>1){
				String[] tmp = tmpArray[1].split(";");
				LocalCpu = LocalCpu + Double.parseDouble(tmp[0]);
				//System.out.println("CPU usage at LB = " + tmp[0]);
				if(tmp.length>1){
					RootCpu= Double.parseDouble(tmp[1]);
					//System.out.println("Root Cpu=" + RootCpu);
				}
			}
			/*System.out.println("CPU usage at LB 2 = " + tmp[1]);
			System.out.println("CPU usage at LB 3 = " + tmp[2]);
			System.out.println("CPU usage at LB 4 = " + tmp[3]);
			System.out.println("CPU usage at LB 5 = " + tmp[4]);
			System.out.println("CPU usage at LB 6 = " + tmp[5]);
			LocalCpu1=Double.parseDouble(tmp[0]);
			LocalCpu2=Double.parseDouble(tmp[1]);
			LocalCpu3=Double.parseDouble(tmp[2]);
			LocalCpu4=Double.parseDouble(tmp[3]);
			LocalCpu5=Double.parseDouble(tmp[4]);
			LocalCpu6=Double.parseDouble(tmp[5]);
			System.out.println("CPU usage at SGW 3 = " + tmp[2]);
			System.out.println("CPU usage at SGW 4 = " + tmp[3]);
			LocalCpu = LocalCpu + Double.parseDouble(tmp[0]) + Double.parseDouble(tmp[1]) + Double.parseDouble(tmp[2]) + Double.parseDouble(tmp[3])
					+ Double.parseDouble(tmp[4]) + Double.parseDouble(tmp[5]);*/
			break;
		/*case "LocalCpuUtilResult2":
			String[] tmp1 = tmpArray[1].split(";");
			System.out.println("CPU usage at SGW 5 = " + tmp1[0]);
			System.out.println("CPU usage at SGW 6 = " + tmp1[1]);
			LocalCpu = LocalCpu + Double.parseDouble(tmp1[0]) + Double.parseDouble(tmp1[1]);
			//System.out.println("CPU usage at Root = " + tmp1[2]);
			RootCpu = Double.parseDouble(tmp1[2]);
			break;*/
		
		case "FlowRateToRoot":
			//System.out.println("FlowRate msg= " + msg);
			//System.out.println("Flow Rate from DGW" + tmpArray[1] + " = " + tmpArray[2]);
			if(tmpArray.length > 2){
				String[] t = tmpArray[2].trim().split("\\s+");
				int len = t.length;
				
				for(int i =0; i<len ;){
					MsgFlowRate[Integer.parseInt(t[i])] = MsgFlowRate[Integer.parseInt(t[i])] + Double.parseDouble(t[i+1]);
					i=i+2;
				}
			}
			
			//FlowRateTot = FlowRateTot + Double.parseDouble(t[Constants.NUM_MSG*2]);
			
			//MsgFlowRate[Integer.parseInt(t[0])] = MsgFlowRate[Integer.parseInt(t[0])] + Double.parseDouble(t[1]);
			//MsgFlowRate[Integer.parseInt(t[2])] = MsgFlowRate[Integer.parseInt(t[2])] + Double.parseDouble(t[3]);
			
			//MsgFlowRate[Integer.parseInt(t[4])] = MsgFlowRate[Integer.parseInt(t[4])] + Double.parseDouble(t[5]);
			//MsgFlowRate[Integer.parseInt(t[6])] = MsgFlowRate[Integer.parseInt(t[6])] + Double.parseDouble(t[7]);
			/*MsgFlowRate[Integer.parseInt(t[8])] = MsgFlowRate[Integer.parseInt(t[8])] + Double.parseDouble(t[9]);
			MsgFlowRate[Integer.parseInt(t[10])] = MsgFlowRate[Integer.parseInt(t[10])] + Double.parseDouble(t[11]);
			MsgFlowRate[Integer.parseInt(t[12])] = MsgFlowRate[Integer.parseInt(t[12])] + Double.parseDouble(t[13]);
			MsgFlowRate[Integer.parseInt(t[14])] = MsgFlowRate[Integer.parseInt(t[14])] + Double.parseDouble(t[15]);
			MsgFlowRate[Integer.parseInt(t[16])] = MsgFlowRate[Integer.parseInt(t[16])] + Double.parseDouble(t[17]);
			MsgFlowRate[Integer.parseInt(t[18])] = MsgFlowRate[Integer.parseInt(t[18])] + Double.parseDouble(t[19]);
			MsgFlowRate[Integer.parseInt(t[20])] = MsgFlowRate[Integer.parseInt(t[20])] + Double.parseDouble(t[21]);*/
			
			/*for(int i =0; i<Constants.NUM_MSG ;){
				MsgFlowRate[Integer.parseInt(t[i])] = MsgFlowRate[Integer.parseInt(t[i])] + Double.parseDouble(t[i+1]);
				i=i+2;
			}*/
			
			//System.out.println("Msg Flow Rate for ids 8,16 is "+ MsgFlowRate[8]+ " " + MsgFlowRate[16]);
			
			/*if (!flag){
				FlowRate1=Double.parseDouble(t[Constants.NUM_MSG*2]);
				System.out.println("FlowRate1 " + FlowRate1);
				flag=true;
			}
			else {
				FlowRate2=Double.parseDouble(t[Constants.NUM_MSG*2]);
				System.out.println("FlowRate2 " + FlowRate2);
				flag=false;
			}*/
			break;
		default:
			System.out.println("Received message: "+ msg);
		}
	}
	
public boolean sendPacketToController(String receiverIP, String data) throws NumberFormatException, IOException{
		
		//clt_sgw=new InterControllerClient(Constants.SGW_IP_ROOT,Integer.parseInt(Constants.SGW_PORT_ROOT),data);
		
		//System.out.println(data);
		/*clt1.clientSend(data);
		clt2.clientSend(data);
		clt3.clientSend(data);
		clt4.clientSend(data);
		clt5.clientSend(data);
		clt6.clientSend(data);*/
		
		if(!flag2){
			flag2 = true;	
			dclt1=new InterControllerClient(Constants.DGW_IP_1,Integer.parseInt(Constants.LOCAL_1_PORT));
			dclt2=new InterControllerClient(Constants.DGW_IP_2,Integer.parseInt(Constants.LOCAL_2_PORT));
			dclt3=new InterControllerClient(Constants.DGW_IP_3,Integer.parseInt(Constants.LOCAL_3_PORT));
			dclt4=new InterControllerClient(Constants.DGW_IP_4,Integer.parseInt(Constants.LOCAL_4_PORT));
			dclt5=new InterControllerClient(Constants.DGW_IP_5,Integer.parseInt(Constants.LOCAL_5_PORT));
			dclt6=new InterControllerClient(Constants.DGW_IP_6,Integer.parseInt(Constants.LOCAL_6_PORT));
			
		}
		dclt1.clientSend(data);
		dclt2.clientSend(data);
		dclt3.clientSend(data);
		dclt4.clientSend(data);
		dclt5.clientSend(data);
		dclt6.clientSend(data);
		
		if(Constants.DEBUG){
			System.out.println("sent");
		}
		return true;
	}
	
	public boolean sendPacketToDgw(String receiverIP, String data) throws NumberFormatException, IOException{
		if(!flag2){
			flag2 = true;	
			dclt1=new InterControllerClient(Constants.DGW_IP_1,Integer.parseInt(Constants.LOCAL_1_PORT));
			dclt2=new InterControllerClient(Constants.DGW_IP_2,Integer.parseInt(Constants.LOCAL_2_PORT));
			dclt3=new InterControllerClient(Constants.DGW_IP_3,Integer.parseInt(Constants.LOCAL_3_PORT));
			dclt4=new InterControllerClient(Constants.DGW_IP_4,Integer.parseInt(Constants.LOCAL_4_PORT));
			dclt5=new InterControllerClient(Constants.DGW_IP_5,Integer.parseInt(Constants.LOCAL_5_PORT));
			dclt6=new InterControllerClient(Constants.DGW_IP_6,Integer.parseInt(Constants.LOCAL_6_PORT));
			
		}
		dclt1.clientSend(data);
		dclt2.clientSend(data);
		dclt3.clientSend(data);
		dclt4.clientSend(data);
		dclt5.clientSend(data);
		dclt6.clientSend(data);
		
		if(Constants.DEBUG){
			System.out.println("sent");
		}
		return true;
	}


}
/*import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.internal.FloodlightProvider;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.PacketParsingException;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionNicira;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraController;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraController.Builder;
import org.projectfloodlight.openflow.protocol.action.OFActionNiciraDecTtl;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.sdnplatform.sync.IStoreListener;
import org.sdnplatform.sync.error.SyncException;
import org.sdnplatform.sync.internal.rpc.IRPCListener;
import org.sdnplatform.sync.internal.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Input {
	 // msgInput format: iptos_val,G,L,X,#XR
    private int msg_id; //iptos_val
    private boolean global; //G
    private boolean local; //L
    private boolean offloadable; //X
    private int noOfOffloadableReads; //#XR

// constructor
public Input(int id, boolean global, boolean local, boolean offloadable, int offReads) {
   this.msg_id= id;
   this.global= global;
   this.local=local;
   this.offloadable=offloadable;
   this.noOfOffloadableReads= offReads;
}

public int getId(){
	return this.msg_id;
}

public boolean getG(){
	return this.global;
}

public boolean getL(){
	return this.local;
}

public boolean getX(){
	return this.offloadable;
}

public int getXR(){
	return this.noOfOffloadableReads;
}
 }

public class OnlineDesignAdaptation implements IFloodlightModule, IOFMessageListener, IOFSwitchListener {
		protected static Logger log = LoggerFactory.getLogger(OnlineDesignAdaptation.class);
		private IFloodlightProviderService floodlightProvider;
		protected IThreadPoolService threadPoolService;
	    protected IOFSwitchService switchService;
	    String controllerIdStr;
	    
	    		
		public OnlineDesignAdaptation(IOFSwitch sw1,IOFSwitch sw2) throws NumberFormatException, IOException{
			OnlineThread t;
			t = new OnlineThread(sw1,sw2);
			t.start();
		}

		
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return OnlineDesignAdaptation.class.getName();
		}

		@Override
		public boolean isCallbackOrderingPrereq(OFType type, String name) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCallbackOrderingPostreq(OFType type, String name) {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public void switchAdded(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchRemoved(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchActivated(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchChanged(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchDeactivated(DatapathId switchId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Collection<Class<? extends IFloodlightService>> getModuleServices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
			// TODO Auto-generated method stub
		    Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		    l.add(IFloodlightProviderService.class);
			return l;
		}

		@Override
		public void init(FloodlightModuleContext context) throws FloodlightModuleException {
			// TODO Auto-generated method stub
			floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
			threadPoolService = context.getServiceImpl(IThreadPoolService.class);
			//switchService = context.getServiceImpl(IOFSwitchService.class);
			Map<String, String> configParams = context.getConfigParams(FloodlightProvider.class);
			controllerIdStr = configParams.get("controllerId");
			
		}

		@Override
		public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
			// TODO Auto-generated method stub
			floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		}

		@Override
		public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
				FloodlightContext cntx) {
			// TODO Auto-generated method stub
			return null;
		}
}

class OnlineThread implements Runnable {
	private static IOFSwitch sw1,sw2;
	//public InterControllerClient clt1, clt2; //clients for SGW conn
	public InterControllerClient dclt1, dclt2; //clients for DGW conn
	public InterControllerServer server;
	public Boolean flag2 = false;
	
	Input[] inputArr = new Input[20]; /// INPUT MSG ARRAY
	Input[] offArr = new Input[20];  /// OFFLOADABLE MSG ARRAY
	Double RootRX = 0.0; // Root received data in Mbps
	Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
	Double LocalCpu = 0.0; // Local CPU utilization
	Double LocalCpu1, LocalCpu2 = 0.0; 
	int NumLocalCpu = 2; // Number of Local CPUs
	Double RootCpu = 0.0; // Root CPU utilization
	Double[] MsgFlowRate = new Double[100];
	//Double[] MsgFlowRateAvg = new Double[100];
	int SyncCauseMsgId; // The message that created high sync cost
	boolean flag=false;
	Double FlowRate1 = 0.0;
	Double FlowRate2 = 0.0;
	
	public OnlineThread(IOFSwitch sw1,IOFSwitch sw2) {
		OnlineThread.sw1 = sw1;
		OnlineThread.sw2 = sw2;
	}

	public void start() throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Constant= " + Constants.FLOW_RATE_UPBOUND);
		/// INPUT MSG INITIALIZATION ///
		//Input(int id, boolean global, boolean local, boolean offloadable, int offReads)
		inputArr[0]= new Input(4,false,false,true,10); // Start
		inputArr[1]= new Input(16,false,false,true,2); // Stop	

		/// END- INPUT MSG INITIALIZATION ///
		initMetrics();
		
		findXmsg(); // Finds Offloadable messages
		
		initServer();
		
		//initMetrics();
		
		try {
			Thread.currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
			//FT.get(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "SyncMap", "2");
			//FT.myStats();
			//FT.get(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "SyncMap", "2");
			//captureMetrics();
			
			try {
				Thread.currentThread();
				//Thread.sleep(70000);
				Thread.sleep(40000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("DONE ");
			
			//printMetrics();
			
			try {
				Thread.currentThread();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			///// //Always compute the Local CPU avg before printing values ///
			LocalCpu = LocalCpu/NumLocalCpu; 
			printMetricVal();
			////////////////////////////////////////////////////////////////////
			
			if(Constants.CENTRALIZED == false){ //U R currently in Remote mode
				//Double diffCpu = Math.abs(LocalCpu1 - LocalCpu2);
				Double f1 = (FlowRate1/FlowRateTot)*100;
				Double f2 = (FlowRate2/FlowRateTot)*100;
				Double diffRate = Math.abs(f1 - f2);
				//if(((RootRX-FlowRateTot) >= Constants.SYNC_THRESHOLD) || diffCpu > 10){
				if(((RootRX-FlowRateTot) >= Constants.SYNC_THRESHOLD) || diffRate > 30){
						//|| (LocalCpu >= Constants.LOCAL_CPU_THRESHOLD_LOW) && (LocalCpu <= Constants.LOCAL_CPU_THRESHOLD_HIGH)){
					boolean a = RootCpu >= Constants.ROOT_CPU_THRESHOLD;
					boolean b = LocalCpu <= Constants.LOCAL_CPU_THRESHOLD;
					System.out.println(" a " + a + " b "+ b);
					//if ((RootCpu >= Constants.ROOT_CPU_THRESHOLD) && (LocalCpu <= Constants.LOCAL_CPU_THRESHOLD)){
						beginCentralizedMode();
					//}
				}
			}else { //U R currently in Centralized mode
				Double f1 = (FlowRate1/FlowRateTot)*100;
				Double f2 = (FlowRate2/FlowRateTot)*100;
				Double diffRate = Math.abs(f1 - f2);
				//System.out.println("Msgrate= "+ MsgFlowRate[SyncCauseMsgId] +"Constant= " +Constants.FLOW_RATE_UPBOUND);
				//if ((MsgFlowRate[SyncCauseMsgId] < Constants.FLOW_RATE_UPBOUND) || (RootCpu >= Constants.ROOT_CPU_THRESHOLD)) { //>= (1/(Constants.NUM_OFF_MSG+1))) {
				if ((MsgFlowRate[SyncCauseMsgId] < Constants.FLOW_RATE_UPBOUND) || diffRate < 20) {
						//|| (RootCpu <= Constants.ROOT_CPU_THRESHOLD_LOW) && (RootCpu >= Constants.ROOT_CPU_THRESHOLD_HIGH)) { //>= (1/(Constants.NUM_OFF_MSG+1))) {
					beginRemoteMode();
				}
			}
						
			try {
				Thread.currentThread();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			//initMetrics();
		}
	}
	
	private void findXmsg(){
		
		int j=0; //Temp Count of offloadable messages
		// FIND OFFLOADABLE MESSAGES
		for (int i=0; i<Constants.NUM_MSG; i++){
			if(inputArr[i].getG()==false){ //If Global states are not accessed
				if(inputArr[i].getX() || inputArr[i].getL()){ //If X or Local state accessed
					offArr[j]= inputArr[i];
					j++;
				}
			}
			//System.out.println(inputArr[i].getId());
		}
		Constants.NUM_OFF_MSG = j;
		System.out.println("Num OFFLOADABLE MSG= " + Constants.NUM_OFF_MSG);
		
		for (int i=0; i<Constants.NUM_OFF_MSG; i++){
			System.out.println(offArr[i].getId());
		}
		
		int max = 0;
		for (int i=0; i<Constants.NUM_OFF_MSG; i++){
			if(offArr[i].getXR() > max){
				max = offArr[i].getXR();
				SyncCauseMsgId = offArr[i].getId();
			}
		}
		System.out.println("Sync message id "+ SyncCauseMsgId);
		// END- FIND OFFLOADABLE MESSAGES
	}
	private void initServer(){
		try {
			server=new InterControllerServer(this,Integer.parseInt(Constants.ROOT_PORT));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPacketToLocal("Hello... U R Connected To ROOT");
		sendPacketToSwitch("Hello... U R Connected To ROOT");

	}
	private void initMetrics(){
		RootRX = 0.0; // Root received data in Mbps
		FlowRateTot = 0.0; // Total Flow Rate in Mbps
		LocalCpu = 0.0; // Local CPU utilization
		//NumLocalCpu = 8; // Number of Local CPUs
		RootCpu = 0.0; // Root CPU utilization
		//for (int i =0; i < Constants.NUM_CHAINS; i++){
		Arrays.fill(MsgFlowRate, 0.0);
		//}
		//Arrays.fill(MsgFlowRateAvg, 0.0);	
	}
	private void printMetricVal(){
		for (int i=0; i<100; i++){
			MsgFlowRateAvg[i] = MsgFlowRate[0][i] + MsgFlowRate[1][i] + MsgFlowRate[2][i] + MsgFlowRate[3][i] + MsgFlowRate[4][i] + MsgFlowRate[5][i];
		}
		
		System.out.println("ROOT RX Mbps= "+ RootRX); 
		System.out.println("Flow Rate from the app"+ FlowRateTot);
		System.out.println("Root CPU= "+ RootCpu);
		System.out.println("Local CPU= "+ LocalCpu);
		System.out.println("Avg Packet Rate from flows-4,16= " + MsgFlowRate[4] + " " + MsgFlowRate[16]);
	}
	private void captureMetrics() throws NumberFormatException, IOException{
	//// ASK DGWs to start gathering flow stats ///
			try {
				sendPacketToDgw("", "FetchFlowRateToRoot");
			} catch (NumberFormatException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
	    	//// END - ASK DGWs to start gathering flow stats ///
			
			//if(Constants.CENTRALIZED ==false){
				///// START: EXEC BASH PROCESS TO GET RX BW in Mbps //////
					ProcessBuilder builder = new ProcessBuilder("sh", "/home/ubuntu/sync-remote.sh");
					builder.redirectOutput(new File("/home/ubuntu/bandwidth1.dat"));
					builder.redirectError(new File("/home/ubuntu/bandwidth1.dat"));
					try {
						Process p = builder.start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // may throw IOException
					
					ProcessBuilder builder1 = new ProcessBuilder("/bin/sh", "-c", "bash /home/ubuntu/cpu/remote-cpu.sh");
					builder1.redirectOutput(new File("/home/ubuntu/cpu/util.dat"));
					builder1.redirectError(new File("/home/ubuntu/cpu/util.dat"));
					try {
						Process p = builder1.start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // may throw IOException
					
				///// END: EXEC BASH PROCESS TO GET RX BW in Mbps //////
				
				// ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4), & FROM SGW 5(for 5-6, & ROOT) ///
				
				sendPacketToDgw("","CpuUtil");
			//}
			// END- ASK TO COMPUTE CPU UTIL FROM SGW 1(for 1-4) ///
			
			// ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///
			ProcessBuilder builder1 = new ProcessBuilder("/bin/sh", "-c", "bash /home/ubuntu/cpu/remote-cpu.sh");
			builder1.redirectOutput(new File("/home/ubuntu/cpu/util.dat"));
			builder1.redirectError(new File("/home/ubuntu/cpu/util.dat"));
			try {
				Process p = builder1.start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // may throw IOException
			
			// END- ASK FOR CPU UTIL FROM Root(for sgw5-6 & root) ///		
			
	}
	
	private void printMetrics() throws NumberFormatException, IOException{
	//// REPORT METRIC VALUES ///
		
		Double RootRX = 0.0; // Root received data in Mbps
		Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
		Double LocalCpu = 0.0; // Local CPU utilization
		int NumLocalCpu = 8; // Number of Local CPUs
		Double RootCpu = 0.0; // Root CPU utilization
		
				String[] cmd = {
						"/bin/sh",
						"-c",
						"cat /home/ubuntu/bandwidth1.dat | tail -n1",
						
						};
				//executeCommand(cmd);
				String result = executeCommandAndReport(cmd);
				System.out.println("RX traffic at Root Controller in Mbps= " + result);
				RootRX = Double.parseDouble(result);
				//////////////////////////
				String[] cmd1 = {
						"/bin/sh",
						"-c",
						"cat /home/ubuntu/cpu/util.dat | tail -n1",
						
						};
				//executeCommand(cmd);
				String result1 = executeCommandAndReport(cmd1);
				System.out.println("Root CPU = " + result1);
				RootCpu = Double.parseDouble(result1);
				
				String[] cmd1 = {
						"/bin/sh",
						"-c",
						"cat /home/ubuntu/cpu/util.dat | tail -n1",
						
						};
				//executeCommand(cmd);
				String result1 = executeCommandAndReport(cmd1);
				String[] tmp = result1.split(";");
				System.out.println("CPU usage at SGW 5 = " + tmp[0]);
				System.out.println("CPU usage at SGW 6 = " + tmp[1]);
				System.out.println("CPU usage at Root = " + tmp[2]);
				
				try {
					sendPacketToDgw("", "FlowRateToRoot");
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// ASK FOR CPU UTIL FROM SGW 1(for 1-4) , & FROM SGW 5(for 5-6, & ROOT) ///
				sendPacketToDgw("","FetchCpuUtil");
				// END- ASK FOR CPU UTIL FROM SGW 1(for 1-4) ///
				
				
				
			//// END- REPORT METRIC VALUES ///
	}
	private void executeCommand(String[] cmd) {

		//StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		//return output.toString();

	}
	
	private String executeCommandAndReport(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
	
	public void beginCentralizedMode() throws NumberFormatException, IOException{
	////////START REMOTE TO CENTRALIZED //////////
		sendPacketToDgw("","StartCentralized");
		Constants.MIGRATING = true;
		Constants.CENTRALIZED = true;
		
		try {
			FT.migrateRemoteStore(LB.LocalClientData);
		} catch (SyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		modifyCentralizedRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
		modifyCentralizedRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
		
		
		
		try {
			Thread.currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Constants.MIGRATING = false;
		
		sendPacketToDgw("","StopCentralized");
				
		////////    ENDS REMOTE TO CENTRALIZED      ///////////////
	}
	public void beginRemoteMode() throws NumberFormatException, IOException{
	/////// START CENTRALIZED TO REMOTE ///////////////////			
			
			
			Constants.MIGRATING = true;
			Constants.CENTRALIZED = false;
			
			//FT.migrateLocalStore(LB.LocalAllocatedServerMap, "AllocatedServerMap");
			FT.migrateLocalStore(FT.ServerConnNumMap);
			//FT.migrateLocalStore(FT.ServerConnNumMap, "LocalServerConnNumMap");
			sendPacketToDgw("","StartRemote");	
			System.out.println("MIGRATED STORES");
					
			try {
				Thread.currentThread();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			FT.migrateLocalStore(FT.uekey_sgw_teid_map, "uekey_sgw_teid_map");
			FT.migrateLocalStore(FT.sgw_teid_uekey_map, "sgw_teid_uekey_map");
			FT.migrateLocalStore(FT.uekey_ueip_map, "uekey_ueip_map");
			FT.migrateLocalStore(FT.ue_state, "ue_state");
			FT.migrateLocalStore(FT.SGW_PGW_TEID_MAP,"SGW_PGW_TEID_MAP");
					
			System.out.println("MIGRATED STORES AGAIN");
			//Constants.CENTRALIZED = false;
			
			//delete(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
			//delete(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
			//delete(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
			//delete(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
			//delete(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
			//delete(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);
			
			modifyLocalRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
			modifyLocalRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
			
			try {
			Thread.currentThread();
			Thread.sleep(5000);
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			
		   Constants.MIGRATING = false;	
		//sendPacketToLocal("StopRemote");
					
		/////// ENDS CENTRALIZED TO REMOTE ///////////////////
	}
		
	public void sendPacketToLocal(String msg){
		try {
			sendPacketToController("", msg);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void sendPacketToSwitch(String msg){
		try {
			sendPacketToDgw("", msg);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void modifyCentralizedRule(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
		System.out.println("SW= " + sw1);
		String ranIp = Constants.RAN_IP_1;
		int controllerId = Constants.CONTROLLER_ID_ROOT;
		
	    int controllerId = rootControllerId;
		modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
		modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,4);

	}
	
	public void modifyLocalRule(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
		System.out.println("SW= " + sw1);
		String ranIp = Constants.RAN_IP_1;
		int controllerId = Constants.CONTROLLER_ID_ROOT;
		
	    int controllerId = localControllerId;
		modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
		modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,4);

	}
	
	public void modifyDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		int ip_tos= msgId * 4;
		//System.out.println((byte)ip_tos);
		System.out.println("MODIFIED MESSAGE ROUTING RULE");
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowModify();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		//mb.setExact(MatchField.IN_PORT,OFPort.of(4));
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		//mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ranIp));
		mb.setExact(MatchField.IP_DSCP,IpDscp.of((byte) ip_tos));
			
		OFActionNiciraController.Builder builder = OFFactories.getFactory(OFVersion.OF_10).actions().buildNiciraController();
		builder.setControllerId(controllerId);
		builder.setMaxLen(0xffFFffFF);
		fmb.setActions(Collections.singletonList(builder.build()));	

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
	}
	public void delete(IOFSwitch sw1, String ranIp, int rootControllerId, int localControllerId){
		//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
		System.out.println("SW= " + sw1);
		String ranIp = Constants.RAN_IP_1;
		int controllerId = Constants.CONTROLLER_ID_ROOT;
		
	    int controllerId = rootControllerId;
	    deleteDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
	    deleteDefaultSwitchNetRuleController(sw1,ranIp,controllerId,3);

	}
	public void deleteDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		int ip_tos= msgId * 4;
		//System.out.println((byte)ip_tos);
		System.out.println("DELETING MESSAGE ROUTING RULE");
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		//mb.setExact(MatchField.IN_PORT,OFPort.of(4));
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		//mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ranIp));
		mb.setExact(MatchField.IP_DSCP,IpDscp.of((byte) ip_tos));
		
		OFActionNiciraController.Builder builder = OFFactories.getFactory(OFVersion.OF_10).actions().buildNiciraController();
		builder.setControllerId(controllerId);
		builder.setMaxLen(0xffFFffFF);
		fmb.setActions(Collections.singletonList(builder.build()));	

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		
		sw.write(fmb.build());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public void processControllerPacket(String msg){
		String tmpArray[];
		//sw=switch_mapping.get(DatapathId.of(Constants.DEFAULT_SWITCH_ID));
		tmpArray = msg.split(Constants.SEPARATOR);		
		StringBuilder response;	
		//System.out.println("I have got "+tmpArray[0] + "and "+tmpArray[0]==Constants.ATTACH_COMPLETE);
		//ue_key +inport + srcip +dstip + srcmac + dstmac +srcport +dstport
		
			
		Double RootRX = 0.0; // Root received data in Mbps
		Double FlowRateTot = 0.0; // Total Flow Rate in Mbps
		Double LocalCpu = 0.0; // Local CPU utilization
		int NumLocalCpu = 8; // Number of Local CPUs
		Double RootCpu = 0.0; // Root CPU utilization
		 	
		switch(tmpArray[0]){
		case "LocalCpuUtilResult":
			String[] tmp = tmpArray[1].split(";");
			System.out.println("CPU usage at LB 1 = " + tmp[0]);
			System.out.println("CPU usage at LB 2 = " + tmp[1]);
			LocalCpu1=Double.parseDouble(tmp[0]);
			LocalCpu2=Double.parseDouble(tmp[1]);
			System.out.println("CPU usage at SGW 3 = " + tmp[2]);
			System.out.println("CPU usage at SGW 4 = " + tmp[3]);
			LocalCpu = LocalCpu + Double.parseDouble(tmp[0]) + Double.parseDouble(tmp[1]); // + Double.parseDouble(tmp[2]) + Double.parseDouble(tmp[3]);
			break;
		case "LocalCpuUtilResult2":
			String[] tmp1 = tmpArray[1].split(";");
			System.out.println("CPU usage at SGW 5 = " + tmp1[0]);
			System.out.println("CPU usage at SGW 6 = " + tmp1[1]);
			LocalCpu = LocalCpu + Double.parseDouble(tmp1[0]) + Double.parseDouble(tmp1[1]);
			//System.out.println("CPU usage at Root = " + tmp1[2]);
			RootCpu = Double.parseDouble(tmp1[2]);
			break;
		
		case "FlowRateToRoot":
			System.out.println("Flow Rate from DGW" + tmpArray[1] + " = " + tmpArray[2]);
			String[] t = tmpArray[2].trim().split("\\s+");
			FlowRateTot = FlowRateTot + Double.parseDouble(t[Constants.NUM_MSG*2]);
			MsgFlowRate[Integer.parseInt(t[0])] = MsgFlowRate[Integer.parseInt(t[0])] + Double.parseDouble(t[1]);
			MsgFlowRate[Integer.parseInt(t[2])] = MsgFlowRate[Integer.parseInt(t[2])] + Double.parseDouble(t[3]);
			MsgFlowRate[Integer.parseInt(t[4])] = MsgFlowRate[Integer.parseInt(t[4])] + Double.parseDouble(t[5]);
			MsgFlowRate[Integer.parseInt(t[6])] = MsgFlowRate[Integer.parseInt(t[6])] + Double.parseDouble(t[7]);
			MsgFlowRate[Integer.parseInt(t[8])] = MsgFlowRate[Integer.parseInt(t[8])] + Double.parseDouble(t[9]);
			MsgFlowRate[Integer.parseInt(t[10])] = MsgFlowRate[Integer.parseInt(t[10])] + Double.parseDouble(t[11]);
			MsgFlowRate[Integer.parseInt(t[12])] = MsgFlowRate[Integer.parseInt(t[12])] + Double.parseDouble(t[13]);
			MsgFlowRate[Integer.parseInt(t[14])] = MsgFlowRate[Integer.parseInt(t[14])] + Double.parseDouble(t[15]);
			MsgFlowRate[Integer.parseInt(t[16])] = MsgFlowRate[Integer.parseInt(t[16])] + Double.parseDouble(t[17]);
			MsgFlowRate[Integer.parseInt(t[18])] = MsgFlowRate[Integer.parseInt(t[18])] + Double.parseDouble(t[19]);
			MsgFlowRate[Integer.parseInt(t[20])] = MsgFlowRate[Integer.parseInt(t[20])] + Double.parseDouble(t[21]);
			
			for(int i =0; i<Constants.NUM_MSG ;){
				MsgFlowRate[Integer.parseInt(t[i])] = MsgFlowRate[Integer.parseInt(t[i])] + Double.parseDouble(t[i+1]);
				i=i+2;
			}
			
			System.out.println("Msg Flow Rate for ids 4,16 is "+MsgFlowRate[4]+ " " + MsgFlowRate[16]);
			if (!flag){
				FlowRate1=Double.parseDouble(t[Constants.NUM_MSG*2]);
				System.out.println("FlowRate1 " + FlowRate1);
				flag=true;
			}
			else {
				FlowRate2=Double.parseDouble(t[Constants.NUM_MSG*2]);
				System.out.println("FlowRate2 " + FlowRate2);
				flag=false;
			}
			break;
		default:
			System.out.println("Received message: "+ msg);
		}
	}
	
	public boolean sendPacketToController(String receiverIP, String data) throws NumberFormatException, IOException{
		
		//clt_sgw=new InterControllerClient(Constants.SGW_IP_ROOT,Integer.parseInt(Constants.SGW_PORT_ROOT),data);
		
		//System.out.println(data);
		clt1.clientSend(data);
		clt2.clientSend(data);
		clt3.clientSend(data);
		clt4.clientSend(data);
		clt5.clientSend(data);
		clt6.clientSend(data);
		
		dclt1.clientSend(data);
		dclt2.clientSend(data);
		dclt3.clientSend(data);
		dclt4.clientSend(data);
		dclt5.clientSend(data);
		dclt6.clientSend(data);
		
		if(Constants.DEBUG){
			System.out.println("sent");
		}
		return true;
	}
	
	public boolean sendPacketToDgw(String receiverIP, String data) throws NumberFormatException, IOException{
		if(!flag2){
			flag2 = true;	
			dclt1=new InterControllerClient(Constants.DGW_IP_1,Integer.parseInt(Constants.LOCAL_1_PORT));
			dclt2=new InterControllerClient(Constants.DGW_IP_2,Integer.parseInt(Constants.LOCAL_2_PORT));
			
		}
		dclt1.clientSend(data);
		dclt2.clientSend(data);
		
		if(Constants.DEBUG){
			System.out.println("sent");
		}
		return true;
	}


}
*/
////////START REMOTE TO CENTRALIZED //////////

/*try {
	Thread.currentThread();
	Thread.sleep(50000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

sendPacketToLocal("StartCentralized");

Constants.MIGRATING = true;
	
modifyCentralizedRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
modifyCentralizedRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
modifyCentralizedRule(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
modifyCentralizedRule(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
modifyCentralizedRule(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
modifyCentralizedRule(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);

Constants.CENTRALIZED = true;

try {
	Thread.currentThread();
	Thread.sleep(5000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
Constants.MIGRATING = false;

sendPacketToLocal("StopCentralized");*/
		
////////    ENDS REMOTE TO CENTRALIZED      ///////////////

//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
/*System.out.println("SW= " + sw1);
String ranIp = Constants.RAN_IP_1;
int controllerId = Constants.CONTROLLER_ID_ROOT;*/

/////// START CENTRALIZED TO REMOTE ///////////////////

/*		try {
	Thread.currentThread();
	Thread.sleep(180000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

sendPacketToLocal("StartRemote");

Constants.MIGRATING = true;
Constants.CENTRALIZED = false;
FT.migrateLocalStore(FT.uekey_sgw_teid_map, "uekey_sgw_teid_map");
FT.migrateLocalStore(FT.sgw_teid_uekey_map, "sgw_teid_uekey_map");
FT.migrateLocalStore(FT.uekey_ueip_map, "uekey_ueip_map");
FT.migrateLocalStore(FT.ue_state, "ue_state");
FT.migrateLocalStore(FT.SGW_PGW_TEID_MAP,"SGW_PGW_TEID_MAP");
		
System.out.println("MIGRATED STORES");

try {
	Thread.currentThread();
	Thread.sleep(5000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

//Constants.CENTRALIZED = false;

//delete(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
//delete(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
//delete(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
//delete(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
//delete(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
//delete(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);

modifyLocalRule(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
modifyLocalRule(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
modifyLocalRule(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
modifyLocalRule(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
modifyLocalRule(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
modifyLocalRule(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);
	
try {
Thread.currentThread();
Thread.sleep(10000);
} catch (InterruptedException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
Constants.MIGRATING = false;

sendPacketToLocal("StopRemote");*/
		
/////// ENDS CENTRALIZED TO REMOTE ///////////////////


/*try {
	Thread.currentThread();
	Thread.sleep(12000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
modify(sw1,Constants.RAN_IP_1, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_1);
modify(sw2,Constants.RAN_IP_2, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_2);
modify(sw3,Constants.RAN_IP_3, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_3);
modify(sw4,Constants.RAN_IP_4, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_4);
modify(sw5,Constants.RAN_IP_5, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_5);
modify(sw6,Constants.RAN_IP_6, Constants.CONTROLLER_ID_ROOT,Constants.CONTROLLER_ID_LOCAL_6);

try {
	Thread.currentThread();
	Thread.sleep(1000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}*/



/*modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,3);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,20);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,5);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,9);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,12);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,13);
//controllerId = Constants.CONTROLLER_ID_LOCAL_1;
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,7);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,14);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,17);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,19);*/


/*public void modify(IOFSwitch sw1){
//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
System.out.println("SW= " + sw1);
String ranIp = Constants.RAN_IP_1;
int controllerId = Constants.CONTROLLER_ID_ROOT;

try {
	Thread.currentThread();
	Thread.sleep(40000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,1);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,3);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,20);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,5);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,9);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,12);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,13);
//controllerId = Constants.CONTROLLER_ID_LOCAL_1;
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,7);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,14);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,17);
modifyDefaultSwitchNetRuleController(sw1,ranIp,controllerId,19);
}

public void modifyDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
//IPv4Address dstIp = IPv4Address.of(dst);
int ip_tos= msgId * 4;
//System.out.println((byte)ip_tos);
System.out.println("MODIFIED MESSAGE ROUTING RULE");
OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowModify();
Match.Builder mb = sw.getOFFactory().buildMatch();
//mb.setExact(MatchField.IN_PORT,OFPort.of(4));
mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
//mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(ranIp));
mb.setExact(MatchField.IP_DSCP,IpDscp.of((byte) ip_tos));
	
OFActionNiciraController.Builder builder = OFFactories.getFactory(OFVersion.OF_10).actions().buildNiciraController();
builder.setControllerId(controllerId);
builder.setMaxLen(0xffFFffFF);
fmb.setActions(Collections.singletonList(builder.build()));	

fmb.setHardTimeout(0)
.setIdleTimeout(0)
.setPriority(1)
.setBufferId(OFBufferId.NO_BUFFER)
.setMatch(mb.build());

sw.write(fmb.build());
}*/
