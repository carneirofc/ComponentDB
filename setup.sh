#!/bin/sh

# CDB setup script for Bourne-type shells
# This file is typically sourced in user's .bashrc file

myDir=`dirname $BASH_SOURCE`
currentDir=`pwd` && cd $myDir
if [ ! -z "$CDB_ROOT_DIR" -a "$CDB_ROOT_DIR" != `pwd` ]; then
    echo "WARNING: Resetting CDB_ROOT_DIR environment variable (old value: $CDB_ROOT_DIR)" 
fi
export CDB_ROOT_DIR=`pwd`

if [ -z $CDB_DATA_DIR ]; then
    export CDB_DATA_DIR=$CDB_ROOT_DIR/../data
    if [ -d $CDB_DATA_DIR ]; then
        cd $CDB_DATA_DIR
        export CDB_DATA_DIR=`pwd`
    fi
fi
if [ ! -d $CDB_DATA_DIR ]; then
    #echo "WARNING: $CDB_DATA_DIR directory does not exist. Developers should point CDB_DATA_DIR to the desired area." 
    unset CDB_DATA_DIR
fi

if [ -z $CDB_VAR_DIR ]; then
    export CDB_VAR_DIR=$CDB_ROOT_DIR/../var
    if [ -d $CDB_VAR_DIR ]; then
        cd $CDB_VAR_DIR
        export CDB_VAR_DIR=`pwd`
    else
    	unset CDB_VAR_DIR
    fi
fi

# Establish machine architecture
CDB_HOST_ARCH=`uname | tr [A-Z] [a-z]`-`uname -m` 

# Check support setup
if [ -z $CDB_SUPPORT_DIR ]; then
    export CDB_SUPPORT_DIR=$CDB_ROOT_DIR/../support 
    if [ -d $CDB_SUPPORT_DIR ]; then
        cd $CDB_SUPPORT_DIR
        export CDB_SUPPORT_DIR=`pwd`
    fi
fi
if [ ! -d $CDB_SUPPORT_DIR ]; then
    echo "Warning: $CDB_SUPPORT_DIR directory does not exist. Developers should point CDB_SUPPORT_DIR to the desired area." 
    unset CDB_SUPPORT_DIR
else
    export CDB_GLASSFISH_DIR=$CDB_SUPPORT_DIR/glassfish/$CDB_HOST_ARCH
fi

# Add to path only if directory exists.
prependPathIfDirExists() {
    _dir=$1
    if [ -d ${_dir} ]; then
        PATH=${_dir}:$PATH
    fi
}

prependPathIfDirExists $CDB_SUPPORT_DIR/java/$CDB_HOST_ARCH/bin
prependPathIfDirExists $CDB_SUPPORT_DIR/ant/bin
prependPathIfDirExists $CDB_ROOT_DIR/bin

# Done
cd $currentDir

