#!/bin/bash

# DR 21470 - Removes deprecated bundle references and converts resource data
#            data references of 'convectprob' to 'probsevere'.
#
# This script should be run on all processing servers for AWIPS II with the
# following invocation:
#
# ./convertConvectProbProcedureReferencesToProbSevere.sh <optional/path/name>
#
# Author: Lee Cronce
# Date: August 5, 2019

### Search for all 'procedures' directory instances under supplied path,
### otherwise default to standard /awips2/edex/data/utility
# Determine if diretory is supplied or if supplied directory exists
if [ $# -eq 0 ] || [ ! -d $1 ]
then
    
    searchPath=/awips2/edex/data/utility
    
else

    searchPath=$1

fi

for eachDir in $(find ${searchPath} -type d -name 'procedures')
do

### Remove deprecated 'displayShape' bundle reference
    # Determine if any instances of 'displayShape' exist within procedures
    fileCount=`grep -r 'displayShape="true"' ${eachDir} | awk -F: '{print $1}' | wc -l`

    # If instance exists, remove the reference from the file. 
    if [ ${fileCount} -gt 0 ]
    then
    
        for eachFile in $(grep -r 'displayShape="true"' ${eachDir} | awk -F: '{print $1}')
	do
	
	    echo 'Removing "displayShape" reference from procedure file: '${eachFile}
	    sed -i 's/displayShape="true"//g' ${eachFile}
	    chmod 640 ${eachFile}
	    chown awips:fxalpha ${eachFile}
	    
	done
    
    fi
    
### Convert constraint value for procedure plugin name to 'probsevere'
    # Determine if any instances of the constraint exist
    fileCount=`grep -r 'constraintValue="convectprob"' ${eachDir} | awk -F: '{print $1}' | wc -l`
    
    # If instance exists, convert the procedure reference
    if [ ${fileCount} -gt 0 ]
    then
    
        for eachFile in $(grep -r 'constraintValue="convectprob"' ${eachDir} | awk -F: '{print $1}')
	do
	
            echo 'Converting "convectprob" plugin reference to "probsevere" in procedure file: '${eachFile}
	    sed -i 's/constraintValue="convectprob"/constraintValue="probsevere"/g' ${eachFile}
	    chmod 640 ${eachFile}
	    chown awips:fxalpha ${eachFile}
	
	done
    
    fi

### Convert resource data reference for procedure to 'probSevere'
    # Determine if any instances of the resource data reference exist
    fileCount=`grep -r 'xsi:type="convectProbResourceData"' ${eachDir} | awk -F: '{print $1}' | wc -l`
    
    # If instance exists, convert the procedure reference
    if [ ${fileCount} -gt 0 ]
    then
    
        for eachFile in $(grep -r 'xsi:type="convectProbResourceData"' ${eachDir} | awk -F: '{print $1}')
	do
	
            echo 'Converting "convectProbResourceData" plugin reference to "probSevereResourceData" in procedure file: '${eachFile}
	    sed -i 's/xsi:type="convectProbResourceData"/xsi:type="probSevereResourceData"/g' ${eachFile}
	    chmod 640 ${eachFile}
	    chown awips:fxalpha ${eachFile}
	
	done
    
    fi

done

exit
