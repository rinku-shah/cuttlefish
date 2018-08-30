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
sudo ovs-vsctl add-port br0 int3 -- set Interface int3 type=internal
sudo ovs-vsctl add-port br0 int4 -- set Interface int4 type=internal
sudo ovs-vsctl add-port br0 int5 -- set Interface int5 type=internal
sudo ovs-vsctl add-port br0 int6 -- set Interface int6 type=internal
#sudo ovs-vsctl add-port br0 int7 -- set Interface int7 type=internal


# Set bridge configuration parameters
ovs-vsctl set bridge br0 protocols=OpenFlow10
ovs-vsctl set bridge br0 other-config:datapath-id=0000000000000004

# Add physical interfaces as ports
sudo ovs-vsctl add-port br0 eth1
sudo ovs-vsctl add-port br0 eth2
sudo ovs-vsctl add-port br0 eth4
sudo ovs-vsctl add-port br0 eth5
sudo ovs-vsctl add-port br0 eth6
sudo ovs-vsctl add-port br0 eth7
#sudo ovs-vsctl add-port br0 eth8


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
sudo ifconfig int1 192.168.3.4 netmask 255.255.255.0

sudo ifconfig eth2 0
sudo ifconfig int2 192.168.4.4 netmask 255.255.255.0

sudo ifconfig eth4 0
sudo ifconfig int3 192.168.9.4 netmask 255.255.255.0

sudo ifconfig eth5 0
sudo ifconfig int4 192.168.10.4 netmask 255.255.255.0

sudo ifconfig eth6 0
sudo ifconfig int5 192.168.13.4 netmask 255.255.255.0

sudo ifconfig eth7 0
sudo ifconfig int6 192.168.100.4 netmask 255.255.255.0

#sudo ifconfig eth8 0
#sudo ifconfig int7 192.168.100.6 netmask 255.255.255.0

#sudo iptables -F
#sudo iptables -t nat -A POSTROUTING -o br0 -j MASQUERADE

#sudo ovs-vsctl set-controller br0 tcp:10.123.41.1:6653 tcp:127.0.0.1:6653
sudo ovs-vsctl set-controller br0 tcp:192.168.100.100:6653 tcp:192.168.100.12:6653 tcp:192.168.100.14:6653 tcp:192.168.100.16:6653 tcp:192.168.100.19:6653 tcp:192.168.100.21:6653 tcp:192.168.100.23:6653 tcp:192.168.100.200:6653

 # RULE FOR SGW_HIER

#sudo ovs-vsctl set-controller br0 tcp:10.123.41.1:6653


#sleep 3
#ovs-ofctl add-flow br0 arp,actions=normal

######route for gateway#####

#sudo route add -net 192.168.3.3 netmask 255.255.255.255 gw 192.168.3.3 dev eth1
###############


if [ $# -ne 1 ]; then
	ifconfig eth1 mtu 2500
	ifconfig eth2 mtu 2500
	ifconfig eth4 mtu 2500
	ifconfig eth5 mtu 2500
	ifconfig eth6 mtu 2500
        ifconfig eth7 mtu 2500
	#ifconfig eth8 mtu 2500

else
	ifconfig eth1 mtu $1
	ifconfig eth2 mtu $1
	ifconfig eth4 mtu $1
	ifconfig eth5 mtu $1
	ifconfig eth6 mtu $1
        ifconfig eth7 mtu $1
	#ifconfig eth8 mtu $1
fi

