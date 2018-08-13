if [ $# != 1 ]; then
        echo "Usage: sh sink.sh <num-servers>"
        exit 1
fi
sh sink_orig_.sh 192.168.4.5 13001 $1
sh sink_orig_.sh 192.168.4.5 23001 $1
sh sink_orig_.sh 192.168.4.5 33001 $1
sh sink_orig_.sh 192.168.4.5 43001 $1
sh sink_orig_.sh 192.168.4.5 53001 $1
sh sink_orig_.sh 192.168.4.5 63001 $1
