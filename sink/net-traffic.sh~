#!/bin/sh
#./net-traffic.sh -i eth1 -s 1 -n 1 -c 110  
#./net-traffic.sh -i eth1 -s 60 -c 5 -n 1 -t 5 -r 15000M
usage(){
echo "Usage: $0 [-i INTERFACE] [-s INTERVAL] [-c COUNT] [-n NUM_UE] [-o OUTPUT_FILE] [-h]"
echo
echo "-i INTERFACE"
echo "    The interface to monitor, default is eth1."
echo "-s INTERVAL"
echo "    The time to wait in seconds between measurements, default is 250 seconds."
echo "-c COUNT"
echo "    The number of times to measure, default is 1."
echo "-n NUM_UE"
echo "    The number of UE, default is 1."
echo "-t TIME(MIN)"
echo "	  Time for which the network traffic is monitored."
echo "-o OUTPUT_FILE"
echo "    File name for output."
echo "-r DATA RATE"
echo "Data Rate (M/K)"
echo "-h"
echo "    Print usage."
exit 4
}

readargs(){
while [ "$#" -gt 0 ] ; do
  case "$1" in
   -i)
    if [ "$2" ] ; then
     interface="$2"
     shift ; shift
    else
     echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -s)
    if [ "$2" ] ; then
     sleep="$2"
     shift ; shift
    else
     echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -c)
    if [ "$2" ] ; then
     counter="$2"
     shift ; shift
    else
     echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -n)
    if [ "$2" ] ; then
     numue="$2"
     shift ; shift
    else
	 echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -o)
    if [ "$2" ] ; then
     outputFile="$2"
     shift ; shift
    else
	 echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -t)
    if [ "$2" ] ; then
     time="$2"
     shift; shift
    else
	echo "Missing a value for $1."
     echo
     shift
     usage
    fi
   ;;
   -r)
    if [ "$2" ] ; then
     rate="$2"
     shift; shift
    else
        echo "Missing value for $1."
     echo
     shift
     usage
    fi
   ;;
   -h)
    shift
    usage
   ;;
   *)
    echo "Unknown option $1."
    echo
    shift
    usage
   ;;
  esac
done
}

checkargs(){
if [ ! "$interface" ] ; then
  interface="eth1"
fi
if [ ! "$sleep" ] ; then
  sleep="290"
fi
if [ ! "$counter" ] ; then
  counter="1"
fi
if [ ! "$numue" ] ; then
  numue="1"
fi
if [ ! "$outputFile" ] ; then
  outputFile="throughput.csv"
fi
}

printrxbytes(){
/sbin/ifconfig "$interface" | grep "RX bytes" | cut -d: -f2 | awk '{ print $1 }'
}

printtxbytes(){
/sbin/ifconfig "$interface" | grep "TX bytes" | cut -d: -f3 | awk '{ print $1 }'
}

#################################################
bytestohumanreadable(){
multiplier="0"
number="$1"
while [ "$number" -ge 1000 ] ; do
  multiplier=$(($multiplier+1))
  number=$(($number/1000))
done
case "$multiplier" in
  1)
   echo "$number Kb"
  ;;
  2)
   echo "$number Mb"
  ;;
  3)
   echo "$number Gb"
  ;;
  4)
   echo "$number Tb"
  ;;
  *)
   echo "$1 b"
  ;;
esac
}
##################################################
  
printresults(){
duration=$(($counter * $sleep))
total=0
totalRecv=0
totalSend=0
while [ "$counter" -ge 0 ] ; do
  counter=$(($counter - 1))
  if [ "$rxbytes" ] ; then
   oldrxbytes="$rxbytes"
   oldtxbytes="$txbytes"
  fi
  rxbytes=$(printrxbytes)
  txbytes=$(printtxbytes)
  if [ "$oldrxbytes" -a "$rxbytes" -a "$oldtxbytes" -a "$txbytes" ] ; then
   temp1=$(($rxbytes - $oldrxbytes))
   temp2=$(($txbytes - $oldtxbytes))
   totalRecv=$(($totalRecv + $temp1))
   totalSend=$(($totalSend + $temp2))
   total=$(($total + $temp1))
   total=$(($total + $temp2))
   echo "RXbytes = $(($rxbytes - $oldrxbytes)) TXbytes = $(($txbytes - $oldtxbytes))"
  else
   echo "Monitoring $interface every $sleep seconds. (RXbyte total = $rxbytes TXbytes total = $txbytes)"
  fi
  if [ "$counter" -ne -1 ] ; then
  	sleep "$sleep"
  fi
done

total=`echo "scale=4; ($total*8)/($duration*1000*1000)" | bc `
recv=`echo "scale=4; ($totalRecv*8)/($duration*1000*1000)" | bc `
snd=`echo "scale=4; ($totalSend*8)/($duration*1000*1000)" | bc `
printf "$numue "
echo $recv $snd $total
if [ ! -e "$outputFile" ]; then
 echo "NumUE,Receive,Send,Total,Time(min),Data Rate" >> $outputFile
fi;
echo $numue,$recv,$snd,$total,$time,$rate >> $outputFile
}

readargs "$@"
checkargs
printresults
