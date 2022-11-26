/*
 * Copyright (c) 2022 VMware, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vmware.tanzu.demos.sbcert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class IndexController {
    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String index() {
        return "Go to /cert to see the custom certificate";
    }
}

@RestController
class CustomCertController {
    @GetMapping(value = "cert", produces = MediaType.TEXT_PLAIN_VALUE)
    String cert() throws KeyStoreException, NoSuchAlgorithmException {
        final var trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        final var x509Certificates = new ArrayList<X509Certificate>();
        trustManagerFactory.init((KeyStore) null);
        Arrays.asList(trustManagerFactory.getTrustManagers()).stream().forEach(t -> {
            x509Certificates.addAll(Arrays.asList(((X509TrustManager) t).getAcceptedIssuers()));
        });
        return x509Certificates.stream()
                .filter(c -> c.getSubjectX500Principal().getName().contains("spring-boot-custom-cert"))
                .map(c -> c.toString())
                .collect(Collectors.joining("\n"));
    }
}
