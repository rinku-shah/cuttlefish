package net.floodlightcontroller.sdnepcCuttlefish;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.FloodlightProvider;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounter;
import net.floodlightcontroller.storage.IStorageSourceService;

import org.projectfloodlight.openflow.protocol.OFControllerRole;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFRoleReply;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.sdnplatform.sync.IStoreClient;
import org.sdnplatform.sync.IStoreListener;
import org.sdnplatform.sync.ISyncService;
import org.sdnplatform.sync.ISyncService.Scope;
import org.sdnplatform.sync.Versioned;
import org.sdnplatform.sync.error.SyncException;
import org.sdnplatform.sync.internal.rpc.IRPCListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sdnplatform.sync.internal.SyncManager;

public class FT implements 
IOFMessageListener, 
IFloodlightModule,
IStoreListener<String>,
IOFSwitchListener,
IRPCListener
{

	public ISyncService syncService;
	private static IStoreClient<String, String> storeEPC1;
	private static IStoreClient<String, String> storeEPC2;
	private static IStoreClient<String, String> storeEPC3;
	private static IStoreClient<String, String> storeEPC4;
	private static IStoreClient<String, String> storeEPC5;
	private static IStoreClient<String, String> storeEPC6;
	protected static Logger logger = LoggerFactory.getLogger(FT.class);
	protected static IOFSwitchService switchService;
	private String controllerId;
		
	public static ConcurrentHashMap<String, String> uekey_sgw_teid_map = new ConcurrentHashMap<String, String>();
	public static ConcurrentHashMap<String, String> sgw_teid_uekey_map = new ConcurrentHashMap<String, String>();
	public static ConcurrentHashMap<String, String> uekey_ueip_map = new ConcurrentHashMap<String, String>();
	public static ConcurrentHashMap<String, String> ue_state = new ConcurrentHashMap<String, String>();	// Key: UE_Key, Value: State (TRUE: Active, FALSE: Idle)
	public static ConcurrentHashMap<String, String> SGW_PGW_TEID_MAP = new ConcurrentHashMap<String, String>();
//	private static ConcurrentHashMap<String, TransportPort> uekey_udp_src_port_map = new ConcurrentHashMap<String, TransportPort>(); // key = ue key, Value = UE UDP port number

	static Long putCount=(long) 0;
	
	static Long getCount=(long) 0;

	static Long[] putArr = new Long[100]; 


	public static void myStats() {
		String[] getStats1 = SyncManager.counterGets.toString().split(",");
		String[] getStats2 = getStats1[3].toString().split("=");
		String[] getStats3 = getStats2[1].toString().split("]");
		System.out.println("NUMBER of GETS= "+getStats3[0].toString());
	}
	
	
	public static void migrateLocalStore(ConcurrentHashMap<String, String> hm, String HMname){
		for (Entry<String, String> entry : hm.entrySet()) {
		    String key = entry.getKey();
		    String val = entry.getValue();
		try {
			String[] keyArray = null;
			keyArray = key.split(Constants.STOREKEYSEPARATOR);
			StringBuilder k = new StringBuilder();
			k.append(HMname).append(Constants.MAPKEYSEPARATOR).append(keyArray[1]);
			//System.out.println("keyArray= "+ keyArray + " Val = "+ val + " key = " + k);
			if (keyArray[0].equals("storeEPC1")){
				storeEPC1.put(k.toString(), val);
			} else if (keyArray[0].equals("storeEPC2")){
				storeEPC2.put(k.toString(), val);
			} else if (keyArray[0].equals("storeEPC3")){
				storeEPC3.put(k.toString(), val);
			} else if (keyArray[0].equals("storeEPC4")){
				storeEPC4.put(k.toString(), val);
			} else if (keyArray[0].equals("storeEPC5")){
				storeEPC5.put(k.toString(), val);
			} else if (keyArray[0].equals("storeEPC6")){
				storeEPC6.put(k.toString(), val);
			}
		} catch (SyncException e) {
		    e.printStackTrace();
		}
		}
		hm.clear();
	}

	public static void printLocalStore(){
		System.out.println("PRINTING STOREEPC1");
		try {
			while(storeEPC1.entries().hasNext()){
				Entry<String, Versioned<String>> e = storeEPC1.entries().next();
				String k = e.getKey();
				String val = e.getValue().toString();
				System.out.println("key= "+ k + " Val = "+ val);
					
			}
		} catch (SyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void put(int msgId,DatapathId dgwDpId, String mapName, String key, String val){
		putCount++;
		putArr[msgId]++;
		//if(Constants.CENTRALIZED && !Constants.MIGRATING){
		if(Constants.CENTRALIZED){
			/*putCount++;*/
			String tmpStore = FT.getStoreNameFromDGW(dgwDpId);
			StringBuilder k = new StringBuilder();
			k.append(tmpStore).append(Constants.STOREKEYSEPARATOR).append(key);
			if(mapName.equals("uekey_sgw_teid_map")){
				uekey_sgw_teid_map.put(k.toString(),val);
			}
			else if(mapName.equals("sgw_teid_uekey_map")){
				sgw_teid_uekey_map.put(k.toString(),val);
			}
			else if(mapName.equals("uekey_ueip_map")){
				uekey_ueip_map.put(k.toString(),val);
			}
			else if(mapName.equals("ue_state")){
				ue_state.put(k.toString(),val);
			}
			else if(mapName.equals("SGW_PGW_TEID_MAP")){
				SGW_PGW_TEID_MAP.put(k.toString(),val);
			}
		}
		else {
			try {
				IStoreClient<String, String> tmpStore = FT.getStoreFromDGW(dgwDpId);
				StringBuilder k = new StringBuilder();
				k.append(mapName).append(Constants.MAPKEYSEPARATOR).append(key);
			    tmpStore.put(k.toString(), val);
			} catch (SyncException e) {
			    e.printStackTrace();
			}
		}
		
	}	
	
	public static String get(DatapathId dgwDpId,String mapName, String key) {
		String val=null;
		//getCount++;
		getCount++;
		if(Constants.CENTRALIZED || Constants.MIGRATING){
		//if(Constants.CENTRALIZED){
		//if(Constants.CENTRALIZED && !Constants.MIGRATING){
			//getCount++;
			String tmpStore = FT.getStoreNameFromDGW(dgwDpId);
			StringBuilder k = new StringBuilder();
			k.append(tmpStore).append(Constants.STOREKEYSEPARATOR).append(key);
			
			if(mapName.equals("uekey_sgw_teid_map")){
				val = uekey_sgw_teid_map.get(k.toString());
			}
			else if(mapName.equals("sgw_teid_uekey_map")){
				val = sgw_teid_uekey_map.get(k.toString());
			}
			else if(mapName.equals("uekey_ueip_map")){
				val = uekey_ueip_map.get(k.toString());
			}
			else if(mapName.equals("ue_state")){
				val = ue_state.get(k.toString());
			}
			else if(mapName.equals("SGW_PGW_TEID_MAP")){
				val = SGW_PGW_TEID_MAP.get(k.toString());
			}
			if(val != null){
				return val;
			}
		}
			final Future<String>  task;
			ExecutorService executor = Executors.newFixedThreadPool(50);
			Callable<String> callable = new GetThread(dgwDpId, mapName, key);
	        task = executor.submit(callable);
	        String value="none";
			try {
				try {
					value = task.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        //System.out.println("The returned value is : "+value);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        executor.shutdown();
	        return value;     
	}
	
	public static void del(int msgId,DatapathId dgwDpId,String mapName, String key){
		//putCount++;
		//putArr[msgId]++;
		if(Constants.CENTRALIZED){
			//String val="none";
			String tmpStore = FT.getStoreNameFromDGW(dgwDpId);
			StringBuilder k = new StringBuilder();
			k.append(tmpStore).append(Constants.STOREKEYSEPARATOR).append(key);
			
			if(mapName.equals("uekey_sgw_teid_map")){
				uekey_sgw_teid_map.remove(k.toString());
			}
			else if(mapName.equals("sgw_teid_uekey_map")){
				sgw_teid_uekey_map.remove(k.toString());
			}
			else if(mapName.equals("uekey_ueip_map")){
				uekey_ueip_map.remove(k.toString());
			}
			else if(mapName.equals("ue_state")){
				ue_state.remove(k.toString());
			}
			else if(mapName.equals("SGW_PGW_TEID_MAP")){
				SGW_PGW_TEID_MAP.remove(k.toString());
			}
		}
		else {	
			try {
				IStoreClient<String, String> tmpStore = getStoreFromDGW(dgwDpId);
				StringBuilder k = new StringBuilder();
				k.append(mapName).append(Constants.MAPKEYSEPARATOR).append(key);
			    tmpStore.delete(k.toString());
			} catch (SyncException e) {
			    e.printStackTrace();
			}
		}
	}
	

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IStorageSourceService.class);
		l.add(ISyncService.class);
		l.add(IOFSwitchService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		
		this.syncService = context.getServiceImpl(ISyncService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);

		Map<String, String> configParams = context.getConfigParams(FloodlightProvider.class);
		controllerId = configParams.get("controllerId");
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		
		for(int i=0; i<100; i++){
			putArr[i] = (long) 0;
		}
		
		switchService.addOFSwitchListener(this);
		syncService.addRPCListener(this);
		try {
			this.syncService.registerStore("SDN_LTE-EPC_1", Scope.LOCAL);
			FT.storeEPC1 = this.syncService
					.getStoreClient("SDN_LTE-EPC_1",
							String.class,
							String.class);
			FT.storeEPC1.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
		
		try {
			this.syncService.registerStore("SDN_LTE-EPC_2", Scope.LOCAL);
			FT.storeEPC2 = this.syncService
					.getStoreClient("SDN_LTE-EPC_2",
							String.class,
							String.class);
			FT.storeEPC2.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
		
		try {
			this.syncService.registerStore("SDN_LTE-EPC_3", Scope.LOCAL);
			FT.storeEPC3 = this.syncService
					.getStoreClient("SDN_LTE-EPC_3",
							String.class,
							String.class);
			FT.storeEPC3.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
		
		try {
			this.syncService.registerStore("SDN_LTE-EPC_4", Scope.LOCAL);
			FT.storeEPC4 = this.syncService
					.getStoreClient("SDN_LTE-EPC_4",
							String.class,
							String.class);
			FT.storeEPC4.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
		
		try {
			this.syncService.registerStore("SDN_LTE-EPC_5", Scope.LOCAL);
			FT.storeEPC5 = this.syncService
					.getStoreClient("SDN_LTE-EPC_5",
							String.class,
							String.class);
			FT.storeEPC5.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
		
		try {
			this.syncService.registerStore("SDN_LTE-EPC_6", Scope.LOCAL);
			FT.storeEPC6 = this.syncService
					.getStoreClient("SDN_LTE-EPC_6",
							String.class,
							String.class);
			FT.storeEPC6.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void keysModified(Iterator<String> keys,
			org.sdnplatform.sync.IStoreListener.UpdateType type) {
		// TODO Auto-generated method stub
			while(keys.hasNext()){
				String k = keys.next();
				try {

					if(type.name().equals("REMOTE")){
						String swIds = storeEPC1.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
						swIds = storeEPC2.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
						swIds = storeEPC3.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
						swIds = storeEPC4.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
						swIds = storeEPC5.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
						swIds = storeEPC6.get(k).getValue();
						logger.debug("REMOTE: NodeId:{}, Switches:{}", k, swIds);
					}
				} catch (SyncException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}

	@Override
	public void switchAdded(DatapathId switchId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// TODO Auto-generated method stub
		String activeSwitches = getActiveSwitchesAndUpdateSyncInfo();
		logger.debug("Switch REMOVED: {}, Syncing: {}", switchId, activeSwitches);
	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// TODO Auto-generated method stub
		String activeSwitches = getActiveSwitchesAndUpdateSyncInfo();
		logger.debug("Switch ACTIVATED: {}, Syncing: {}", switchId, activeSwitches);
		
	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port,
			PortChangeType type) {
		// TODO Auto-generated method stub
		logger.debug("Switch Port CHANGED: {}", switchId);
	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub
		String activeSwitches = getActiveSwitchesAndUpdateSyncInfo();
		logger.debug("Switch CHANGED: {}, Syncing: {}", switchId, activeSwitches);
		
	}
	
	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub
		String activeSwitches = getActiveSwitchesAndUpdateSyncInfo();
		logger.debug("Switch DEACTIVATED: {}, Syncing: {}", switchId, activeSwitches);
	}


	public String getActiveSwitchesAndUpdateSyncInfo(){
		String activeSwitches = "";
		if(switchService == null)
			return "";
		
		Iterator<DatapathId> itDpid = switchService.getAllSwitchDpids().iterator();
		while (itDpid.hasNext()) {
			DatapathId dpid = itDpid.next();
			try{
				if(switchService.getActiveSwitch(dpid).isActive()){
					activeSwitches += dpid;
					if(itDpid.hasNext())
						activeSwitches += ",";	
				}
			}
			catch(NullPointerException npe){
				return "";
			}
		}
		
		if(activeSwitches.equals(""))
			return "";
		
		try {
			FT.storeEPC1.put(controllerId, activeSwitches);
			FT.storeEPC2.put(controllerId, activeSwitches);
			FT.storeEPC3.put(controllerId, activeSwitches);
			FT.storeEPC4.put(controllerId, activeSwitches);
			FT.storeEPC5.put(controllerId, activeSwitches);
			FT.storeEPC6.put(controllerId, activeSwitches);
			return activeSwitches;
		} catch (SyncException e) {
			e.printStackTrace();
			return "";
		}
		
	}
	
	public void setSwitchRole(OFControllerRole role, String swId){
		IOFSwitch sw = switchService.getActiveSwitch(DatapathId.of(swId));
		OFRoleReply reply=null;
		UtilDurable utilDurable = new UtilDurable();
		reply = utilDurable.setSwitchRole(sw, role);
		
		if(reply!=null){
			logger.info("DEFINED {} as {}, reply.getRole:{}!", 
					new Object[]{
					sw.getId(), 
					role,
					reply.getRole()});
		}
		else
			logger.info("Reply NULL!");
	}

	@Override
	public void disconnectedNode(Short nodeId) {
		// TODO Auto-generated method stub
		String swIds=null; 
		try {
			swIds = storeEPC1.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);	
			swIds = storeEPC2.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);
			swIds = storeEPC3.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);
			swIds = storeEPC4.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);
			swIds = storeEPC5.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);
			swIds = storeEPC6.get(""+nodeId).getValue();
			logger.debug("Switches managed by nodeId:{}, Switches:{}", nodeId, swIds);
		} catch (SyncException e) {
			e.printStackTrace();
		}
		
		if(swIds != null){
			String swId[] = swIds.split(",");
			for (int i = 0; i < swId.length; i++) {
				setSwitchRole(OFControllerRole.ROLE_EQUAL, swId[i]);	
			}	
		}
	}

	@Override
	public void connectedNode(Short nodeId) {
		// TODO Auto-generated method stub
		String activeSwicthes = getActiveSwitchesAndUpdateSyncInfo();
		logger.debug("NodeID:{} connected, sending my Switches: {}", nodeId, activeSwicthes);
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return FT.class.getSimpleName();
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
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static IStoreClient<String,String> getStoreFromDGW(DatapathId dgw){
		IStoreClient<String,String> store = null;
		if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			store = storeEPC1;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			store = storeEPC2;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
			store = storeEPC3;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
			store = storeEPC4;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
			store = storeEPC5;
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
			store = storeEPC6;
		return store;
	}
	
	public static String getStoreNameFromDGW(DatapathId dgw){
		String store = null;
		if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			store = "storeEPC1";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			store = "storeEPC2";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
			store = "storeEPC3";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
			store = "storeEPC4";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
			store = "storeEPC5";
		else if (dgw.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
			store = "storeEPC6";
		return store;
	}
}

class GetThread extends Thread implements Callable<String> {
	private static DatapathId dgwDpId;
	private static String mapName;
	private static String key;
	
	public GetThread(DatapathId dgwDpId, String mapName, String key) {
		GetThread.dgwDpId = dgwDpId;
		GetThread.mapName = mapName;
		GetThread.key = key;
	}

	@Override
	public String call() throws InterruptedException {
		Boolean b = false;
		String tmp = "";
		String defaultValue = "none";
		//int count = 0;
	
			IStoreClient<String, String> tmpStore = FT.getStoreFromDGW(dgwDpId);
			StringBuilder k = new StringBuilder();
			k.append(mapName).append(Constants.MAPKEYSEPARATOR).append(key);
			try {
				tmp = tmpStore.getValue(k.toString(), defaultValue);
			} catch (SyncException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/*if(tmp.equals(defaultValue) && Constants.MIGRATING){
				tmp = FT.getLocal(dgwDpId, mapName, key);
			}*/
			while (tmp.equals(defaultValue)){
				try {
					GetThread.currentThread();
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					tmp = tmpStore.getValue(k.toString(), defaultValue);
				} catch (SyncException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			return tmp;
	}
}
