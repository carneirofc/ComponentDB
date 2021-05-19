# Development 
For detailed development instructions please refer to our [developers guide](https://confluence.aps.anl.gov/display/APSUCMS/Developer+Guide). 

**Getting Started with development:**

    # first make a fork of this project. 
    # create a desired development directory and cdb into it
    mkdir $desired_dev_directory
    cd $desired_dev_directory
    git clone https://github.com/AdvancedPhotonSource/ComponentDB.git
    
    # Getting support software
    cd ComponentDb
    make support 
    # Get Netbeans
    make support-netbeans

    # Load up the environment 
    source setup.sh

    # Prepare Dev DB    
    # mysql could be installed as part of ComponentDB support by running 'make support-mysql' 
    # - Afterwards run `./etc/init.d/cdb-mysql start`
    # if you have mysql installed and started run...
    make clean-db   # sample-db will be coming later 
    
    # Start development
    make dev-config     

    # Open Netbeans
    netbeans & 

## Preparing Netbeans
Once netbeans is open a few steps need to be taken to prepare netbeans for CDB development.
1. Click Tools > Plugins
2. Navigate to Available Plugins and install the following:
    * nbjavac Library
    * Oracle JS Parser Implementation
    * Payara EE Common
    * Payara Common
    * Payara Server
    * Payara Tooling
3. Add payara server reference. 
4. Click Window > Services 
5. Right click Server > Add Server
6. Select Payara 
7. Use installation location: $desired_dev_directory/support-`hostname`/netbeans/payara
8. Ensure Local domain is selected and hit next. 
9. Leaave defaults and hit finish. 
10. Open CDB Project: File > Open Project
11. Navigate to $desired_dev_directory/ComponentDB/src/java
12. Select CdbWebPortal and hit Open Project
13. If prompted install: JavaFX Implementation for linux
14. Right click CdbWebPortal and hit Resolve Data Source Problem...
15. Add Connection > Add Driver File
16. Navigate to $desired_dev_directory/ComponentDB/src/java/CdbWebPortal/lib/mysql-connector-java-*-bin.jar
17. Finish Add Connection wizard.
18. Run the project 
    
# License
[Copyright (c) UChicago Argonne, LLC. All rights reserved.](https://github.com/AdvancedPhotonSource/ComponentDB/blob/master/LICENSE)
