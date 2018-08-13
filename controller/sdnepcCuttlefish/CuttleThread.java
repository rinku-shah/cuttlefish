package net.floodlightcontroller.sdnepcCuttlefish;

import java.io.BufferedReader;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.IOFSwitch;

public class CuttleThread {
	static float G;
	static float P;
	static float T;
	static float Put[] = new float[100];
	public CuttleThread() {
		CutThread t;
		t = new CutThread();
		t.start();
	}

}

class CutThread implements Runnable {
	Long prevG, prevP, currG, currP, prevT, currT;
	float diffG, diffP, diffT;
	Long prevPut[] = new Long[100];
	Long currPut[] = new Long[100];
	float diffPut[] = new float[100];
	int putLen = 100;
	
	int period = 20000; //stats for 30sec	//30000 replaced by 10000
	
	public CutThread() {

	}

	public void start() {
		// TODO Auto-generated method stub
		for(int i=0; i<putLen; i++){
			prevPut[i]= (long)0;
			currPut[i]= (long)0;
			diffPut[i]= (long)0;
			CuttleThread.Put[i] = (float) 0;
		}
		System.out.println("Cuttle Thread started");
	}

	@Override
	public void run() {
		System.out.println("Cuttle Thread run");
		/*/////////////////////////// BENCHMARKING CODE STARTS /////////////////////////////////////////////////////////////
		///RESULTS//
		time elapsed in ms: 8651 puts/sec=3468
		time elapsed in ms: 3969
		time elapsed in ms: 1368
		time elapsed in ms: 967
		////////////
		 		
		try {
			Thread.currentThread();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Inside CutThread");
		
		while(true){
		
		long startTime = System.currentTimeMillis();
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "uekey_sgw_teid_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(2, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "sgw_teid_uekey_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(3, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "uekey_ueip_map", Integer.toString(i), Integer.toString(i+1));
			FT.put(4, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "ue_state", Integer.toString(i), Integer.toString(i+1));
			FT.put(5, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "SGW_PGW_TEID_MAP", Integer.toString(i), Integer.toString(i+1));
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("time elapsed in ms: " + estimatedTime);
		}
		
		/////////////////////////// BENCHMARKING CODE ENDS /////////////////////////////////////////////////////////////
		*/
		
		try {
			Thread.currentThread();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		prevG = FT.getCount;
		prevP = FT.putCount;
		prevT = MME.packetCount;
		for(int j=0; j<putLen; j++){
			prevPut[j] = FT.putArr[j];
		}
		
		while(true){
			try {
				Thread.currentThread();
				Thread.sleep(period);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currG = FT.getCount;
			currP = FT.putCount;
			currT = MME.packetCount;
			for(int j=0; j<putLen; j++){
				currPut[j] = FT.putArr[j];
			}
			
			diffG = ((currG - prevG)/period);
			diffP = ((currP-prevP)/period);
			diffG = (float) ((currG - prevG)*1000.0/period);
			diffP = (float) ((currP-prevP)*1000.0/period);
			diffT = (float) ((currT-prevT)*1000.0/period);
			for(int j=0; j<putLen; j++){
			//diffPut = (float) ((currPut-prevPut)*1000.0/period);
				diffPut[j] = (float) ((currPut[j]-prevPut[j])*1000.0/period);
			}
			
			//Initialized for access from Online thread
			CuttleThread.G = diffG;
			CuttleThread.P = diffP;
			CuttleThread.T = diffT;
			for(int j=0; j<putLen; j++){
				CuttleThread.Put[j] = diffPut[j];
			}
			
			diffG = (float) ((currG - prevG));
			diffP = (float) ((currP-prevP));
			/*System.out.println("Put/s= " + diffP + " Get/s= " + diffG + " GTT= " + diffG/diffT + " PTT= " + diffP/diffT);
			for(int j =0; j<putLen; j++){
				System.out.print(" Put/s[" + j + "]= " + diffPut[j]);
			}
			System.out.println();
			for(int j =0; j<putLen; j++){
				System.out.print(" Put[" + j + "]= " + FT.putArr[j]);
			}
			System.out.println();*/
			
			//System.out.println("currG= " + currG+ " currP=  " + currP+ " totPak="+ MME.packetCount);
			prevG = currG;
			prevP = currP;
			prevT = currT;
			for(int j=0; j<putLen; j++){
				prevPut[j] = currPut[j];
			}

		}
	}
}