# Python API Client

Install JDK 11
```bash
conda install -c conda-forge openjdk=11.0.9.1
# or
sudo apt install openjdk-11-jre openjdk-11-jdk
```

Generate python module:
```bash
cd tools/developer_tools/python-client
./generatePyClient.sh <cdb_server_url>

# More instructions at ./pythonApi/README.md
cd pythonApi
python setup.py install --user
```

**IMPORTANT**
Disable ssl checks
```python
import cdbApi

configuration = cdbApi.Configuration(
    host = "http://localhost"
)
configuration.verify_ssl = False
```

# License
[Copyright (c) UChicago Argonne, LLC. All rights reserved.](https://github.com/AdvancedPhotonSource/ComponentDB/blob/master/LICENSE)
