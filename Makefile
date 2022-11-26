# Copyright 2022 VMware. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

all: test

test:
	./mvnw clean test

run:
	./mvnw spring-boot:run

clean:
	./mvnw clean

create-bindings: bindings/custom-cert/custom.crt bindings/custom-cert/type

view-custom-cert: create-bindings
	openssl x509 -in bindings/custom-cert/custom.crt -text -noout

bindings/custom-cert/custom.crt: generated-custom-cert/custom.crt
	mkdir -p bindings/custom-cert
	cp generated-custom-cert/custom.crt bindings/custom-cert

bindings/custom-cert/type:
	mkdir -p bindings/custom-cert
	/bin/echo -n ca-certificates > bindings/custom-cert/type

generated-custom-cert/root-ca.key:
	mkdir -p generated-custom-cert
	openssl genrsa -out generated-custom-cert/root-ca.key 4096

generated-custom-cert/root-ca.crt: generated-custom-cert/root-ca.key
	mkdir -p generated-custom-cert
	openssl req -x509 -new -nodes -key generated-custom-cert/root-ca.key -sha256 -days 1024 -out generated-custom-cert/root-ca.crt -subj "/C=FR/ST=Paris/O=VMware Inc./CN=*.demos.tanzu.vmware.com"

generated-custom-cert/custom.key:
	mkdir -p generated-custom-cert
	openssl genrsa -out generated-custom-cert/custom.key 2048

generated-custom-cert/custom.csr: generated-custom-cert/custom.key
	mkdir -p generated-custom-cert
	openssl req -new -sha256 -key generated-custom-cert/custom.key -subj "/C=FR/ST=Paris/O=VMware Inc./CN=spring-boot-custom-cert.demos.tanzu.vmware.com" -out generated-custom-cert/custom.csr

generated-custom-cert/custom.crt: generated-custom-cert/custom.csr generated-custom-cert/root-ca.crt generated-custom-cert/root-ca.key
	mkdir -p generated-custom-cert
	openssl x509 -req -in generated-custom-cert/custom.csr -CA generated-custom-cert/root-ca.crt -CAkey generated-custom-cert/root-ca.key -CAcreateserial -out generated-custom-cert/custom.crt -days 500 -sha256
