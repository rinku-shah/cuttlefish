#!/bin/bash

sudo ovs-vsctl del-controller br0
sudo ovs-vsctl --if-exists del-port br0 eth1
sudo ovs-vsctl --if-exists del-port br0 eth2
sudo ovs-vsctl --if-exists del-port br0 eth4
sudo ovs-vsctl --if-exists del-port br0 eth5
sudo ovs-vsctl --if-exists del-port br0 eth6
sudo ovs-vsctl --if-exists del-port br0 eth7
sudo ovs-vsctl --if-exists del-port br0 eth8

sudo ovs-vsctl --if-exists del-port br0 int1
sudo ovs-vsctl --if-exists del-port br0 int2
sudo ovs-vsctl --if-exists del-port br0 int3
sudo ovs-vsctl --if-exists del-port br0 int4
sudo ovs-vsctl --if-exists del-port br0 int5
sudo ovs-vsctl --if-exists del-port br0 int6
sudo ovs-vsctl --if-exists del-port br0 int7

sudo ovs-vsctl --if-exists del-br br0

sudo ifconfig eth1 192.168.3.4 netmask 255.255.255.0
#sudo route add default gw 10.128.250.1 eth0

sudo ifconfig eth2 192.168.4.4 netmask 255.255.255.0
#sudo route add default gw 10.127.250.1 eth1

sudo ifconfig eth4 192.168.9.4 netmask 255.255.255.0
#sudo route add default gw 10.126.250.1 eth2
#sudo iptables -F

sudo ifconfig eth5 192.168.10.4 netmask 255.255.255.0

sudo ifconfig eth6 192.168.13.4 netmask 255.255.255.0

sudo ifconfig eth7 192.168.100.4 netmask 255.255.255.0

#sudo ifconfig eth8 192.168.100.6 netmask 255.255.255.0
