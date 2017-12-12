FROM maven:3.5-jdk-8-alpine

WORKDIR      /consumer
ADD     .    /consumer

RUN mkdir -p /consumer/filestore

RUN mvn clean package -D "maven.test.skip=true"
RUN cp target/consumer-1.0-SNAPSHOT.jar /consumer/consumer-1.0.0.jar

CMD java -jar -Dserver.port=$PORT /consumer/consumer-1.0.0.jar
