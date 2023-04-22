# kotlin-spring-data-rest-movies

Demo project to showcase how to build a REST HAL API application using:

- Kotlin
- Gradle
- Spring Boot 3
- JPA
- Flyway
- PostgreSQL
- Docker & Testcontainers

----

## Requirements

The application requires JDK 17 at least on a GraalVM distribution.

````shell
$ sdk install java 22.3.r17-grl
$ sdk use java 22.3.r17-grl
````
----

## Build and test

````
./gradlew build
````
----
## Run the application

### JDK version

````
./gradlew bootRun
````

*Note: by default requires Postgres to run locally on port 5431.*

### Native version

````
./gradlew nativeRun
````

*Note: by default requires Postgres to run locally on port 5431.*


### Docker

````
./gradlew bootBuildImage && docker-compose -f src/main/docker/docker-compose.yml up
````