# Producer

## Running

```
mvn clean install
java -jar -Dserver.port=8080 target/producer-1.0-SNAPSHOT.jar
```

## General information

Simple spring boot application. Sends data to RabbitMq store queue.
