/*************************************************************************

* * This file contains all the socket level functions that is used by UE.  *************************************************************************/

#include "network.h"

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
//#include<netinet/udp.h>   //Provides declarations for udp header
#include<netinet/tcp.h>   //Provides declarations for tcp header
//#include<netinet/ip.h>    //Provides declarations for ip header
#include<netinet/if_ether.h>  //For ETH_P_ALL
#include<net/ethernet.h>  //For ether_header
//#include<sys/socket.h>
#include<arpa/inet.h>
#include<sys/ioctl.h>
#include<sys/time.h>
#include<sys/types.h>
//#include<unistd.h>

 Network::Network(int ID){
	
	tID=2000+ID; //use global tID to distinguish between threads
        //cout<<"TID= "<<tID<<endl;
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
        //int sock_raw = socket( AF_PACKET , SOCK_RAW , htons(ETH_P_ALL)) ;
        //setsockopt(sock_raw , SOL_SOCKET , SO_BINDTODEVICE , "eth1" , strlen("eth1")+ 1 );
     
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
 void Network::input_server_details(int server_port, const char *server_address){
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
 void Network::read_data(){
        int status=0;
        unsigned char* my_buffer;
	
        //Receive a packet
        int c=0;

	while(status!=17 || !tflag ){	
	 	bzero(client_buffer, BUFFER_SIZE);
        data_size = recvfrom(sock_raw, client_buffer , BUFFER_SIZE-1 , 0 , &saddr , (socklen_t*)&saddr_size);        
        status=ProcessPacket((unsigned char*)client_buffer , data_size); 
	}
	
 }


int Network::ProcessPacket(unsigned char* buffer, int size)
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
 
 
void Network::print_ip_header(unsigned char* Buffer, int Size)
{
    //print_ethernet_header(Buffer , Size);
   
    unsigned short iphdrlen;
         
    struct iphdr *iph = (struct iphdr *)(Buffer  + sizeof(struct ethhdr) );
    iphdrlen =iph->ihl*4;
     
    memset(&source, 0, sizeof(source));
    source.sin_addr.s_addr = iph->saddr;
     
    memset(&dest, 0, sizeof(dest));
    dest.sin_addr.s_addr = iph->daddr;
}
 

 
void Network::print_udp_packet(unsigned char *Buffer , int Size)
{
     
    unsigned short iphdrlen;
     
    struct iphdr *iph = (struct iphdr *)(Buffer +  sizeof(struct ethhdr));
    iphdrlen = iph->ihl*4;
     
    struct udphdr *udph = (struct udphdr*)(Buffer + iphdrlen  + sizeof(struct ethhdr));
     
    int header_size =  sizeof(struct ethhdr) + iphdrlen + sizeof udph;
          
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
   //We pass msg_id to write_data to set IPToS field
 */
 void Network::write_data(string msg_id){
   
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
     
    //Data part
    data = datagram + sizeof(struct iphdr) + sizeof(struct udphdr);
   
    strcpy(data , client_buffer);
    bzero(client_buffer, BUFFER_SIZE);
    if(MY_DEBUG){	
    cout<<"SENDING - "<<data<<endl;
    }

    //some address resolution
    strcpy(source_ip , CLIENT_IP); //Source IP
   
    server_sock_addr.sin_family = AF_INET;
    server_sock_addr.sin_port = htons(gw_port);
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

    //UDP header
    //udph->source = htons (RAN_UDP_PORT);
    udph->source = htons (tID);
    udph->dest = htons (gw_port);// htons(stoi(msg_id)+2000);
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
 void Network::read_byte(){
        int status;
        bzero(client_byte_buffer, BUFFER_SIZE);
        status = recvfrom(client_socket, client_byte_buffer, BUFFER_SIZE-1, 0, NULL, NULL);
        report_error(status);
 }

/*
 * This function writes to the UDP socket in the form of unsigned char.
 */
 void Network::write_byte(){
        int status;
        status = sendto(client_socket, client_byte_buffer, strlen((char*)client_byte_buffer), 0,(struct sockaddr*)&server_sock_addr, sizeof(server_sock_addr));
        report_error(status);
 }


// Destructor: Close the UDP client socket
Network::~Network(){
        close(client_socket);
	close(sock_raw);
}
                                                                                                                                                                                                                         

