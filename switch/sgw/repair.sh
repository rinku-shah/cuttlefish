#!/bin/bash

sudo ovs-vsctl del-controller br0
sudo ovs-vsctl --if-exists del-port br0 eth1
sudo ovs-vsctl --if-exists del-port br0 eth2
#sudo ovs-vsctl --if-exists del-port br0 eth3
sudo ovs-vsctl --if-exists del-port br0 int1
sudo ovs-vsctl --if-exists del-port br0 int2
#sudo ovs-vsctl --if-exists del-port br0 int3
sudo ovs-vsctl --if-exists del-br br0

sudo ifconfig eth1 192.168.2.3 netmask 255.255.255.0
#sudo route add default gw 10.128.250.1 eth0

sudo ifconfig eth2 192.168.3.3 netmask 255.255.255.0
#sudo route add default gw 10.127.250.1 eth1

#sudo ifconfig eth3 192.168.10.6 netmask 255.255.255.0
#sudo route add default gw 10.126.250.1 eth2
#sudo iptables -F
