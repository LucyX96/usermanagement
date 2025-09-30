#!/bin/bash

# Cartella dove mettere i certificati
CERT_DIR="./certs"
mkdir -p "$CERT_DIR"
cd "$CERT_DIR" || exit

# Nome file chiave e certificato
KEY_FILE="key.pem"
CERT_FILE="cert.pem"
PKCS12_FILE="keystore.p12"
ALIAS="spring"
PASSWORD="changeit"

echo "Generazione della chiave privata..."
openssl genrsa -out "$KEY_FILE" 2048

echo "Generazione del certificato autofirmato..."
openssl req -x509 -new -nodes -key "$KEY_FILE" -sha256 -days 365 -out "$CERT_FILE" \
  -subj "/C=IT/ST=State/L=City/O=Organization/OU=Department/CN=localhost"

echo "Creazione del keystore PKCS12..."
openssl pkcs12 -export -in "$CERT_FILE" -inkey "$KEY_FILE" -out "$PKCS12_FILE" \
  -name "$ALIAS" -password pass:"$PASSWORD"

echo "Tutti i file generati in $CERT_DIR:"
ls -l

echo ""
echo "Spring Boot properties da usare:"
echo "server.port=8443"
echo "server.ssl.key-store=$(pwd)/$PKCS12_FILE"
echo "server.ssl.key-store-password=$PASSWORD"
echo "server.ssl.key-store-type=PKCS12"
echo "server.ssl.key-alias=$ALIAS"
