/********************************************************************
 * This file contains all the functionalities associated with a UE. *
 ********************************************************************/

#include "ue.h"
#include <time.h>

/* Message codes */
//Message from client to Switch/LB/Server
string GET = "1";
string GETG = "2"; 	
string PUT = "3";
string PUTG = "4";
//Message from Server to Controller LB
//string SERVERSTATS = "5"; 
//Reply from LB to Client(ACK)
string SERVERFOUND = "6";
//string FIN = "7";

char SEPARATOR[] = "@:##:@";


/*
 * Constructor: Create a UE object.
 */
UserEquipment::UserEquipment(int ue_num){
	
}

void UserEquipment::get(Network &user, int key){
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;
	time(&curTime);

	send = GET + SEPARATOR + to_string(key);

	bzero(user.client_buffer, BUFFER_SIZE);
	sprintf(user.client_buffer,"%s",send.c_str());
	user.write_data(GET); //We pass msg_id to write_data to set IPToS field
	time(&curTime);

	// Receive reply from LB
	user.read_data();
	
	time(&curTime);
	receive = (string) (user.client_buffer);
	if (receive.find(SEPARATOR) != std::string::npos) {
		tmpArray = split(user.client_buffer, SEPARATOR);
		if(tmpArray[0] == SERVERFOUND){
			if(DO_DEBUG){
				cout <<"VALUE : "<<tmpArray[1]<<endl;
			}
                }
		//cout <<"VALUE : "<<tmpArray[1]<<endl;
		//<<"VALUE: "<<tmpArray[2]<<endl;
		//cout<<"Received"<<endl;
        }
}

void UserEquipment::getG(Network &user, int key){
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;

////////////////// Global GET /////////////////
	time(&curTime);

	send = GETG + SEPARATOR + to_string(key);

	bzero(user.client_buffer, BUFFER_SIZE);
	sprintf(user.client_buffer,"%s",send.c_str());
	user.write_data(GETG); //We pass msg_id to write_data to set IPToS field
	time(&curTime);

	// Receive reply from KV
	user.read_data();
	
	time(&curTime);
	receive = (string) (user.client_buffer);
	if (receive.find(SEPARATOR) != std::string::npos) {
		tmpArray = split(user.client_buffer, SEPARATOR);
		if(tmpArray[0] == SERVERFOUND){
			if(DO_DEBUG){
				cout <<"VALUE : "<<tmpArray[1]<<endl;
			}
                }
		//cout <<"VALUE : "<<tmpArray[1]<<endl;
		//<<"VALUE: "<<tmpArray[2]<<endl;
		//cout<<"Received"<<endl;
        }
///////////////// Global GET ends ////////////////
}

void UserEquipment::put(Network &user, int key, int val){
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;
	time(&curTime);

	send = PUT + SEPARATOR + to_string(key) + SEPARATOR + to_string(val);

	bzero(user.client_buffer, BUFFER_SIZE);
	sprintf(user.client_buffer,"%s",send.c_str());
	user.write_data(PUT);
	time(&curTime);

	// Receive reply from KV
	user.read_data();
	
	time(&curTime);
	receive = (string) (user.client_buffer);
	if (receive.find(SEPARATOR) != std::string::npos) {
		tmpArray = split(user.client_buffer, SEPARATOR);
		if(tmpArray[0] == SERVERFOUND){
			if(DO_DEBUG){
				cout <<"PUT COMPLETE : "<<tmpArray[0]<<endl;
			}
                }
        }
}

void UserEquipment::putG(Network &user, int key, int val){
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;

////////// Global PUT ////////////////////////////

	time(&curTime);

	send = PUTG + SEPARATOR + to_string(key) + SEPARATOR + to_string(val);

	bzero(user.client_buffer, BUFFER_SIZE);
	sprintf(user.client_buffer,"%s",send.c_str());
	user.write_data(PUTG);
	time(&curTime);

	// Receive reply from LB
	user.read_data();
	
	time(&curTime);
	receive = (string) (user.client_buffer);
	if (receive.find(SEPARATOR) != std::string::npos) {
		tmpArray = split(user.client_buffer, SEPARATOR);
		if(tmpArray[0] == SERVERFOUND){
			if(DO_DEBUG){
				cout <<"GLOBAL PUT COMPLETE : "<<tmpArray[0]<<endl;
			}
                }
        }
////////// Global PUT ENDS ////////////////////////////
}

UserEquipment::~UserEquipment(){
	// Dummy destructor
}
