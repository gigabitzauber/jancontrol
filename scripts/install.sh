#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root."
   exit 1
fi

APP_NAME="jancontrol"
INSTALL_DIR="/opt/$APP_NAME"
REPO="gigabitzauber/jancontrol"

read -p "Do you want to start the installation of $APP_NAME? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Installation aborted."
    exit 1
fi

mkdir -p $INSTALL_DIR

LATEST_RELEASE=$(curl -s https://api.github.com/repos/$REPO/releases/latest)
JAR_URL=$(echo $LATEST_RELEASE | grep -oP '"browser_download_url": "\K[^"]*\.jar')
SERVICE_FILE_URL=$(echo $LATEST_RELEASE | grep -oP '"browser_download_url": "\K[^"]*\.service')

curl -L $JAR_URL -o "$INSTALL_DIR/$APP_NAME.jar"
curl -L $SERVICE_FILE_URL -o "/etc/systemd/system/$APP_NAME.service"

# 5. Prompt before enabling and starting
read -p "Installation files downloaded. Do you want to enable and start the service now? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Service not started. You can start it manually later with: systemctl enable --now $APP_NAME"
    exit 0
fi

# 6. Enable and start service
systemctl daemon-reload
systemctl enable --now $APP_NAME
echo "Service $APP_NAME started."
echo "Remember to provide a proper config file at /etc/$APP_NAME.yaml"
