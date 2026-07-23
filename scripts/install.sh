#!/bin/bash

set -eo pipefail

APP_NAME="jancontrol"
INSTALL_DIR="/opt/$APP_NAME"
JC_TMP_DIR="/tmp/${APP_NAME}"
REPO="gigabitzauber/jancontrol"

cleanup() {
    rm -rf "$JC_TMP_DIR"
}

trap cleanup EXIT

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root."
   exit 1
fi

read -p "Do you want to start the installation of $APP_NAME? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Installation aborted."
    exit 1
fi

mkdir -p $INSTALL_DIR
mkdir -p "${JC_TMP_DIR}"
cd "${JC_TMP_DIR}" || exit 1

LATEST_RELEASE=$(curl -s https://api.github.com/repos/$REPO/releases/latest)
# -o.. output match only
# -P.. expression is a Perl regexp
# -m1.. print first match only
# \K.. reset match point start
JAR_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '.*"browser_download_url": "\K.*[0-9]+\.jar')
#SERVICE_FILE_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '"browser_download_url": "\Kjancontrol.service')
HASH_FILE_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '"browser_download_url": "\K.*jancontrol-hashes.sha256')
HASH_FILE_ASC_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '"browser_download_url": "\K.*jancontrol-hashes.sha256.asc')
RAW_VERSION=$(echo "${LATEST_RELEASE}" | grep -oP '"tag_name": "\K[^"]*')
VERSION=${RAW_VERSION#v}
CONFIG_FILE_PATH="/etc/${APP_NAME}.yaml"

curl -L "${JAR_URL}" -o "${APP_NAME}-${VERSION}.jar"
curl -L "${SERVICE_FILE_URL}" -o "$APP_NAME.service"
curl -L "${HASH_FILE_URL}" -o "jancontrol-hashes.sha256"
curl -L "${HASH_FILE_ASC_URL}" -o "jancontrol-hashes.sha256.asc"

gpg --no-default-keyring --keyring temp-keyring.gpg --recv-key --keyserver hkps://keys.openpgp.org 61A05420313C12BB
gpg --no-default-keyring --keyring temp-keyring.gpg --verify "jancontrol-hashes.sha256.asc" "jancontrol-hashes.sha256"

sha256sum --check --ignore-missing "jancontrol-hashes.sha256"

cp "${APP_NAME}-${VERSION}.jar" "$INSTALL_DIR/${APP_NAME}.jar"
cp "${APP_NAME}.service" "/etc/systemd/system/"

if [ ! -f "${CONFIG_FILE_PATH}" ]; then
  touch "${CONFIG_FILE_PATH}"
  echo "# Default ${APP_NAME} config file" >> "${CONFIG_FILE_PATH}"
  echo "# See https://github.com/gigabitzauber/jancontrol/tree/main/docs/examples for config file examples." >> "${CONFIG_FILE_PATH}"
fi

read -p "Installation finished. Do you want to enable and start the service now? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Service not started. You can start it manually later with: systemctl enable --now $APP_NAME"
    exit 0
fi

systemctl daemon-reload
systemctl enable --now $APP_NAME
echo "Service $APP_NAME started."
echo "Remember to provide a proper config file at ${CONFIG_FILE_PATH}"
