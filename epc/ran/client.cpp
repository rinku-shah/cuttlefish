/*************************************************************************

* * This file contains all the socket level functions that is used by UE.  *************************************************************************/

#include "client.h"
//#include "utils.h"
//Below are the files included for raw packets
#include<stdio.h> //for printf
#include<string.h> //memset
#include<sys/socket.h>    //for socket ofcourse
#include<stdlib.h> //for exit(0);
#include<errno.h> //For errno - the error number
#include<netinet/udp.h>   //Provides declarations for udp header
#include<netinet/ip.h>    //Provides declarations for ip header
#include <unistd.h> //Used to get PID of current process
#include<netinet/ip_icmp.h>   //Provides declarations for icmp header
#include<netinet/tcp.h>   //Provides declarations for tcp header
#include<netinet/if_ether.h>  //For ETH_P_ALL
#include<net/ethernet.h>  //For ether_header

#include<arpa/inet.h>
#include<sys/ioctl.h>
#include<sys/time.h>
#include<sys/types.h>

 Client::Client(int ID){
	
	tID=2000+ID; //use global tID to distinguish between threads

        //Create a raw socket of type IPPROTO
        client_socket = socket (AF_INET, SOCK_RAW, IPPROTO_RAW);
	if(MY_DEBUG){
        cout << "Raw Send Socket created"<<endl;
	}
        //client_socket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
 	 if(client_socket < 0){
    	cout << "ERROR opening UDP socket" << endl;
    exit(1);
  }

	sock_raw = socket( AF_PACKET , SOCK_RAW , htons(ETH_P_ALL)) ;
	if(MY_DEBUG){
	cout << "Raw Receive Socket created"<<endl;
	}
        
       if(sock_raw < 0)
       {
        //Print the error with proper message
        perror("Socket Error");
        //return 1;
       }
 }


struct pseudo_header
{
    u_int32_t source_address;
    u_int32_t dest_address;
    u_int8_t placeholder;
    u_int8_t protocol;
    u_int16_t udp_length;
};
 
/*
    Generic checksum calculation function
*/
unsigned short csum(unsigned short *ptr,int nbytes) 
{
    register long sum;
    unsigned short oddbyte;
    register short answer;
 
    sum=0;
    while(nbytes>1) {
        sum+=*ptr++;
        nbytes-=2;
    }
    if(nbytes==1) {
        oddbyte=0;
        *((u_char*)&oddbyte)=*(u_char*)ptr;
        sum+=oddbyte;
    }
 
    sum = (sum>>16)+(sum & 0xffff);
    sum = sum + (sum>>16);
    answer=(short)~sum;
     
    return(answer);
}

/*
 * This function configures the port number and IP address of the created socket.
 */
 void Client::input_server_details(int server_port, const char *server_address){
        int status;
        this->server_port = server_port;
        this->server_address = server_address;
        bzero((char*)&server_sock_addr, sizeof(server_sock_addr));
        server_sock_addr.sin_family = AF_INET;
        server_sock_addr.sin_port = htons(server_port);
        // Store this IP address in server_sock_addr; pton supports IPv6 while aton does not
        status = inet_pton(AF_INET, server_address, &(server_sock_addr.sin_addr));
        if(status == 0){
                cout<<"ERROR: Invalid IP address"<<endl;
                exit(EXIT_FAILURE);
        }
 }


/*
 * This function reads from the UDP socket.
 */
 void Client::read_data(){
        int status=0;
        unsigned char* my_buffer;
	    int c=0;

	while(status!=17 || !tflag ){	
	 bzero(client_buffer, BUFFER_SIZE);
        data_size = recvfrom(sock_raw, client_buffer , BUFFER_SIZE-1 , 0 , &saddr , (socklen_t*)&saddr_size);       
        status=ProcessPacket((unsigned char*)client_buffer , data_size); 
	}
	
 }


int Client::ProcessPacket(unsigned char* buffer, int size)
{   
    
    int tcp=0,udp=0,icmp=0,others=0,igmp=0,total=0,i,j; 
    ++total;
  
    //Get the IP Header part of this packet , excluding the ethernet header
    struct iphdr *iph = (struct iphdr*)(buffer + sizeof(struct ethhdr));

    switch (iph->protocol) //Check the Protocol and do accordingly...
    {           
        case 17: //UDP Protocol
	    //cout<<"Inside process packet:UDP"<<endl;
            //++udp;
            print_udp_packet(buffer , size);
	    return(17);
            break;
         
        default: //Some Other Protocol like ARP etc.
            //cout<<"Inside process packet:others";
            //++others;
	    return(0);
            break;
    }

}
 
 
void Client::print_ip_header(unsigned char* Buffer, int Size)
{
 
    unsigned short iphdrlen;
         
    struct iphdr *iph = (struct iphdr *)(Buffer  + sizeof(struct ethhdr) );
    iphdrlen =iph->ihl*4;
     
    memset(&source, 0, sizeof(source));
    source.sin_addr.s_addr = iph->saddr;
     
    memset(&dest, 0, sizeof(dest));
    dest.sin_addr.s_addr = iph->daddr;
    
}
 
void Client::print_udp_packet(unsigned char *Buffer , int Size)
{
     
    unsigned short iphdrlen;
     
    struct iphdr *iph = (struct iphdr *)(Buffer +  sizeof(struct ethhdr));
    iphdrlen = iph->ihl*4;
     
    struct udphdr *udph = (struct udphdr*)(Buffer + iphdrlen  + sizeof(struct ethhdr));
     
    int header_size =  sizeof(struct ethhdr) + iphdrlen + sizeof udph;
     
    //fprintf(logfile , "\n\n***********************UDP Packet*************************\n");
     
    print_ip_header(Buffer,Size);           

    if ((int)ntohs(udph->dest) == tID) {
		tflag=true;
    }
    else 
	tflag=false;

    strcpy(client_buffer, (const char*) Buffer + header_size);

}

/*
 * This function writes to the UDP socket.
 */
 void Client::write_data(string msg_id){
   
    //Datagram to represent the packet
    char datagram[4096] , source_ip[32] , *data , *pseudogram;
     
    //zero out the packet buffer
    memset (datagram, 0, 4096);
     
    //IP header
    struct iphdr *iph = (struct iphdr *) datagram;
     
    //UDP header
    struct udphdr *udph = (struct udphdr *) (datagram + sizeof (struct ip));
     
    struct sockaddr_in server_sock_addr;
    struct pseudo_header psh;
     
    data = datagram + sizeof(struct iphdr) + sizeof(struct udphdr);
    strcpy(data , client_buffer);
    bzero(client_buffer, BUFFER_SIZE);
    if(MY_DEBUG){	
    cout<<"SENDING - "<<data<<endl;
    }

    //some address resolution
    strcpy(source_ip , RAN_IP); //Source IP
   
    server_sock_addr.sin_family = AF_INET;
    server_sock_addr.sin_port = htons(g_mme_port);
    server_sock_addr.sin_addr.s_addr = inet_addr(DGW_IP); //Dest IP
    
    //Fill in the IP Header
    iph->ihl = 5;
    iph->version = 4;
    iph->tos = stoi(msg_id)*4; //16;
    iph->tot_len = sizeof (struct iphdr) + sizeof (struct udphdr) + strlen(data);
    iph->id = htonl(0); //htonl (12345); //Id of this packet
    iph->frag_off = 0;
    iph->ttl = 255;
    iph->protocol = IPPROTO_UDP;
    iph->check = 0;      //Set to 0 before calculating checksum
    iph->saddr = inet_addr(source_ip);    //Spoof the source ip address
    iph->daddr = server_sock_addr.sin_addr.s_addr;
     
    //Ip checksum
    iph->check = csum ((unsigned short *) datagram, iph->tot_len);
    
    udph->source = htons (tID);
    udph->dest = htons (g_mme_port);// htons(stoi(msg_id)+2000);
    udph->len = htons(8 + strlen(data)); //tcp header size
    udph->check = 0; //leave checksum 0 now, filled later by pseudo header
     
    //Now the UDP checksum using the pseudo header
    psh.source_address = inet_addr( source_ip );
    psh.dest_address = server_sock_addr.sin_addr.s_addr;
    psh.placeholder = 0;
    psh.protocol = IPPROTO_UDP;
    psh.udp_length = htons(sizeof(struct udphdr) + strlen(data) );
     
    int psize = sizeof(struct pseudo_header) + sizeof(struct udphdr) + strlen(data);
    pseudogram =(char*) malloc(psize);
     
    memcpy(pseudogram , (char*) &psh , sizeof (struct pseudo_header));
    memcpy(pseudogram + sizeof(struct pseudo_header) , udph , sizeof(struct udphdr) + strlen(data));
     
    udph->check = csum( (unsigned short*) pseudogram , psize);

	 int status;
        status = sendto(client_socket, datagram, iph->tot_len, 0 , (struct sockaddr*) &server_sock_addr, sizeof(server_sock_addr));
	
        report_error(status);

 }

/*
 * This function reads from the UDP socket in the form of unsigned char.
 */
 void Client::read_byte(){
        int status;
        bzero(client_byte_buffer, BUFFER_SIZE);
        status = recvfrom(client_socket, client_byte_buffer, BUFFER_SIZE-1, 0, NULL, NULL);
        report_error(status);
 }

/*
 * This function writes to the UDP socket in the form of unsigned char.
 */
 void Client::write_byte(){
        int status;
        status = sendto(client_socket, client_byte_buffer, strlen((char*)client_byte_buffer), 0,(struct sockaddr*)&server_sock_addr, sizeof(server_sock_addr));
        report_error(status);
 }

/*
 * This function generates TCP traffic at given rate using iperf3 for the specified duration of time.
 */
 int Client::sendUEData(int ue_num, string srcIp, string dstIp, int portnum, int startingPort, int endPort, string rate, size_t meanTime){
        const char *srcIpptr = srcIp.c_str();
        char c = tolower(rate[rate.size()-1]);
        string dstNetwork(SINK_SERVER_NETMASK);
        string format(1,c);
                                                                                                                                                                                                                         
 		string f = "iperf3 -c "+dstIp+" -p "+to_string(portnum)+" -b "+rate+" -M "+to_string(LINK_MTU)+" -f "+format+" -t "+to_string(meanTime)+" -B "+srcIp;

        if(DO_DEBUG){
                cout<<"SOURCE IP="<<srcIp<<endl;
                cout<<"DESTINATION IP="<<dstIp<<endl;
		cout<<"*** client.cpp DATA TIME _--- "<<meanTime<<endl;
                cout<<f<<endl;
        }

        bool done = false, loopedOnce = false;
        int count = 0, tmp_port, ret, realCounter = 0;
        int port_gap = endPort - startingPort;
        int numGlobaltriedPorts = 0;

        do{
        
		string g = runIperfCommand("iperf3 -c "+dstIp+" -p "+to_string(portnum)+" -b "+rate+" -M "+to_string(LINK_MTU)+" -f "+format+" -t "+to_string(meanTime)+" -B "+srcIp, srcIp);

        string cmd1 = "iperf3 -c "+dstIp+" -p "+to_string(portnum)+" -b "+rate+" -M "+to_string(LINK_MTU)+" -f "+format+" -t "+to_string(meanTime)+" -B "+srcIp;

		size_t f = g.find("Connecting to host");
                size_t found = g.find("iperf3: error - the server is busy running a test");
                size_t timeout = g.find("iperf3: error - unable to connect to server:");

                if(f == std::string::npos && found == std::string::npos && timeout != std::string::npos){
                        cout<<"FAILURE FOR "<<cmd1<<endl;
                        cout<<"iperf3 output: "<<g<<"\n for cmd "<<cmd1<<endl;
                        
			exit(1);
                }
                if(found != std::string::npos){
                        cout<<"iperf3 output: "<<g<<endl;
                        cout<< "Using local port " << portnum<<" "<<" count="<<count<<" real counter="<<realCounter<<endl;
                        portnum++;
                        if(portnum >= endPort){
                                if(count < port_gap){
                                        portnum = startingPort;
                                }else{
                                        loopedOnce = true;
                                        tmp_port = startingPort;
                                }
                        }
                        if(count >= port_gap && numGlobaltriedPorts <= NUM_GLOBAL_PORTS){
                                numGlobaltriedPorts++;
                                // Use global ports
                                ret = pthread_mutex_lock(&request_mutex);
                                if(ret < 0)
                                {
                                        perror("ERROR: mutex lock failed");
                                        exit(1);
                                }
                                portnum = global_ports[globalPortsIndex];
                                if(globalPortsIndex < NUM_GLOBAL_PORTS-1){
                                        globalPortsIndex++;
                                }else{
                                        globalPortsIndex = 0;
                                }

                                // Releasing lock
                                ret = pthread_mutex_unlock(&request_mutex);
                                if(ret < 0)
                                {
                                        perror("ERROR: mutex unlock failed");
                                        exit(1);
                                }
                                cout<<"Global port ="<<portnum<<" used approx index="<<(globalPortsIndex-1)<<endl;
                        }else if(count >= port_gap && numGlobaltriedPorts > NUM_GLOBAL_PORTS){
                                numGlobaltriedPorts = 0;
                                count = 0;
                                portnum = tmp_port;
                        }
 			count++;
                        realCounter++;
                }else{
                        if(realCounter != 0){
                                cout<< "Using global port " << portnum<<" "<<realCounter<<" DONE"<<endl;
                        }
                        done = true;
                }
        }while(!done);
	if(DO_DEBUG)
		cout<<"SENT DATA with SRC IP "<<srcIp<<endl;
        if(loopedOnce){
                portnum = tmp_port;
        }
	/*string get_pid = "ps -aux | grep -E 'iperf3.*"+srcIp+"'|grep -v 'grep'| tr -s \" \"| cut -d \" \" -f 2";
	cout<<"GET PID COMMAND --- "<<get_pid<<endl;
	string pid = GetStdoutFromCommand(get_pid);
	cout<<"\nPROCESS TO KILL - "<<pid<<endl; 	
	if(!pid.empty()){
		cout<<"\ninside PROCESS TO KILL - "<<pid<<endl; 		
		string kill_out = GetStdoutFromCommand("sudo kill -9 "+pid);
		cout<<"KILL OUTPUT ---- "<<kill_out<<endl;
	}*/
//	const char* kill_cmd = ("sudo kill $(ps -aux | grep -E 'iperf3.*"+srcIp+"'|grep -v 'grep'| tr -s \" \"| cut -d \" \" -f 2)").c_str();
//	system(kill_cmd);

        return portnum;
 }

/*
 * This function executes the specified command and returns its output.
 */
 string Client::runIperfCommand(string cmd, string srcIp) {
        string data;
        FILE * stream;
        const int max_buffer = 256;
        char buffer[max_buffer];
        cmd.append(" 2>&1");
        stream = popen(cmd.c_str(), "r");
        if (stream) {
                while (!feof(stream)){
                        if (fgets(buffer, max_buffer, stream) != NULL) data.append(buffer);
                }
		string get_pid = "ps -aux | grep -E 'iperf3.*"+srcIp+"'|grep -v 'grep'| tr -s \" \"| cut -d \" \" -f 2";
		string pid = GetStdoutFromCommand(get_pid);
		if(!pid.empty()){
			string kill_out = GetStdoutFromCommand("sudo kill -9 "+pid);
		}		


		pclose(stream);
        }
        return data;
 }

string Client::GetStdoutFromCommand(string cmd) {
        string data;
        FILE * stream;
        const int max_buffer = 256;
        char buffer[max_buffer];
        cmd.append(" 2>&1");
        stream = popen(cmd.c_str(), "r");
        if (stream) {
                while (!feof(stream)){
                        if (fgets(buffer, max_buffer, stream) != NULL) data.append(buffer);
                }
		
		pclose(stream);
        }
        return data;
 }

// Destructor: Close the UDP client socket
Client::~Client(){
        close(client_socket);
	close(sock_raw);
}
                                                                                                                                                                                                                         

