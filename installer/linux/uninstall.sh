#!/bin/bash
#
# Check whether init.d or systemd is used and install the start script
# edit path to the client starter script in the init scripts
#
# sudo needed!!!
#
CLIENT_PATH=~/.studip_client
APPLICATION_NAME='StudIP_Client.jar'

echo "Remove Client from " $CLIENT_PATH
if [ -d $CLIENT_PATH ]
    then
    cd $CLIENT_PATH

    #shut down Client if running
    echo "Shut down Client if running"
    PID=`ps -aux | grep [-]Dname=StudIP_Client | awk {'print $2'}`
    if [[ "$PID" =~ "^[0-9]+$" ]]
        then
        kill -HUP $PID
    fi
    
    if [ -f "StudIP_Client.jar" ]
    then
        java -jar StudIP_Client.jar -d
    else
        echo "Could not delete Java userPrefs. Please delete manually from ~/.java/.userPrefs"
    fi
    
    # Remove Files
    rm -R $CLIENT_PATH
fi

# Remove .desktop File from autostart folder
echo "Remove Autostart Link"
autostartfile="~/.config/autostart/studip_client.desktop"
if [ -f "$autostartfile" ] 
    then
    rm $autostartfile
fi

