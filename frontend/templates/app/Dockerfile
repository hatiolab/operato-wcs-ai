# Use an official ubuntu image
FROM ubuntu:18.04

# Use an official node image
FROM node:12.20.2

ARG DEBIAN_FRONTEND=noninteractive

# Install the required packages
RUN apt-get update -o Acquire::CompressionTypes::Order::=gz
RUN apt-get upgrade -y

RUN echo "deb http://ftp.de.debian.org/debian stable main" > /etc/apt/sources.list

RUN apt-get update

RUN apt-get install -y --no-install-recommends apt-utils
RUN apt-get install -y chromium
RUN apt-get install -y libcups2-dev 
RUN apt-get install -y libavahi-compat-libdnssd-dev 
RUN apt-get install -y gconf-service libasound2 libatk1.0-0 libcairo2 libcups2 libfontconfig1 libgdk-pixbuf2.0-0 libgtk-3-0 libnspr4 libpango-1.0-0 libxss1 fonts-liberation libnss3 lsb-release xdg-utils libaio1

RUN apt update
RUN apt-get install -y ghostscript
RUN apt-get install -y curl
RUN apt-get install -y git

# install chrome
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN dpkg -i google-chrome-stable_current_amd64.deb; apt-get -fy install
RUN rm google-chrome-stable_current_amd64.deb



# make oracle path
RUN mkdir -p /opt/oracle
WORKDIR /opt/oracle

# download newest oracle cilent for connect to Oracle Database
RUN wget https://download.oracle.com/otn_software/linux/instantclient/instantclient-basiclite-linuxx64.zip && \
    unzip instantclient-basiclite-linuxx64.zip && \ 
    rm -f instantclient-basiclite-linuxx64.zip && \
    cd /opt/oracle/instantclient* && \
    rm -f *jdbc* *occi* *mysql* *mql1* *ipc1* *jar uidrvci genezi adrci && \
    echo /opt/oracle/instantclient* > /etc/ld.so.conf.d/oracle-instantclient.conf &&\
    ldconfig

# Set the working directory to /app
WORKDIR /app

# copy application & configuration files
COPY . .

# run node install command
RUN yarn install

# Make port 3000 available to the world outside this container
EXPOSE 3000

CMD [ "yarn", "run", "serve" ]