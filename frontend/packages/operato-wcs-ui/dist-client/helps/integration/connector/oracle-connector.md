# Oracle connector

## Support
    1. requires Oracle Client libraries version 11.2 or later
    2. Can connect to Oracle Database 10g or later

## Setting
### MAC OS
* install oracle client 
    ```sh
    cd $HOME/Downloads
    curl -O https://download.oracle.com/otn_software/mac/instantclient/198000/instantclient-basic-macos.x64-19.8.0.0.0dbru.dmg
    hdiutil mount instantclient-basic-macos.x64-19.8.0.0.0dbru.dmg
    /Volumes/instantclient-basic-macos.x64-19.8.0.0.0dbru/install_ic.sh
    hdiutil unmount /Volumes/instantclient-basic-macos.x64-19.8.0.0.0dbru

    ln -s ~/Downloads/instantclient_19_8/libclntsh.dylib node_modules/oracledb/build/Release
    ```

### Linux
* make oracle path
    ```sh
    mkdir -p /opt/oracle
    cd /opt/oracle
    ```
* download newest oracle cilent for connect to Oracle Database
    ```sh
    wget https://download.oracle.com/otn_software/linux/instantclient/instantclient-basiclite-linuxx64.zip && \
        unzip instantclient-basiclite-linuxx64.zip && \ 
        rm -f instantclient-basiclite-linuxx64.zip && \
        cd /opt/oracle/instantclient* && \
        rm -f *jdbc* *occi* *mysql* *mql1* *ipc1* *jar uidrvci genezi adrci && \
        echo /opt/oracle/instantclient* > /etc/ld.so.conf.d/oracle-instantclient.conf &&\
        ldconfig
    ```
### Detail please Refer
* https://oracle.github.io/node-oracledb/INSTALL.html


## endpoint

Oracle engine hostname and service port

- hostname : database server host
- service port : database service port (default - 1521)
- format
  - {hostname}[:{port}]
  - eg.
    - "localhost:1521"
    - "localhost"
    - "192.168.0.1:1521"

## Parameters

### user

- user id for oracle database

### password

- user password for oracle database

### database

- oracle sid name

