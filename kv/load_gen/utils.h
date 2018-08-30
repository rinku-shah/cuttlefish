//To delete entries from delay file:  sed -i '/^pattern/d' delay.csv
//(C++) Operations: Input/Output
#include <iostream>
#include <math.h>

//(C++) STL Operations: String, Vector, String stream
#include <string>
#include <vector>
#include <sstream>
#include <unordered_map>
#include <queue>
#include <mutex>

// For integrity protection (NAS Signalling)
#include <openssl/hmac.h>

// For encryption/decryption (AES)
#include <openssl/aes.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>

//(C) Operations: Input/Output, String, Standard libraries(like atoi, malloc)
#include <stdio.h>
#include <random>
#include <string.h>
#include <stdlib.h>
#include <thread> 

//(C) Operations: Socket programming
#include <sys/timeb.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <signal.h>
#include <netdb.h> 
#include <arpa/inet.h>
#include <fcntl.h>
#include <sys/types.h>

//(C) Operations: Multithreading
#include <pthread.h>

// Raw socket
#include <linux/if_packet.h>
#include <sys/ioctl.h>
#include <bits/ioctls.h>
#include <net/if.h>
#include <netinet/ip.h>
#include <netinet/udp.h>
#include <netinet/ether.h>

//Tun device
#include <linux/if_tun.h>
#include <sys/stat.h>
#include <sys/select.h>
#include <sys/time.h>
#include <errno.h>
#include <stdarg.h>

//Writing file
#include <fstream>
//#include <atomic>
using namespace std;

#define DO_DEBUG 0
#define MY_DEBUG 0
#define WANT_DELAY_CDF 0
#define UE_PER_THREAD 1000000
/**************************************** Configurable parameters **********************************************/

#define DEFAULT_IF  "eth1"   // Default network interface name                                                *
#define DGW_IP "192.168.1.2"    // IP address of DGW machine                                                  
#define CLIENT_IP "192.168.1.1"   // IP address of RAN machine

#define SINK_SERVER_NETMASK "/16"   // Sink subnet netmask                                                     
              

/***************************************************************************************************************/

#define BUFFER_SIZE 300   // Maximum packet size
#define MAX_PACKET_SIZE 2048
#define LINK_MTU 1500   // MTU value for iperf3

#define RAN_UDP_PORT 5858
#define SINK_UDP_PORT 7891

#define COMMA ","
#define STATISTIC_FILE "stats.csv"
#define DELAY_FILE "delay.csv"
#define INST_FILE "inst.csv" //STORES PER PERIOD TPT & LAT

extern int gw_port;
extern const char *gw_address;

/* Utility functions */
int my_rand();
bool att_prob(float);
bool serv_prob(float);

void report_error(int);
void print_message(string);
void print_message(string,int);
void print_message(string,unsigned long long);
const char* to_char_array(unsigned long long);
string longToString(unsigned long long);
void trim(string& );
vector<string> split(char *, const char *);
int get_mix(vector<int>);
