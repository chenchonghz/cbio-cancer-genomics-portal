#!/bin/sh

echo "Usage: moveSEG.sh <host> <user>"

HOST=$1
USER=$2

find $PORTAL_HOME/cgds-data -name *.seg | xargs -i% scp % $USER@$HOST:/srv/www/htdocs/cancergenomics/public-portal/
