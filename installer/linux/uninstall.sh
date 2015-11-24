#!/bin/bash

CLIENT_PATH=~/.studip_client
APPLICATION_NAME='StudIP_Client.jar'

echo "Remove Client from " $CLIENT_PATH
if [ -d $CLIENT_PATH ]
    then
    cd $CLIENT_PATH

    #shut down Client if running
    PID=`ps -aux | grep [-]Dname=StudIP_Client | awk {'print $2'}`
    echo $PID
    if [[ $PID -gt 0 ]] # =~ "^[0-9]+$"
        then
    	echo "Shut down Client"
        kill -HUP $PID
    fi
    
    if [ -f "StudIP_Client.jar" ]
    then
	echo "Delete Java userPrefs"
        java -jar StudIP_Client.jar -d
    else
        echo "Could not delete Java userPrefs. Please delete manually from ~/.java/.userPrefs"
    fi
    
    # Remove Files
    echo "Remove all Client Files"
    rm -R $CLIENT_PATH

fi

# Remove .desktop File from autostart folder
autostartfile=~/.config/autostart/studip_client.desktop
if [ -f $autostartfile ] 
    then
    echo "Remove Autostart Link"
    rm $autostartfile
fi

