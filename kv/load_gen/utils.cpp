/****************************************************************************************
 * This file contains all the utility functions such as integrity and cipher functions. *
 ****************************************************************************************/

#include "utils.h"
#include <random>

mutex mtx;
//int base_thread_ID=1;
//int gw_port = 9876;
int gw_port = 5858;
const char *gw_address = DGW_IP; // Packets should reach the default switch, so setting its IP to that of default switch
default_random_engine generator;

std::random_device rd;
std::mt19937 gen(rd());
std::uniform_real_distribution<> att(0, 1);
std::uniform_real_distribution<> serv(0, 1);


int get_mix(vector<int> weights){
	int sum = 0;
	for(int i=0;i<weights.size();i++){
		sum+=weights[i];
	}

	int rnd = rand()%sum+1;
	if(DO_DEBUG)	
		cout<<"RANDOM "<<rnd<<endl;

	for(int i=0; i<weights.size(); i++) {
		if(rnd <= weights[i])
	    	return i;
	  	rnd -= weights[i];
	}
	//assert(!"should never get here");
}



int my_rand(){
 int num;

  /* initialize random seed: */
  srand (time(NULL));

  /* generate secret number between 1 and 1000: */
  num = rand() % 200 + 1;

  return(num);
}

void report_error(int arg){
	if(arg < 0){
		perror("ERROR");
		exit(EXIT_FAILURE);
	}
}

void print_message(string message){
	cout<<"***********************"<<endl;
	cout<<message<<endl;
	cout<<"***********************"<<endl;
}

void print_message(string message, int arg){
	cout<<"***********************"<<endl;
	cout<<message<<" "<<arg<<endl;
	cout<<"***********************"<<endl;
}

void print_message(string message, unsigned long long arg){
	cout<<"***********************"<<endl;
	cout<<message<<" "<<arg<<endl;
	cout<<"***********************"<<endl;
}

const char* to_char_array(unsigned long long arg){
	string tem;
	stringstream out;
	out<<arg;
	tem = out.str();
	const char *ans = tem.c_str();
	return ans;
}

string longToString(unsigned long long arg){
	stringstream out;
	out<<arg;
	return out.str();
}

void trim(string& s){
	s.erase(s.find_last_not_of(" \n\r\t")+1);
}

vector<string> split(char *str, const char *delim){
	vector<string> ans;
	string s(str);
	string delimiter(delim);
	size_t pos = 0;
	std::string token;
	while ((pos = s.find(delimiter)) != std::string::npos) {
		token = s.substr(0, pos);
		ans.push_back(token);
		s.erase(0, pos + delimiter.length());
	}
	ans.push_back(s);
	return ans;
}

