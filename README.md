## HTTP2 setup with Jetty 9

Servlets are server code are copied from [Jetty examples](http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/embedded/src/main/java/org/eclipse/jetty/embedded)

#### Build 

```
mvn clean package
```

#### Run

See [jetty notes](http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-starting) (in particular the "Versions" section) on JDK to alpn-boot.jar version matching

Make sure your JDK version corresponds to alpn-boot version.

```
 $ java -version
java version "1.8.0_51"
Java(TM) SE Runtime Environment (build 1.8.0_51-b16)
Java HotSpot(TM) 64-Bit Server VM (build 25.51-b03, mixed mode)
```

Run with alpn-boot. In my case alpn-boot-8.1.4.v20150727.jar was downloaded earlier by maven, so jar from maven repo is used.

```
 $ java -Xbootclasspath/p:`ls ~/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.4.v20150727/alpn-boot-8.1.4.v20150727.jar` -jar ./target/asyncservlet-0.0.1-SNAPSHOT-uberjar.jar 

```


#### Test

Blindly accepting self-signed certificate with `-k` option.


```
curl -vk --http2 -d 'sdfasdfasfdasdfasdddddddddddddddddddddddddddddddddddd'   https://localhost:8443/hello
```