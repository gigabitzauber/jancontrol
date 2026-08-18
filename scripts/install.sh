#!/bin/bash

set -eo pipefail

SCRIPT_VERSION="0.0.3"
APP_NAME="jancontrol"
GIZA_GPG_KEY_ID="61A05420313C12BB"
INSTALL_DIR="/opt/$APP_NAME"
JC_TMP_DIR="/tmp/${APP_NAME}"
REPO="gigabitzauber/jancontrol"

cleanup() {
    rm -rf "$JC_TMP_DIR" || true
}

trap cleanup EXIT

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root."
   exit 1
fi

LATEST_RELEASE=$(curl -s https://api.github.com/repos/$REPO/releases/latest)
RAW_VERSION=$(echo "${LATEST_RELEASE}" | grep -oP '"tag_name": "\K[^"]*')

echo "This is $APP_NAME install script version $SCRIPT_VERSION"
read -p "Do you want to start the installation of $APP_NAME $RAW_VERSION? (y/n) " -r </dev/tty
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Installation aborted."
    exit 1
fi

mkdir -p $INSTALL_DIR
mkdir -p "${JC_TMP_DIR}"
cd "${JC_TMP_DIR}" || exit 1

# -o.. output match only
# -P.. expression is a Perl regexp
# -m1.. print first match only
# \K.. reset match point start
JAR_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '.*"browser_download_url": "\K.*[0-9]+\.jar')
SERVICE_FILE_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '.*"browser_download_url": "\K.*jancontrol.service')
HASH_FILE_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '.*"browser_download_url": "\K.*jancontrol-hashes.sha256')
HASH_FILE_ASC_URL=$(echo "${LATEST_RELEASE}" | grep -oP -m1 '.*"browser_download_url": "\K.*jancontrol-hashes.sha256.asc')
VERSION=${RAW_VERSION#v}
CONFIG_FILE_PATH="/etc/${APP_NAME}.yaml"
SYSTEMD_UNIT_FILE_NAME="${APP_NAME}.service"
SYSTEMD_UNIT_FILE_PATH="/etc/systemd/system/${SYSTEMD_UNIT_FILE_NAME}"
JAR_FILE_NAME="${APP_NAME}-${VERSION}.jar"

curl -L "${JAR_URL}" -o "${JAR_FILE_NAME}"
curl -L "${SERVICE_FILE_URL}" -o "$SYSTEMD_UNIT_FILE_NAME"
curl -L "${HASH_FILE_URL}" -o "jancontrol-hashes.sha256"
curl -L "${HASH_FILE_ASC_URL}" -o "jancontrol-hashes.sha256.asc"

gpg --homedir "${JC_TMP_DIR}/.gnupg" --recv-key --keyserver hkps://keys.openpgp.org "${GIZA_GPG_KEY_ID}"
gpg --homedir "${JC_TMP_DIR}/.gnupg" --verify "jancontrol-hashes.sha256.asc" "jancontrol-hashes.sha256"

sha256sum --check --ignore-missing "jancontrol-hashes.sha256"
JAR_HASH=$(sha256sum "${JAR_FILE_NAME}" | cut -d' ' -f1)

if systemctl is-active --quiet "$APP_NAME"; then
    read -p "$APP_NAME is already running as a systemd service. Do you want to stop it before updating? (y/n) " -r </dev/tty
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping $APP_NAME service.."
        systemctl stop "$APP_NAME"
    fi
fi

cp "${JAR_FILE_NAME}" "$INSTALL_DIR/${APP_NAME}.jar"
echo "v${VERSION} installed on $(date -Is)" > "$INSTALL_DIR/install.log"
echo "SHA256 hash of ${APP_NAME}.jar: ${JAR_HASH}" >> "$INSTALL_DIR/install.log"

if [ -f "$SYSTEMD_UNIT_FILE_PATH" ]; then
    read -p "A systemd service file already exists at ${SYSTEMD_UNIT_FILE_PATH}. Do you want to overwrite it? (y/n) " -r </dev/tty
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Overwriting existing systemd service file."
        cp "${SYSTEMD_UNIT_FILE_NAME}" "$SYSTEMD_UNIT_FILE_PATH"
    fi
else
    cp "${SYSTEMD_UNIT_FILE_NAME}" "$SYSTEMD_UNIT_FILE_PATH"
fi

if [ ! -f "${CONFIG_FILE_PATH}" ]; then
  touch "${CONFIG_FILE_PATH}"
  # Individual echos provide better readability.
  # shellcheck disable=SC2129
  echo "# Default ${APP_NAME} config file" >> "${CONFIG_FILE_PATH}"
  echo "# See https://github.com/gigabitzauber/jancontrol/tree/main/docs/examples for config file examples." >> "${CONFIG_FILE_PATH}"
  echo "fans:" >> "${CONFIG_FILE_PATH}"
fi

read -p "Installation finished. Do you want to enable and start the service now? (y/n) " -r </dev/tty
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Service not started. You can start it manually later with: systemctl enable --now $APP_NAME"
    exit 0
fi

systemctl daemon-reload
systemctl enable --now $APP_NAME
echo "Service $APP_NAME started."
echo "Remember to provide a proper config file at ${CONFIG_FILE_PATH}"
