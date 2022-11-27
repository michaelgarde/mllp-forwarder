# mllp-forwarder

This project is meant as an HL7 testing tool for testing communication with mllp and http endpoints.

Once running it serves 4 purposes:

1. Exposing a rest endpoint for forwarding HL7 messages to MLLP
2. Exposing a rest endpoint for forwarding HL7 messages to HTTP
3. Exposing an HL7 MLLP consumer dummy endpoint
4. Exposing an HL7 HTTP consumer dummy endpoint 

The first two allows you to curl or setup a [Postman](https://www.postman.com/downloads/) [collection](https://learning.postman.com/docs/getting-started/creating-the-first-collection/), or any other tool you might like.
The last two acts as consumers for MLLP and HTTP and are useful for local development, when you need local receiving endpoints for testing. 

## How to run

There are several options for running and using this project

1. Run locally in Quarkus dev mode. See [Running the application in dev mode](#running-the-application-in-dev-mode)
   1. This requires Maven and Java installed
1. Run locally as a docker container using docker-compose
   1. Build as described in [Packaging and running the application](#packaging-and-running-the-application) 
      1. This still requires Maven and Java installed
   2. Build the docker image using `docker-compose build`
   3. Run the [docker compose](docker-compose.yml) file using `docker-compose up`

## How to use

### Forwarding messages

> **_NOTE:_**  The remote systems are provided by url parameters. This avoids setting them up as environment variables.

Forwarding is exposed on port 8080. The type of forwarder to use is given by the url path:

* `localhost:8080/mllp/forward` or
* `localhost:8080/http/forward`

The mandatory `receiving-system` and `receiving-system-port` tells the application where to forward the message.
If successful, the Application Accept (AA) from the receiving system should be returned.
An Application Error message should be returned, if there are issues with the receiving system.

See the examples below for usage.

### Forwarding HL7 messages to an mllp endpoint

```curl
curl --location --request POST 'localhost:8080/mllp/forward?receiving-system=localhost&receiving-system-port=57911' \
--header 'Content-Type: text/plain' \
--data-raw 'MSH|^~\&|||||20221011134017.828+0200||ADT^A14^ADT_A05|2901|T|2.5.1'
```

The above curl can also be imported to Postman, which i would highly recommend.

### Forwarding HL7 messages to an http endpoint

This feature is kind of redundant, since you might as well send the messages directly to the HL7 http service, however, 
this application performs some mild HL7 validation by unmarshalling using [HL7DataFormat](https://javadoc.io/doc/org.apache.camel/camel-hl7/2.13.3/org/apache/camel/component/hl7/HL7DataFormat.html) 
and converts Line Feeds (LF) to Carriage Returns (CR).

```curl
curl --location --request POST 'localhost:8080/http/forward?receiving-system=localhost&receiving-system-port=52087' \
--header 'Content-Type: text/plain' \
--data-raw 'MSH|^~\&|||||20221011134017.828+0200||ADT^A14^ADT_A05|2901|T|2.5.1'
```

The above curl can also be imported to Postman, which i would highly recommend.

### OpenAPI and Swagger UI

A swagger UI is also available at [http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/)
providing a quick and dirty web interface for sending messages.





# Quarkus Readme

This project uses Quarkus and Camel.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Related Guides

- Camel Log ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/log.html)): Log messages to the underlying logging mechanism
- Camel Core ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html)): Camel core functionality and basic Camel languages: Constant, ExchangeProperty, Header, Ref, Simple and Tokenize
- Camel MLLP ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/mllp.html)): Communicate with external systems using the MLLP protocol
- Camel Direct ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/direct.html)): Call another endpoint from the same Camel Context synchronously
- Camel HL7 ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/hl7.html)): Marshal and unmarshal HL7 (Health Care) model objects using the HL7 MLLP codec
- Camel Rest ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/rest.html)): Expose REST services and their OpenAPI Specification or call external REST services
