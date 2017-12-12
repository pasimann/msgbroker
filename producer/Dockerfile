FROM maven:3.5-jdk-8-alpine

WORKDIR      /server
ADD     .    /server

RUN mvn clean package -D "maven.test.skip=true"
RUN cp target/producer-1.0-SNAPSHOT.jar /server/producer-1.0.0.jar

CMD java -jar -Dserver.port=$PORT /server/producer-1.0.0.jar