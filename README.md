# Adding a custom certificate to a Spring Boot app

This project shows how to add a custom certificate to a Spring Boot application,
leveraging the
[Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
and [Paketo Buildpacks](https://paketo.io/).

In fact, we rely on the [Paketo Buildpack for CA Certificates](https://github.com/paketo-buildpacks/ca-certificates)
to embed a custom certificate at build time.

## How does it work?

A custom certificate is available in the `bindings/custom-cert` directory.

You may generate your own certificate using this command:

```shell
rm -rf bindings && make create-bindings
```

When building a container image with Spring Boot, this directory is mounted
as a [binding volume](https://paketo.io/docs/howto/configuration/#bindings)
of type `ca-certificates`. The buildpack will embed certificates in the
resulting image.

This behavior is enabled with this Spring Boot Maven plugin configuration:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <!-- The custom certificate will be added to the resulting container image. -->
            <env>
                <BP_EMBED_CERTS>true</BP_EMBED_CERTS>
            </env>
            <bindings>
                <binding>${project.basedir}/bindings:/platform/bindings</binding>
            </bindings>
        </image>
    </configuration>
</plugin>
```

Build the container image:

```shell
./mvnw spring-boot:build-image
```

If you look at the logs, you will find this output from Paketo Buildpack for CA Certificates:

```
[INFO]     [creator]     Paketo Buildpack for CA Certificates 3.5.0
[INFO]     [creator]       https://github.com/paketo-buildpacks/ca-certificates
[INFO]     [creator]       Launch Helper: Reusing cached layer
[INFO]     [creator]       CA Certificates: Contributing to layer
[INFO]     [creator]         Embedding CA certificate(s)
[INFO]     [creator]         Added 1 additional CA certificate(s) to system truststore
[INFO]     [creator]         Writing env.build/SSL_CERT_DIR.append
[INFO]     [creator]         Writing env.build/SSL_CERT_DIR.delim
[INFO]     [creator]         Writing env.build/SSL_CERT_FILE.default
[INFO]     [creator]         Writing env.launch/SSL_CERT_DIR.append
[INFO]     [creator]         Writing env.launch/SSL_CERT_DIR.delim
[INFO]     [creator]         Writing env.launch/SSL_CERT_FILE.default
```

Now run the app using the container image:

```shell
docker run --rm -p 8080:8080 ghcr.io/alexandreroman/spring-boot-custom-cert
```

The custom certificate will be added at runtime to the JVM truststore.

Use the endpoint `/cert` from the app to see the custom certificate:

```shell
curl http://localhost:8080/cert
[
[
  Version: V1
  Subject: CN=spring-boot-custom-cert.demos.tanzu.vmware.com, O=VMware Inc., ST=Paris, C=FR
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11

  Key:  Sun RSA public key, 2048 bits
  params: null
  modulus: 27843965317473272092956771915280720248339162693696328044170439225313752177826001668141677240534704602742896854064839171143552365840260050735449868807124371738826567735567721780351308600726758818201864302994937086642646213352773646443081339841807025510735451045652180693586971541326952774043134554093948166480046888570845592190487433857657957274481751192689917840753543239987907653243987800615964549699618085667345596794936835691783265732918336822543882783347659377825162266513679592306445407212837251526998642452772005302137426634033681431151132696502439009730937196458423779721778498753528945875607153645980082807313
  public exponent: 65537
  Validity: [From: Sat Nov 26 16:35:04 UTC 2022,
               To: Tue Apr 09 16:35:04 UTC 2024]
  Issuer: CN=*.demos.tanzu.vmware.com, O=VMware Inc., ST=Paris, C=FR
  SerialNumber: [    df15cbb7 b1313443]

]
  Algorithm: [SHA256withRSA]
  Signature:
0000: 7F EA FD 01 D0 2E 72 8E   2E D7 7B 63 09 E9 F8 2C  ......r....c...,
0010: 0F 04 A9 1C FD 92 2B CB   6B 18 6E E6 54 1D AE FD  ......+.k.n.T...
0020: 60 FD 9A 25 CD EC B5 E3   9D 5E 73 5F 37 20 8D F0  `..%.....^s_7 ..
0030: 22 01 EA 99 C7 BC 9E 7C   B1 7D 07 F9 F2 47 CD 29  "............G.)
0040: 3A 4D 8E C3 54 51 3B 1F   01 B9 9D E8 A4 52 B2 56  :M..TQ;......R.V
0050: 20 49 80 DC 24 A8 83 DD   63 01 AF 01 23 DF 19 96   I..$...c...#...
0060: 0A D3 39 8C DF 4D E4 53   62 D9 66 60 44 AF 15 AB  ..9..M.Sb.f`D...
0070: BC 1B 4B 6A 54 23 B6 4D   F9 94 5D 68 92 15 0C 7C  ..KjT#.M..]h....
0080: F4 6A DC 75 CB FE AC 68   56 2A E2 BA F6 29 3A B8  .j.u...hV*...):.
0090: A7 BA E3 93 15 F6 53 2C   DC E1 D7 7C 0C 84 D9 76  ......S,.......v
00A0: 82 4B 9D 79 CA 53 E4 D1   F3 C5 0F F6 AE 67 06 AD  .K.y.S.......g..
00B0: E1 C0 F3 6F 82 82 19 69   7F D4 DA 5F B8 8F F0 4E  ...o...i..._...N
00C0: D0 50 0E 15 D4 54 96 60   9E 16 18 DD 4F 76 F5 22  .P...T.`....Ov."
00D0: 65 BF 51 7E 79 A3 98 AE   67 CE 91 7B 71 41 6D 21  e.Q.y...g...qAm!
00E0: F7 6C 6C C0 09 1F 07 8B   F5 D4 68 84 D8 96 FA 4A  .ll.......h....J
00F0: 91 AD B5 36 B5 45 96 2A   B5 72 63 11 61 01 40 1F  ...6.E.*.rc.a.@.
0100: 00 80 EB 40 C6 8D FD 7F   3B D2 2B 8B C0 63 6C 34  ...@....;.+..cl4
0110: 7A 8E 32 35 BB 8E 62 6F   23 CA F5 13 A7 57 A7 D3  z.25..bo#....W..
0120: 55 0F 7C 7F 2B 78 23 4F   8C C2 88 27 CD 9B E0 54  U...+x#O...'...T
0130: 0C ED BC 9D C5 FC 16 0B   98 D7 50 58 48 FD 53 2F  ..........PXH.S/
0140: 65 BD 11 69 96 9C 53 C0   56 06 0A 1E 08 7B 31 9B  e..i..S.V.....1.
0150: 25 4E CC DC 5B 12 61 0D   A3 51 A1 92 FB 19 45 D6  %N..[.a..Q....E.
0160: 92 48 08 66 9B 5B 93 5D   2B A1 FC 01 A2 57 97 10  .H.f.[.]+....W..
0170: 35 50 9B 4A 49 7C 95 4A   21 73 96 5D E1 37 A2 8F  5P.JI..J!s.].7..
0180: 93 67 BE 36 02 06 BA 22   7C F4 80 C4 E8 73 09 88  .g.6...".....s..
0190: 40 6D D6 66 04 1A D4 40   AF C7 2A 90 DE C7 A7 FF  @m.f...@..*.....
01A0: C3 E6 DA 84 E1 8A 0D 00   81 F2 17 8E 7D 55 09 D2  .............U..
01B0: B2 2B 55 CE 7C 9C 68 DF   E3 D1 F0 9C 01 C6 E4 E9  .+U...h.........
01C0: 8D B8 6E 6B C6 18 B8 5E   9B 6B 2F 02 7B B4 79 71  ..nk...^.k/...yq
01D0: F4 9E 57 F4 C1 EE 21 44   90 83 B1 92 EA D9 1F FA  ..W...!D........
01E0: BD 39 66 09 90 71 84 CE   B3 6A 23 A8 10 DF 10 EA  .9f..q...j#.....
01F0: 9C 07 34 30 BE C0 8E E6   5B 7D 2A 9A 19 AA 22 A8  ..40....[.*...".

]%
```

Pretty cool isn't it?

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2022 [VMware, Inc. or its affiliates](https://vmware.com).

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
