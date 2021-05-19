# Component DB

[![Documentation Status](https://readthedocs.org/projects/componentdb/badge/?version=latest)](http://componentdb.readthedocs.io/en/latest/?badge=latest)
[![Build Status](https://travis-ci.org/AdvancedPhotonSource/ComponentDB.svg?branch=master)](https://travis-ci.org/AdvancedPhotonSource/ComponentDB)

**Prerequisites:**

In order to deploy or develop Component DB, you must have some support software installed. Follow the instructions below to achieve this.
    
    # For red-hat based linux distribution run the following:
    yum install -y gcc libgcc expect zlib-devel openssl-devel openldap-devel readline-devel git make cmake sed gawk autoconf automake wget mysql mysql-libs mysql-server mysql-devel curl unzip
    # For debian based linux distributions run the following:
    apt-get install wget gcc git make cmake build-essential libcurses-ocaml-dev curl expect mysql-server libmysqlclient-dev openssl libssl-dev libldap2-dev libsasl2-dev sed gawk unzip

# Deployment

[**Deployment Procedure**](./docs/DEPLOYMENT.md)

# Development 

[**Development Procedure**](./docs/DEVELOPMENT.md)

## Python API Client

[**Python Client**](./docs/CLIENT.md)

# License
[Copyright (c) UChicago Argonne, LLC. All rights reserved.](https://github.com/AdvancedPhotonSource/ComponentDB/blob/master/LICENSE)
