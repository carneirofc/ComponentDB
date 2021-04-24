#!/bin/bash

# Copyright (c) UChicago Argonne, LLC. All rights reserved.
# See LICENSE file.

ANACONDA_VERSION=3.8.3

CDB_HOST_ARCH=$(uname -sm | tr -s '[:upper:][:blank:]' '[:lower:][\-]')

currentDir=`pwd`
cd `dirname $0`/.. && topDir=`pwd`

srcDir=$topDir/src
buildDir=$topDir/build
anacondaInstallDir=$topDir/anaconda/$CDB_HOST_ARCH
anacondaFileName="Miniconda3-${ANACONDA_VERSION}-Linux-x86_64.sh"

echo $anacondaFileName

DOWNLOAD_URL="https://repo.anaconda.com/miniconda/$anacondaFileName"

mkdir -p $srcDir
cd $srcDir

if [ ! -f $anacondaFileName ]; then
    echo "Retrieving $DOWNLOAD_URL"
    curl -o $anacondaFileName $DOWNLOAD_URL
fi
set -x
if [ -f $anacondaFileName ]; then
    path="-p $anacondaInstallDir"

    echo "Installing anaconda"
    sh $anacondaFileName -b $path -f
fi
set +x
