#include "client.h"
#define NO_EPOCH 6 
#define INST_ARR_LEN 300
#define INST_PERIOD 10 // period in secs

struct _threadArgs {
	int threadId;
	int num_threads;
};

struct _srcPortArgs {
	int startingPort;
	int endingPort;
	int numPorts;
};

int flowWeight;	// Number of messages send for encryption/decryption per connection
vector<unsigned long long> num_req_per_thread_P, num_req_per_thread_G, num_req_per_thread;
vector<unsigned long long> response_time_P, response_time_G, response_time;


struct timeval start;   // Simulation start time
time_t endTime;		// Simulation end time
long mtime, useconds, seconds; //For resp time calc

double num_ue_inst[INST_ARR_LEN] = {0};  // Enough to store 50 min data if captured after every 10 sec
double resp_time_inst[INST_ARR_LEN] = {0};  // Enough to store 50 min data if captured after every 10 sec
int instIndex = 0; // Index to remember number of entries in instr arrays
time_t inst_endTime; //End time of instrumenting current period (10sec)
unsigned long long prev_tpt = 0; //To remember tpt till previous period
unsigned long long prev_lat = 0; //To remember lat till previous period

/// DYNAMIC LOAD GENERATOR VARIABLES START HERE  ///
std::mutex traffic_mtx; 
std::mutex inst_mtx;
std::mutex lat_mtx;
int traffic_shape_size = NO_EPOCH;
//0---5:95
//1---10_80
//2---18_82
//3---25_75
//4---50_50
//5---66_34
//6---75_25
//7---100_1
//traffic_shape[][] = {{time,mix_num}}
int traffic_shape[10][2] = {{5,1},{5,0},{5,7},{6,2},{5,0},{5,5},{5,4},{6,1}};
//int traffic_shape[5][2] = {{5,0},{5,6},{5,0},{6,6}};
//int traffic_shape[5][2] = {{2,0},{2,1},{2,2},{2,3}};
int curr_mix_index=0;
bool dynLoad = true;
bool instrumentTptLat = true; //Instrument num_ue and response_time every 10 sec
int mix_num=0;	//choose the traffix mix from above traffic_options -> {0,1,2}
time_t mix_endTime; //End time of current traffic mix
float tpt[NO_EPOCH] = {0};
float lat[NO_EPOCH] = {0};
unsigned long long num_req_per_epoch[NO_EPOCH] = {0};
unsigned long long response_time_per_epoch[NO_EPOCH] = {0};

/// DYNAMIC LOAD GENERATOR VARIABLES END HERE ///


bool putFlag = true;
//int put_percent = 100; //out of 1000 eg. 100 = 10%
//nPut & nGet are used to generate varied load as put:get
//0---1:20---5% put
//1---1:10---10% put
//2---1:5---18%
//3---1:3---25%
//4---1:1---50%
//5---2:1---66%
//6---3:1---75%
//7---1:0---100%

int nPut = 1;
int nGet = 20;

// Thread function for each simulated UE
void* multithreading_func(void *arg){
	struct _threadArgs *args = (struct _threadArgs *)arg;
	int threadId = args->threadId;
	int maxThreads = args->num_threads;
	Network user(threadId+1);
	pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
	pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);
	time_t curTime;
	time(&curTime);
	struct timeval start1, end1; //Used to find time between request send n response received // response time metrics
	int key = 0; // key is used for the key
	
	while(curTime < endTime){
		do {
			UserEquipment ue(threadId+1);
			gettimeofday(&start, NULL);


                        int step = threadId * flowWeight;
                        if((putFlag) && (threadId==0)){
				for (int k=0; k<flowWeight; k++){
					gettimeofday(&start1, NULL);
					putG(user,ue,k+step,k+1);
					//usleep(1);
					num_req_per_thread_P[threadId]++;
					num_req_per_thread[threadId]++;
					//////PRINT CONN RESP TIME TO ARRAY////
					gettimeofday(&end1, NULL);
					seconds  = end1.tv_sec  - start1.tv_sec;
					useconds = end1.tv_usec - start1.tv_usec;
					mtime = ((seconds) * 1000000 + useconds);
					response_time_P[threadId] += mtime;
					response_time[threadId] += mtime;
				}
		                //sleep(1);
				putFlag=false;
			}
		
			//int step = threadId * flowWeight;
			//cout<<"Step= " <<step<<endl;
			
			//for (int i=0; i<flowWeight; i++){
			for (int k=0; k < nPut; k++){
				//PUT CODE
				gettimeofday(&start1, NULL);
				putG(user,ue,key+step,key+1);
				//usleep(1);
				num_req_per_thread_P[threadId]++;
				num_req_per_thread[threadId]++;
				//////PRINT CONN RESP TIME TO ARRAY////
				gettimeofday(&end1, NULL);
				seconds  = end1.tv_sec  - start1.tv_sec;
				useconds = end1.tv_usec - start1.tv_usec;
				mtime = ((seconds) * 1000000 + useconds);
				response_time_P[threadId] += mtime;
				response_time[threadId] += mtime;
			}
			for (int k=0; k < nGet; k++){
				//GET CODE				
				gettimeofday(&start1, NULL);
				get(user,ue,key+step);
				//usleep(1);
				num_req_per_thread_G[threadId]++;
				num_req_per_thread[threadId]++;
				//////PRINT CONN RESP TIME TO ARRAY////
				gettimeofday(&end1, NULL);
				seconds  = end1.tv_sec  - start1.tv_sec;
				useconds = end1.tv_usec - start1.tv_usec;
				mtime = ((seconds) * 1000000 + useconds);
				response_time_G[threadId] += mtime;
				response_time[threadId] += mtime;
		        }

			if (key < flowWeight){ //i is used for key
					key++;
			} else
			{ key = 0;
			}				

			//usleep(1000);
			//Terminate the connection
			gettimeofday(&start1, NULL);
		
			//////PRINT CONN RESP TIME TO ARRAY////
			gettimeofday(&end1, NULL);
			seconds  = end1.tv_sec  - start1.tv_sec;
			useconds = end1.tv_usec - start1.tv_usec;
			mtime = ((seconds) * 1000000 + useconds);


			time(&curTime);

			//////  DYNAMIC LOAD GENERATION CODE STARTS HERE /////////////////////////////////////////////////////////

			if(dynLoad){
				traffic_mtx.lock();
				if(curTime >= mix_endTime) {

					lat_mtx.lock();

					for(int i=0; i<maxThreads; i++){
						num_req_per_epoch[curr_mix_index] = num_req_per_epoch[curr_mix_index] + num_req_per_thread[i];
						response_time_per_epoch[curr_mix_index] = response_time_per_epoch[curr_mix_index] + response_time[i];
					}
					lat_mtx.unlock();
	
					if ( curr_mix_index > 0){
						int i = curr_mix_index;
						while (i > 0) {
							num_req_per_epoch[curr_mix_index] = num_req_per_epoch[curr_mix_index] - num_req_per_epoch[i-1];
							response_time_per_epoch[curr_mix_index] =  response_time_per_epoch[curr_mix_index] - response_time_per_epoch[i-1];
							i--;
						}
					}
					float tmp_tpt = (num_req_per_epoch[curr_mix_index]*1.0)/(traffic_shape[curr_mix_index][0] * 60);
					float tmp_lat = (response_time_per_epoch[curr_mix_index]*0.001)/num_req_per_epoch[curr_mix_index];

					tpt[curr_mix_index] = tmp_tpt;
					lat[curr_mix_index] = tmp_lat;
					
					if(curr_mix_index < traffic_shape_size){
						curr_mix_index++;
					}
					mix_num = traffic_shape[curr_mix_index][1];
					setMix(mix_num);
					int tmp1 = traffic_shape[curr_mix_index][0] * 60;
					mix_endTime = curTime + (int) tmp1;

					cout<<"mix="<<mix_num<<endl;

				}
				traffic_mtx.unlock();
			}
			//////  DYNAMIC LOAD GENERATION CODE ENDS HERE /////////////////////////////////////////////////////////

	
			//////  INSTRUMENTATION CODE STARTS HERE /////////////////////////////////////////////////////////
			
			if(instrumentTptLat){
				inst_mtx.lock();
				time(&curTime);
				if(curTime >= inst_endTime) {
					//cout<<"start time="<<curTime<<endl;
					lat_mtx.lock();
					//num_ue_inst[instIndex] = attNo + detNo + sreqNo;
					for(int i=0; i<maxThreads; i++){
						num_ue_inst[instIndex] =  num_ue_inst[instIndex] + num_req_per_thread[i];
						resp_time_inst[instIndex] = resp_time_inst[instIndex] + response_time[i];
					}
					lat_mtx.unlock();
					float prev_t = num_ue_inst[instIndex];
					float prev_l = resp_time_inst[instIndex];
					num_ue_inst[instIndex] = num_ue_inst[instIndex] - prev_tpt; //Get current period val
					resp_time_inst[instIndex] = resp_time_inst[instIndex] - prev_lat;
					resp_time_inst[instIndex] = (resp_time_inst[instIndex]*0.001)/num_ue_inst[instIndex]; //Keep it bfor num_ue_inst update coz we need total number and not throughtput
					num_ue_inst[instIndex] = (num_ue_inst[instIndex]*1.0)/INST_PERIOD;
					prev_tpt = prev_t;
					prev_lat = prev_l;
					instIndex++;
					inst_endTime = curTime + (int) INST_PERIOD;
				}
				inst_mtx.unlock();
			}


//////  INSTRUMENTATION CODE ENDS HERE /////////////////////////////////////////////////////////

		}while(curTime < endTime); //end do-while
	} //end while

	free(args);
	pthread_exit(NULL);
}

int main(int argc, char *args[]){
	long maxThreads = 0;
	int status;
	stringstream ss;
	string data = "";
	std::ofstream outfile;
	std::ofstream delayfile;
	std::ofstream instfile;
	
	if(argc != 4){
		fprintf(stderr,"Usage: %s <max-threads> <program-run-time(in mins)> <num-msg per connection(thin/fat flow)> \n", args[0]);
		exit(0);
	}

	maxThreads = atoi(args[1]);
	if(maxThreads <= 0){
		printf("Number of threads should be greater than 0\n");
		exit(0);
	}
	double tmp;
	ss << args[2];
	ss >> tmp;
	if(tmp <= 0.0){
		printf("Run time of each threads should be greater than 0.0\n");
		exit(0);
	}

	flowWeight = atoi(args[3]);
	//cout<<flowWeight;
	if(flowWeight < 1){
		printf("Connection size should be atleast 1 packet\n");
		exit(0);
	}

	num_req_per_thread_P.resize(maxThreads, 0);
	num_req_per_thread_G.resize(maxThreads, 0);
	num_req_per_thread.resize(maxThreads,0);
	response_time_P.resize(maxThreads, 0);
	response_time_G.resize(maxThreads, 0);
	response_time.resize(maxThreads, 0);
      	
	cout<<"***************STARTING NOW***************"<<endl;
	tmp = tmp * 60;
	time_t curTime;
	time(&curTime);
	if(DO_DEBUG){
		cout<<"start time="<<curTime<<endl;
	}

	endTime = curTime + (int) tmp;
	if(DO_DEBUG){
		cout<<"end time="<<endTime<<endl;
	}
	int simulationTime = (int) tmp;

	pthread_t tid[maxThreads];

	///////// DYNAMIC LOAD GEN in MAIN STARTS ///////////////
	if (dynLoad){	
		cout<<"start time="<<curTime<<endl;
		int tmp1 = traffic_shape[curr_mix_index][0] * 60;
		mix_endTime = curTime + (int) tmp1;
		mix_num = traffic_shape[curr_mix_index][1];
		setMix(mix_num);
		cout<<"mix="<<mix_num<<endl;
	}
	///////// DYNAMIC LOAD GEN in MAIN ENDS ///////////////

	///// INSTRUMENTATION CODE STARTS /////////

	if (instrumentTptLat){	

		int tmp2 = INST_PERIOD;
		inst_endTime = curTime + (int) tmp2;

	}
	///// INSTRUMENTATION CODE ENDS /////////


	// Create UE threads
	for(int i = 0;i<maxThreads;i++){
		struct _threadArgs * args = (struct _threadArgs *)malloc(sizeof(struct _threadArgs));
		args->threadId = i;
		args->num_threads = maxThreads;

		status = pthread_create(&tid[i], NULL, multithreading_func, args);
		report_error(status);
	}

	// Sleep for the specified simulation time
	usleep(simulationTime * 1000000); // 1sec

	/* Wake up and cancel/join all the UE threads to end simulation */
	for(int i=0;i<maxThreads;i++){
		if(DO_DEBUG){
			cout<<"******* ENDING THREAD - "<<i<<endl;
		}
		pthread_cancel(tid[i]);
		pthread_join(tid[i],NULL);
	}
	if(DO_DEBUG){
		cout<<"************ENDED!!!************"<<endl;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////	
	/* Calculate and display various metrics */
	string s = "";
	int total_G = 0;
	int total_P = 0;
	unsigned long long total_response_time_P = 0;
	unsigned long long total_response_time_G = 0;
	unsigned long long total_conn_response_time = 0;
	double average_response_time_P = 0.0;
	double average_response_time_G = 0.0;
	double average_response_time = 0.0;
	double throughput = 0.0, throughput_P = 0.0, throughput_G = 0.0;
	double percentP = 0.0;

	time_t actual_endTime;
	time(&actual_endTime);
	for(int i=0;i<maxThreads;i++){
		total_P += num_req_per_thread_P[i];
		total_G += num_req_per_thread_G[i];
		total_response_time_P += response_time_P[i];
		total_response_time_G += response_time_G[i];
		cout<<"num_req_per_thread_P["<<i<<"] "<<num_req_per_thread_P[i]<<endl;
		cout<<"num_req_per_thread_G["<<i<<"] "<<num_req_per_thread_G[i]<<endl;
		cout<<"put response_time["<<i<<"] "<<((response_time_P[i]*1.0)/num_req_per_thread_P[i])<<" us"<<endl;
		cout<<"get response_time["<<i<<"] "<<((response_time_G[i]*1.0)/num_req_per_thread_G[i])<<" us"<<endl;
	}
	average_response_time_P = (total_response_time_P*1.0)/(total_P*1.0);
        average_response_time_G = (total_response_time_G*1.0)/(total_G*1.0);
	average_response_time = ((total_response_time_P + total_response_time_G)*1.0)/((total_P + total_G)*1.0);
	throughput_P = ((total_P)*1.0)/(actual_endTime - curTime);
	throughput_G = ((total_G)*1.0)/(actual_endTime - curTime);
	throughput = ((total_P + total_G)*1.0)/(actual_endTime - curTime);
	percentP = (throughput_P/throughput)*100;
	cout<<"***************************************STATISTICS***************************************"<<endl;
	double averageReqPerThread = (((total_P + total_G)*1.0)/maxThreads);
	averageReqPerThread = roundf(averageReqPerThread * 100) / 100; 


	ostringstream strsR;
	strsR << averageReqPerThread;
	string avReq = strsR.str();
	ostringstream strsC;

	printf("Total Number of Threads=%ld\n", maxThreads);
	printf("Total Number of Puts=%d\n", total_P);
	printf("Total Number of Gets=%d\n", total_G);
	cout<<"Average Number of Requests per Thread="<<averageReqPerThread<<endl;
	printf("Total Execution Time=%ld sec\n", (actual_endTime - curTime));
	average_response_time = average_response_time/1000000.0;
	cout<<"Average Request Latency = "<<average_response_time<<" secs"<<endl;
	cout<<"Put Throughput="<<throughput_P<<" requests/sec"<<endl;
	cout<<"Get Throughput="<<throughput_G<<" requests/sec"<<endl;
	cout<<"Total Throughput="<<throughput<<" requests/sec"<<endl;
	cout<<"Put Percent="<<percentP<<endl;
	
	if (instrumentTptLat) {
		instfile.open(INST_FILE, std::ios_base::app);
		data = "";
		if (instfile.is_open()){
			//cout<<"inst_index="<<instIndex<<endl;
			for(int i = 0; i < instIndex; i++){
				float t = num_ue_inst[i];
				float l = resp_time_inst[i];
				data.append(to_string(t)).append(COMMA).append(to_string(l)).append("\n");

			}
			instfile << data;
			instfile.close();
		}
	}

	/* Write the metrics to the statistics file */
	if(!fileExists(STATISTIC_FILE)){
		data.append("#MaxThreads").append(COMMA);
		data.append("FlowWeight").append(COMMA).append("ExecutionTime").append(COMMA);
		data.append("#Gets").append(COMMA).append("#Puts").append(COMMA);
		data.append("GetResponseTime").append(COMMA);
		data.append("PutResponseTime").append(COMMA);
		data.append("GetTpt").append(COMMA);
		data.append("PutTpt").append(COMMA);
		data.append("AvgRequestLatency").append(COMMA).append("AvgThroughput");
		data.append(COMMA).append("Put%");
		data.append("\n");
	}


	outfile.open(STATISTIC_FILE, std::ios_base::app);
	if (outfile.is_open()){
		data.append(to_string(maxThreads)).append(COMMA).append(to_string(flowWeight)).append(COMMA).append(to_string(tmp).append(COMMA));
		data.append(to_string(total_G)).append(COMMA);
		data.append(to_string(total_P)).append(COMMA);
		data.append(to_string(average_response_time_G)).append(COMMA);
		data.append(to_string(average_response_time_P)).append(COMMA);
		//data.append(to_string(total_G + total_P)).append(COMMA);
		data.append(to_string(throughput_G)).append(COMMA);
		data.append(to_string(throughput_P)).append(COMMA);
		data.append(to_string(average_response_time)).append(COMMA);
		data.append(to_string(throughput)).append(COMMA);
		data.append(to_string(percentP));	
	}
	data.append("\n");
	outfile << data;
	outfile.close();
	exit(0);
	return 0;
}

inline bool fileExists (const std::string& name) {
	struct stat buffer;   
	return (stat (name.c_str(), &buffer) == 0); 
}

void get(Network &user,UserEquipment &ue, int key){
	return ue.get(user,key);
}

void put(Network &user,UserEquipment &ue, int key, int val){
	return ue.put(user,key,val);
}

void getG(Network &user,UserEquipment &ue, int key){
	return ue.getG(user,key);
}

void putG(Network &user,UserEquipment &ue, int key, int val){
	return ue.putG(user,key,val);
}

void setMix(int traffic_type){
//nPut & nGet are used to generate varied load as put:get
//0---1:20---5% put
//1---1:10---10% put
//2---1:5---18%
//3---1:3---25%
//4---1:1---50%
//5---2:1---66%
//6---3:1---75%
//7---1:0---100%

	switch(traffic_type){
		case 0: 
			nPut=1;
			nGet=20;
			break;
		case 1: 
			nPut=1;
			nGet=10;
			break;
		case 2: 
			nPut=1;
			nGet=5;
			break;
		case 3: 
			nPut=1;
			nGet=3;
			break;
		case 4: 
			nPut=1;
			nGet=1;
			break;
		case 5: 
			nPut=2;
			nGet=1;
			break;
		case 6: 
			nPut=3;
			nGet=1;
			break;
		case 7: 
			nPut=1;
			nGet=0;
			break;
		default:
			cout<<"Incorrect traffic type"<<endl;
			break;
	}
}




