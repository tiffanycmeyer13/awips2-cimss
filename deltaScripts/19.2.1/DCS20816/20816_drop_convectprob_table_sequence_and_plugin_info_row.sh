#! /bin/bash

# DCS 20816 - Removes obsolete 'convectprob' table, sequence, and 'plugin_info' table row
#             and then deletes the h5 files.
#
# Author: Lee Cronce
# Date: February 5, 2019

psql=/awips2/psql/bin/psql

echo 'INFO: Removing obsolete convectprob table, sequence, and plugin_info table row.'

# Remove obsolete convectprob table, sequence, and plugin_info table row
${psql} -d metadata -U awipsadmin -Atc "
    DROP SEQUENCE IF EXISTS convectprobseq;
    DROP TABLE IF EXISTS convectprob;
    DELETE FROM plugin_info WHERE name = 'convectprob';
    "

echo 'INFO: Done with psql table operations.'

echo 'INFO: Deleting convectprob hdf5.'
DATA_DIRECTORY="/awips2/edex/data/hdf5/convectprob"

if [ ! -d ${DATA_DIRECTORY} ]; then
   echo "INFO: There was no convectprob data found on the system."
else
   rm -f ${DATA_DIRECTORY}/convectprob-*.h5
   rmdir ${DATA_DIRECTORY}/
fi

echo 'INFO: Done with hdf5 removal.'

exit
