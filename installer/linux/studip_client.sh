#!/bin/bash
#
# Starting script for the StudIP Client
#
#
cd ~/.studip_client
exec java -Dname=StudIP_Client -jar updater.jar > /dev/null &

