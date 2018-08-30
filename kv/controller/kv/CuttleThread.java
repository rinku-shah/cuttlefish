package net.floodlightcontroller.kv;

import java.io.BufferedReader;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.IOFSwitch;

public class CuttleThread {
	static float G;
	static float P;
	static float T;
	static float Put[] = new float[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public CuttleThread() {
		CutThread t;
		t = new CutThread();
		t.start();
	}

}

class CutThread implements Runnable {
	Long prevG, prevP, currG, currP, prevT, currT;
	float diffG, diffP, diffT;
	Long prevPut[] = new Long[] {(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0};
	Long currPut[] = new Long[] {(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0,(long)0};
	float diffPut[] = new float[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	int putLen = 20;
	
	int period = 10000; //stats for 30sec	//30000 replaced by 10000
	
	public CutThread() {

	}

	public void start() {
		// TODO Auto-generated method stub
		System.out.println("Cuttle Thread started");
	}

	@Override
	public void run() {
		
		/*/////////////////////////// BENCHMARKING CODE STARTS /////////////////////////////////////////////////////////////
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
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_1), "kv", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_2), "kv", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_3), "kv", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_4), "kv", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_5), "kv", Integer.toString(i), Integer.toString(i+1));
		}
		for(int i =0; i<1000; i++){
			FT.put(1, DatapathId.of(Constants.DEFAULT_SWITCH_ID_6), "kv", Integer.toString(i), Integer.toString(i+1));
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
		prevT = KV.packetCount;
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
			currT = KV.packetCount;
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
			for(int j=0; j<10; j++){
				CuttleThread.Put[j] = diffPut[j];
			}
			
			diffG = (float) ((currG - prevG));
			diffP = (float) ((currP-prevP));
			//////////////////////// PUT GET DEBUGGING CODE //////////////////////////////////////////
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
			//////////////////////// PUT GET DEBUGGING CODE ENDS //////////////////////////////////////////


			prevG = currG;
			prevP = currP;
			prevT = currT;
			for(int j=0; j<putLen; j++){
				prevPut[j] = currPut[j];
			}

		}
	}
}