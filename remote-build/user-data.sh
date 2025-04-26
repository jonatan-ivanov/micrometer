#! /bin/bash

sudo apt update -y
sudo apt upgrade -y
sudo apt install openjdk-17-jdk -y

uname -a
cat /etc/os-release
git --version
java --version
