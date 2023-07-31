# *Spring Kafka Exactly Once Processing*

<p id="top"></p>

## Overview

A Spring Kafka application for processing Foo messages into Bar messages. The system uses transactions for exactly-once processing semantics 
and has an effective retry mechanism which comes with a micrometer counter for messages sent to the DLQ. 

The Test classes use an embedded Kafka Broker for easy local development and has utilities for producing and consuming from the local application instance.

<!-- GETTING STARTED -->


## Interfaces

| Client | Type                   | Description                                          | 
|--------|------------------------|------------------------------------------------------|
| Kafka  | Topic Consumer         | consumes message from 'input-topic'                  |
| Kafka  | Topic ErrorHandler     | send erroneous message to 'dlq-topic'                |
| Kafka  | Topic Producer         | send processed messages to 'output-topic'            |
| Kafka  | Consumer Group         | exactly-once-message-processing-application-grp      |
| Web    | Actuator REST Endpoint | actuator/health, */info, */env */metrics/dlq.counter |

## Getting Started

*This section will guide the user through proper installation and usage.*

### Prerequisites

* Java 17
* Gradle >= 7.4
* Docker (optional)
* [Kafka](https://kafka.apache.org/quickstart)

```sh
$ brew install gradl 7.4
```

### Installation

*This section will guide the user through the download and installation process.*

Example:

1. **Clone the Repo**

```sh
git clone git@github.com:tom-craven/exactly-once-processing.git
```

2. **Build via gradle**

   Execute the following command from the parent directory:

```sh
./gradlew build
```

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->

## Usage

### Running

**Start the service**

Execute the following command from the parent directory:

```sh
./gradlew bootRun
```

### Testing

*Provided test cases*

1. **Dead letter queue test**
   Sending erroneous messages to the deal letter queue
2. **Error handler test**
   Retrying messages in the event of a retryable error, with exponential backoff in the event a dependant service is down. 
   
3. **Prometheus test**
   Incrementing the dlq counter found at the /actuator/metrics/dlq.counter endpoint
4. **Message processing application test**
   Happy path and message validation

```sh
./gradlew test
```

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- Built With -->

### Work with  Docker

Modify the kafka config/server.properties adding your IP address as an advertised listener. This will make the local
Kafka instance visible to the dockerized client.

```sh
advertised.listeners=PLAINTEXT://<local-machine-ip>:9092
```

3. **Build the Docker**

```sh
docker build -t message-processing-application .
```

2. **Run the Docker**

If you are developing docker locally you can add "sleep infinity" to pause the docker after it starts and attach to
inspect it.

```sh
export BOOTSTRAP_SERVERS=<local-machine-ip>:9092
docker run --name message-processing-application --env BOOTSTRAP_SERVERS -d message-processing-application .
```

### Tear Down

Execute the following command from the parent directory:

```sh
docker stop message-processing-application
docker rm message-processing-application

./gradlew clean
```

## Built With

_Will provide the user with a list of sources and documentation used when building the project_

Example:

For further reference, please consider the following:

* [Spring Boot](https://docs.spring.io/spring-boot/docs/2.6.3/reference/html/)
* [Gradle](https://docs.gradle.org/current/userguide/userguide.html)
* [Docker](https://docs.docker.com/)
* [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
* [Spring Actuator](https://docs.spring.io/spring-boot/docs/2.6.3/actuator-api/htmlsingle/)

<p align="right">(<a href="#top">back to top</a>)</p>
