#!/bin/bash

# Copyright (c) UChicago Argonne, LLC. All rights reserved.
# See LICENSE file.
set -ex

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
    ls -la
fi
if [ -f $anacondaFileName ]; then
    path="-p $anacondaInstallDir"
    mkdir -p ${anacondaInstallDir}

    echo "Installing anaconda"
    bash ./$anacondaFileName -b $path -f
    export PATH="${anacondaInstallDir}/bin:$PATH"
    ls -l ${anacondaInstallDir}/bin
    conda install pip -y
fi

pip install click


