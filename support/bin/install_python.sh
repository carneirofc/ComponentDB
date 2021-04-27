#!/bin/bash
set -ex

# Copyright (c) UChicago Argonne, LLC. All rights reserved.
# See LICENSE file.


currentDir=`pwd`
cd `dirname $0`/.. && topDir=`pwd`
binDir=$topDir/bin

$binDir/build_python.sh || exit 1
$binDir/install_setuptools.sh || exit 1
$binDir/install_pip.sh || exit 1
