# kotlin-spring-data-rest-movies

Demo project to showcase how to build a REST HAL API application using:

- Kotlin
- Gradle
- Spring Boot 3
- Querydsl
- JPA
- Flyway
- PostgreSQL
- Docker & Testcontainers

Articles that were written based in this project, in order:

- [Building and testing a REST HAL API using Kotlin, Spring Data REST and JPA](https://aregall.tech/building-and-testing-a-rest-hal-api-using-kotlin-spring-data-rest-and-jpa)
- [Integration between Querydsl and Spring Data REST using Kotlin, Gradle and Spring Boot 3](https://aregall.tech/integration-between-querydsl-and-spring-data-rest-using-kotlin-gradle-and-spring-boot-3)

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

*Note: by default requires Postgres to run locally on port 5432.*

### Native version

````
./gradlew nativeRun
````

*Note: by default requires Postgres to run locally on port 5432.*


### Docker

````
./gradlew bootBuildImage && docker-compose -f src/main/docker/docker-compose.yml up
````