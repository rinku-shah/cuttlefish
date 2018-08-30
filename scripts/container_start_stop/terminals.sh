#!/bin/bash

# Usage: ./terminals.sh
# Do not run as superuser

# Start the containers
sudo lxc-start -n root_1_May -d
#sudo lxc-start -n root_2_May -d
#sudo lxc-start -n ran_2 -d
sudo lxc-start -n ran_3 -d
sudo lxc-start -n ran_6 -d
#sudo lxc-start -n dgw_2 -d
sudo lxc-start -n dgw_3 -d
sudo lxc-start -n dgw_6 -d
#sudo lxc-start -n sgw_2 -d
sudo lxc-start -n sgw_3 -d
sudo lxc-start -n sgw_6 -d
#sudo lxc-start -n pgw_1 -d
#sudo lxc-start -n sink_1 -d

#sleep 3
sudo lxc-ls --fancy

#echo "Pinging ran_1..."
#ping 10.129.2.203 -c 2
#echo
#echo "Pinging ran_2..."
#ping 10.129.2.214 -c 2
#echo
#echo "Pinging dgw_1..."
#ping 10.129.2.206 -c 2
#echo
#echo "Pinging dgw_2..."
#ping 10.129.2.217 -c 2
#echo
#echo "Pinging sgw_1..."
#ping 10.129.2.209 -c 2
#echo
#echo "Pinging sgw_2..."
#ping 10.129.2.220 -c 2
#echo
#echo "Pinging pgw_1..."
#ping 10.129.2.212 -c 2
#echo
#echo "Pinging sink_1..."
#ping 10.129.41.96 -c 2
#echo
#echo "Pinging root_1..."
#ping 10.0.3.244 -c 2
#echo
#echo "Pinging ran_3..."
#ping 10.0.3.39 -c 2
#echo
#echo "Pinging dgw_3..."
#ping 10.0.3.156 -c 2
#echo
#echo "Pinging sgw_3..."
#ping 10.0.3.167 -c 2

#Open terminal and login to ue
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh ubuntu@10.0.3.39; bash" &
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh ubuntu@10.0.3.39; bash" &

#Open terminal and login to sink
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh ubuntu@10.0.3.166; bash" &

#sleep 180

#Open terminal and login to dgw
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.156 -t 'echo ubuntu | sudo -S su && sudo su; bash -l'; bash" &
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.156 -t 'echo ubuntu | sudo -S su && sudo su; bash -l'; bash" &

#Open terminal and login to sgw
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.167 -t 'echo ubuntu | sudo -S su && sudo su; bash -l'; bash" &
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.167 -t 'echo ubuntu | sudo -S su && sudo su; bash -l'; bash" &

#Open terminal and login to pgw
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.32 -t 'echo ubuntu | sudo -S su && sudo su; bash -l'; bash" &

#Open terminal and login to controller
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.244; bash" 

#Open terminal and login to kv_store
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -XY ubuntu@10.0.3.191; bash" &


#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -X ubuntu@10.0.3.243 -t 'eclipse; bash -l'; bash"
#Open terminal and login to controller
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -X ubuntu@10.0.3.31 -t '~/eclipse/java-neon/eclipse/eclipse ; bash -l'; bash" &

#Open terminal and login to controller_2
#gnome-terminal -x sh -c "sshpass -p ubuntu ssh -X ubuntu@10.0.3.245; bash"





