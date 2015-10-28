#!/bin/bash
#
# Check whether init.d or systemd is used and install the start script
# edit path to the client starter script in the init scripts
#
# sudo needed!!!
#
CLIENT_PATH=~/.studip_client
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# save clientlocation to settings file
echo "Copy Client Files to " $CLIENT_PATH
if [ -d $CLIENT_PATH ]
then echo "Client is already installed"
else 
    mkdir $CLIENT_PATH
    cd $DIR
    cp uninstall.sh $CLIENT_PATH
    cp studip_client.sh $CLIENT_PATH
    cp updater.jar $CLIENT_PATH
    cp install.sh $CLIENT_PATH

    # Go to Client and start it
    echo "First run Client to update it self"
    cd $CLIENT_PATH
    sh studip_client.sh

fi

