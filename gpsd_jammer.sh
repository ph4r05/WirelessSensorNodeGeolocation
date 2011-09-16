#!/bin/bash
#
# gpsd udev rules makes problem since tmote sky has same signature as GPSD
# rule conflicting:
# ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", SYMLINK+="gps%n", RUN+="/lib/udev/gpsd.hotplug.wrapper"
#
# own udev rule for tmote

# main daemon loop
while [ true ]; do
	# Allowed to run?
	ALLOWED=1

	if [ $ALLOWED -eq 1 ]; then
		# allowed to rsync
		echo "Jamming! `date`"

		# slow down to 640KBps = 5Mbit
		# write time of last sync to timeout file
		sudo /sbin/service gpsd stop

                sudo chmod 0666 /dev/ttyUSB0 2> /dev/null
	fi

     # sleep for next check, sleep for 5 minutes
     sleep 5
done
