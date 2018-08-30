#!/bin/bash

############# Namespace #############
#ip netns add h1
#ip link add h1-eth0 type veth peer name eth1
#####################################

sudo ovs-vsctl --if-exists del-br br0
sudo ovs-vsctl add-br br0

#sudo ovs-vsctl set bridge br0 stp_enable=true

# Add internal ports
sudo ovs-vsctl add-port br0 int1 -- set Interface int1 type=internal
sudo ovs-vsctl add-port br0 int2 -- set Interface int2 type=internal
#sudo ovs-vsctl add-port br0 int3 -- set Interface int3 type=internal

# Set bridge configuration parameters
ovs-vsctl set bridge br0 protocols=OpenFlow10
ovs-vsctl set bridge br0 other-config:datapath-id=0000000000000002
#ovs-vsctl set bridge br0 other-config:n-handler-threads=20
# Add physical interfaces as ports
sudo ovs-vsctl add-port br0 eth1
sudo ovs-vsctl add-port br0 eth2
#sudo ovs-vsctl add-port br0 eth3

############ Namespace #############
#ip link set h1-eth0 netns h1
#sudo ovs-vsctl add-port br0 eth1
#ip netns exec h1 ifconfig h1-eth0 10.129.41.2
####################################

# ########## Gre Tunnel ##########
#sudo ovs-vsctl add-port br0 gre0 -- set interface gre0 type=gre options:remote_ip=10.129.41.4
#sudo ovs-vsctl add-port br0 gre1 -- set interface gre1 type=gre options:remote_ip=10.129.41.1

# Assign eth1 parameters to internal port br0
sudo ifconfig eth1 0
sudo ifconfig int1 192.168.2.3 netmask 255.255.255.0

#sudo route add default gw 10.129.250.1 br0

# Assign eth2 parameters to internal port int1
sudo ifconfig eth2 0
sudo ifconfig int2 192.168.3.3 netmask 255.255.255.0

# Assign eth3 parameters to internal port int2
#sudo ifconfig eth3 0
#sudo ifconfig int3 192.168.10.6 netmask 255.255.255.0

#sudo iptables -F
#sudo iptables -t nat -A POSTROUTING -o br0 -j MASQUERADE

sudo ovs-vsctl set-controller br0 tcp:192.168.100.100:6653 tcp:192.168.100.12:6653 tcp:192.168.100.200:6653
#sudo ovs-vsctl set-controller br0 tcp:10.124.41.1:6653


#to set rule to accept OpenFlow messages from DGW for local controller at SGW
#sudo ovs-ofctl add-flow br0 nw_src=10.126.41.3,nw_dst=10.126.41.4,actions=normal

#sleep 3
#ovs-ofctl add-flow br0 arp,actions=normal


######route for gateway#####

#sudo route add -net 192.168.10.0 netmask 255.255.255.0 gw 192.168.3.4 br3
###############


if [ $# -ne 1 ]; then
	ifconfig eth1 mtu 2500
	ifconfig eth2 mtu 2500
#	ifconfig eth3 mtu 2500
else
	ifconfig eth1 mtu $1
	ifconfig eth2 mtu $1
#	ifconfig eth3 mtu $1
fi
