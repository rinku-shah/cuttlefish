/****************************************************************
 * This file contains code of MME and also contains code which  *
 * install/deletes flow rules from the default switch           *
 ****************************************************************/

package net.floodlightcontroller.kv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.IListener.Command;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KV implements IFloodlightModule, IOFMessageListener, IOFSwitchListener {
	private final Lock mutex = new ReentrantLock();
	
	protected static Logger log = LoggerFactory.getLogger(KV.class);
	private IFloodlightProviderService floodlightProvider;
	
	HashMap<DatapathId, Long> switchStats =  new HashMap<DatapathId, Long>();
	DatapathId defaultSwitch;
	DatapathId sgw_dpid;

	public static Long packetCount=(long) 0;
	protected IThreadPoolService threadPoolService;
	protected IOFSwitchService switchService;
	protected SingletonTask discoveryTask;
	Set<DatapathId> switches=null;
	int switchAddedCount = 0;
	FT ft = new FT();
	IOFSwitch sw = null;
	private Boolean flag = true;
	Date start, end;
	int period = 5000; //stats for 5sec		
	Long prevTot = (long)0 ;
	Long currTot= (long)0 ;
	float currRate = (float) 0.0;
	float prevRate = (float) 0.0;

	public KV(){

	}
	
	public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
		this.floodlightProvider = floodlightProvider;
	}

	@Override
	public String getName() {
		return KV.class.getPackage().getName();
	}

	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		if(flag){
			start = new Date();
			flag = false;
		}
		switch (msg.getType()) {
		case PACKET_IN:
			++packetCount;
			if(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1).equals(sw.getId()) || DatapathId.of(Constants.DEFAULT_SWITCH_ID_2).equals(sw.getId()) || DatapathId.of(Constants.DEFAULT_SWITCH_ID_3).equals(sw.getId()) || DatapathId.of(Constants.DEFAULT_SWITCH_ID_4).equals(sw.getId()) || DatapathId.of(Constants.DEFAULT_SWITCH_ID_5).equals(sw.getId()) || DatapathId.of(Constants.DEFAULT_SWITCH_ID_6).equals(sw.getId())) {
				defaultSwitch = sw.getId(); 
				try {
					return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return Command.CONTINUE;
		case ERROR:
			log.info("received an error {} from switch {}", msg, sw);
			return Command.CONTINUE;
		default:
			log.error("received an unexpected message {} from switch {}", msg, sw);
			return Command.CONTINUE;
		}
	}

	

	public String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
	
	private static void spin(int milliseconds) {
	    long sleepTime = milliseconds*1000000L; // convert to nanoseconds
	    long startTime = System.nanoTime();
	    while ((System.nanoTime() - startTime) < sleepTime) {}
	}
	
	private synchronized Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) throws InterruptedException {
		OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));

		/* Read packet header attributes into Match */
		Match m = createMatchFromPacket(sw, inPort, cntx);
		DatapathId sgw_dpId = null;
		MacAddress sourceMac = m.get(MatchField.ETH_SRC);
		MacAddress destMac = m.get(MatchField.ETH_DST);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		TransportPort srcPort=TransportPort.of(67), dstPort=TransportPort.of(67);
		IPv4Address srcIp, dstIp;

		if (sourceMac == null) {
			sourceMac = MacAddress.NONE;
		}

		if (destMac == null) {
			destMac = MacAddress.NONE;
		}
		if (vlan == null) {
			vlan = VlanVid.ZERO;
		}

		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if(eth.getEtherType() == EthType.IPv4){

			IPv4 ipPkt = (IPv4)eth.getPayload();
			srcIp = ipPkt.getSourceAddress();
			dstIp = ipPkt.getDestinationAddress();
			if(ipPkt.getProtocol().equals(IpProtocol.UDP)){
				UDP udpPkt = (UDP)ipPkt.getPayload();
				srcPort = udpPkt.getSourcePort();
				dstPort = udpPkt.getDestinationPort();

				Data dataPkt = null;
				if(Data.class.isInstance(udpPkt.getPayload())){
					dataPkt = (Data)udpPkt.getPayload();
					byte[] arr = dataPkt.getData();
					String payload = "";
					try {
						payload = new String(arr, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					if(Constants.DEBUG){
						if(!payload.startsWith("{") && !payload.startsWith("_ipps") && !payload.startsWith("�") && !payload.contains("arpa")){
							System.out.println("RECEIVED: "+payload);
						}
					}

					String tmpArray[];
									
					StringBuilder response;
					if(payload.contains(Constants.SEPARATOR)){
						tmpArray = payload.split(Constants.SEPARATOR);
						Date d1 = null, d2 = null;
						int step = 0;
						switch(tmpArray[0]){
						case Constants.GET:
							if(Constants.DEBUG){
								System.out.println("Inside case GET");
							}
							DatapathId dgw_dpId = Constants.getDgwDpidFromIp(srcIp.toString());
													
							String val = FT.get(dgw_dpId,"kv", tmpArray[1]);
														
							response = new StringBuilder();
							response.append(Constants.SERVERFOUND).append(Constants.SEPARATOR).append(val);
							
							//spin(2);
							
							sendPacket(sw, inPort, destMac, sourceMac, dstIp, srcIp, IpProtocol.UDP, dstPort, srcPort, response.toString());
									
							break;
							
						case Constants.PUT:
							if(Constants.DEBUG){
								System.out.println("Inside case PUT");
							}
							DatapathId dgw_dpId1 = Constants.getDgwDpidFromIp(srcIp.toString());
						
							FT.put(Integer.parseInt(Constants.PUT), dgw_dpId1, "kv", tmpArray[1], tmpArray[2]);
													
							response = new StringBuilder();
							response.append(Constants.SERVERFOUND).append(Constants.SEPARATOR);
							
							sendPacket(sw, inPort, destMac, sourceMac, dstIp, srcIp, IpProtocol.UDP, dstPort, srcPort, response.toString());
			
							break;
							
						case Constants.GETG:
							if(Constants.DEBUG){
								System.out.println("Inside case GET");
							}
							DatapathId dgw_dpId2 = Constants.getDgwDpidFromIp(srcIp.toString());

							String val1 = FT.get(dgw_dpId2,"kv", tmpArray[1]);

							response = new StringBuilder();
							response.append(Constants.SERVERFOUND).append(Constants.SEPARATOR).append(val1);
							
							//spin(2);
							
							sendPacket(sw, inPort, destMac, sourceMac, dstIp, srcIp, IpProtocol.UDP, dstPort, srcPort, response.toString());
									
							break;
							
						case Constants.PUTG:
							if(Constants.DEBUG){
								System.out.println("Inside case PUT");
							}
							DatapathId dgw_dpId3 = Constants.getDgwDpidFromIp(srcIp.toString());

							FT.put(Integer.parseInt(Constants.PUTG), dgw_dpId3, "kv", tmpArray[1], tmpArray[2]);
							
							response = new StringBuilder();
							response.append(Constants.SERVERFOUND).append(Constants.SEPARATOR);
							
							//spin(2);
							
							sendPacket(sw, inPort, destMac, sourceMac, dstIp, srcIp, IpProtocol.UDP, dstPort, srcPort, response.toString());
			
							break;
							
						default: 
							System.out.println("Entered Default");
							break;
						}
					}
				}
			}
		}
		return Command.CONTINUE;
	}

	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	

	// uplink rule
	//UPLINK RULE: dpid,inPort,srcIp,srcPort,server_id(MAC,IP,Ofport)
	//DOWNLINK RULE: dpid,server_id(port),srcIP,srcPort,destMac,dstIP,inPort
		private void installUplink(DatapathId dpId, OFPort inPort, IPv4Address srcIp, TransportPort srcPort, String serverMac, String serverIp, int serverOfPort){
			//if(sw == null){
				sw = switchService.getSwitch(dpId);
			//}
			OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
			Match.Builder mb = sw.getOFFactory().buildMatch();


			mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
			mb.setExact(MatchField.IN_PORT, inPort);
			mb.setExact(MatchField.IPV4_SRC, srcIp);
			mb.setExact(MatchField.UDP_SRC, srcPort);

			List<OFAction> actions = new ArrayList<OFAction>();
			if(serverIp != "")
				actions.add(sw.getOFFactory().actions().setNwDst(IPv4Address.of(serverIp)));
			actions.add(sw.getOFFactory().actions().setDlDst(MacAddress.of(serverMac)));
			actions.add(sw.getOFFactory().actions().output(OFPort.of(serverOfPort), Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
			fmb.setActions(actions);

			fmb.setHardTimeout(0)
			.setIdleTimeout(0)
			.setPriority(1)
			.setBufferId(OFBufferId.NO_BUFFER)
			.setMatch(mb.build());

			sw.write(fmb.build());
		}
		
		//UPLINK RULE: dpid,inPort,srcIp,srcPort,server_id(MAC,IP,Ofport)
		//DOWNLINK RULE: dpid,server_id(port),srcIP,srcPort,destMac,dstIP,inPort
			private void installDownlink(DatapathId dpId, OFPort inPort, IPv4Address srcIp, IPv4Address dstIp, MacAddress dstMac, TransportPort srcPort, String serverMac, String serverIp, int serverOfPort){
				//if(sw == null){
					sw = switchService.getSwitch(dpId);
				//}
				OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
				Match.Builder mb = sw.getOFFactory().buildMatch();


				mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
				mb.setExact(MatchField.IN_PORT, OFPort.of(serverOfPort));
				mb.setExact(MatchField.IPV4_DST, srcIp);
				mb.setExact(MatchField.UDP_DST, srcPort);

				List<OFAction> actions = new ArrayList<OFAction>();
				
				actions.add(sw.getOFFactory().actions().setNwSrc(dstIp));
				actions.add(sw.getOFFactory().actions().setDlSrc(dstMac));
				actions.add(sw.getOFFactory().actions().output(inPort, Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
				fmb.setActions(actions);

				fmb.setHardTimeout(0)
				.setIdleTimeout(0)
				.setPriority(1)
				.setBufferId(OFBufferId.NO_BUFFER)
				.setMatch(mb.build());

				sw.write(fmb.build());
			}
	
	
			// uplink rule
			//UPLINK RULE: dpid,inPort,srcIp,srcPort,server_id(MAC,IP,Ofport)
			//DOWNLINK RULE: dpid,server_id(port),srcIP,srcPort,destMac,dstIP,inPort
				private void deleteUplink(DatapathId dpId, OFPort inPort, IPv4Address srcIp, TransportPort srcPort, String serverMac, String serverIp, int serverOfPort){
					//if(sw == null){
						sw = switchService.getSwitch(dpId);
					//}
					OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
					Match.Builder mb = sw.getOFFactory().buildMatch();


					mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
					mb.setExact(MatchField.IN_PORT, inPort);
					mb.setExact(MatchField.IPV4_SRC, srcIp);
					mb.setExact(MatchField.UDP_SRC, srcPort);

					List<OFAction> actions = new ArrayList<OFAction>();
					if(serverIp != "")
						actions.add(sw.getOFFactory().actions().setNwDst(IPv4Address.of(serverIp)));
					actions.add(sw.getOFFactory().actions().setDlDst(MacAddress.of(serverMac)));
					actions.add(sw.getOFFactory().actions().output(OFPort.of(serverOfPort), Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
					fmb.setActions(actions);

					fmb.setHardTimeout(0)
					.setIdleTimeout(0)
					.setPriority(1)
					.setBufferId(OFBufferId.NO_BUFFER)
					.setMatch(mb.build());

					sw.write(fmb.build());
				}
				
				//UPLINK RULE: dpid,inPort,srcIp,srcPort,server_id(MAC,IP,Ofport)
				//DOWNLINK RULE: dpid,server_id(port),srcIP,srcPort,destMac,dstIP,inPort
					private void deleteDownlink(DatapathId dpId, OFPort inPort, IPv4Address srcIp, IPv4Address dstIp, MacAddress dstMac, TransportPort srcPort, String serverMac, String serverIp, int serverOfPort){
						//if(sw == null){
							sw = switchService.getSwitch(dpId);
						//}
						OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
						Match.Builder mb = sw.getOFFactory().buildMatch();


						mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
						mb.setExact(MatchField.IN_PORT, OFPort.of(serverOfPort));
						mb.setExact(MatchField.IPV4_DST, srcIp);
						mb.setExact(MatchField.UDP_DST, srcPort);

						List<OFAction> actions = new ArrayList<OFAction>();
						
						actions.add(sw.getOFFactory().actions().setNwSrc(dstIp));
						actions.add(sw.getOFFactory().actions().setDlSrc(dstMac));
						actions.add(sw.getOFFactory().actions().output(inPort, Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
						fmb.setActions(actions);

						fmb.setHardTimeout(0)
						.setIdleTimeout(0)
						.setPriority(1)
						.setBufferId(OFBufferId.NO_BUFFER)
						.setMatch(mb.build());

						sw.write(fmb.build());
					}
					
		
	//delete uplink rule
	private void deleteFlowRuleWithIP(DatapathId dpId, int inPort, String ue_ip){
		if(sw == null){
			sw = switchService.getSwitch(dpId);
		}
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
		Match.Builder mb = sw.getOFFactory().buildMatch();

		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		.setExact(MatchField.IN_PORT, OFPort.of(inPort))
		.setExact(MatchField.IPV4_SRC, IPv4Address.of(ue_ip));

		fmb.setMatch(mb.build());
		sw.write(fmb.build());
	}

	//delete downlink rule
	private void deleteFlowRuleWithTEID(DatapathId dpId, int inPort, int ue_teid, String srcIP){
		if(sw == null){
			sw = switchService.getSwitch(dpId);
		}
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowDelete();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		.setExact(MatchField.IN_PORT, OFPort.of(inPort))
		.setExact(MatchField.IPV4_SRC, IPv4Address.of(srcIP))
		.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(VlanVid.ofVlan(ue_teid)));

		fmb.setMatch(mb.build());
		sw.write(fmb.build());
	}

	// uplink rule
	private void installFlowRuleWithIP(DatapathId dpId, int inPort, int outPort, int outTunnelId, String UE_IP, String srcIP, String dstIP, String dstMac){
		//if(sw == null){
			sw = switchService.getSwitch(dpId);
		//}
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		Match.Builder mb = sw.getOFFactory().buildMatch();


		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		mb.setExact(MatchField.IN_PORT, OFPort.of(inPort));
		mb.setExact(MatchField.IPV4_SRC, IPv4Address.of(UE_IP));

		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(sw.getOFFactory().actions().setVlanVid(VlanVid.ofVlan(outTunnelId)));

		if(dstIP != "")
			actions.add(sw.getOFFactory().actions().setNwDst(IPv4Address.of(dstIP)));

		actions.add(sw.getOFFactory().actions().setDlDst(MacAddress.of(dstMac)));
		actions.add(sw.getOFFactory().actions().output(OFPort.of(outPort), Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
		fmb.setActions(actions);

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
	}

	//downlink rule
	private void installFlowRule(DatapathId dpId, int inPort, int inTunnelId, int outPort, int outTunnelId, String srcIP, String dstIP, String dstMac){
		//if(sw == null){
			sw = switchService.getSwitch(dpId);
		//}
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		.setExact(MatchField.IN_PORT, OFPort.of(inPort))
		.setExact(MatchField.IPV4_SRC, IPv4Address.of(srcIP))
		.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(VlanVid.ofVlan(inTunnelId)));

		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(sw.getOFFactory().actions().setVlanVid(VlanVid.ZERO));//ofVlan(outTunnelId)
		actions.add(sw.getOFFactory().actions().setNwDst(IPv4Address.of(dstIP)));
		actions.add(sw.getOFFactory().actions().setDlDst(MacAddress.of(dstMac)));
		actions.add(sw.getOFFactory().actions().output(OFPort.of(outPort), Integer.MAX_VALUE)); // FLOOD is a more selective/efficient version of ALL
		fmb.setActions(actions);

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
	}

	public DatapathId selectS_GW(){
		DatapathId minLoadedSwitch = null;
		Long minBytes = 0l;
		boolean first_time = true;
		for (Map.Entry<DatapathId, Long> entry : switchStats.entrySet()) {
			if(first_time){
				minLoadedSwitch = entry.getKey();
				minBytes = entry.getValue();
				first_time = false;
			}else{
				if(entry.getValue() < minBytes){
					minBytes = entry.getValue();
					minLoadedSwitch = entry.getKey();
				}
			}
		}
		return minLoadedSwitch;
	}

	/*
	 * create and send packet to default switch on port in which it arrived.
	 */
	public boolean sendPacket(IOFSwitch sw, OFPort outPort, MacAddress srcMac, MacAddress dstMac, 
			IPv4Address srcIP, IPv4Address dstIP, IpProtocol proto, 
			TransportPort srcPort, TransportPort dstPort, String data){

		try{
			//sending packet in response
			OFPacketOut.Builder pktNew = sw.getOFFactory().buildPacketOut();
			pktNew.setBufferId(OFBufferId.NO_BUFFER);

			Ethernet ethNew = new Ethernet();
			ethNew.setSourceMACAddress(srcMac);
			ethNew.setDestinationMACAddress(dstMac);
			ethNew.setEtherType(EthType.IPv4);

			IPv4 ipNew = new IPv4();
			ipNew.setSourceAddress(srcIP);
			ipNew.setDestinationAddress(dstIP);

			ipNew.setProtocol(proto);
			ipNew.setTtl((byte) 64);

			UDP updNew = new UDP();
			updNew.setSourcePort(srcPort);
			updNew.setDestinationPort(dstPort);

			Data dataNew = new Data();
			dataNew.setData(data.getBytes());

			//putting it all together
			ethNew.setPayload(ipNew.setPayload(updNew.setPayload(dataNew)));

			// set in-port to OFPP_NONE
			pktNew.setInPort(OFPort.ZERO);
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(sw.getOFFactory().actions().output(outPort, 0xffFFffFF));

			pktNew.setActions(actions);
			pktNew.setData(ethNew.serialize());

			sw.write(pktNew.build());
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {
		// The packet in match will only contain the port number.
		// We need to add in specifics for the hosts we're routing between.
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, srcMac)
		.setExact(MatchField.ETH_DST, dstMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}

		return mb.build();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

	// IFloodlightModule

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// We don't provide any services, return null
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService>
	getServiceImpls() {
		// We don't provide any services, return null
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>>
	getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = 
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		switchService.addOFSwitchListener(this);
	}


	@Override
	public void switchRemoved(DatapathId switchId) {
		System.out.println("SWITCH REMOVED: "+switchId);
		switchStats.remove(switchId);

	}
	
	@Override
	public void switchActivated(DatapathId switchId) {
	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port,
			PortChangeType type) {
	}

	@Override
	public void switchChanged(DatapathId switchId) {
	}

	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchAdded(DatapathId switchId) {
		IOFSwitch sw = switchService.getSwitch(switchId);
			OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
			Match.Builder mb = sw.getOFFactory().buildMatch();
			mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
	
			OFActionOutput.Builder actionBuilder = sw.getOFFactory().actions().buildOutput();
			actionBuilder.setPort(OFPort.NORMAL);
			fmb.setActions(Collections.singletonList((OFAction) actionBuilder.build()));	
			fmb.setHardTimeout(0)
			.setIdleTimeout(0)
			.setPriority(1)
			.setBufferId(OFBufferId.NO_BUFFER)
			.setMatch(mb.build());
			sw.write(fmb.build());
/////////////////////////////////// CODE TO DIVERT FLOWS TO SPECIFIC CONTROLLER /////////////////////////////////////////
			
			IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1));
			IOFSwitch sw2 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2));
			IOFSwitch sw3 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3));
			IOFSwitch sw4 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4));
			IOFSwitch sw5 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5));
			IOFSwitch sw6 = switchService.getSwitch(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6));
			
			OnlineThread o = new OnlineThread(sw1,sw2,sw3,sw4,sw5,sw6);
			//OnlineDesignAdaptation Online = new OnlineDesignAdaptation(sw1,sw2);
			
			//initCentralizedDesign(switchId, sw);
									
			initRemoteDesign(switchId, sw);
			
			switchAddedCount++;
	
			if (switchAddedCount == 6){
					
				//o.sendPacketToLocal("StartCentralized");
				o.sendPacketToLocal("StartRemote");
				
				Runnable Online = new OnlineThread(sw1,sw2,sw3,sw4,sw5,sw6);
				Thread t2 = new Thread(Online);
				t2.start();
				
				Runnable Cut = new CutThread();
				Thread t1 = new Thread(Cut);
				t1.start();	
				//////
				
			}
			
	}
	
	public void initCentralizedDesign(DatapathId switchId, IOFSwitch sw){
		IOFSwitch sw_t = sw;
		DatapathId switchId_t = switchId;
		Constants.CENTRALIZED = true; 
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			{
				String ranIp = Constants.RAN_IP_1;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);
				
			}
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			{
				String ranIp = Constants.RAN_IP_2;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);

			}
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
			{
				String ranIp = Constants.RAN_IP_3;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);

			}
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
			{
				String ranIp = Constants.RAN_IP_4;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);

			}
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
			{
				String ranIp = Constants.RAN_IP_5;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);

			}
			if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
			{
				String ranIp = Constants.RAN_IP_6;
				int controllerId = Constants.CONTROLLER_ID_ROOT;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,4);

			}
			
	}
	
	public void initRemoteDesign(DatapathId switchId, IOFSwitch sw){
		IOFSwitch sw_t = sw;
		DatapathId switchId_t = switchId;
		Constants.CENTRALIZED = false;
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_1)))
			{
				String ranIp = Constants.RAN_IP_1;
				int root = Constants.CONTROLLER_ID_ROOT;
				int controllerId = Constants.CONTROLLER_ID_LOCAL_1;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				
				addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);

			}
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_2)))
			{
				String ranIp = Constants.RAN_IP_2;
				int root = Constants.CONTROLLER_ID_ROOT;
				int controllerId = Constants.CONTROLLER_ID_LOCAL_2;
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
				addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
				
				addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
				addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);
			}
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_3)))
		{
			String ranIp = Constants.RAN_IP_3;
			int root = Constants.CONTROLLER_ID_ROOT;
			int controllerId = Constants.CONTROLLER_ID_LOCAL_3;
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
			
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);
		}
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_4)))
		{
			String ranIp = Constants.RAN_IP_4;
			int root = Constants.CONTROLLER_ID_ROOT;
			int controllerId = Constants.CONTROLLER_ID_LOCAL_4;
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
			
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);
		}
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_5)))
		{
			String ranIp = Constants.RAN_IP_5;
			int root = Constants.CONTROLLER_ID_ROOT;
			int controllerId = Constants.CONTROLLER_ID_LOCAL_5;
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
			
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);
		}
		if (switchId_t.equals(DatapathId.of(Constants.DEFAULT_SWITCH_ID_6)))
		{
			String ranIp = Constants.RAN_IP_6;
			int root = Constants.CONTROLLER_ID_ROOT;
			int controllerId = Constants.CONTROLLER_ID_LOCAL_6;
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,1);
			addDefaultSwitchNetRuleController(sw_t,ranIp,controllerId,3);
			
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,2);
			addDefaultSwitchNetRuleController(sw_t,ranIp,root,4);
		}
	}
	
	public void addDefaultSwitchNetRuleController(IOFSwitch sw, String ranIp, int controllerId, int msgId) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		int ip_tos= msgId * 4;
		//System.out.println((byte)ip_tos);
		System.out.println("PUSH MESSAGE ROUTING RULE");
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
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

	
	public void addDefaultSwitchNetRule(IOFSwitch sw, int msg_id) {
		//IPv4Address dstIp = IPv4Address.of(dst);
		//int ip_tos= msg_id * 4;
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT,OFPort.of(4));
		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		mb.setExact(MatchField.ETH_SRC,MacAddress.of("00:16:3e:1f:c0:12"));
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		
		OFActionSetNwDst action1 = sw.getOFFactory().actions().setNwDst(IPv4Address.of("10.128.41.1"));

		OFActionOutput action4 = sw.getOFFactory().actions().buildOutput()
				    .setPort(OFPort.CONTROLLER)
			        .setMaxLen(0xffFFffFF)
			        .build();
		OFActionOutput action5 = sw.getOFFactory().actions().buildOutput()
				.setPort(OFPort.of(6))
		        .build();

		actionList.add(action1);

		actionList.add(action5);

		actionList.add(action4);
		fmb.setActions(actionList);
	
		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		//.setOutPort(OFPort.of(6))
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
		
	}
	
	public void addDefaultSwitchNetRule_SGW(IOFSwitch sw, String dst) {
		IPv4Address dstIp = IPv4Address.of(dst);
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IPV4_DST,dstIp);
		OFActionOutput.Builder actionBuilder = sw.getOFFactory().actions().buildOutput();
		actionBuilder.setPort(OFPort.NORMAL);
		fmb.setActions(Collections.singletonList((OFAction) actionBuilder.build()));

		fmb.setHardTimeout(0)
		.setIdleTimeout(0)
		.setPriority(1)
		.setBufferId(OFBufferId.NO_BUFFER)
		.setMatch(mb.build());

		sw.write(fmb.build());
		
	}
}

	
