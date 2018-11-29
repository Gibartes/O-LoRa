#!/bin/bash

# Start script

function show_help(){
	echo """
************************************************************************
*   Usage : ./start.sh Option 1 Option 2 Option 3
****************************************************************************
*    Option Description
****************************************************************************
*    Control Unit Start Option : 
*        help    : show this text message.
*         run    : run olora
*        make   : build olora system
*                   install/uninstall     : register at the start script (oloraNT)
*                   cu install/uninstall : register at the start script (python Files)
*        kill    : kill olora processes being executed.
*****************************************************************************
	"""
}

clear
echo "[*] Olora System [*] "

if [ $# < 1 ]; then
	show_help
	exit 0
fi

cd ./gate
if   [ "$1" = "help" ]; then
	show_help
	exit 0
elif [ "$1" = "run" ]; then
	python3 gate.py
	../bluetooth/src/oloraNT
	python3 ../xbee/oloraXB.py 
	exit 0
elif [ "$1" = "make" ] && [ "$2" = "install" ] ||  [ "$2" = "uninstall" ]; then 
	cd - > /dev/null
	cd ./bluetooth/src
	make clean > /dev/null
	make $2
	if [ $? -gt 0 ]; then
		echo "[!] Abort."
		exit 0
	fi
elif  [ "$1" = "make" ] && [ "$2" = "cu" ] && [ "$3" = "install" ]; then 
	mkdir -p /usr/local/olora/bluetooth
	mkdir -p /usr/local/olora/gate
	mkdir -p /usr/local/olora/xbee
	cp -rf ../bluetooth /usr/local/olora/
	cp -rf ../gate /usr/local/olora/
	cp -rf ../xbee /usr/local/olora/
	chmod u+x ./*.service
	chmod u+x ./*.py
	cp -rf ../gate/*.service /etc/systemd/system/
	sudo systemctl enable oloragate
	sudo systemctl enable oloraxb
	sudo service oloragate start
	sudo service oloraxb start	
	if [ $? -gt 0 ]; then
		echo "[!] Abort."
		exit 0
	fi
	exit 0
elif [ "$1" = "make" ] && [ "$2" = "cu" ] && [ "$3" = "uninstall" ]; then
	sudo systemctl disable oloragate
	sudo systemctl disable oloraxb
	sudo service oloraxb stop	
	sudo service oloragate stop
	rm -rf /etc/systemd/system/oloraxb.service	
	rm -rf /etc/systemd/system/oloragate.service
	rm -rf /usr/local/olora
	exit 0
elif [ "$1" = "make" ]; then 
	cd - > /dev/null
	cd ./bluetooth/src
	make clean > /dev/null
	make > /dev/null
	if [ $? -gt 0 ]; then
		echo "[!] Detect errors when build Bluetooth Network."
		echo "[*] Dependency re-install."
		if [ "$(. /etc/os-release; echo $ID_LIKE)" = "debian" ]; then
			sudo apt update
			sudo apt install libbluetooth-dev -y
			sudo apt install libssl-dev -y
			sudo apt install libpam-dev -y
			sudo pip3 install pyolora
		elif [ "$(. /etc/os-release; echo $ID_LIKE)" = "fedora" ]; then
			sudo dnf update
			sudo dnf install -y bluez bluez-libs
			sudo dnf install -y openssl-devel
			sudo pip3 install pyolora			
		fi
		make clean -> /dev/null
		make
		if [ $? -gt 0 ]; then
			echo "[!] Abort: Build Failure."
			exit 0
		fi
	else
		echo "[*] Builded."
	fi
	exit 0	
elif [ "$1" = "kill" ]; then
	echo "[!] Kill all."
	pkill -9 "olora*"
	exit 0
fi
cd - > /dev/null